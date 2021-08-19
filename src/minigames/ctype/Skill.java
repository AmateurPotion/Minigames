package minigames.ctype;

import arc.func.Boolp;
import arc.func.Cons;

public record Skill<T>(Class<T> type, String name, Cons<T> listener, Boolp trigger) {
    public static <T> Skill<T> constructor(Class<T> type, String name, Cons<T> listener) {
        return new Skill<>(type, name, listener, () -> true);
    }

    public static <T> Skill<T> constructor(Class<T> type, Cons<T> listener) {
        return new Skill<>(type, "tempSkill@NS", listener, () -> true);
    }

    public static Skill<Object> constructor(String name) {
        return new Skill<>(Object.class, name + "@MARK", e -> {}, () -> true);
    }

    public Skill<T> imitation() {
        return new Skill<T>(type, name, listener, trigger);
    }
}
