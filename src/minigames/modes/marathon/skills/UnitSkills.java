package minigames.modes.marathon.skills;

import arc.Events;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer;
import mindustry.content.*;
import mindustry.ctype.ContentList;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.gen.Nulls;
import minigames.ctype.Skill;
import minigames.database.DataType.PlayerData;
import minigames.events.EventList;
import minigames.function.PlayerManager;
import minigames.modes.marathon.Marathon;

import java.util.Objects;
import java.util.Random;

import static minigames.Entry.db;

public class UnitSkills implements ContentList {
    Random random = new Random();

    @Override
    public void load() {
        Skill<?>
        cryoShots = new Skill<>(EventType.TapEvent.class, "cryoShot", e -> {
            if(db.gameMode("marathon")) {
                float damage = (int)(e.player.unit().health() / 100);
                e.player.unit().heal(-(damage * 10));
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
                if(data.isReady("flash")) {
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
                    Call.infoToast(e.player.con, "Skill flash is in coolTime(" + data.coolTimes.get("flash") + ").", 1.5f);
                }
            }
        }, null, UnitTypes.nova),
        metaShield = new Skill<>(EventType.TapEvent.class, "metaShield", e -> {
            if(db.gameMode("marathon")) {
                if(e.tile != null && e.tile.block() == Blocks.air && e.tile.floor() == Blocks.stone) {
                    Call.setTile(e.tile, Blocks.phaseWall, e.player.team(), 0);
                    e.tile.build.health(10);
                }
            }
        }, null, UnitTypes.mono),
        hardest = new Skill<>(EventType.UnitChangeEvent.class, "hardest", e -> {
            if(db.gameMode("marathon")) {
                Unit target = Groups.unit.find(u -> u.team() == e.player.team());
                if(e.unit == Nulls.unit && target != null && target.type() == UnitTypes.fortress) {
                    target.healthMultiplier(10);
                    target.apply(StatusEffects.unmoving, Float.MAX_VALUE);
                    db.find(e.player).config.put("shellingMode@NS", true);
                    Call.announce(e.player.con, db.bundle.getString(e.player, "marathon.skill.shelling.on"));
                }
            }
        }, null, null),
        cancelHardest = new Skill<>(EventType.UnitChangeEvent.class, "cancelHardest", e -> {
            if(db.gameMode("marathon")) {
                e.unit.healthMultiplier(1);
                e.unit.unapply(StatusEffects.unmoving);
                e.unit.apply(StatusEffects.unmoving, 5);
                db.find(e.player).config.put("shellingMode@NS", false);
                Call.announce(e.player.con, db.bundle.getString(e.player, "marathon.skill.shelling.off"));
            }
        }, null, UnitTypes.fortress),
        shellingMode = new Skill<>(EventType.TapEvent.class, "shellingMode", e -> {
            if(db.gameMode("marathon") && db.find(e.player).config.getBool("shellingMode@NS", false)) {
                PlayerData data = db.find(e.player);
                Unit target = Groups.unit.find(u -> u.team() == e.player.team());
                if(target.ammof() == 1) {
                    //target.ammo(0);
                    int range = 15, duration = 5;
                    target.apply(StatusEffects.unmoving, duration + 6);
                    for(int i = -1; i < 2; i++) {
                        if(i != 0) {
                            Call.label("" + Iconc.statusBlasted, duration + 6, e.tile.getX() + (i * 8 * range), e.tile.getY());
                            Call.label("" + Iconc.statusBlasted, duration + 6, e.tile.getX(), e.tile.getY() + (i * 8 * range));
                            for(int j = -1; j < 2; j++) {
                                if(j != 0) {
                                    float t = (float) Math.sqrt(range * range / 2);
                                    Call.label("" + Iconc.statusBlasted, duration + 6, e.tile.getX() + (i * 8 * t), e.tile.getY() + (j * 8 * t));
                                }
                            }
                        }
                    }
                    Call.label("[scarlet]" + Iconc.warning, duration + 6, e.tile.getX(), e.tile.getY());
                    new Timer().scheduleTask(new Timer.Task() {
                        @Override
                        public void run() {
                            new Timer().scheduleTask(new Timer.Task() {
                                @Override
                                public void run() {
                                    int explodeRange = 2, rRange = range - explodeRange;
                                    int sCount = 5 + random.nextInt(10);
                                    for(int i = 0; i < sCount; i++) {
                                        float x = e.tile.getX() + 8 * (random.nextInt(rRange * 2) - rRange), y = e.tile.getY() + 8 * (random.nextInt(rRange * 2) - rRange);
                                        Call.effect(Fx.plasticExplosionFlak, x, y, 0, Color.red);
                                        Groups.unit.each(u -> u.dst(x, y) < explodeRange * 8, unit -> {
                                            unit.damageMultiplier(unit.damageMultiplier() * 2);
                                            unit.healthMultiplier(unit.healthMultiplier() / 2);
                                            if(unit.health() > 100) {
                                                unit.health(unit.health() - 100);
                                            } else {
                                                Log.info("reuse");
                                                unit.health(0);
                                                Events.fire(new EventList.SkillReuseEvent<>(EventType.TapEvent.class, db.skills.find(skill -> Objects.equals(skill.name(), "shellingMode")), e));
                                            }
                                        });
                                    }
                                }
                            }, 0, 0.2f, 2);
                        }
                    }, duration, 2, 2);
                }
            }
        }, null, null);

        Seq<Skill<?>> skills = Seq.with(
                cryoShots, cryoShots.imitation(UnitTypes.aegires),
                bigBang,
                flash,
                metaShield,
                hardest, cancelHardest, shellingMode
        );

        db.skills.addAll(skills);
    }
}
