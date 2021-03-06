package com.severell.core.http;

import java.util.Date;

public interface Cookie {
    String getName();

    String getValue();

    Cookie setValue(String var1);

    String getPath();

    Cookie setPath(String var1);

    String getDomain();

    Cookie setDomain(String var1);

    Integer getMaxAge();

    Cookie setMaxAge(Integer var1);

    boolean isDiscard();

    Cookie setDiscard(boolean var1);

    boolean isSecure();

    Cookie setSecure(boolean var1);

    int getVersion();

    Cookie setVersion(int var1);

    boolean isHttpOnly();

    Cookie setHttpOnly(boolean var1);

    Date getExpires();

    Cookie setExpires(Date var1);

    String getComment();

    Cookie setComment(String var1);

    default boolean isSameSite() {
        return false;
    }

    default Cookie setSameSite(boolean sameSite) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default String getSameSiteMode() {
        return null;
    }

    default Cookie setSameSiteMode(String mode) {
        throw new UnsupportedOperationException("Not implemented");
    }
}