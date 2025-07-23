package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntity;
import com.kio7po.originsfurs.fabric.client.model.OriginFurModel;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @ModifyReturnValue(method = "getSkinTextures", at=@At("RETURN"))
    private SkinTextures changeElytraTexture(SkinTextures skinTextures) {
        Identifier elytraTextureId = skinTextures.elytraTexture();
        for (OriginFurModel model : ((IPlayerEntity)this).originsfurs$getCurrentModels()) {
            if (model != null && model.hasCustomElytraTexture()) {
                elytraTextureId = model.getElytraTexture();
                break;
            }
        }
        return new SkinTextures(
                skinTextures.texture(),
                skinTextures.textureUrl(),
                skinTextures.capeTexture(),
                elytraTextureId,
                skinTextures.model(),
                skinTextures.secure()
        );
    }
}