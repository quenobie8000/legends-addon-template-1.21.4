package legends.ultra.cool.addons.hud.widget;

import legends.ultra.cool.addons.hud.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class CounterWidget extends HudWidget {

    private int ticksElapsed = 0;
    private int value = 0;
    private int ticksPerIncrement = 20; // 1 second

    public CounterWidget(int x, int y) {
        super("Counter", x, y);
    }

    // Called from ClientTickHandler
    public void tick() {
        ticksElapsed++;

        if (ticksElapsed >= ticksPerIncrement) {
            value++;
            ticksElapsed = 0;
        }
    }

    public void reset() {
        ticksElapsed = 0;
        value = 0;
    }

    @Override
    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        String text = "Time: " + value;

        int width = client.textRenderer.getWidth(text);
        int height = client.textRenderer.fontHeight;

        // background
        if (style.drawBackground) {
            context.fill(
                    (int) (x - 2),
                    (int) (y - 2),
                    (int) (x + width + 2),
                    (int) (y + height + 2),
                    style.backgroundColor
            );
        }

        // border
        if (style.drawBorder) {
            context.drawBorder(
                    (int) (x - 2),
                    (int) (y - 2),
                    getWidth() + 4,
                    getHeight() + 4,
                    style.borderColor
            );
        }

        context.drawText(
                client.textRenderer,
                text,
                (int) x,
                (int) y,
                style.textColor,
                false
        );
    }

    @Override
    public int getWidth() {
        return MinecraftClient.getInstance()
                .textRenderer
                .getWidth("Time: " + value);
    }

    @Override
    public int getHeight() {
        return MinecraftClient.getInstance()
                .textRenderer
                .fontHeight;
    }

    @Override
    public List<HudSetting> getSettings() {
        return List.of(
                HudSetting.toggle("bgToggle", "Background"),
                HudSetting.color("bgColor", "BG Color"),
                HudSetting.toggle("brdToggle", "Background"),
                HudSetting.color("brdColor", "Border Color"),
                HudSetting.color("txtColor", "Text Color")
        );
    }
}

