package minigames.modes.marathon;

import arc.func.Cons;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import minigames.type.dataType.Content;
import minigames.type.dataType.EventModule;
import minigames.type.dataType.PlayerData;
import minigames.utils.PlayerManager;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.content;
import static minigames.Entry.db;

public class MarathonEvents implements Content {
    private Marathon mode;
    private final Seq<EventModule<?>> events;

    public MarathonEvents() {
        // init
        events = Seq.with();
    }

    private <T> void addEvent(Class<T> type, Cons<T> listener) {
        EventModule<T> target = (EventModule<T>) events.find(module -> module.getType() == type);
        if(target == null) {
            events.add(new EventModule<>(type, listener));
        } else {
            Cons<T> rl = e -> {
                target.getListener().get(e);
                listener.get(e);
            };
            EventModule<T> result = new EventModule<>(type, rl);
            events.add(result);
        }
    }

    @Override
    public void load() {
        mode = db.gameMode(Marathon.class, "marathon");

        // Event Loading
        addEvent(EventType.PlayerJoin.class, e -> {
            e.player.team(Team.derelict);
            Log.info(e.player.locale());
            Call.menu(e.player.con, db.menu.get("marathon_join"), "Welcome!",
                    db.bundle.getString(e.player, "marathon.join"), new String[][]{{db.bundle.getString(e.player, "marathon.join.b1"), db.bundle.getString(e.player, "marathon.join.b2")}});
        });

        addEvent(MarathonEvents.MarathonLineArrivalEvent.class, e -> {
            int line = e.tile().x > 100 ? (e.tile().y > 100 ? 1 : 2) : (e.tile().y > 100 ? 4 : 3);
            PlayerData data = db.players.find(p -> p.player == e.player());
            if(line == data.config.getInt("line@NS", 1)) {
                data.config.put("line@NS", line != 4 ? line + 1 : 1);
                Groups.unit.each(u -> u.team() == e.player().team(), Call::unitDespawn);
                PlayerManager.changeUnit(e.player(), content.units().copy().filter(u -> u != UnitTypes.block).random());

                Position start;
                if(line == 4) {
                    start = mode.getStartPoint(0);
                    int refineChance = data.config.getInt("refineChance", 0) + 1;
                    data.config.put("refineChance", refineChance);
                    Call.infoToast(data.player.con(), db.bundle.getString(data.player, "refine.gainChance") + refineChance, 2);
                } else {
                    start = mode.getStartPoint(line);
                }
                PlayerManager.setPosition(e.player(), start.getX(), start.getY());

                int score = (int)(e.player().unit().health() * e.player().unit().ammof() * 2);
                mode.updateScore(data, score);
            }
        });

        addEvent(EventType.PlayerLeave.class, e -> {
            Groups.unit.each(unit -> unit.team() == e.player.team(), Call::unitDespawn);
        });
    }

    // default methods
    @Override
    public boolean active() {
        try {
            events.each(EventModule::active);
            return true;
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void disable() {
        events.each(EventModule::disable);
    }

    public static record MarathonLineArrivalEvent(@NotNull Player player, Unit unit, @NotNull Tile tile) {}
}
