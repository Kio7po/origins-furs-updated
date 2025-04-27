package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.*;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayerManager;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerEntityRenderer.class, priority = 100)
public class PlayerEntityRenderMixin {

    @Unique
    private static final boolean HOLD_MY_ITEMS_LOADED = FabricLoader.getInstance().isModLoaded("hold-my-items");
    @Unique
    private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    @Inject(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", shift = At.Shift.AFTER)
    )
    private void renderArmOverlayTexture(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local Identifier skinTextureId) {
        for (OriginLayer layer : OriginLayerManager.values()) {
            Origin origin = ModComponents.ORIGIN.get(player).getOrigin(layer);
            if (origin != null) {
                // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
                for (OriginFur fur : ((IPlayerEntityMixins)player).originsFurs$getCurrentFurs()) {
                    // Se asume que el modelo del jugador implementa ModelRootAccessor (mixin)
                    //ModelRootAccessor model = (ModelRootAccessor) this.getModel();
                    OriginFurModel furModel = fur.getGeoModel();
                    Identifier overlayTexture = furModel.getOverlayTexture(false/*model.originsFurs$isSlim()*/);
                    Identifier emissiveTexture = furModel.getEmissiveTexture(false/*model.originsFurs$isSlim()*/);

                    int argb = ColorHelper.Argb.fromFloats (/*revealed ? 0.15F : visible ? 1.0F : 0F*/1,1, 1, 1);

                    List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(player, ModelColorPowerType.class);
                    if (!modelColorPowers.isEmpty()) {
                        argb = Alib.getArgbFromColorPowers(argb, modelColorPowers);
                    }

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
        }
    }

    @Inject(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", shift = At.Shift.BEFORE)
    )
    private void adjustPositionAndHideArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local Identifier skinTextureId) {
        if (HOLD_MY_ITEMS_LOADED || IRIS_LOADED) return;

        // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
        IPlayerEntityMixins iPlayerEntity = (IPlayerEntityMixins) player; // mixin makes this work
        for (OriginFur fur : iPlayerEntity.originsFurs$getCurrentFurs()) {
            if (fur != null && fur.currentAssociatedOrigin != null) {
                PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
                OriginFurModel furModel = fur.getGeoModel();
                EnumSet<OriginFurModel.VMP> hiddenParts = furModel.getHiddenVanillaParts();

                PlayerEntityModel<AbstractClientPlayerEntity> playerModel = entityRenderer.getModel();
                boolean isRightArm = arm == playerModel.rightArm;

                if (isRightArm) arm.hidden = hiddenParts.contains(OriginFurModel.VMP.RIGHT_ARM);
                else arm.hidden = hiddenParts.contains(OriginFurModel.VMP.LEFT_ARM);
            }
        }

        /*
        matrices.push();

        if (player instanceof IPlayerEntityMixins playerMixin) {
            List<OriginFurModel> models = playerMixin.originsFurs$getCurrentModels();
            if (!models.isEmpty()) {
                // todo: Loop all models and use the biggest offset
                OriginFurModel furModel = models.getFirst();
                if (furModel != null) {
                    Vector3d offset = new Vector3d();
                    switch (player.getMainArm()) {
                        case RIGHT -> offset = furModel.getRightOffset();
                        case LEFT -> offset = furModel.getLeftOffset();
                    }
                    // Z <-> Y y Positivo <-> Negativo
                    matrices.translate(offset.getX(), offset.getZ(), offset.getY());
                }
            }
        }

        //int overlay = getOverlay(livingEntity, getAnimationCounter(livingEntity, tickDelta));
        IPlayerEntityMixins iPlayerEntity = (IPlayerEntityMixins) player; // mixin makes this work
        for (OriginsFursClient.OriginFur fur : iPlayerEntity.originsFurs$getCurrentFurs()) {
            if (fur != null && fur.currentAssociatedOrigin != null) {
                PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
                OriginFurModel furModel = fur.getGeoModel();
                Origin origin = fur.currentAssociatedOrigin;

                Vector3d offset = new Vector3d();
                switch (player.getMainArm()) {
                    case RIGHT -> offset = furModel.getRightOffset();
                    case LEFT -> offset = furModel.getLeftOffset();
                }
                matrices.translate(-offset.getX(), -offset.getY(), -offset.getZ());
            }
        }*/
    }

    @Inject(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", shift = At.Shift.AFTER)
    )
    private void renderArmModel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local Identifier skinTextureId) {
        if (HOLD_MY_ITEMS_LOADED || IRIS_LOADED) return;

        // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
        IPlayerEntityMixins iPlayerEntity = (IPlayerEntityMixins) player; // mixin makes this work
        for (OriginFur fur : iPlayerEntity.originsFurs$getCurrentFurs()) {
            if (fur != null && fur.currentAssociatedOrigin != null) {
                PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
                IPlayerEntityMixins entityRenderMixin = (IPlayerEntityMixins) entityRenderer;
                ModelRootAccessor accessor = (ModelRootAccessor)entityRenderer.getModel(); // mixin makes it work
                OriginFurAnimatable animatable = fur.getAnimatable();
                OriginFurModel furModel = fur.getGeoModel();
                Origin origin = fur.currentAssociatedOrigin;

                int argb = ColorHelper.Argb.fromFloats (1,1, 1, 1);
                List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(player, ModelColorPowerType.class);
                if (!modelColorPowers.isEmpty()) {
                    argb = Alib.getArgbFromColorPowers(argb, modelColorPowers);
                }
                fur.setRenderColor(argb);

                // Recorre los huesos decidiendo si deben ser visibles o no
                furModel.preprocess(origin, entityRenderer, entityRenderMixin, accessor, player);

                PlayerEntityModel<AbstractClientPlayerEntity> playerModel = entityRenderer.getModel();
                boolean isRightArm = arm == playerModel.rightArm;

                furModel.getAnimationProcessor().getRegisteredBones().forEach(coreGeoBone -> {
                    String boneName = coreGeoBone.getName();
                    //LogManager.getLogger("Origins Furs").info("BONE: "+boneName);
                    // Esconde aquellos huesos visibles que no sean un brazo y no sean hijos (para evitar esconder hijos del brazo)
                    coreGeoBone.setHidden(coreGeoBone.isHidden() || (!boneName.contains(isRightArm ? "RightArm" : "LeftArm") && coreGeoBone.getParent()==null));
                });

                //fur.setAnimatablePlayer(player);
                matrices.push();

                // Le da la vuelta al modelo (por alguna razón está del revés)
                matrices.multiply(new Quaternionf().rotateX(180 * MathHelper.RADIANS_PER_DEGREE));

                // Baja el modelo (por alguna razón está muy arriba)
                matrices.translate(0, -1.51, 0);
                // Ajustar más el modelo
                matrices.translate(-0.5, -0.5, -0.5);

                if (isRightArm) {
                    IMojangModelPart rightArm = (IMojangModelPart) (Object) arm;
                    furModel.translatePositionForBone("bipedRightArm", rightArm.originsFurs$getPosition());
                    furModel.translatePositionForBone("bipedRightArm", new Vector3d(-5, 2, 0));
                    furModel.setRotationForBone("bipedRightArm", rightArm.originsFurs$getRotation());
                    furModel.invertRotForPart("bipedRightArm", false, true, true);
                } else {
                    IMojangModelPart leftArm = (IMojangModelPart) (Object) arm;
                    furModel.translatePositionForBone("bipedLeftArm", leftArm.originsFurs$getPosition());
                    furModel.translatePositionForBone("bipedLeftArm", new Vector3d(5, 2, 0));
                    furModel.setRotationForBone("bipedLeftArm", leftArm.originsFurs$getRotation());
                    furModel.invertRotForPart("bipedLeftArm", false, true, true);
                }

                // getTextureResource no hace nada con el animatable, se le pasa simplemente por la firma
                fur.render(matrices, animatable, vertexConsumers, RenderLayer.getEntityTranslucent(furModel.getTextureResource(animatable)), null, light);
                fur.render(matrices, animatable, vertexConsumers, RenderLayer.getEntityTranslucentEmissive(furModel.getFullbrightTextureResource(animatable)), null, Integer.MAX_VALUE - 1);
                matrices.pop();
            }
        }
        //matrices.pop();
    }
}
