package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.IPlayerEntityMixins;
import com.kio7po.originsfurs.fabric.client.ModelRootAccessor;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PlayerEntityModel.class, priority = 100)
public abstract class PlayerEntityModelMixin implements IPlayerEntityMixins, ModelRootAccessor {
    // Acceso al atributo thinArms de PlayerEntityModel
    @Shadow
    @Final
    private boolean thinArms;
    //@Unique ModelPart root;

        /*
        elytra stuff not needed
        */

//        @Inject(method = "<init>", at = @At("TAIL"))
//        private void onInit(ModelPart root, boolean thinArms, CallbackInfo ci) {
//            this.root = root;
//        }

        /*
        proccessed slim stuff not needed
         */

    // Allows you to call the method isSlim to access the private attribute
    // of PlayerEntityModel
    @Override
    public boolean originsFurs$isSlim() {
        return thinArms;
    }
}