package ${package};

import com.severell.core.middleware.CsrfMiddleware;
import com.severell.core.middleware.SecureHeadersMiddleware;

public class Middleware {

    public static final Class[] MIDDLEWARE = new Class[]{
            CsrfMiddleware.class,
            SecureHeadersMiddleware.class,
    };
}
