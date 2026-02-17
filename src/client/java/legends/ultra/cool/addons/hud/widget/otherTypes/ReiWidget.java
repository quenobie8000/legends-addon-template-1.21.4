package legends.ultra.cool.addons.hud.widget.otherTypes;

import legends.ultra.cool.addons.data.WidgetConfigManager;
import legends.ultra.cool.addons.hud.HudWidget;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.REIRuntime;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ReiWidget extends HudWidget {
    public static final String WIDGET_NAME = "REI";
    private static final String SETTING_SHOW_VANILLA = "showVanillaItems";
    public static ReiWidget INSTANCE;
    private boolean restoreOverlayVisible = false;

    public ReiWidget() {
        super(WIDGET_NAME, 0, 0);
        INSTANCE = this;
    }

    public static boolean showVanillaItems() {
        return WidgetConfigManager.getBool(WIDGET_NAME, SETTING_SHOW_VANILLA, false);
    }

    private static void setShowVanillaItems(boolean value) {
        boolean previous = showVanillaItems();
        WidgetConfigManager.setBool(WIDGET_NAME, SETTING_SHOW_VANILLA, value, true);
        if (previous != value) {
            requestReiReload();
        }
    }

    private static void requestReiReload() {
        RoughlyEnoughItemsCoreClient.reloadPlugins(null, null);
    }

    @Override
    public void toggle() {
        super.toggle();
        syncOverlayVisibility();
    }

    public void syncOverlayVisibility() {
        if (enabled) {
            if (restoreOverlayVisible && !REIRuntime.getInstance().isOverlayVisible()) {
                REIRuntime.getInstance().toggleOverlayVisible();
            }
            restoreOverlayVisible = false;
            return;
        }

        boolean visible = REIRuntime.getInstance().isOverlayVisible();
        restoreOverlayVisible = visible;
        if (visible) {
            REIRuntime.getInstance().toggleOverlayVisible();
        }
    }

    public void enforceHiddenIfDisabled() {
        if (!enabled && REIRuntime.getInstance().isOverlayVisible()) {
            REIRuntime.getInstance().toggleOverlayVisible();
        }
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
    public List<HudSetting> getSettings() {
        return List.of(
                HudSetting.toggle(
                        SETTING_SHOW_VANILLA,
                        "Show vanilla items",
                        () -> true,
                        ReiWidget::showVanillaItems,
                        ReiWidget::setShowVanillaItems,
                        false
                )
        );
    }
}
