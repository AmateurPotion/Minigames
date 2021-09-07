package minigames.modes.marathon;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import minigames.type.dataType.Content;

import static minigames.Entry.db;

public class MarathonCommands implements Content {
    private final Seq<String> serverCommands, clientCommands;

    public MarathonCommands() {
        serverCommands = Seq.with();
        clientCommands = Seq.with();
    }

    public void registerServerCommand() {
        CommandHandler handler = db.cLoader.server();
    }

    public void registerClientCommand() {
        CommandHandler handler = db.cLoader.client();
    }

    @Override
    public boolean active() {
        try {
            registerServerCommand();
            registerClientCommand();
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void disable() {
        serverCommands.each(db.cLoader.server()::removeCommand);
        clientCommands.each(db.cLoader.client()::removeCommand);
    }
}