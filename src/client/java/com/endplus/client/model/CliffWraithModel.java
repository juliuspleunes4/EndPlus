package com.endplus.client.model;

import com.endplus.entity.mob.CliffWraithEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

/**
 * Skeletal raptor-wraith with Ambopteryx-style wings: a short bony arm ending in a wrist claw, from
 * which one very long rod-like bone (the styliform element) reaches out and back. A tattered leathery
 * membrane is stretched between the arm, the rod and the body, tapering to a point at the rod's tip.
 */
public class CliffWraithModel<T extends CliffWraithEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "cliff_wraith"), "main");

    // Wing geometry, in pixels along the span (x) measured from the body centre (left wing).
    private static final float SHOULDER_X = 2.0f;
    private static final int ARM_LENGTH = 12;
    private static final int FOREARM_LENGTH = 15;
    private static final int WRIST_LENGTH = 5;
    private static final int ROD_LENGTH = 36;
    private static final float ROD_YAW = -0.3f; // negative = swept back on the left wing
    private static final float WRIST_X = SHOULDER_X + ARM_LENGTH + FOREARM_LENGTH;
    private static final float ROD_X = WRIST_X + WRIST_LENGTH;
    private static final float TIP_X = ROD_X + ROD_LENGTH * (float) Math.cos(ROD_YAW);
    /** The rod's slope: how far back (+z) it drifts per pixel outwards. */
    private static final float ROD_SLOPE = (float) -Math.tan(ROD_YAW);
    private static final float TRAILING_ROOT_Z = 8.0f;
    private static final float TRAILING_TIP_Z = ROD_LENGTH * (float) Math.sin(-ROD_YAW);

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final Wing leftWing;
    private final Wing rightWing;

    public CliffWraithModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.leftWing = new Wing(this.body.getChild("left_wing"), "left");
        this.rightWing = new Wing(this.body.getChild("right_wing"), "right");
    }

    /** Handles to the animated joints of one wing. */
    private static final class Wing {
        final ModelPart arm;
        final ModelPart forearm;
        final ModelPart wrist;
        final ModelPart rod;

        Wing(ModelPart arm, String side) {
            this.arm = arm;
            this.forearm = arm.getChild(side + "_forearm");
            this.wrist = this.forearm.getChild(side + "_wrist");
            this.rod = this.wrist.getChild(side + "_rod");
        }
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.NONE);

        ModelPartData body = root.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-2.0f, -2.0f, -4.5f, 4, 4, 9),
                ModelTransform.pivot(0, 16.0f, 0));

        body.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 14).cuboid(-2.0f, -2.0f, -4.0f, 4, 4, 4)
                        .uv(18, 14).cuboid(-1.0f, 0.0f, -7.0f, 2, 1, 3),
                ModelTransform.pivot(0, -0.5f, -4.5f));

        body.addChild("tail",
                ModelPartBuilder.create().uv(64, 0).cuboid(-1.0f, -0.5f, 0, 2, 1, 9),
                ModelTransform.of(0, 0.5f, 4.5f, 0.15f, 0, 0));

        addWing(body, "left", 1);
        addWing(body, "right", -1);

        body.addChild("left_talon",
                ModelPartBuilder.create().uv(40, 0).cuboid(-0.5f, 0, -0.5f, 1, 3, 1),
                ModelTransform.of(1.0f, 2.0f, 1.0f, 0.4f, 0, 0));
        body.addChild("right_talon",
                ModelPartBuilder.create().uv(40, 0).cuboid(-0.5f, 0, -0.5f, 1, 3, 1),
                ModelTransform.of(-1.0f, 2.0f, 1.0f, 0.4f, 0, 0));

        return TexturedModelData.of(data, 128, 64);
    }

    /** side = +1 builds the left wing (extends along +x), -1 the right (along -x, mirrored). */
    private static void addWing(ModelPartData body, String name, int side) {
        // Bone strips (tools/cliff_wraith_skin.js): each cuboid maps onto a strip of
        // 2*(length+thickness) x 2*thickness pixels. Membrane panels all sample one shared patch.
        ModelPartData arm = body.addChild(name + "_wing",
                bone(0, 24, ARM_LENGTH, 3, side),
                ModelTransform.pivot(SHOULDER_X * side, -1.0f, -0.5f));
        addPanel(arm, name + "_membrane_arm", SHOULDER_X, SHOULDER_X + ARM_LENGTH, SHOULDER_X, side);

        ModelPartData forearm = arm.addChild(name + "_forearm",
                bone(0, 30, FOREARM_LENGTH, 3, side),
                ModelTransform.pivot(ARM_LENGTH * side, 0, 0));
        addPanel(forearm, name + "_membrane_forearm", SHOULDER_X + ARM_LENGTH, WRIST_X, SHOULDER_X + ARM_LENGTH, side);

        ModelPartData wrist = forearm.addChild(name + "_wrist",
                bone(0, 36, WRIST_LENGTH, 3, side),
                ModelTransform.pivot(FOREARM_LENGTH * side, 0, 0));
        addPanel(wrist, name + "_membrane_wrist", WRIST_X, ROD_X, WRIST_X, side);
        // Hooked claw on the wrist.
        wrist.addChild(name + "_claw",
                bone(0, 36, 4, 1, side),
                ModelTransform.of(1.0f * side, 0, -1.5f, 0, 0.9f * side, 0));

        ModelPartData rod = wrist.addChild(name + "_rod",
                bone(0, 42, ROD_LENGTH, 2, side),
                ModelTransform.of(WRIST_LENGTH * side, 0, 0, 0, ROD_YAW * side, 0));
        // The outer membrane hangs off the wrist (not the rod) so it keeps its shape as the rod yaws.
        float step = (TIP_X - ROD_X) / 4.0f;
        for (int i = 0; i < 4; i++) {
            addPanel(wrist, name + "_membrane_outer_" + i, ROD_X + step * i, ROD_X + step * (i + 1), WRIST_X, side);
        }
    }

    /** Membrane leading edge: the arm up to the rod's root, then along the rod. */
    private static float leadingZ(float x) {
        return x <= ROD_X ? 0.0f : (x - ROD_X) * ROD_SLOPE;
    }

    /** Membrane trailing edge: from the hip straight out to the rod's tip. */
    private static float trailingZ(float x) {
        float t = (x - SHOULDER_X) / (TIP_X - SHOULDER_X);
        return TRAILING_ROOT_Z + (TRAILING_TIP_Z - TRAILING_ROOT_Z) * Math.min(Math.max(t, 0.0f), 1.0f);
    }

    /**
     * One flat slab of membrane covering span range [fromX, toX] (measured from the body centre).
     * Coordinates are converted into the parent bone's local frame by subtracting parentStartX, the
     * span position of that bone's origin.
     */
    private static void addPanel(ModelPartData parent, String name, float fromX, float toX, float parentStartX, int side) {
        float mid = (fromX + toX) / 2.0f;
        float z0 = leadingZ(mid);
        float dz = Math.max(1.0f, trailingZ(mid) - z0);
        float dx = toX - fromX;
        float local = fromX - parentStartX;
        ModelPartBuilder builder = ModelPartBuilder.create().uv(0, 50);
        if (side > 0) {
            builder.cuboid(local, -0.5f, z0, dx, 1, dz);
        } else {
            builder.mirrored().cuboid(-local - dx, -0.5f, z0, dx, 1, dz);
        }
        parent.addChild(name, builder, ModelTransform.NONE);
    }

    private static ModelPartBuilder bone(int u, int v, int length, int thickness, int side) {
        ModelPartBuilder builder = ModelPartBuilder.create().uv(u, v);
        float t = thickness / 2.0f;
        if (side > 0) {
            return builder.cuboid(0, -t, -t, length, thickness, thickness);
        }
        return builder.mirrored().cuboid(-length, -t, -t, length, thickness, thickness);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        head.yaw = headYaw * ((float) Math.PI / 180F) * 0.5f;
        head.pitch = headPitch * ((float) Math.PI / 180F) * 0.5f;

        if (entity.isAttacking()) {
            // Diving: wings swept hard back, body tipped nose-down, quick flutter.
            float flutter = (float) Math.sin(animationProgress * 1.3f) * 0.08f;
            body.pitch = 0.55f;
            tail.pitch = 0.45f;
            poseWing(leftWing, 1, 0.15f + flutter, -0.95f, 0.1f, 0.2f);
            poseWing(rightWing, -1, 0.15f + flutter, -0.95f, 0.1f, 0.2f);
        } else {
            // Slow, heavy flapping; each joint lags the one before so the rod tip whips.
            float t = animationProgress * 0.45f;
            body.pitch = 0.1f + (float) Math.sin(t + 1.0f) * 0.06f;
            tail.pitch = 0.15f + (float) Math.sin(animationProgress * 0.35f) * 0.1f;
            float armRoll = (float) Math.sin(t) * 0.6f;
            float forearmRoll = (float) Math.sin(t - 0.7f) * 0.45f;
            float wristRoll = (float) Math.sin(t - 1.4f) * 0.4f;
            poseWing(leftWing, 1, armRoll, 0.12f, forearmRoll, wristRoll);
            poseWing(rightWing, -1, armRoll, 0.12f, forearmRoll, wristRoll);
        }
    }

    /** Roll is mirrored for the right wing; yaw sweeps both wings backwards when negative. */
    private static void poseWing(Wing wing, int side, float armRoll, float armYaw, float forearmRoll, float wristRoll) {
        wing.arm.roll = armRoll * side;
        wing.arm.yaw = armYaw * side;
        wing.forearm.roll = forearmRoll * side;
        wing.forearm.yaw = armYaw * 0.5f * side;
        wing.wrist.roll = wristRoll * side;
        // The long rod sags a little behind the wrist, so its tip trails the flap.
        wing.rod.roll = wristRoll * 0.6f * side;
    }
}
