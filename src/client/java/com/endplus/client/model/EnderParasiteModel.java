package com.endplus.client.model;

import com.endplus.entity.mob.EnderParasiteEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Low, six-legged arthropod with a swollen, glowing abdomen and clamping mandibles. */
public class EnderParasiteModel<T extends EnderParasiteEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "ender_parasite"), "main");

    private static final float[] LEG_Z = {-1.0f, 1.0f, 3.0f};

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart abdomen;
    private final ModelPart leftMandible;
    private final ModelPart rightMandible;
    private final ModelPart[] leftLegs = new ModelPart[3];
    private final ModelPart[] rightLegs = new ModelPart[3];

    public EnderParasiteModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.abdomen = this.root.getChild("abdomen");
        this.leftMandible = this.head.getChild("left_mandible");
        this.rightMandible = this.head.getChild("right_mandible");
        for (int i = 0; i < 3; i++) {
            this.leftLegs[i] = this.root.getChild("left_leg_" + i);
            this.rightLegs[i] = this.root.getChild("right_leg_" + i);
        }
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.NONE);

        root.addChild("thorax",
                ModelPartBuilder.create().uv(0, 0).cuboid(-2.0f, -1.5f, -2.0f, 4, 3, 4),
                ModelTransform.pivot(0, 19.0f, 1.0f));

        ModelPartData head = root.addChild("head",
                ModelPartBuilder.create().uv(0, 8).cuboid(-1.5f, -1.0f, -3.0f, 3, 2, 3),
                ModelTransform.pivot(0, 19.0f, -1.0f));
        head.addChild("left_mandible",
                ModelPartBuilder.create().uv(0, 14).cuboid(-0.5f, -0.5f, -2.0f, 1, 1, 2),
                ModelTransform.of(1.0f, 0.5f, -3.0f, 0, 0.3f, 0));
        head.addChild("right_mandible",
                ModelPartBuilder.create().uv(0, 14).mirrored().cuboid(-0.5f, -0.5f, -2.0f, 1, 1, 2),
                ModelTransform.of(-1.0f, 0.5f, -3.0f, 0, -0.3f, 0));

        root.addChild("abdomen",
                ModelPartBuilder.create().uv(20, 0).cuboid(-2.5f, -2.0f, 0, 5, 4, 6),
                ModelTransform.pivot(0, 18.5f, 2.0f));

        for (int i = 0; i < 3; i++) {
            root.addChild("left_leg_" + i,
                    ModelPartBuilder.create().uv(0, 18).cuboid(-0.5f, 0, -0.5f, 1, 5, 1),
                    ModelTransform.of(2.0f, 19.5f, LEG_Z[i], 0, 0, -0.55f));
            root.addChild("right_leg_" + i,
                    ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-0.5f, 0, -0.5f, 1, 5, 1),
                    ModelTransform.of(-2.0f, 19.5f, LEG_Z[i], 0, 0, 0.55f));
        }

        return TexturedModelData.of(data, 64, 32);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        head.yaw = headYaw * ((float) Math.PI / 180F) * 0.4f;

        // Tripod gait: legs in alternate groups swing together.
        float phase = limbAngle * 1.2f;
        float swing = 0.9f * limbDistance;
        for (int i = 0; i < 3; i++) {
            float offset = (i % 2 == 0) ? 0.0f : (float) Math.PI;
            leftLegs[i].yaw = MathHelper.cos(phase + offset) * swing;
            rightLegs[i].yaw = MathHelper.cos(phase + offset + (float) Math.PI) * swing;
        }

        float throb = MathHelper.sin(animationProgress * (entity.isLatched() ? 0.5f : 0.15f));
        abdomen.pitch = throb * 0.04f;
        float chew = entity.isLatched() ? MathHelper.sin(animationProgress * 0.8f) * 0.25f : 0.0f;
        leftMandible.yaw = 0.3f - chew;
        rightMandible.yaw = -0.3f + chew;
    }
}
