package ${package};

import com.severell.core.http.MiddlewareInclude;
import com.severell.core.middleware.CsrfMiddleware;
import com.severell.core.middleware.SecureHeadersMiddleware;
import com.severell.core.middleware.SessionMiddleware;

@MiddlewareInclude
public class Middleware {

    // Runs in this order. Secure headers come before CSRF so a refused request still gets them.
    SessionMiddleware sessionMiddleware;
    SecureHeadersMiddleware secureHeadersMiddleware;
    CsrfMiddleware csrfMiddleware;

}
