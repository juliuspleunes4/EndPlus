package com.endplus.client.model;

import com.endplus.entity.mob.AncientEndGolemEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Colossal, long-armed stone guardian with a rune pillar on its back and shards hovering at its shoulders. */
public class AncientEndGolemModel<T extends AncientEndGolemEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "ancient_end_golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShard;
    private final ModelPart leftShard;

    public AncientEndGolemModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
        this.rightShard = this.body.getChild("right_shard");
        this.leftShard = this.body.getChild("left_shard");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.NONE);

        ModelPartData body = root.addChild("body",
                ModelPartBuilder.create()
                        .uv(0, 18).cuboid(-9.0f, -8.0f, -5.0f, 18, 16, 10)
                        .uv(52, 46).cuboid(-2.0f, -12.0f, 5.0f, 4, 12, 3),
                ModelTransform.pivot(0, 0, 0));

        body.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8, 8, 8),
                ModelTransform.pivot(0, -8.0f, 0));

        body.addChild("right_arm",
                ModelPartBuilder.create().uv(0, 46).cuboid(-3.0f, 0, -3.0f, 6, 26, 6),
                ModelTransform.pivot(-12.0f, -6.0f, 0));
        body.addChild("left_arm",
                ModelPartBuilder.create().uv(0, 46).mirrored().cuboid(-3.0f, 0, -3.0f, 6, 26, 6),
                ModelTransform.pivot(12.0f, -6.0f, 0));

        body.addChild("right_shard",
                ModelPartBuilder.create().uv(70, 46).cuboid(-1.5f, -1.5f, -1.5f, 3, 3, 3),
                ModelTransform.pivot(-9.0f, -15.0f, 1.0f));
        body.addChild("left_shard",
                ModelPartBuilder.create().uv(70, 46).cuboid(-1.5f, -1.5f, -1.5f, 3, 3, 3),
                ModelTransform.pivot(9.0f, -15.0f, 1.0f));

        root.addChild("right_leg",
                ModelPartBuilder.create().uv(26, 46).cuboid(-3.0f, 0, -3.0f, 6, 16, 6),
                ModelTransform.pivot(-5.0f, 8.0f, 0));
        root.addChild("left_leg",
                ModelPartBuilder.create().uv(26, 46).mirrored().cuboid(-3.0f, 0, -3.0f, 6, 16, 6),
                ModelTransform.pivot(5.0f, 8.0f, 0));

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

        // Heavy, slow gait.
        float phase = limbAngle * 0.5f;
        rightLeg.pitch = MathHelper.cos(phase) * 1.1f * limbDistance;
        leftLeg.pitch = MathHelper.cos(phase + (float) Math.PI) * 1.1f * limbDistance;
        rightArm.pitch = MathHelper.cos(phase + (float) Math.PI) * 1.0f * limbDistance;
        leftArm.pitch = MathHelper.cos(phase) * 1.0f * limbDistance;
        rightArm.roll = 0.05f;
        leftArm.roll = -0.05f;

        float breath = MathHelper.sin(animationProgress * 0.05f);
        body.pitch = breath * 0.01f;

        // The shards hover and bob out of step with one another.
        rightShard.pivotY = -15.0f + MathHelper.sin(animationProgress * 0.08f) * 1.5f;
        leftShard.pivotY = -15.0f + MathHelper.sin(animationProgress * 0.08f + 2.0f) * 1.5f;
        rightShard.yaw = animationProgress * 0.04f;
        leftShard.yaw = -animationProgress * 0.04f;

        if (entity.isSlamming()) {
            // Fists overhead, body arched back.
            rightArm.pitch = -2.9f;
            leftArm.pitch = -2.9f;
            rightArm.roll = 0.2f;
            leftArm.roll = -0.2f;
            body.pitch = -0.2f;
            head.pitch -= 0.2f;
        } else if (entity.isAttacking()) {
            rightArm.pitch = -1.5f;
            leftArm.pitch = -1.3f;
        }
    }
}
