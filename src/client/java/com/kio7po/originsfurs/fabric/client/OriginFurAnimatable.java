package com.kio7po.originsfurs.fabric.client;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

// todo: Most things used here are deprecated, must update
@Environment(EnvType.CLIENT)
public class OriginFurAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    PlayerEntity player;

    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController[]{
                new AnimationController(
                        this,
                        "origin_fur",
                        animationState -> {
                            animationState.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
                            return PlayState.CONTINUE;
                        }
                )
        });
    }

    public void setPlayer(PlayerEntity player) {this.player = player;}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        // used to be MinecraftClient.getInstance().getTickDelta();
        return MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
    }
}
