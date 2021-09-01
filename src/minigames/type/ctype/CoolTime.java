package minigames.type.ctype;

import java.util.HashMap;

public class CoolTime {
    public final HashMap<String, Float> coolTimes;
    public CoolTime() {
        coolTimes = new HashMap<>();
    }

    public Float get(String name) {
        if(coolTimes.get(name) != null) {
            return coolTimes.get(name);
        }
        return 0f;
    }

    public void set(String name, float coolTime) {
        coolTimes.put(name, coolTime);
    }

    public float add(String name, float coolTime) {
        Float t = coolTimes.get(name);
        if(t != null) {
            t = t + coolTime;
            coolTimes.put(name, t);
            return t;
        } else {
            coolTimes.put(name, coolTime);
            return coolTime;
        }
    }

    public void proceed(float time) {
        coolTimes.forEach((name, remainTime) -> {
            if(remainTime < time) {
                coolTimes.put(name, 0f);
            } else {
                add(name, -time);
            }
        });
    }
}

