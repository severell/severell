package com.severell.core.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Turns off {@link com.severell.core.middleware.CsrfMiddleware} for a route, or for every route
 * in a controller when put on the class.
 *
 * Only use it on routes that browsers can't be tricked into calling with the user's cookies:
 * routes authenticated by a bearer token or API key, or routes with no authentication at all.
 * Never use it on a route that relies on the session cookie.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SkipCsrf {
}
