package minigames.modes.marathon;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Timer;
import mindustry.content.*;
import mindustry.ctype.ContentList;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.gen.Nulls;
import minigames.type.ctype.Skill;
import minigames.type.dataType.Content;
import minigames.type.dataType.PlayerData;
import minigames.utils.PlayerManager;
import minigames.modes.marathon.Marathon;

import java.util.Random;

import static minigames.Entry.db;

public class MarathonSkills implements Content {
    private Seq<Skill<?>> skills;
    private final Random random = new Random();
    private final Marathon mode = db.gameMode(Marathon.class, "marathon");

    @Override
    public boolean active() {
        try {
            Skill<?>
                    cryoShots = new Skill<>(EventType.TapEvent.class, "cryoShot", e -> {
                float damage = (int)(e.player.unit().health() / 100);
                e.player.unit().heal(-(damage * 10));
                for(int i = 0; i < 8; i++) {
                    Call.createBullet(Bullets.heavyCryoShot, e.player.team(), e.tile.x * 8, e.tile.y * 8
                            , 45 * i, (int)(damage / 8), 0.8f, 1);
                }
            }, null, UnitTypes.oct),
                    bigBang = new Skill<>(EventType.UnitDestroyEvent.class, "bigBang", e -> {
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
                            mode.updateScore(data, 0);
                        }
                    }, null, UnitTypes.crawler),
                    flash = new Skill<>(EventType.TapEvent.class, "flash", e -> {
                        if(e.player.dst(e.tile) < 20 * 8) {
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
                        if(e.tile != null && e.tile.block() == Blocks.air && e.tile.floor() == Blocks.stone) {
                            Call.setTile(e.tile, Blocks.phaseWall, e.player.team(), 0);
                            e.tile.build.health(10);
                        }
                    }, null, UnitTypes.mono),
                    hardest = new Skill<>(EventType.UnitChangeEvent.class, "hardest", e -> {
                        Unit target = Groups.unit.find(u -> u.team() == e.player.team());
                        if(e.unit == Nulls.unit && target != null && target.type() == UnitTypes.fortress) {
                            target.healthMultiplier(10);
                            target.apply(StatusEffects.unmoving, 3);
                            db.find(e.player).config.put("bombing@NS@DR", true);
                            Call.announce(e.player.con, db.bundle.getString(e.player, "skill.bombing.on"));
                        }
                    }, null, null),
                    cancelHardest = new Skill<>(EventType.UnitChangeEvent.class, "cancelHardest@ND", e -> {
                        e.unit.healthMultiplier(1);
                        e.unit.apply(StatusEffects.unmoving, 5);
                        db.find(e.player).config.put("bombing@NS@DR", false);
                        Call.announce(e.player.con, db.bundle.getString(e.player, "skill.bombing.off"));
                    }, null, UnitTypes.fortress),
                    bombing = new Skill<>(EventType.TapEvent.class, "bombing", e -> {
                        if((db.find(e.player).config.getBool("bombing@NS@DR", false) || db.find(e.player).config.getBool("bombing-repeat@NS@DR", false))) {
                            Unit target = Groups.unit.find(u -> u.team() == e.player.team());
                            if(target != null && target.ammof() == 1) {
                                target.ammo(0);
                                int range = 15, coolDown = 5;
                                target.apply(StatusEffects.unmoving, (coolDown + 6) * 100);
                                for(int i = 0; i < 361; i = i + 3){
                                    Call.label("" + Iconc.statusBlasted, coolDown + 6, (float) (e.tile.getX() + Math.cos(i) * (range * 8)), (float) (e.tile.getY() + Math.sin(i) * (range * 8)));
                                }
                                Call.label("[scarlet]" + Iconc.warning, coolDown + 6, e.tile.getX(), e.tile.getY());
                                new Timer().scheduleTask(new Timer.Task() {
                                    @Override
                                    public void run() {
                                        new Timer().scheduleTask(new Timer.Task() {
                                            @Override
                                            public void run() {
                                                int explodeRange = 3, rRange = range - explodeRange;
                                                int sCount = 5 + random.nextInt(10);
                                                for(int i = 0; i < sCount; i++) {
                                                    float x = e.tile.getX() + 8 * (random.nextInt(rRange * 2) - rRange), y = e.tile.getY() + 8 * (random.nextInt(rRange * 2) - rRange);
                                                    Call.effect(Fx.plasticExplosionFlak, x, y, 10, Color.red);
                                                    Groups.unit.each(u -> u.dst(x, y) < explodeRange * 8, unit -> {
                                                        unit.damageMultiplier(unit.damageMultiplier() * 2);
                                                        unit.healthMultiplier(unit.healthMultiplier() / 2);
                                                        if(unit.health() > 100) {
                                                            unit.health(unit.health() - 150);
                                                        } else {
                                                            Log.info("reuse");
                                                            unit.health(0);
                                                            db.find(e.player).config.put("bombing-repeat@NS@DR", true);
                                                            db.skill("bombing").listener().get(e);
                                                        }
                                                    });
                                                }
                                            }
                                        }, 0, 0.2f, 2);
                                    }
                                }, coolDown, 2, 2);
                            }
                        }
                    }, null, null),
                    missileShots = new Skill<>(EventType.TapEvent.class, "missileShots", e -> {
                        if(Groups.player.size() > 1) {
                            if(Groups.unit.contains(u -> u != e.player.unit() && u.dst(e.tile.worldx(), e.tile.worldy()) <= 12 * 8f)) return;
                            Seq<Unit> seq = Groups.unit.copy(new Seq<>()).filter(u -> u != e.player.unit()).sort((u1, u2) -> Float.compare(u1.dst(e.player.x, e.player.y),u2.dst(e.player.x, e.player.y)));
                            seq.reverse();
                            float ux = seq.peek().x;
                            float uy = seq.peek().y;
                            float tx = e.tile.worldx();
                            float ty = e.tile.worldy();
                            float px = e.player.x;
                            float py = e.player.y;

                            float centerx = (ux + tx + px) / 3;
                            float centery = (uy + ty + py) / 3;

                            for(int i = 0; i < 360; i += Mathf.random(12, 30)){
                                e.player.unit().heal(-0.01f * e.player.unit().healthf() * i);
                                Call.createBullet(Bullets.missileIncendiary, e.player.team(), centerx, centery,
                                        Mathf.pow(Mathf.pi, Mathf.E) * i * i * Mathf.random(2, 5), 0, i * Mathf.pow(Mathf.pi, Mathf.E) / 5000f,i/100f);
                            }
                        }
                    }, Team.derelict, null);

            skills = Seq.with(
                    cryoShots, cryoShots.imitation(UnitTypes.aegires),
                    bigBang,
                    flash,
                    metaShield,
                    hardest, cancelHardest, bombing,
                    missileShots
            );
            skills.each(db::registerSkill);
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    public void disable() {
        db.skills.removeAll(skills);
    }
}
