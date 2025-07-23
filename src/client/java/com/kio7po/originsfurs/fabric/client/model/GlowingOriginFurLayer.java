package com.kio7po.originsfurs.fabric.client.model;

import com.kio7po.originsfurs.fabric.client.util.Alib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.ClientUtil;

public class GlowingOriginFurLayer extends AutoGlowingGeoLayer<OriginFurAnimatable> {
    public GlowingOriginFurLayer(OriginFur renderer) {
        super(renderer);
    }

    @Override
    protected RenderLayer getRenderType(OriginFurAnimatable animatable, @Nullable VertexConsumerProvider bufferSource) {
        Identifier glowmask = ((OriginFurModel)getRenderer().getGeoModel()).getTextureGlowmaskResource();

        PlayerEntity player = animatable.player;
        boolean invisible = player.isInvisible();
        boolean revealed = invisible && !player.isInvisibleTo(ClientUtil.getClientPlayer());
        boolean outlined = MinecraftClient.getInstance().hasOutline(player);

        return Alib.getRenderLayer(glowmask, !invisible, revealed, outlined, true);
    }
}
