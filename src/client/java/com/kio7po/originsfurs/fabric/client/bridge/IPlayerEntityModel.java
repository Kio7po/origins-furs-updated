package com.kio7po.originsfurs.fabric.client.bridge;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IPlayerEntityModel {
    default boolean originsfurs$isSlim() {
        return false;
    }
}
