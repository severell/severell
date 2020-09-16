package com.severell.plugin;

import com.severell.core.container.Container;
import com.severell.core.providers.ServiceProvider;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ProjectClassLoader {

    private final MavenProject project;
    private String basePackage;
    private URLClassLoader classLoader;
    private Log log;

    public ProjectClassLoader(MavenProject project, String basePackage, Log log) throws MalformedURLException, DependencyResolutionRequiredException {
        this.project = project;
        this.basePackage = basePackage;
        this.log = log;
        this.loadClassPath();
    }

    private void loadClassPath() throws DependencyResolutionRequiredException, MalformedURLException {
        List<URL> pathURL = new ArrayList<>();
        for(String mc : project.getCompileClasspathElements()) {
            pathURL.add(new File(mc).toURI().toURL());
        }
        URL[] urlsForCL = pathURL.toArray(URL[]::new);
        classLoader = new URLClassLoader(urlsForCL, AOTRouteGenerator.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public Class loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    public ServiceProvider[] getProviders(Container container) {
        try {
            Class cl = classLoader.loadClass(basePackage + ".Providers");
            return (ServiceProvider[]) cl.getMethod("load", Container.class).invoke(null, container);
        }catch (Exception e) {
            log.info("Unable to retrieve Providers");
            return new ServiceProvider[0];
        }

    }

    public Class[] getMiddleware() {
        try {
            Class cl = classLoader.loadClass(basePackage + ".Middleware");
            return (Class[]) cl.getField("MIDDLEWARE").get(null);
        }catch (Exception e) {
            log.info("Unable to retrieve Middleware");
            return new Class[0];
        }
    }

    public void initRoutes() {
        try {
            Class cl = classLoader.loadClass(basePackage + ".routes.Routes");
            cl.getMethod("init").invoke(null);
        }catch (Exception e) {
            log.error(e);
            log.info("Unable to init Routes");
        }
    }
}
