package minigames.type.dataType;

import arc.util.serialization.Jval;
import org.jetbrains.annotations.NotNull;

public class Property<T> {
    public T val;
    public Jval jval; // json 오브젝트

    public Property(@NotNull T val, Jval jval) {
        this.val = val;
        this.jval = jval;
    }
}
