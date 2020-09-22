package ${package}.commands;

import com.severell.core.commands.Command;
import com.severell.core.commands.Flag;
import com.severell.core.config.Config;
import org.apache.maven.shared.utils.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Commander {

    public static void main(String[] args) throws Exception {
        if(args.length >0) {
            loadConfig();
            switch (args[0]) {
                case "load":
                    load();
                    break;
                default:
                    runCommand(args[0], getArgumentsToPass(args));
                    break;
            }
        }

    }

    private static String[] getArgumentsToPass(String[] args) {
        if(args.length > 1) {
            return Arrays.copyOfRange(args, 1, args.length);
        }
        return new String[]{};
    }

    private static void loadConfig() {
        try {
            Config.loadConfig();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runCommand(String clazz, String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Command command = (Command) Class.forName(clazz).getDeclaredConstructor().newInstance();
        String[] parts = Commander.class.getCanonicalName().split("\\.");
        String[] subarray = Arrays.copyOfRange(parts, 0, parts.length - 2);
        command.setCalleePackage(StringUtils.join(subarray, "."));
        command.setDataSource(setupDatabase);
        command.run(args);
    }

    public static void load() throws IOException, ParseException {
        FileReader reader = new FileReader("severell.json");
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = (JSONObject) jsonParser.parse(reader);
        JSONArray commands = new JSONArray();


        for(Class<Command> clazz : Commands.COMMANDS) {
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
        Files.write(Paths.get("severell.json"), obj.toJSONString().getBytes());
    }

    private BasicDataSource setupDatabase() {
        BasicDataSource connectionPool = new BasicDataSource();
        connectionPool.setUsername(Config.get("DB_USERNAME"));
        connectionPool.setPassword(Config.get("DB_PASSWORD"));
        connectionPool.setDriverClassName(Config.get("DB_DRIVER"));
        connectionPool.setUrl(Config.get("DB_CONNSTRING"));
        connectionPool.setInitialSize(1);
        connectionPool.setMinIdle(1);
        connectionPool.setMaxIdle(1);
        return connectionPool;
    }
}
