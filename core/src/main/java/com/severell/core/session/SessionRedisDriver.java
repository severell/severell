package com.severell.core.session;

import com.severell.core.http.Cookie;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.io.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;

public class SessionRedisDriver extends BaseSessionDriver implements Session, HttpSession, RemoteSession {

    private HashMap<String, Object> session;

    @Override
    public Object get(String key) {
        return session.get(key);
    }

    private String getActualKey() {
        String actualKey = "";
        Cookie jsessCookie = request.cookie("JSESSIONID");
        if(jsessCookie != null) {
            actualKey += "session-" + jsessCookie.getValue();
        } else {
//            String sessionId = request.().getId();
//            actualKey += "session-" + sessionId;
        }
        return actualKey;
    }

    @Override
    public String getString(String key) {
        Object val = get(key);
        return val == null ? null : (String) val;
    }

    @Override
    public <T> T get(String key, Class<T> c) {
        Object val = get(key);
        return val == null ? null : (T) val;
    }

    @Override
    public String getId() {
        Cookie jsessCookie = request.cookie("JSESSIONID");
        return jsessCookie.getValue();
    }

    @Override
    public void put(String key, Object value) {
       session.put(key, value);
    }


    //HTTPSession Methods
    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {

    }

    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        //Deprecated
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return get(key);
    }

    @Override
    public Object getValue(String s) {
        //Deprecated
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String[] getValueNames() {
        //Deprecated
        return new String[0];
    }

    @Override
    public void setAttribute(String s, Object o) {
        put(s, o);
    }

    @Override
    public void putValue(String s, Object o) {
        //Deprecated
    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public void removeValue(String s) {
        //Deprecated
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public void getRemoteSession() {
        this.session = new HashMap<>();
        String key = getActualKey();
        JedisPool pool = container.make(JedisPool.class);
        ObjectInputStream objectInputStream = null;
        try(Jedis jedis = pool.getResource()) {
            if(jedis.exists(key)) {
                String data = jedis.get(key);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                objectInputStream
                        = new ObjectInputStream(inputStream);
                this.session = (HashMap<String, Object>) objectInputStream.readObject();
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
}
