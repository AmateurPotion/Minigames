package minigames.modes.solo;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.serialization.Jval;
import minigames.modes.GameMode;
import org.jetbrains.annotations.NotNull;

public class Solo implements GameMode {
    private final Jval config;
    private boolean active = false;

    public Solo() {
        config = Jval.newObject();
    }

    @Override
    public boolean active() {
        try {
            active = true;
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void disable() {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String name() {
        return "solo";
    }

    @Override
    public @NotNull Jval config() {
        return config;
    }
}
