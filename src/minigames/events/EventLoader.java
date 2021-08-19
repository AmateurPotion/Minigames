package minigames.events;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Payloadc;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.StorageBlock;
import minigames.function.PlayerManager;
import minigames.database.PlayerData;
import minigames.modes.Marathon;

import static mindustry.Vars.content;
import static mindustry.Vars.state;
import static mindustry.content.Blocks.coreShard;
import static mindustry.content.Items.*;
import static minigames.Entry.jdb;
import static minigames.modes.Marathon.updateScore;

public class EventLoader {
    public void load() {
        tapEvents();
        pickUpEvents();
        unitCreateEvents();
        playerJoinEvents();
        playerLeaveEvents();
        playEvents();
        marathonLineArrivalEvents();
        unitDestroyEvents();
    }

    public void tapEvents() {
        Events.on(EventType.TapEvent.class, e -> {
            //Log.info(e.tile.x + " / " + e.tile.y + " / " + e.tile.floor().name);
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
                    Log.info(building.getDisplayName());
                    //e.build.team(e.carrier.team);
                    //Call.payloadDropped(e.carrier, e.carrier.x, e.carrier.y);
                }
            }
        });
    }

    public void unitCreateEvents() {
        Events.on(EventType.UnitChangeEvent.class, e -> {
        });
    }

    public void playerJoinEvents() {
        Events.on(EventType.PlayerJoin.class, e-> {
            Jval pData = jdb.getPlayerData(e.player.uuid());
            jdb.players.add(new PlayerData(e.player));
            if(pData != null) {
                e.player.name(pData.getString("name"));
            }
            if(Core.settings.getBool("marathon", false)) {
                e.player.team(Team.derelict);
                Call.infoMessage(e.player.con, """
                        current mode : blood marathon
                        [/join] to join this game
                        out of line : - 1% score
                        death : - 10% score
                        [/respawn] : -200 score
                        [/spec] : change to spectate mode
                        """);
            }
        });
    }

    public void playerLeaveEvents() {
        Events.on(EventType.PlayerLeave.class, e -> {
            if(Core.settings.getBool("marathon", false)) {
                Groups.unit.each(unit -> unit.team() == e.player.team(), Call::unitDespawn);
            }
            jdb.updatePlayerData(jdb.players.find(data -> data.player == e.player));
            jdb.players.remove(data -> data.player == e.player);
        });
    }

    public void playEvents() {
        Events.on(EventType.PlayEvent.class, e -> {
            if(!state.map.name().equals("marathon")) {
                Core.settings.put("marathon", false);
            }
        });
    }

    public void marathonLineArrivalEvents() {
        Events.on(EventList.MarathonLineArrivalEvent.class, e -> {
            // TODO 중복되는 연산 삭제
           if(db.gameMode("marathon")) {
               int line = e.tile().x > 100 ? (e.tile().y > 100 ? 1 : 2) : (e.tile().y > 100 ? 4 : 3);
               PlayerData data = jdb.players.find(p -> p.player == e.player());
               if(line == data.config.getInt("line@NS", 1)) {
                   data.config.put("line@NS", line != 4 ? line + 1 : 1);
                   switch (line) {
                       case 1 -> PlayerManager.setPosition(e.player(), Marathon.start2.getX(), Marathon.start2.getY());
                       case 2 -> PlayerManager.setPosition(e.player(), Marathon.start3.getX(), Marathon.start3.getY());
                       case 3 -> PlayerManager.setPosition(e.player(), Marathon.start4.getX(), Marathon.start4.getY());
                       case 4 -> PlayerManager.setPosition(e.player(), Marathon.start1.getX(), Marathon.start1.getY());
                   }
                   int score = (int)e.player().unit().health() + (int)e.player().unit().ammo();
                   Marathon.updateScore(data, score);
                   PlayerManager.changeUnit(e.player(), content.units().copy().filter(u -> u != UnitTypes.omura && u != UnitTypes.block).random());
               } else {
                   Marathon.home(data);
               }
           }
        });
    }

    public void unitDestroyEvents() {
        Events.on(EventType.UnitDestroyEvent.class, e -> {
            if(Core.settings.getBool("marathon", false)) {
                PlayerData data = jdb.players.find(p -> p.player.team() == e.unit.team());
                if(data != null) {
                    updateScore(data, -(int)(data.config.getInt("score", 0) * 0.1f));
                    Marathon.respawn(data);
                }
            }
        });
    }
}
