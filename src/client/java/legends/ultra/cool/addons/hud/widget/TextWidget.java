package legends.ultra.cool.addons.hud.widget;

import legends.ultra.cool.addons.hud.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class TextWidget extends HudWidget {
    private final String text;

    public TextWidget(int x, int y, String text) {
        super("Text", x, y);
        this.text = text;
    }

    @Override
    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

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
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    @Override
    public int getHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }
}
