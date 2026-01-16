package legends.ultra.cool.addons.mixin.client;

import legends.ultra.cool.addons.data.WidgetConfigManager;
import legends.ultra.cool.addons.util.ColorUtil;
import legends.ultra.cool.addons.util.EntityDebug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMultilineLabelMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void legends$renderMultiline(S state, Text text, MatrixStack matrices, VertexConsumerProvider consumers, int light, CallbackInfo ci) {

        Vec3d pos = state.nameLabelPos;

        float yOffset = WidgetConfigManager.getFloat("Nameplates", "yOffset", 1f);
        float scale = WidgetConfigManager.getFloat("Nameplates", "scale", 1.0f);

        if (pos == null) return;

        String raw = text.getString();
        if (!raw.contains("\n")) return; // let vanilla handle single-line

        String[] lines = raw.split("\\R");
        if (lines.length == 0) return;

        ci.cancel();

        MinecraftClient client = MinecraftClient.getInstance();

        TextRenderer tr = client.textRenderer;

        matrices.push();
        matrices.translate(pos.x, pos.y + yOffset, pos.z);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(0.025f * scale, -0.025f * scale, 0.025f * scale);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        int lineH = tr.fontHeight + 1;
        float startY = (-(lines.length - 1) * lineH * 0.5f) - yOffset;

        // Compute block dimensions (in nameplate pixel space)
        int maxW = 0;
        for (String s : lines) maxW = Math.max(maxW, tr.getWidth(s));
        int totalH = lines.length * lineH;

        int padX = 4;
        int padY = 3;

        float panelW = maxW + padX * 2f;
        float panelH = totalH + padY * 2f;

        float panelX = -panelW / 2f;
        float panelY = -padY - (panelH / 3);

        int argb = 0xFF520016;

//        for (ItemStack armor : mob.getArmorItems()) {
//            if (armor.isEmpty()) continue;
//
//            int c = EntityDebug.getArmorColorARGB(armor);
//            if (c != 0xFFff0044) { // found dyed leather
//                argb = c;
//                break;
//            }
//        }

        argb = ColorUtil.forceValueToMax(argb);

        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float a = ((argb >>> 24) & 0xFF) / 255f;

//        float r = 82f;
//        float g = 0f;
//        float b = 22;
//        float a = 1f;

        int lit = LightmapTextureManager.applyEmission(light, 2);
// always bright: LightmapTextureManager.MAX_LIGHT_COORDINATE


        drawNineSlice(
                consumers,
                mat,
                panelX,
                panelY,
                panelW,
                panelH,
                9, 9, 4,
                r, g, b, a,
                lit
        );

        for (int i = 0; i < lines.length; i++) {
            String s = lines[i];
            float x = -tr.getWidth(s) / 2.0f;
            float y = (startY + i * lineH);

            tr.draw(
                    s, x, y,
                    -2130706433,
                    false,
                    mat,
                    consumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0,
                    LightmapTextureManager.applyEmission(light, 2)
            );
        }

        matrices.pop();
    }

    private static final Identifier NAMEPLATE_BORDER =
            Identifier.of("legends-addon", "textures/gui/nameplate_border_grayscaled.png");

    // Draw a textured quad in nameplate-pixel space
    private static void drawQuad(
            VertexConsumer vc,
            Matrix4f mat,
            float x1, float y1, float x2, float y2,
            float u1, float v1, float u2, float v2,
            float r, float g, float b, float a,
            int light
    ) {
        vc.vertex(mat, x1, y2, -0.01f).texture(u1, v2).color(r, g, b, a).light(light);
        vc.vertex(mat, x2, y2, -0.01f).texture(u2, v2).color(r, g, b, a).light(light);
        vc.vertex(mat, x2, y1, -0.01f).texture(u2, v1).color(r, g, b, a).light(light);
        vc.vertex(mat, x1, y1, -0.01f).texture(u1, v1).color(r, g, b, a).light(light);
    }

    private static void drawNineSlice(
            VertexConsumerProvider consumers,
            Matrix4f mat,
            float x, float y,
            float w, float h,
            int texW, int texH,
            int corner,
            float r, float g, float b, float a,
            int light
    ) {
        // Source texture coords (pixels -> UV)
        float cU = corner / (float) texW;
        float cV = corner / (float) texH;
        float uMax = 1f;
        float vMax = 1f;

        // Dest coords
        float x0 = x;
        float y0 = y;
        float x1 = x + corner;
        float y1 = y + corner;
        float x2 = x + w - corner;
        float y2 = y + h - corner;
        float x3 = x + w;
        float y3 = y + h;

        // Guard against tiny panels
        if (w < corner * 2f) {
            x2 = x1;
        }
        if (h < corner * 2f) {
            y2 = y1;
        }

        VertexConsumer vc = consumers.getBuffer(RenderLayer.getGuiTextured(NAMEPLATE_BORDER)); // common way to draw GUI textures :contentReference[oaicite:0]{index=0}

        // 4 corners
        drawQuad(vc, mat, x0, y0, x1, y1, 0f, 0f, cU, cV, r, g, b, a, light);                 // TL
        drawQuad(vc, mat, x2, y0, x3, y1, uMax - cU, 0f, uMax, cV, r, g, b, a, light);        // TR
        drawQuad(vc, mat, x0, y2, x1, y3, 0f, vMax - cV, cU, vMax, r, g, b, a, light);        // BL
        drawQuad(vc, mat, x2, y2, x3, y3, uMax - cU, vMax - cV, uMax, vMax, r, g, b, a, light); // BR

        // Edges (stretch)
        drawQuad(vc, mat, x1, y0, x2, y1, cU, 0f, uMax - cU, cV, r, g, b, a, light);          // Top
        drawQuad(vc, mat, x1, y2, x2, y3, cU, vMax - cV, uMax - cU, vMax, r, g, b, a, light); // Bottom
        drawQuad(vc, mat, x0, y1, x1, y2, 0f, cV, cU, vMax - cV, r, g, b, a, light);          // Left
        drawQuad(vc, mat, x2, y1, x3, y2, uMax - cU, cV, uMax, vMax - cV, r, g, b, a, light); // Right

        // Center (fill)
        drawQuad(vc, mat, x1, y1, x2, y2, cU, cV, uMax - cU, vMax - cV, r, g, b, a, light);
    }


}