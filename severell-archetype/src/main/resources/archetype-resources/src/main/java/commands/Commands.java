package ${package}.commands;

import com.severell.core.commands.*;

public class Commands {

    public static final Class<Command>[] COMMANDS = new Class[]{
        MakeMigration.class,
        MigrateCommand.class,
        MigrateRollbackCommand.class,
        MakeCommand.class,
        MakeController.class,
        MakeModel.class
    };
}
