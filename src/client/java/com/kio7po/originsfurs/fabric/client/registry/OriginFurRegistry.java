package com.kio7po.originsfurs.fabric.client.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kio7po.originsfurs.fabric.client.model.OriginFur;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import static com.kio7po.originsfurs.fabric.client.OriginsFursClient.LOGGER;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OriginFurRegistry {

    // Furs Map
    private static final LinkedHashMap<Identifier, OriginFur> REGISTRY = new LinkedHashMap<>();
    // Fur JSONs Map
    private static final LinkedHashMap<Identifier, Resource> RESOURCES = new LinkedHashMap<>();

    public static void registerFur(Identifier originId, OriginFur originFur) {
        REGISTRY.put(originId, originFur);
    }

    public static boolean containsFur(Identifier originId) {
        return REGISTRY.containsKey(originId);
    }

    public static OriginFur getFur(Identifier originId) {
        return REGISTRY.get(originId);
    }

    public static Set<Identifier> getOriginsIds() {
        return REGISTRY.keySet();
    }

    public static void registerResource(Identifier originId, Resource resource) {
        RESOURCES.put(originId, resource);
    }

    public static Resource getResource(Identifier originId) {
        return RESOURCES.get(originId);
    }

    public static boolean containsResource(Identifier originId) {
        return RESOURCES.containsKey(originId);
    }

    public static void clear() {
        REGISTRY.clear();
        RESOURCES.clear();
    }

    public static void loadFursFromOrigins(Map<Identifier, Origin> origins) {
        if (origins == null || origins.isEmpty())
            return;

        LOGGER.info("[Origins Furs] Loading furs from origins...");
        origins.forEach((originId, origin) -> {
            // Check if there is a resource for that origin and assign a fur
            if (!containsResource(originId)) {
                // Puts in the registry that this Origin has an empty Fur
                LOGGER.info("[Origins Furs] Loading Empty Fur: " + originId);
                registerFur(originId, OriginFur.EMPTY);
            } else {
                // If there is a resource, read the JSON and register the new Fur
                Resource furJson = getResource(originId);
                try (InputStream inputStream = furJson.getInputStream()) {
                    LOGGER.info("[Origins Furs] Loading Fur: " + originId);
                    String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
                    registerFur(originId, new OriginFur(origin, json));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        LOGGER.info("[Origins Furs] Finished loading furs from origins.");
    }

    public static void loadFursFromOrigins(Set<Map.Entry<Identifier, Origin>> entrySet) {
        Map<Identifier, Origin> origins = entrySet.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        loadFursFromOrigins(origins);
    }
}
