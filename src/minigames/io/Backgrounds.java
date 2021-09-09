package minigames.io;

import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import minigames.modes.marathon.MarathonEvents;
import minigames.type.dataType.PlayerData;
import minigames.modes.marathon.Marathon;

import static minigames.Entry.db;

public class Backgrounds {
    public static Timer background;
    private final Timer.Task checkTask;
    public Backgrounds() {
        background = new Timer();

        checkTask = new Timer.Task() {
            @Override
            public void run() {
                Marathon mode = db.gameMode(Marathon.class, "marathon");
                if(Vars.state.isPlaying() && mode.isActive()) {
                    db.players.each(data -> !data.config.getBool("joinAllow@NS", true), data -> {
                        data.coolTimes.proceed(0.5f);
                    });

                    Groups.player.each(player -> player.team() != Team.derelict && player.unit() != null && !(player.unit() instanceof BlockUnitc), player -> {
                        Tile current = Vars.world.tile((int)(player.x / 8), (int)(player.y / 8));
                        if(current == null || current.floor() == Blocks.space) {
                            PlayerData data = db.players.find(d -> d.player == player);
                            mode.home(data);
                            mode.updateScore(data, -(int)(data.config.getInt("score", 0) * 0.01f));
                        }
                        else if(current.floor() == Blocks.sandWater || current.floor() == Blocks.dirt) Events.fire(new MarathonEvents.MarathonLineArrivalEvent(player, player.unit(), current));
                    });
                }
            }
        };
    }

    public void startPlayerChecker() {
        background.scheduleTask(checkTask,  0.5f, 0.5f);
    }
}
