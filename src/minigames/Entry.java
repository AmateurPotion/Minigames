package minigames;

import arc.*;
import arc.files.Fi;
import arc.util.*;
import arc.util.io.Streams;
import mindustry.Vars;
import mindustry.mod.*;
import minigames.io.PacketLoader;
import minigames.io.commands.CommandLoader;
import minigames.database.Database;
import minigames.io.Backgrounds;
import minigames.io.EventLoader;
import minigames.utils.TeamManager;

import java.io.InputStream;

public class Entry extends Mod{
    public final static Database db = new Database();
    private final CommandLoader cLoader = new CommandLoader();
    private Backgrounds backgrounds;

    @Override
    public void init() {
        loadMap();
        // loadEvent
        new EventLoader().load();
        new PacketLoader().load();
        db.skillLoader.load();
        backgrounds = new Backgrounds();

        Timer shuffleTimer = new Timer();
        Timer.Task shuffleTask = new Timer.Task() {
            @Override
            public void run() {
                if(db.gameMode("shuffle")) { // put teamShuffle false bool
                    TeamManager.shuffle();
                }
            }
        };
        shuffleTimer.scheduleTask(shuffleTask, 10, Core.settings.getInt("teamShuffleDelay", 120));

        backgrounds.startPlayerChecker();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        cLoader.registerServerCommands(handler);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        cLoader.registerClientCommands(handler);
    }

    private void loadMap() {
        String[] mapList = new String[]{"4P.msav", "marathon.msav"};
        Fi mapDir = new Fi(Vars.dataDirectory.path() + "/maps", Vars.dataDirectory.type());
        for(String mapName : mapList) {
            Fi target = new Fi(mapDir.path() + "/" + mapName, mapDir.type());
            if(!target.exists()) {
                InputStream in = getClass().getResourceAsStream("/assets/maps/" + mapName);
                if(in != null) {
                    target.write(in, false);
                    Streams.close(in);
                }
            }
        }
    }
}
