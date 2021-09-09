package minigames.modes.marathon;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import minigames.type.dataType.Content;
import minigames.utils.Synthesis;

import java.util.Objects;

import static minigames.Entry.db;

public class MarathonCommands implements Content {
    private final Seq<CommandHandler.Command> serverCommands;
    private final Seq<CommandHandler.Command> clientCommands;

    public MarathonCommands() {
        serverCommands = Seq.with();
        clientCommands = Seq.with();
    }

    public void createServerCommand() {
        serverCommands.add(new CommandHandler.Command("testCommand", "", "", (args, parameter) -> {
            Log.info(parameter);
        }));
    }

    public void createClientCommand() {
    }

    @Override
    public void load() {
        createServerCommand();
        createClientCommand();
    }

    @Override
    public boolean active() {
        try {
            CommandHandler serverHandler = db.cLoader.server();
            CommandHandler clientHandler = db.cLoader.client();
            Seq<CommandHandler.Command> sc = serverHandler.getCommandList();
            Seq<CommandHandler.Command> cc = clientHandler.getCommandList();
            serverCommands.each(cmd -> {
                if(sc.contains(command -> Objects.equals(command.text, cmd.text))) {
                    serverCommands.remove(cmd);
                } else {
                    try {
                        CommandHandler.CommandRunner<?> runner = Synthesis.invokeT(CommandHandler.CommandRunner.class, cmd, "runner");
                        serverHandler.register(cmd.text, cmd.paramText, cmd.description, runner);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        Log.info(e.getLocalizedMessage());
                    }
                }
            });
            clientCommands.each(cmd -> {
                if(cc.contains(command -> Objects.equals(command.text, cmd.text))) {
                    clientCommands.remove(cmd);
                } else {
                    try {
                        CommandHandler.CommandRunner<?> runner = Synthesis.invokeT(CommandHandler.CommandRunner.class, cmd, "runner");
                        clientHandler.register(cmd.text, cmd.paramText, cmd.description, runner);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        Log.info(e.getLocalizedMessage());
                    }
                }
            });
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void disable() {
        serverCommands.each(cmd -> db.cLoader.server().removeCommand(cmd.text));
        clientCommands.each(cmd -> db.cLoader.client().removeCommand(cmd.text));
    }
}