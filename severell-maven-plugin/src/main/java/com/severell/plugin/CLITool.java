package com.severell.plugin;

import com.severell.core.commands.*;
import com.severell.core.container.Container;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import picocli.CommandLine;

import java.net.MalformedURLException;
import java.util.Scanner;

@Mojo(name="cli", defaultPhase = LifecyclePhase.NONE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CLITool extends SeverellMojo {
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Container container = new Container();
        ProjectClassLoader classLoader = null;
        try {
            classLoader = new ProjectClassLoader(project, basePackage, getLog());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
        }
        bootstrap(container, classLoader);

        Scanner in = new Scanner(System.in);
        boolean running = true;
        printPrompt();
        while(running) {
            String line = in.nextLine();
            String cmd;
            String args = "";
            if(line.indexOf(" ") == -1) {
                cmd = line;
            } else {
                cmd = line.substring(0, line.indexOf(" "));
                args = line.substring(line.indexOf(" ") + 1);
            }
            
            Command command = null;

            switch (cmd) {
                case "make:migration":
                    command = new MakeMigration();
                    break;
                case "make:controller":
                    command = new MakeController();
                    break;
                case "make:model":
                    command = new MakeModel();
                    break;
                case "migrate":
                    command = new MigrateCommand();
                    break;
                case "migrate:reset":
                    command = new MigrateResetCommand();
                    break;
                case "migrate:rollback":
                    command = new MigrateRollbackCommand();
                    break;
                default:
                    System.out.println("Unknown Command.");
            }

            if(command != null) {
                command.setCalleePackage(basePackage);
                String[] argArray = args.trim().split(" ");
                if(args.trim().isEmpty()) {
                    argArray = new String[]{};
                }

                new CommandLine(command).execute(argArray);
            }
            printPrompt();
        }
    }

    private void printPrompt() {
        System.out.print("severell$ ");
    }
}
