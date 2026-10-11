package com.endplus.client.model;

import com.endplus.entity.mob.ShadeLurkerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

/** Knuckle-walking, long-armed brute with a low-slung head and umbral twigs growing from its back. */
public class ShadeLurkerModel<T extends ShadeLurkerEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "shade_lurker"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ShadeLurkerModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.root.getChild("head");
        this.rightArm = this.root.getChild("right_arm");
        this.leftArm = this.root.getChild("left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.NONE);

        ModelPartData body = root.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -3.5f, -5.0f, 8, 7, 10),
                ModelTransform.pivot(0, 11.5f, 0));
        // Umbral twigs sprouting from the spine.
        body.addChild("twig_front",
                ModelPartBuilder.create().uv(0, 30).cuboid(-0.5f, -4.0f, -0.5f, 1, 4, 1),
                ModelTransform.of(-1.5f, -3.5f, -2.5f, -0.3f, 0, -0.3f));
        body.addChild("twig_mid",
                ModelPartBuilder.create().uv(0, 30).cuboid(-0.5f, -5.0f, -0.5f, 1, 5, 1),
                ModelTransform.of(1.5f, -3.5f, 0.5f, 0.1f, 0, 0.35f));
        body.addChild("twig_back",
                ModelPartBuilder.create().uv(0, 30).cuboid(-0.5f, -4.0f, -0.5f, 1, 4, 1),
                ModelTransform.of(-0.5f, -3.5f, 3.5f, 0.45f, 0, -0.1f));

        ModelPartData head = root.addChild("head",
                ModelPartBuilder.create().uv(0, 18).cuboid(-2.5f, -2.5f, -4.0f, 5, 5, 4),
                ModelTransform.pivot(0, 10.5f, -5.0f));
        head.addChild("twig_left",
                ModelPartBuilder.create().uv(0, 30).cuboid(-0.5f, -4.0f, -0.5f, 1, 4, 1),
                ModelTransform.of(1.5f, -2.0f, -1.0f, -0.2f, 0, 0.5f));
        head.addChild("twig_right",
                ModelPartBuilder.create().uv(0, 30).cuboid(-0.5f, -4.0f, -0.5f, 1, 4, 1),
                ModelTransform.of(-1.5f, -2.0f, -1.0f, -0.2f, 0, -0.5f));

        ModelPartBuilder arm = ModelPartBuilder.create().uv(36, 0).cuboid(-1.5f, 0, -1.5f, 3, 16, 3);
        root.addChild("right_arm", arm, ModelTransform.pivot(-5.5f, 8.0f, -3.0f));
        root.addChild("left_arm", arm, ModelTransform.pivot(5.5f, 8.0f, -3.0f));

        ModelPartBuilder leg = ModelPartBuilder.create().uv(48, 0).cuboid(-1.5f, 0, -1.5f, 3, 8, 3);
        root.addChild("right_leg", leg, ModelTransform.pivot(-2.5f, 16.0f, 4.0f));
        root.addChild("left_leg", leg, ModelTransform.pivot(2.5f, 16.0f, 4.0f));

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

        float phase = limbAngle * 0.6662f;
        float swing = 1.1f * limbDistance;
        rightArm.pitch = (float) Math.cos(phase) * swing;
        leftArm.pitch = (float) Math.cos(phase + Math.PI) * swing;
        rightLeg.pitch = (float) Math.cos(phase + Math.PI) * swing;
        leftLeg.pitch = (float) Math.cos(phase) * swing;

        // Slow breathing; the idle arms rock very slightly.
        float breath = (float) Math.sin(animationProgress * 0.06f);
        body.pitch = breath * 0.015f;
        rightArm.roll = 0.04f + breath * 0.02f;
        leftArm.roll = -0.04f - breath * 0.02f;

        if (entity.isAttacking()) {
            // Mid-lunge: both arms thrown forward.
            rightArm.pitch = -1.7f;
            leftArm.pitch = -1.55f;
            body.pitch = 0.2f;
        }
    }
}
