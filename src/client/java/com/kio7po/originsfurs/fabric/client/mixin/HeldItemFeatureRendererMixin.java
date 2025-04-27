package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.IPlayerEntityMixins;
import com.kio7po.originsfurs.fabric.client.OriginFurModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {
    @Inject(method = "renderItem", at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    shift = At.Shift.BEFORE
            )
    )
    void adjustItemPosition(
            LivingEntity entity,
            ItemStack stack,
            ModelTransformationMode transformationMode,
            Arm arm,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int light,
            CallbackInfo ci
    ) {
        if (entity instanceof ClientPlayerEntity cpe && entity instanceof IPlayerEntityMixins pem) {
            List<OriginFurModel> models = pem.originsFurs$getCurrentModels();
            if (!models.isEmpty()) {
                // todo: Loop all models and use the biggest offset (or, better, cache it)
                OriginFurModel model = models.getFirst();
                if (model != null) {
                    Vector3d offset = new Vector3d();
                    switch (arm) {
                        case RIGHT -> offset = model.getRightOffset();
                        case LEFT -> offset = model.getLeftOffset();
                    }
                    matrixStack.translate(offset.x(), offset.y(), offset.z());
                }
            }
        }
    }
}
