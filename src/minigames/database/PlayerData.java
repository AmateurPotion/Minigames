package minigames.database;

import arc.util.serialization.Jval;
import mindustry.gen.Player;

import java.util.Objects;

import static minigames.Entry.*;

public class PlayerData {
    public final Player player;
    public final Jval config;

    public PlayerData(Player player) {
        this.player = player;
        config = Objects.requireNonNullElseGet(jdb.getPlayerData(player.uuid()), Jval::newObject);
        configCheck("name", player.name());
        configCheck("score", 0);
        configCheck("line@NS", 1);
    }

    private void configCheck(String name, Object val) {
        if(config.get(name) == null) {
            if(val instanceof Jval obj) config.put(name, obj);
            else if(val instanceof String obj) config.put(name, obj);
            else if(val instanceof Number obj) config.put(name, obj);
            else if(val instanceof Boolean obj) config.put(name, obj);
        }
    }
}
