package legends.ultra.cool.addons.overlay;

import legends.ultra.cool.addons.hud.HudManager;
import legends.ultra.cool.addons.hud.widget.CounterWidget;
import legends.ultra.cool.addons.mixin.client.HandledScreenAccessor;
import legends.ultra.cool.addons.util.ContainerScan;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ContainerOverlay {
    private ContainerOverlay() {
    }

    private static Identifier texture =
            Identifier.of("legends-addon", "textures/gui/t1_inv.png");


    public static void init() {

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // Register a per-screen render hook
            ScreenEvents.afterRender(screen).register((scr, ctx, mouseX, mouseY, delta) -> {
                if (!(scr instanceof HandledScreen<?> hs)) return;

                if (!shouldOverlay(hs)) return;

                drawOverlay(hs, ctx);
            });
        });

    }

    public static void setTexture(String texturePath) {
        texture = Identifier.of("legends-addon", texturePath);
    }

    /**
     * Decide which container(s) get the overlay
     */
    private static boolean shouldOverlay(HandledScreen<?> hs) {
        // Example filters you can combine:

        // 1) By title text (simple & common)
        String title = hs.getTitle().getString();
        if (!title.toLowerCase().contains("foraging tree")) return false;

        // 2) By slot count (optional)
        ScreenHandler handler = hs.getScreenHandler();
        int slots = handler.slots.size();
        // if (slots != 54) return false;

        return true;
    }

    public final class RenderLayers {
        public static final Function<Identifier, RenderLayer> GUI =
                RenderLayer::getGuiTextured;

        private RenderLayers() {
        }
    }

    /**
     * Draw a texture on top of the container background area
     */
    private static void drawOverlay(HandledScreen<?> hs, DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof HandledScreen<?> screen) {
            HandledScreenAccessor acc = (HandledScreenAccessor) screen;

            int guiX = acc.legends$getX();
            int guiY = acc.legends$getY();

            // HandledScreen gives you the container's top-left & size
            int w = 160;
            int h = 106;
            int x = guiX + 8;
            int y = guiY + 18;


            // This draws the whole texture stretched to w/h.
            ctx.drawTexture(RenderLayers.GUI, texture, x, y, 0, 0, w, h, w, h);
        }
    }
}
