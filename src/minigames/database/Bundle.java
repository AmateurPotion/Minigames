package minigames.database;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class Bundle {
    public final Seq<ResourceBundle> bundles;
    private final HashMap<String, Locale> localeMap;

    public Bundle() {
        // init
        bundles = Seq.with();
        localeMap = new HashMap<>();

        // load
        bundles.add(ResourceBundle.getBundle("bundle.bundle", Locale.KOREA));
        localeMap.put("ko", Locale.KOREA);
        bundles.add(ResourceBundle.getBundle("bundle.bundle", Locale.ENGLISH));
        localeMap.put("en", Locale.ENGLISH);
    }

    public ResourceBundle bundle(Locale locale) {
        return bundles.find(b -> b.getLocale() == locale);
    }

    public String getString(Player player, String key) {
        if(bundles.contains(bundle -> localeMap.get(player.locale()) != null && localeMap.get(player.locale()) == bundle.getLocale())) {
            return bundle(localeMap.get(player.locale())).getString(key);
        } else {
            return bundle(Locale.ENGLISH).getString(key);
        }
    }
}
