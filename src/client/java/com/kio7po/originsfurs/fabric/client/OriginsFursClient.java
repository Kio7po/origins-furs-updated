package com.kio7po.originsfurs.fabric.client;

import com.kio7po.originsfurs.fabric.client.registry.OriginFurRegistry;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

// todo: This uses deprecated stuff
@Environment(EnvType.CLIENT)
public class OriginsFursClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Origins Furs");

    @Override
    public void onInitializeClient() {

        // Listener that reacts to resource reloads (F3+T or ResourcePacks)
        // It loads for the first time when you enter the game
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {

                // Search in /resources/assets/originsfurs/furs (originsfurs:furs/*)
                @Override
                public Identifier getFabricId() {
                    return Identifier.of("originsfurs", "furs");
                }

                @Override
                public void reload(ResourceManager manager) {
                    OriginFurRegistry.clear();

                    // Find all files within assets that end in .json
                    Map<Identifier, Resource> resources = manager.findResources(
                            "furs", identifier -> identifier.getPath().endsWith(".json"));

                    // Iterates through the JSONs of the found furs and puts them into resources
                    for (Identifier resourceId : resources.keySet()) {
                        String path = resourceId.getPath();
                        LOGGER.info("[Origins Furs] Loading resource " + path);
                        // Gets the name of the furJson.json (should match an origin)
                        String[] parts = path.split("/", 3);

                        // Path should be /furs/<mod_name>/<origin_name>.json
                        if (parts.length != 3) {
                            LOGGER.warn("[Origins Furs] Incorrect path for Fur: " + path + ". Should be /furs/<mod_name>/<origin_name>.json. It will be ignored.");
                            continue;
                        }

                        String modname = parts[1];
                        String filename = parts[2].substring(0, parts[2].lastIndexOf('.'));

                        // Get the originId based on the path
                        Identifier originId = Identifier.of(modname, filename);

                        // Save the json
                        OriginFurRegistry.registerResource(originId, resources.get(resourceId));
                        LOGGER.info("[Origins Furs] Registered resource "+resourceId+" with ID "+originId);
                    }

                    // Iterates over the origins and (re)creates their furs
                    // There are no Origins until entering a world,
                    // only effective when reloading resources inside a world
                    // OriginManagerMixin is who load the Furs for the first time
                    OriginFurRegistry.loadFursFromOrigins(OriginManager.entrySet());
                }
            }
        );
    }
}
