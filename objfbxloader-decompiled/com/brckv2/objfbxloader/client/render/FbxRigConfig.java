package com.brckv2.objfbxloader.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;

public record FbxRigConfig(
   String headBone,
   String neckBone,
   String rightHandBone,
   String leftHandBone,
   String upperTorsoBone,
   String rightArmBone,
   String leftArmBone,
   String rightItemBone,
   String leftItemBone,
   float rightHandOffsetX,
   float rightHandOffsetY,
   float rightHandOffsetZ,
   float rightHandRotX,
   float rightHandRotY,
   float rightHandRotZ,
   float rightHandScale,
   float leftHandOffsetX,
   float leftHandOffsetY,
   float leftHandOffsetZ,
   float leftHandRotX,
   float leftHandRotY,
   float leftHandRotZ,
   float leftHandScale,
   boolean handAttachmentEnabled,
   boolean thirdPersonItemVisible,
   String torsoLookBone,
   String attackStopBone,
   float headLookPitchFactor,
   float headLookYawFactor,
   float neckLookFactor,
   float torsoLookPitchFactor,
   float torsoLookYawFactor,
   String voiceUpperLipBone,
   String voiceLowerLipBone,
   float voiceLipPitchFactor,
   float voiceLipYawFactor,
   float voiceLipRollFactor,
   float voiceLipMaxDegrees,
   boolean attackUpperBodyOnly,
   float entityOffsetX,
   float entityOffsetY,
   float entityOffsetZ,
   float entityRotX,
   float entityRotY,
   float entityRotZ,
   float entityScale,
   Map<String, Float> animationSpeedMultipliers,
   Map<String, FbxItemTransform> customItemTransforms
) {
   public static final FbxRigConfig EMPTY = new FbxRigConfig(
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      1.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      1.0F,
      true,
      true,
      "",
      "",
      0.55F,
      0.55F,
      0.35F,
      0.2F,
      0.2F,
      "",
      "",
      1.0F,
      0.0F,
      0.0F,
      16.0F,
      true,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      0.0F,
      1.0F,
      Map.of(),
      Map.of()
   );
   private static final String DEFAULT_NAMESPACE = "minecraft";
   private static final String PLAYER_TARGET = "player";

   public FbxRigConfig(
      String headBone,
      String neckBone,
      String rightHandBone,
      String leftHandBone,
      String upperTorsoBone,
      String rightArmBone,
      String leftArmBone,
      String rightItemBone,
      String leftItemBone,
      float rightHandOffsetX,
      float rightHandOffsetY,
      float rightHandOffsetZ,
      float rightHandRotX,
      float rightHandRotY,
      float rightHandRotZ,
      float rightHandScale,
      float leftHandOffsetX,
      float leftHandOffsetY,
      float leftHandOffsetZ,
      float leftHandRotX,
      float leftHandRotY,
      float leftHandRotZ,
      float leftHandScale,
      boolean handAttachmentEnabled,
      boolean thirdPersonItemVisible,
      String torsoLookBone,
      String attackStopBone,
      float headLookPitchFactor,
      float headLookYawFactor,
      float neckLookFactor,
      float torsoLookPitchFactor,
      float torsoLookYawFactor,
      String voiceUpperLipBone,
      String voiceLowerLipBone,
      float voiceLipPitchFactor,
      float voiceLipYawFactor,
      float voiceLipRollFactor,
      float voiceLipMaxDegrees,
      boolean attackUpperBodyOnly,
      float entityOffsetX,
      float entityOffsetY,
      float entityOffsetZ,
      float entityRotX,
      float entityRotY,
      float entityRotZ,
      float entityScale,
      Map<String, Float> animationSpeedMultipliers,
      Map<String, FbxItemTransform> customItemTransforms
   ) {
      animationSpeedMultipliers = sanitizeAnimationSpeedMultipliers(animationSpeedMultipliers);
      customItemTransforms = sanitizeCustomItemTransforms(customItemTransforms);
      this.headBone = headBone;
      this.neckBone = neckBone;
      this.rightHandBone = rightHandBone;
      this.leftHandBone = leftHandBone;
      this.upperTorsoBone = upperTorsoBone;
      this.rightArmBone = rightArmBone;
      this.leftArmBone = leftArmBone;
      this.rightItemBone = rightItemBone;
      this.leftItemBone = leftItemBone;
      this.rightHandOffsetX = rightHandOffsetX;
      this.rightHandOffsetY = rightHandOffsetY;
      this.rightHandOffsetZ = rightHandOffsetZ;
      this.rightHandRotX = rightHandRotX;
      this.rightHandRotY = rightHandRotY;
      this.rightHandRotZ = rightHandRotZ;
      this.rightHandScale = rightHandScale;
      this.leftHandOffsetX = leftHandOffsetX;
      this.leftHandOffsetY = leftHandOffsetY;
      this.leftHandOffsetZ = leftHandOffsetZ;
      this.leftHandRotX = leftHandRotX;
      this.leftHandRotY = leftHandRotY;
      this.leftHandRotZ = leftHandRotZ;
      this.leftHandScale = leftHandScale;
      this.handAttachmentEnabled = handAttachmentEnabled;
      this.thirdPersonItemVisible = thirdPersonItemVisible;
      this.torsoLookBone = torsoLookBone;
      this.attackStopBone = attackStopBone;
      this.headLookPitchFactor = headLookPitchFactor;
      this.headLookYawFactor = headLookYawFactor;
      this.neckLookFactor = neckLookFactor;
      this.torsoLookPitchFactor = torsoLookPitchFactor;
      this.torsoLookYawFactor = torsoLookYawFactor;
      this.voiceUpperLipBone = voiceUpperLipBone;
      this.voiceLowerLipBone = voiceLowerLipBone;
      this.voiceLipPitchFactor = voiceLipPitchFactor;
      this.voiceLipYawFactor = voiceLipYawFactor;
      this.voiceLipRollFactor = voiceLipRollFactor;
      this.voiceLipMaxDegrees = voiceLipMaxDegrees;
      this.attackUpperBodyOnly = attackUpperBodyOnly;
      this.entityOffsetX = entityOffsetX;
      this.entityOffsetY = entityOffsetY;
      this.entityOffsetZ = entityOffsetZ;
      this.entityRotX = entityRotX;
      this.entityRotY = entityRotY;
      this.entityRotZ = entityRotZ;
      this.entityScale = entityScale;
      this.animationSpeedMultipliers = animationSpeedMultipliers;
      this.customItemTransforms = customItemTransforms;
   }

   public static FbxRigConfig load(class_3300 resourceManager, String namespace, String targetPath) {
      String safeNamespace = namespace != null && !namespace.isBlank() ? namespace : "minecraft";
      String safeTargetPath = targetPath != null && !targetPath.isBlank() ? targetPath : "player";
      FbxRigConfig direct = tryLoad(resourceManager, safeNamespace, safeTargetPath);
      if (direct != null) {
         return direct;
      } else {
         if (!"minecraft".equals(safeNamespace)) {
            FbxRigConfig vanillaNamespace = tryLoad(resourceManager, "minecraft", safeTargetPath);
            if (vanillaNamespace != null) {
               return vanillaNamespace;
            }
         }

         if (!"player".equals(safeTargetPath)) {
            FbxRigConfig sameNamespacePlayer = tryLoad(resourceManager, safeNamespace, "player");
            if (sameNamespacePlayer != null) {
               return sameNamespacePlayer;
            }
         }

         if (!"minecraft".equals(safeNamespace) || !"player".equals(safeTargetPath)) {
            FbxRigConfig vanillaPlayer = tryLoad(resourceManager, "minecraft", "player");
            if (vanillaPlayer != null) {
               return vanillaPlayer;
            }
         }

         return EMPTY;
      }
   }

   private static FbxRigConfig tryLoad(class_3300 resourceManager, String namespace, String targetPath) {
      class_2960 id = class_2960.method_43902(namespace, "configs/entity/" + targetPath + ".json");
      class_3298 resource = null;
      if (id != null) {
         resource = (class_3298)resourceManager.method_14486(id).orElse(null);
      }

      if (resource == null) {
         class_2960 legacy = class_2960.method_43902(namespace, "configs/" + targetPath + ".json");
         if (legacy != null) {
            resource = (class_3298)resourceManager.method_14486(legacy).orElse(null);
         }
      }

      if (resource == null) {
         return null;
      } else {
         try {
            FbxRigConfig var10;
            try (BufferedReader reader = resource.method_43039()) {
               StringBuilder builder = new StringBuilder();

               String line;
               while ((line = reader.readLine()) != null) {
                  builder.append(line);
               }

               String jsonText = builder.toString();
               JsonObject json = parseJsonObject(jsonText);
               var10 = new FbxRigConfig(
                  readJsonString(json, "headBone", ""),
                  readJsonString(json, "neckBone", ""),
                  readJsonString(json, "rightHandBone", ""),
                  readJsonString(json, "leftHandBone", ""),
                  readJsonString(json, "upperTorsoBone", ""),
                  readJsonString(json, "rightArmBone", ""),
                  readJsonString(json, "leftArmBone", ""),
                  readJsonString(json, "rightItemBone", ""),
                  readJsonString(json, "leftItemBone", ""),
                  readJsonFloat(json, "rightHandOffsetX", 0.0F),
                  readJsonFloat(json, "rightHandOffsetY", 0.0F),
                  readJsonFloat(json, "rightHandOffsetZ", 0.0F),
                  readJsonFloat(json, "rightHandRotX", 0.0F),
                  readJsonFloat(json, "rightHandRotY", 0.0F),
                  readJsonFloat(json, "rightHandRotZ", 0.0F),
                  readJsonFloat(json, "rightHandScale", 1.0F),
                  readJsonFloat(json, "leftHandOffsetX", 0.0F),
                  readJsonFloat(json, "leftHandOffsetY", 0.0F),
                  readJsonFloat(json, "leftHandOffsetZ", 0.0F),
                  readJsonFloat(json, "leftHandRotX", 0.0F),
                  readJsonFloat(json, "leftHandRotY", 0.0F),
                  readJsonFloat(json, "leftHandRotZ", 0.0F),
                  readJsonFloat(json, "leftHandScale", 1.0F),
                  readJsonBoolean(json, "handAttachmentEnabled", true),
                  readJsonBoolean(json, "thirdPersonItemVisible", true),
                  readJsonString(json, "torsoLookBone", ""),
                  readJsonString(json, "attackStopBone", ""),
                  readJsonFloat(json, "headLookPitchFactor", 0.55F),
                  readJsonFloat(json, "headLookYawFactor", 0.55F),
                  readJsonFloat(json, "neckLookFactor", 0.35F),
                  readJsonFloat(json, "torsoLookPitchFactor", 0.12F),
                  readJsonFloat(json, "torsoLookYawFactor", 0.12F),
                  readJsonString(json, "voiceUpperLipBone", ""),
                  readJsonString(json, "voiceLowerLipBone", ""),
                  readJsonFloat(json, "voiceLipPitchFactor", 1.0F),
                  readJsonFloat(json, "voiceLipYawFactor", 0.0F),
                  readJsonFloat(json, "voiceLipRollFactor", 0.0F),
                  readJsonFloat(json, "voiceLipMaxDegrees", 16.0F),
                  readJsonBoolean(json, "attackUpperBodyOnly", true),
                  readJsonFloat(json, "entityOffsetX", 0.0F),
                  readJsonFloat(json, "entityOffsetY", 0.0F),
                  readJsonFloat(json, "entityOffsetZ", 0.0F),
                  readJsonFloat(json, "entityRotX", 0.0F),
                  readJsonFloat(json, "entityRotY", 0.0F),
                  readJsonFloat(json, "entityRotZ", 0.0F),
                  readJsonFloat(json, "entityScale", 1.0F),
                  readAnimationSpeedMultipliers(json),
                  readCustomItemTransforms(json)
               );
            }

            return var10;
         } catch (Exception var13) {
            return null;
         }
      }
   }

   public String resolveTorsoLookBone() {
      if (this.torsoLookBone != null && !this.torsoLookBone.isBlank()) {
         return this.torsoLookBone;
      } else if (this.upperTorsoBone != null && !this.upperTorsoBone.isBlank()) {
         return this.upperTorsoBone;
      } else {
         String primaryNeck = this.resolvePrimaryNeckBone();
         if (primaryNeck != null && !primaryNeck.isBlank()) {
            String neck = primaryNeck;
            String[] inferredCandidates = new String[]{
               primaryNeck.replace("Neck", "Spine2"),
               primaryNeck.replace("Neck", "Spine1"),
               primaryNeck.replace("Neck", "Chest"),
               primaryNeck.replace("neck", "spine2"),
               primaryNeck.replace("neck", "spine1"),
               primaryNeck.replace("neck", "chest")
            };

            for (String candidate : inferredCandidates) {
               if (candidate != null && !candidate.isBlank() && !candidate.equals(neck)) {
                  return candidate;
               }
            }
         }

         return "";
      }
   }

   public String resolvePrimaryHeadBone() {
      return resolvePrimaryBoneName(this.headBone);
   }

   public String resolvePrimaryNeckBone() {
      return resolvePrimaryBoneName(this.neckBone);
   }

   public List<String> headBones() {
      return splitBoneTargets(this.headBone);
   }

   public List<String> neckBones() {
      return splitBoneTargets(this.neckBone);
   }

   public static List<String> splitBoneTargets(String value) {
      if (value != null && !value.isBlank()) {
         LinkedHashSet<String> unique = new LinkedHashSet<>();
         String normalized = value.replace('\n', '|').replace('\r', '|');
         String[] parts = normalized.split("[|,;]");

         for (String part : parts) {
            if (part != null) {
               String trimmed = part.trim();
               if (!trimmed.isBlank()) {
                  unique.add(trimmed);
               }
            }
         }

         if (unique.isEmpty()) {
            String trimmed = value.trim();
            return trimmed.isBlank() ? List.of() : List.of(trimmed);
         } else {
            return List.copyOf(unique);
         }
      } else {
         return List.of();
      }
   }

   public static String joinBoneTargets(List<String> bones) {
      if (bones != null && !bones.isEmpty()) {
         List<String> cleaned = new ArrayList<>();
         LinkedHashSet<String> unique = new LinkedHashSet<>();

         for (String bone : bones) {
            if (bone != null) {
               String trimmed = bone.trim();
               if (!trimmed.isBlank() && unique.add(trimmed)) {
                  cleaned.add(trimmed);
               }
            }
         }

         return cleaned.isEmpty() ? "" : String.join("|", cleaned);
      } else {
         return "";
      }
   }

   private static String resolvePrimaryBoneName(String raw) {
      List<String> bones = splitBoneTargets(raw);
      return bones.isEmpty() ? "" : bones.get(0);
   }

   public String resolveAttackStopBone() {
      return this.attackStopBone != null && !this.attackStopBone.isBlank() ? this.attackStopBone : this.resolveTorsoLookBone();
   }

   public float animationSpeedMultiplier(String animationName) {
      if (animationName != null && !animationName.isBlank()) {
         Float configured = this.animationSpeedMultipliers.get(animationName.trim().toLowerCase(Locale.ROOT));
         return configured != null && Float.isFinite(configured) ? Math.max(0.05F, Math.min(4.0F, configured)) : 1.0F;
      } else {
         return 1.0F;
      }
   }

   public FbxItemTransform customItemTransform(class_2960 itemId) {
      return itemId == null ? null : this.customItemTransforms.get(itemId.toString().toLowerCase(Locale.ROOT));
   }

   private static JsonObject parseJsonObject(String jsonText) {
      try {
         JsonElement parsed = JsonParser.parseString(jsonText);
         if (parsed != null && parsed.isJsonObject()) {
            return parsed.getAsJsonObject();
         }
      } catch (Exception var2) {
      }

      return new JsonObject();
   }

   private static String readJsonString(JsonObject json, String key, String fallback) {
      if (json != null && key != null) {
         JsonElement value = json.get(key);
         if (value == null || value.isJsonNull()) {
            return fallback;
         } else {
            if (value.isJsonPrimitive()) {
               try {
                  String asString = value.getAsString();
                  return asString == null ? fallback : asString;
               } catch (Exception var5) {
               }
            }

            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static float readJsonFloat(JsonObject json, String key, float fallback) {
      if (json != null && key != null) {
         try {
            JsonElement value = json.get(key);
            if (value != null && !value.isJsonNull()) {
               float parsed = value.getAsFloat();
               return Float.isFinite(parsed) ? parsed : fallback;
            } else {
               return fallback;
            }
         } catch (Exception var5) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static boolean readJsonBoolean(JsonObject json, String key, boolean fallback) {
      if (json != null && key != null) {
         try {
            JsonElement value = json.get(key);
            if (value == null || value.isJsonNull()) {
               return fallback;
            }

            if (value.isJsonPrimitive()) {
               if (value.getAsJsonPrimitive().isBoolean()) {
                  return value.getAsBoolean();
               }

               String asString = value.getAsString();
               if (asString != null) {
                  String normalized = asString.trim().toLowerCase(Locale.ROOT);
                  if (!"true".equals(normalized) && !"yes".equals(normalized) && !"on".equals(normalized) && !"1".equals(normalized)) {
                     if (!"false".equals(normalized) && !"no".equals(normalized) && !"off".equals(normalized) && !"0".equals(normalized)) {
                        return fallback;
                     }

                     return false;
                  }

                  return true;
               }
            }
         } catch (Exception var6) {
         }

         return fallback;
      } else {
         return fallback;
      }
   }

   private static Map<String, Float> readAnimationSpeedMultipliers(JsonObject json) {
      if (json == null) {
         return Map.of();
      } else {
         JsonElement raw = json.get("animationSpeedMultipliers");
         if (raw != null && raw.isJsonObject()) {
            Map<String, Float> result = new HashMap<>();
            JsonObject object = raw.getAsJsonObject();

            for (Entry<String, JsonElement> entry : object.entrySet()) {
               String key = entry.getKey();
               JsonElement value = entry.getValue();
               if (key != null && !key.isBlank() && value != null && !value.isJsonNull()) {
                  try {
                     float multiplier = value.getAsFloat();
                     if (Float.isFinite(multiplier)) {
                        result.put(key.trim().toLowerCase(Locale.ROOT), Math.max(0.05F, Math.min(4.0F, multiplier)));
                     }
                  } catch (Exception var9) {
                  }
               }
            }

            return result.isEmpty() ? Map.of() : Map.copyOf(result);
         } else {
            return Map.of();
         }
      }
   }

   private static Map<String, Float> sanitizeAnimationSpeedMultipliers(Map<String, Float> source) {
      if (source != null && !source.isEmpty()) {
         Map<String, Float> cleaned = new HashMap<>();

         for (Entry<String, Float> entry : source.entrySet()) {
            if (entry != null && entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               float multiplier = entry.getValue();
               if (Float.isFinite(multiplier)) {
                  cleaned.put(entry.getKey().trim().toLowerCase(Locale.ROOT), Math.max(0.05F, Math.min(4.0F, multiplier)));
               }
            }
         }

         return cleaned.isEmpty() ? Map.of() : Map.copyOf(cleaned);
      } else {
         return Map.of();
      }
   }

   private static Map<String, FbxItemTransform> readCustomItemTransforms(JsonObject json) {
      if (json == null) {
         return Map.of();
      } else {
         JsonElement raw = json.get("customItemTransforms");
         if (raw != null && raw.isJsonObject()) {
            Map<String, FbxItemTransform> out = new HashMap<>();
            JsonObject object = raw.getAsJsonObject();

            for (Entry<String, JsonElement> entry : object.entrySet()) {
               String key = entry.getKey();
               JsonElement value = entry.getValue();
               if (key != null && !key.isBlank() && value != null && value.isJsonObject()) {
                  JsonObject node = value.getAsJsonObject();
                  FbxItemTransform transform = new FbxItemTransform(
                     readJsonFloat(node, "rightOffsetX", 0.0F),
                     readJsonFloat(node, "rightOffsetY", 0.0F),
                     readJsonFloat(node, "rightOffsetZ", 0.0F),
                     readJsonFloat(node, "rightRotX", 0.0F),
                     readJsonFloat(node, "rightRotY", 0.0F),
                     readJsonFloat(node, "rightRotZ", 0.0F),
                     readJsonFloat(node, "rightScale", 1.0F),
                     readJsonFloat(node, "leftOffsetX", 0.0F),
                     readJsonFloat(node, "leftOffsetY", 0.0F),
                     readJsonFloat(node, "leftOffsetZ", 0.0F),
                     readJsonFloat(node, "leftRotX", 0.0F),
                     readJsonFloat(node, "leftRotY", 0.0F),
                     readJsonFloat(node, "leftRotZ", 0.0F),
                     readJsonFloat(node, "leftScale", 1.0F)
                  );
                  out.put(key.trim().toLowerCase(Locale.ROOT), transform);
               }
            }

            return out.isEmpty() ? Map.of() : Map.copyOf(out);
         } else {
            return Map.of();
         }
      }
   }

   private static Map<String, FbxItemTransform> sanitizeCustomItemTransforms(Map<String, FbxItemTransform> source) {
      if (source != null && !source.isEmpty()) {
         Map<String, FbxItemTransform> cleaned = new HashMap<>();

         for (Entry<String, FbxItemTransform> entry : source.entrySet()) {
            if (entry != null && entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               cleaned.put(entry.getKey().trim().toLowerCase(Locale.ROOT), entry.getValue());
            }
         }

         return cleaned.isEmpty() ? Map.of() : Map.copyOf(cleaned);
      } else {
         return Map.of();
      }
   }
}
