package minigames.events;

import arc.Core;
import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import minigames.Entry;
import minigames.database.DataType.PlayerData;
import minigames.modes.marathon.Marathon;

import static minigames.Entry.db;
import static minigames.modes.marathon.Marathon.updateScore;

public class Backgrounds {
    public static Timer background;
    private final Timer.Task checkTask;
    public Backgrounds() {
        background = new Timer();

        checkTask = new Timer.Task() {
            @Override
            public void run() {
                if(Vars.state.isPlaying() && db.gameMode("marathon")) {
                    Groups.player.each(player -> player.team() != Team.derelict && player.unit() != null && !(player.unit() instanceof BlockUnitc), player -> {
                        Tile current = Vars.world.tile((int)(player.x / 8), (int)(player.y / 8));
                        if(current == null || current.floor() == Blocks.space) {
                            PlayerData data = db.players.find(d -> d.player == player);
                            Marathon.home(data);
                            updateScore(data, -(int)(data.config.getInt("score", 0) * 0.01f));
                        }
                        else if(current.floor() == Blocks.sandWater || current.floor() == Blocks.dirt) Events.fire(new EventList.MarathonLineArrivalEvent(player, current));

                        //Log.info(player.x + " / " + player.y + " / " + (current.floor() != null ? current.floor().name : null));
                    });
                }
            }
        };
    }

    public void startPlayerChecker() {
        background.scheduleTask(checkTask,  0.5f, 0.5f);
    }
}
