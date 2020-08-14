package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.annotations.After;
import com.mitchdennett.framework.annotations.Before;

public class MockMiddleware {

    @Before
    public boolean before() {
        return true;
    }

    @After
    public boolean after() {
        return true;
    }
}
