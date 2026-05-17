package com.kio7po.originsfurs.fabric.client.mixin;

import static com.kio7po.originsfurs.fabric.client.OriginsFursClient.LOGGER;
import com.kio7po.originsfurs.fabric.client.registry.OriginFurRegistry;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = OriginManager.class)
public abstract class OriginManagerMixin {
    @Shadow(remap = false) @Final private static Object2ObjectOpenHashMap<Identifier, Origin> ORIGINS_BY_ID;

    @Inject(method = "endBuilding()V", at = @At(value = "TAIL"), remap = false)
    private static void onEndBuilding(CallbackInfo ci) {
        LOGGER.info("[Origins Furs] Received origins list.");
        OriginFurRegistry.loadFursFromOrigins(ORIGINS_BY_ID);
    }
}
