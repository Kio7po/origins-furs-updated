package com.kio7po.originsfurs.fabric.client.feature;

import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntity;
import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntityModel;
import com.kio7po.originsfurs.fabric.client.model.OriginFur;
import com.kio7po.originsfurs.fabric.client.model.OriginFurAnimatable;
import com.kio7po.originsfurs.fabric.client.model.OriginFurModel;
import com.kio7po.originsfurs.fabric.client.util.Alib;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;

@Environment(EnvType.CLIENT)
public class FurFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private GeoBone head;
    private GeoBone body;
    private GeoBone rightArm;
    private GeoBone leftArm;
    private GeoBone rightLeg;
    private GeoBone leftLeg;


    public FurFeatureRenderer(PlayerEntityRenderer renderer) {
        super(renderer);
    }

    private void grabRelevantBones(OriginFurModel model) {
        this.head = model.getCachedGeoBone("bipedHead");
        this.body = model.getCachedGeoBone("bipedBody");
        this.rightArm = model.getCachedGeoBone("bipedRightArm");
        this.leftArm = model.getCachedGeoBone("bipedLeftArm");
        this.rightLeg = model.getCachedGeoBone("bipedRightLeg");
        this.leftLeg = model.getCachedGeoBone("bipedLeftLeg");
    }

    private void copyScale(ModelPart from, GeoBone to) {
        to.updateScale(from.xScale, from.yScale, from.zScale);
    }

    // Transformations to match a GeoBone with a ModelPart. Copied from GeoArmorRender
    private void applyBaseTransformations(BipedEntityModel<?> baseModel) {
        if (this.head != null) {
            ModelPart headPart = baseModel.head;

            RenderUtil.matchModelPartRot(headPart, this.head);
            this.head.updatePosition(headPart.pivotX, -headPart.pivotY, headPart.pivotZ);
            copyScale(headPart, this.head);
        }

        if (this.body != null) {
            ModelPart bodyPart = baseModel.body;

            RenderUtil.matchModelPartRot(bodyPart, this.body);
            this.body.updatePosition(bodyPart.pivotX, -bodyPart.pivotY, bodyPart.pivotZ);
            copyScale(bodyPart, this.body);
        }

        if (this.rightArm != null) {
            ModelPart rightArmPart = baseModel.rightArm;

            RenderUtil.matchModelPartRot(rightArmPart, this.rightArm);
            this.rightArm.updatePosition(rightArmPart.pivotX + 5, 2 - rightArmPart.pivotY, rightArmPart.pivotZ);
            copyScale(rightArmPart, this.rightArm);
        }

        if (this.leftArm != null) {
            ModelPart leftArmPart = baseModel.leftArm;

            RenderUtil.matchModelPartRot(leftArmPart, this.leftArm);
            this.leftArm.updatePosition(leftArmPart.pivotX - 5, 2 - leftArmPart.pivotY, leftArmPart.pivotZ);
            copyScale(leftArmPart, this.leftArm);
        }

        if (this.rightLeg != null) {
            ModelPart rightLegPart = baseModel.rightLeg;

            RenderUtil.matchModelPartRot(rightLegPart, this.rightLeg);
            this.rightLeg.updatePosition(rightLegPart.pivotX + 2, 12 - rightLegPart.pivotY, rightLegPart.pivotZ-0.35f);
            copyScale(rightLegPart, this.rightLeg);
        }

        if (this.leftLeg != null) {
            ModelPart leftLegPart = baseModel.leftLeg;

            RenderUtil.matchModelPartRot(leftLegPart, this.leftLeg);
            this.leftLeg.updatePosition(leftLegPart.pivotX - 2, 12 - leftLegPart.pivotY, leftLegPart.pivotZ-0.7f);
            copyScale(leftLegPart, this.leftLeg);
        }
    }

//    public static void matchModelPartRot(ModelPart from, GeoBone to) {
//        to.updateRotation((-from.pitch), (-from.yaw), (from.roll));
//    }

    @Override
    public void render(
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int light,
            AbstractClientPlayerEntity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        // todo: Revisar esto, lo de hacer loop en cada frame como que no mola
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean invisible = entity.isInvisible();
        boolean revealed = invisible && !entity.isInvisibleTo(ClientUtil.getClientPlayer());
        boolean outlined = mc.hasOutline(entity);

        if (!invisible || revealed || outlined) {
            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = getContextModel();
            IPlayerEntityModel accessor = (IPlayerEntityModel) playerEntityModel; // mixin makes it work
            int argb = Alib.getRenderColor(entity, revealed);

            //float partialTick = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
            for (OriginFur fur : ((IPlayerEntity)entity).originsfurs$getCurrentFurs()) {
                OriginFurAnimatable animatable = fur.getAnimatable();
                OriginFurModel model = fur.getGeoModel();

                // Set the color based on Origins color powers
                fur.setRenderColor(argb);

                // Iterates the bones deciding which ones should be visible
                model.preprocess(accessor, entity);
                fur.setAnimatablePlayer(entity);

                // Set the bones positions
                grabRelevantBones(model);
                applyBaseTransformations(playerEntityModel);

                matrixStack.push();
//                    // Rotate the model (is upside down for some reason)
//                    matrixStack.multiply(new Quaternionf().rotateZ(180 * MathHelper.RADIANS_PER_DEGREE));
//
//                    // Baja el modelo (por alguna razón está muy arriba)
//                    matrixStack.translate(0, -1.51, 0);
//                    // Ajustar más el modelo
//                    matrixStack.translate(-0.5, -0.5, -0.5);

                RenderLayer renderLayer = Alib.getRenderLayer(model.getTextureResource(animatable), !invisible, revealed, outlined, false);
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);

                fur.render(matrixStack, animatable, vertexConsumerProvider, renderLayer, vertexConsumer, light, tickDelta);

                matrixStack.pop();
            }
        }
    }
}
