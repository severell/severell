package com.severell.core.server;

import com.severell.core.container.Container;
import com.severell.core.http.Cookie;
import com.severell.core.http.Request;
import com.severell.core.session.RemoteSession;
import com.severell.core.session.Session;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
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

    // Whether the session is new in this request: the browser didn't send its id (a first
    // visit), or regenerate()/reset() made it. Only a new session may be created in Redis.
    private boolean createdHere;
    // The browser sent this session's id but it was gone from Redis by the time it was loaded:
    // it was signed out or replaced meanwhile, so it must not be written back.
    private boolean ended;

    public void getRemoteSession() {
        String key = getActualKey();
        io.undertow.server.handlers.Cookie cookie = exchange == null ? null : exchange.getRequestCookies().get("JSESSIONID");
        createdHere = cookie == null || !undertowSession.getId().equals(cookie.getValue());
        JedisPool pool = container.make(JedisPool.class);
        ObjectInputStream objectInputStream = null;
        try(Jedis jedis = pool.getResource()) {
            String data = jedis.get(key);
            ended = data == null && !createdHere;
            if(data != null) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                objectInputStream
                        = new ObjectInputStream(inputStream);
                HashMap<String, Object> session = (HashMap<String, Object>) objectInputStream.readObject();
                // Redis has the current data; drop anything this server's copy has that it doesn't.
                for(String name : new HashSet<>(undertowSession.getAttributeNames())) {
                    if(!session.containsKey(name)) {
                        undertowSession.removeAttribute(name);
                    }
                }
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
        if (ended && !createdHere) {
            return;
        }
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

            String value = Base64.getEncoder().encodeToString(writer.toByteArray());
            // XX: only update a session that still exists, so a write can't revive one that was
            // deleted while this request ran. NX: a session created here is new to Redis.
            jedis.set(key, value, createdHere ? SetParams.setParams().nx() : SetParams.setParams().xx());
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
        createdHere = true;
        // Delete the old copy before the new session is created, so the old id is unknown from
        // now on and can't be used to load the data.
        JedisPool pool = container.make(JedisPool.class);
        try(Jedis jedis = pool.getResource()) {
            jedis.del(key(oldId));
        }
    }
}
