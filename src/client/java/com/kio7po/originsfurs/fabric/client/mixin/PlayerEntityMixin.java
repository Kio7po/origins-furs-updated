package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.OriginsFursClient;
import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntity;
import com.kio7po.originsfurs.fabric.client.model.OriginFur;
import com.kio7po.originsfurs.fabric.client.registry.OriginFurRegistry;
//import com.kio7po.originsfurs.fabric.client.AbstractClientPlayerEntityCompatMixins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements IPlayerEntity {
    /**
     * Loop through all the player's Origins by layer
     * For each one, checks to see if it has a Fur assigned in the registry
     * @return A new list with all the player's Furs
     */
    @Override
    public List<OriginFur> originsfurs$getCurrentFurs() {
        Collection<Origin> origins = ModComponents.ORIGIN.get(this).getOrigins().values();
        List<OriginFur> furs = new ArrayList<>(origins.size());
        origins.forEach(origin -> {
            if (origin != Origin.EMPTY) {
                Identifier id = origin.getId();
                if (!OriginFurRegistry.containsFur(id)) {
                    OriginsFursClient.LOGGER.warn("[Origins Furs] Fur was null for origin: " + id + ". This should NEVER happen!");
                    System.out.println(OriginFurRegistry.getOriginsIds());
                } else {
                    furs.add(OriginFurRegistry.getFur(id));
                }
            }
        });
        return furs;
    }
}
