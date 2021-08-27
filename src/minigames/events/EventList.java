package minigames.events;

import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import minigames.ctype.Skill;

public class EventList {
    public static record MarathonLineArrivalEvent(Player player, Unit unit, Tile tile) {
    }
}
