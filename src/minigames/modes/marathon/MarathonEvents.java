package minigames.modes.marathon;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import minigames.type.dataType.Content;
import org.jetbrains.annotations.NotNull;

public class MarathonEvents implements Content {
    @Override
    public boolean active() {
        try {
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void disable() {

    }

    public static record MarathonLineArrivalEvent(@NotNull Player player, Unit unit, @NotNull Tile tile) {}
}
