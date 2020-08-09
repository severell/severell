package com.mitchdennett.framework.servlet;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;
import com.mitchdennett.framework.http.Router;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BasicServlet extends HttpServlet {

    private final Container c;
    private final Router router;

    public BasicServlet(Container c, Router router) {
        this.c = c;
        this.router = router;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.doRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.doRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, InvocationTargetException, IllegalAccessException {
            Request req = new Request(request);
            Method ref = router.lookup(request.getRequestURI(), HttpMethod.fromString(request.getMethod()), req);

            if(ref != null) {
                Class[] params = ref.getParameterTypes();
                ArrayList<Object> paramsToPass = new ArrayList<Object>(params.length);
                for (Class p : params) {
                    if (p != Request.class && p != Response.class) {
                        Object obj = c.make(p);
                        paramsToPass.add(p.cast(obj));
                    } else {
                        if (p == Request.class) {
                            paramsToPass.add(req);
                        } else {
                            paramsToPass.add(new Response(response));
                        }
                    }
                }

                ref.invoke(null, paramsToPass.toArray());
            } else {
                throw new ServletException("Not Found");
            }

    }
}
