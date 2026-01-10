package legends.ultra.cool.addons.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import legends.ultra.cool.addons.hud.HudWidget;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WidgetConfigManager {

    private static final Gson GSON = new Gson();
    private static final String FILE_NAME = "legendsaddon_widgets.json";

    private static Map<String, WidgetData> widgetDataMap = new HashMap<>();

    private static Path getConfigPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve(FILE_NAME);
    }

    public static void load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                Type type = new TypeToken<Map<String, WidgetData>>(){}.getType();
                widgetDataMap = GSON.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        Path path = getConfigPath();
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(widgetDataMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void registerWidget(HudWidget widget) {
        if (widget == null) return;
        String key = widget.getName();
        WidgetData data = widgetDataMap.getOrDefault(key, new WidgetData(widget));
        widget.x = data.x;
        widget.y = data.y;
        widget.enabled = data.enabled;

        widget.style.drawBackground = data.drawBackground;
        widget.style.backgroundColor = data.backgroundColor;
        widget.style.textColor = data.textColor;
        widget.style.drawBorder = data.drawBorder;
        widget.style.borderColor = data.borderColor;

        widgetDataMap.put(key, data);
    }

    public static void updateWidget(HudWidget widget) {
        if (widget == null) return;
        String key = widget.getName();
        WidgetData data = widgetDataMap.getOrDefault(key, new WidgetData(widget));
        data.x = widget.x;
        data.y = widget.y;
        data.enabled = widget.isEnabled();

        data.drawBackground = widget.style.drawBackground;
        data.backgroundColor = widget.style.backgroundColor;
        data.textColor = widget.style.textColor;
        data.drawBorder = widget.style.drawBorder;
        data.borderColor = widget.style.borderColor;

        widgetDataMap.put(key, data);
        save();
    }

    public static class WidgetData {
        public double x;
        public double y;
        public boolean enabled;

        public boolean drawBackground = true;
        public int backgroundColor = 0x80000000;
        public int textColor = 0xFFFFFFFF;
        public boolean drawBorder = true;
        public int borderColor = 0xFFFFFFFF;

        public WidgetData() {} // gson
        public WidgetData(HudWidget widget) {
            this.x = widget.x;
            this.y = widget.y;
            this.enabled = widget.isEnabled();

            this.drawBackground = widget.style.drawBackground;
            this.backgroundColor = widget.style.backgroundColor;
            this.textColor = widget.style.textColor;
            this.drawBorder = widget.style.drawBorder;
            this.borderColor = widget.style.borderColor;
        }
    }


    public static void resetAll() {
        widgetDataMap.clear();
        save();
    }
}
