package legends.ultra.cool.addons.hud;

import java.util.ArrayList;
import java.util.List;

public class HudManager {
    private static final List<HudWidget> WIDGETS = new ArrayList<>();

    public static void register(HudWidget widget) {
        WIDGETS.add(widget);
    }

    public static List<HudWidget> getWidgets() {
        return WIDGETS;
    }
}

