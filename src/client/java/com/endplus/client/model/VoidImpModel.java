package com.endplus.client.model;

import com.endplus.entity.minion.VoidImpEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class VoidImpModel<T extends VoidImpEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of("endplus", "void_imp"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public VoidImpModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData partData = data.getRoot();

        ModelPartData root = partData.addChild("root",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0, -2.5f, 0));

        root.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-2.5f, -5.0f, -2.5f, 5, 5, 5),
                ModelTransform.pivot(0, 20.0f, 0));

        ModelPartData body = root.addChild("body",
                ModelPartBuilder.create()
                        .uv(0, 10).cuboid(-1.5f, 0, -1.0f, 3, 4, 2)
                        .uv(0, 16).cuboid(-1.5f, 1.0f, -1.0f, 3, 5, 2, new Dilation(-0.2f)),
                ModelTransform.pivot(0, 20.0f, 0));

        body.addChild("right_arm",
                ModelPartBuilder.create()
                        .uv(23, 0).cuboid(-1.25f, -0.5f, -1.0f, 2, 4, 2, new Dilation(-0.1f)),
                ModelTransform.pivot(-1.75f, 0.25f, 0));

        body.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(23, 6).cuboid(-0.75f, -0.5f, -1.0f, 2, 4, 2, new Dilation(-0.1f)),
                ModelTransform.pivot(1.75f, 0.25f, 0));

        body.addChild("left_wing",
                ModelPartBuilder.create()
                        .uv(16, 14).cuboid(0, 0, 0, 0, 5, 8),
                ModelTransform.pivot(0.5f, 1.0f, 1.0f));

        body.addChild("right_wing",
                ModelPartBuilder.create()
                        .uv(16, 14).cuboid(0, 0, 0, 0, 5, 8),
                ModelTransform.pivot(-0.5f, 1.0f, 1.0f));

        return TexturedModelData.of(data, 32, 32);
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
    }
}
