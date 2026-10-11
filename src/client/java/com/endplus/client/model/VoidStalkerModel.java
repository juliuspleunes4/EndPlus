package com.endplus.client.model;

import com.endplus.entity.mob.VoidStalkerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class VoidStalkerModel<T extends VoidStalkerEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "void_stalker"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;

    public VoidStalkerModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.tail = this.root.getChild("tail");
        this.frontRightLeg = this.root.getChild("front_right_leg");
        this.frontLeftLeg = this.root.getChild("front_left_leg");
        this.backRightLeg = this.root.getChild("back_right_leg");
        this.backLeftLeg = this.root.getChild("back_left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.NONE);

        root.addChild("body",
                ModelPartBuilder.create().uv(0, 12).cuboid(-3.0f, -2.5f, -6.0f, 6, 5, 12),
                ModelTransform.pivot(0, 15.5f, 0));

        root.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-2.5f, -2.5f, -5.0f, 5, 5, 5)
                        .uv(22, 0).cuboid(-2.5f, -4.5f, -2.0f, 1, 2, 1)
                        .uv(22, 0).cuboid(1.5f, -4.5f, -2.0f, 1, 2, 1),
                ModelTransform.pivot(0, 14.5f, -6.0f));

        root.addChild("tail",
                ModelPartBuilder.create().uv(40, 10).cuboid(-1.0f, -1.0f, 0, 2, 2, 8),
                ModelTransform.of(0, 14.0f, 6.0f, 0.5f, 0, 0));

        ModelPartBuilder leg = ModelPartBuilder.create().uv(40, 0).cuboid(-1.0f, 0, -1.0f, 2, 6, 2);
        root.addChild("front_right_leg", leg, ModelTransform.pivot(-2.0f, 18.0f, -4.5f));
        root.addChild("front_left_leg", leg, ModelTransform.pivot(2.0f, 18.0f, -4.5f));
        root.addChild("back_right_leg", leg, ModelTransform.pivot(-2.0f, 18.0f, 4.5f));
        root.addChild("back_left_leg", leg, ModelTransform.pivot(2.0f, 18.0f, 4.5f));

        return TexturedModelData.of(data, 64, 32);
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
        float swing = limbDistance * 1.4f;
        float phase = limbAngle * 0.6662f;
        frontRightLeg.pitch = (float) Math.cos(phase) * swing;
        backLeftLeg.pitch = (float) Math.cos(phase) * swing;
        frontLeftLeg.pitch = (float) Math.cos(phase + Math.PI) * swing;
        backRightLeg.pitch = (float) Math.cos(phase + Math.PI) * swing;
        tail.yaw = (float) Math.sin(animationProgress * 0.1f) * 0.15f;
    }
}
