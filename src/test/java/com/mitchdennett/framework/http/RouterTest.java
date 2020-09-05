package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouterTest {

    public static void index() throws IOException {

    }

    public static void post() throws IOException {

    }

    private static Request req;
    private static Container c;
    private static ServletContextHandler context;

    @BeforeEach
    public void setUp() throws NoSuchMethodException, ClassNotFoundException {
        Router.clearRoutes();
        req = mock(Request.class);
        context = mock(ServletContextHandler.class);
        c = mock(Container.class);
        Router.Get("/", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Post("/", "com.mitchdennett.framework.http.RouterTest::post");
        Router.Get("/page/:id/", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/user/:id", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/user/:id/:name", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/blog/*files", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/searching", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/search", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/blogging", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/post/:id/settings", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/house/:id/*homes", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/user_:name","com.mitchdennett.framework.http.RouterTest::index");
    }

    @Test
    public void routerReturnsCorrectHandle() throws Exception {
        String path = "/";
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");

        Router router = new Router();
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);
        assertEquals(route.getPath(), path);
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerReturnsCorrectRouteForPost() throws Exception {
        String path = "/";
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("post");

        Router router = new Router();
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "POST", req);
        assertEquals(route.getPath(), path);
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerHandlesNamedParam() throws Exception {
        String id = "12";
        String path = "/user/" + id;

        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        Router router = new Router();
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);
        verify(req).addParam(key.capture(), val.capture());
        assertEquals("/user/:id",route.getPath() );
        assertEquals(route.getMethod(), meth);
        assertEquals("id", key.getValue());
        assertEquals(id, val.getValue());
    }

    @Test
    public void routerGetCorrectNamedParamWithTrailingSlash() throws Exception {
        String id = "12";
        String path = "/user/" + id + "/";

        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        Router router = new Router();
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);
        verify(req).addParam(key.capture(), val.capture());

        assertEquals("/user/:id/:name",route.getPath() );
        assertEquals(route.getMethod(), meth);
        assertEquals("id", key.getValue());
        assertEquals(id, val.getValue());
    }

    @Test
    public void routerHandlesNamedParamWithTrailingSlash() throws Exception {
        String id = "12";
        String path = "/page/" + id + "/";

        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        Router router = new Router();
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);
        verify(req).addParam(key.capture(), val.capture());
        assertEquals(route.getPath(), "/page/:id/");
        assertEquals(route.getMethod(), meth);
        assertEquals(key.getValue(), "id");
        assertEquals(val.getValue(), id);
    }

    @Test
    public void routerHandlesWildCardCorrectly() throws Exception {
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();


        String path = "/blog/hello";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);

        assertEquals(route.getPath(), "/blog/*files");
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerHandlesTwoCatchAllRoutes() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.Get("/blog/*file", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void routerHandlesNamedParamInMidPathCorrectly() throws Exception {
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();
        Request req = mock(Request.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);


        String path = "/post/123/settings";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);

        verify(req).addParam(key.capture(), val.capture());
        assertEquals(key.getValue(), "id");
        assertEquals(val.getValue(), "123");

        assertEquals(route.getPath(), "/post/:id/settings");
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerSplitsEdgeCorrectly() throws Exception {
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();


        String path = "/search";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);


        assertEquals(route.getPath(), "/search");
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void routerHandleTwoNamesParams() throws Exception {
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();
        Request req = mock(Request.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        String path = "/user/123/mitch";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);

        verify(req, times(2)).addParam(key.capture(), val.capture());
        assertEquals(key.getAllValues().get(0), "id");
        assertEquals(val.getAllValues().get(0), "123");

        assertEquals(key.getAllValues().get(1), "name");
        assertEquals(val.getAllValues().get(1), "mitch");

        assertEquals(route.getPath(), "/user/:id/:name");
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void testOnlyOneNamedParamPerPathSegmentAllowed() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.Get("/test/:id:name", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void testWildCardsMustBeName() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.Get("/test/*", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void testWildCardCantConflictWithExistingChildren() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.Get("/test/hello", "com.mitchdennett.framework.http.RouterTest::index");
            Router.Get("/test/*names", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void testCatchAllAreOnlyAllowedAtEndOfPath() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.Get("/test/*names/somethingelse", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void testSlashMustBeBeforeCatchAll() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.Get("/test*names", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void routerHandlesNamedParamAndCatchAll() throws Exception {
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();


        String path = "/house/1/something";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);


        assertEquals(route.getPath(), "/house/:id/*homes");
        assertEquals(route.getMethod(), meth);
    }

    @Test
    public void testEmptyWildCardName() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.clearRoutes();
            Router.Get("/", "com.mitchdennett.framework.http.RouterTest::index");
            Router.Get("/*test", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void testTrailingSlashOnWildCard() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.clearRoutes();
            Router.Get("/*test", "com.mitchdennett.framework.http.RouterTest::index");
            Router.Get("/*test/", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void testSameRouteWithParamAppended() throws Exception {
        Router.clearRoutes();
        Router.Get("/some", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/some:name", "com.mitchdennett.framework.http.RouterTest::index");
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();
        Request req = mock(Request.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        String path = "/some";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);

        assertEquals("/some", route.getPath());
        assertEquals(meth, route.getMethod());

        route = router.lookup("/something", "GET", req);
        verify(req).addParam(key.capture(), val.capture());

        assertEquals("/some:name", route.getPath());
        assertEquals(meth, route.getMethod());

        assertEquals("name", key.getAllValues().get(0));
        assertEquals("thing", val.getAllValues().get(0));
    }

    @Test
    public void testWildCardWithLongerDupeNames() throws Exception {
        assertThrows(Exception.class, () -> {
            Router.clearRoutes();
            Router.Get("/*test", "com.mitchdennett.framework.http.RouterTest::index");
            Router.Get("/*testing", "com.mitchdennett.framework.http.RouterTest::index");
            Router router = new Router();
            router.compileRoutes(context, c);
        });
    }

    @Test
    public void routerHandlesPartFixedPartWildcard() throws Exception {
        Method meth = Class.forName("com.mitchdennett.framework.http.RouterTest").getDeclaredMethod("index");
        Router router = new Router();
        Request req = mock(Request.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);


        String path = "/user_mitch";
        router.compileRoutes(context, c);
        Route route = router.lookup(path, "GET", req);

        verify(req).addParam(key.capture(), val.capture());
        assertEquals("name", key.getValue());
        assertEquals("mitch", val.getValue());

        assertEquals("/user_:name", route.getPath());
        assertEquals(meth, route.getMethod());
    }

    @Test
    public void testRouterReturnsNullWhenNotFindingPath() throws Exception {
        Router.clearRoutes();
        Router.Get("/", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/search", "com.mitchdennett.framework.http.RouterTest::index");
        Router.Get("/blog/:id", "com.mitchdennett.framework.http.RouterTest::index");
        Router router = new Router();
        Request req = mock(Request.class);

        String path = "/homebase";
        router.compileRoutes(context, c);
        assertNull(router.lookup(path, "GET", req));
        assertNull(router.lookup("/search/2", "GET", req));
        assertNull(router.lookup("/blog/2/home", "GET", req));
        assertNull(router.lookup("/blog/2/", "GET", req));
    }

    @Test
    public void testRouterThrowsErrorWhenMethodNotFound() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            Router.clearRoutes();
            Router.Get("/", "com.mitchdennett.framework.http.RouterTest::notFound");
        });
    }
}
