package com.endplus.client.model;

import com.endplus.entity.minion.ShadowDrakeEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ShadowDrakeModel<T extends ShadowDrakeEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "shadow_drake"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;

    public ShadowDrakeModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail = root.getChild("tail");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-3, -3, -5, 6, 6, 8),
                ModelTransform.pivot(0, -1, -8));
        root.addChild("body",
                ModelPartBuilder.create().uv(28, 0).cuboid(-4, 0, -2, 8, 6, 8),
                ModelTransform.pivot(0, 0, 0));
        root.addChild("left_wing",
                ModelPartBuilder.create().uv(0, 14).cuboid(0, -1, 0, 20, 1, 12),
                ModelTransform.pivot(4, 0, 1));
        root.addChild("right_wing",
                ModelPartBuilder.create().uv(0, 14).cuboid(-20, -1, 0, 20, 1, 12),
                ModelTransform.pivot(-4, 0, 1));
        root.addChild("tail",
                ModelPartBuilder.create().uv(0, 27).cuboid(-1, 0, 0, 2, 2, 10),
                ModelTransform.pivot(0, 2, 6));
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        head.yaw = headYaw * ((float) Math.PI / 180F);
        head.pitch = headPitch * ((float) Math.PI / 180F);
        float wingFlap = MathHelper.sin(animationProgress * 0.15F) * 0.4F;
        leftWing.roll = -wingFlap;
        rightWing.roll = wingFlap;
        tail.pitch = MathHelper.sin(animationProgress * 0.1F) * 0.15F;
    }
}
