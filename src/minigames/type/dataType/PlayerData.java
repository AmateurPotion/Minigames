package minigames.type.dataType;

import arc.struct.Seq;
import arc.util.serialization.Jval;
import mindustry.gen.Player;
import minigames.type.ctype.CoolTime;
import minigames.type.ctype.Skill;

import java.util.Objects;

import static minigames.Entry.*;

public class PlayerData {
    public final Player player;
    public final Seq<Skill<?>> skillSet;
    public final Jval config;
    public final CoolTime coolTimes;

    public PlayerData(Player player) {
        this(player, db.skills);
    }

    public PlayerData(Player player, Seq<Skill<?>> originalSkills) {
        // init
        this.player = player;
        skillSet = Seq.with();
        config = Objects.requireNonNullElseGet(db.getPlayerData(player.uuid()), Jval::newObject);
        coolTimes = new CoolTime();

        // configuration

        if(config.get("skillSet") != null) {
            originalSkills.each(skill -> config.get("skillSet").asArray().contains(val -> Objects.equals(val.toString(), skill.name())), skillSet::add);
        }
        config.remove("skillSet");

        configCheck("name", player.name());
        configCheck("permission level", 0);
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

    public int permissionLevel() {
        return config.getInt("permission level", 0);
    }

    public void permissionLevel(int level) {
        config.put("permission level", level);
    }

    public boolean isReady(String name) {
        return coolTimes.get(name) == 0;
    }
}