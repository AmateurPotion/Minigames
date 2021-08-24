package minigames.ctype;

import arc.func.Cons;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

import java.util.HashMap;

public record Skill<T>(Class<T> entry, String name, Cons<T> listener, Team team, UnitType unitType) {
    private static final HashMap<String, Integer> names = new HashMap<>();

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
        if(names.get(name) == null) {
            names.put(name, 0);
            num = 0;
        } else {
            num = names.get(name) + 1;
            names.put(name, num);
        }

        return new Skill<>(entry, name + "#" + num, listener, team, type);
    }
}
