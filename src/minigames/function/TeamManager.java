package minigames.function;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class TeamManager {
    public static void shuffle() {
        Seq<Player> players = Seq.with();
        Seq<mindustry.game.Team> teams = Seq.with();
        Groups.player.copy(players);
        Groups.player.find(player -> {
            teams.add(player.team());
            Log.info(player.team().name);
            return false;
        });

        for(int i = 0; i < Groups.player.size(); i++) {
            int finalI = i;
            mindustry.game.Team tempT = teams.random();
            Groups.player.find(p -> p == players.get(finalI)).team(tempT);
            Call.unitClear(Groups.player.find(p -> p == players.get(finalI)));
            teams.remove(t -> t == tempT);
        }

        players.clear();
        teams.clear();
    }
}
