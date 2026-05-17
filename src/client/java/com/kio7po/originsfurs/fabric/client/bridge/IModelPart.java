package com.kio7po.originsfurs.fabric.client.bridge;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public interface IModelPart {
    Vector3d originsFurs$getPosition();
    Vector3d originsFurs$getRotation();
    //default Vector3d originfurs$getScale() {return new Vector3d();}
    //default ModelPart originfurs$getHolderPart() {return null;}
}