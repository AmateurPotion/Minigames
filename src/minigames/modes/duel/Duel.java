package minigames.modes.duel;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.gen.Unit;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Duel {
    public final LinkedList<DuelStack> duelQueue;
    public DuelStack currentDuel;

    public Duel() {
        duelQueue = new LinkedList<>();
    }

    public void addDuel(DuelStack duel) {
        duelQueue.add(duel);
        updateDuel();
    }

    public void updateDuel() {
        try {
            DuelStack s = duelQueue.get(0);
        } catch (NoSuchElementException e) {
            Log.info(e);
        }
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
