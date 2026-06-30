package com.brckv2.objfbxloader.client.voice;

import com.brckv2.objfbxloader.PlatformPaths;
import com.brckv2.objfbxloader.client.render.FbxRigConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

public final class VoiceLipSyncState {
   private static final Path CONFIG_PATH = PlatformPaths.gameDir().resolve("configs").resolve("objfbxloader_voice_lipsync.json");
   private static volatile boolean enabled = false;
   private static volatile float sensitivity = 8.0F;
   private static volatile boolean microphoneMuted = false;
   private static volatile float smoothedLevel = 0.0F;
   private static volatile long lastAudioSampleAtMs = 0L;
   private static volatile float lastDetectedPitchHz = 0.0F;
   private static volatile float pitchDampingFactor = 1.0F;
   private static volatile long holdUntilMs = 0L;
   private static volatile boolean initialized = false;

   private VoiceLipSyncState() {
   }

   public static synchronized void initialize() {
      if (!initialized) {
         initialized = true;
         loadConfig();
      }
   }

   public static synchronized void setSettings(boolean enabledValue, float sensitivityValue) {
      enabled = enabledValue;
      sensitivity = clampFinite(sensitivityValue, 0.5F, 64.0F, 8.0F);
      saveConfig();
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static float getSensitivity() {
      return sensitivity;
   }

   public static void onMicrophoneMuted(boolean muted) {
      microphoneMuted = muted;
      if (muted) {
         smoothedLevel = 0.0F;
         lastAudioSampleAtMs = 0L;
         lastDetectedPitchHz = 0.0F;
         pitchDampingFactor = 1.0F;
         holdUntilMs = 0L;
      }
   }

   public static void onClientAudioFrame(short[] rawAudio) {
      if (enabled && !microphoneMuted && rawAudio != null && rawAudio.length != 0) {
         double sumSq = 0.0;

         for (short sample : rawAudio) {
            float normalized = sample / 32768.0F;
            sumSq += normalized * normalized;
         }

         double rms = Math.sqrt(sumSq / rawAudio.length);
         float target = clamp01((float)rms * sensitivity);
         float detectedPitchHz = estimatePitchHz(rawAudio, 48000.0F);
         boolean voicedPitch = detectedPitchHz >= 60.0F && detectedPitchHz <= 600.0F;
         boolean activeSound = target > 0.003F;
         long now = System.currentTimeMillis();
         if (activeSound || voicedPitch) {
            holdUntilMs = Math.max(holdUntilMs, now) + 220L;
            long maxHold = now + 1800L;
            if (holdUntilMs > maxHold) {
               holdUntilMs = maxHold;
            }
         }

         if (voicedPitch) {
            if (lastDetectedPitchHz > 0.0F) {
               float delta = Math.abs(detectedPitchHz - lastDetectedPitchHz);
               float tolerance = Math.max(12.0F, lastDetectedPitchHz * 0.12F);
               float similarity = 1.0F - clamp01(delta / tolerance);
               pitchDampingFactor = 1.0F - 0.45F * similarity;
            } else {
               pitchDampingFactor = 1.0F;
            }

            lastDetectedPitchHz = detectedPitchHz;
         } else {
            pitchDampingFactor = 1.0F;
            lastDetectedPitchHz = 0.0F;
         }

         float motionTarget = target * clampFinite(pitchDampingFactor, 0.45F, 1.0F, 1.0F);
         if (now < holdUntilMs && motionTarget < 0.12F) {
            motionTarget = 0.12F;
         }

         smoothedLevel = smoothedLevel * 0.72F + motionTarget * 0.28F;
         lastAudioSampleAtMs = System.currentTimeMillis();
      }
   }

   public static Map<String, Vector3f> buildCurrentBoneOffsets(FbxRigConfig rigConfig) {
      if (!enabled) {
         return Map.of();
      } else {
         float level = currentLevel();
         return buildBoneOffsetsForLevel(rigConfig, level);
      }
   }

   public static Map<String, Vector3f> buildBoneOffsetsForLevel(FbxRigConfig rigConfig, float rawLevel) {
      if (!enabled) {
         return Map.of();
      } else {
         float level = clamp01(rawLevel);
         if (level <= 1.0E-4F) {
            return Map.of();
         } else {
            String upperBones = "";
            String lowerBones = "";
            float pitchFactor = 1.0F;
            float yawFactor = 0.0F;
            float rollFactor = 0.0F;
            float maxDegrees = 16.0F;
            if (rigConfig != null) {
               upperBones = rigConfig.voiceUpperLipBone() == null ? "" : rigConfig.voiceUpperLipBone();
               lowerBones = rigConfig.voiceLowerLipBone() == null ? "" : rigConfig.voiceLowerLipBone();
               pitchFactor = clampFinite(rigConfig.voiceLipPitchFactor(), -4.0F, 4.0F, 1.0F);
               yawFactor = clampFinite(rigConfig.voiceLipYawFactor(), -4.0F, 4.0F, 0.0F);
               rollFactor = clampFinite(rigConfig.voiceLipRollFactor(), -4.0F, 4.0F, 0.0F);
               maxDegrees = clampFinite(rigConfig.voiceLipMaxDegrees(), 0.0F, 45.0F, maxDegrees);
            }

            float clampedDegrees = maxDegrees * level;
            if (clampedDegrees <= 1.0E-4F) {
               return Map.of();
            } else {
               float pitch = clampedDegrees * pitchFactor;
               float yaw = clampedDegrees * yawFactor;
               float roll = clampedDegrees * rollFactor;
               Map<String, Vector3f> out = new HashMap<>();

               for (String bone : splitBones(upperBones)) {
                  out.put(bone, new Vector3f(-pitch, -yaw, -roll));
               }

               for (String bone : splitBones(lowerBones)) {
                  Vector3f existing = out.get(bone);
                  if (existing == null) {
                     out.put(bone, new Vector3f(pitch, yaw, roll));
                  } else {
                     existing.x += pitch;
                     existing.y += yaw;
                     existing.z += roll;
                  }
               }

               return out.isEmpty() ? Map.of() : Map.copyOf(out);
            }
         }
      }
   }

   public static float currentLipSyncLevel() {
      return enabled && !microphoneMuted ? clamp01(currentLevel()) : 0.0F;
   }

   private static float currentLevel() {
      float level = clamp01(smoothedLevel);
      long last = lastAudioSampleAtMs;
      if (last <= 0L) {
         smoothedLevel = 0.0F;
         return 0.0F;
      } else {
         long age = Math.max(0L, System.currentTimeMillis() - last);
         if (age > 220L) {
            long effectiveAge = age;
            long now = System.currentTimeMillis();
            if (now < holdUntilMs) {
               long holdRemainingMs = holdUntilMs - now;
               effectiveAge = Math.max(0L, age - holdRemainingMs);
            }

            float decay = (float)Math.exp(-Math.max(0L, effectiveAge - 220L) / 260.0);
            level *= decay;
            smoothedLevel = level;
         }

         return clamp01(level);
      }
   }

   private static float estimatePitchHz(short[] rawAudio, float sampleRate) {
      if (rawAudio != null && rawAudio.length >= 64 && Float.isFinite(sampleRate) && !(sampleRate <= 0.0F)) {
         int crossings = 0;
         int prev = rawAudio[0];

         for (int i = 1; i < rawAudio.length; i++) {
            int cur = rawAudio[i];
            if (prev <= 0 && cur > 0 || prev >= 0 && cur < 0) {
               crossings++;
            }

            prev = cur;
         }

         return crossings <= 1 ? 0.0F : crossings * sampleRate / (2.0F * rawAudio.length);
      } else {
         return 0.0F;
      }
   }

   private static synchronized void loadConfig() {
      enabled = false;
      sensitivity = 8.0F;
      if (Files.isRegularFile(CONFIG_PATH)) {
         try {
            String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            enabled = readBoolean(object, "enabled", false);
            sensitivity = clampFinite(readFloat(object, "sensitivity", 8.0F), 0.5F, 64.0F, 8.0F);
         } catch (Exception var2) {
         }
      }
   }

   private static synchronized void saveConfig() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         JsonObject object = new JsonObject();
         object.addProperty("enabled", enabled);
         object.addProperty("sensitivity", sensitivity);
         Files.writeString(CONFIG_PATH, object.toString(), StandardCharsets.UTF_8);
      } catch (IOException var1) {
      }
   }

   private static List<String> splitBones(String value) {
      if (value != null && !value.isBlank()) {
         String normalized = value.replace('\n', '|').replace('\r', '|');
         String[] raw = normalized.split("[|,;]");
         LinkedHashSet<String> unique = new LinkedHashSet<>();

         for (String part : raw) {
            if (part != null) {
               String trimmed = part.trim();
               if (!trimmed.isBlank()) {
                  unique.add(trimmed);
               }
            }
         }

         return unique.isEmpty() ? List.of() : List.copyOf(unique);
      } else {
         return List.of();
      }
   }

   private static float clamp01(float value) {
      if (!Float.isFinite(value)) {
         return 0.0F;
      } else {
         return value < 0.0F ? 0.0F : Math.min(1.0F, value);
      }
   }

   private static float clampFinite(float value, float min, float max, float fallback) {
      if (!Float.isFinite(value)) {
         return fallback;
      } else {
         return value < min ? min : Math.min(max, value);
      }
   }

   private static String readString(JsonObject object, String key, String fallback) {
      if (object != null && key != null && object.has(key)) {
         try {
            String value = object.get(key).getAsString();
            return value == null ? fallback : value;
         } catch (Exception var4) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static float readFloat(JsonObject object, String key, float fallback) {
      if (object != null && key != null && object.has(key)) {
         try {
            float value = object.get(key).getAsFloat();
            return Float.isFinite(value) ? value : fallback;
         } catch (Exception var4) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static boolean readBoolean(JsonObject object, String key, boolean fallback) {
      if (object != null && key != null && object.has(key)) {
         try {
            return object.get(key).getAsBoolean();
         } catch (Exception var4) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }
}
