package com.kio7po.originsfurs.fabric.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kio7po.originsfurs.fabric.client.util.Alib;
import com.kio7po.originsfurs.fabric.client.bridge.IPlayerEntityModel;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.origins.origin.Origin;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.joml.Vector3d;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.ClientUtil;

import java.util.*;

// todo: update deprecated stuff
// todo: desarrollar segun sea necesario
@Environment(EnvType.CLIENT)
public class OriginFurModel extends GeoModel<OriginFurAnimatable> {
    //public static final OriginFurModel EMPTY = new OriginFurModel(Origin.EMPTY, new JsonObject());
    public static final Vector3d VECTOR3D_ZERO = new Vector3d();
    public static final Vector3f VECTOR3F_ZERO = new Vector3f();

    public static final Identifier MISSING_MODEL = Identifier.tryParse("originsfurs:geo/missing.geo.json");
    public static final Identifier MISSING_TEXTURE = Identifier.tryParse("originsfurs:textures/missing.png");
    public static final Identifier MISSING_ANIMATION = Identifier.tryParse("originsfurs:animations/missing.animation.json");

    public JsonObject json;
    private Identifier model;
    private Identifier texture;
    private Identifier textureGlowmask;
    private Identifier elytraTexture;
    private Identifier overlayTexture;
    private Identifier overlayTextureSlim;
    private Identifier emissiveOverlayTexture;
    private Identifier emissiveOverlayTextureSlim;
    private Identifier animation;
    private Vector3d rightHeldItemOffset = VECTOR3D_ZERO;
    private Vector3d leftHeldItemOffset = VECTOR3D_ZERO;
    public EnumSet<OriginFurModel.VMP> hiddenParts = EnumSet.noneOf(OriginFurModel.VMP.class);

    private Origin origin;

    public Long2ReferenceLinkedOpenHashMap<software.bernie.geckolib.cache.object.GeoBone> boneCache = new Long2ReferenceLinkedOpenHashMap<>();

    public OriginFurModel(Origin origin, JsonObject json) {
        this.origin = origin;
        this.recompile(json);
    }

    // Limpia y da valor a todos los parámetros en base al JSON
    public void recompile(JsonObject json) {
        parseJson(json);
        this.boneCache.clear();
    }

    public void parseJson(JsonObject json) {
        this.json = json;
        parseModel();
        parseTexture();
        parseTextureGlowmask();
        parseAnimation();
        parseHiddenParts();
        parseOverlayTexture();
        parseEmissiveOverlayTexture();
        parseElytraTexture();
        parseHeldItemOffsets();
    }

    private void parseModel() {
        if (JsonHelper.hasString(json, "model"))
            this.model = Identifier.tryParse(JsonHelper.getString(json, "model"));
        else this.model = MISSING_MODEL;
    }

    private void parseTexture() {
        if (JsonHelper.hasString(json, "texture"))
            this.texture = Identifier.tryParse(JsonHelper.getString(json, "texture"));
        else this.texture = MISSING_TEXTURE;
    }

    private void parseTextureGlowmask() {
        if (JsonHelper.hasString(json, "texture_glowmask"))
            this.textureGlowmask = Identifier.tryParse(JsonHelper.getString(json, "texture_glowmask"));
    }

    private void parseAnimation() {
        if (JsonHelper.hasString(json, "animation"))
            this.animation = Identifier.tryParse(JsonHelper.getString(json, "animation"));
        else this.animation = MISSING_ANIMATION;
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

    private void parseElytraTexture() {
        if (JsonHelper.hasString(json, "elytra_texture"))
            this.elytraTexture = Identifier.tryParse(JsonHelper.getString(json, "elytra_texture"));
    }

    private void parseOverlayTexture() {
        if (JsonHelper.hasString(json, "overlay"))
            this.overlayTexture = Identifier.tryParse(JsonHelper.getString(json, "overlay"));
        if (JsonHelper.hasString(json,"overlay_slim"))
            this.overlayTextureSlim = Identifier.tryParse(JsonHelper.getString(json, "overlay_slim"));
    }

    private void parseEmissiveOverlayTexture() {
        if (JsonHelper.hasString(json, "emissive_overlay"))
            this.emissiveOverlayTexture = Identifier.tryParse(JsonHelper.getString(json, "emissive_overlay"));
        if (JsonHelper.hasString(json,"emissive_overlay_slim"))
            this.emissiveOverlayTextureSlim = Identifier.tryParse(JsonHelper.getString(json, "emissive_overlay_slim"));
    }

    private void parseHeldItemOffsets() {
        if (JsonHelper.hasJsonObject(json, "held_item_offsets")) {
            JsonObject offsets = this.json.getAsJsonObject("held_item_offsets");
            if (JsonHelper.hasArray(offsets, "right"))
                rightHeldItemOffset = Alib.jsonToVector3d(offsets.get("right"));
            if (JsonHelper.hasArray(offsets, "left"))
                leftHeldItemOffset = Alib.jsonToVector3d(offsets.get("left"));
        }
    }

    @Override
    public Identifier getModelResource(OriginFurAnimatable animatable) {
        return this.model;
    }

    @Override
    public Identifier getTextureResource(OriginFurAnimatable animatable) {
        return this.texture;
    }

    public Identifier getTextureGlowmaskResource() {
        return this.textureGlowmask;
    }

    @Override
    public Identifier getAnimationResource(OriginFurAnimatable animatable) {
        return this.animation;
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

//    private Vector3f vector3dToVector3f(Vector3d vec3d) {
//        return new Vector3f((float)vec3d.x(), (float)vec3d.y(), (float)vec3d.z());
//    }
//
//    public GeoBone setPositionForBone(String identifier, Vector3d pos) {
//        return setPositionForBone(identifier, vector3dToVector3f(pos));
//    }
//
//    public GeoBone setPositionForBone(String identifier, Vector3f pos) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null) {
//            return null;
//        } else {
//            bone.setPosX(pos.x());
//            bone.setPosY(pos.y());
//            bone.setPosZ(pos.z());
//            return bone;
//        }
//    }

//    // No usage
//    public GeoBone setRotationForBone(String identifier, Vector3d rot) {
//        return setRotationForBone(identifier, vector3dToVector3f(rot));
//    }
//    public final GeoBone setRotationForBone(String identifier, Vector3f rot) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null) {
//            return null;
//        } else {
//            bone.setRotX(rot.x());
//            bone.setRotY(rot.y());
//            bone.setRotZ(rot.z());
//            return bone;
//        }
//    }

//    public GeoBone translatePositionForBone(String identifier, Vector3d pos) {
//        return translatePositionForBone(identifier, vector3dToVector3f(pos));
//    }
//
//    public final GeoBone translatePositionForBone(String identifier, Vector3f pos) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null) {
//            return null;
//        }
//        Vector3d newPos = new Vector3d(pos.x() + bone.getPosX(), pos.y() + bone.getPosY(),pos.z() + bone.getPosZ());
//        return this.setPositionForBone(identifier, newPos);
//    }

//    public GeoBone setModelPositionForBone(String identifier, Vector3d pos) {
//        return setModelPositionForBone(identifier, vector3dToVector3f(pos));
//    }
//
//    public final GeoBone setModelPositionForBone(String identifier, Vector3f pos) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null) {
//            return null;
//        }
//        bone.setModelPosition(new Vector3d(pos.x(), pos.y(), pos.z()));
//        return bone;
//    }

//    public final GeoBone setScaleForBone(String identifier, Vector3d scale) {
//        return setScaleForBone(identifier, vector3dToVector3f(scale));
//    }

//    public final GeoBone setScaleForBone(String identifier, Vector3f scale) {
//        GeoBone bone = this.getCachedGeoBone(identifier);
//        if (bone == null) {
//            return null;
//        }
//        bone.setScaleX(scale.x());
//        bone.setScaleY(scale.y());
//        bone.setScaleZ(scale.z());
//        return bone;
//    }

//    public final GeoBone resetBone(String name) {
//        setPositionForBone(name, VECTOR3F_ZERO);
//        setRotationForBone(name, VECTOR3F_ZERO);
//        setModelPositionForBone(name, VECTOR3F_ZERO);
//        return setScaleForBone(name, new Vector3d(1,1,1));
//    }

    private Vector3d getHeldItemOffset(String id) {
        return switch (id) {
            case "right" -> rightHeldItemOffset;
            case "left" -> leftHeldItemOffset;
            default -> null;
        };
    }

    public final Vector3d getRightOffset() {
        return getHeldItemOffset("right");
    }

    public final Vector3d getLeftOffset() {
        return getHeldItemOffset("left");
    }

    public boolean hasCustomElytraTexture() {
        return elytraTexture!=null;
    }
    public Identifier getElytraTexture() {
        return elytraTexture;
    }

    public Identifier getOverlayTexture(boolean isSlim) {
        if (!isSlim || overlayTextureSlim==null) {
            return overlayTexture;
        } else {
            return overlayTextureSlim;
        }
    }

    public Identifier getEmissiveOverlayTexture(boolean isSlim) {
        if (!isSlim || emissiveOverlayTextureSlim==null) {
            return emissiveOverlayTexture;
        } else {
            return emissiveOverlayTextureSlim;
        }
    }

    /**
     * @return If the player model must be invisible.
     */
    public final boolean isPlayerModelInvisible() {
        return JsonHelper.getBoolean(json, "playerInvisible", false);
    }

    public EnumSet<VMP> getHiddenVanillaParts() {
        return hiddenParts;
    }

    public Origin getOrigin() {
        return origin;
    }

    /**
     * Decides what parts of the Model must be hidden and hides them.
     */
    public void preprocess(
            IPlayerEntityModel playerEntityModel,
            AbstractClientPlayerEntity abstractClientPlayerEntity
    ) {
        this.getAnimationProcessor().getRegisteredBones().forEach(coreGeoBone -> {
            coreGeoBone.setHidden(false);
            String boneName = coreGeoBone.getName();

            // Verificar condiciones en orden de prioridad
            coreGeoBone.setHidden(
                    shouldHideBone(boneName, playerEntityModel, abstractClientPlayerEntity)
            );
        });
    }

    private boolean shouldHideBone(
            String boneName,
            IPlayerEntityModel playerEntityModel,
            AbstractClientPlayerEntity player
    ) {
        // Reasons to be hidden
        return isThinHiddenBone(boneName, playerEntityModel) ||
                isWideHiddenBone(boneName, playerEntityModel) ||
                isElytraHiddenBone(boneName, player) ||
                isArmorHiddenBone(boneName, player) ||
                isModHiddenBone(boneName);
    }

    // Hidden due to being for thin only and model is wide
    private boolean isThinHiddenBone(String boneName, IPlayerEntityModel model) {
        return boneName.endsWith("thin_only") && !model.originsfurs$isSlim();
    }

    // Hidden due to being for wide only and model is thin
    private boolean isWideHiddenBone(String boneName, IPlayerEntityModel model) {
        return boneName.endsWith("wide_only") && model.originsfurs$isSlim();
    }

    // Hidden due to having elytra equipped
    private boolean isElytraHiddenBone(String boneName, AbstractClientPlayerEntity player) {
        return boneName.contains("elytra_hides") &&
                (this.origin.hasPower(PowerManager.get(Identifier.of("origins:elytra"))) ||
                        player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA));
    }

    // Hidden due to having some equipment piece equipped
    private boolean isArmorHiddenBone(String boneName, AbstractClientPlayerEntity player) {
        return (boneName.contains("helmet_hides") && !player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) ||
                (boneName.contains("chestplate_hides") && !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) ||
                (boneName.contains("leggings_hides") && !player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) ||
                (boneName.contains("boots_hides") && !player.getEquippedStack(EquipmentSlot.FEET).isEmpty());
    }

    // Hidden due to having a mod installed
    private boolean isModHiddenBone(String boneName) {
        return FabricLoader.getInstance().getAllMods().stream()
                .anyMatch(mod -> boneName.contains("mod_hides_" + mod.getMetadata().getId()));
    }

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