package legends.ultra.cool.addons.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class HudRenderer {
    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            HudManager.getWidgets().forEach(w -> {
                if (w.enabled) w.render(context);
            });
        });
    }
}

