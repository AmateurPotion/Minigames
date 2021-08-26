package minigames.database.DataType;

import arc.struct.Seq;
import arc.util.serialization.Jval;
import mindustry.gen.Player;
import minigames.ctype.CoolTime;
import minigames.ctype.Skill;

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

        // load
        Jval.JsonArray tempSkillSet;
        if(config.get("skillSet") != null) {
            tempSkillSet = config.get("skillSet").asArray();
            tempSkillSet.each(skill -> {
                Skill<?> s = originalSkills.find(os -> Objects.equals(os.name(), skill.toString()));
                if(s != null) {
                    skillSet.add(s);
                }
            });
        }
        config.remove("skillSet");

        configCheck("name", player.name());
        configCheck("permission level", 0);
        configCheck("point", 0);
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

    public int point() {
        return config.getInt("point", 0);
    }

    public void addPoint(int point) {
        config.put("point", this.point() + point);
    }

    public boolean isReady(String name) {
        return coolTimes.get(name) == 0;
    }
}