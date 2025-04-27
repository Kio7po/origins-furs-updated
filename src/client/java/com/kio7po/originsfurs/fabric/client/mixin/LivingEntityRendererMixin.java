package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayerManager;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

@Mixin(value = LivingEntityRenderer.class, priority = 100)
public abstract class LivingEntityRendererMixin <T extends LivingEntity, M extends EntityModel<T>> implements IPlayerEntityMixins {

    @Shadow
    public static int getOverlay(LivingEntity entity, float whiteOverlayProgress) {
        return 0;
    }

    @Shadow protected abstract float getAnimationCounter(T entity, float tickDelta);

    @Shadow public abstract M getModel();

    @Shadow protected abstract boolean isVisible(T entity);

    @Shadow protected M model;

    @Unique
    boolean isInvisible = false;

    @Unique
    private void setPlayerEntityModelPartsHidden(PlayerEntityModel<T> model, boolean hidden) {
        model.hat.hidden = hidden;
        model.head.hidden = hidden;
        model.body.hidden = hidden;
        model.jacket.hidden = hidden;
        model.leftArm.hidden = hidden;
        model.leftSleeve.hidden = hidden;
        model.rightArm.hidden = hidden;
        model.rightSleeve.hidden = hidden;
        model.leftLeg.hidden = hidden;
        model.leftPants.hidden = hidden;
        model.rightLeg.hidden = hidden;
        model.rightPants.hidden = hidden;
    }

    @Unique
    private void setPlayerEntityModelPartsHidden(PlayerEntityModel<T> model, EnumSet<OriginFurModel.VMP> hiddenParts) {
        model.hat.hidden = hiddenParts.contains(OriginFurModel.VMP.HAT);
        model.head.hidden = hiddenParts.contains(OriginFurModel.VMP.HEAD);
        model.body.hidden = hiddenParts.contains(OriginFurModel.VMP.BODY);
        model.jacket.hidden = hiddenParts.contains(OriginFurModel.VMP.JACKET);
        model.rightArm.hidden = hiddenParts.contains(OriginFurModel.VMP.RIGHT_ARM);
        model.leftArm.hidden = hiddenParts.contains(OriginFurModel.VMP.LEFT_ARM);
        model.rightSleeve.hidden = hiddenParts.contains(OriginFurModel.VMP.RIGHT_SLEEVE);
        model.leftSleeve.hidden = hiddenParts.contains(OriginFurModel.VMP.LEFT_SLEEVE);
        model.rightLeg.hidden = hiddenParts.contains(OriginFurModel.VMP.RIGHT_LEG);
        model.leftLeg.hidden = hiddenParts.contains(OriginFurModel.VMP.LEFT_LEG);
        model.rightPants.hidden = hiddenParts.contains(OriginFurModel.VMP.RIGHT_PANTS);
        model.leftPants.hidden = hiddenParts.contains(OriginFurModel.VMP.LEFT_PANTS);
    }

    // Acceso a method de la clase original. Necesario para usarlo en el mixin
    @Shadow protected abstract boolean addFeature(FeatureRenderer<T, M> feature);

    // Añade la feature, lo que causa que se lance su render (el modelo) en cada frame
    // Es lo que usan para renderizar cosas como las elytra y los objetos en la mano
    @Inject(method="<init>", at=@At(value = "TAIL"))
    void initMixin(EntityRendererFactory.Context context, EntityModel model, float shadowRadius, CallbackInfo ci) {
        if (model instanceof PlayerEntityModel) {
            addFeature(new FurRenderFeature((LivingEntityRenderer)(Object)this));
        }
    }

    @Unique
    private static OriginComponent originComponent(PlayerEntity player) {
        return ModComponents.ORIGIN.get(player);
    }

    // Mixin for player render that adds skin textures and emissive textures
    // Si el modelo vanilla no debe verse (isInvisible) entonces no hace nada
    @Inject(
            method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", shift = At.Shift.AFTER)
    )
    private void renderOverlayTexture(
            T livingEntity,
            float f,
            float tickDelta,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int light,
            CallbackInfo ci
    ) {
        if (!isInvisible && livingEntity instanceof AbstractClientPlayerEntity acpe) {
            int overlay = getOverlay(livingEntity, getAnimationCounter(livingEntity, tickDelta));
            // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
            for (OriginLayer layer : OriginLayerManager.values()) {
                Origin origin = originComponent((PlayerEntity)livingEntity).getOrigin(layer);
                if (origin != null) {
                    // todo: Revisar esto, está regulero
                    for (OriginFur fur : ((IPlayerEntityMixins)acpe).originsFurs$getCurrentFurs()) {
                        // Se asume que el modelo del jugador implementa ModelRootAccessor (mixin)
                        ModelRootAccessor model = (ModelRootAccessor) this.getModel();
                        OriginFurModel furModel = fur.getGeoModel();
                        Identifier overlayTexture = furModel.getOverlayTexture(model.originsFurs$isSlim());
                        Identifier emissiveTexture = furModel.getEmissiveTexture(model.originsFurs$isSlim());

                        // Si el modelo es visible
                        boolean visible = isVisible(livingEntity);
                        // Si a pesar de no ser visible, lo puedes ver (espectador)
                        boolean revealed = !visible && !livingEntity.isInvisibleTo(MinecraftClient.getInstance().player);

                        int argb = ColorHelper.Argb.fromFloats (revealed ? 0.15F : visible ? 1.0F : 0F,1, 1, 1);

                        // Only if you can directly see it
                        if (visible) {
                            List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(acpe, ModelColorPowerType.class);
                            if (!modelColorPowers.isEmpty()) {
                                argb = Alib.getArgbFromColorPowers(argb, modelColorPowers);
                            }
                        }

                        if (overlayTexture != null) {
                            RenderLayer renderLayer;
//                                if (OriginsFursClient.isRenderingInWorld && FabricLoader.getInstance().isModLoaded("iris")) {
//                                    renderLayer = RenderLayer.getEntityTranslucent(overlayTexture);
//                                } else {
                            renderLayer = RenderLayer.getEntityTranslucent(overlayTexture);
//                                }
                            this.model.render(
                                    matrixStack,
                                    vertexConsumerProvider.getBuffer(renderLayer),
                                    light,
                                    overlay,
                                    argb
                            );
                        }
                        if (emissiveTexture != null) {
                            RenderLayer renderLayer = RenderLayer.getEntityTranslucentEmissive(emissiveTexture);
                            this.model.render(
                                    matrixStack,
                                    vertexConsumerProvider.getBuffer(renderLayer),
                                    light,
                                    overlay,
                                    argb
                            );
                        }
                        PlayerEntityModel<T> pem = (PlayerEntityModel<T>) model;
                        setPlayerEntityModelPartsHidden(pem, false);
                    }
                }
            }
        }
    }

    // Se encarga de esconder las partes del modelo vanilla
    @Inject(
            method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at=@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void renderPreProcessMixin(
            T livingEntity,
            float f,
            float tickDelta,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int light, CallbackInfo ci)
    {
        if (livingEntity instanceof AbstractClientPlayerEntity acpe) {
            //isInvisible = false;
            OriginComponent component = originComponent(acpe);
            // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
            for (OriginLayer layer : OriginLayerManager.values()) {
                Origin origin = component.getOrigin(layer);
                if (origin != null) {
                    for (OriginFur fur : ((IPlayerEntityMixins)acpe).originsFurs$getCurrentFurs()) {
                        OriginFurModel furModel = fur.getGeoModel();

                        //furModel.preRender$mixinOnly(acpe);

                        // No se refiere a si es literalmente invisible,
                        // si no a si el furModel indica que debe serlo
                        // No lo necesito ahora mismo
                            /*
                            if (furModel.isPlayerModelInvisible()) {
                                isInvisible = true;
                                //matrixStack.translate(0, 9999, 0);
                            } else {
                                isInvisible = false;
                            }*/

                        if (!isInvisible) {
                            EnumSet<OriginFurModel.VMP> hiddenParts = furModel.getHiddenVanillaParts();
                            PlayerEntityModel<T> model = (PlayerEntityModel<T>) this.getModel(); // modelo original del jugador
                            setPlayerEntityModelPartsHidden(model, hiddenParts);
                        }
                    }
                }
            }
        }
    }
}