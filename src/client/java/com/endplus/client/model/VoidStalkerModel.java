package com.endplus.client.model;

import com.endplus.entity.mob.VoidStalkerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

/**
 * Gaunt humanoid: legs and head/shoulders stand upright, but the spine is broken in the middle,
 * kinking sideways between the hips and the chest. Over-long arms with a broken elbow and hooked
 * claws, and jagged spikes down the skull.
 */
public class VoidStalkerModel<T extends VoidStalkerEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "void_stalker"), "main");

    /** Lower torso leans one way, upper torso swings back past vertical, so the shoulders sit upright. */
    private static final float LOWER_LEAN = 0.38f;
    private static final float UPPER_LEAN = -0.44f;

    private final ModelPart root;
    private final ModelPart lowerTorso;
    private final ModelPart upperTorso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightForearm;
    private final ModelPart leftArm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public VoidStalkerModel(ModelPart root) {
        this.root = root.getChild("root");
        this.lowerTorso = this.root.getChild("lower_torso");
        this.upperTorso = this.lowerTorso.getChild("upper_torso");
        this.head = this.upperTorso.getChild("head");
        this.rightArm = this.upperTorso.getChild("right_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftArm = this.upperTorso.getChild("left_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.NONE);

        // Lower torso pivots at the hips; the upper torso pivots at the waist, 7px higher.
        ModelPartData lowerTorso = root.addChild("lower_torso",
                ModelPartBuilder.create().uv(0, 12).cuboid(-3.0f, -7.0f, -2.0f, 6, 7, 4),
                ModelTransform.pivot(0, 8.0f, 0));

        ModelPartData upperTorso = lowerTorso.addChild("upper_torso",
                ModelPartBuilder.create().uv(0, 24).cuboid(-3.0f, -7.0f, -2.0f, 6, 7, 4),
                ModelTransform.pivot(0, -7.0f, 0));

        upperTorso.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-2.0f, -7.0f, -2.0f, 4, 7, 4)
                        .uv(40, 40).cuboid(-0.5f, -10.0f, 0.5f, 1, 3, 1)
                        .uv(40, 40).cuboid(-2.0f, -9.0f, 1.0f, 1, 3, 1)
                        .uv(40, 40).cuboid(1.0f, -8.5f, 1.0f, 1, 3, 1),
                ModelTransform.pivot(0, -7.0f, 0));

        ModelPartData rightArm = upperTorso.addChild("right_arm",
                ModelPartBuilder.create().uv(24, 12).cuboid(-1.0f, 0, -1.0f, 2, 11, 2),
                ModelTransform.pivot(-4.0f, -6.0f, 0));
        ModelPartData rightForearm = rightArm.addChild("right_forearm",
                ModelPartBuilder.create().uv(24, 26).cuboid(-1.0f, 0, -1.0f, 2, 11, 2),
                ModelTransform.pivot(0, 11.0f, 0));
        rightForearm.addChild("right_claws",
                ModelPartBuilder.create()
                        .uv(48, 40).cuboid(-1.0f, 11.0f, -1.0f, 1, 4, 1)
                        .uv(48, 40).cuboid(0, 11.0f, 0, 1, 4, 1),
                ModelTransform.NONE);

        ModelPartData leftArm = upperTorso.addChild("left_arm",
                ModelPartBuilder.create().uv(24, 12).cuboid(-1.0f, 0, -1.0f, 2, 11, 2),
                ModelTransform.pivot(4.0f, -6.0f, 0));
        ModelPartData leftForearm = leftArm.addChild("left_forearm",
                ModelPartBuilder.create().uv(24, 26).cuboid(-1.0f, 0, -1.0f, 2, 11, 2),
                ModelTransform.pivot(0, 11.0f, 0));
        leftForearm.addChild("left_claws",
                ModelPartBuilder.create()
                        .uv(48, 40).cuboid(-1.0f, 11.0f, 0, 1, 4, 1)
                        .uv(48, 40).cuboid(0, 11.0f, -1.0f, 1, 4, 1),
                ModelTransform.NONE);

        ModelPartBuilder leg = ModelPartBuilder.create().uv(36, 12).cuboid(-1.0f, 0, -1.0f, 2, 16, 2);
        root.addChild("right_leg", leg, ModelTransform.pivot(-1.5f, 8.0f, 0));
        root.addChild("left_leg", leg, ModelTransform.pivot(1.5f, 8.0f, 0));

        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        lowerTorso.roll = LOWER_LEAN;
        upperTorso.roll = UPPER_LEAN;

        head.yaw = headYaw * ((float) Math.PI / 180F);
        // Shoulders are (nearly) upright again, so the head only droops slightly and cocks a touch.
        head.pitch = headPitch * ((float) Math.PI / 180F) + 0.12f;
        head.roll = 0.1f + (float) Math.sin(animationProgress * 0.05f) * 0.03f;

        float phase = limbAngle * 0.6662f;
        rightLeg.pitch = (float) Math.cos(phase) * 1.2f * limbDistance;
        leftLeg.pitch = (float) Math.cos(phase + Math.PI) * 1.2f * limbDistance;

        float sway = (float) Math.sin(animationProgress * 0.067f) * 0.05f;
        if (entity.isAttacking()) {
            // Reaching out with hooked hands.
            rightArm.pitch = -1.5f + sway;
            leftArm.pitch = -1.35f - sway;
            rightForearm.pitch = -0.35f;
            leftForearm.pitch = -0.55f;
            rightArm.roll = 0.15f;
            leftArm.roll = -0.15f;
        } else {
            // Dangling with the elbows broken at different angles.
            rightArm.pitch = -0.2f + (float) Math.cos(phase + Math.PI) * 0.5f * limbDistance + sway;
            leftArm.pitch = 0.1f + (float) Math.cos(phase) * 0.5f * limbDistance - sway;
            rightForearm.pitch = -1.15f;
            leftForearm.pitch = -0.7f;
            rightArm.roll = 0.22f;
            leftArm.roll = -0.12f;
        }
        rightForearm.roll = -0.12f;
        leftForearm.roll = 0.18f;
    }
}
