package com.endplus.client.model;

import com.endplus.entity.minion.VoidWitchEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class VoidWitchModel<T extends VoidWitchEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "void_witch"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public VoidWitchModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData partData = data.getRoot();

        ModelPartData head = partData.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8, 10, 8),
                ModelTransform.NONE);

        ModelPartData hat = head.addChild("hat",
                ModelPartBuilder.create()
                        .uv(0, 64).cuboid(0, 0, 0, 10, 2, 10),
                ModelTransform.pivot(-5.0f, -10.03125f, -5.0f));

        ModelPartData hat2 = hat.addChild("hat2",
                ModelPartBuilder.create()
                        .uv(0, 76).cuboid(0, 0, 0, 7, 4, 7),
                ModelTransform.of(1.75f, -4.0f, 2.0f, -0.05235988f, 0, 0.02617994f));

        ModelPartData hat3 = hat2.addChild("hat3",
                ModelPartBuilder.create()
                        .uv(0, 87).cuboid(0, 0, 0, 4, 4, 4),
                ModelTransform.of(1.75f, -4.0f, 2.0f, -0.10471976f, 0, 0.05235988f));

        hat3.addChild("hat4",
                ModelPartBuilder.create()
                        .uv(0, 95).cuboid(0, 0, 0, 1, 2, 1, new Dilation(0.25f)),
                ModelTransform.of(1.75f, -2.0f, 2.0f, -0.20943952f, 0, 0.10471976f));

        head.addChild("hat_outer",
                ModelPartBuilder.create()
                        .uv(32, 0).cuboid(-4.0f, -10.0f, -4.0f, 8, 10, 8, new Dilation(0.51f)),
                ModelTransform.NONE);

        head.addChild("hat_rim",
                ModelPartBuilder.create()
                        .uv(30, 47).cuboid(-8.0f, -8.0f, -6.0f, 16, 16, 1),
                ModelTransform.of(0, 0, 0, -1.5707964f, 0, 0));

        ModelPartData nose = head.addChild("nose",
                ModelPartBuilder.create()
                        .uv(24, 0).cuboid(-1.0f, -1.0f, -6.0f, 2, 4, 2),
                ModelTransform.pivot(0, -2.0f, 0));

        nose.addChild("mole",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(0, 3.0f, -6.75f, 1, 1, 1, new Dilation(-0.25f)),
                ModelTransform.pivot(0, -2.0f, 0));

        ModelPartData body = partData.addChild("body",
                ModelPartBuilder.create()
                        .uv(16, 20).cuboid(-4.0f, 0, -3.0f, 8, 12, 6),
                ModelTransform.NONE);

        body.addChild("jacket",
                ModelPartBuilder.create()
                        .uv(0, 38).cuboid(-4.0f, 0, -3.0f, 8, 20, 6, new Dilation(0.5f)),
                ModelTransform.NONE);

        partData.addChild("arms",
                ModelPartBuilder.create()
                        .uv(44, 22).cuboid(-8.0f, -2.0f, -2.0f, 4, 8, 4)
                        .uv(44, 22).mirrored().cuboid(4.0f, -2.0f, -2.0f, 4, 8, 4).mirrored(false)
                        .uv(40, 38).cuboid(-4.0f, 2.0f, -2.0f, 8, 4, 4),
                ModelTransform.of(0, 3.0f, -1.0f, -0.75f, 0, 0));

        partData.addChild("right_leg",
                ModelPartBuilder.create()
                        .uv(0, 22).cuboid(-2.0f, 0, -2.0f, 4, 12, 4),
                ModelTransform.pivot(-2.0f, 12.0f, 0));

        partData.addChild("left_leg",
                ModelPartBuilder.create()
                        .uv(0, 22).mirrored().cuboid(-2.0f, 0, -2.0f, 4, 12, 4),
                ModelTransform.pivot(2.0f, 12.0f, 0));

        return TexturedModelData.of(data, 64, 128);
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
        rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4F * limbDistance;
    }
}
