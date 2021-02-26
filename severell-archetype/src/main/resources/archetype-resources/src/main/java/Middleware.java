package ${package};

import com.severell.core.http.MiddlewareInclude;
import com.severell.core.middleware.CsrfMiddleware;
import com.severell.core.middleware.SecureHeadersMiddleware;
import com.severell.core.middleware.SessionMiddleware;

@MiddlewareInclude
public class Middleware {

    SessionMiddleware sessionMiddleware;
    CsrfMiddleware csrfMiddleware;
    SecureHeadersMiddleware secureHeadersMiddleware;

}
