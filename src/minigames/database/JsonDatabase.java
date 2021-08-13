package minigames.database;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;

public class JsonDatabase {
    public final Fi databaseDirectory;
    public final Seq<PlayerData> players = Seq.with();

    public JsonDatabase() {
        databaseDirectory = new Fi(Vars.dataDirectory.path() + "/database", Vars.dataDirectory.type());
        databaseDirectory.mkdirs();
    }

    public boolean updatePlayerData(PlayerData data) {
        Fi path = new Fi(databaseDirectory.path() + "/" + data.player.uuid() + ".json");
        if(!path.exists()) {
            Jval pData = Jval.newObject();
            data.config.asObject().forEach(e -> {
                if(!e.key.contains("@NS")) {
                    pData.put(e.key, e.value);
                }
            });
            path.writeString(pData.toString(Jval.Jformat.formatted));
            return true;
        } else {
            return false;
        }
    }

    public @Nullable Jval getPlayerData(String uuid) {
        Jval data;
        Fi path = new Fi(databaseDirectory.path() + "/" + uuid + ".json");

        if(path.exists()) {
            data = Jval.read(path.reader());
            return data;
        } else {
            return null;
        }
    }
}
