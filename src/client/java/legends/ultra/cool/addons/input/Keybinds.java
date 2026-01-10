package legends.ultra.cool.addons.input;

import legends.ultra.cool.addons.hud.HudEditorScreen;
import legends.ultra.cool.addons.util.EntityDebug;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static KeyBinding OPEN_EDITOR;

    public static void init() {
        KeyBinding openEditorKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.legendsaddon.hud_editor",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        "category.legendsaddon"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditorKey.wasPressed()) {
                client.setScreen(new HudEditorScreen());
            }
        });


        KeyBinding DUMP_MOB_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.legendsaddon.dump_mob",      // translation key
                GLFW.GLFW_KEY_K,                  // default key (K)
                "category.legendsaddon.debug"     // category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // fires once per press (doesn't spam while held)
            while (DUMP_MOB_KEY.wasPressed()) {
                dumpLookedAtMob(client);
            }
        });
    }

    private static void dumpLookedAtMob(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        if (!(client.crosshairTarget instanceof EntityHitResult hit)) {
            return;
        }

        if (!(hit.getEntity() instanceof LivingEntity mob)) {
            return;
        }

        EntityDebug.dumpTargetFiltered(mob);
    }
}

