package com.endplus.client.renderer;

import com.endplus.client.model.AncientEndGolemModel;
import com.endplus.entity.mob.AncientEndGolemEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AncientEndGolemRenderer extends MobEntityRenderer<AncientEndGolemEntity, AncientEndGolemModel<AncientEndGolemEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/ancient_end_golem.png");
    private static final Identifier GLOW_TEXTURE = Identifier.of("endplus", "textures/entity/ancient_end_golem_glow.png");
    private static final float SCALE = 1.3f;

    public AncientEndGolemRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new AncientEndGolemModel<>(ctx.getPart(AncientEndGolemModel.LAYER_LOCATION)), 1.0f);
        // Runes breathe slowly, and blaze steadily while a Void Slam is winding up.
        this.addFeature(new EmissiveFeature<>(this, GLOW_TEXTURE, (golem, tickDelta) ->
                golem.isSlamming() ? 1.0f : 0.45f + 0.35f * (0.5f + 0.5f * MathHelper.sin((golem.age + tickDelta) * 0.06f))));
    }

    @Override
    public Identifier getTexture(AncientEndGolemEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AncientEndGolemEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(SCALE, SCALE, SCALE);
    }
}
