package minigames.utils;

import arc.struct.Seq;
import mindustry.Vars;
import mindustry.net.Packet;
import minigames.type.dataType.PacketListener;

import java.util.HashMap;
import java.util.Objects;

public class PacketManager {
    private static final HashMap<Class<?>, Seq<PacketListener>> packetListeners = new HashMap<>();

    public static <T extends Packet> void addPacketListener(PacketListener<T> listener) {
        if(!packetListeners.containsKey(listener.type())) {
            packetListeners.put(listener.type(), Seq.with());
        }
        packetListeners.get(listener.type()).add(listener);
        updatePacket(listener.type());
    }

    public static void removePacketListener(String name) {
        packetListeners.forEach((type, listener) -> {
            if(listener.contains(l -> Objects.equals(l.name(), name))) {
                listener.removeAll(l -> Objects.equals(l.name(), name));
                updatePacket(type);
            }
        });
    }

    public static void removePacketListener(PacketListener<? extends Packet> listener) {
        if(packetListeners.containsKey(listener.type())) {
            packetListeners.get(listener.type()).remove(listener);
            updatePacket(listener.type());
        }
    }

    private static void updatePacket(Class type) {
        Vars.net.handleServer(type, (con, packet) -> packetListeners.get(type).each(listener -> listener.listener().get(con, packet)));
    }
}
