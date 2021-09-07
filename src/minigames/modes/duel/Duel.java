package minigames.modes.duel;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import minigames.modes.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Duel implements GameMode {
    public final LinkedList<DuelStack> duelQueue;
    public DuelStack currentDuel;
    private boolean active = false;
    private final Jval config;

    public Duel() {
        config = Jval.newObject();
        duelQueue = new LinkedList<>();
    }



    public void addDuel(DuelStack duel) {
        duelQueue.add(duel);
        updateDuel();
    }

    public void updateDuel() {
        try {
            DuelStack duel = duelQueue.get(0);
        } catch (NoSuchElementException e) {
            Log.info(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean active() {
        try{
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
        return "duel";
    }

    @Override
    public @NotNull Jval config() {
        return config;
    }

    public static class DuelStack {
        public final Player red;
        public final Player blue;
        public Unit redUnit, blueUnit;

        public DuelStack(Player red, Player blue) {
            this.red = red;
            this.blue = blue;
        }
    }
}
