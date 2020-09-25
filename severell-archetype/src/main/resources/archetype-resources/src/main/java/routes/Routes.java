package ${package}.routes;

import com.severell.core.http.Router;
import ${package}.controller.MainController;
import ${package}.controller.AuthController;

public class Routes {

    public static void init() throws NoSuchMethodException, ClassNotFoundException {
        Router.Get("/", MainController.class, "index");
        Router.Get("/register", AuthController.class, "register");
        Router.Get("/login", AuthController.class, "login");
        Router.Post("/login", AuthController.class, "loginPost");
        Router.Post("/register", AuthController.class, "registerPost");
    }
}
