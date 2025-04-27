package com.kio7po.originsfurs.fabric.client;

import com.google.gson.JsonObject;
import io.github.apace100.origins.origin.Origin;
import mod.azure.azurelib.common.api.client.renderer.GeoObjectRenderer;
import mod.azure.azurelib.core.object.Color;
import net.minecraft.entity.player.PlayerEntity;

// todo: La jerarquía de clases de esta es un poco rara
public class OriginFur extends GeoObjectRenderer<OriginFurAnimatable> {
    public Origin currentAssociatedOrigin = Origin.EMPTY;
    public OriginFurAnimatable animatable;
    private Color color;
    public static final OriginFur EMPTY_FUR = new OriginFur(new JsonObject());

    /*
    private float getTick() {
        return MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
    }

    public void renderBone(String name, MatrixStack poseStack, @Nullable VertexConsumerProvider bufferSource,
                           @Nullable RenderLayer renderType, @Nullable VertexConsumer buffer, int packedLight) {
        poseStack.push();
        GeoBone bone = this.getGeoModel().getBone(name).orElse(null);
        if (bone != null) {
            if (buffer == null)
                buffer = bufferSource.getBuffer(renderType);

            List<GeoCube> cubes = bone.getCubes();
            float tick = getTick();
            int packedOverlay = this.getPackedOverlay(this.animatable, 0.0F, tick);

            for (GeoBone child_bone : bone.getChildBones()) {
                cubes.addAll(child_bone.getCubes());
            }

            VertexConsumer finalBuffer = buffer;
            cubes.forEach(cube -> this.renderRecursively(
                    poseStack,
                    this.animatable,
                    bone,
                    renderType,
                    bufferSource,
                    finalBuffer,
                    false,
                    tick,
                    packedLight,
                    packedOverlay,
                    0xFFFFFFFF  // Formato ARGB (Alpha-Red-Green-Blue)
            ));
            poseStack.pop();
        }
    }
    */

    // Solía ser setPlayer, pero este nombre me parece mejor
    public void setAnimatablePlayer(PlayerEntity player) {
        this.animatable.setPlayer(player);
    }

    @Override
    public OriginFurModel getGeoModel() {
        return (OriginFurModel) super.getGeoModel();
    }

    @Override
    public OriginFurAnimatable getAnimatable() {
        return this.animatable;
    }

    @Override
    public Color getRenderColor(OriginFurAnimatable animatable, float partialTick, int packedLight) {
        return this.color;
    }

    public void setRenderColor(int argb) {
        this.color = new Color(argb);
    }

    public OriginFur(JsonObject json) {
        super(new OriginFurModel(json));
        this.animatable = new OriginFurAnimatable();
    }
}
