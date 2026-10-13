package com.severell.core.server;

import com.severell.core.container.Container;
import com.severell.core.http.Cookie;
import com.severell.core.http.Request;
import com.severell.core.session.RemoteSession;
import com.severell.core.session.Session;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UndertowRedisSession extends UndertowInMemorySession implements Session, RemoteSession {

    public UndertowRedisSession(io.undertow.server.session.Session session) {
        super(session);
    }

    public UndertowRedisSession(Container container) {
        super(container);
    }

    // The session id is the cookie's id when that session exists (see
    // SeverellSessionCookieConfig), and the new id after regenerate() or reset().
    private String getActualKey() {
        return key(undertowSession.getId());
    }

    static String key(String sessionId) {
        return "session-" + sessionId;
    }

    public void getRemoteSession() {
        String key = getActualKey();
        JedisPool pool = container.make(JedisPool.class);
        ObjectInputStream objectInputStream = null;
        try(Jedis jedis = pool.getResource()) {
            if(jedis.exists(key)) {
                String data = jedis.get(key);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                objectInputStream
                        = new ObjectInputStream(inputStream);
                HashMap<String, Object> session = (HashMap<String, Object>) objectInputStream.readObject();
                for(Map.Entry<String, Object> map : session.entrySet()) {
                    undertowSession.setAttribute(map.getKey(), map.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateRemoteSession() {
        String key = getActualKey();
        JedisPool pool = container.make(JedisPool.class);

        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try(Jedis jedis = pool.getResource()) {
            objectOutputStream = new ObjectOutputStream(writer);
            HashMap<String, Object> session = new HashMap<>();
            Iterator<String> iterator;
            try {
                iterator = undertowSession.getAttributeNames().iterator();
            } catch (IllegalStateException e) {
                // Another request replaced this session (sign-in or sign-out); don't bring it back.
                return;
            }
            while(iterator.hasNext()) {
                String sessKey = iterator.next();
                session.put(sessKey, undertowSession.getAttribute(sessKey));
            }
            objectOutputStream.writeObject(session);

            jedis.set(key, Base64.getEncoder().encodeToString(writer.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(objectOutputStream != null) {
                try {
                    objectOutputStream.flush();
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setRequest(Request r) {
        request = r;
    }

    @Override
    protected void forgetSession(String oldId) {
        // Delete the old copy before the new session is created, so the old id is unknown from
        // now on and can't be used to load the data.
        JedisPool pool = container.make(JedisPool.class);
        try(Jedis jedis = pool.getResource()) {
            jedis.del(key(oldId));
        }
    }
}
