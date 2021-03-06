package com.severell.core.http;

import com.severell.core.container.Container;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouterTest {

    public static void index() {

    }

    public static void post() {

    }

    private static RequestOld req;
    private static Container c;
    private static ServletContextHandler context;
    private static ArrayList<RouteExecutor> routes = new ArrayList<>();
    private static Method method;



    @BeforeEach
    public void setUp() throws NoSuchMethodException, ClassNotFoundException {
        Router.clearRoutes();
        req = mock(RequestOld.class);
        context = mock(ServletContextHandler.class);
        c = mock(Container.class);
        routes =  new ArrayList<>();

        routes.add(new RouteExecutor("/user_:name", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/", HttpMethod.POST, ((request, response, container) -> {
            post();
        })));

        routes.add(new RouteExecutor("/page/:id/", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/user/:id", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/user/:id/:name", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/blog/*files", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/searching", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/search", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/blogging", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/post/:id/settings", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        routes.add(new RouteExecutor("/house/:id/*homes", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));

        Router.setCompiledRoutes(routes);

        method = RouterTest.class.getMethod("index");

    }

    @Test
    public void routerReturnsCorrectHandle() throws Exception {
        String path = "/";
        Method meth = Class.forName("com.severell.core.http.RouterTest").getDeclaredMethod("index");

        Router router = new Router();
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);
        assertEquals(route.getPath(), path);
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerReturnsCorrectRouteForPost() throws Exception {
        String path = "/";
        Method meth = Class.forName("com.severell.core.http.RouterTest").getDeclaredMethod("post");

        Router router = new Router();
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "POST", req);
        assertEquals(route.getPath(), path);
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerHandlesNamedParam() throws Exception {
        String id = "12";
        String path = "/user/" + id;

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        Router router = new Router();
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);
        verify(req).addParam(key.capture(), val.capture());
        assertEquals("/user/:id",route.getPath() );
//        assertEquals(route.getMethod(), meth);
        assertEquals("id", key.getValue());
        assertEquals(id, val.getValue());
    }

    @Test
    public void routerGetCorrectNamedParamWithTrailingSlash() throws Exception {
        String id = "12";
        String path = "/user/" + id + "/";

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        Router router = new Router();
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);
        verify(req).addParam(key.capture(), val.capture());

        assertEquals("/user/:id/:name",route.getPath() );
        assertEquals("id", key.getValue());
        assertEquals(id, val.getValue());
    }

    @Test
    public void routerHandlesNamedParamWithTrailingSlash() throws Exception {
        String id = "12";
        String path = "/page/" + id + "/";

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        Router router = new Router();
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);
        verify(req).addParam(key.capture(), val.capture());
        assertEquals(route.getPath(), "/page/:id/");
        assertEquals(key.getValue(), "id");
        assertEquals(val.getValue(), id);
    }

    @Test
    public void routerHandlesWildCardCorrectly() throws Exception {
        Method meth = Class.forName("com.severell.core.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();


        String path = "/blog/hello";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);

        assertEquals(route.getPath(), "/blog/*files");
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerHandlesTwoCatchAllRoutes() throws Exception {
        assertThrows(Exception.class, () -> {
            routes.add(new RouteExecutor("/blog/*file", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void routerHandlesNamedParamInMidPathCorrectly() throws Exception {
        Router router = new Router();
        RequestOld req = mock(RequestOld.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);


        String path = "/post/123/settings";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);

        verify(req).addParam(key.capture(), val.capture());
        assertEquals(key.getValue(), "id");
        assertEquals(val.getValue(), "123");

        assertEquals(route.getPath(), "/post/:id/settings");
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerSplitsEdgeCorrectly() throws Exception {
        Router router = new Router();


        String path = "/search";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);


        assertEquals(route.getPath(), "/search");
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerHandleTwoNamesParams() throws Exception {
        Router router = new Router();
        RequestOld req = mock(RequestOld.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        String path = "/user/123/mitch";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);

        verify(req, times(2)).addParam(key.capture(), val.capture());
        assertEquals(key.getAllValues().get(0), "id");
        assertEquals(val.getAllValues().get(0), "123");

        assertEquals(key.getAllValues().get(1), "name");
        assertEquals(val.getAllValues().get(1), "mitch");

        assertEquals(route.getPath(), "/user/:id/:name");
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void testOnlyOneNamedParamPerPathSegmentAllowed() throws Exception {
        assertThrows(Exception.class, () -> {
            routes.add(new RouteExecutor("/test/:id:name", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void testWildCardsMustBeName() throws Exception {
        assertThrows(Exception.class, () -> {
            routes.add(new RouteExecutor("/test/*", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void testWildCardCantConflictWithExistingChildren() throws Exception {
        assertThrows(Exception.class, () -> {
            routes.add(new RouteExecutor("/test/hello", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            routes.add(new RouteExecutor("/test/*names", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void testCatchAllAreOnlyAllowedAtEndOfPath() throws Exception {
        assertThrows(Exception.class, () -> {
            routes.add(new RouteExecutor("/test/*names/somethingelse", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void testSlashMustBeBeforeCatchAll() throws Exception {
        assertThrows(Exception.class, () -> {
            routes.add(new RouteExecutor("/test*names", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void routerHandlesNamedParamAndCatchAll() throws Exception {
        Router router = new Router();


        String path = "/house/1/something";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);


        assertEquals(route.getPath(), "/house/:id/*homes");
//        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void testEmptyWildCardName() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.clearRoutes();
            routes.add(new RouteExecutor("/", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            routes.add(new RouteExecutor("/*test", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router.setCompiledRoutes(routes);
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void testTrailingSlashOnWildCard() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.clearRoutes();
            routes.add(new RouteExecutor("/*test", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            routes.add(new RouteExecutor("/*test/", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router.setCompiledRoutes(routes);
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void testSameRouteWithParamAppended() throws Exception {
        Router.clearRoutes();

        routes.add(new RouteExecutor("/some", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));
        routes.add(new RouteExecutor("/some:name", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));
        Router.setCompiledRoutes(routes);
        Router router = new Router();
        RequestOld req = mock(RequestOld.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        String path = "/some";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);

        assertEquals("/some", route.getPath());
//        assertEquals(meth, route.getMethod());

        route = router.lookup("/something", "GET", req);
        verify(req).addParam(key.capture(), val.capture());

        assertEquals("/some:name", route.getPath());
//        assertEquals(meth, route.getMethod());

        assertEquals("name", key.getAllValues().get(0));
        assertEquals("thing", val.getAllValues().get(0));
    }

    @Test
    public void testWildCardWithLongerDupeNames() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.clearRoutes();
            routes.add(new RouteExecutor("/*test", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            routes.add(new RouteExecutor("/*testing", HttpMethod.GET, ((request, response, container) -> {
                index();
            })));
            Router.setCompiledRoutes(routes);
            Router router = new Router();
            router.compileRoutes();
        });
    }

    @Test
    public void routerHandlesPartFixedPartWildcard() throws Exception {
        Router router = new Router();
        RequestOld req = mock(RequestOld.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);


        String path = "/user_mitch";
        router.compileRoutes();
        RouteExecutor route = router.lookup(path, "GET", req);

        verify(req).addParam(key.capture(), val.capture());
        assertEquals("name", key.getValue());
        assertEquals("mitch", val.getValue());

        assertEquals("/user_:name", route.getPath());
//        assertEquals(meth, route.getMethod());
    }

    @Test
    public void testRouterReturnsNullWhenNotFindingPath() throws Exception {
        Router.clearRoutes();
        routes = new ArrayList<>();
        routes.add(new RouteExecutor("/", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));
        routes.add(new RouteExecutor("/search", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));
        routes.add(new RouteExecutor("/blog/:id", HttpMethod.GET, ((request, response, container) -> {
            index();
        })));
        Router.setCompiledRoutes(routes);
        Router router = new Router();
        RequestOld req = mock(RequestOld.class);

        String path = "/homebase";
        router.compileRoutes();
        assertNull(router.lookup(path, "GET", req));
        assertNull(router.lookup("/search/2", "GET", req));
        assertNull(router.lookup("/blog/2/home", "GET", req));
        assertNull(router.lookup("/blog/2/", "GET", req));
    }

    @Test
    public void testRouterAddsToRouteListCorrectly() throws NoSuchMethodException, ClassNotFoundException {
        Router.clearRoutes();
        Router.get("/", method, new Class[0]);
        Router.post("/", method, new Class[0]);
        Router.put("/", method, new Class[0]);
        Router.patch("/", method, new Class[0]);
        Router.delete("/", method, new Class[0]);
        Router.options("/", method, new Class[0]);

        Router router = new Router();
        ArrayList<RouteInfo> routes = router.getRoutes();
        assertEquals(HttpMethod.GET, routes.get(0).getHttpMethod());
        assertEquals("/", routes.get(0).getPath());
        assertEquals(HttpMethod.HEAD, routes.get(1).getHttpMethod());
        assertEquals("/", routes.get(1).getPath());

        assertEquals(HttpMethod.POST, routes.get(2).getHttpMethod());
        assertEquals("/", routes.get(2).getPath());

        assertEquals(HttpMethod.PUT, routes.get(3).getHttpMethod());
        assertEquals("/", routes.get(3).getPath());

        assertEquals(HttpMethod.PATCH, routes.get(4).getHttpMethod());
        assertEquals("/", routes.get(4).getPath());

        assertEquals(HttpMethod.DELETE, routes.get(5).getHttpMethod());
        assertEquals("/", routes.get(5).getPath());

        assertEquals(HttpMethod.OPTIONS, routes.get(6).getHttpMethod());
        assertEquals("/", routes.get(6).getPath());
    }

    @Test
    public void routerReturnsNullForEmptyRoute() throws Exception {
        Router.clearRoutes();
        req = mock(RequestOld.class);
        context = mock(ServletContextHandler.class);
        c = mock(Container.class);
        routes =  new ArrayList<>();

        String path = "/";

        Router router = new Router();
        router.compileRoutes();
        RouteExecutor route = null;
        route = router.lookup(path, "GET", req);
        assertNull(route);
        route = router.lookup(path, "POST", req);
        assertNull(route);
        route = router.lookup(path, "PUT", req);
        assertNull(route);
        route = router.lookup(path, "PATCH", req);
        assertNull(route);
        route = router.lookup(path, "DELETE", req);
        assertNull(route);
        route = router.lookup(path, "OPTION", req);
        assertNull(route);
    }

}
