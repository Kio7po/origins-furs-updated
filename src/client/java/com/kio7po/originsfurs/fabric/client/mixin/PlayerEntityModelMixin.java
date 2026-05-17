package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntity;
import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PlayerEntityModel.class, priority = 100)
public abstract class PlayerEntityModelMixin implements IPlayerEntity, IPlayerEntityModel {
    // Acceso al atributo thinArms de PlayerEntityModel
    @Shadow
    @Final
    private boolean thinArms;

    // Allows you to call the method isSlim to access the private attribute
    // of PlayerEntityModel
    // mod$method syntax is forced when adding new methods to the class
    @Override
    public boolean originsfurs$isSlim() {
        return thinArms;
    }
}