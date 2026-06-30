package com.brckv2.objfbxloader.forge;

import com.brckv2.objfbxloader.ObjFbxLoader;
import com.brckv2.objfbxloader.client.gui.RigEditorRootScreen;
import com.brckv2.objfbxloader.client.render.ResourcePackObjBlockModel;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import org.lwjgl.glfw.GLFW;

final class ObjFbxLoaderNeoClientBootstrap {
   private static final class_2960 DEFAULT_MODEL = class_2960.method_60656("player");
   private static final long EMPTY_RETRY_INTERVAL_MS = 1500L;
   private static final long STABLE_RECHECK_INTERVAL_MS = 120000L;
   private static volatile boolean started;
   private static volatile class_2960 localFallbackModelTarget = DEFAULT_MODEL;
   private static boolean openRigEditorKeyDown;

   private ObjFbxLoaderNeoClientBootstrap() {
   }

   static void initialize() {
      if (started) {
         ObjFbxLoader.LOGGER.info("NeoForge client bootstrap already running.");
      } else {
         started = true;
         ObjFbxLoader.LOGGER.info("Launching NeoForge client bootstrap worker.");
         Thread worker = new Thread(ObjFbxLoaderNeoClientBootstrap::runLoop, "objfbxloader-neoforge-client");
         worker.setDaemon(true);
         worker.start();
      }
   }

   private static void runLoop() {
      class_3300 lastResourceManager = null;
      String lastPackSignature = "";
      long lastLoadAttemptAt = 0L;
      int lastLoadedEntityCount = -1;

      while (true) {
         try {
            class_310 client = class_310.method_1551();
            if (client != null) {
               class_3300 current = client.method_1478();
               long now = System.currentTimeMillis();
               String packSignature = buildPackSignature(client, current);
               boolean managerChanged = current != null && current != lastResourceManager;
               boolean packSelectionChanged = !packSignature.equals(lastPackSignature);
               long minInterval = lastLoadedEntityCount <= 0 ? 1500L : 120000L;
               boolean timedRetry = current != null && now - lastLoadAttemptAt >= minInterval;
               if (current != null && (managerChanged || packSelectionChanged || timedRetry)) {
                  lastResourceManager = current;
                  lastPackSignature = packSignature;
                  lastLoadAttemptAt = now;

                  try {
                     ResourcePackObjLivingOverrides.OverrideData data = ResourcePackObjLivingOverrides.load(current);
                     lastLoadedEntityCount = data.entityModels().size();
                     ResourcePackObjLivingOverrides.setData(data);
                     List<class_2960> targets = new ArrayList<>(data.entityModels().keySet());
                     targets.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));
                     boolean hasPlayerTarget = data.entityModels().containsKey(DEFAULT_MODEL);
                     localFallbackModelTarget = hasPlayerTarget ? DEFAULT_MODEL : (targets.isEmpty() ? DEFAULT_MODEL : targets.get(0));
                     ObjFbxLoader.LOGGER
                        .info(
                           "NeoForge bootstrap loaded {} entity override(s) from active resource packs. hasPlayerTarget={} localFallbackModelTarget={} targets={}",
                           new Object[]{lastLoadedEntityCount, hasPlayerTarget, localFallbackModelTarget, targets}
                        );
                  } catch (Throwable var19) {
                     ObjFbxLoader.LOGGER.error("Failed loading resource-pack entity overrides on NeoForge.", var19);
                  }
               }

               handleOpenRigEditorHotkey(client);
               if (client.field_1724 != null) {
                  int entityId = client.field_1724.method_5628();
                  ResourcePackObjLivingOverrides.setLocalPlayerEntityId(entityId);
                  ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(entityId, localFallbackModelTarget, client.field_1724.method_5477().getString());
               } else {
                  ResourcePackObjLivingOverrides.setLocalPlayerEntityId(Integer.MIN_VALUE);
                  openRigEditorKeyDown = false;
               }

               ResourcePackObjBlockModel.tick(client);
            }
         } catch (Throwable var20) {
            ObjFbxLoader.LOGGER.debug("NeoForge client bootstrap tick failed", var20);
         }

         try {
            Thread.sleep(50L);
         } catch (InterruptedException var18) {
            Thread.currentThread().interrupt();
            return;
         }
      }
   }

   private static void handleOpenRigEditorHotkey(class_310 client) {
      if (client != null && client.method_22683() != null) {
         long window = 0L;

         try {
            window = client.method_22683().method_4490();
         } catch (Throwable var4) {
            window = resolveWindowHandle(client.method_22683());
         }

         boolean isDown = window != 0L && GLFW.glfwGetKey(window, 79) == 1;
         if (isDown && !openRigEditorKeyDown && client.field_1755 == null && client.field_1724 != null) {
            client.execute(() -> {
               if (client.field_1755 == null && client.field_1724 != null) {
                  client.method_1507(new RigEditorRootScreen(null));
               }
            });
         }

         openRigEditorKeyDown = isDown;
      } else {
         openRigEditorKeyDown = false;
      }
   }

   private static long resolveWindowHandle(Object windowObj) {
      if (windowObj == null) {
         return 0L;
      } else {
         for (String methodName : new String[]{"getHandle", "getWindow", "window", "getWindowHandle"}) {
            try {
               Method method = windowObj.getClass().getMethod(methodName);
               method.setAccessible(true);
               if (method.invoke(windowObj) instanceof Number number) {
                  return number.longValue();
               }
            } catch (Throwable var9) {
            }
         }

         for (String fieldName : new String[]{"handle", "window"}) {
            try {
               Field field = windowObj.getClass().getDeclaredField(fieldName);
               field.setAccessible(true);
               if (field.get(windowObj) instanceof Number number) {
                  return number.longValue();
               }
            } catch (Throwable var8) {
            }
         }

         return 0L;
      }
   }

   private static String buildPackSignature(class_310 client, class_3300 manager) {
      StringBuilder builder = new StringBuilder();
      builder.append("rm=").append(manager == null ? "null" : Integer.toHexString(System.identityHashCode(manager)));
      builder.append("|repoSelected=").append(String.join(",", readSelectedPackIdsFromRepository(client)));
      List<String> selected = readStringListField(client.field_1690, "resourcePacks");
      List<String> incompatible = readStringListField(client.field_1690, "incompatibleResourcePacks");
      builder.append("|selected=").append(String.join(",", selected));
      builder.append("|incompatible=").append(String.join(",", incompatible));
      return builder.toString();
   }

   private static List<String> readSelectedPackIdsFromRepository(class_310 client) {
      if (client == null) {
         return List.of();
      } else {
         try {
            Object repository = invokeNoArgs(client, "getResourcePackRepository");
            if (repository == null) {
               repository = invokeNoArgs(client, "getResourcePackManager");
            }

            if (repository == null) {
               return List.of();
            }

            if (invokeNoArgs(repository, "getSelectedIds") instanceof Collection<?> collection) {
               List<String> out = new ArrayList<>();

               for (Object entry : collection) {
                  if (entry != null) {
                     out.add(String.valueOf(entry));
                  }
               }

               Collections.sort(out, String::compareToIgnoreCase);
               return out;
            }
         } catch (Throwable var7) {
         }

         return List.of();
      }
   }

   private static Object invokeNoArgs(Object target, String methodName) {
      if (target != null && methodName != null && !methodName.isBlank()) {
         try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static List<String> readStringListField(Object target, String fieldName) {
      if (target != null && fieldName != null && !fieldName.isBlank()) {
         try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.get(target) instanceof Collection<?> collection) {
               List<String> out = new ArrayList<>();

               for (Object entry : collection) {
                  if (entry != null) {
                     out.add(String.valueOf(entry));
                  }
               }

               return out;
            } else {
               return List.of();
            }
         } catch (Throwable var8) {
            return List.of();
         }
      } else {
         return List.of();
      }
   }
}
