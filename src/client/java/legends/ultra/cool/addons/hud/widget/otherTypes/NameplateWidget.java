package legends.ultra.cool.addons.hud.widget.otherTypes;

import legends.ultra.cool.addons.hud.HudWidget;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

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

    @Override
    public List<HudSetting> getSettings() {
        return List.of(
                HudSetting.slider("yOffset", "Height",0f,2f,0.25f),
                HudSetting.slider("scale", "Scale",0.1f, 2f, 0.25f)
        );
    }
}

