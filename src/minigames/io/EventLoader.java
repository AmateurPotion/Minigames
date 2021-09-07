package minigames.io;

import arc.Events;
import arc.math.geom.Position;
import arc.util.Log;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.StorageBlock;
import minigames.modes.marathon.MarathonEvents;
import minigames.utils.PlayerManager;
import minigames.type.dataType.PlayerData;
import minigames.modes.marathon.Marathon;

import java.util.Objects;
import static mindustry.Vars.content;
import static mindustry.content.Blocks.coreShard;
import static mindustry.content.Items.*;
import static minigames.Entry.db;

public class EventLoader {
    public void load() {
        tapEvents();
        pickUpEvents();
        playerJoinEvents();
        playerLeaveEvents();
        marathonLineArrivalEvents();
        unitDestroyEvents();

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
            PlayerData data = db.players.find(d -> Objects.equals(d.player.uuid(), e.player.uuid()));
            if(data != null) {
                db.savePlayerData(data);
                db.players.remove(data);
            }
            db.players.add(new PlayerData(e.player));
            if(db.gameMode("marathon").isActive()) {
                e.player.team(Team.derelict);
                Log.info(e.player.locale());
                Call.menu(e.player.con, db.menu.get("marathon_join"), "Welcome!",
                        db.bundle.getString(e.player, "marathon.join"), new String[][]{{db.bundle.getString(e.player, "marathon.join.b1"), db.bundle.getString(e.player, "marathon.join.b2")}});
            }
        });
    }

    public void playerLeaveEvents() {
        Events.on(EventType.PlayerLeave.class, e -> {
            if(db.gameMode("marathon").isActive()) {
                Groups.unit.each(unit -> unit.team() == e.player.team(), Call::unitDespawn);
                PlayerData data = db.players.find(d -> d.player == e.player);
                db.savePlayerData(data);
                db.players.remove(data);
            }
        });
    }

    public void marathonLineArrivalEvents() {
        Events.on(MarathonEvents.MarathonLineArrivalEvent.class, e -> {
            Marathon mode = db.gameMode(Marathon.class, "marathon");
            if(mode.isActive()) {
               int line = e.tile().x > 100 ? (e.tile().y > 100 ? 1 : 2) : (e.tile().y > 100 ? 4 : 3);
               PlayerData data = db.players.find(p -> p.player == e.player());
               if(line == data.config.getInt("line@NS", 1)) {
                   data.config.put("line@NS", line != 4 ? line + 1 : 1);
                   Groups.unit.each(u -> u.team() == e.player().team(), Call::unitDespawn);
                   PlayerManager.changeUnit(e.player(), content.units().copy().filter(u -> u != UnitTypes.block).random());

                   Position start = mode.getStartPoint(line + 1);
                   if(line == 4) {
                       start = mode.getStartPoint(1);
                       int refineChance = data.config.getInt("refineChance", 0) + 1;
                       data.config.put("refineChance", refineChance);
                       Call.infoToast(data.player.con(), db.bundle.getString(data.player, "refine.gainChance") + refineChance, 2);
                   }
                   PlayerManager.setPosition(e.player(), start.getX(), start.getY());

                   int score = (int)(e.player().unit().health() * e.player().unit().ammof() * 2);
                   mode.updateScore(data, score);
               }
           }
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
}
