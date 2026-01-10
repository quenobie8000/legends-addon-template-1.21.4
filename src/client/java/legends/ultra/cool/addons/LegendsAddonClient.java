package legends.ultra.cool.addons;

import legends.ultra.cool.addons.data.WidgetConfigManager;
import legends.ultra.cool.addons.events.ClientTickHandler;
import legends.ultra.cool.addons.events.WorldJoinLeaveHandler;
import legends.ultra.cool.addons.hud.HudManager;
import legends.ultra.cool.addons.hud.HudRenderer;
import legends.ultra.cool.addons.hud.HudWidget;
import legends.ultra.cool.addons.hud.widget.CounterWidget;
import legends.ultra.cool.addons.hud.widget.TextWidget;
import legends.ultra.cool.addons.input.Keybinds;
import net.fabricmc.api.ClientModInitializer;


public class LegendsAddonClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        //load config
        WidgetConfigManager.load();

        for (HudWidget widget : HudManager.getWidgets()) {
            WidgetConfigManager.registerWidget(widget);
        }

        HudRenderer.init();
        Keybinds.init();
        WorldJoinLeaveHandler.init();
        ClientTickHandler.init();

        TextWidget textWidget = new TextWidget(10, 10, "test");
        CounterWidget counterWidget = new CounterWidget(10, 30);

        addWidget(textWidget);
        addWidget(counterWidget);
    }

    public void addWidget(HudWidget w) {
        WidgetConfigManager.registerWidget(w);
        HudManager.register(w);
    }
}