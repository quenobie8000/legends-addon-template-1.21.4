package legends.ultra.cool.addons.mixin.client;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderState.class)
public interface EntityRenderStateAccessor {

    @Accessor("displayName")
    void legends$setDisplayName(Text text);

    @Accessor("displayName")
    Text legends$getDisplayName();

    @Accessor("nameLabelPos")
    Vec3d legends$getNameLabelPos();

    @Accessor("nameLabelPos")
    void legends$setNameLabelPos(Vec3d pos);
}
