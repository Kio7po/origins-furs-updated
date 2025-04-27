package com.kio7po.originsfurs.fabric.client;

import com.google.common.hash.Hashing;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import net.minecraft.nbt.*;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3d;

import java.nio.charset.StandardCharsets;
import java.util.List;

// todo: desarrollar segun sea necesario
public class Alib {
    /**
     * Converts a JsonElement into a NbtElement (List, Compound o Primitive).
     * @throws IllegalArgumentException if JSON is not compatible.
     */
//    public static NbtElement jsonToNBT(JsonElement jsonElement) {
//        switch (jsonElement) {
//            case JsonArray array -> {
//                NbtList nbtList = new NbtList();
//                array.forEach(element -> nbtList.add(jsonToNBT(element)));
//                return nbtList;
//            }
//            case JsonObject object -> {
//                NbtCompound nbtCompound = new NbtCompound();
//                object.entrySet().forEach(entry ->
//                        nbtCompound.put(entry.getKey(), jsonToNBT(entry.getValue())));
//                return nbtCompound;
//            }
//            case JsonPrimitive primitive -> {
//                return jsonPrimitiveToNbtElement(primitive);
//            }
//            default -> throw new IllegalArgumentException("Unexpected JSON element" + jsonElement);
//        }
//    }

//    private static NbtElement jsonPrimitiveToNbtElement(JsonPrimitive jp) {
//        if (jp.isNumber()) return numberToNbtElement(jp.getAsNumber());
//        else if (jp.isBoolean()) return NbtByte.of(jp.getAsBoolean());
//        else if (jp.isString()) return NbtString.of(jp.getAsString());
//        else throw new IllegalArgumentException("Unexpected primitive" + jp);
//    }

//    private static NbtElement numberToNbtElement(Number number) {
//        return switch (number) {
//            case Integer i -> NbtInt.of(i);
//            case Long l -> NbtLong.of(l);
//            case Float f -> NbtFloat.of(f);
//            case Double d -> NbtDouble.of(d);
//            case Short s -> NbtShort.of(s);
//            case Byte b -> NbtByte.of(b);
//            case LazilyParsedNumber lpn -> numberToNbtElement(gsonLazilyParsedNumberToNumber(lpn));
//            default -> throw new IllegalStateException("Unexpected value: " + number);
//        };
//    }

//    private static Number gsonLazilyParsedNumberToNumber(LazilyParsedNumber lpn) {
//        String value = lpn.toString();
//        try {
//            return Integer.parseInt(value);
//        } catch (NumberFormatException ignored) {}
//        try {
//            return Long.parseLong(value);
//        } catch (NumberFormatException ignored) {}
//        return Double.parseDouble(value);
//    }

    /**
     * Gets the Hash of a String as a Long (64 bits)
     */
    public static long getHash64(String input) {
        return Hashing.murmur3_128().hashString(input, StandardCharsets.UTF_8).asLong();
    }

    /**
     * Parses a Vector3d from JSON. Supported formats:
     * - Array: [x, y, z]
     * - Object: {"x": val, "y": val, "z": val}
     * - Number (primitive): treated as a packed long (x/y/z as 16-bit values)
     * @throws JsonParseException if JSON is malformed or missing required fields.
     */
    public static Vector3d jsonToVector3d(JsonElement element) {
        switch (element) {
            case JsonArray array -> {
                if (array.size() != 3)
                    throw new JsonParseException("Expected 3 elements in array, got " + array.size());
                return new Vector3d(
                        array.get(0).getAsDouble(),
                        array.get(1).getAsDouble(),
                        array.get(2).getAsDouble()
                );
            }
            case JsonObject object -> {
                if (!object.has("x") || !object.has("y") || !object.has("z")) {
                    throw new JsonParseException("Missing required field(s) in vector object");
                }
                return new Vector3d(
                        object.get("x").getAsDouble(),
                        object.get("y").getAsDouble(),
                        object.get("z").getAsDouble()
                );
            }
            case JsonPrimitive primitive when primitive.isNumber() -> {
                return longToVector3d(primitive.getAsLong());
            }
            default -> throw new JsonParseException("Failed to parse Vector3d from JSON: " + element);
        }
    }

    /**
     * Converts a packed long into Vector3d (x/y/z as 16-bit values).
     * Note: Lossy conversion (16 bits per component).
     */
    private static Vector3d longToVector3d(long value) {
        return new Vector3d(
                value & 0xFFFF,
                (value >> 16) & 0xFFFF,
                (value >> 32) & 0xFFFF
        );
    }

    /**
     * Receives an ARGB and a list of ModelColorPowers and applies
     * them to create a new ARGB.
     */
    public static int getArgbFromColorPowers(int argb, List<ModelColorPowerType> modelColorPowers) {
        float newRed = modelColorPowers
                .stream()
                .map(ModelColorPowerType::getRed)
                .reduce((float) ColorHelper.Argb.getRed(argb) / 255, (a, b) -> a * b);
        float newGreen = modelColorPowers
                .stream()
                .map(ModelColorPowerType::getGreen)
                .reduce((float) ColorHelper.Argb.getGreen(argb) / 255, (a, b) -> a * b);
        float newBlue = modelColorPowers
                .stream()
                .map(ModelColorPowerType::getBlue)
                .reduce((float) ColorHelper.Argb.getBlue(argb) / 255, (a, b) -> a * b);

        float oldAlpha = (float) ColorHelper.Argb.getAlpha(argb) / 255;
        float newAlpha = modelColorPowers
                .stream()
                .map(ModelColorPowerType::getAlpha)
                .min(Float::compareTo)
                .map(alphaFactor -> oldAlpha * alphaFactor)
                .orElse(oldAlpha);
        return ColorHelper.Argb.fromFloats(newAlpha, newRed, newGreen, newBlue);
    }
}
