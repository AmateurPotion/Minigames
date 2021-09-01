package minigames.type.ctype;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.type.UnitType;

import java.util.HashMap;

public record Skill<T>(Class<T> entry, String name, Cons<T> listener, Team team, UnitType unitType) {
    private static final HashMap<String, Integer> intTags = new HashMap<>();
    private static final HashMap<String, Seq<String>> tags = new HashMap<>();

    public static <T> Skill<T> constructor(Class<T> entry, String name, Cons<T> listener) {
        return new Skill<>(entry, name, listener, null, null);
    }

    public static <T> Skill<T> constructor(Class<T> entry, String name, Cons<T> listener, Team team) {
        return new Skill<>(entry, name, listener, team, null);
    }

    public static <T> Skill<T> constructor(Class<T> entry, String name, Cons<T> listener, UnitType unitType) {
        return new Skill<>(entry, name, listener, null, unitType);
    }

    public static <T> Skill<T> constructor(Class<T> entry, Cons<T> listener) {
        return new Skill<>(entry, "tempSkill@NS", listener, null, null);
    }

    public static Skill<Object> constructor(String name) {
        return new Skill<>(Object.class, name + "@MARK", e -> {}, null, null);
    }

    public Skill<T> imitation(Team team) {
        return imitation(team, unitType);
    }

    public Skill<T> imitation(UnitType type) {
        return imitation(team, type);
    }

    public Skill<T> imitation(Team team, UnitType type) {
        int num;
        if(intTags.get(name) == null) {
            intTags.put(name, 0);
            num = 0;
        } else {
            num = intTags.get(name) + 1;
            intTags.put(name, num);
        }
        return new Skill<>(entry, name + "#" + num, listener, team, type);
    }

    public Skill<T> imitation(Team team, UnitType type, String customTag) {
        if(intTags.containsKey(customTag) && getTags(name).contains(customTag)) {
            Log.info("The tag is not available. Tag : " + customTag);
            return this;
        } else {
            return new Skill<>(entry, name + "#" + customTag, listener, team, type);
        }
    }

    public Seq<String> getTags(String skillName) {
        if(tags.containsKey(skillName)) {
            return tags.get(skillName);
        } else {
            tags.put(skillName, Seq.with());
            return Seq.with();
        }
    }
}
