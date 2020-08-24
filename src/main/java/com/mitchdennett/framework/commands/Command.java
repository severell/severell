package com.mitchdennett.framework.commands;

import java.util.ArrayList;

public abstract class Command {

    protected String description;
    protected String command;
    protected ArrayList<Flag> flags;

    public abstract void execute(String[] args);

    public void run(String[] args) {
        //TODO CLEAN THIS UP A LITTLE BIT. I DON'T LIKE IT A WHOLE LOT
        String[] argsToPass = new String[0];
        if(args.length > 1) {
//            System.out.println(args[0]);
            String[] argParts = args[0].split("args=");
            if(argParts.length > 0 && argParts[1] != null) {
                argsToPass = argParts[1].split(",");
            }
            parseFlags(args[1]);
        }
        this.execute(argsToPass);
    }

    private void parseFlags(String arg1) {
        String[] flags;
        String[] flagParts = arg1.split("flags=");
        if(flagParts.length > 0 &&  flagParts[1] != null) {
            flags = flagParts[1].split(",");
            for(String f : flags) {
                String name = f.split("=")[0];
                String val = f.split("=")[1];
                for(Flag fl : this.getFlags()) {
                    if(name.substring(1).equals(fl.flag)) {
                        fl.value = val;
                    }
                }
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Flag> getFlags() {
        return flags == null ? new ArrayList<>() : flags;
    }

    public void addFlag(String flag, String description) {
        if(flags == null) {
            this.flags = new ArrayList<>();
        }

        this.flags.add(new Flag(flag, description));
    }
}
