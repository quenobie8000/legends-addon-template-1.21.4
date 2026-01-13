package legends.ultra.cool.addons.hud.otherTypes;

import legends.ultra.cool.addons.hud.HudWidget;
import net.minecraft.client.gui.DrawContext;

public class NameplateWidget extends HudWidget {

    // Simple global access for mixins (no new systems)
    public static NameplateWidget INSTANCE;

    public NameplateWidget() {
        super("Nameplates", 0, 0);
        INSTANCE = this;
    }

    public static boolean isEnabledGlobal() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }

    @Override
    public void render(DrawContext context) {
        // Intentionally empty: this widget is just a toggle/config handle
    }

    @Override
    public int getWidth() {
        return 1; // non-interactive on canvas
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public boolean hasSettings() {
        return false; // disables the settings modal for this widget
    }
}

