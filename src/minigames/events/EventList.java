package minigames.events;

import mindustry.gen.Player;
import mindustry.world.Tile;

public class EventList {
    public record MarathonLineArrivalEvent(Player player, Tile tile) {
    }
}
