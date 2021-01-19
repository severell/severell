package com.severell.plugin;

import com.severell.core.commands.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import picocli.CommandLine;

import java.util.Scanner;

@Mojo(name="cli", defaultPhase = LifecyclePhase.NONE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CLITool extends AbstractMojo {
    @Parameter
    public String basePackage;

    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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
                new CommandLine(command).execute(args.split(" "));
            }
            printPrompt();
        }
    }

    private void printPrompt() {
        System.out.print("severell$ ");
    }
}
