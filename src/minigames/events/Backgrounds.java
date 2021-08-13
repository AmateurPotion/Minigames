package minigames.events;

import arc.Core;
import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import minigames.Entry;
import minigames.database.PlayerData;
import minigames.modes.Marathon;

public class Backgrounds {
    public static Timer background;
    private static Timer.Task checkTask;
    public Backgrounds() {
        background = new Timer();

        checkTask = new Timer.Task() {
            @Override
            public void run() {
                if(Vars.state.isPlaying() && Core.settings.getBool("marathon", false)) {
                    Groups.player.each(player -> player.team() != Team.derelict && player.unit() != null && !(player.unit() instanceof BlockUnitc), player -> {
                        Tile current = Vars.world.tile((int)(player.x / 8), (int)(player.y / 8));
                        if(current == null || current.floor() == Blocks.space) {
                            PlayerData data = Entry.jdb.players.find(d -> d.player == player);
                            Marathon.home(data);
                            data.config.put("score", data.config.getInt("score", 0) - 10);
                            Call.setHudText(data.player.con, "score : " + data.config.getInt("score", 0));
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
