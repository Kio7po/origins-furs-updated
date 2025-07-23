package com.kio7po.originsfurs.fabric.client.mixin;

import static com.kio7po.originsfurs.fabric.client.OriginsFursClient.LOGGER;
import com.kio7po.originsfurs.fabric.client.registry.OriginFurRegistry;

import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginsS2CPacket;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(value = OriginManager.class)
public abstract class OriginManagerMixin {
    @Shadow public static Set<Map.Entry<Identifier, Origin>> entrySet() {
        return null;
    }

    // Escuchar a la recepción de orígenes para añadir sus furs
//    @Inject(method = "receive", at = @At("TAIL"), remap = false)
//    private static void onReceive(SyncOriginsS2CPacket packet, ClientPlayNetworking.Context context, CallbackInfo ci) {
//        Map<Identifier, Origin> origins = packet.originsById();
//        LOGGER.info("Received SyncOriginsS2CPacket with size "+origins.size());
//        OriginFurRegistry.loadFursFromOrigins(origins);
//    }

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/origin/OriginManager;endBuilding()V"), remap = false)
    private static void onApply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler, CallbackInfo ci) {
        LOGGER.info("[Origins Furs] Loading furs from origins...");
        if (entrySet()!=null) OriginFurRegistry.loadFursFromOrigins(entrySet());
        LOGGER.info("[Origins Furs] Finished loading furs from origins.");
    }
}
