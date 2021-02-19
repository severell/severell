package ${package}.middleware;

import com.severell.core.session.Session;
import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import com.severell.core.middleware.Middleware;

public class AuthenticationMiddleware implements Middleware {

    private Session session;

    public AuthenticationMiddleware(Session session) {
        this.session = session;
    }

    @Override
    public void handle(Request request, Response response, MiddlewareChain middlewareChain) throws Exception {
        if(session.get("userid") == null) {
            response.redirect("/login");
        } else {
            middlewareChain.next();
        }
    }
}
