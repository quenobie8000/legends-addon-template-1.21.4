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

public class WidgetConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "legendsaddon_widgets.json";

    private static Map<String, WidgetData> widgetDataMap = new HashMap<>();

    private static Path getConfigPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve(FILE_NAME);
    }

    public static void load() {
        Path path = getConfigPath();
        if (!Files.exists(path)) return;

        try (Reader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<Map<String, WidgetData>>() {}.getType();
            Map<String, WidgetData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) widgetDataMap = loaded;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Migrate old fixed fields -> settings map (backwards compatible)
        for (WidgetData d : widgetDataMap.values()) {
            if (d != null) d.migrateLegacyIntoSettings();
        }
    }

    public static void save() {
        Path path = getConfigPath();
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(widgetDataMap, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Ensures we always have a WidgetData entry for this widget id/name. */
    private static WidgetData dataFor(String widgetId) {
        WidgetData d = widgetDataMap.get(widgetId);
        if (d == null) {
            d = new WidgetData();
            widgetDataMap.put(widgetId, d);
        }
        d.migrateLegacyIntoSettings(); // safe no-op if already migrated
        return d;
    }

    // ------------------------------------------------------------
    // Public API used by your HUD system
    // ------------------------------------------------------------

    public static void registerWidget(HudWidget widget) {
        if (widget == null) return;

        String id = widget.getName();
        WidgetData data = dataFor(id);

        // Base widget fields
        widget.x = data.x;
        widget.y = data.y;
        widget.enabled = data.enabled;

        // Existing style fields (still supported)
        widget.style.drawBackground = getBool(id, "drawBackground", widget.style.drawBackground);
        widget.style.backgroundColor = getInt(id, "backgroundColor", widget.style.backgroundColor);
        widget.style.textColor = getInt(id, "textColor", widget.style.textColor);
        widget.style.drawBorder = getBool(id, "drawBorder", widget.style.drawBorder);
        widget.style.borderColor = getInt(id, "borderColor", widget.style.borderColor);

        // Ensure defaults exist in the file after first register (optional but handy)
        setBool(id, "drawBackground", widget.style.drawBackground, false);
        setInt(id, "backgroundColor", widget.style.backgroundColor, false);
        setInt(id, "textColor", widget.style.textColor, false);
        setBool(id, "drawBorder", widget.style.drawBorder, false);
        setInt(id, "borderColor", widget.style.borderColor, false);
    }

    /**
     * Keeps your old call sites working:
     * - saves position/enabled
     * - saves the standard WidgetStyle fields
     * - writes to disk
     */
    public static void updateWidget(HudWidget widget) {
        if (widget == null) return;

        String id = widget.getName();
        WidgetData data = dataFor(id);

        data.x = widget.x;
        data.y = widget.y;
        data.enabled = widget.isEnabled();

        // Store style in settings map (so the modal can keep working)
        setBool(id, "drawBackground", widget.style.drawBackground, false);
        setInt(id, "backgroundColor", widget.style.backgroundColor, false);
        setInt(id, "textColor", widget.style.textColor, false);
        setBool(id, "drawBorder", widget.style.drawBorder, false);
        setInt(id, "borderColor", widget.style.borderColor, false);

        save();
    }

    public static void resetAll() {
        widgetDataMap.clear();
        save();
    }

    // ------------------------------------------------------------
    // Generic per-widget setting getters/setters
    // These are what your dynamic settings UI should use.
    // ------------------------------------------------------------

    public static boolean getBool(HudWidget widget, String key, boolean def) {
        return getBool(widget.getName(), key, def);
    }

    public static int getInt(HudWidget widget, String key, int def) {
        return getInt(widget.getName(), key, def);
    }

    public static float getFloat(HudWidget widget, String key, float def) {
        return getFloat(widget.getName(), key, def);
    }

    public static String getString(HudWidget widget, String key, String def) {
        return getString(widget.getName(), key, def);
    }

    public static void setBool(HudWidget widget, String key, boolean value) {
        setBool(widget.getName(), key, value, true);
    }

    public static void setInt(HudWidget widget, String key, int value) {
        setInt(widget.getName(), key, value, true);
    }

    public static void setFloat(HudWidget widget, String key, float value) {
        setFloat(widget.getName(), key, value, true);
    }

    public static void setString(HudWidget widget, String key, String value) {
        setString(widget.getName(), key, value, true);
    }

    // --- String widgetId overloads (useful in mixins) ---

    public static boolean getBool(String widgetId, String key, boolean def) {
        WidgetData d = dataFor(widgetId);
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
        WidgetData d = dataFor(widgetId);
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
        WidgetData d = dataFor(widgetId);
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
        WidgetData d = dataFor(widgetId);
        JsonElement e = d.settings.get(key);
        if (e == null || !e.isJsonPrimitive()) return def;
        JsonPrimitive p = e.getAsJsonPrimitive();
        try {
            return p.getAsString();
        } catch (Exception ignored) {}
        return def;
    }

    public static void setBool(String widgetId, String key, boolean value, boolean autosave) {
        WidgetData d = dataFor(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    public static void setInt(String widgetId, String key, int value, boolean autosave) {
        WidgetData d = dataFor(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    public static void setFloat(String widgetId, String key, float value, boolean autosave) {
        WidgetData d = dataFor(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    private static void setString(String widgetId, String key, String value, boolean autosave) {
        WidgetData d = dataFor(widgetId);
        d.settings.put(key, new JsonPrimitive(value));
        if (autosave) save();
    }

    // ------------------------------------------------------------
    // Data model
    // ------------------------------------------------------------

    public static class WidgetData {
        public double x;
        public double y;
        public boolean enabled = true;

        /** New: generic settings map */
        public Map<String, JsonElement> settings = new HashMap<>();

        // Legacy fields (so old JSON still loads). Gson will fill these if present.
        // We migrate them into settings once on load/register.
        public Boolean drawBackground;
        public Integer backgroundColor;
        public Integer textColor;
        public Boolean drawBorder;
        public Integer borderColor;

        public void migrateLegacyIntoSettings() {
            if (settings == null) settings = new HashMap<>();

            // Only migrate if legacy value exists AND settings doesn't already have it
            if (drawBackground != null && !settings.containsKey("drawBackground")) {
                settings.put("drawBackground", new JsonPrimitive(drawBackground));
            }
            if (backgroundColor != null && !settings.containsKey("backgroundColor")) {
                settings.put("backgroundColor", new JsonPrimitive(backgroundColor));
            }
            if (textColor != null && !settings.containsKey("textColor")) {
                settings.put("textColor", new JsonPrimitive(textColor));
            }
            if (drawBorder != null && !settings.containsKey("drawBorder")) {
                settings.put("drawBorder", new JsonPrimitive(drawBorder));
            }
            if (borderColor != null && !settings.containsKey("borderColor")) {
                settings.put("borderColor", new JsonPrimitive(borderColor));
            }

            // Optional: clear legacy to keep file clean on next save
            // (safe because values are now in settings)
            drawBackground = null;
            backgroundColor = null;
            textColor = null;
            drawBorder = null;
            borderColor = null;
        }
    }
}
