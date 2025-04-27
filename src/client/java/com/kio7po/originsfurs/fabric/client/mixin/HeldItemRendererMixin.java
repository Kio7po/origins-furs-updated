package com.kio7po.originsfurs.fabric.client.mixin;

//@Mixin(HeldItemRenderer.class)
//public class HeldItemRendererMixin {

    //@Unique
    //Vec3d currentOffset;

    // el renderItem que llama a renderFirstPersonItem, el primero
    // Esta mueve entera la vista, no sirve
    /*@Inject(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.BEFORE)
    )
    private void adjustItemPosition(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
        //if (renderMode.isFirstPerson()) {
            //matrices.push();
            if (player instanceof IPlayerEntityMixins playerMixin) {
                List<OriginFurModel> models = playerMixin.originsFurs$getCurrentModels();
                if (!models.isEmpty()) {
                    // todo: Loop all models and use the biggest offset
                    OriginFurModel furModel = models.getFirst();
                    if (furModel != null) {
                        Vec3d offset = Vec3d.ZERO;
                        switch (player.getMainArm()) {
                            case RIGHT -> offset = furModel.getRightOffset();
                            case LEFT -> offset = furModel.getLeftOffset();
                        }
                        currentOffset = offset;
                        matrices.translate(offset.getX(), offset.getY(), offset.getZ());
                    }
                }
            }
        //}
    }

    @Inject(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("TAIL")
    )
    private void restoreItemPosition(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
        //matrices.pop();
    }*/

    // Este no funciona debido a HoldMyItems
    /*@Inject(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE // Aplica tu cambio ANTES de renderizar el item
            )
    )
    private void adjustItemPosition(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (player instanceof IPlayerEntityMixins playerMixin) {
            List<OriginFurModel> models = playerMixin.originsFurs$getCurrentModels();
            if (!models.isEmpty()) {
                // todo: Loop all models and use the biggest offset
                OriginFurModel furModel = models.getFirst();
                if (furModel != null) {
                    Vec3d offset = Vec3d.ZERO;
                    switch (player.getMainArm()) {
                        case RIGHT -> {
                            switch (hand) {
                                case MAIN_HAND -> offset = furModel.getRightOffset();
                                case OFF_HAND -> offset = furModel.getLeftOffset();
                            }
                        }
                        case LEFT -> {
                            switch (hand) {
                                case MAIN_HAND -> offset = furModel.getLeftOffset();
                                case OFF_HAND -> offset = furModel.getRightOffset();
                            }
                        }
                    }
                    matrices.translate(offset.getX(), offset.getY(), offset.getZ());
                }
            }
        }
    }*/

    // Esta parece ser la buena, pero lamentablemente mueve el item en función del item
    /*@Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void adjustItemPosition(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.push();

        if (entity instanceof PlayerEntity player && entity instanceof IPlayerEntityMixins playerMixin) {


            List<OriginFurModel> models = playerMixin.originsFurs$getCurrentModels();
            if (!models.isEmpty()) {
                // todo: Loop all models and use the biggest offset
                OriginFurModel furModel = models.getFirst();
                if (furModel != null) {
                    Vec3d offset = Vec3d.ZERO;
                    switch (renderMode) {
                        case ModelTransformationMode.FIRST_PERSON_RIGHT_HAND -> offset = furModel.getRightOffset();
                        case ModelTransformationMode.FIRST_PERSON_LEFT_HAND -> offset = furModel.getLeftOffset();
                    }
                    //matrices.translate(offset.getX(), offset.getY(), offset.getZ());
                }
            }
        }
    }

    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void resetItemPosition(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.pop();
    }*/

    // No
    /*
    @Inject(
            method = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V",
            at = @At("HEAD")
    )
    private void fixArmPosition(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (currentOffset != null) {
            //matrices.push();
            //matrices.translate(currentOffset.getX(), currentOffset.getY(), -0.15);
        }
    }

    @Inject(
            method = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V",
            at = @At("RETURN")
    )
    private void resetArmPosition(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (currentOffset != null) {
            //matrices.pop();
        }
    }
    */

//}