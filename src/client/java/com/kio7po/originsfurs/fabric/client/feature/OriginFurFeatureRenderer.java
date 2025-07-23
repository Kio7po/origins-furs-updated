//package com.kio7po.originsfurs.fabric.client;
//
//import io.github.apace100.apoli.component.PowerHolderComponent;
//import io.github.apace100.apoli.power.type.ModelColorPowerType;
//import io.github.apace100.origins.origin.Origin;
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.network.AbstractClientPlayerEntity;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.PlayerEntityRenderer;
//import net.minecraft.client.render.entity.feature.FeatureRenderer;
//import net.minecraft.client.render.entity.feature.FeatureRendererContext;
//import net.minecraft.client.render.entity.model.PlayerEntityModel;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.util.math.ColorHelper;
//import net.minecraft.util.math.MathHelper;
//import org.joml.Quaternionf;
//import org.joml.Vector3d;
//
//import java.util.List;
//
//// FeatureRender is called RenderLayer with normal mappings
//@Environment(EnvType.CLIENT)
//public class OriginFurFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
//
//    public OriginFurFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
//        super(context);
//    }
//
//    @Override
//    public void render(
//            MatrixStack matrixStack,
//            VertexConsumerProvider vertexConsumerProvider,
//            int light,
//            AbstractClientPlayerEntity playerEntity,
//            float limbAngle,
//            float limbDistance,
//            float tickDelta,
//            float animationProgress,
//            float headYaw,
//            float headPitch
//    ) {
//            // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
//        if (!playerEntity.isInvisible() /*&& !playerEntity.isSpectator()*/) {
//            IPlayerEntityMixins iPlayerEntity = (IPlayerEntityMixins) playerEntity; // mixin makes this work
//            for (OriginFur fur : iPlayerEntity.originsFurs$getCurrentFurs()) {
//                if (fur != null && fur.origin != null) {
//                    PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(playerEntity);
//                    IPlayerEntityMixins entityRenderMixin = (IPlayerEntityMixins) entityRenderer;
//                    ModelRootAccessor accessor = (ModelRootAccessor)entityRenderer.getModel(); // mixin makes it work
//                    OriginFurAnimatable animatable = fur.getAnimatable();
//                    OriginFurModel model = fur.getGeoModel();
//                    Origin origin = fur.origin;
//
//                    int argb = ColorHelper.Argb.fromFloats (1,1, 1, 1);
//                    List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(playerEntity, ModelColorPowerType.class);
//                    if (!modelColorPowers.isEmpty()) {
//                        argb = Alib.getArgbFromColorPowers(argb, modelColorPowers);
//                    }
//                    fur.setRenderColor(argb);
//
////                        model.getAnimationProcessor().getRegisteredBones().forEach(coreGeoBone -> {
////                             algo de IGeoBone e isHiddenByDefault
////                            model.preprocess(origin, entityRenderer, entityRenderMixin, accessor, playerEntity);
////                        });
//
//                    // Recorre los huesos decidiendo si deben ser visibles o no
//                    model.preprocess(accessor, playerEntity);
//
//                    fur.setAnimatablePlayer(playerEntity);
//                    matrixStack.push();
//
//                    // Le da la vuelta al modelo (por alguna razón está del revés)
//                    matrixStack.multiply(new Quaternionf().rotateX(180 * MathHelper.RADIANS_PER_DEGREE));
//                    //matrixStack.multiply(new Quaternionf().rotateZ(180 * MathHelper.RADIANS_PER_DEGREE));
//
//                    // Baja el modelo (por alguna razón está muy arriba)
//                    matrixStack.translate(0, -1.51f, 0);
//                    // Ajustar más el modelo
//                    matrixStack.translate(-0.5, -0.5, -0.5);
//
//                    // Resetea la posición de los huesos no es necesario, creo
//
//                    IMojangModelPart head = (IMojangModelPart) (Object) entityRenderer.getModel().head; // mixin
//                    IMojangModelPart body = (IMojangModelPart) (Object) entityRenderer.getModel().body;
//                    IMojangModelPart rightArm = (IMojangModelPart) (Object) entityRenderer.getModel().rightArm;
//                    IMojangModelPart leftArm = (IMojangModelPart) (Object) entityRenderer.getModel().leftArm;
//                    IMojangModelPart rightLeg = (IMojangModelPart) (Object) entityRenderer.getModel().rightLeg;
//                    IMojangModelPart leftLeg = (IMojangModelPart) (Object) entityRenderer.getModel().leftLeg;
//
////                        model.translatePositionForBone("bipedHead", head.originsFurs$getPosition());
////                        model.translatePositionForBone("bipedBody", body.originsFurs$getPosition());
////                        model.translatePositionForBone("bipedRightArm", rightArm.originsFurs$getPosition());
////                        model.translatePositionForBone("bipedLeftArm", leftArm.originsFurs$getPosition());
////                        model.translatePositionForBone("bipedRightLeg", leftLeg.originsFurs$getPosition());
////                        model.translatePositionForBone("bipedLeftLeg", rightLeg.originsFurs$getPosition());
//
//                    model.setPositionForBone("bipedHead", head.originsFurs$getPosition());
//                    model.setPositionForBone("bipedBody", body.originsFurs$getPosition());
//                    model.setPositionForBone("bipedRightArm", rightArm.originsFurs$getPosition());
//                    model.setPositionForBone("bipedLeftArm", leftArm.originsFurs$getPosition());
//                    model.setPositionForBone("bipedRightLeg", leftLeg.originsFurs$getPosition());
//                    model.setPositionForBone("bipedLeftLeg", rightLeg.originsFurs$getPosition());
//
//                    // Ajustes a la posición de las extremidades (por alguna razón están muy separadas)
//                    model.translatePositionForBone("bipedRightArm", new Vector3d(-5, 2, 0));
//                    model.translatePositionForBone("bipedLeftArm", new Vector3d(5, 2, 0));
//                    model.translatePositionForBone("bipedRightLeg", new Vector3d(2, 12, 0));
//                    model.translatePositionForBone("bipedLeftLeg", new Vector3d(-2, 12, 0));
//
//                    model.setRotationForBone("bipedHead", head.originsFurs$getRotation());
//                    model.setRotationForBone("bipedBody", body.originsFurs$getRotation());
//                    model.setRotationForBone("bipedRightArm", rightArm.originsFurs$getRotation());
//                    model.setRotationForBone("bipedLeftArm", leftArm.originsFurs$getRotation());
//                    model.setRotationForBone("bipedLeftLeg", rightLeg.originsFurs$getRotation());
//                    model.setRotationForBone("bipedRightLeg", leftLeg.originsFurs$getRotation());
//
//                    // Invertir la rotación de las partes (está en espejo por invertirlo al principio)
//                    model.invertRotForPart("bipedHead", false, true, true);
//                    model.invertRotForPart("bipedBody", false, true, false);
//                    model.invertRotForPart("bipedRightArm", false, true, true);
//                    model.invertRotForPart("bipedLeftArm", false, true, true);
//                    model.invertRotForPart("bipedRightLeg", false, true, true);
//                    model.invertRotForPart("bipedLeftLeg", false, true, true);
//
//                    float partialTick = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
//
//                    // getTextureResource no hace nada con el animatable, se le pasa simplemente por la firma
//                    fur.render(matrixStack, animatable, vertexConsumerProvider, RenderLayer.getEntityTranslucent(model.getTextureResource(animatable)), null, light, partialTick);
//                    fur.render(matrixStack, animatable, vertexConsumerProvider, RenderLayer.getEntityTranslucentEmissive(model.getFullbrightTextureResource(animatable)), null, Integer.MAX_VALUE - 1, partialTick);
//                    matrixStack.pop();
//                }
//            }
//        }
//    }
//}
