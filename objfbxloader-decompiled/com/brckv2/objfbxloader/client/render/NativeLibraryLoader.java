package com.brckv2.objfbxloader.client.render;

import com.brckv2.objfbxloader.ObjFbxLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NativeLibraryLoader {
   private static boolean assimpLoaded = false;
   private static boolean animationLoadAttempted = false;
   private static boolean animationLoaded = false;
   private static Path nativeTempDir;

   private NativeLibraryLoader() {
   }

   public static synchronized void loadAssimpNatives() {
      if (!assimpLoaded) {
         try {
            Path tempDir = ensureNativeTempDir();
            if (isAndroid()) {
               String abi = detectAndroidAbi();
               if (abi == null) {
                  ObjFbxLoader.LOGGER.info("Unsupported Android ABI for bundled Assimp natives; relying on system/LWJGL loading.");
                  assimpLoaded = true;
                  return;
               }

               Path dracoPath = extractFirstOptional(tempDir, "libdraco.so", "/natives/android/" + abi + "/libdraco.so", "/natives/android/libdraco.so");
               Path assimpPath = extractFirstOptional(tempDir, "libassimp.so", "/natives/android/" + abi + "/libassimp.so", "/natives/android/libassimp.so");
               if (dracoPath != null) {
                  System.load(dracoPath.toAbsolutePath().toString());
               }

               if (assimpPath != null) {
                  System.load(assimpPath.toAbsolutePath().toString());
               } else {
                  ObjFbxLoader.LOGGER.info("No bundled Android assimp native found in /natives/android/" + abi + "; relying on system/LWJGL loading.");
               }
            } else if (isWindows()) {
               extract(tempDir, "/natives/windows/draco.dll", "draco.dll");
               extract(tempDir, "/natives/windows/assimp.dll", "assimp.dll");
               System.load(tempDir.resolve("draco.dll").toAbsolutePath().toString());
               System.load(tempDir.resolve("assimp.dll").toAbsolutePath().toString());
            } else if (isLinux()) {
               Path dracoPathx = extractOptional(tempDir, "/natives/linux/libdraco.so", "libdraco.so");
               Path assimpPathx = extractOptional(tempDir, "/natives/linux/libassimp.so", "libassimp.so");
               if (dracoPathx != null) {
                  System.load(dracoPathx.toAbsolutePath().toString());
               }

               if (assimpPathx != null) {
                  System.load(assimpPathx.toAbsolutePath().toString());
               } else {
                  ObjFbxLoader.LOGGER.info("No bundled Linux assimp native found in /natives/linux; relying on system/LWJGL loading.");
               }
            } else {
               ObjFbxLoader.LOGGER.info("Unsupported OS for bundled Assimp natives; relying on system/LWJGL loading.");
            }

            assimpLoaded = true;
         } catch (IOException var4) {
            throw new RuntimeException("Failed to load Assimp natives.", var4);
         } catch (Throwable var5) {
            throw new RuntimeException("Failed to load platform native dependencies.", var5);
         }
      }
   }

   public static synchronized boolean loadAnimationNative() {
      if (animationLoadAttempted) {
         return animationLoaded;
      } else {
         animationLoadAttempted = true;
         String resourcePath;
         String fileName;
         if (isAndroid()) {
            String abi = detectAndroidAbi();
            if (abi == null) {
               return false;
            }

            fileName = "libfbxanim_native.so";
            resourcePath = "/natives/android/" + abi + "/libfbxanim_native.so";
         } else if (isWindows()) {
            resourcePath = "/natives/windows/fbxanim_native.dll";
            fileName = "fbxanim_native.dll";
         } else {
            if (!isLinux()) {
               return false;
            }

            resourcePath = "/natives/linux/libfbxanim_native.so";
            fileName = "libfbxanim_native.so";
         }

         try {
            Path tempDir = ensureNativeTempDir();
            Path libraryPath;
            if (isAndroid()) {
               String abi = detectAndroidAbi();
               if (abi == null) {
                  return false;
               }

               libraryPath = extractFirstOptional(
                  tempDir, fileName, "/natives/android/" + abi + "/libfbxanim_native.so", "/natives/android/libfbxanim_native.so"
               );
            } else {
               libraryPath = extractOptional(tempDir, resourcePath, fileName);
            }

            if (libraryPath == null) {
               return false;
            } else {
               System.load(libraryPath.toAbsolutePath().toString());
               animationLoaded = true;
               return true;
            }
         } catch (Throwable var5) {
            ObjFbxLoader.LOGGER.warn("Could not load native animation backend library. Falling back to Java skinning.", var5);
            animationLoaded = false;
            return false;
         }
      }
   }

   private static boolean isWindows() {
      String os = System.getProperty("os.name", "").toLowerCase();
      return os.contains("win");
   }

   private static boolean isLinux() {
      String os = System.getProperty("os.name", "").toLowerCase();
      return os.contains("linux");
   }

   private static boolean isAndroid() {
      String runtimeName = System.getProperty("java.runtime.name", "").toLowerCase();
      String vmName = System.getProperty("java.vm.name", "").toLowerCase();
      String vendor = System.getProperty("java.vendor", "").toLowerCase();
      String home = System.getProperty("user.home", "").toLowerCase();
      if (!runtimeName.contains("android")
         && !vmName.contains("dalvik")
         && !vendor.contains("android")
         && !home.contains("/storage/emulated")
         && !home.contains("/data/user/")) {
         try {
            Class.forName("android.os.Build", false, NativeLibraryLoader.class.getClassLoader());
            return true;
         } catch (Throwable var5) {
            return false;
         }
      } else {
         return true;
      }
   }

   private static String detectAndroidAbi() {
      String arch = System.getProperty("os.arch", "").toLowerCase();
      if (arch.contains("aarch64") || arch.contains("arm64")) {
         return "arm64-v8a";
      } else if (arch.contains("arm")) {
         return "armeabi-v7a";
      } else if (arch.contains("x86_64") || arch.contains("amd64")) {
         return "x86_64";
      } else {
         return arch.contains("x86") ? "x86" : null;
      }
   }

   private static Path ensureNativeTempDir() throws IOException {
      if (nativeTempDir != null) {
         return nativeTempDir;
      } else {
         nativeTempDir = Files.createTempDirectory("com.brckv2.objfbxloader-natives");
         nativeTempDir.toFile().deleteOnExit();
         return nativeTempDir;
      }
   }

   private static void extract(Path dir, String resourcePath, String fileName) throws IOException {
      try (InputStream in = NativeLibraryLoader.class.getResourceAsStream(resourcePath)) {
         if (in == null) {
            throw new IOException("Missing native resource: " + resourcePath);
         }

         Path out = dir.resolve(fileName);
         Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
         out.toFile().deleteOnExit();
      }
   }

   private static Path extractOptional(Path dir, String resourcePath, String fileName) throws IOException {
      Path var5;
      try (InputStream in = NativeLibraryLoader.class.getResourceAsStream(resourcePath)) {
         if (in == null) {
            return null;
         }

         Path out = dir.resolve(fileName);
         Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
         out.toFile().deleteOnExit();
         var5 = out;
      }

      return var5;
   }

   private static Path extractFirstOptional(Path dir, String fileName, String... resourcePaths) throws IOException {
      if (resourcePaths != null && resourcePaths.length != 0) {
         for (String resourcePath : resourcePaths) {
            if (resourcePath != null && !resourcePath.isBlank()) {
               Path extracted = extractOptional(dir, resourcePath, fileName);
               if (extracted != null) {
                  return extracted;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
