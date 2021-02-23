package ${package}.controller.auth;

import ${package}.auth.Auth;
import com.severell.core.crypto.PasswordUtils;
import com.severell.core.session.Session;
import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import com.severell.core.http.HttpMethod;
import com.severell.core.http.Route;
import ${package}.models.User;

import java.io.IOException;
import java.util.HashMap;

public class AuthController {

    @Route(path = "/register", method = HttpMethod.GET)
    public void register(Request request, Response resp) throws IOException, ViewException {
        resp.render("auth/register.mustache", new HashMap<String, Object>());
    }

    @Route(path = "/login", method = HttpMethod.GET)
    public void login(Request request, Response resp, Session session, Auth auth) throws IOException, ViewException {
        resp.render("auth/login.mustache", new HashMap<String, Object>());
    }

    @Route(path = "/login", method = HttpMethod.POST)
    public void loginPost(Request req, Auth auth, Response resp) throws IOException {
        if(auth.login(req.input("email"), req.input("password"))){
            resp.redirect("/");
        } else {
            resp.redirect("/login");
        }
    }

    @Route(path = "/register", method = HttpMethod.POST)
    public void registerPost(Request req, Auth auth, Response resp) throws IOException {
        System.out.println();
        System.out.println(PasswordUtils.hashPassword(req.input("password")));

        User user = new User();
        user.setEmail(req.input("email"));
        user.setPassword(PasswordUtils.hashPassword(req.input("password")));
        user.setName(req.input("email"));
        user.save();

        auth.login(req.input("email"), req.input("password"));

        resp.redirect("/");
    }
	
}