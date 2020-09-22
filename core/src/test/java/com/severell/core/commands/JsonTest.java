package com.severell.core.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {

    @Test
    public void jsonTest() {
        JSONArray commands = new JSONArray();
        ArrayList<Class> list = new ArrayList<>();
        list.add(MakeCommand.class);
        list.add(MakeMigration.class);
        JSONObject obj = new JSONObject();

        for(Class<Command> clazz : list) {
            try {
                Command command = clazz.getDeclaredConstructor().newInstance();
                JSONObject comObj = new JSONObject();
                comObj.put("class", clazz.getName());
                comObj.put("description", command.getDescription());
                comObj.put("command", command.getCommand());

                JSONArray flags = new JSONArray();
                for(Flag flag : command.getFlags()) {
                    JSONObject flagObj = new JSONObject();
                    flagObj.put("flag", flag.getFlag());
                    flagObj.put("description", flag.getDescription());
                    flags.add(flagObj);
                }
                comObj.put("flags", flags);
                comObj.put("numArgs", command.getNumArgs());

                commands.add(comObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        obj.put("commands", commands);
        assertEquals("{\"commands\":[{\"numArgs\":1,\"flags\":[],\"description\":\"Make a new command class\",\"class\":\"com.severell.core.commands.MakeCommand\",\"command\":\"make:command [name]\"},{\"numArgs\":1,\"flags\":[{\"flag\":\"t\",\"description\":\"Table Name\"},{\"flag\":\"c\",\"description\":\"Create a new table with the following name\"}],\"description\":\"Create a new Migration\",\"class\":\"com.severell.core.commands.MakeMigration\",\"command\":\"make:migration [name]\"}]}", obj.toJSONString());


    }
}
