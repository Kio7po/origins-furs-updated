package com.kio7po.originsfurs.fabric.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

@Environment(EnvType.CLIENT)
public class OriginFurAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    PlayerEntity player;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    // Registra un controlador que simplemente pone la animación IDLE en bucle
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(
                this,
                "origin_fur",
                animationState -> animationState.setAndContinue(IDLE)
        ));
    }

    public void setPlayer(PlayerEntity player) {this.player = player;}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
    }
}
