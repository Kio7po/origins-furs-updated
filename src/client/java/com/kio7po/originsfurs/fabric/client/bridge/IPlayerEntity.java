package com.kio7po.originsfurs.fabric.client.bridge;


import com.kio7po.originsfurs.fabric.client.model.OriginFur;
import com.kio7po.originsfurs.fabric.client.model.OriginFurModel;
import io.github.apace100.origins.origin.Origin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface IPlayerEntity {
    List<OriginFur> originsfurs$getCurrentFurs();
    default List<OriginFurModel> originsfurs$getCurrentModels() {
        List<OriginFurModel> models = new ArrayList<>();
        originsfurs$getCurrentFurs().forEach(
                fur -> models.add(fur.getGeoModel())
        );
        return models;
    }
}
