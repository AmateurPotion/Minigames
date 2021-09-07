package minigames.io;

import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.input.Binding;
import mindustry.world.Tile;
import org.jetbrains.annotations.NotNull;

public class EventList {


    public static record BindingReceiveEvent(@NotNull Player player, @NotNull Binding binding) {}
}
