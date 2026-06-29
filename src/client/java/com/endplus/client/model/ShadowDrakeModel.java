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
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;

    public ShadowDrakeModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        this.leftWingBase = body.getChild("left_wing_base");
        this.leftWingTip = this.leftWingBase.getChild("left_wing_tip");
        this.rightWingBase = body.getChild("right_wing_base");
        this.rightWingTip = this.rightWingBase.getChild("right_wing_tip");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData partData = data.getRoot();

        ModelPartData body = partData.addChild("body",
                ModelPartBuilder.create()
                        .uv(0, 8).cuboid(-3.0f, -2.0f, -8.0f, 5, 3, 9),
                ModelTransform.of(0, 0, 0, -0.1f, 0, 0));

        ModelPartData tailBase = body.addChild("tail_base",
                ModelPartBuilder.create()
                        .uv(3, 20).cuboid(-2.0f, 0, 0, 3, 2, 6),
                ModelTransform.pivot(0, -2.0f, 1.0f));

        tailBase.addChild("tail_tip",
                ModelPartBuilder.create()
                        .uv(4, 29).cuboid(-1.0f, 0, 0, 1, 1, 6),
                ModelTransform.pivot(0, 0.5f, 6.0f));

        ModelPartData leftWingBase = body.addChild("left_wing_base",
                ModelPartBuilder.create()
                        .uv(23, 12).cuboid(0, 0, 0, 6, 2, 9),
                ModelTransform.of(2.0f, -2.0f, -8.0f, 0, 0, 0.1f));

        leftWingBase.addChild("left_wing_tip",
                ModelPartBuilder.create()
                        .uv(16, 24).cuboid(0, 0, 0, 13, 1, 9),
                ModelTransform.of(6.0f, 0, 0, 0, 0, 0.1f));

        ModelPartData rightWingBase = body.addChild("right_wing_base",
                ModelPartBuilder.create()
                        .uv(23, 12).mirrored().cuboid(-6.0f, 0, 0, 6, 2, 9),
                ModelTransform.of(-3.0f, -2.0f, -8.0f, 0, 0, -0.1f));

        rightWingBase.addChild("right_wing_tip",
                ModelPartBuilder.create()
                        .uv(16, 24).mirrored().cuboid(-13.0f, 0, 0, 13, 1, 9),
                ModelTransform.of(-6.0f, 0, 0, 0, 0, -0.1f));

        body.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0f, -2.0f, -5.0f, 7, 3, 5),
                ModelTransform.of(0, 1.0f, -7.0f, 0.2f, 0, 0));

        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        float wingAngle = MathHelper.sin(animationProgress * 0.1f) * 0.4f;
        leftWingBase.roll = wingAngle;
        leftWingTip.roll = wingAngle;
        rightWingBase.roll = -wingAngle;
        rightWingTip.roll = -wingAngle;
    }
}
