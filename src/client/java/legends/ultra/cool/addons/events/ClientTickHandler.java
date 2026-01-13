package legends.ultra.cool.addons.events;

import legends.ultra.cool.addons.hud.HudManager;
import legends.ultra.cool.addons.hud.widget.CounterWidget;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ClientTickHandler {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HudManager.getWidgets().forEach(widget -> {
                if (widget instanceof CounterWidget counter && counter.enabled) {
                    counter.tick();
                }
            });
        });
    }
}

