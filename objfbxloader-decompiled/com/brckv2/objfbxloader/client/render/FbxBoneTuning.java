package com.brckv2.objfbxloader.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3f;

public final class FbxBoneTuning {
   private static final Map<String, Vector3f> ROTATION_OFFSETS = new ConcurrentHashMap<>();
   private static final Set<String> KNOWN_BONES = ConcurrentHashMap.newKeySet();

   private FbxBoneTuning() {
   }

   public static void registerBone(String boneName) {
      if (boneName != null && !boneName.isBlank()) {
         KNOWN_BONES.add(boneName);
      }
   }

   public static List<String> getKnownBones() {
      List<String> bones = new ArrayList<>(KNOWN_BONES);
      bones.sort(String::compareToIgnoreCase);
      return bones;
   }

   public static Vector3f getRotationOffset(String boneName) {
      Vector3f stored = ROTATION_OFFSETS.get(boneName);
      return stored == null ? new Vector3f() : new Vector3f(stored);
   }

   public static void addRotationOffset(String boneName, float x, float y, float z) {
      if (boneName != null && !boneName.isBlank()) {
         ROTATION_OFFSETS.compute(boneName, (key, current) -> {
            Vector3f value = current == null ? new Vector3f() : new Vector3f(current);
            value.add(x, y, z);
            return value;
         });
      }
   }

   public static void resetBone(String boneName) {
      if (boneName != null && !boneName.isBlank()) {
         ROTATION_OFFSETS.remove(boneName);
      }
   }

   public static void resetAll() {
      ROTATION_OFFSETS.clear();
   }
}
