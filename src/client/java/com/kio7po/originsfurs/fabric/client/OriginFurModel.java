package com.kio7po.originsfurs.fabric.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.origins.origin.Origin;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import mod.azure.azurelib.common.api.client.model.GeoModel;
import mod.azure.azurelib.common.internal.common.cache.object.GeoBone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

// todo: update deprecated stuff
// todo: desarrollar segun sea necesario
@Environment(EnvType.CLIENT)
public class OriginFurModel extends GeoModel<OriginFurAnimatable> {
    public static final OriginFurModel EMPTY_MODEL = new OriginFurModel(new JsonObject());
    public static final Vector3d VECTOR3D_ZERO = new Vector3d(0, 0, 0);
    public static final Vector3f VECTOR3F_ZERO = new Vector3f(0, 0, 0);

    //public PlayerEntity player;
    public JsonObject json;
    public EnumSet<OriginFurModel.VMP> hiddenParts = EnumSet.noneOf(OriginFurModel.VMP.class);
    //private boolean dirty = false;

    // Pilas de transformaciones 3D usadas para manejar jerarquías de animación/modelado
    //protected final LinkedHashMap<String, Stack<Vector3d>> posStack = new LinkedHashMap<>();
    //protected final LinkedHashMap<String, Stack<Vector3d>> rotStack = new LinkedHashMap<>();
    //protected final LinkedHashMap<String, Stack<Vector3d>> sclStack = new LinkedHashMap<>();

    public Long2ReferenceLinkedOpenHashMap<GeoBone> boneCache = new Long2ReferenceLinkedOpenHashMap<>();
    //public List<ResourceOverride> overrides = new ArrayList<>();
    //Identifier mRLast = null; //???
    //OriginFurModel.ResourceOverride currentOverride = new OriginFurModel.ResourceOverride();

    public OriginFurModel(JsonObject json) {
        this.recompile(json);
    }

    // Limpia y da valor a todos los parámetros en base al JSON
    public void recompile(JsonObject json) {
        this.json = json;
        this.parseHiddenParts();
        this.boneCache.clear();
        // Obtiene todos los overrides del JSON, los combierte e inserta en overrides
        // y los ordena en función de su peso
//        if (this.json.has("overrides") && this.json.get("overrides").isJsonArray()) {
//            JsonHelper.getArray(this.json, "overrides").forEach(override -> {
//                ResourceOverride resourceOverride = ResourceOverride.deserialize(override.getAsJsonObject());
//                this.overrides.add(resourceOverride);
//            });
//            this.overrides.sort((o1, o2) -> Float.compare(o1.weight, o2.weight));
//        }
    }

    /**
     * Da valor a hiddenParts en función del JSON actual,
     * indicando las partes del modelo origina que no deben verse,
     * como en el caso de los brazos del Blaze
     */
    private void parseHiddenParts() {
        EnumSet<OriginFurModel.VMP> set = EnumSet.noneOf(OriginFurModel.VMP.class);
        if(this.json.has("hidden")) {
            JsonArray hiddenParts = this.json.getAsJsonArray("hidden");
            hiddenParts.forEach(hiddenPart -> {
                switch (hiddenPart.getAsString().toLowerCase()) {
                    case "rightsleeve": set.add(VMP.RIGHT_SLEEVE); break;
                    case "leftsleeve": set.add(VMP.LEFT_SLEEVE); break;
                    case "rightarm": set.add(VMP.RIGHT_ARM); break;
                    case "leftarm": set.add(VMP.LEFT_ARM); break;
                    case "rightpants": set.add(VMP.RIGHT_PANTS); break;
                    case "leftpants": set.add(VMP.LEFT_PANTS); break;
                    case "rightleg": set.add(VMP.RIGHT_LEG); break;
                    case "leftleg": set.add(VMP.LEFT_LEG); break;
                    case "hat": set.add(VMP.HAT); break;
                    case "head": set.add(VMP.HEAD); break;
                    case "jacket": set.add(VMP.JACKET); break;
                    case "body": set.add(VMP.BODY); break;
                }
            });
        }

        this.hiddenParts = set;
    }

    // no usage?
    // Vector en precisión simple
//    public Vector3f getPositionFromBone(String identifier) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null) {
//            return new Vector3f(0, 0, 0);
//        } else {
//            Vector3d pos = bone.getLocalPosition();
//            return new Vector3f((float)pos.x, (float)pos.y, (float)pos.z);
//        }
//    }

    // no usage
//    public Vector3d getPositionFromBoneD(String identifier) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null)
//            return new Vector3d();
//        else {
//            Vector3d pos = bone.getLocalPosition();
//            return new Vector3d(pos.x, pos.y, pos.z);
//        }
//    }

    public final GeoBone getCachedGeoBone(String identifier) {
        long hash = Alib.getHash64(identifier);
        if (boneCache.containsKey(hash)) {
            return boneCache.get(hash);
        } else {
            GeoBone bone = this.getBone(identifier).orElse(null);
            if (bone != null) {
                this.boneCache.putAndMoveToFirst(hash, bone);
                return bone;
            } else return null;
        }
    }

    private Vector3f vector3dToVector3f(Vector3d vec3d) {
        return new Vector3f((float)vec3d.x(), (float)vec3d.y(), (float)vec3d.z());
    }

    public GeoBone setPositionForBone(String identifier, Vector3d pos) {
        return setPositionForBone(identifier, vector3dToVector3f(pos));
    }

    public GeoBone setPositionForBone(String identifier, Vector3f pos) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone == null) {
            return null;
        } else {
            bone.setPosX(pos.x());
            bone.setPosY(pos.y());
            bone.setPosZ(pos.z());
            return bone;
        }
    }

    // No ussage
    public GeoBone setRotationForBone(String identifier, Vector3d rot) {
        return setRotationForBone(identifier, vector3dToVector3f(rot));
    }
    public final GeoBone setRotationForBone(String identifier, Vector3f rot) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone == null) {
            return null;
        } else {
            bone.setRotX(rot.x());
            bone.setRotY(rot.y());
            bone.setRotZ(rot.z());
            return bone;
        }
    }

    public GeoBone translatePositionForBone(String identifier, Vector3d pos) {
        return translatePositionForBone(identifier, vector3dToVector3f(pos));
    }

    public final GeoBone translatePositionForBone(String identifier, Vector3f pos) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone == null) {
            return null;
        }
        Vector3d newPos = new Vector3d(pos.x() + bone.getPosX(), pos.y() + bone.getPosY(),pos.z() + bone.getPosZ());
        return this.setPositionForBone(identifier, newPos);
    }

    public GeoBone setModelPositionForBone(String identifier, Vector3d pos) {
        return setModelPositionForBone(identifier, vector3dToVector3f(pos));
    }

    public final GeoBone setModelPositionForBone(String identifier, Vector3f pos) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone == null) {
            return null;
        }
        bone.setModelPosition(new Vector3d(pos.x(), pos.y(), pos.z()));
        return bone;
    }

    public final GeoBone setScaleForBone(String identifier, Vector3d scale) {
        return setScaleForBone(identifier, vector3dToVector3f(scale));
    }

    public final GeoBone setScaleForBone(String identifier, Vector3f scale) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone == null) {
            return null;
        }
        bone.setScaleX(scale.x());
        bone.setScaleY(scale.y());
        bone.setScaleZ(scale.z());
        return bone;
    }

    public final GeoBone invertRotForPart(String identifier, boolean x, boolean y, boolean z) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone == null) {
            return null;
        }
        Vector3d rot = bone.getRotationVector().mul(x ? -1 : 1, y ? -1 : 1, z ? -1 : 1);
        bone.setRotX((float)rot.x());
        bone.setRotY((float) rot.y());
        bone.setRotZ((float) rot.z());
        return bone;
    }

    public GeoBone setWorldPositionForBone(String identifier, Vector3d pos) {
        return setWorldPositionForBone(identifier, vector3dToVector3f(pos));
    }

    public GeoBone setWorldPositionForBone(String identifier, Vector3f pos) {
        GeoBone bone = this.getCachedGeoBone(identifier);
        if (bone != null) {
            return null;
        } else {
            Vector4f v = bone.getWorldSpaceMatrix().transform(new Vector4f(pos.x(), pos.y(), pos.z(), 1.0f));
            bone.setPosX(v.x());
            bone.setPosY(v.y());
            bone.setPosZ(v.z());
            return bone;
        }
    }

    public final GeoBone resetBone(String name) {
        setPositionForBone(name, VECTOR3F_ZERO);
        setRotationForBone(name, VECTOR3F_ZERO);
        setModelPositionForBone(name, VECTOR3F_ZERO);
        return setScaleForBone(name, new Vector3d(1,1,1));
    }

    @Override
    public Identifier getModelResource(OriginFurAnimatable animatable) {
        return dMR(this.json);
    }

    @Override
    public Identifier getTextureResource(OriginFurAnimatable animatable) {
        return dTR(this.json);
    }

    public Identifier getFullbrightTextureResource(OriginFurAnimatable animatable) {
        String jsonContent = JsonHelper.getString(json, "fullbrightTexture", "originsfurs:textures/missing.png");
        return Identifier.tryParse(jsonContent);
    }

    @Override
    public Identifier getAnimationResource(OriginFurAnimatable animatable) {
        return dAR(this.json);
    }

    // defaultModelResource or something? Weird implementation
    private static Identifier dMR(JsonObject json) {
        return Identifier.tryParse(JsonHelper.getString(json, "model", "originsfurs:geo/missing.geo.json"));
    }
    private static Identifier dTR(JsonObject json) {
        return Identifier.tryParse(JsonHelper.getString(json, "texture", "originsfurs:textures/missing.png"));
    }
    private static Identifier dAR(JsonObject json) {
        return Identifier.tryParse(JsonHelper.getString(json, "animation", "originsfurs:animations/missing.animation.json"));
    }

    private boolean hasRenderingOffsets() {
        return this.json.has("rendering_offsets");
    }

    private boolean hasSubRenderingOffset(String id) {
        return this.hasRenderingOffsets() && this.json.getAsJsonObject("rendering_offsets").has(id);
    }

    private Vector3d getRenderingOffset(String id) {
        return hasSubRenderingOffset(id) ?
                Alib.jsonToVector3d(json.getAsJsonObject("rendering_offsets").get(id))
                : VECTOR3D_ZERO;
    }

    public final Vector3d getRightOffset() {
        return getRenderingOffset("right");
    }

    public final Vector3d getLeftOffset() {
        return getRenderingOffset("left");
    }

    public boolean hasCustomElytraTexture() {
        return json.has("elytraTexture");
    }
    public Identifier getElytraTexture() {
        String jsonContent = JsonHelper.getString(json, "elytraTexture", "textures/entity/elytra.png");
        return Identifier.tryParse(jsonContent);
    }

    public Identifier getOverlayTexture(boolean isSlim) {
        if (!isSlim || !this.json.has("overlay_slim")) {
            if (json.has("overlay")) {
                String jsonContent = JsonHelper.getString(json, "overlay");
                return Identifier.tryParse(jsonContent);
            }
        } else {
            if (this.json.has("overlay_slim")) {
                String jsonContent = JsonHelper.getString(json, "overlay_slim");
                return Identifier.tryParse(jsonContent);
            }
        }
        return null;
    }

    public Identifier getEmissiveTexture(boolean isSlim) {
        if (!isSlim || !this.json.has("emissive_overlay_slim")) {
            if (json.has("emissive_overlay")) {
                String jsonContent = JsonHelper.getString(json, "emissive_overlay");
                return Identifier.tryParse(jsonContent);
            }
        } else {
            if (this.json.has("emissive_overlay_slim")) {
                String jsonContent = JsonHelper.getString(json, "emissive_overlay_slim");
                return Identifier.tryParse(jsonContent);
            }
        }
        return null;
    }

    /*
    public void preRender$mixinOnly(PlayerEntity player) {
        this.player = player;
    }
    */

    /**
     * @return If the player model must be invisible.
     */
    public final boolean isPlayerModelInvisible() {
        return JsonHelper.getBoolean(json, "playerInvisible", false);
    }

    public EnumSet<VMP> getHiddenVanillaParts() {
        return hiddenParts;
    }

    /**
     * Decides what parts of the Model must be hidden and hides them.
     */
    public void preprocess(
            Origin origin,
            PlayerEntityRenderer playerEntityRender,
            IPlayerEntityMixins playerEntity,
            ModelRootAccessor playerEntityModel,
            AbstractClientPlayerEntity abstractClientPlayerEntity
    ) {
        this.getAnimationProcessor().getRegisteredBones().forEach(coreGeoBone -> {
            coreGeoBone.setHidden(false);
            String boneName = coreGeoBone.getName();

            // Verificar condiciones en orden de prioridad
            coreGeoBone.setHidden(
                    shouldHideBone(boneName, playerEntityModel, origin, abstractClientPlayerEntity)
            );
        });
    }

    private boolean shouldHideBone(
            String boneName,
            ModelRootAccessor playerEntityModel,
            Origin origin,
            AbstractClientPlayerEntity player
    ) {
        // Razones para estar escondido
        return isThinHiddenBone(boneName, playerEntityModel) ||
                isWideHiddenBone(boneName, playerEntityModel) ||
                isElytraHiddenBone(boneName, origin, player) ||
                isArmorHiddenBone(boneName, player) ||
                isModHiddenBone(boneName);
    }

    private boolean isThinHiddenBone(String boneName, ModelRootAccessor model) {
        return boneName.endsWith("thin_only") && !model.originsFurs$isSlim();
    }

    private boolean isWideHiddenBone(String boneName, ModelRootAccessor model) {
        return boneName.endsWith("wide_only") && model.originsFurs$isSlim();
    }

    private boolean isElytraHiddenBone(String boneName, Origin origin, AbstractClientPlayerEntity player) {
        return boneName.contains("elytra_hides") &&
                (origin.hasPower(PowerManager.get(Identifier.of("origins:elytra"))) ||
                        player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA));
    }

    private boolean isArmorHiddenBone(String boneName, AbstractClientPlayerEntity player) {
        return (boneName.contains("helmet_hides") && !player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) ||
                (boneName.contains("chestplate_hides") && !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) ||
                (boneName.contains("leggings_hides") && !player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) ||
                (boneName.contains("boots_hides") && !player.getEquippedStack(EquipmentSlot.FEET).isEmpty());
    }

    private boolean isModHiddenBone(String boneName) {
        return FabricLoader.getInstance().getAllMods().stream()
                .anyMatch(mod -> boneName.contains("mod_hides_" + mod.getMetadata().getId()));
    }

    // This is what actually contains the information about the model
//    private static class ResourceOverride {
//        public NbtElement requirements;
//        public Identifier textureResource = Identifier.tryParse("originsfurs:textures/missing.png");
//        public Identifier modelResource = Identifier.tryParse("originsfurs:geo/missing.geo.json");
//        public Identifier animationResource = Identifier.tryParse("originsfurs:animations/missing.animation.json");
//        public float weight;
//
//        // Gets conditions and weight?
//        private static ResourceOverride deserializeBase(JsonObject object, ResourceOverride resourceOverride) {
//            resourceOverride.requirements = Alib.jsonToNBT(object.get("condition"));
//            resourceOverride.weight = JsonHelper.getFloat(object, "weight", 1F);
//            return resourceOverride;
//        }
//
//        // Creates a ResourceOverride extracting texture, model and animations from the JSON
//        public static ResourceOverride deserialize(JsonObject object) {
//            ResourceOverride resourceOverride = deserializeBase(object, new ResourceOverride());
//            resourceOverride.textureResource = Identifier.tryParse(JsonHelper.getString(object, "texture", "originsfurs:textures/missing.png"));
//            resourceOverride.modelResource = Identifier.tryParse(JsonHelper.getString(object, "model", "originsfurs:geo/missing.geo.json"));
//            resourceOverride.animationResource = Identifier.tryParse(JsonHelper.getString(object, "animation", "originsfurs:animations/missing.animation.json"));
//            return resourceOverride;
//        }
//    }

    // Vanilla Model Parts
    public enum VMP {
        LEFT_ARM,
        RIGHT_ARM,
        RIGHT_SLEEVE,
        LEFT_SLEEVE,
        RIGHT_LEG,
        LEFT_LEG,
        RIGHT_PANTS,
        LEFT_PANTS,
        HAT,
        HEAD,
        BODY,
        JACKET;

        public static final EnumSet<OriginFurModel.VMP> ALL_OPTS =
                EnumSet.allOf(OriginFurModel.VMP.class);
    }
}