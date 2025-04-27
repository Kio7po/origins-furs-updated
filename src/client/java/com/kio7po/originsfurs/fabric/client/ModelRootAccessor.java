package com.kio7po.originsfurs.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;

@Environment(EnvType.CLIENT)
public interface ModelRootAccessor {
//    void originsFurs$setProcessedSlim(boolean state);
//    default boolean originsFurs$hasProcessedSlim() {
//        return true;
//    }
//    ModelPart originsFurs$getModelRoot();
    default boolean originsFurs$isSlim() {
        return false;
    }
//    default boolean originsFurs$justUsedElytra() {
//        return false;
//    }
//    void originsFurs$setJustUsedElytra(boolean b);
//    default float originsFurs$elytraPitch() {
//        return 0.0F;
//    }
//    void originsFurs$setElytraPitch(float b);
}
