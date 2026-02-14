package legends.ultra.cool.addons.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import legends.ultra.cool.addons.hud.HudWidget;
import net.minecraft.client.MinecraftClient;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WidgetConfigManager {

    private WidgetConfigManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "legendsaddon_widgets.json";

    private static final Type MAP_TYPE = new TypeToken<Map<String, WidgetData>>() {}.getType();

    private static Map<String, WidgetData> widgetDataMap = new HashMap<>();
    private static boolean loaded = false;

    private static Path getConfigPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config/" + FILE_NAME);
    }

    // ------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------

    public static void load() {
        if (loaded) return;
        loaded = true;

        Path path = getConfigPath();
        if (!Files.exists(path)) {
            widgetDataMap = new HashMap<>();
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, WidgetData> loadedMap = GSON.fromJson(reader, MAP_TYPE);
            widgetDataMap = (loadedMap != null) ? loadedMap : new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            widgetDataMap = new HashMap<>();
        }
    }

    public static void save() {
        Path path = getConfigPath();
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(widgetDataMap, MAP_TYPE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean configExists() {
        return Files.exists(getConfigPath());
    }

    // ------------------------------------------------------------
    // Widget registration / persistence
    // ------------------------------------------------------------

    /**
     * Call once per widget at startup (after load()).
     *
     * If the widget exists in JSON: apply x/y/enabled to the widget.
     * If it does not exist: create an entry with widget defaults + default settings and save.
     */
    public static void registerWidget(HudWidget widget) {
        if (widget == null) return;
        load();

        String id = widget.getName();
        WidgetData data = widgetDataMap.get(id);

        if (data == null) {
            // First time ever: seed file with defaults from the widget object
            data = new WidgetData();
            data.x = widget.x;
            data.y = widget.y;
            data.enabled = widget.enabled;

            // Seed default settings from widget.getSettings()
            for (HudWidget.HudSetting s : safeSettings(widget)) {
                seedDefaultSettingIfMissing(data, s);
            }

            widgetDataMap.put(id, data);
            save();
            return;
        }

        // Exists already: apply saved state to widget
        widget.x = data.x;
        widget.y = data.y;
        widget.enabled = data.enabled;

        // Also ensure NEW settings added in an update get default values in JSON
        boolean changed = false;
        for (HudWidget.HudSetting s : safeSettings(widget)) {
            if (!data.settings.containsKey(s.key())) {
                seedDefaultSettingIfMissing(data, s);
                changed = true;
            }
        }
        if (changed) save();
    }

    /**
     * Call when you want to persist position/enabled changes (dragging/toggle).
     * Does NOT overwrite settings.
     */
    public static void updateWidget(HudWidget widget) {
        if (widget == null) return;
        load();

        String id = widget.getName();
        WidgetData data = widgetDataMap.get(id);
        if (data == null) {
            // If someone forgot to register, fallback:
            registerWidget(widget);
            data = widgetDataMap.get(id);
            if (data == null) return;
        }

        data.x = widget.x;
        data.y = widget.y;
        data.enabled = widget.enabled;
        save();
    }

    public static void resetAll() {
        load();
        widgetDataMap.clear();
        save();
    }

    public static void clearSetting(String widgetId, String key, boolean autosave) {
        load();
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) return;
        if (d.settings.remove(key) != null && autosave) save();
    }

    // ------------------------------------------------------------
    // Settings API (generic)
    // ------------------------------------------------------------

    public static boolean getBool(String widgetId, String key, boolean def) {
        load();
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) return def;
        JsonElement e = d.settings.get(key);
        if (e == null || !e.isJsonPrimitive()) return def;

        JsonPrimitive p = e.getAsJsonPrimitive();
        try {
            if (p.isBoolean()) return p.getAsBoolean();
            if (p.isNumber()) return p.getAsInt() != 0;
            if (p.isString()) return Boolean.parseBoolean(p.getAsString());
        } catch (Exception ignored) {}
        return def;
    }

    public static int getInt(String widgetId, String key, int def) {
        load();
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) return def;
        JsonElement e = d.settings.get(key);
        if (e == null || !e.isJsonPrimitive()) return def;

        JsonPrimitive p = e.getAsJsonPrimitive();
        try {
            if (p.isNumber()) return p.getAsInt();
            if (p.isString()) return Integer.parseInt(p.getAsString());
            if (p.isBoolean()) return p.getAsBoolean() ? 1 : 0;
        } catch (Exception ignored) {}
        return def;
    }

    public static float getFloat(String widgetId, String key, float def) {
        load();
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) return def;
        JsonElement e = d.settings.get(key);
        if (e == null || !e.isJsonPrimitive()) return def;

        JsonPrimitive p = e.getAsJsonPrimitive();
        try {
            if (p.isNumber()) return p.getAsFloat();
            if (p.isString()) return Float.parseFloat(p.getAsString());
            if (p.isBoolean()) return p.getAsBoolean() ? 1f : 0f;
        } catch (Exception ignored) {}
        return def;
    }

    public static String getString(String widgetId, String key, String def) {
        load();
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) return def;
        JsonElement e = d.settings.get(key);
        if (e == null || !e.isJsonPrimitive()) return def;

        try {
            return e.getAsString();
        } catch (Exception ignored) {}
        return def;
    }

    public static void setBool(String widgetId, String key, boolean value, boolean autosave) {
        load();
        WidgetData d = ensure(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    public static void setInt(String widgetId, String key, int value, boolean autosave) {
        load();
        WidgetData d = ensure(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    public static void setFloat(String widgetId, String key, float value, boolean autosave) {
        load();
        WidgetData d = ensure(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    public static void setString(String widgetId, String key, String value, boolean autosave) {
        load();
        WidgetData d = ensure(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    // ------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------

    private static WidgetData ensure(String widgetId) {
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) {
            d = new WidgetData();
            widgetDataMap.put(widgetId, d);
        }
        return d;
    }

    private static Iterable<HudWidget.HudSetting> safeSettings(HudWidget w) {
        var s = w.getSettings();
        return (s != null) ? s : java.util.List.of();
    }

    private static void seedDefaultSettingIfMissing(WidgetData data, HudWidget.HudSetting s) {
        // Only called when key is missing
        switch (s.type()) {
            case TOGGLE -> data.settings.put(s.key(), new JsonPrimitive(s.defaultBool()));
            case COLOR  -> data.settings.put(s.key(), new JsonPrimitive(s.defaultColor()));
            case SLIDER -> data.settings.put(s.key(), new JsonPrimitive(s.defaultFloat()));
        }
    }

    // ------------------------------------------------------------
    // Data model
    // ------------------------------------------------------------

    public static final class WidgetData {
        public double x;
        public double y;
        public boolean enabled;
        public Map<String, JsonElement> settings = new HashMap<>();
    }
}
