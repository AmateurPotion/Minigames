package minigames;

import arc.*;
import arc.files.Fi;
import arc.util.*;
import mindustry.Vars;
import mindustry.mod.*;
import minigames.commands.CommandLoader;
import minigames.database.JsonDatabase;
import minigames.events.Backgrounds;
import minigames.events.EventLoader;
import minigames.function.TeamManager;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class Entry extends Mod{
    public static JsonDatabase jdb;
    private final CommandLoader cLoader = new CommandLoader();
    private Backgrounds backgrounds;

    @Override
    public void init() {
        loadMap();
        new EventLoader().load();
        jdb = new JsonDatabase();
        Core.settings.put("marathon", false);
        backgrounds = new Backgrounds();

        Timer shuffleTimer = new Timer();
        Timer.Task shuffleTask = new Timer.Task() {
            @Override
            public void run() {
                if(Objects.equals(Core.settings.getBool("teamShuffle", false), true)) { // put teamShuffle false bool
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
                }
            }
        }
    }
}
