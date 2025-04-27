package com.kio7po.originsfurs.fabric.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import dev.kosmx.playerAnim.api.layered.PlayerAnimationFrame;
//import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// todo: This uses deprecated stuff
@Environment(EnvType.CLIENT)
public class OriginsFursClient implements ClientModInitializer {
    //public static boolean isRenderingInWorld = false;
    // Furs Map
    public static LinkedHashMap<Identifier, OriginFur> FUR_REGISTRY = new LinkedHashMap<>();
    // Fur JSONs Mp
    public static LinkedHashMap<Identifier, Resource> FUR_RESOURCE = new LinkedHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger("Origins Furs");

    @Override
    public void onInitializeClient() {
        // Compatibility with playeranimator? Not needed
        /*if (FabricLoader.getInstance().isModLoaded("playeranimator")) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                Identifier.of("originsfurs", "item_renderer"),
                999,
                ItemRendererFeatureAnim::new
            );
        }*/

        //WorldRenderEvents.END.register(context -> this.isRenderingInWorld = false);
        //WorldRenderEvents.START.register(context -> this.isRenderingInWorld = true);

        //ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {});

        //ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
        //    client.world.getPlayers().forEach(player -> {});
        //});

        // Listener that reacts to resource reloads (F3+T or ResourcePacks)
        // It loads for the first time when you enter the game
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                /* expresión regular (regex) que se usa para identificar rutas de archivos JSON
                en cadenas de texto. Básicamente admite casi cualquier ruta que acabe en .json */
                //final String r_M = "\\/([A-Za-z0-9_.-]+)\\.json";

                @Override
                public Identifier getFabricId() {
                    return Identifier.of("originsfurs", "furs");
                }

                @Override
                public void reload(ResourceManager manager) {
                    // Find all files within assets that end in .json
                    Map<Identifier, Resource> resources = manager.findResources(
                            "furs", identifier -> identifier.getPath().endsWith(".json"));

                    // Iterates through the JSONs of the found furs and puts them into resources
                    for (Identifier identifier : resources.keySet()) {
                        String path = identifier.getPath();
                        LOGGER.info("[Origins Furs] Loading resource " + path);
                        // Gets the name of the fur.json (should match an origin)
                        String itemName = path.substring(path.indexOf('/') + 1, path.lastIndexOf('.'));
                        // Check if it exists
                        Identifier id = Identifier.of("origins", itemName);
                        // Separate the name into two parts by the first dot
                        String[] parts = itemName.split("\\.", 2);
                        // If it has two parts, it means it's from a mod, so search in that mod
                        if (parts.length > 1) {
                            id = Identifier.of(parts[0], parts[1]);
                        }
                        // Replace the '/' and '\' in the path with dots
                        id = Identifier.of(id.getNamespace(), id.getPath().replace('/', '.').replace('\\', '.'));

                        // Deletes what does not originally belong to the mod's namespace
                        if (!identifier.getNamespace().contentEquals("assets/originsfurs")) {
                            OriginsFursClient.FUR_REGISTRY.remove(id);
                            OriginsFursClient.FUR_RESOURCE.remove(id);
                        }

                        // Check if the fur is in the registry
                        if (OriginsFursClient.FUR_REGISTRY.containsKey(id)) {
                            // Gets the model from the registry
                            OriginFurModel model = FUR_REGISTRY.get(id).getGeoModel();

                            // Try with resources that closes the InputStream
                            try (InputStream inputStream = resources.get(identifier).getInputStream()) {
                                // Read the found JSON and assign it to the model (updates it)
                                String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                                JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
                                model.recompile(json);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            // If not in the registry, save the JSON as a resource (???)
                            FUR_RESOURCE.put(id, resources.get(identifier));
                        }
                    }

                    // Check that Origins is loaded (not needed, it's a dependency)
                    //assert FabricLoader.getInstance().isModLoaded("origins");

                    // Iterates over the origins and (re)creates their furs
                    // There are no Origins until entering a world, so
                    // it is only effective when reloading resources inside a world
                    // OriginManagerMixin to load the Furs for the first time
                    OriginManager.entrySet().forEach(entry -> {
                        Identifier id = entry.getKey();
                        Origin origin = entry.getValue();
                        // Check if there is a resource with that id
                        Resource fur = FUR_RESOURCE.get(id);
                        //If there is no resource with that id, try to get it from origins
                        if (fur == null) {
                            id = Identifier.of("origins", id.getPath());
                            fur = FUR_RESOURCE.get(id);
                        }
                        // If there is still no resource for that Origin, it has no Fur, assign an empty one to it
                        if (fur == null) {
                            // Puts in the registry that this Origin has an empty Fur
                            LOGGER.info("[Origins Furs] Reloaded Empty Fur: " + id);
                            FUR_REGISTRY.put(id, OriginFur.EMPTY_FUR);
                        } else {
                            // If there is a resource, read the JSON and assign a new Fur to the registry
                            try (InputStream inputStream = fur.getInputStream()) {
                                LOGGER.info("[Origins Furs] Reloaded Fur: " + id);
                                String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                                JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
                                FUR_REGISTRY.put(id, new OriginFur(json));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        );
    }

    /*public static class ItemRendererFeatureAnim extends PlayerAnimationFrame {
        PlayerEntity player;
        private int time = 0;

        ItemRendererFeatureAnim(PlayerEntity player) {
            this.player = player;
        }

        @Override
        public void tick() {
            this.time++;
        }

        @Override
        // Doesn't actually do anything at all
        public void setupAnim(float v) {
            if (this.player instanceof ClientPlayerEntity clientPlayerEntity && this.player instanceof IPlayerEntityMixins iPlayerEntity) {
                iPlayerEntity.originsFurs$getCurrentModels().forEach(model -> {
                    if (model == null) {
                        return;
                    }
                    Vector3d leftPart = model.getLeftOffset();
                    Vector3d rightPart = model.getRightOffset();
                });
            }
        }
    }*/
}
