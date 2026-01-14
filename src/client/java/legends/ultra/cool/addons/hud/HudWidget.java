package legends.ultra.cool.addons.hud;

import legends.ultra.cool.addons.hud.widget.settings.WidgetStyle;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public abstract class HudWidget {
    public double x;
    public double y;
    public boolean enabled = true;
    protected final String name;
    public final WidgetStyle style = new WidgetStyle();

    public HudWidget(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public abstract void render(DrawContext context);
    public abstract int getWidth();
    public abstract int getHeight();

    public boolean isMouseOver(double mouseX, double mouseY) {
        int ix = (int) x;
        int iy = (int) y;
        return mouseX >= ix && mouseX <= ix + getWidth()
                && mouseY >= iy && mouseY <= iy + getHeight();
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public boolean hasSettings() {
        return true;
    }

    /** Per-widget settings schema for the editor to render dynamically. */
    public List<HudSetting> getSettings() {
        return List.of(); // default: none
    }

    public record HudSetting(
            String key,       // "bgToggle"
            String label,     // "Background"
            Type type,        // TOGGLE / COLOR / SLIDER
            float min,        // slider only
            float max,        // slider only
            float step        // slider only
    ) {
        public enum Type { TOGGLE, COLOR, SLIDER }

        public static HudSetting toggle(String key, String label) {
            return new HudSetting(key, label, Type.TOGGLE, 0, 0, 0);
        }

        public static HudSetting color(String key, String label) {
            return new HudSetting(key, label, Type.COLOR, 0, 0, 0);
        }

        public static HudSetting slider(String key, String label, float min, float max, float step) {
            return new HudSetting(key, label, Type.SLIDER, min, max, step);
        }
    }

}

