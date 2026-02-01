package legends.ultra.cool.addons.events;

import legends.ultra.cool.addons.hud.HudManager;
import legends.ultra.cool.addons.hud.widget.CounterWidget;
import legends.ultra.cool.addons.hud.widget.TimerWidget;
import legends.ultra.cool.addons.overlay.ContainerOverlay;
import legends.ultra.cool.addons.util.ContainerScan;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


public class ClientTickHandler {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HudManager.getWidgets().forEach(widget -> {
                if (widget instanceof CounterWidget counter && counter.enabled) {
                    counter.tick();
                }

                if (widget instanceof TimerWidget timer && timer.enabled) {
                    timer.tick(timer.getToggleState());
                }

                if (ContainerScan.containerHasName("dirt5"))
                    ContainerOverlay.setTexture("textures/gui/t5_inv.png");
                else if (ContainerScan.containerHasName("dirt4"))
                    ContainerOverlay.setTexture("textures/gui/t4_inv.png");
                else if (ContainerScan.containerHasName("dirt3"))
                    ContainerOverlay.setTexture("textures/gui/t3_inv.png");
                else if (ContainerScan.containerHasName("dirt2"))
                    ContainerOverlay.setTexture("textures/gui/t2_inv.png");
                else ContainerOverlay.setTexture("textures/gui/t1_inv.png");
            });
        });
    }

}

