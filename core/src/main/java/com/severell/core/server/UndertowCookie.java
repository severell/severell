package com.severell.core.server;

import com.severell.core.http.Cookie;
import io.undertow.UndertowLogger;
import io.undertow.UndertowMessages;

import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.CookieSameSiteMode;

import java.util.Arrays;
import java.util.Date;

public class UndertowCookie implements Cookie{

    private final String name;
    private String value;
    private String path;
    private String domain;
    private Integer maxAge;
    private Date expires;
    private boolean discard;
    private boolean secure;
    private boolean httpOnly;
    private int version = 0;
    private String comment;
    private boolean sameSite;
    private String sameSiteMode;

    public UndertowCookie(CookieImpl cookie) {
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.path = cookie.getPath();
        this.domain = cookie.getDomain();
        this.maxAge = cookie.getMaxAge();
        this.expires = cookie.getExpires();
        this.discard = cookie.isDiscard();
        this.secure = cookie.isSecure();
        this.httpOnly = cookie.isHttpOnly();
        this.version = cookie.getVersion();
        this.comment = cookie.getComment();
        this.sameSite = cookie.isSameSite();
        this.sameSiteMode = cookie.getSameSiteMode();
    }

    public UndertowCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public UndertowCookie(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public Cookie setValue(String value) {
        this.value = value;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public Cookie setPath(String path) {
        this.path = path;
        return this;
    }

    public String getDomain() {
        return this.domain;
    }

    public Cookie setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public Integer getMaxAge() {
        return this.maxAge;
    }

    public Cookie setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isDiscard() {
        return this.discard;
    }

    public Cookie setDiscard(boolean discard) {
        this.discard = discard;
        return this;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public Cookie setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public int getVersion() {
        return this.version;
    }

    public Cookie setVersion(int version) {
        this.version = version;
        return this;
    }

    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    public Cookie setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public Date getExpires() {
        return this.expires;
    }

    public Cookie setExpires(Date expires) {
        this.expires = expires;
        return this;
    }

    public String getComment() {
        return this.comment;
    }

    public Cookie setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean isSameSite() {
        return this.sameSite;
    }

    public Cookie setSameSite(boolean sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    public String getSameSiteMode() {
        return this.sameSiteMode;
    }

    public Cookie setSameSiteMode(String mode) {
        String m = CookieSameSiteMode.lookupModeString(mode);
        if (m != null) {
            UndertowLogger.REQUEST_LOGGER.tracef("Setting SameSite mode to [%s] for cookie [%s]", m, this.name);
            this.sameSiteMode = m;
            this.setSameSite(true);
        } else {
            UndertowLogger.REQUEST_LOGGER.warnf(UndertowMessages.MESSAGES.invalidSameSiteMode(mode, Arrays.toString(CookieSameSiteMode.values())), "Ignoring specified SameSite mode [%s] for cookie [%s]", mode, this.name);
        }

        return this;
    }
}
