package minigames;

import arc.*;
import arc.util.*;
import mindustry.mod.*;
import minigames.commands.CommandLoader;
import minigames.database.JsonDatabase;
import minigames.events.Backgrounds;
import minigames.events.EventLoader;
import minigames.function.TeamManager;

import java.util.Objects;

public class Entry extends Mod{
    public static JsonDatabase jdb;
    private final CommandLoader cLoader = new CommandLoader();
    private Backgrounds backgrounds;

    @Override
    public void init() {
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
}
