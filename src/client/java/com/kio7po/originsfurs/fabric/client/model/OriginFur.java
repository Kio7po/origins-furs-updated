package com.kio7po.originsfurs.fabric.client.model;

import com.google.gson.JsonObject;
import com.kio7po.originsfurs.fabric.client.mixin.WorldRendererAccessor;
import io.github.apace100.origins.origin.Origin;
//import mod.azure.azurelib.common.api.client.renderer.GeoObjectRenderer;
//import mod.azure.azurelib.core.object.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.Color;

// todo: La jerarquía de clases de esta es un poco rara
public class OriginFur extends GeoObjectRenderer<OriginFurAnimatable> {
    public Origin origin;
    public OriginFurAnimatable animatable;
    private Color color;
    public static final OriginFur EMPTY = new OriginFur(Origin.EMPTY, new JsonObject());

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

    // Makes the renderer use the same Overlay as the player (ex: red overlay from damage)
    @Override
    public int getPackedOverlay(OriginFurAnimatable animatable, float u, float partialTick) {
        return LivingEntityRenderer.getOverlay(animatable.player, 0);
    }

    public OriginFur(Origin origin, JsonObject json) {
        super(new OriginFurModel(origin, json));
        if (this.getGeoModel().getTextureGlowmaskResource() != null) {
            addRenderLayer(new GlowingOriginFurLayer(this));
        }
        this.origin = origin;
        this.animatable = new OriginFurAnimatable();
    }

    // Overrides version from GeoObjectRenderer
    // Applies the transformations from LivingEntityRenderer
    @Override
    public void preRender(MatrixStack poseStack, OriginFurAnimatable animatable, BakedGeoModel model, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (!isReRender) {
            this.objectRenderTranslations = new Matrix4f(poseStack.peek().getPositionMatrix());

            scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);

//            poseStack.multiply(new Quaternionf().rotateZ(180 * MathHelper.RADIANS_PER_DEGREE));
//            poseStack.translate(0,-1.5,0);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.5F, 0.0F);
        }
    }
}
