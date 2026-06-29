package com.endplus.client.model;

import com.endplus.entity.minion.EndriteGolemEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class EndriteGolemModel<T extends EndriteGolemEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "endrite_golem"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public EndriteGolemModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4, -8, -4, 8, 8, 8),
                ModelTransform.pivot(0, 0, 0));
        root.addChild("body",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4, 0, -2, 8, 12, 4),
                ModelTransform.pivot(0, 0, 0));
        root.addChild("right_arm",
                ModelPartBuilder.create().uv(40, 16).cuboid(-3, -2, -2, 4, 12, 4),
                ModelTransform.pivot(-5, 2, 0));
        root.addChild("left_arm",
                ModelPartBuilder.create().uv(32, 48).cuboid(-1, -2, -2, 4, 12, 4),
                ModelTransform.pivot(5, 2, 0));
        root.addChild("right_leg",
                ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2, 4, 12, 4),
                ModelTransform.pivot(-2, 12, 0));
        root.addChild("left_leg",
                ModelPartBuilder.create().uv(16, 48).cuboid(-2, 0, -2, 4, 12, 4),
                ModelTransform.pivot(2, 12, 0));
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
        rightArm.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 2F * limbDistance * 0.5F;
        leftArm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 2F * limbDistance * 0.5F;
        rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4F * limbDistance;
    }
}
