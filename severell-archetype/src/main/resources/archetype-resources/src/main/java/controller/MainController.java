package ${package}.controller;

import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import com.severell.core.http.HttpMethod;
import com.severell.core.http.Route;

import java.io.IOException;
import java.util.HashMap;

public class MainController {

    @Route(path = "/", method = HttpMethod.GET)
    public void index(Request request, Response resp) throws IOException, ViewException {
        resp.render("index.jte", new HashMap<String, Object>());
    }

}
