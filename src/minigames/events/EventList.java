package minigames.events;

import arc.util.Nullable;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import minigames.ctype.Skill;

public class EventList {
    public static record MarathonLineArrivalEvent(Player player, Unit unit, Tile tile) {
    }
    public static record SkillReuseEvent<T>(Class<T> type, Skill<?> skill, T arg) {
    }
}
