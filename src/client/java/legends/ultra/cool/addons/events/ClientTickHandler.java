package legends.ultra.cool.addons.events;

import legends.ultra.cool.addons.hud.HudManager;
import legends.ultra.cool.addons.hud.widget.CounterWidget;
import legends.ultra.cool.addons.hud.widget.TimerWidget;
import legends.ultra.cool.addons.util.ChatLookup;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ClientTickHandler {
    static int tick;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tick++;

            boolean start = ChatLookup.consumeExact("start");
            boolean stop  = ChatLookup.consumeExact("stop");
            boolean reset = ChatLookup.consumeExact("reset");
            boolean completed = ChatLookup.consumeExact("completed");
            boolean failed = ChatLookup.consumeExact("failed");

            HudManager.getWidgets().forEach(widget -> {
                if (widget instanceof CounterWidget counter && counter.enabled) {
                    counter.tick();
                }

                if (widget instanceof TimerWidget timer && timer.enabled) {
                    if (reset) timer.reset();
                    timer.tick(start, stop);

                    if (!ChatLookup.getResult().isEmpty()) {
                        if (tick > (tick + (20 * 5))) {
                            ChatLookup.result = "";
                            tick = 0;
                        }
                    }
                }
            });
        });
    }

}

