package minigames.type.dataType;

public interface Content {
    // This method is call after all databases have been loaded.
    void load();
    boolean active();
    void disable();
}
