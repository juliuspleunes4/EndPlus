package com.endplus.client.model;

import com.endplus.entity.minion.EnderPhantomEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class EnderPhantomModel<T extends EnderPhantomEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "ender_phantom"), "main");

    private final ModelPart root;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public EnderPhantomModel(ModelPart root) {
        this.root = root;
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-5, -5, -5, 10, 10, 10),
                ModelTransform.pivot(0, 0, 0));
        root.addChild("left_wing",
                ModelPartBuilder.create().uv(0, 20).cuboid(0, -2, 0, 16, 4, 8),
                ModelTransform.pivot(5, 0, 0));
        root.addChild("right_wing",
                ModelPartBuilder.create().uv(0, 32).cuboid(-16, -2, 0, 16, 4, 8),
                ModelTransform.pivot(-5, 0, 0));
        root.addChild("tail",
                ModelPartBuilder.create().uv(40, 0).cuboid(-1, 0, 0, 2, 2, 8),
                ModelTransform.pivot(0, 3, 5));
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        float wingFlap = MathHelper.sin(animationProgress * 0.1F) * 0.2F;
        leftWing.roll = -wingFlap;
        rightWing.roll = wingFlap;
    }
}
