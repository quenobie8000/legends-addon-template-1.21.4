package legends.ultra.cool.addons.hud;

import legends.ultra.cool.addons.hud.widget.CounterWidget;
import legends.ultra.cool.addons.hud.widget.settings.WidgetStyle;
import net.minecraft.client.gui.DrawContext;

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
        if (!enabled) {
            HudManager.getWidgets().forEach(widget -> {
                if (widget instanceof CounterWidget counter) {
                    counter.stop();
                }
            });
        }
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
}

