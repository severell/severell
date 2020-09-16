package ${package}.routes;

import com.severell.core.http.Router;

public class Routes {

    public static void init() throws NoSuchMethodException, ClassNotFoundException {
        Router.Get("/", "${package}.controller.MainController::index");
        Router.Get("/register", "${package}.controller.auth.AuthController::register");
        Router.Get("/login", "${package}.controller.auth.AuthController::login");
        Router.Post("/login", "${package}.controller.auth.AuthController::loginPost");
        Router.Post("/register", "${package}.controller.auth.AuthController::registerPost");
    }
}
