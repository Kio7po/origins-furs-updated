package com.kio7po.originsfurs.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public interface IMojangModelPart {
    default Vector3d originsFurs$getPosition() {return new Vector3d();}
    default Vector3d originsFurs$getRotation() {return new Vector3d();}
    //default Vector3d originfurs$getScale() {return new Vector3d();}
    //default ModelPart originfurs$getHolderPart() {return null;}
}