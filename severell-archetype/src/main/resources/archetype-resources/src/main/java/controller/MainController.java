package ${package}.controller;

import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

import java.io.IOException;
import java.util.HashMap;

public class MainController {

    public void index(Request request, Response resp) throws IOException, ViewException {
        resp.render("index.mustache", new HashMap<String, Object>());
    }

}
