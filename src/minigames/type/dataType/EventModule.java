package minigames.type.dataType;

import arc.Events;
import arc.func.Cons;
import arc.util.Log;
import org.jetbrains.annotations.NotNull;

public class EventModule<T> {
    private boolean active;
    private final @NotNull Class<T> type;
    private final @NotNull Cons<T> listener;

    public EventModule(@NotNull Class<T> type, @NotNull Cons<T> listener) {
        active = false;
        this.type = type;
        this.listener = listener;
    }

    public @NotNull Class<T> getType() {
        return type;
    }

    public @NotNull Cons<T> getListener() {
        return listener;
    }

    public boolean active() {
        try {
            Events.on(type, listener);
            active = true;
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    public void disable() {
        if(active) {
            Events.remove(type, listener);
        }
    }

    public boolean isActive() {
        return active;
    }
}