package minigames.io;

import arc.Events;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.StorageBlock;
import minigames.type.dataType.PlayerData;
import minigames.modes.marathon.Marathon;

import static mindustry.content.Blocks.coreShard;
import static mindustry.content.Items.*;
import static minigames.Entry.db;

public class EventLoader {
    public void load() {
        tapEvents();
        pickUpEvents();
        playerJoinEvents();
        playerLeaveEvents();
        unitDestroyEvents();
        stateChangeEvents();

        db.loadSkillEvent();
    }

    public void tapEvents() {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.tile.build instanceof StorageBlock.StorageBuild build) {
                if(build.items.get(copper) >= 990 && build.items.get(lead) >= 990 && e.tile.block().size == 3) {
                    Call.setTile(e.tile, coreShard, e.tile.team(), 0);
                }
            }
        });
    }

    public void pickUpEvents() {
        Events.on(EventType.PickupEvent.class, e -> {
            if(e.build != null && e.unit == null && e.carrier instanceof Payloadc picker) {
                if(e.build instanceof Wall.WallBuild building) {
                    //Log.info(building.getDisplayName());
                    //e.build.team(e.carrier.team);
                    //Call.payloadDropped(e.carrier, e.carrier.x, e.carrier.y);
                }
            }
        });
    }

    public void playerJoinEvents() {
        Events.on(EventType.PlayerJoin.class, e -> {
            PlayerData data = db.find(e.player);
            if(data == null) {
                db.players.add(new PlayerData(e.player));
            } else {
                e.player.kick(db.bundle.getString(e.player, "kick.same-uuid"));
            }
        });
    }

    public void playerLeaveEvents() {
        Events.on(EventType.PlayerLeave.class, e -> {
            PlayerData data = db.players.find(d -> d.player == e.player);
            db.savePlayerData(data);
            db.players.remove(data);
        });
    }

    public void unitDestroyEvents() {
        Events.on(EventType.UnitDestroyEvent.class, e -> {
            Marathon mode = db.gameMode(Marathon.class, "marathon");
            if(mode.isActive()) {
                PlayerData data = db.players.find(p -> p.player.team() == e.unit.team());
                if(data != null) {
                    mode.respawn(data);
                    if(data.config.getInt("score", 0) > 0) mode.updateScore(data, -(int)(data.config.getInt("score", 0) * 0.1f));
                }
            }
        });
    }

    public void stateChangeEvents() {
        Events.on(EventType.StateChangeEvent.class, e -> {
            if(e.to == GameState.State.menu) {
                db.gameMode.forEach((name, mode) -> mode.disable());
            }
        });
    }
}
