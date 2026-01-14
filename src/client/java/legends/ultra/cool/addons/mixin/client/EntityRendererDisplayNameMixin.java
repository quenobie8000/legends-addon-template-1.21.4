package legends.ultra.cool.addons.mixin.client;

import legends.ultra.cool.addons.hud.widget.otherTypes.NameplateWidget;
import legends.ultra.cool.addons.util.EntityDebug;
import legends.ultra.cool.addons.util.TextHeathbar;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererDisplayNameMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void legends$forceAndOverrideDisplayName(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (!NameplateWidget.isEnabledGlobal()) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (!living.isAlive() ) return;
        if (entity instanceof net.minecraft.entity.player.PlayerEntity) return;
        if (entity instanceof net.minecraft.entity.decoration.ArmorStandEntity) return;

        if (state.squaredDistanceToCamera >= 2048.0) return;


        EntityRenderStateAccessor acc = (EntityRenderStateAccessor) (Object) state;

        String mobName = entity.getDisplayName().getString();
        double[] stats = EntityDebug.getMobStats(living);

        if (mobName.matches(".*?(\\d+).*\\/(\\d+).*")) mobName = mobName.replaceAll("(.*?).\\[\\d+.*\\/\\d+.*", "$1");

        Text custom = Text.literal(mobName + "\n§c" + TextHeathbar.heathBar(stats[0], stats[1]) + "\n§c" + stats[0] + "❤ §r| §2" + stats[2] + "\uD83D\uDEE1 §r| §c" + stats[3] + "⚔");

        // Force label position if vanilla didn't compute it (otherwise nothing draws)
        if (acc.legends$getNameLabelPos() == null) {
            Vec3d p = entity.getAttachments().getPointNullable(
                    EntityAttachmentType.NAME_TAG,
                    0,
                    entity.getLerpedYaw(tickDelta)
            );
            acc.legends$setNameLabelPos(p);
        }


        acc.legends$setDisplayName(custom);
    }
}
