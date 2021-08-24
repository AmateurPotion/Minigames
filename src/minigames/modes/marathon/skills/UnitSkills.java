package minigames.modes.marathon.skills;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.ctype.ContentList;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import minigames.ctype.Skill;
import minigames.database.DataType.PlayerData;
import minigames.function.PlayerManager;
import minigames.modes.marathon.Marathon;

import static minigames.Entry.db;

public class UnitSkills implements ContentList {
    @Override
    public void load() {
        Skill<?> cryoShots = new Skill<>(EventType.TapEvent.class, "cryoShot", e -> {
            if(db.gameMode("marathon")) {
                float damage = (int)(e.player.unit().health() / 100);
                if(e.player.unit().type() == UnitTypes.oct) {
                    e.player.unit().heal(-(damage * 10));
                }
                for(int i = 0; i < 8; i++) {
                    Call.createBullet(Bullets.heavyCryoShot, e.player.team(), e.tile.x * 8, e.tile.y * 8
                            , 45 * i, (int)(damage / 8), 0.8f, 1);
                }
            }
        }, null, UnitTypes.oct),
        bigBang = new Skill<>(EventType.UnitDestroyEvent.class, "bigBang", e -> {
            if(db.gameMode("marathon")) {
                Player currentPlayer = db.getPlayer(e.unit.team());
                PlayerData data = currentPlayer != null ? db.players.find(d -> d.player == currentPlayer) : null;
                if(data != null) {
                    // restore score
                    data.config.put("score", data.config.getInt("score", 0) * 10 / 9);

                    // kill all
                    Groups.unit.each(u -> u.team() != Team.sharded && u.team() != Team.derelict, u -> {
                        data.config.put("score", data.config.getInt("score", 0) + u.health());
                        u.health(0);
                    });

                    // update score
                    Marathon.updateScore(data, 0);
                }
            }
        }, null, UnitTypes.crawler),
        flash = new Skill<>(EventType.TapEvent.class, "flash", e -> {
            if(db.gameMode("marathon") && e.player.dst(e.tile) < 20 * 8) {
                PlayerData data = db.find(e.player);
                if(data.coolTimes.get("flash") == 0f) {
                    Groups.unit.each(u -> u.dst(e.tile) < 30 * 8 && u.team() != e.player.team(), unit -> {
                        if(unit.ammof() == 0) {
                            unit.ammo(unit.type().ammoCapacity);
                            Call.effect(Fx.greenBomb, unit.x(), unit.y(), 0, Color.red);
                        } else {
                            unit.ammo(0f);
                            Call.effect(Fx.instBomb, unit.x(), unit.y(), 0, Color.red);
                        }
                    });
                    data.coolTimes.add("flash", 10f);
                    PlayerManager.setPosition(e.player, e.tile.getX(), e.tile.getY());
                } else {
                    Call.announce(e.player.con, "Skill flash is in coolTime(" + data.coolTimes.get("flash") + ").");
                }
            }
        }, null, UnitTypes.nova),
        metaShield = new Skill<>(EventType.TapEvent.class, "metaShield", e -> {
            if(e.tile != null && e.tile.block() == Blocks.air && e.tile.floor() == Blocks.stone) {
                Call.setTile(e.tile, Blocks.phaseWall, e.player.team(), 0);
                e.tile.build.health(10);
            }
        }, null, UnitTypes.mono);

        Seq<Skill<?>> skills = Seq.with(
                cryoShots, cryoShots.imitation(UnitTypes.aegires),
                bigBang,
                flash,
                metaShield
        );

        db.skills.addAll(skills);
    }
}
