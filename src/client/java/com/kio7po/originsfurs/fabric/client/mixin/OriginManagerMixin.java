package com.kio7po.originsfurs.fabric.client.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kio7po.originsfurs.fabric.client.OriginFur;
import com.kio7po.originsfurs.fabric.client.OriginsFursClient;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginsS2CPacket;
import io.github.apace100.origins.origin.OriginManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Environment(EnvType.CLIENT)
@Mixin(value = OriginManager.class)
public class OriginManagerMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Origins Furs");

    // Escuchar a la recepción de orígenes para añadir sus furs
    @Inject(method = "receive", at = @At("TAIL"), remap = false)
    private static void onReceive(SyncOriginsS2CPacket packet, ClientPlayNetworking.Context context, CallbackInfo ci) {
        packet.originsById().forEach((id, origin) -> {
            // Revisa si tiene un recurso asignado
            Resource fur = OriginsFursClient.FUR_RESOURCE.get(id);
            if (fur == null) {
                // Si no lo tiene, lo registra con un Fur vacío
                LOGGER.info("[Origins Furs] Registered empty Fur: " + id);
                OriginsFursClient.FUR_REGISTRY.put(id, OriginFur.EMPTY_FUR);
            } else {
                try (InputStream inputStream = fur.getInputStream()) {
                    LOGGER.info("[Origins Furs] Registered Fur: " + id);
                    String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
                    OriginsFursClient.FUR_REGISTRY.put(id, new OriginFur(json));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load fur", e);
                }
            }
        });
    }
}
