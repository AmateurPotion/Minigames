package minigames.database;

import arc.struct.Seq;
import mindustry.gen.Player;
import org.jetbrains.annotations.NotNull;

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
        localeMap.put("ko_KR", Locale.KOREA);
        bundles.add(ResourceBundle.getBundle("bundle.bundle", Locale.ENGLISH));
        localeMap.put("en", Locale.ENGLISH);
        localeMap.put("en_US", Locale.ENGLISH);
    }

    public ResourceBundle bundle(Locale locale) {
        return bundles.find(b -> b.getLocale() == locale);
    }

    public String getString(@NotNull String locale, @NotNull String key) {
        if(localeMap.get(locale) != null && bundles.contains(bundle -> localeMap.get(locale) == bundle.getLocale())) {
            return bundle(localeMap.get(locale)).getString(key);
        } else {
            return bundle(Locale.getDefault()).getString(key);
        }
    }

    public String getString(@NotNull Player player, @NotNull String key) {
        return getString(player.locale(), key);
    }

    public String getString(@NotNull String locale, @NotNull String key, @NotNull String... args) {
        String result = getString(locale, key);
        for(int i = 0; i < args.length; i++) {
            if(result.contains("{" + i + "}")) {
                result = result.replaceAll("\\{" + i + "}", args[i]);
            }
        }
        return result;
    }

    public String getString(@NotNull Player player, @NotNull String key, @NotNull String... args) {
        return getString(player.locale(), key, args);
    }
}
