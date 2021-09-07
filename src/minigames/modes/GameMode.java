package minigames.modes;

import arc.util.serialization.Jval;
import minigames.type.dataType.Content;
import org.jetbrains.annotations.NotNull;

public interface GameMode extends Content {
    boolean isActive();
    String name();
    @NotNull Jval config();
}
