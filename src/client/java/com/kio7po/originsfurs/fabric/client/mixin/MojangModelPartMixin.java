package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.IMojangModelPart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(ModelPart.class)
public abstract class MojangModelPartMixin implements IMojangModelPart {
    // Acceso al method de la case ModelPart
    @Shadow
    public abstract ModelTransform getTransform();

    // Permite obtener la posición de una parte del modelo vanilla
    @Override
    public Vector3d originsFurs$getPosition() {
        ModelTransform t = getTransform();
        return new Vector3d(t.pivotX, t.pivotY, t.pivotZ).negate();
    }

    // Permite obtener la rotación de una parte del modelo vanilla
    @Override
    public Vector3d originsFurs$getRotation() {
        ModelTransform t = getTransform();
        return new Vector3d(t.pitch, t.yaw, t.roll);
    }

    // Scale no se usa ahora mismo
    /*
    @Override
    public Vector3d originfurs$getScale() {
        return new Vector3d(xScale, yScale, zScale);
    }
    */
}
