package com.severell.plugin;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.providers.ServiceProvider;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SeverellMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    @Parameter
    public String basePackage;

    @Override
    public abstract void execute() throws MojoExecutionException, MojoFailureException ;

    public void bootstrap(Container c, ProjectClassLoader classLoader) {
        try {
            Config.setDir(project.getBuild().getOutputDirectory());
            Config.loadConfig();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        c.singleton("_MiddlewareList", classLoader.getMiddleware());
        try {
            c.singleton(classLoader.loadClass(basePackage + ".auth.Auth"), classLoader.initClass(classLoader.loadClass(basePackage + ".auth.Auth")));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        List<ServiceProvider> providers = Arrays.stream(classLoader.getProviders(c))
                .filter(s -> {
                    return !s.getClass().getSimpleName().equals("AppProvider")
                            && !s.getClass().getSimpleName().equals("JettyProvider")
                            && !s.getClass().getSimpleName().equals("RouteProvider");
                })
                .collect(Collectors.toList());

        for(ServiceProvider provider : providers) {
            try {
                provider.register();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        classLoader.initRoutes();

        for(ServiceProvider provider : providers) {
            try {
                provider.boot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
