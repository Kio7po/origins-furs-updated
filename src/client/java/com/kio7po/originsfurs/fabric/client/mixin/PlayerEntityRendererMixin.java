package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntity;
import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntityModel;
import com.kio7po.originsfurs.fabric.client.feature.FurFeatureRenderer;
import com.kio7po.originsfurs.fabric.client.model.OriginFur;
import com.kio7po.originsfurs.fabric.client.model.OriginFurAnimatable;
import com.kio7po.originsfurs.fabric.client.model.OriginFurModel;
import com.kio7po.originsfurs.fabric.client.util.Alib;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

import java.util.EnumSet;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerEntityRenderer.class, priority = 100)
public class PlayerEntityRendererMixin {

    @Unique
    private static final boolean HOLD_MY_ITEMS_LOADED = FabricLoader.getInstance().isModLoaded("hold-my-items");
    @Unique
    private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    @Inject(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", shift = At.Shift.AFTER)
    )
    private void renderArmOverlayTexture(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local Identifier skinTextureId) {
        int argb = Alib.getRenderColor(player, /*revealed*/false);
        for (OriginFur fur : ((IPlayerEntity)player).originsfurs$getCurrentFurs()) {
            // Se asume que el modelo del jugador implementa ModelRootAccessor (mixin)
            //ModelRootAccessor model = (ModelRootAccessor) this.getModel();
            OriginFurModel furModel = fur.getGeoModel();
            // todo: Terminar esto
            Identifier overlayTexture = furModel.getOverlayTexture(false/*model.originsFurs$isSlim()*/);
            Identifier emissiveTexture = furModel.getEmissiveOverlayTexture(false/*model.originsFurs$isSlim()*/);

            if (overlayTexture != null) {
                RenderLayer renderLayer;
                    //if (OriginsFursClient.isRenderingInWorld && FabricLoader.getInstance().isModLoaded("iris")) {
                    //    renderLayer = RenderLayer.getEntityTranslucent(overlayTexture);
                    //} else {
                        renderLayer = RenderLayer.getEntityTranslucent(overlayTexture);
                    //}
                arm.render(matrices, vertexConsumers.getBuffer(renderLayer), light, OverlayTexture.DEFAULT_UV, argb);
            }
            if (emissiveTexture != null) {
                RenderLayer renderLayer = RenderLayer.getEntityTranslucentEmissive(emissiveTexture);
                arm.render(matrices, vertexConsumers.getBuffer(renderLayer), light, OverlayTexture.DEFAULT_UV, argb);
            }
        }
    }

//    @Inject(
//            method = "renderArm",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", shift = At.Shift.BEFORE)
//    )
//    private void adjustPositionAndHideArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local Identifier skinTextureId) {
//        if (HOLD_MY_ITEMS_LOADED || IRIS_LOADED) return;
//
//        PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
//        PlayerEntityModel<AbstractClientPlayerEntity> playerModel = entityRenderer.getModel();
//        boolean isRightArm = arm == playerModel.rightArm;
//
//        for (OriginFur fur : ((IPlayerEntity) player).originsfurs$getCurrentFurs()) {
////            if (arm.hidden) break;
//
//            OriginFurModel furModel = fur.getGeoModel();
//            EnumSet<OriginFurModel.VMP> hiddenParts = furModel.getHiddenVanillaParts();
//
//            arm.hidden = hiddenParts.contains(isRightArm?OriginFurModel.VMP.RIGHT_ARM:OriginFurModel.VMP.LEFT_ARM);
//        }
//
//        /*
//        matrices.push();
//
//        if (player instanceof IPlayerEntityMixins playerMixin) {
//            List<OriginFurModel> models = playerMixin.originsFurs$getCurrentModels();
//            if (!models.isEmpty()) {
//                // todo: Loop all models and use the biggest offset
//                OriginFurModel furModel = models.getFirst();
//                if (furModel != null) {
//                    Vector3d offset = new Vector3d();
//                    switch (player.getMainArm()) {
//                        case RIGHT -> offset = furModel.getRightOffset();
//                        case LEFT -> offset = furModel.getLeftOffset();
//                    }
//                    // Z <-> Y y Positivo <-> Negativo
//                    matrices.translate(offset.getX(), offset.getZ(), offset.getY());
//                }
//            }
//        }
//
//        //int overlay = getOverlay(livingEntity, getAnimationCounter(livingEntity, tickDelta));
//        IPlayerEntityMixins iPlayerEntity = (IPlayerEntityMixins) player; // mixin makes this work
//        for (OriginsFursClient.OriginFur fur : iPlayerEntity.originsFurs$getCurrentFurs()) {
//            if (fur != null && fur.currentAssociatedOrigin != null) {
//                PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
//                OriginFurModel furModel = fur.getGeoModel();
//                Origin origin = fur.currentAssociatedOrigin;
//
//                Vector3d offset = new Vector3d();
//                switch (player.getMainArm()) {
//                    case RIGHT -> offset = furModel.getRightOffset();
//                    case LEFT -> offset = furModel.getLeftOffset();
//                }
//                matrices.translate(-offset.getX(), -offset.getY(), -offset.getZ());
//            }
//        }*/
//    }
//
//    @Inject(
//            method = "renderArm",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", shift = At.Shift.AFTER)
//    )
//    private void renderArmModel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local Identifier skinTextureId) {
//        if (HOLD_MY_ITEMS_LOADED || IRIS_LOADED) return;
//
//        float partialTick = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
//        int argb = Alib.getRenderColor(player, false);
//        PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
//        IPlayerEntityModel accessor = (IPlayerEntityModel)entityRenderer.getModel(); // mixin makes it work
//        PlayerEntityModel<AbstractClientPlayerEntity> playerModel = entityRenderer.getModel();
//        boolean isRightArm = arm == playerModel.rightArm;
//
//        for (OriginFur fur : ((IPlayerEntity)player).originsfurs$getCurrentFurs()) {
//            OriginFurAnimatable animatable = fur.getAnimatable();
//            OriginFurModel furModel = fur.getGeoModel();
//
//            // Color
//            fur.setRenderColor(argb);
//
//            // Decides and sets visibility for bones
//            furModel.preprocess(accessor, player);
//            fur.setAnimatablePlayer(player);
//
//            furModel.getAnimationProcessor().getRegisteredBones().forEach(coreGeoBone -> {
//                String boneName = coreGeoBone.getName();
//                // Esconde aquellos huesos visibles que no sean un brazo y no sean hijos (para evitar esconder hijos del brazo)
//                coreGeoBone.setHidden(coreGeoBone.isHidden() || (!boneName.contains(isRightArm ? "RightArm" : "LeftArm") && coreGeoBone.getParent()==null));
//            });
//
//            if (isRightArm) {
//                GeoBone rightArm = furModel.getCachedGeoBone("bipedRightArm");
//                if (rightArm != null) {
//                    RenderUtil.matchModelPartRot(arm, rightArm);
//                    rightArm.updatePosition(arm.pivotX + 5, 2 - arm.pivotY, arm.pivotZ);
//                }
//            } else {
//                GeoBone leftArm = furModel.getCachedGeoBone("bipedLeftArm");
//                if (leftArm != null) {
//                    RenderUtil.matchModelPartRot(arm, leftArm);
//                    leftArm.updatePosition(arm.pivotX - 5, 2 - arm.pivotY, arm.pivotZ);
//                }
//            }
//
//            matrices.push();
//
//            // getTextureResource no hace nada con el animatable, se le pasa simplemente por la firma
//            fur.render(matrices, animatable, vertexConsumers, RenderLayer.getEntityTranslucent(furModel.getTextureResource(animatable)), null, light, partialTick);
////            fur.render(matrices, animatable, vertexConsumers, RenderLayer.getEntityTranslucentEmissive(furModel.getTextureGlowmaskResource()), null, Integer.MAX_VALUE - 1, partialTick);
//
//            matrices.pop();
//        }
//        //matrices.pop();
//    }

    // Añade la feature, lo que causa que se lance su render() en cada frame
    // Es lo que usan para renderizar cosas como las elytra y los objetos en la mano
    @Inject(method="<init>", at=@At(value = "TAIL"))
    void initMixin(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)this;
        PlayerEntityRenderer renderer = (PlayerEntityRenderer)(Object)this;
        accessor.invokeAddFeature(new FurFeatureRenderer(renderer));
    }
}
