package minigames.events;

import arc.util.Nullable;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;

public class EventList {
    public static record MarathonLineArrivalEvent(Player player, Unit unit, Tile tile) {
    }
}
