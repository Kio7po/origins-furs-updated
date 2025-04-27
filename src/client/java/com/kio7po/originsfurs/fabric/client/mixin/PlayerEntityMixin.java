package com.kio7po.originsfurs.fabric.client.mixin;

import com.kio7po.originsfurs.fabric.client.IPlayerEntityMixins;
import com.kio7po.originsfurs.fabric.client.OriginFur;
import com.kio7po.originsfurs.fabric.client.OriginsFursClient;
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
public class PlayerEntityMixin implements IPlayerEntityMixins {
    /**
     * Loop through all the player's Origins by layer
     * For each one, checks to see if they have a Fur assigned in the registry
     * @return A new list with all the player's Furs
     */
    @Override
    public List<OriginFur> originsFurs$getCurrentFurs() {
        List<Origin> origins = originsFurs$currentOrigins();
        List<OriginFur> furs = new ArrayList<>(origins.size());
        for (Origin origin : origins) {
            Identifier id = origin.getId();
            id = Identifier.of(id.getNamespace(), id.getPath().replace('/', '.').replace('\\', '.'));
            OriginFur fur = OriginsFursClient.FUR_REGISTRY.get(id);
            if (fur == null) {
                Identifier newId = Identifier.of("origins", id.getPath());
                fur = OriginsFursClient.FUR_REGISTRY.get(newId);
                if (fur == null) {
                    // todo: throw?
                    System.out.println("[Origin Furs] Fur was null in entity mixin: " + id + ". This should NEVER happen! Report this to the devs!");
                    System.out.println(OriginsFursClient.FUR_REGISTRY.keySet());
                    continue;
                }
            }
            fur.currentAssociatedOrigin = origin;
            furs.add(fur);
        }
        return furs;
    }

    @Override
    public List<Origin> originsFurs$currentOrigins() {
        Collection<Origin> collection = ModComponents.ORIGIN.get(this).getOrigins().values();;
        return new ArrayList<>(collection);
    }
}
