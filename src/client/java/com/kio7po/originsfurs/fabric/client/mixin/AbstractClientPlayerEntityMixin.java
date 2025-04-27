package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.IPlayerEntityMixins;
import com.kio7po.originsfurs.fabric.client.OriginFurModel;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin implements IPlayerEntityMixins/*, AbstractClientPlayerEntityCompatMixins*/ {
//        @Unique
//        Origin[] orif$origins = new Origin[]{};
//
//        @Override
//        public List<Origin> originsFurs$getOrigins() {
//            return List.of(orif$origins);
//        }
//
//        @Override
//        public void originsFurs$setOrigins(List<Identifier> origins) {
//            // Doesn't make anything???
//        }

    @ModifyReturnValue(method = "getSkinTextures", at=@At("RETURN"))
    private SkinTextures changeElytraTexture(SkinTextures skinTextures) {
        Identifier elytraTextureId = skinTextures.elytraTexture();
        for (OriginFurModel model : this.originsFurs$getCurrentModels()) {
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