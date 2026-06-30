package com.brckv2.objfbxloader;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PlatformPaths {
   private PlatformPaths() {
   }

   public static Path gameDir() {
      return resolveViaFabricLoader("getGameDir", Paths.get(".").toAbsolutePath().normalize());
   }

   public static Path configDir() {
      return resolveViaFabricLoader("getConfigDir", gameDir().resolve("config"));
   }

   private static Path resolveViaFabricLoader(String methodName, Path fallback) {
      try {
         Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
         Object instance = loaderClass.getMethod("getInstance").invoke(null);
         if (loaderClass.getMethod(methodName).invoke(instance) instanceof Path resolved) {
            return resolved;
         }
      } catch (Throwable var6) {
      }

      return fallback;
   }
}
