package minigames.events;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.StorageBlock;
import minigames.function.PlayerManager;
import minigames.database.DataType.PlayerData;
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
        gameOverEvents();

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
            if(db.gameMode("marathon")) {
                e.player.team(Team.derelict);
                Call.menu(e.player.con, db.menu.get("marathon_join"), "Welcome!",
                        db.bundle.getString(e.player, "marathon.join"), new String[][]{{db.bundle.getString(e.player, "marathon.join.b1"), db.bundle.getString(e.player, "marathon.join.b2")}});
            }
        });
    }

    public void playerLeaveEvents() {
        Events.on(EventType.PlayerLeave.class, e -> {
            if(db.gameMode("marathon")) {
                Groups.unit.each(unit -> unit.team() == e.player.team(), Call::unitDespawn);
                PlayerData data = db.players.find(d -> d.player == e.player);
                db.savePlayerData(data);
                db.players.remove(data);
            }
        });
    }

    public void marathonLineArrivalEvents() {
        Events.on(EventList.MarathonLineArrivalEvent.class, e -> {
            // TODO 중복되는 연산 삭제
           if(db.gameMode("marathon")) {
               int line = e.tile().x > 100 ? (e.tile().y > 100 ? 1 : 2) : (e.tile().y > 100 ? 4 : 3);
               PlayerData data = db.players.find(p -> p.player == e.player());
               if(line == data.config.getInt("line@NS", 1)) {
                   data.config.put("line@NS", line != 4 ? line + 1 : 1);
                   Groups.unit.each(u -> u.team() == e.player().team(), Call::unitDespawn);
                   PlayerManager.changeUnit(e.player(), content.units().copy().filter(u -> u != UnitTypes.block).random());
                   switch (line) {
                       case 1 -> PlayerManager.setPosition(e.player(), Marathon.start2.getX(), Marathon.start2.getY());
                       case 2 -> PlayerManager.setPosition(e.player(), Marathon.start3.getX(), Marathon.start3.getY());
                       case 3 -> PlayerManager.setPosition(e.player(), Marathon.start4.getX(), Marathon.start4.getY());
                       case 4 -> PlayerManager.setPosition(e.player(), Marathon.start1.getX(), Marathon.start1.getY());
                   }
                   int score = (int)(e.player().unit().health() * e.player().unit().ammof());
                   Marathon.updateScore(data, score);
               }
           }
        });
    }

    public void unitDestroyEvents() {
        Events.on(EventType.UnitDestroyEvent.class, e -> {
            if(db.gameMode("marathon")) {
                PlayerData data = db.players.find(p -> p.player.team() == e.unit.team());
                if(data != null) {
                    Marathon.respawn(data);
                    if(data.config.getInt("score", 0) > 0) Marathon.updateScore(data, -(int)(data.config.getInt("score", 0) * 0.1f));
                }
            }
        });
    }

    public void gameOverEvents() {
        Events.on(EventType.GameOverEvent.class, e -> {
            db.gameMode("marathon", false);
        });
    }
}
