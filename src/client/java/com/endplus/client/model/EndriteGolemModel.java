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
        ModelPartData partData = data.getRoot();

        partData.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0f, -12.0f, -5.5f, 8, 10, 8)
                        .uv(24, 0).cuboid(-1.0f, -5.0f, -7.5f, 2, 4, 2),
                ModelTransform.pivot(0, -7.0f, -2.0f));

        ModelPartData body = partData.addChild("body",
                ModelPartBuilder.create()
                        .uv(0, 40).cuboid(-9.0f, -2.0f, -6.0f, 18, 12, 11)
                        .uv(0, 70).cuboid(-4.5f, 10.0f, -3.0f, 9, 5, 6, new Dilation(0.5f)),
                ModelTransform.pivot(0, -7.0f, 0));

        partData.addChild("right_arm",
                ModelPartBuilder.create()
                        .uv(60, 21).cuboid(-13.0f, -2.5f, -3.0f, 4, 30, 6),
                ModelTransform.pivot(0, -7.0f, 0));

        partData.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(60, 58).cuboid(9.0f, -2.5f, -3.0f, 4, 30, 6),
                ModelTransform.pivot(0, -7.0f, 0));

        partData.addChild("right_leg",
                ModelPartBuilder.create()
                        .uv(37, 0).cuboid(-3.5f, -3.0f, -3.0f, 6, 16, 5),
                ModelTransform.pivot(-4.0f, 11.0f, 0));

        partData.addChild("left_leg",
                ModelPartBuilder.create()
                        .uv(60, 0).mirrored().cuboid(-3.5f, -3.0f, -3.0f, 6, 16, 5),
                ModelTransform.pivot(5.0f, 11.0f, 0));

        return TexturedModelData.of(data, 128, 128);
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
        rightArm.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.5F * limbDistance;
        leftArm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.5F * limbDistance;
        rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4F * limbDistance;
    }
}
