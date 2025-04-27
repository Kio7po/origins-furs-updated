package com.kio7po.originsfurs.fabric.client;


import io.github.apace100.origins.origin.Origin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface IPlayerEntityMixins {
    // BetterCombat
    /*default boolean betterCombat$isSwinging() {return false;}
    void betterCombat$setSwinging(boolean value);*/

    // OriginsFurs
    default boolean originsFurs$isPlayerInvisible() {return false;}
    default List<Origin> originsFurs$currentOrigins() {
        return List.of(Origin.EMPTY);
    }
    default List<OriginFur> originsFurs$getCurrentFurs() {
        return new ArrayList<>();
    }
    default List<OriginFurModel> originsFurs$getCurrentModels() {
        List<OriginFurModel> models = new ArrayList<>();
        originsFurs$getCurrentFurs().forEach(
                fur -> models.add(fur.getGeoModel())
        );
        return models;
    }
}
