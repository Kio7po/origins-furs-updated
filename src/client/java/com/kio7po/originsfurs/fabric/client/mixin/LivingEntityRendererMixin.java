package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntity;
import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntityModel;
import com.kio7po.originsfurs.fabric.client.model.OriginFur;
import com.kio7po.originsfurs.fabric.client.model.OriginFurModel;
import com.kio7po.originsfurs.fabric.client.util.Alib;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayerManager;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.util.ClientUtil;

import java.util.EnumSet;
import java.util.List;

@Mixin(value = LivingEntityRenderer.class, priority = 100)
public abstract class LivingEntityRendererMixin <T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    public static int getOverlay(LivingEntity entity, float whiteOverlayProgress) {
        return 0;
    }

    @Shadow protected abstract float getAnimationCounter(T entity, float tickDelta);

    @Shadow public abstract M getModel();

//    @Shadow protected abstract boolean isVisible(T entity);

    @Shadow protected M model;

    @Shadow protected abstract @Nullable RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

//    @Unique boolean isInvisible = false;

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
        model.hat.hidden = model.hat.hidden || hiddenParts.contains(OriginFurModel.VMP.HAT);
        model.head.hidden = model.head.hidden || hiddenParts.contains(OriginFurModel.VMP.HEAD);
        model.body.hidden = model.body.hidden || hiddenParts.contains(OriginFurModel.VMP.BODY);
        model.jacket.hidden = model.jacket.hidden || hiddenParts.contains(OriginFurModel.VMP.JACKET);
        model.rightArm.hidden = model.rightArm.hidden || hiddenParts.contains(OriginFurModel.VMP.RIGHT_ARM);
        model.leftArm.hidden = model.leftArm.hidden || hiddenParts.contains(OriginFurModel.VMP.LEFT_ARM);
        model.rightSleeve.hidden = model.rightSleeve.hidden || hiddenParts.contains(OriginFurModel.VMP.RIGHT_SLEEVE);
        model.leftSleeve.hidden = model.leftSleeve.hidden || hiddenParts.contains(OriginFurModel.VMP.LEFT_SLEEVE);
        model.rightLeg.hidden = model.rightLeg.hidden || hiddenParts.contains(OriginFurModel.VMP.RIGHT_LEG);
        model.leftLeg.hidden = model.leftLeg.hidden || hiddenParts.contains(OriginFurModel.VMP.LEFT_LEG);
        model.rightPants.hidden = model.rightPants.hidden || hiddenParts.contains(OriginFurModel.VMP.RIGHT_PANTS);
        model.leftPants.hidden = model.leftPants.hidden || hiddenParts.contains(OriginFurModel.VMP.LEFT_PANTS);
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
        boolean invisible = livingEntity.isInvisible();
        boolean revealed = invisible && !livingEntity.isInvisibleTo(ClientUtil.getClientPlayer());
        boolean outlined = MinecraftClient.getInstance().hasOutline(livingEntity);

        if ((!invisible || revealed || outlined) && livingEntity instanceof AbstractClientPlayerEntity acpe) {
            int overlay = getOverlay(livingEntity, getAnimationCounter(livingEntity, tickDelta));
            // Se asume que el modelo del jugador implementa ModelRootAccessor (mixin)
            IPlayerEntityModel modelAccessor = (IPlayerEntityModel) this.getModel();
            int argb = Alib.getRenderColor(acpe, revealed);

            PlayerEntityModel<T> pem = (PlayerEntityModel<T>) this.getModel();
            setPlayerEntityModelPartsHidden(pem, false);

            for (OriginFur fur : ((IPlayerEntity)acpe).originsfurs$getCurrentFurs()) {
                OriginFurModel furModel = fur.getGeoModel();
                Identifier overlayTexture = furModel.getOverlayTexture(modelAccessor.originsfurs$isSlim());
                Identifier emissiveTexture = furModel.getEmissiveOverlayTexture(modelAccessor.originsfurs$isSlim());

                if (overlayTexture != null) {
                    RenderLayer renderLayer = Alib.getRenderLayer(overlayTexture, !invisible, revealed, outlined, false);
                    this.model.render(
                            matrixStack,
                            vertexConsumerProvider.getBuffer(renderLayer),
                            light,
                            overlay,
                            argb
                    );
                }
                if (emissiveTexture != null) {
                    RenderLayer renderLayer = Alib.getRenderLayer(emissiveTexture, !invisible, revealed, outlined, true);
                    this.model.render(
                            matrixStack,
                            vertexConsumerProvider.getBuffer(renderLayer),
                            light,
                            overlay,
                            argb
                    );
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
    private void renderPreProcess(
            T livingEntity,
            float f,
            float tickDelta,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int light, CallbackInfo ci)
    {
        if (livingEntity instanceof AbstractClientPlayerEntity acpe) {
            //isInvisible = false;
            for (OriginFur fur : ((IPlayerEntity)acpe).originsfurs$getCurrentFurs()) {
                OriginFurModel furModel = fur.getGeoModel();

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

                //if (!isInvisible) {
                    EnumSet<OriginFurModel.VMP> hiddenParts = furModel.getHiddenVanillaParts();
                    PlayerEntityModel<T> model = (PlayerEntityModel<T>) this.getModel(); // modelo original del jugador
                    setPlayerEntityModelPartsHidden(model, hiddenParts);
                //}
            }
        }
    }
}