package com.brckv2.objfbxloader.client.render;

import com.brckv2.objfbxloader.ObjFbxLoader;
import com.brckv2.objfbxloader.client.voice.VoiceLipSyncState;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_10034;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1011;
import net.minecraft.class_10426;
import net.minecraft.class_1043;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_5352;
import net.minecraft.class_638;
import net.minecraft.class_7367;
import net.minecraft.class_7677;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_9224;
import net.minecraft.class_3262.class_7664;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class ResourcePackObjLivingOverrides {
   private static final String IDLE_ANIMATION = "idle";
   private static final String WALK_ANIMATION = "walk";
   private static final String BACK_WALK_ANIMATION = "backwalk";
   private static final String BACK_WALK_ALT_ANIMATION = "back_walk";
   private static final String SPRINT_ANIMATION = "sprint";
   private static final String RUN_JUMP_ANIMATION = "run_jump";
   private static final String JUMP_ANIMATION = "jump";
   private static final String FALL_ANIMATION = "fall";
   private static final String ATTACK_ANIMATION = "attack";
   private static final String SNEAK_ANIMATION = "sneak";
   private static final String SNEAK_WALK_ANIMATION = "sneak_walk";
   private static final String SIT_ANIMATION = "sit";
   private static final String HURT_ANIMATION = "hurt";
   private static final String DEATH_ANIMATION = "death";
   private static final String CLIMB_ANIMATION = "climb";
   private static final String SWIM_ANIMATION = "swim";
   private static final String GLIDE_ANIMATION = "glide";
   private static final String RAGDOLL_ALIAS_TARGET = "ragdoll";
   private static final float ATTACK_SWING_TRIGGER = 0.08F;
   private static final float WALK_THRESHOLD = 0.08F;
   private static final float BACK_WALK_SPEED_THRESHOLD = 0.02F;
   private static final float BACK_WALK_ENTER_THRESHOLD = -0.09F;
   private static final float BACK_WALK_EXIT_THRESHOLD = 0.03F;
   private static final float SPRINT_THRESHOLD = 0.22F;
   private static final float AIR_UP_SPEED_THRESHOLD = 0.24F;
   private static final float AIR_DOWN_SPEED_THRESHOLD = -0.24F;
   private static final float AIR_UP_CONFIRM_PREVIOUS_SPEED_THRESHOLD = 0.1F;
   private static final float AIRBORNE_DESCENT_SPEED_THRESHOLD = -0.03F;
   private static final float AIRBORNE_MAX_HOLD_TICKS = 18.0F;
   private static final float RUN_JUMP_HORIZONTAL_THRESHOLD = 0.14F;
   private static final float JUMP_PLAYBACK_RATE = 1.4F;
   private static final float FALL_PLAYBACK_RATE = 1.2F;
   private static final float PLAYER_STANDING_HEIGHT_SCALE = 1.95F;
   private static final float MODEL_FORWARD_CENTER_OFFSET = 0.075F;
   private static final float CLIMB_VERTICAL_THRESHOLD = 0.03F;
   private static final float CLIMB_VERTICAL_MAX = 0.28F;
   private static final float CLIMB_HORIZONTAL_MAX = 0.05F;
   private static final float CLIMB_LIMB_AMPLITUDE_MIN = 0.02F;
   private static final float TICKS_PER_SECOND = 20.0F;
   private static final float HELD_ITEM_RENDER_SCALE = 0.45F;
   private static final float DEFAULT_WALK_SPEED_BPS = 4.317F;
   private static final float DEFAULT_SPRINT_SPEED_BPS = 5.612F;
   private static final float DEFAULT_SNEAK_SPEED_BPS = 1.295F;
   private static final float BASE_WALK_ANIM_RATE = 0.84F;
   private static final float BASE_BACK_WALK_ANIM_RATE = 0.84F;
   private static final float BASE_SPRINT_ANIM_RATE = 1.08F;
   private static final float BASE_SNEAK_WALK_ANIM_RATE = 0.7F;
   private static final class_2960 RAGDOLL_MODEL_ENTITY_ID = class_2960.method_60656("armor_stand");
   private static final int RAGDOLL_MAX_ACTIVE = 64;
   private static final int RAGDOLL_RENDER_LIGHT = 15728880;
   private static final float RAGDOLL_GRAVITY_PER_TICK = 0.08F;
   private static final float RAGDOLL_AIR_DRAG = 0.985F;
   private static final float RAGDOLL_GROUND_DRAG = 0.9F;
   private static final float RAGDOLL_COLLISION_RESTITUTION = 0.0F;
   private static final float RAGDOLL_FORCE_DAMPING = 0.92F;
   private static final float RAGDOLL_IMPACT_FORCE_SCALE = 0.22F;
   private static final float RAGDOLL_CONTACT_FORCE_SCALE = 0.035F;
   private static final float RAGDOLL_EXTERNAL_FORCE_MAX = 0.55F;
   private static final int RAGDOLL_COLLISION_SUBSTEPS = 4;
   private static final int RAGDOLL_COLLISION_SOLVER_ITERATIONS = 3;
   private static final double RAGDOLL_COLLISION_MAX_PUSH_PER_ITER = 0.24;
   private static final double RAGDOLL_COLLISION_CONTACT_SLOP = 0.001;
   private static final double RAGDOLL_PLAYER_PUSH_MAX = 0.1;
   private static final float RAGDOLL_PLAYER_PUSH_VELOCITY_SCALE = 0.22F;
   private static final ResourcePackObjLivingOverrides.RagdollCollisionProbe[] RAGDOLL_COLLISION_PROBES = new ResourcePackObjLivingOverrides.RagdollCollisionProbe[]{
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(0.0F, 1.65F, 0.0F, 0.14F, 0.95F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(0.0F, 1.35F, 0.0F, 0.18F, 1.0F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(0.0F, 1.0F, 0.0F, 0.2F, 1.0F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(-0.3F, 1.08F, 0.0F, 0.1F, 0.8F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(0.3F, 1.08F, 0.0F, 0.1F, 0.8F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(-0.14F, 0.56F, 0.02F, 0.11F, 0.85F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(0.14F, 0.56F, 0.02F, 0.11F, 0.85F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(-0.16F, 0.19F, 0.07F, 0.11F, 0.9F),
      new ResourcePackObjLivingOverrides.RagdollCollisionProbe(0.16F, 0.19F, 0.07F, 0.11F, 0.9F)
   };
   private static final float ANIMATION_BLEND_TICKS = 3.0F;
   private static final AtomicLong STATS_RUNTIME_FRAME_CACHE_HITS = new AtomicLong();
   private static final AtomicLong STATS_RUNTIME_FRAME_CACHE_MISSES = new AtomicLong();
   private static final int MAX_REMOTE_BUNDLE_BYTES = 100663296;
   private static final String REMOTE_BUNDLE_PACK_ID = "objfbxloader-remote-sync";
   private static Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> baseEntityModels = Map.of();
   private static Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> entityModels = Map.of();
   private static Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> armorModels = Map.of();
   private static final Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> downloadedEntityModels = new HashMap<>();
   private static final Set<class_2960> downloadedTextureIds = new LinkedHashSet<>();
   private static final Map<class_2960, FbxRigConfig> rigConfigByTargetId = new HashMap<>();
   private static final Map<Integer, class_2960> networkPlayerModelTargetByEntityId = new HashMap<>();
   private static final Map<Integer, String> networkPlayerNameByEntityId = new HashMap<>();
   private static final Map<Integer, ResourcePackObjLivingOverrides.NetworkLipSyncState> networkLipSyncByEntityId = new HashMap<>();
   private static final Map<String, class_2960> downloadedVariantByPlayerName = new HashMap<>();
   private static final Set<Integer> loggedRemotePlayerAppliedIds = new LinkedHashSet<>();
   private static final Set<Integer> loggedMissingPlayerRenderStateIds = new LinkedHashSet<>();
   private static int localPlayerEntityId = Integer.MIN_VALUE;
   private static boolean multiplayerPlayerModelsEnabled = true;
   private static boolean othersModeledEnabled = false;
   private static final Map<class_10042, Integer> rememberedEntityIdByRenderState = new WeakHashMap<>();
   private static final Map<class_10042, Boolean> rememberedSittingByRenderState = new WeakHashMap<>();
   private static final Map<Class<?>, Field> MOTION_STATE_ID_FIELD_CACHE = new HashMap<>();
   private static final Map<Class<?>, Field> SITTING_STATE_FLAG_FIELD_CACHE = new HashMap<>();
   private static final Map<Class<?>, Method> ENTITY_SITTING_METHOD_CACHE = new HashMap<>();
   private static final Map<Integer, ResourcePackObjLivingOverrides.MotionSample> motionSamplesByEntityId = new HashMap<>();
   private static final Map<Integer, ResourcePackObjLivingOverrides.MotionSampleDelta> motionDeltasByEntityId = new HashMap<>();
   private static final Map<Integer, Float> previousVerticalSpeedByEntityId = new HashMap<>();
   private static final Map<Integer, ResourcePackObjLivingOverrides.AirborneJumpArcState> airborneJumpAnimationByEntityId = new HashMap<>();
   private static final Map<Integer, Boolean> backwardLocomotionByEntityId = new HashMap<>();
   private static final Map<Integer, ResourcePackObjLivingOverrides.LocomotionPhaseState> locomotionPhaseByEntityId = new HashMap<>();
   private static final Map<Integer, ResourcePackObjLivingOverrides.OneShotPlaybackState> oneShotPlaybackByEntityId = new HashMap<>();
   private static final Map<Integer, Boolean> sittingByEntityId = new HashMap<>();
   private static final List<ResourcePackObjLivingOverrides.LocalRagdollInstance> localRagdolls = new ArrayList<>();
   private static int nextLocalRagdollId = 1;
   private static class_638 ragdollWorldRef = null;

   private ResourcePackObjLivingOverrides() {
   }

   public static void setData(ResourcePackObjLivingOverrides.OverrideData data) {
      baseEntityModels = data.entityModels();
      rebuildEntityModelIndex();
      armorModels = data.armorModels();
      rigConfigByTargetId.entrySet().removeIf(entry -> {
         class_2960 id = entry.getKey();
         String namespace = id == null ? null : id.method_12836();
         return namespace == null || !namespace.startsWith("objfbxloader_remote_");
      });
      sittingByEntityId.clear();
      synchronized (networkPlayerModelTargetByEntityId) {
         networkPlayerModelTargetByEntityId.entrySet().removeIf(entry -> entry.getValue() != null && !entityModels.containsKey(entry.getValue()));
      }

      synchronized (rememberedSittingByRenderState) {
         rememberedSittingByRenderState.clear();
      }
   }

   private static void rebuildEntityModelIndex() {
      Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> merged = new HashMap<>(baseEntityModels);
      merged.putAll(downloadedEntityModels);
      entityModels = Map.copyOf(merged);
   }

   public static Set<class_2960> getResolvedTextureIdsForModel(class_2960 modelId) {
      return getResolvedTextureIdsForModel(modelId, false);
   }

   public static Set<class_2960> getLoadedEntityModelIds() {
      return Set.copyOf(entityModels.keySet());
   }

   public static Set<class_2960> getResolvedIdleTextureIdsForModel(class_2960 modelId) {
      return getResolvedTextureIdsForModel(modelId, true);
   }

   private static Set<class_2960> getResolvedTextureIdsForModel(class_2960 modelId, boolean idleOnly) {
      if (modelId == null) {
         return Set.of();
      } else {
         ResourcePackObjLivingOverrides.AnimatedObjModel model = entityModels.get(modelId);
         if (model == null) {
            return Set.of();
         } else {
            Set<class_2960> textures = new LinkedHashSet<>();
            collectTexturesFromObjModel(model.staticModel, textures);
            if (idleOnly) {
               List<ResourcePackObjLivingOverrides.ObjModel> idleFrames = model.animations.get("idle");
               if (idleFrames != null) {
                  for (ResourcePackObjLivingOverrides.ObjModel frame : idleFrames) {
                     collectTexturesFromObjModel(frame, textures);
                  }
               }

               collectTexturesFromRuntimeSample(model.runtimeAnimations.get("idle"), textures);
            } else {
               for (List<ResourcePackObjLivingOverrides.ObjModel> frames : model.animations.values()) {
                  if (frames != null) {
                     for (ResourcePackObjLivingOverrides.ObjModel frame : frames) {
                        collectTexturesFromObjModel(frame, textures);
                     }
                  }
               }

               for (ResourcePackMeshLoader.AnimatedMeshData runtime : model.runtimeAnimations.values()) {
                  collectTexturesFromRuntimeSample(runtime, textures);
               }
            }

            textures.removeIf(id -> id == null);
            return Set.copyOf(textures);
         }
      }
   }

   private static void collectTexturesFromRuntimeSample(ResourcePackMeshLoader.AnimatedMeshData runtime, Set<class_2960> textures) {
      if (runtime != null && textures != null) {
         try {
            ResourcePackMeshLoader.MeshData sample = runtime.sampleFrame(0.0F, 0.0F, 0.0F, false, "", "");
            if (sample == null) {
               return;
            }

            if (sample.texture() != null) {
               textures.add(sample.texture());
            }

            if (sample.triangles() != null) {
               for (ResourcePackMeshLoader.MeshTriangle triangle : sample.triangles()) {
                  if (triangle != null && triangle.texture() != null) {
                     textures.add(triangle.texture());
                  }
               }
            }
         } catch (Throwable var5) {
         }
      }
   }

   private static void collectTexturesFromObjModel(ResourcePackObjLivingOverrides.ObjModel model, Set<class_2960> textures) {
      if (model != null && textures != null) {
         if (model.texture() != null) {
            textures.add(model.texture());
         }

         if (model.triangles() != null) {
            for (ResourcePackMeshLoader.MeshTriangle triangle : model.triangles()) {
               if (triangle != null && triangle.texture() != null) {
                  textures.add(triangle.texture());
               }
            }
         }
      }
   }

   public static void setNetworkPlayerModelTarget(int entityId, class_2960 targetId) {
      setNetworkPlayerModelTarget(entityId, targetId, null);
   }

   public static void setNetworkPlayerModelTarget(int entityId, class_2960 targetId, String playerName) {
      if (entityId != Integer.MIN_VALUE) {
         synchronized (networkPlayerModelTargetByEntityId) {
            if (targetId == null) {
               networkPlayerModelTargetByEntityId.remove(entityId);
               synchronized (networkPlayerNameByEntityId) {
                  networkPlayerNameByEntityId.remove(entityId);
               }
            } else {
               networkPlayerModelTargetByEntityId.put(entityId, targetId);
               synchronized (networkPlayerNameByEntityId) {
                  if (playerName != null && !playerName.isBlank()) {
                     networkPlayerNameByEntityId.put(entityId, playerName.trim().toLowerCase(Locale.ROOT));
                  } else {
                     networkPlayerNameByEntityId.remove(entityId);
                  }
               }
            }
         }
      }
   }

   public static void clearNetworkPlayerModelTarget(int entityId) {
      if (entityId != Integer.MIN_VALUE) {
         synchronized (networkPlayerModelTargetByEntityId) {
            networkPlayerModelTargetByEntityId.remove(entityId);
         }

         synchronized (networkPlayerNameByEntityId) {
            networkPlayerNameByEntityId.remove(entityId);
         }

         synchronized (networkLipSyncByEntityId) {
            networkLipSyncByEntityId.remove(entityId);
         }
      }
   }

   public static void clearAllNetworkPlayerModelTargets() {
      synchronized (networkPlayerModelTargetByEntityId) {
         networkPlayerModelTargetByEntityId.clear();
      }

      synchronized (networkPlayerNameByEntityId) {
         networkPlayerNameByEntityId.clear();
      }

      synchronized (networkLipSyncByEntityId) {
         networkLipSyncByEntityId.clear();
      }

      synchronized (loggedRemotePlayerAppliedIds) {
         loggedRemotePlayerAppliedIds.clear();
      }

      synchronized (loggedMissingPlayerRenderStateIds) {
         loggedMissingPlayerRenderStateIds.clear();
      }

      synchronized (downloadedVariantByPlayerName) {
         downloadedVariantByPlayerName.clear();
      }

      synchronized (downloadedEntityModels) {
         downloadedEntityModels.clear();
      }

      rigConfigByTargetId.entrySet().removeIf(entry -> {
         class_2960 id = entry.getKey();
         return id != null && id.method_12836() != null && id.method_12836().startsWith("objfbxloader_remote_");
      });
      rebuildEntityModelIndex();
      clearDownloadedTextures();
   }

   public static void setNetworkPlayerLipSyncLevel(int entityId, float level) {
      if (entityId != Integer.MIN_VALUE) {
         float safe = Math.max(0.0F, Math.min(1.0F, Float.isFinite(level) ? level : 0.0F));
         synchronized (networkLipSyncByEntityId) {
            networkLipSyncByEntityId.put(entityId, new ResourcePackObjLivingOverrides.NetworkLipSyncState(safe, System.currentTimeMillis()));
         }
      }
   }

   public static float getNetworkPlayerLipSyncLevel(int entityId) {
      synchronized (networkLipSyncByEntityId) {
         ResourcePackObjLivingOverrides.NetworkLipSyncState state = networkLipSyncByEntityId.get(entityId);
         if (state == null) {
            return 0.0F;
         } else {
            long ageMs = Math.max(0L, System.currentTimeMillis() - state.updatedAtMs());
            if (ageMs > 500L) {
               float decay = (float)Math.exp(-(ageMs - 500L) / 350.0);
               return Math.max(0.0F, Math.min(1.0F, state.level() * decay));
            } else {
               return state.level();
            }
         }
      }
   }

   public static boolean installRemotePlayerModelBundle(String playerName, byte[] bundleZipBytes) {
      if (playerName != null && !playerName.isBlank() && bundleZipBytes != null && bundleZipBytes.length != 0) {
         if (bundleZipBytes.length > 100663296) {
            ObjFbxLoader.LOGGER.warn("Ignoring oversized remote model bundle for player {} ({} bytes).", playerName, bundleZipBytes.length);
            return false;
         } else {
            String sanitized = playerName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
            if (sanitized.isBlank()) {
               return false;
            } else {
               Map<class_2960, byte[]> rawResources = decodeBundleResources(bundleZipBytes, sanitized);
               if (rawResources.isEmpty()) {
                  ObjFbxLoader.LOGGER.warn("Remote model bundle for {} did not contain any usable resources.", playerName);
                  return false;
               } else {
                  registerDownloadedTextures(rawResources, playerName);
                  class_310 client = class_310.method_1551();
                  class_3300 fallbackManager = client == null ? null : client.method_1478();
                  class_3300 manager = new ResourcePackObjLivingOverrides.InMemoryResourceManager(rawResources, fallbackManager);
                  Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> loaded = mergeModelMaps(
                     mergeModelMaps(
                        loadModels(manager, "models/player", ResourcePackObjLivingOverrides.ResourceKind.PLAYER_VARIANT),
                        loadModels(manager, "models/players", ResourcePackObjLivingOverrides.ResourceKind.PLAYER_VARIANT)
                     ),
                     mergeModelMaps(
                        loadModels(manager, "models/entity", ResourcePackObjLivingOverrides.ResourceKind.PLAYER_VARIANT),
                        loadModels(manager, "models/entities", ResourcePackObjLivingOverrides.ResourceKind.PLAYER_VARIANT)
                     )
                  );
                  if (loaded.isEmpty()) {
                     ObjFbxLoader.LOGGER.warn("Remote model bundle for {} did not produce a loadable model.", playerName);
                     return false;
                  } else {
                     class_2960 sourceId = loaded.containsKey(class_2960.method_60656("player"))
                        ? class_2960.method_60656("player")
                        : loaded.keySet().iterator().next();
                     ResourcePackObjLivingOverrides.AnimatedObjModel model = loaded.get(sourceId);
                     if (model == null) {
                        return false;
                     } else {
                        class_2960 targetId = sourceId;
                        synchronized (downloadedEntityModels) {
                           downloadedEntityModels.put(targetId, model);
                        }

                        synchronized (downloadedVariantByPlayerName) {
                           downloadedVariantByPlayerName.put(sanitized, targetId);
                        }

                        FbxRigConfig syncedConfig = FbxRigConfig.load(manager, sourceId.method_12836(), sourceId.method_12832());
                        rigConfigByTargetId.put(sourceId, syncedConfig == null ? FbxRigConfig.EMPTY : syncedConfig);
                        rebuildEntityModelIndex();
                        synchronized (loggedMissingPlayerRenderStateIds) {
                           loggedMissingPlayerRenderStateIds.clear();
                        }

                        ObjFbxLoader.LOGGER.info("Installed remote player model bundle for {} as targetId={}.", playerName, sourceId);
                        return true;
                     }
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   private static void registerDownloadedTextures(Map<class_2960, byte[]> rawResources, String playerName) {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1531() != null && rawResources != null && !rawResources.isEmpty()) {
         int registered = 0;

         for (Entry<class_2960, byte[]> entry : rawResources.entrySet()) {
            class_2960 id = entry.getKey();
            byte[] bytes = entry.getValue();
            if (isTextureResource(id) && bytes != null && bytes.length != 0) {
               try {
                  class_1011 image = class_1011.method_49277(bytes);
                  client.method_1531().method_4616(id, new class_1043(() -> "objfbxloader-downloaded", image));
                  synchronized (downloadedTextureIds) {
                     downloadedTextureIds.add(id);
                  }

                  registered++;
               } catch (Exception var12) {
                  ObjFbxLoader.LOGGER.warn("Failed to register downloaded texture {} for synced player {}.", new Object[]{id, playerName, var12});
               }
            }
         }

         if (registered > 0) {
            ObjFbxLoader.LOGGER.info("Registered {} downloaded texture(s) for synced player model bundle: {}", registered, playerName);
         }
      }
   }

   private static void clearDownloadedTextures() {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1531() != null) {
         List<class_2960> ids;
         synchronized (downloadedTextureIds) {
            if (downloadedTextureIds.isEmpty()) {
               return;
            }

            ids = new ArrayList<>(downloadedTextureIds);
            downloadedTextureIds.clear();
         }

         for (class_2960 id : ids) {
            try {
               client.method_1531().method_4615(id);
            } catch (Exception var5) {
            }
         }
      } else {
         synchronized (downloadedTextureIds) {
            downloadedTextureIds.clear();
         }
      }
   }

   private static boolean isTextureResource(class_2960 id) {
      if (id == null) {
         return false;
      } else {
         String path = id.method_12832().toLowerCase(Locale.ROOT);
         return !path.startsWith("textures/") && !path.startsWith("embedded/")
            ? false
            : path.endsWith(".png")
               || path.endsWith(".jpg")
               || path.endsWith(".jpeg")
               || path.endsWith(".tga")
               || path.endsWith(".webp")
               || path.endsWith(".bmp")
               || path.endsWith(".gif");
      }
   }

   private static Map<class_2960, byte[]> decodeBundleResources(byte[] bundleZipBytes, String sanitizedPlayerName) {
      Map<class_2960, byte[]> out = new HashMap<>();
      String remappedNamespace = buildRemoteBundleNamespace(sanitizedPlayerName);

      try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bundleZipBytes))) {
         while (true) {
            ZipEntry entry = zip.getNextEntry();
            if (entry == null) {
               return out;
            }

            if (!entry.isDirectory()) {
               String name = entry.getName();
               if (name != null) {
                  String normalized = name.replace('\\', '/');
                  int assetsIndex = normalized.indexOf("assets/");
                  if (assetsIndex >= 0) {
                     normalized = normalized.substring(assetsIndex);
                     String[] parts = normalized.split("/", 3);
                     if (parts.length >= 3) {
                        String namespace = parts[1];
                        String path = parts[2];
                        if (!namespace.isBlank() && !path.isBlank()) {
                           class_2960 id = class_2960.method_43902(namespace, path);
                           if (id != null) {
                              class_2960 remapped = class_2960.method_43902(remappedNamespace, id.method_12832());
                              if (remapped != null) {
                                 byte[] bytes = zip.readAllBytes();
                                 if (bytes.length != 0) {
                                    out.put(remapped, bytes);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (IOException var17) {
         ObjFbxLoader.LOGGER.warn("Failed to decode remote model bundle.", var17);
         return Map.of();
      }
   }

   private static String buildRemoteBundleNamespace(String sanitizedPlayerName) {
      String cleaned = sanitizedPlayerName == null ? "" : sanitizedPlayerName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
      if (cleaned.isBlank()) {
         cleaned = "unknown";
      }

      return "objfbxloader_remote_" + cleaned;
   }

   public static boolean spawnLocalRagdollFromPlayer(class_1657 player) {
      return false;
   }

   public static int clearLocalRagdolls() {
      int cleared = localRagdolls.size();
      localRagdolls.clear();
      return cleared;
   }

   public static void tickLocalRagdolls(class_310 client) {
      localRagdolls.clear();
   }

   public static void renderLocalRagdolls(WorldRenderContext context) {
   }

   private static ResourcePackObjLivingOverrides.AnimatedObjModel resolveRagdollModel() {
      ResourcePackObjLivingOverrides.AnimatedObjModel ragdoll = entityModels.get(RAGDOLL_MODEL_ENTITY_ID);
      return ragdoll != null ? ragdoll : entityModels.get(class_2960.method_60656("player"));
   }

   private static ResourcePackMeshLoader.AnimatedMeshData resolveRagdollRuntimeAnimation(ResourcePackObjLivingOverrides.AnimatedObjModel model) {
      if (model != null && !model.runtimeAnimations.isEmpty()) {
         ResourcePackMeshLoader.AnimatedMeshData idle = model.runtimeAnimations.get("idle");
         if (idle != null) {
            return idle;
         } else {
            for (ResourcePackMeshLoader.AnimatedMeshData runtime : model.runtimeAnimations.values()) {
               if (runtime != null) {
                  return runtime;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static void clearLocalRagdollsInternal() {
      ResourcePackObjLivingOverrides.AnimatedObjModel ragdollModel = resolveRagdollModel();
      if (ragdollModel != null) {
         for (ResourcePackMeshLoader.AnimatedMeshData animation : ragdollModel.runtimeAnimations.values()) {
            if (animation != null) {
               animation.clearAllRagdollStates();
            }
         }
      }

      localRagdolls.clear();
   }

   private static void clearRagdollStateForId(ResourcePackObjLivingOverrides.AnimatedObjModel model, int ragdollId) {
      if (model != null) {
         for (ResourcePackMeshLoader.AnimatedMeshData animation : model.runtimeAnimations.values()) {
            if (animation != null) {
               animation.clearRagdollState(ragdollId);
            }
         }
      }
   }

   private static void simulateBlockCollision(class_310 client, class_638 world, ResourcePackObjLivingOverrides.LocalRagdollInstance ragdoll) {
      double stepDx = ragdoll.velocityX / 20.0F / 4.0F;
      double stepDy = ragdoll.velocityY / 20.0F / 4.0F;
      double stepDz = ragdoll.velocityZ / 20.0F / 4.0F;
      float impactX = Math.abs(ragdoll.velocityX);
      float impactY = Math.abs(ragdoll.velocityY);
      float impactZ = Math.abs(ragdoll.velocityZ);
      boolean blockedX = false;
      boolean blockedY = false;
      boolean blockedZ = false;
      boolean grounded = false;
      float normalX = 0.0F;
      float normalY = 0.0F;
      float normalZ = 0.0F;

      for (int step = 0; step < 4; step++) {
         double beforeX = ragdoll.x;
         double beforeY = ragdoll.y;
         double beforeZ = ragdoll.z;
         double targetX = beforeX + stepDx;
         double targetY = beforeY + stepDy;
         double targetZ = beforeZ + stepDz;
         ResourcePackObjLivingOverrides.CollisionResolution resolved = resolveRagdollProbeCollisions(world, targetX, targetY, targetZ);
         ragdoll.x = resolved.x();
         ragdoll.y = resolved.y();
         ragdoll.z = resolved.z();
         grounded |= resolved.grounded();
         double appliedDx = ragdoll.x - beforeX;
         double appliedDy = ragdoll.y - beforeY;
         double appliedDz = ragdoll.z - beforeZ;
         if (Math.abs(stepDx - appliedDx) > 1.0E-5) {
            blockedX = true;
         }

         if (Math.abs(stepDy - appliedDy) > 1.0E-5) {
            blockedY = true;
         }

         if (Math.abs(stepDz - appliedDz) > 1.0E-5) {
            blockedZ = true;
         }

         normalX += resolved.normalX();
         normalY += resolved.normalY();
         normalZ += resolved.normalZ();
      }

      if (blockedX) {
         float hitNormalX = Math.abs(normalX) > 1.0E-5F ? Math.signum(normalX) : (ragdoll.velocityX > 0.0F ? -1.0F : 1.0F);
         ragdoll.velocityX = -ragdoll.velocityX * 0.0F;
         ragdoll.externalForceX += hitNormalX * (impactX * 0.22F);
      }

      if (blockedY) {
         if (ragdoll.velocityY < 0.0F) {
            grounded = true;
            ragdoll.externalForceY += impactY * 0.044F;
         } else if (ragdoll.velocityY > 0.0F) {
            ragdoll.externalForceY -= impactY * 0.033F;
         }

         ragdoll.velocityY = 0.0F;
      }

      if (blockedZ) {
         float hitNormalZ = Math.abs(normalZ) > 1.0E-5F ? Math.signum(normalZ) : (ragdoll.velocityZ > 0.0F ? -1.0F : 1.0F);
         ragdoll.velocityZ = -ragdoll.velocityZ * 0.0F;
         ragdoll.externalForceZ += hitNormalZ * (impactZ * 0.22F);
      }

      float normalLenSq = normalX * normalX + normalY * normalY + normalZ * normalZ;
      if (normalLenSq > 1.0E-6F) {
         float normalLen = (float)Math.sqrt(normalLenSq);
         float invLen = normalLen <= 1.0E-6F ? 0.0F : 1.0F / normalLen;
         ragdoll.externalForceX += normalX * invLen * 0.035F;
         ragdoll.externalForceY += normalY * invLen * 0.0035F;
         ragdoll.externalForceZ += normalZ * invLen * 0.035F;
      }

      if (grounded || normalY > 0.25F) {
         ragdoll.velocityX *= 0.9F;
         ragdoll.velocityZ *= 0.9F;
         if (Math.abs(ragdoll.velocityY) < 0.01F) {
            ragdoll.velocityY = 0.0F;
         }
      }

      resolvePlayerCollision(client, world, ragdoll);
      ragdoll.externalForceX = clamp(ragdoll.externalForceX, -0.55F, 0.55F);
      ragdoll.externalForceY = clamp(ragdoll.externalForceY, -0.55F, 0.55F);
      ragdoll.externalForceZ = clamp(ragdoll.externalForceZ, -0.55F, 0.55F);
   }

   private static ResourcePackObjLivingOverrides.CollisionResolution resolveRagdollProbeCollisions(
      class_638 world, double targetX, double targetY, double targetZ
   ) {
      double x = targetX;
      double y = targetY;
      double z = targetZ;
      float totalNormalX = 0.0F;
      float totalNormalY = 0.0F;
      float totalNormalZ = 0.0F;
      boolean grounded = false;

      for (int iter = 0; iter < 3; iter++) {
         double correctionX = 0.0;
         double correctionY = 0.0;
         double correctionZ = 0.0;
         boolean anyCollision = false;

         for (ResourcePackObjLivingOverrides.RagdollCollisionProbe probe : RAGDOLL_COLLISION_PROBES) {
            ResourcePackObjLivingOverrides.ProbeCollisionResult contact = solveProbeWorldCollision(
               world, x + probe.offsetX(), y + probe.offsetY(), z + probe.offsetZ(), probe.radius()
            );
            if (contact != null) {
               anyCollision = true;
               double weight = probe.responseWeight();
               correctionX += contact.pushX() * weight;
               correctionY += contact.pushY() * weight;
               correctionZ += contact.pushZ() * weight;
               totalNormalX += (float)(contact.normalX() * weight);
               totalNormalY += (float)(contact.normalY() * weight);
               totalNormalZ += (float)(contact.normalZ() * weight);
               if (contact.normalY() > 0.45) {
                  grounded = true;
               }
            }
         }

         if (!anyCollision) {
            break;
         }

         double correctionLenSq = correctionX * correctionX + correctionY * correctionY + correctionZ * correctionZ;
         if (correctionLenSq > 0.0576) {
            double scale = 0.24 / Math.sqrt(correctionLenSq);
            correctionX *= scale;
            correctionY *= scale;
            correctionZ *= scale;
         }

         x += correctionX;
         y += correctionY;
         z += correctionZ;
         if (Math.abs(correctionX) < 1.0E-6 && Math.abs(correctionY) < 1.0E-6 && Math.abs(correctionZ) < 1.0E-6) {
            break;
         }
      }

      return new ResourcePackObjLivingOverrides.CollisionResolution(x, y, z, totalNormalX, totalNormalY, totalNormalZ, grounded);
   }

   private static ResourcePackObjLivingOverrides.ProbeCollisionResult solveProbeWorldCollision(
      class_638 world, double centerX, double centerY, double centerZ, double radius
   ) {
      class_238 probeBox = new class_238(centerX - radius, centerY - radius, centerZ - radius, centerX + radius, centerY + radius, centerZ + radius);
      class_2338 min = class_2338.method_49637(probeBox.field_1323, probeBox.field_1322, probeBox.field_1321);
      class_2338 max = class_2338.method_49637(probeBox.field_1320, probeBox.field_1325, probeBox.field_1324);
      double pushX = 0.0;
      double pushY = 0.0;
      double pushZ = 0.0;
      double normalX = 0.0;
      double normalY = 0.0;
      double normalZ = 0.0;
      int contacts = 0;
      double radiusSq = radius * radius;

      for (int bx = min.method_10263(); bx <= max.method_10263(); bx++) {
         for (int by = min.method_10264(); by <= max.method_10264(); by++) {
            for (int bz = min.method_10260(); bz <= max.method_10260(); bz++) {
               class_2338 pos = new class_2338(bx, by, bz);
               class_2680 state = world.method_8320(pos);
               if (state != null && !state.method_26215()) {
                  class_265 shape = state.method_26220(world, pos);
                  if (shape != null && !shape.method_1110()) {
                     for (class_238 shapeBox : shape.method_1090()) {
                        class_238 worldBox = shapeBox.method_996(pos);
                        double nearestX = clampDouble(centerX, worldBox.field_1323, worldBox.field_1320);
                        double nearestY = clampDouble(centerY, worldBox.field_1322, worldBox.field_1325);
                        double nearestZ = clampDouble(centerZ, worldBox.field_1321, worldBox.field_1324);
                        double deltaX = centerX - nearestX;
                        double deltaY = centerY - nearestY;
                        double deltaZ = centerZ - nearestZ;
                        double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                        if (!(distanceSq >= radiusSq)) {
                           double distance = Math.sqrt(distanceSq);
                           double nx;
                           double ny;
                           double nz;
                           if (distance > 1.0E-6) {
                              double invDistance = 1.0 / distance;
                              nx = deltaX * invDistance;
                              ny = deltaY * invDistance;
                              nz = deltaZ * invDistance;
                           } else {
                              Vector3f axis = pickBestEjectionNormal(centerX, centerY, centerZ, worldBox);
                              nx = axis.x();
                              ny = axis.y();
                              nz = axis.z();
                           }

                           double penetration = radius - distance + 0.001;
                           pushX += nx * penetration;
                           pushY += ny * penetration;
                           pushZ += nz * penetration;
                           normalX += nx;
                           normalY += ny;
                           normalZ += nz;
                           contacts++;
                        }
                     }
                  }
               }
            }
         }
      }

      if (contacts <= 0) {
         return null;
      } else {
         double invContacts = 1.0 / contacts;
         return new ResourcePackObjLivingOverrides.ProbeCollisionResult(
            pushX * invContacts, pushY * invContacts, pushZ * invContacts, normalX * invContacts, normalY * invContacts, normalZ * invContacts
         );
      }
   }

   private static ResourcePackObjLivingOverrides.ProbeCollisionResult solveProbeBoxCollision(
      double centerX, double centerY, double centerZ, double radius, class_238 worldBox
   ) {
      double nearestX = clampDouble(centerX, worldBox.field_1323, worldBox.field_1320);
      double nearestY = clampDouble(centerY, worldBox.field_1322, worldBox.field_1325);
      double nearestZ = clampDouble(centerZ, worldBox.field_1321, worldBox.field_1324);
      double deltaX = centerX - nearestX;
      double deltaY = centerY - nearestY;
      double deltaZ = centerZ - nearestZ;
      double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
      double radiusSq = radius * radius;
      if (distanceSq >= radiusSq) {
         return null;
      } else {
         double distance = Math.sqrt(distanceSq);
         double nx;
         double ny;
         double nz;
         if (distance > 1.0E-6) {
            double invDistance = 1.0 / distance;
            nx = deltaX * invDistance;
            ny = deltaY * invDistance;
            nz = deltaZ * invDistance;
         } else {
            Vector3f axis = pickBestEjectionNormal(centerX, centerY, centerZ, worldBox);
            nx = axis.x();
            ny = axis.y();
            nz = axis.z();
         }

         double penetration = radius - distance + 0.001;
         return new ResourcePackObjLivingOverrides.ProbeCollisionResult(nx * penetration, ny * penetration, nz * penetration, nx, ny, nz);
      }
   }

   private static void resolvePlayerCollision(class_310 client, class_638 world, ResourcePackObjLivingOverrides.LocalRagdollInstance ragdoll) {
      if (client != null && client.field_1724 != null) {
         class_238 playerBox = client.field_1724.method_5829();
         if (playerBox != null) {
            double pushX = 0.0;
            double pushY = 0.0;
            double pushZ = 0.0;
            double normalX = 0.0;
            double normalY = 0.0;
            double normalZ = 0.0;
            int contacts = 0;

            for (ResourcePackObjLivingOverrides.RagdollCollisionProbe probe : RAGDOLL_COLLISION_PROBES) {
               ResourcePackObjLivingOverrides.ProbeCollisionResult contact = solveProbeBoxCollision(
                  ragdoll.x + probe.offsetX(), ragdoll.y + probe.offsetY(), ragdoll.z + probe.offsetZ(), probe.radius(), playerBox
               );
               if (contact != null) {
                  double weight = probe.responseWeight();
                  pushX += contact.pushX() * weight;
                  pushY += contact.pushY() * weight;
                  pushZ += contact.pushZ() * weight;
                  normalX += contact.normalX() * weight;
                  normalY += contact.normalY() * weight;
                  normalZ += contact.normalZ() * weight;
                  contacts++;
               }
            }

            if (contacts > 0) {
               double invContacts = 1.0 / contacts;
               pushX *= invContacts;
               pushY = 0.0;
               pushZ *= invContacts;
               double pushLenSq = pushX * pushX + pushY * pushY + pushZ * pushZ;
               if (pushLenSq > 0.010000000000000002) {
                  double scale = 0.1 / Math.sqrt(pushLenSq);
                  pushX *= scale;
                  pushY *= scale;
                  pushZ *= scale;
               }

               ragdoll.x += pushX;
               ragdoll.y += pushY;
               ragdoll.z += pushZ;
               float pushVelX = (float)(pushX * 20.0 * 0.22F);
               float pushVelZ = (float)(pushZ * 20.0 * 0.22F);
               ragdoll.velocityX = clamp(ragdoll.velocityX + pushVelX, -0.65F, 0.65F);
               ragdoll.velocityZ = clamp(ragdoll.velocityZ + pushVelZ, -0.65F, 0.65F);
               float nX = (float)(normalX * invContacts);
               float nZ = (float)(normalZ * invContacts);
               ragdoll.externalForceX += nX * 0.026250001F;
               ragdoll.externalForceZ += nZ * 0.026250001F;
               ResourcePackObjLivingOverrides.CollisionResolution worldResolved = resolveRagdollProbeCollisions(world, ragdoll.x, ragdoll.y, ragdoll.z);
               ragdoll.x = worldResolved.x();
               ragdoll.y = worldResolved.y();
               ragdoll.z = worldResolved.z();
            }
         }
      }
   }

   private static Vector3f pickBestEjectionNormal(double centerX, double centerY, double centerZ, class_238 box) {
      double left = Math.abs(centerX - box.field_1323);
      double right = Math.abs(box.field_1320 - centerX);
      double down = Math.abs(centerY - box.field_1322);
      double up = Math.abs(box.field_1325 - centerY);
      double back = Math.abs(centerZ - box.field_1321);
      double front = Math.abs(box.field_1324 - centerZ);
      double min = left;
      Vector3f normal = new Vector3f(-1.0F, 0.0F, 0.0F);
      if (right < left) {
         min = right;
         normal.set(1.0F, 0.0F, 0.0F);
      }

      if (down < min) {
         min = down;
         normal.set(0.0F, -1.0F, 0.0F);
      }

      if (up < min) {
         min = up;
         normal.set(0.0F, 1.0F, 0.0F);
      }

      if (back < min) {
         min = back;
         normal.set(0.0F, 0.0F, -1.0F);
      }

      if (front < min) {
         normal.set(0.0F, 0.0F, 1.0F);
      }

      return normal;
   }

   private static double clampDouble(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public static void setAnimationCachingEnabled(boolean enabled) {
   }

   public static boolean isAnimationCachingEnabled() {
      return false;
   }

   public static String cacheStatsSummary() {
      int runtimeFrameCacheSize = 0;

      for (ResourcePackObjLivingOverrides.AnimatedObjModel model : entityModels.values()) {
         if (model != null) {
            runtimeFrameCacheSize += model.runtimeFrameCacheSize();
         }
      }

      for (ResourcePackObjLivingOverrides.AnimatedObjModel modelx : armorModels.values()) {
         if (modelx != null) {
            runtimeFrameCacheSize += modelx.runtimeFrameCacheSize();
         }
      }

      return "LivingOverrides cache stats: objCache=off, frameHit="
         + STATS_RUNTIME_FRAME_CACHE_HITS.get()
         + ", frameMiss="
         + STATS_RUNTIME_FRAME_CACHE_MISSES.get()
         + ", frameCacheSize="
         + runtimeFrameCacheSize
         + ", caching=false";
   }

   public static void resetCacheStats() {
      STATS_RUNTIME_FRAME_CACHE_HITS.set(0L);
      STATS_RUNTIME_FRAME_CACHE_MISSES.set(0L);
   }

   public static void setLocalPlayerEntityId(int entityId) {
      localPlayerEntityId = entityId;
      if (entityId == Integer.MIN_VALUE) {
         motionSamplesByEntityId.clear();
         motionDeltasByEntityId.clear();
         previousVerticalSpeedByEntityId.clear();
         backwardLocomotionByEntityId.clear();
         locomotionPhaseByEntityId.clear();
         oneShotPlaybackByEntityId.clear();
         sittingByEntityId.clear();
         synchronized (rememberedSittingByRenderState) {
            rememberedSittingByRenderState.clear();
         }
      }
   }

   public static void setMultiplayerPlayerModelsEnabled(boolean enabled) {
      multiplayerPlayerModelsEnabled = enabled;
   }

   public static boolean isMultiplayerPlayerModelsEnabled() {
      return multiplayerPlayerModelsEnabled;
   }

   public static void setOthersModeledEnabled(boolean enabled) {
      othersModeledEnabled = enabled;
   }

   public static boolean isOthersModeledEnabled() {
      return othersModeledEnabled;
   }

   public static void rememberRenderStateEntityId(class_10042 state, int entityId) {
      if (state != null) {
         if (state instanceof RenderStateEntityIdAccessor accessor) {
            accessor.objfbxloader$setEntityId(entityId);
         }

         synchronized (rememberedEntityIdByRenderState) {
            rememberedEntityIdByRenderState.put(state, entityId);
         }
      }
   }

   public static void rememberEntitySittingState(class_1309 entity, class_10042 state) {
      if (entity != null && state != null) {
         int entityId = entity.method_5628();
         rememberRenderStateEntityId(state, entityId);
         boolean sitting = detectEntitySitting(entity, state);
         sittingByEntityId.put(entityId, sitting);
         synchronized (rememberedSittingByRenderState) {
            rememberedSittingByRenderState.put(state, sitting);
         }
      }
   }

   private static Integer resolveRememberedRenderStateEntityId(class_10042 state) {
      if (state == null) {
         return null;
      } else {
         if (state instanceof RenderStateEntityIdAccessor accessor) {
            int direct = accessor.objfbxloader$getEntityId();
            if (direct != Integer.MIN_VALUE) {
               return direct;
            }
         }

         synchronized (rememberedEntityIdByRenderState) {
            return rememberedEntityIdByRenderState.get(state);
         }
      }
   }

   private static Integer resolveMotionEntityId(class_10042 state) {
      if (state == null) {
         return null;
      } else if (state instanceof class_10055 playerState) {
         return playerState.field_53528;
      } else {
         Integer remembered = resolveRememberedRenderStateEntityId(state);
         return remembered != null ? remembered : tryReflectRenderStateEntityId(state);
      }
   }

   private static Integer tryReflectRenderStateEntityId(class_10042 state) {
      if (state == null) {
         return null;
      } else {
         Class<?> type = state.getClass();
         Field field;
         synchronized (MOTION_STATE_ID_FIELD_CACHE) {
            if (MOTION_STATE_ID_FIELD_CACHE.containsKey(type)) {
               field = MOTION_STATE_ID_FIELD_CACHE.get(type);
            } else {
               field = findRenderStateIdField(type);
               MOTION_STATE_ID_FIELD_CACHE.put(type, field);
            }
         }

         if (field == null) {
            return null;
         } else {
            try {
               return field.getInt(state);
            } catch (Throwable var5) {
               return null;
            }
         }
      }
   }

   private static Field findRenderStateIdField(Class<?> type) {
      for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
         try {
            Field field = current.getDeclaredField("id");
            field.setAccessible(true);
            if (field.getType() == int.class) {
               return field;
            }
         } catch (Throwable var4) {
         }

         try {
            Field field = current.getDeclaredField("entityId");
            field.setAccessible(true);
            if (field.getType() == int.class) {
               return field;
            }
         } catch (Throwable var3) {
         }
      }

      return null;
   }

   private static boolean isSittingState(class_10042 state) {
      if (state == null) {
         return false;
      } else {
         synchronized (rememberedSittingByRenderState) {
            Boolean remembered = rememberedSittingByRenderState.get(state);
            if (Boolean.TRUE.equals(remembered)) {
               return true;
            }
         }

         Integer stateEntityId = resolveMotionEntityId(state);
         if (stateEntityId != null && Boolean.TRUE.equals(sittingByEntityId.get(stateEntityId))) {
            return true;
         } else if (state.field_53465 != null && "SITTING".equals(state.field_53465.name())) {
            return true;
         } else {
            Boolean reflected = tryReflectSittingFlag(state);
            return reflected != null && reflected;
         }
      }
   }

   private static boolean detectEntitySitting(class_1309 entity, class_10042 state) {
      if (entity == null) {
         return false;
      } else if (entity.method_5765()) {
         return true;
      } else if (entity instanceof class_1321 tameable && tameable.method_6172()) {
         return true;
      } else if (state != null && state.field_53465 != null && "SITTING".equals(state.field_53465.name())) {
         return true;
      } else if (entity.method_18376() != null && "SITTING".equals(entity.method_18376().name())) {
         return true;
      } else {
         Boolean reflectedMethod = tryInvokeEntitySittingMethod(entity);
         if (reflectedMethod != null) {
            return reflectedMethod;
         } else {
            Boolean reflectedField = tryReadEntitySittingField(entity);
            return reflectedField != null && reflectedField;
         }
      }
   }

   private static Boolean tryInvokeEntitySittingMethod(class_1309 entity) {
      Class<?> type = entity.getClass();
      Method method;
      synchronized (ENTITY_SITTING_METHOD_CACHE) {
         if (ENTITY_SITTING_METHOD_CACHE.containsKey(type)) {
            method = ENTITY_SITTING_METHOD_CACHE.get(type);
         } else {
            method = findEntitySittingMethod(type);
            ENTITY_SITTING_METHOD_CACHE.put(type, method);
         }
      }

      if (method == null) {
         return null;
      } else {
         try {
            return method.invoke(entity) instanceof Boolean bool ? bool : null;
         } catch (Throwable var5) {
            return null;
         }
      }
   }

   private static Method findEntitySittingMethod(Class<?> type) {
      for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
         for (Method method : current.getDeclaredMethods()) {
            if (method != null && method.getParameterCount() == 0 && method.getReturnType() == boolean.class) {
               String name = method.getName();
               if (name != null) {
                  String normalized = name.toLowerCase(Locale.ROOT);
                  if (normalized.equals("issitting")
                     || normalized.equals("isinsittingpose")
                     || normalized.equals("issittingpose")
                     || normalized.equals("isorderedtosit")) {
                     try {
                        method.setAccessible(true);
                        return method;
                     } catch (Throwable var9) {
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static Boolean tryReadEntitySittingField(class_1309 entity) {
      for (Class<?> current = entity.getClass(); current != null && current != Object.class; current = current.getSuperclass()) {
         for (Field field : current.getDeclaredFields()) {
            if (field != null && field.getType() == boolean.class) {
               String name = field.getName();
               if (name != null) {
                  String normalized = name.toLowerCase(Locale.ROOT);
                  if (normalized.contains("sitting") || normalized.contains("orderedtosit")) {
                     try {
                        field.setAccessible(true);
                        return field.getBoolean(entity);
                     } catch (Throwable var9) {
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static Boolean tryReflectSittingFlag(class_10042 state) {
      if (state == null) {
         return null;
      } else {
         Class<?> type = state.getClass();
         Field field;
         synchronized (SITTING_STATE_FLAG_FIELD_CACHE) {
            if (SITTING_STATE_FLAG_FIELD_CACHE.containsKey(type)) {
               field = SITTING_STATE_FLAG_FIELD_CACHE.get(type);
            } else {
               field = findSittingFlagField(type);
               SITTING_STATE_FLAG_FIELD_CACHE.put(type, field);
            }
         }

         if (field == null) {
            return null;
         } else {
            try {
               return field.getBoolean(state);
            } catch (Throwable var5) {
               return null;
            }
         }
      }
   }

   private static Field findSittingFlagField(Class<?> type) {
      for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
         Field[] fields = current.getDeclaredFields();

         for (Field field : fields) {
            if (field != null && field.getType() == boolean.class) {
               String name = field.getName();
               if (name != null) {
                  String normalized = name.toLowerCase(Locale.ROOT);
                  if (normalized.contains("sitting")) {
                     try {
                        field.setAccessible(true);
                        return field;
                     } catch (Throwable var10) {
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   public static ResourcePackObjLivingOverrides.OverrideData load(class_3300 resourceManager) {
      Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> entity = mergeModelMaps(
         loadModels(resourceManager, "models/entity", ResourcePackObjLivingOverrides.ResourceKind.ENTITY),
         loadModels(resourceManager, "models/entities", ResourcePackObjLivingOverrides.ResourceKind.ENTITY)
      );
      entity = mergeModelMaps(
         entity,
         mergeModelMaps(
            loadModels(resourceManager, "models/player", ResourcePackObjLivingOverrides.ResourceKind.PLAYER_VARIANT),
            loadModels(resourceManager, "models/players", ResourcePackObjLivingOverrides.ResourceKind.PLAYER_VARIANT)
         )
      );
      return new ResourcePackObjLivingOverrides.OverrideData(
         entity, loadModels(resourceManager, "models/armor", ResourcePackObjLivingOverrides.ResourceKind.ARMOR)
      );
   }

   private static Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> mergeModelMaps(
      Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> primary, Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> secondary
   ) {
      if (primary.isEmpty()) {
         return secondary;
      } else if (secondary.isEmpty()) {
         return primary;
      } else {
         Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> merged = new HashMap<>(primary);
         merged.putAll(secondary);
         return Map.copyOf(merged);
      }
   }

   public static boolean hasEntityOverride(class_10042 state) {
      if (!shouldRenderOverrideForState(state)) {
         return false;
      } else {
         class_2960 entityId = getStateTargetId(state);
         if (entityId == null) {
            return false;
         } else {
            ResourcePackObjLivingOverrides.AnimatedObjModel model = entityModels.get(entityId);
            return model != null && model.hasRenderableContent();
         }
      }
   }

   public static boolean isLocalPlayerState(class_10042 state) {
      if (!(state instanceof class_10055)) {
         return false;
      } else {
         Integer renderEntityId = resolvePlayerRenderEntityId(state);
         return renderEntityId != null && renderEntityId == localPlayerEntityId;
      }
   }

   public static void renderEntityOverride(class_10042 state, class_4587 matrices, class_11659 queue) {
      if (shouldRenderOverrideForState(state)) {
         class_2960 entityId = getStateTargetId(state);
         ResourcePackObjLivingOverrides.AnimatedObjModel model = entityId == null ? null : entityModels.get(entityId);
         if (model != null && model.hasRenderableContent()) {
            matrices.method_22903();
            applyModelSpaceTransform(state, matrices, 1.0F, state.field_53330);
            model.render(state, matrices, queue, state.field_61820);
            matrices.method_22909();
         }
      }
   }

   public static void renderArmorOverrides(class_10034 state, class_4587 matrices, class_11659 queue) {
      if (shouldRenderOverrideForState(state)) {
         renderArmorStack(state, state.field_55309, matrices, queue, state.field_61820, 1.01F, state.field_53330);
         renderArmorStack(state, state.field_53418, matrices, queue, state.field_61820, 1.03F, state.field_53330);
         renderArmorStack(state, state.field_53419, matrices, queue, state.field_61820, 1.02F, state.field_53330);
         renderArmorStack(state, state.field_53420, matrices, queue, state.field_61820, 1.015F, state.field_53330);
      }
   }

   public static boolean renderHeldItemOverrides(class_10426 state, class_4587 matrices, class_11659 queue, int light) {
      if (!hasHeldItemOverride(state)) {
         return false;
      } else {
         class_2960 entityId = getStateTargetId(state);
         ResourcePackObjLivingOverrides.AnimatedObjModel model = entityId == null ? null : entityModels.get(entityId);
         if (model == null) {
            return false;
         } else {
            boolean renderedRight = renderHeldItemForArm(state, model, class_1306.field_6183, state.field_55305, matrices, queue, light);
            boolean renderedLeft = renderHeldItemForArm(state, model, class_1306.field_6182, state.field_55307, matrices, queue, light);
            return renderedRight || renderedLeft;
         }
      }
   }

   public static boolean renderHeldItemOverrideForArm(class_10426 state, class_1306 arm, class_4587 matrices, class_11659 queue, int light) {
      if (state != null && arm != null && hasHeldItemOverride(state)) {
         class_2960 entityId = getStateTargetId(state);
         ResourcePackObjLivingOverrides.AnimatedObjModel model = entityId == null ? null : entityModels.get(entityId);
         if (model == null) {
            return false;
         } else {
            class_10444 itemState = arm == class_1306.field_6182 ? state.field_55307 : state.field_55305;
            return renderHeldItemForArm(state, model, arm, itemState, matrices, queue, light);
         }
      } else {
         return false;
      }
   }

   public static boolean shouldSuppressVanillaHeldItemForArm(class_10426 state, class_1306 arm) {
      if (state != null && arm != null && shouldRenderOverrideForState(state)) {
         FbxRigConfig config = resolveRigConfigForState(state);
         if (config != null && !config.thirdPersonItemVisible() && isLocalPlayerState(state)) {
            class_310 client = class_310.method_1551();
            if (client != null && client.field_1690 != null && !client.field_1690.method_31044().method_31034()) {
               class_10444 itemState = arm == class_1306.field_6182 ? state.field_55307 : state.field_55305;
               return itemState != null && !itemState.method_65606();
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean hasHeldItemOverride(class_10426 state) {
      if (state != null && shouldRenderOverrideForState(state)) {
         FbxRigConfig config = resolveRigConfigForState(state);
         if (!shouldRenderAttachedItemsForState(state, config)) {
            return false;
         } else {
            class_2960 entityId = getStateTargetId(state);
            ResourcePackObjLivingOverrides.AnimatedObjModel model = entityId == null ? null : entityModels.get(entityId);
            if (model == null) {
               return false;
            } else {
               boolean rightHasItem = state.field_55305 != null && !state.field_55305.method_65606();
               boolean leftHasItem = state.field_55307 != null && !state.field_55307.method_65606();
               if (!rightHasItem && !leftHasItem) {
                  return false;
               } else {
                  return rightHasItem && model.resolveItemAnchorTransform(state, class_1306.field_6183) == null
                     ? false
                     : !leftHasItem || model.resolveItemAnchorTransform(state, class_1306.field_6182) != null;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static boolean renderHeldItemForArm(
      class_10426 state,
      ResourcePackObjLivingOverrides.AnimatedObjModel model,
      class_1306 arm,
      class_10444 itemState,
      class_4587 matrices,
      class_11659 queue,
      int light
   ) {
      if (itemState != null && !itemState.method_65606()) {
         FbxRigConfig config = resolveRigConfigForState(state);
         if (!shouldRenderAttachedItemsForState(state, config)) {
            return false;
         } else {
            Matrix4f anchor = model.resolveItemAnchorTransform(state, arm);
            if (anchor == null) {
               return false;
            } else {
               matrices.method_22903();
               applyModelSpaceTransform(state, matrices, 1.0F, state.field_53330);
               matrices.method_34425(anchor);
               matrices.method_22907(class_7833.field_40714.rotationDegrees(-90.0F));
               matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F));
               boolean left = arm == class_1306.field_6182;
               matrices.method_46416((left ? -1.0F : 1.0F) / 16.0F, 0.02F, -0.22F);
               class_2960 heldItemId = resolveHeldItemIdForArm(state, arm);
               applyHeldItemRigAdjustment(state, arm, matrices, heldItemId);
               matrices.method_22905(0.45F, 0.45F, 0.45F);
               itemState.method_65604(matrices, queue, light, class_4608.field_21444, state.field_61821);
               matrices.method_22909();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean shouldRenderAttachedItemsForState(class_10426 state, FbxRigConfig config) {
      if (config == null) {
         return true;
      } else if (!config.handAttachmentEnabled()) {
         return false;
      } else {
         if (!config.thirdPersonItemVisible() && isLocalPlayerState(state)) {
            class_310 client = class_310.method_1551();
            if (client != null && client.field_1690 != null && !client.field_1690.method_31044().method_31034()) {
               return false;
            }
         }

         return true;
      }
   }

   private static void applyHeldItemRigAdjustment(class_10426 state, class_1306 arm, class_4587 matrices, class_2960 heldItemId) {
      FbxRigConfig config = resolveRigConfigForState(state);
      if (config != null && config != FbxRigConfig.EMPTY) {
         boolean left = arm == class_1306.field_6182;
         float offsetX = left ? config.leftHandOffsetX() : config.rightHandOffsetX();
         float offsetY = left ? config.leftHandOffsetY() : config.rightHandOffsetY();
         float offsetZ = left ? config.leftHandOffsetZ() : config.rightHandOffsetZ();
         float rotX = left ? config.leftHandRotX() : config.rightHandRotX();
         float rotY = left ? config.leftHandRotY() : config.rightHandRotY();
         float rotZ = left ? config.leftHandRotZ() : config.rightHandRotZ();
         float configuredScale = left ? config.leftHandScale() : config.rightHandScale();
         if (heldItemId != null && ResourcePackObjItemModel.isCustomModelItem(heldItemId)) {
            FbxItemTransform itemTransform = config.customItemTransform(heldItemId);
            if (itemTransform != null) {
               offsetX += itemTransform.offsetX(left);
               offsetY += itemTransform.offsetY(left);
               offsetZ += itemTransform.offsetZ(left);
               rotX += itemTransform.rotX(left);
               rotY += itemTransform.rotY(left);
               rotZ += itemTransform.rotZ(left);
               configuredScale *= itemTransform.scale(left);
            }
         }

         float safeScale = Float.isFinite(configuredScale) && configuredScale != 0.0F ? configuredScale : 1.0F;
         if (offsetX != 0.0F || offsetY != 0.0F || offsetZ != 0.0F) {
            matrices.method_46416(offsetX, offsetY, offsetZ);
         }

         if (rotX != 0.0F) {
            matrices.method_22907(class_7833.field_40714.rotationDegrees(rotX));
         }

         if (rotY != 0.0F) {
            matrices.method_22907(class_7833.field_40716.rotationDegrees(rotY));
         }

         if (rotZ != 0.0F) {
            matrices.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
         }

         if (safeScale != 1.0F) {
            matrices.method_22905(safeScale, safeScale, safeScale);
         }
      }
   }

   private static class_2960 resolveHeldItemIdForArm(class_10426 state, class_1306 arm) {
      class_1799 stack = resolveHeldItemStackForArm(state, arm);
      return stack != null && !stack.method_7960() && stack.method_7909() != null ? class_7923.field_41178.method_10221(stack.method_7909()) : null;
   }

   private static class_1799 resolveHeldItemStackForArm(class_10426 state, class_1306 arm) {
      if (state != null && arm != null) {
         Integer entityId = resolveMotionEntityId(state);
         if (entityId != null && entityId != Integer.MIN_VALUE) {
            class_310 client = class_310.method_1551();
            if (client != null && client.field_1687 != null) {
               if (client.field_1687.method_8469(entityId) instanceof class_1309 living) {
                  boolean mainArmMatches = living.method_6068() == arm;
                  class_1799 stack = mainArmMatches ? living.method_6047() : living.method_6079();
                  return stack == null ? class_1799.field_8037 : stack;
               } else {
                  return class_1799.field_8037;
               }
            } else {
               return class_1799.field_8037;
            }
         } else {
            return class_1799.field_8037;
         }
      } else {
         return class_1799.field_8037;
      }
   }

   private static FbxRigConfig resolveRigConfigForState(class_10042 state) {
      class_2960 entityId = getStateTargetId(state);
      return resolveRigConfigForTarget(entityId);
   }

   public static FbxRigConfig getRigConfigForTarget(class_2960 entityId) {
      return resolveRigConfigForTarget(entityId);
   }

   public static List<class_2960> getDetectedEntityTargets() {
      LinkedHashSet<class_2960> unique = new LinkedHashSet<>();
      if (entityModels != null) {
         for (class_2960 id : entityModels.keySet()) {
            if (id != null) {
               unique.add(id);
            }
         }
      }

      if (unique.isEmpty()) {
         unique.add(class_2960.method_60656("player"));
      }

      List<class_2960> list = new ArrayList<>(unique);
      class_2960 playerId = class_2960.method_60656("player");
      list.sort((a, b) -> {
         boolean aPlayer = playerId.equals(a);
         boolean bPlayer = playerId.equals(b);
         if (aPlayer && !bPlayer) {
            return -1;
         } else {
            return !aPlayer && bPlayer ? 1 : a.toString().compareToIgnoreCase(b.toString());
         }
      });
      return List.copyOf(list);
   }

   public static List<String> getAnimationNamesForTarget(class_2960 entityId) {
      if (entityId == null) {
         return List.of();
      } else {
         ResourcePackObjLivingOverrides.AnimatedObjModel model = entityModels.get(entityId);
         if (model == null) {
            return List.of();
         } else {
            LinkedHashSet<String> names = new LinkedHashSet<>();

            for (String name : model.animations.keySet()) {
               if (name != null && !name.isBlank()) {
                  names.add(name);
               }
            }

            for (String namex : model.runtimeAnimations.keySet()) {
               if (namex != null && !namex.isBlank()) {
                  names.add(namex);
               }
            }

            if (names.isEmpty()) {
               return List.of();
            } else {
               List<String> list = new ArrayList<>(names);
               list.sort(String::compareToIgnoreCase);
               return List.copyOf(list);
            }
         }
      }
   }

   public static List<String> getBoneNamesForTarget(class_2960 entityId) {
      if (entityId == null) {
         return List.of();
      } else {
         ResourcePackObjLivingOverrides.AnimatedObjModel model = entityModels.get(entityId);
         return model == null ? List.of() : model.availableBoneNames();
      }
   }

   public static void setRigConfigOverrideForTarget(class_2960 entityId, FbxRigConfig config) {
      if (entityId != null) {
         rigConfigByTargetId.put(entityId, config == null ? FbxRigConfig.EMPTY : config);
      }
   }

   public static void clearRigConfigOverrideForTarget(class_2960 entityId) {
      if (entityId != null) {
         rigConfigByTargetId.remove(entityId);
      }
   }

   private static FbxRigConfig resolveRigConfigForTarget(class_2960 entityId) {
      if (entityId == null) {
         return FbxRigConfig.EMPTY;
      } else {
         FbxRigConfig cached = rigConfigByTargetId.get(entityId);
         if (cached != null) {
            return cached;
         } else {
            class_310 client = class_310.method_1551();
            if (client != null && client.method_1478() != null) {
               String targetPath = class_2960.method_60656("player").equals(entityId) ? "player" : entityId.method_12832();
               FbxRigConfig loaded = FbxRigConfig.load(client.method_1478(), entityId.method_12836(), targetPath);
               FbxRigConfig resolved = loaded == null ? FbxRigConfig.EMPTY : loaded;
               rigConfigByTargetId.put(entityId, resolved);
               return resolved;
            } else {
               return FbxRigConfig.EMPTY;
            }
         }
      }
   }

   private static boolean shouldRenderOverrideForState(class_10042 state) {
      if (state instanceof class_10055 playerState) {
         Integer resolvedId = resolvePlayerRenderEntityId(state);
         int candidateId = resolvedId != null ? resolvedId : playerState.field_53528;
         return candidateId == localPlayerEntityId || multiplayerPlayerModelsEnabled || othersModeledEnabled;
      } else {
         return true;
      }
   }

   private static void renderArmorStack(
      class_10034 state, class_1799 stack, class_4587 matrices, class_11659 queue, int light, float layerScale, float entityHeight
   ) {
      if (stack != null && !stack.method_7960()) {
         class_2960 itemId = class_7923.field_41178.method_10221(stack.method_7909());
         ResourcePackObjLivingOverrides.AnimatedObjModel model = armorModels.get(itemId);
         if (model != null) {
            matrices.method_22903();
            applyModelSpaceTransform(state, matrices, layerScale, entityHeight);
            model.render(state, matrices, queue, light);
            matrices.method_22909();
         }
      }
   }

   private static void applyModelSpaceTransform(class_10042 state, class_4587 matrices, float layerScale, float entityHeight) {
      FbxRigConfig config = resolveRigConfigForState(state);
      float heightScale = resolveRenderHeightScale(state, entityHeight);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(180.0F));
      float offsetX = config == null ? 0.0F : config.entityOffsetX();
      float offsetY = config == null ? 0.0F : config.entityOffsetY();
      float offsetZ = config == null ? 0.0F : config.entityOffsetZ();
      float rotX = config == null ? 0.0F : config.entityRotX();
      float rotY = config == null ? 0.0F : config.entityRotY();
      float rotZ = config == null ? 0.0F : config.entityRotZ();
      float configScale = config == null ? 1.0F : config.entityScale();
      if (!Float.isFinite(configScale) || configScale < 0.01F) {
         configScale = 1.0F;
      }

      matrices.method_46416(offsetX, -1.5F + offsetY, 0.075F + offsetZ);
      if (Math.abs(rotX) > 0.001F) {
         matrices.method_22907(class_7833.field_40714.rotationDegrees(rotX));
      }

      if (Math.abs(rotY) > 0.001F) {
         matrices.method_22907(class_7833.field_40716.rotationDegrees(rotY));
      }

      if (Math.abs(rotZ) > 0.001F) {
         matrices.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
      }

      float finalScale = heightScale * layerScale * configScale;
      matrices.method_22905(finalScale, finalScale, finalScale);
   }

   private static float resolveRenderHeightScale(class_10042 state, float height) {
      float resolved = height <= 0.0F ? 1.95F : height;
      return state instanceof class_10055 ? Math.max(1.95F, resolved) : resolved;
   }

   private static class_2960 getStateTargetId(class_10042 state) {
      if (state instanceof class_10055) {
         Integer renderEntityIdValue = resolvePlayerRenderEntityId(state);
         if (renderEntityIdValue == null) {
            return null;
         } else {
            int renderEntityId = renderEntityIdValue;
            class_2960 selected;
            synchronized (networkPlayerModelTargetByEntityId) {
               selected = networkPlayerModelTargetByEntityId.get(renderEntityId);
            }

            boolean isLocalPlayer = renderEntityId == localPlayerEntityId;
            if (selected != null && entityModels.containsKey(selected)) {
               if (!isLocalPlayer && class_2960.method_60656("player").equals(selected)) {
                  class_2960 playerVariant = resolveRemotePlayerVariantId(renderEntityId);
                  if (playerVariant != null && entityModels.containsKey(playerVariant)) {
                     synchronized (loggedRemotePlayerAppliedIds) {
                        if (!loggedRemotePlayerAppliedIds.contains(renderEntityId)) {
                           loggedRemotePlayerAppliedIds.add(renderEntityId);
                           ObjFbxLoader.LOGGER
                              .info(
                                 "Applying per-player named variant for entityId={} targetId={} (from synced minecraft:player).", renderEntityId, playerVariant
                              );
                        }

                        return playerVariant;
                     }
                  } else if (!othersModeledEnabled) {
                     return null;
                  } else {
                     class_2960 localSelected;
                     synchronized (networkPlayerModelTargetByEntityId) {
                        localSelected = networkPlayerModelTargetByEntityId.get(localPlayerEntityId);
                     }

                     if (localSelected != null && entityModels.containsKey(localSelected)) {
                        return localSelected;
                     } else {
                        return entityModels.containsKey(class_2960.method_60656("player")) ? class_2960.method_60656("player") : null;
                     }
                  }
               } else {
                  if (!isLocalPlayer) {
                     synchronized (loggedRemotePlayerAppliedIds) {
                        if (!loggedRemotePlayerAppliedIds.contains(renderEntityId)) {
                           loggedRemotePlayerAppliedIds.add(renderEntityId);
                           ObjFbxLoader.LOGGER.info("Applying synced remote model for entityId={} targetId={}", renderEntityId, selected);
                        }
                     }
                  }

                  return selected;
               }
            } else if (isLocalPlayer) {
               return class_2960.method_60656("player");
            } else {
               if (othersModeledEnabled) {
                  class_2960 localSelected;
                  synchronized (networkPlayerModelTargetByEntityId) {
                     localSelected = networkPlayerModelTargetByEntityId.get(localPlayerEntityId);
                  }

                  if (localSelected != null && entityModels.containsKey(localSelected)) {
                     return localSelected;
                  }

                  if (entityModels.containsKey(class_2960.method_60656("player"))) {
                     return class_2960.method_60656("player");
                  }
               }

               synchronized (loggedMissingPlayerRenderStateIds) {
                  if (!loggedMissingPlayerRenderStateIds.contains(renderEntityId)) {
                     loggedMissingPlayerRenderStateIds.add(renderEntityId);
                     ObjFbxLoader.LOGGER.info("No matching synced model target for remote player entityId={}; rendering vanilla.", renderEntityId);
                  }

                  return null;
               }
            }
         }
      } else {
         return state.field_58171 == null ? null : class_7923.field_41177.method_10221(state.field_58171);
      }
   }

   private static class_2960 resolveRemotePlayerVariantId(int entityId) {
      String name;
      synchronized (networkPlayerNameByEntityId) {
         name = networkPlayerNameByEntityId.get(entityId);
      }

      if (name != null && !name.isBlank()) {
         String sanitized = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
         if (sanitized.isBlank()) {
            return null;
         } else {
            synchronized (downloadedVariantByPlayerName) {
               class_2960 downloaded = downloadedVariantByPlayerName.get(sanitized);
               if (downloaded != null) {
                  return downloaded;
               }
            }

            class_2960 preferred = class_2960.method_12829("minecraft:player-" + sanitized);
            return preferred != null ? preferred : class_2960.method_12829("minecraft:" + sanitized);
         }
      } else {
         return null;
      }
   }

   private static Integer resolvePlayerRenderEntityId(class_10042 state) {
      Integer remembered = resolveRememberedRenderStateEntityId(state);
      if (remembered != null) {
         return remembered;
      } else {
         return state instanceof class_10055 playerState ? playerState.field_53528 : tryReflectRenderStateEntityId(state);
      }
   }

   private static Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> loadModels(
      class_3300 resourceManager, String root, ResourcePackObjLivingOverrides.ResourceKind kind
   ) {
      Map<class_2960, ResourcePackObjLivingOverrides.ModelBuilder> models = new HashMap<>();
      Map<class_2960, class_3298> resources = resourceManager.method_14488(
         root.substring(0, root.lastIndexOf(47)),
         id -> id.method_12832().startsWith(root + "/") && ResourcePackMeshLoader.isSupportedMeshModelPath(id.method_12832())
      );

      for (Entry<class_2960, class_3298> entry : resources.entrySet()) {
         class_2960 resourceId = entry.getKey();
         String path = resourceId.method_12832();
         String modelPath = path.substring((root + "/").length(), path.length() - extensionLength(path));
         ResourcePackObjLivingOverrides.ModelPathParts pathParts = splitModelPath(modelPath);
         String targetPath = pathParts.targetPath;
         String variantName = pathParts.variantPath;
         ResourcePackObjLivingOverrides.ResolvedTarget resolvedTarget = kind.resolveTarget(resourceId.method_12836(), targetPath);
         class_2960 targetId = resolvedTarget.id();
         String resolvedTargetPath = resolvedTarget.configTargetPath();
         if (targetId != null && resolvedTargetPath != null && kind.isValidId(targetId)) {
            ResourcePackObjLivingOverrides.AnimationFrame animationFrame = parseAnimationFrame(variantName);
            if (animationFrame != null && animationFrame.frameIndex < 0 && ResourcePackMeshLoader.isAssimpModelPath(resourceId.method_12832())) {
               ResourcePackObjLivingOverrides.ModelBuilder builder = models.computeIfAbsent(
                  targetId, ignored -> new ResourcePackObjLivingOverrides.ModelBuilder()
               );
               builder.runtimeAnimationSources
                  .put(
                     animationFrame.animationName,
                     new ResourcePackObjLivingOverrides.RuntimeAnimationSource(resourceId, entry.getValue(), resolvedTargetPath, modelPath)
                  );
            } else {
               ResourcePackObjLivingOverrides.ObjModel model = loadModel(resourceManager, resourceId, entry.getValue(), kind, resolvedTargetPath, modelPath);
               if (!model.triangles.isEmpty()) {
                  ResourcePackObjLivingOverrides.ModelBuilder builder = models.computeIfAbsent(
                     targetId, ignored -> new ResourcePackObjLivingOverrides.ModelBuilder()
                  );
                  if (animationFrame == null) {
                     builder.staticModel = model;
                  } else {
                     builder.animations
                        .computeIfAbsent(animationFrame.animationName, ignored -> new ArrayList<>())
                        .add(new ResourcePackObjLivingOverrides.FrameEntry(animationFrame.frameIndex, model));
                  }
               }
            }
         } else {
            ObjFbxLoader.LOGGER.warn("Skipping OBJ {} model {} because it does not match a registered target.", kind.logName, resourceId);
         }
      }

      Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> builtModels = new HashMap<>();

      for (Entry<class_2960, ResourcePackObjLivingOverrides.ModelBuilder> entryx : models.entrySet()) {
         resolveRuntimeAnimations(resourceManager, kind, entryx.getValue());
         ResourcePackObjLivingOverrides.AnimatedObjModel built = entryx.getValue().build();
         if (!built.hasRenderableContent()) {
            ObjFbxLoader.LOGGER.warn("Skipping {} model override {} because it has no renderable mesh data after load.", kind.logName, entryx.getKey());
         } else {
            builtModels.put(entryx.getKey(), built);
         }
      }

      return Map.copyOf(builtModels);
   }

   private static void resolveRuntimeAnimations(
      class_3300 resourceManager, ResourcePackObjLivingOverrides.ResourceKind kind, ResourcePackObjLivingOverrides.ModelBuilder builder
   ) {
      if (!builder.runtimeAnimationSources.isEmpty()) {
         ResourcePackObjLivingOverrides.RuntimeAnimationSource idleSource = builder.runtimeAnimationSources.get("idle");
         ResourcePackMeshLoader.AnimatedMeshData idleBase = null;
         if (idleSource != null) {
            idleBase = ResourcePackMeshLoader.loadAnimatedFbxModel(
               resourceManager,
               idleSource.resourceId(),
               idleSource.resource(),
               kind.textureFolder,
               idleSource.targetPath(),
               idleSource.modelPath(),
               1.0F,
               false,
               kind.logName
            );
            if (!idleBase.isEmpty()) {
               builder.runtimeAnimations.put("idle", idleBase);
               if (!builder.timeBasedAnimations.contains("idle")) {
                  builder.timeBasedAnimations.add("idle");
               }
            }
         }

         for (Entry<String, ResourcePackObjLivingOverrides.RuntimeAnimationSource> entry : builder.runtimeAnimationSources.entrySet()) {
            String animationName = entry.getKey();
            if (!"idle".equals(animationName)) {
               ResourcePackObjLivingOverrides.RuntimeAnimationSource source = entry.getValue();
               ResourcePackMeshLoader.AnimatedMeshData resolved;
               if (idleBase != null && !idleBase.isEmpty()) {
                  ResourcePackMeshLoader.AnimationClipData clip = ResourcePackMeshLoader.loadAssimpAnimationClip(
                     source.resourceId(), source.resource(), kind.logName
                  );
                  if (clip != null) {
                     resolved = idleBase.withAnimationClip(clip);
                  } else {
                     resolved = ResourcePackMeshLoader.loadAnimatedFbxModel(
                        resourceManager,
                        source.resourceId(),
                        source.resource(),
                        kind.textureFolder,
                        source.targetPath(),
                        source.modelPath(),
                        1.0F,
                        false,
                        kind.logName
                     );
                  }
               } else {
                  resolved = ResourcePackMeshLoader.loadAnimatedFbxModel(
                     resourceManager,
                     source.resourceId(),
                     source.resource(),
                     kind.textureFolder,
                     source.targetPath(),
                     source.modelPath(),
                     1.0F,
                     false,
                     kind.logName
                  );
               }

               if (!resolved.isEmpty()) {
                  builder.runtimeAnimations.put(animationName, resolved);
                  if (!builder.timeBasedAnimations.contains(animationName)) {
                     builder.timeBasedAnimations.add(animationName);
                  }
               }
            }
         }
      }
   }

   private static ResourcePackObjLivingOverrides.ObjModel loadModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      ResourcePackObjLivingOverrides.ResourceKind kind,
      String targetPath,
      String modelPath
   ) {
      ResourcePackMeshLoader.MeshData mesh = ResourcePackMeshLoader.loadModel(
         resourceManager, resourceId, resource, kind.textureFolder, targetPath, modelPath, 1.0F, false, kind.logName
      );
      return toObjModel(mesh);
   }

   private static ResourcePackObjLivingOverrides.ObjModel toObjModel(ResourcePackMeshLoader.MeshData mesh) {
      return buildObjModel(mesh);
   }

   private static ResourcePackObjLivingOverrides.ObjModel buildObjModel(ResourcePackMeshLoader.MeshData mesh) {
      return mesh.packedRenderStream() != null && mesh.packedRenderStream().length > 0 && mesh.packedTriangleCount() > 0 && !hasMultipleTriangleTextures(mesh)
         ? new ResourcePackObjLivingOverrides.ObjModel(mesh.texture(), List.of(), mesh.packedRenderStream(), mesh.packedTriangleCount())
         : new ResourcePackObjLivingOverrides.ObjModel(mesh.texture(), mesh.triangles(), new float[0], 0);
   }

   private static boolean hasMultipleTriangleTextures(ResourcePackMeshLoader.MeshData mesh) {
      if (mesh != null && mesh.triangles() != null && !mesh.triangles().isEmpty()) {
         class_2960 first = null;

         for (ResourcePackMeshLoader.MeshTriangle triangle : mesh.triangles()) {
            if (triangle != null) {
               class_2960 texture = triangle.texture() != null ? triangle.texture() : mesh.texture();
               if (texture != null) {
                  if (first == null) {
                     first = texture;
                  } else if (!first.equals(texture)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static int extensionLength(String path) {
      return ResourcePackMeshLoader.modelExtensionLength(path);
   }

   private static ResourcePackObjLivingOverrides.AnimationFrame parseAnimationFrame(String variantName) {
      if (variantName.isBlank()) {
         return null;
      } else {
         String normalized = variantName.replace('\\', '/');
         int slash = normalized.indexOf(47);
         if (slash >= 0) {
            String animationFolder = normalized.substring(0, slash).trim().toLowerCase();
            String frameName = normalized.substring(slash + 1).trim();
            int dot = frameName.lastIndexOf(46);
            if (dot >= 0) {
               frameName = frameName.substring(0, dot);
            }

            if (frameName.startsWith("_")) {
               frameName = frameName.substring(1);
            }

            if (!animationFolder.isBlank()) {
               if (isDigits(frameName)) {
                  return new ResourcePackObjLivingOverrides.AnimationFrame(animationFolder, Integer.parseInt(frameName));
               }

               return new ResourcePackObjLivingOverrides.AnimationFrame(animationFolder, -1);
            }
         }

         int underscore = normalized.lastIndexOf(95);
         if (underscore > 0 && underscore < variantName.length() - 1) {
            String frameSuffix = normalized.substring(underscore + 1);
            return !isDigits(frameSuffix)
               ? null
               : new ResourcePackObjLivingOverrides.AnimationFrame(normalized.substring(0, underscore).toLowerCase(), Integer.parseInt(frameSuffix));
         } else {
            return null;
         }
      }
   }

   private static String selectAnimation(class_10042 state) {
      if (state.field_53449 > 0.0F) {
         clearTrackedJumpArc(state);
         return "death";
      } else if (state.field_53460) {
         clearTrackedJumpArc(state);
         return "hurt";
      } else if (isSittingState(state)) {
         clearOneShotPlayback(state);
         return "sit";
      } else {
         if (state instanceof class_10426 armedState && armedState.field_63604 > 0.08F) {
            FbxRigConfig config = resolveRigConfigForState(state);
            boolean upperBodyOnly = config != null && config.attackUpperBodyOnly();
            if (!upperBodyOnly || !isLocomotionActive(state)) {
               clearTrackedJumpArc(state);
               return "attack";
            }
         }

         ResourcePackObjLivingOverrides.MotionSampleDelta motion = sampleMotionDelta(state);
         float verticalMotion = motion.verticalSpeed();
         float horizontalMotion = motion.horizontalSpeed();
         Integer stateEntityId = resolveMotionEntityId(state);
         ResourcePackObjLivingOverrides.AirborneJumpArcState trackedJumpArc = stateEntityId == null ? null : airborneJumpAnimationByEntityId.get(stateEntityId);
         if (state instanceof class_10034 bipedState) {
            if (bipedState.field_53411 || state.field_53465 == class_4050.field_18077) {
               clearTrackedJumpArc(state);
               return "glide";
            }

            if (bipedState.field_53412 || state.field_53465 == class_4050.field_18079 || state.field_53458) {
               clearTrackedJumpArc(state);
               return "swim";
            }

            if (isLikelyClimbing(state, verticalMotion, horizontalMotion)) {
               clearTrackedJumpArc(state);
               return "climb";
            }

            if (bipedState.field_53410 || state.field_53334) {
               clearTrackedJumpArc(state);
               return state.field_53451 > 0.08F ? "sneak_walk" : "sneak";
            }
         }

         if (state.field_53465 != class_4050.field_30095 && !(verticalMotion > 0.24F)) {
            if (stateEntityId != null && trackedJumpArc != null) {
               if (trackedJumpArc.startedDescent()) {
                  float elapsed = Math.max(0.0F, state.field_53328 - trackedJumpArc.startAge());
                  if (!isLikelyLandingFrame(state) && !(elapsed > 18.0F)) {
                     return "fall";
                  }

                  airborneJumpAnimationByEntityId.remove(stateEntityId);
               } else {
                  if (verticalMotion < -0.03F) {
                     airborneJumpAnimationByEntityId.put(
                        stateEntityId, new ResourcePackObjLivingOverrides.AirborneJumpArcState(trackedJumpArc.animationName(), trackedJumpArc.startAge(), true)
                     );
                     return "fall";
                  }

                  float elapsed = Math.max(0.0F, state.field_53328 - trackedJumpArc.startAge());
                  if (!(elapsed > 18.0F)) {
                     return trackedJumpArc.animationName();
                  }

                  airborneJumpAnimationByEntityId.remove(stateEntityId);
               }
            }

            if (stateEntityId != null && trackedJumpArc != null && isLikelyLandingFrame(state)) {
               airborneJumpAnimationByEntityId.remove(stateEntityId);
            }

            if (verticalMotion < -0.24F) {
               clearTrackedJumpArc(state);
               return "fall";
            } else if (isBackwardLocomotion(state, motion.forwardSpeed()) && state.field_53451 > 0.08F) {
               return "backwalk";
            } else if (state.field_53451 > 0.22F) {
               return "sprint";
            } else {
               return state.field_53451 > 0.08F ? "walk" : "idle";
            }
         } else {
            String launchedAnimation = horizontalMotion >= 0.14F ? "run_jump" : "jump";
            if (stateEntityId != null) {
               if (state.field_53465 == class_4050.field_30095) {
                  airborneJumpAnimationByEntityId.put(
                     stateEntityId, new ResourcePackObjLivingOverrides.AirborneJumpArcState(launchedAnimation, state.field_53328, false)
                  );
                  return launchedAnimation;
               }

               float previousVertical = previousVerticalSpeedByEntityId.getOrDefault(stateEntityId, 0.0F);
               if (!(previousVertical <= 0.1F)) {
                  airborneJumpAnimationByEntityId.put(
                     stateEntityId, new ResourcePackObjLivingOverrides.AirborneJumpArcState(launchedAnimation, state.field_53328, false)
                  );
                  return launchedAnimation;
               }
            }

            return launchedAnimation;
         }
      }
   }

   private static boolean isLocomotionActive(class_10042 state) {
      if (state == null) {
         return false;
      } else if (state.field_53451 > 0.08F) {
         return true;
      } else {
         ResourcePackObjLivingOverrides.MotionSampleDelta motion = sampleMotionDelta(state);
         return motion.horizontalSpeed() > 0.01F;
      }
   }

   private static ResourcePackObjLivingOverrides.MotionSampleDelta sampleMotionDelta(class_10042 state) {
      Integer resolvedEntityId = resolveMotionEntityId(state);
      if (resolvedEntityId == null) {
         return ResourcePackObjLivingOverrides.MotionSampleDelta.ZERO;
      } else {
         int entityId = resolvedEntityId;
         ResourcePackObjLivingOverrides.MotionSample previous = motionSamplesByEntityId.get(entityId);
         if (previous != null && Math.abs(state.field_53328 - previous.age()) < 1.0E-4F) {
            return motionDeltasByEntityId.getOrDefault(entityId, ResourcePackObjLivingOverrides.MotionSampleDelta.ZERO);
         } else {
            if (motionSamplesByEntityId.size() > 256) {
               ResourcePackObjLivingOverrides.MotionSample keep = motionSamplesByEntityId.get(entityId);
               ResourcePackObjLivingOverrides.MotionSampleDelta keepDelta = motionDeltasByEntityId.get(entityId);
               Float keepPreviousVertical = previousVerticalSpeedByEntityId.get(entityId);
               ResourcePackObjLivingOverrides.AirborneJumpArcState keepJumpArcAnimation = airborneJumpAnimationByEntityId.get(entityId);
               Boolean keepBackward = backwardLocomotionByEntityId.get(entityId);
               ResourcePackObjLivingOverrides.LocomotionPhaseState keepPhase = locomotionPhaseByEntityId.get(entityId);
               ResourcePackObjLivingOverrides.OneShotPlaybackState keepOneShot = oneShotPlaybackByEntityId.get(entityId);
               Boolean keepSitting = sittingByEntityId.get(entityId);
               motionSamplesByEntityId.clear();
               motionDeltasByEntityId.clear();
               previousVerticalSpeedByEntityId.clear();
               airborneJumpAnimationByEntityId.clear();
               backwardLocomotionByEntityId.clear();
               locomotionPhaseByEntityId.clear();
               oneShotPlaybackByEntityId.clear();
               sittingByEntityId.clear();
               if (keep != null) {
                  motionSamplesByEntityId.put(entityId, keep);
               }

               if (keepDelta != null) {
                  motionDeltasByEntityId.put(entityId, keepDelta);
               }

               if (keepPreviousVertical != null) {
                  previousVerticalSpeedByEntityId.put(entityId, keepPreviousVertical);
               }

               if (keepJumpArcAnimation != null) {
                  airborneJumpAnimationByEntityId.put(entityId, keepJumpArcAnimation);
               }

               if (keepBackward != null) {
                  backwardLocomotionByEntityId.put(entityId, keepBackward);
               }

               if (keepPhase != null) {
                  locomotionPhaseByEntityId.put(entityId, keepPhase);
               }

               if (keepOneShot != null) {
                  oneShotPlaybackByEntityId.put(entityId, keepOneShot);
               }

               if (keepSitting != null) {
                  sittingByEntityId.put(entityId, keepSitting);
               }
            }

            if (previous == null) {
               ResourcePackObjLivingOverrides.MotionSampleDelta delta = ResourcePackObjLivingOverrides.MotionSampleDelta.ZERO;
               motionSamplesByEntityId.put(
                  entityId, new ResourcePackObjLivingOverrides.MotionSample(state.field_53325, state.field_53326, state.field_53327, state.field_53328)
               );
               motionDeltasByEntityId.put(entityId, delta);
               return delta;
            } else {
               float dt = state.field_53328 - previous.age();
               if (!Float.isFinite(dt) || dt <= 1.0E-4F) {
                  dt = 1.0F;
               }

               dt = Math.max(0.05F, Math.min(1.5F, dt));
               float dx = (float)((state.field_53325 - previous.x()) / dt);
               float dy = (float)((state.field_53326 - previous.y()) / dt);
               float dz = (float)((state.field_53327 - previous.z()) / dt);
               float dyDelta = (float)(state.field_53326 - previous.y());
               float rawHorizontal = (float)Math.sqrt(dx * dx + dz * dz);
               float yawRadians = (float)Math.toRadians(state.field_53446);
               float forwardX = -((float)Math.sin(yawRadians));
               float forwardZ = (float)Math.cos(yawRadians);
               float rawForward = dx * forwardX + dz * forwardZ;
               ResourcePackObjLivingOverrides.MotionSampleDelta previousDelta = motionDeltasByEntityId.get(entityId);
               float vertical = previousDelta == null ? dy : previousDelta.verticalSpeed() * 0.35F + dy * 0.65F;
               float horizontal = previousDelta == null ? rawHorizontal : previousDelta.horizontalSpeed() * 0.82F + rawHorizontal * 0.18F;
               float previousVertical = previousDelta == null ? 0.0F : previousDelta.verticalSpeed();
               float forward = previousDelta == null ? rawForward : previousDelta.forwardSpeed() * 0.8F + rawForward * 0.2F;
               float verticalDelta = previousDelta == null ? dyDelta : previousDelta.verticalDelta() * 0.7F + dyDelta * 0.3F;
               ResourcePackObjLivingOverrides.MotionSampleDelta delta = new ResourcePackObjLivingOverrides.MotionSampleDelta(
                  vertical, horizontal, forward, verticalDelta
               );
               motionSamplesByEntityId.put(
                  entityId, new ResourcePackObjLivingOverrides.MotionSample(state.field_53325, state.field_53326, state.field_53327, state.field_53328)
               );
               motionDeltasByEntityId.put(entityId, delta);
               previousVerticalSpeedByEntityId.put(entityId, previousVertical);
               return delta;
            }
         }
      }
   }

   private static boolean isBackwardLocomotion(class_10042 state, float forwardSpeed) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId == null) {
         return forwardSpeed < -0.02F;
      } else {
         int entityId = stateEntityId;
         boolean wasBackward = backwardLocomotionByEntityId.getOrDefault(entityId, false);
         boolean nowBackward = wasBackward ? forwardSpeed < 0.03F : forwardSpeed < -0.09F;
         backwardLocomotionByEntityId.put(entityId, nowBackward);
         return nowBackward;
      }
   }

   private static boolean isLikelyClimbing(class_10042 state, float verticalMotion, float horizontalMotion) {
      if (!state.field_53458 && state.field_53465 != class_4050.field_18079 && state.field_53465 != class_4050.field_18077) {
         float absVertical = Math.abs(verticalMotion);
         if (absVertical < 0.03F || absVertical > 0.28F) {
            return false;
         } else {
            return state.field_53451 < 0.02F ? false : horizontalMotion <= 0.05F;
         }
      } else {
         return false;
      }
   }

   private static int selectFrameIndex(String animationName, class_10042 state, int frameCount) {
      if (frameCount <= 1) {
         return 0;
      } else {
         float playbackMultiplier = resolveAnimationPlaybackMultiplier(animationName, state);
         if ("attack".equals(animationName) && state instanceof class_10426 armedState) {
            float frame = armedState.field_63604 * frameCount * playbackMultiplier;
            return Math.min(frameCount - 1, (int)frame);
         } else if ("death".equals(animationName)) {
            float frame = state.field_53449 * 1.5F * playbackMultiplier;
            return Math.min(frameCount - 1, (int)frame);
         } else if ("hurt".equals(animationName)) {
            float frame = state.field_53328 * 8.0F * playbackMultiplier;
            return Math.min(frameCount - 1, (int)frame % frameCount);
         } else if ("walk".equals(animationName)) {
            float frame = locomotionFrame(state, 0.84F, 4.317F, 0.35F, 2.0F) * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("backwalk".equals(animationName) || "back_walk".equals(animationName)) {
            float frame = locomotionFrame(state, 0.84F, 4.317F, 0.35F, 2.0F) * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("sprint".equals(animationName)) {
            float frame = locomotionFrame(state, 1.08F, 5.612F, 0.55F, 2.0F) * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("sneak_walk".equals(animationName)) {
            float frame = locomotionFrame(state, 0.7F, 1.295F, 0.5F, 1.8F) * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("climb".equals(animationName)) {
            float frame = state.field_53328 * 9.0F * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("run_jump".equals(animationName) || "jump".equals(animationName)) {
            float frame = oneShotFramePhase(state, animationName, 1.4F) * playbackMultiplier;
            return Math.min(frameCount - 1, Math.max(0, (int)Math.floor(frame)));
         } else if ("fall".equals(animationName)) {
            float frame = state.field_53328 * 1.2F * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("swim".equals(animationName) || "glide".equals(animationName)) {
            float frame = state.field_53328 * 6.0F * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else if ("sneak".equals(animationName)) {
            float frame = state.field_53328 * 4.0F * playbackMultiplier;
            return Math.floorMod((int)frame, frameCount);
         } else {
            return Math.floorMod((int)(state.field_53328 * 1.5F * playbackMultiplier), frameCount);
         }
      }
   }

   private static int selectTimeBasedFrameIndex(class_10042 state, int frameCount) {
      return frameCount <= 1 ? 0 : Math.floorMod((int)state.field_53328, frameCount);
   }

   private static float selectFrameProgress(String animationName, class_10042 state, int frameCount) {
      if (frameCount <= 1) {
         return 0.0F;
      } else {
         float playbackMultiplier = resolveAnimationPlaybackMultiplier(animationName, state);
         if ("attack".equals(animationName) && state instanceof class_10426 armedState) {
            float frame = armedState.field_63604 * frameCount * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("death".equals(animationName)) {
            float frame = state.field_53449 * 1.5F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("hurt".equals(animationName)) {
            float frame = state.field_53328 * 8.0F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("walk".equals(animationName)) {
            float frame = locomotionFrame(state, 0.84F, 4.317F, 0.35F, 2.0F) * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("backwalk".equals(animationName) || "back_walk".equals(animationName)) {
            float frame = locomotionFrame(state, 0.84F, 4.317F, 0.35F, 2.0F) * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("sprint".equals(animationName)) {
            float frame = locomotionFrame(state, 1.08F, 5.612F, 0.55F, 2.0F) * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("sneak_walk".equals(animationName)) {
            float frame = locomotionFrame(state, 0.7F, 1.295F, 0.5F, 1.8F) * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("climb".equals(animationName)) {
            float frame = state.field_53328 * 9.0F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("run_jump".equals(animationName) || "jump".equals(animationName)) {
            float frame = oneShotFramePhase(state, animationName, 1.4F) * playbackMultiplier;
            int index = Math.min(frameCount - 1, Math.max(0, (int)Math.floor(frame)));
            return index >= frameCount - 1 ? 0.0F : frame - (float)Math.floor(frame);
         } else if ("fall".equals(animationName)) {
            float frame = state.field_53328 * 1.2F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("swim".equals(animationName) || "glide".equals(animationName)) {
            float frame = state.field_53328 * 6.0F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else if ("sneak".equals(animationName)) {
            float frame = state.field_53328 * 4.0F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         } else {
            float frame = state.field_53328 * 1.5F * playbackMultiplier;
            return frame - (float)Math.floor(frame);
         }
      }
   }

   private static float selectTimeBasedFrameProgress(class_10042 state, int frameCount) {
      return frameCount <= 1 ? 0.0F : state.field_53328 - (float)Math.floor(state.field_53328);
   }

   private static float resolveAnimationPlaybackMultiplier(String animationName, class_10042 state) {
      if (animationName == null || state == null) {
         return 1.0F;
      } else if (!"run_jump".equals(animationName) && !"jump".equals(animationName) && !"fall".equals(animationName)) {
         FbxRigConfig config = resolveRigConfigForState(state);
         if (config == null) {
            return 1.0F;
         } else {
            float multiplier = config.animationSpeedMultiplier(animationName);
            return Float.isFinite(multiplier) ? clamp(multiplier, 0.05F, 4.0F) : 1.0F;
         }
      } else {
         return 1.0F;
      }
   }

   private static float locomotionFrame(class_10042 state, float baseAnimRate, float defaultSpeedBps, float minSpeedMultiplier, float maxSpeedMultiplier) {
      float horizontalSpeedBps = sampleMotionDelta(state).horizontalSpeed() * 20.0F;
      float speedMultiplier;
      if (defaultSpeedBps <= 1.0E-4F) {
         speedMultiplier = 1.0F;
      } else {
         speedMultiplier = horizontalSpeedBps / defaultSpeedBps;
      }

      speedMultiplier = clamp(speedMultiplier, minSpeedMultiplier, maxSpeedMultiplier);
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId == null) {
         return state.field_53328 * baseAnimRate * speedMultiplier;
      } else {
         int entityId = stateEntityId;
         ResourcePackObjLivingOverrides.LocomotionPhaseState phaseState = locomotionPhaseByEntityId.computeIfAbsent(
            entityId, ignored -> new ResourcePackObjLivingOverrides.LocomotionPhaseState(state.field_53328, 0.0F)
         );
         float dt = state.field_53328 - phaseState.lastAge;
         if (!Float.isFinite(dt) || dt < 0.0F) {
            dt = 0.0F;
         }

         dt = Math.min(dt, 1.5F);
         phaseState.phase += dt * baseAnimRate * speedMultiplier;
         phaseState.lastAge = state.field_53328;
         return phaseState.phase;
      }
   }

   private static boolean isAirTransitionAnimation(String animationName) {
      return "run_jump".equals(animationName) || "jump".equals(animationName) || "fall".equals(animationName);
   }

   private static boolean isJumpOneShotAnimation(String animationName) {
      return "run_jump".equals(animationName) || "jump".equals(animationName);
   }

   private static String getCurrentOneShotAnimationName(class_10042 state) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId == null) {
         return null;
      } else {
         ResourcePackObjLivingOverrides.OneShotPlaybackState playback = oneShotPlaybackByEntityId.get(stateEntityId);
         return playback == null ? null : playback.animationName();
      }
   }

   private static boolean isLikelyLandingFrame(class_10042 state) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId == null) {
         return false;
      } else {
         ResourcePackObjLivingOverrides.MotionSampleDelta motion = sampleMotionDelta(state);
         float previousVertical = previousVerticalSpeedByEntityId.getOrDefault(stateEntityId, 0.0F);
         return previousVertical < -0.03F && motion.verticalSpeed() >= -0.03F;
      }
   }

   private static void clearOneShotPlayback(class_10042 state) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId != null) {
         airborneJumpAnimationByEntityId.remove(stateEntityId);
         oneShotPlaybackByEntityId.remove(stateEntityId);
      }
   }

   private static void clearTrackedJumpArc(class_10042 state) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId != null) {
         airborneJumpAnimationByEntityId.remove(stateEntityId);
      }
   }

   private static void ensureOneShotPlaybackStarted(class_10042 state, String animationName, boolean restartIfSame) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId != null) {
         int entityId = stateEntityId;
         ResourcePackObjLivingOverrides.OneShotPlaybackState existing = oneShotPlaybackByEntityId.get(entityId);
         boolean shouldRestart = existing == null
            || !animationName.equals(existing.animationName())
            || state.field_53328 < existing.startAge() - 1.0E-4F
            || restartIfSame;
         if (shouldRestart) {
            oneShotPlaybackByEntityId.put(entityId, new ResourcePackObjLivingOverrides.OneShotPlaybackState(animationName, state.field_53328));
         }
      }
   }

   private static float oneShotFramePhase(class_10042 state, String animationName, float playbackRate) {
      Integer stateEntityId = resolveMotionEntityId(state);
      if (stateEntityId == null) {
         return state.field_53328 * playbackRate;
      } else {
         int entityId = stateEntityId;
         ResourcePackObjLivingOverrides.OneShotPlaybackState playback = oneShotPlaybackByEntityId.get(entityId);
         if (playback == null || !animationName.equals(playback.animationName()) || state.field_53328 < playback.startAge() - 1.0E-4F) {
            playback = new ResourcePackObjLivingOverrides.OneShotPlaybackState(animationName, state.field_53328);
            oneShotPlaybackByEntityId.put(entityId, playback);
         }

         float elapsed = Math.max(0.0F, state.field_53328 - playback.startAge());
         return elapsed * playbackRate;
      }
   }

   private static boolean isOneShotPlaybackFinished(class_10042 state, String animationName, int frameCount, float playbackRate) {
      if (frameCount <= 0) {
         return true;
      } else {
         Integer stateEntityId = resolveMotionEntityId(state);
         if (stateEntityId == null) {
            return true;
         } else {
            ResourcePackObjLivingOverrides.OneShotPlaybackState playback = oneShotPlaybackByEntityId.get(stateEntityId);
            if (playback != null && animationName.equals(playback.animationName())) {
               float elapsed = Math.max(0.0F, state.field_53328 - playback.startAge());
               float frame = elapsed * playbackRate;
               return frame >= frameCount - 1.0F;
            } else {
               return true;
            }
         }
      }
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   private static boolean isDigits(String value) {
      if (value.isBlank()) {
         return false;
      } else {
         for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   private static ResourcePackObjLivingOverrides.ModelPathParts splitModelPath(String modelPath) {
      int slash = modelPath.indexOf(47);
      return slash < 0
         ? new ResourcePackObjLivingOverrides.ModelPathParts(modelPath, "")
         : new ResourcePackObjLivingOverrides.ModelPathParts(modelPath.substring(0, slash), modelPath.substring(slash + 1));
   }

   private record AirborneJumpArcState(String animationName, float startAge, boolean startedDescent) {
   }

   public static final class AnimatedObjModel {
      private static final int BLEND_STATE_CACHE_LIMIT = 4096;
      private static final int RUNTIME_FRAME_MODEL_CACHE_LIMIT = 4096;
      private static final float RUNTIME_FRAME_BUCKET = 0.75F;
      private static final float RUNTIME_LOOK_BUCKET_DEGREES = 10.0F;
      private static final float FAR_RUNTIME_FRAME_BUCKET = 1.5F;
      private static final float FAR_RUNTIME_LOOK_BUCKET_DEGREES = 16.0F;
      private static final float VERY_FAR_RUNTIME_FRAME_BUCKET = 3.0F;
      private static final float VERY_FAR_RUNTIME_LOOK_BUCKET_DEGREES = 24.0F;
      private static final double NEAR_ENTITY_EXACT_SAMPLING_DISTANCE_SQ = 1024.0;
      private static final double FAR_ENTITY_DISTANCE_SQ = 2304.0;
      private static final double VERY_FAR_ENTITY_DISTANCE_SQ = 9216.0;
      private static final Map<Class<?>, Field> STATE_ID_FIELD_CACHE = new HashMap<>();
      private final ResourcePackObjLivingOverrides.ObjModel staticModel;
      private final Map<String, List<ResourcePackObjLivingOverrides.ObjModel>> animations;
      private final Map<String, ResourcePackMeshLoader.AnimatedMeshData> runtimeAnimations;
      private final List<String> timeBasedAnimations;
      private final Map<Integer, ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState> blendStateByEntityId = new LinkedHashMap<Integer, ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState>(
         512, 0.75F, true
      ) {
         @Override
         protected boolean removeEldestEntry(Entry<Integer, ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState> eldest) {
            return this.size() > 4096;
         }
      };
      private final Map<Long, ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample> runtimeFrameModelCache = new LinkedHashMap<Long, ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample>(
         512, 0.75F, true
      ) {
         @Override
         protected boolean removeEldestEntry(Entry<Long, ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample> eldest) {
            return this.size() > 4096;
         }
      };

      private AnimatedObjModel(
         ResourcePackObjLivingOverrides.ObjModel staticModel,
         Map<String, List<ResourcePackObjLivingOverrides.ObjModel>> animations,
         Map<String, ResourcePackMeshLoader.AnimatedMeshData> runtimeAnimations,
         List<String> timeBasedAnimations
      ) {
         this.staticModel = staticModel;
         this.animations = animations;
         this.runtimeAnimations = runtimeAnimations;
         this.timeBasedAnimations = timeBasedAnimations;
      }

      private boolean hasRenderableContent() {
         if (isRenderableObjModel(this.staticModel)) {
            return true;
         } else {
            for (List<ResourcePackObjLivingOverrides.ObjModel> frames : this.animations.values()) {
               if (frames != null && !frames.isEmpty()) {
                  for (ResourcePackObjLivingOverrides.ObjModel frame : frames) {
                     if (isRenderableObjModel(frame)) {
                        return true;
                     }
                  }
               }
            }

            for (ResourcePackMeshLoader.AnimatedMeshData runtime : this.runtimeAnimations.values()) {
               if (runtime != null && !runtime.isEmpty()) {
                  return true;
               }
            }

            return false;
         }
      }

      private static boolean isRenderableObjModel(ResourcePackObjLivingOverrides.ObjModel model) {
         if (model == null) {
            return false;
         } else {
            boolean hasPacked = model.packedRenderStream() != null && model.packedTriangleCount() > 0;
            boolean hasTriangles = model.triangles() != null && !model.triangles().isEmpty();
            return hasPacked || hasTriangles;
         }
      }

      private List<String> availableBoneNames() {
         if (this.runtimeAnimations != null && !this.runtimeAnimations.isEmpty()) {
            LinkedHashSet<String> names = new LinkedHashSet<>();

            for (ResourcePackMeshLoader.AnimatedMeshData runtime : this.runtimeAnimations.values()) {
               if (runtime != null) {
                  names.addAll(runtime.availableBoneNames());
               }
            }

            if (names.isEmpty()) {
               return List.of();
            } else {
               List<String> sorted = new ArrayList<>(names);
               sorted.sort(String::compareToIgnoreCase);
               return List.copyOf(sorted);
            }
         } else {
            return List.of();
         }
      }

      private void render(class_10042 state, class_4587 matrices, class_11659 queue, int light) {
         ResourcePackObjLivingOverrides.ObjModel model = this.resolveFrame(state);
         if (model != null) {
            model.render(matrices, queue, light);
         }
      }

      private ResourcePackObjLivingOverrides.ObjModel resolveFrame(class_10042 state) {
         ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState blendState = this.resolveBlendState(state);
         String animationName = ResourcePackObjLivingOverrides.selectAnimation(state);
         String activeOneShotAnimation = ResourcePackObjLivingOverrides.getCurrentOneShotAnimationName(state);
         if ("sit".equals(animationName)) {
            ResourcePackObjLivingOverrides.clearOneShotPlayback(state);
         } else if (!ResourcePackObjLivingOverrides.isAirTransitionAnimation(animationName)) {
            if (ResourcePackObjLivingOverrides.isJumpOneShotAnimation(activeOneShotAnimation)
               && this.shouldHoldOneShotAnimation(state, activeOneShotAnimation, 1.4F)
               && !ResourcePackObjLivingOverrides.isLikelyLandingFrame(state)) {
               animationName = activeOneShotAnimation;
            } else {
               ResourcePackObjLivingOverrides.clearOneShotPlayback(state);
            }
         } else if (ResourcePackObjLivingOverrides.isJumpOneShotAnimation(animationName)
            && ResourcePackObjLivingOverrides.isJumpOneShotAnimation(activeOneShotAnimation)
            && this.shouldHoldOneShotAnimation(state, activeOneShotAnimation, 1.4F)) {
            animationName = activeOneShotAnimation;
         }

         String resolvedAnimation = animationName;
         List<ResourcePackObjLivingOverrides.ObjModel> frames = this.animations.get(animationName);
         ResourcePackMeshLoader.AnimatedMeshData runtime = this.runtimeAnimations.get(animationName);
         if ("fall".equals(animationName)) {
            if (this.shouldHoldOneShotAnimation(state, "run_jump", 1.4F)) {
               resolvedAnimation = "run_jump";
               frames = this.animations.get("run_jump");
               runtime = this.runtimeAnimations.get("run_jump");
            } else if (this.shouldHoldOneShotAnimation(state, "jump", 1.4F)) {
               resolvedAnimation = "jump";
               frames = this.animations.get("jump");
               runtime = this.runtimeAnimations.get("jump");
            }
         }

         if ((frames == null || frames.isEmpty()) && runtime == null && "backwalk".equals(animationName)) {
            resolvedAnimation = "back_walk";
            frames = this.animations.get("back_walk");
            runtime = this.runtimeAnimations.get("back_walk");
            if ((frames == null || frames.isEmpty()) && runtime == null) {
               resolvedAnimation = "walk";
               frames = this.animations.get("walk");
               runtime = this.runtimeAnimations.get("walk");
            }
         }

         if ((frames == null || frames.isEmpty()) && runtime == null && "run_jump".equals(resolvedAnimation)) {
            resolvedAnimation = "jump";
            frames = this.animations.get("jump");
            runtime = this.runtimeAnimations.get("jump");
         }

         if ((frames == null || frames.isEmpty()) && runtime == null && "walk".equals(resolvedAnimation)) {
            resolvedAnimation = "sprint";
            frames = this.animations.get("sprint");
            runtime = this.runtimeAnimations.get("sprint");
         }

         if ((frames == null || frames.isEmpty()) && runtime == null) {
            resolvedAnimation = "idle";
            if (resolvedAnimation == null) {
               resolvedAnimation = "idle";
            }

            frames = this.animations.get("idle");
            runtime = this.runtimeAnimations.get("idle");
            if (!"idle".equals(resolvedAnimation)) {
               frames = this.animations.get(resolvedAnimation);
               runtime = this.runtimeAnimations.get(resolvedAnimation);
            }
         }

         if ((frames == null || frames.isEmpty()) && runtime == null) {
            return this.staticModel;
         } else {
            if ("run_jump".equals(resolvedAnimation) || "jump".equals(resolvedAnimation)) {
               ResourcePackObjLivingOverrides.ensureOneShotPlaybackStarted(state, resolvedAnimation, false);
            }

            boolean includeBoneTransforms = shouldCaptureBoneAnchorsForState(state);
            Map<String, Matrix4f> sampledBoneTransforms = Map.of();
            ResourcePackObjLivingOverrides.ObjModel targetFrame;
            if (runtime != null) {
               ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample sample = this.sampleRuntimeFrame(
                  resolvedAnimation, runtime, state, includeBoneTransforms
               );
               targetFrame = sample.model();
               sampledBoneTransforms = sample.boneTransforms();
            } else if (this.timeBasedAnimations.contains(resolvedAnimation)) {
               targetFrame = getInterpolatedTimeFrame(frames, state);
            } else {
               targetFrame = getInterpolatedAnimationFrame(resolvedAnimation, frames, state);
            }

            Map<String, Matrix4f> targetBoneTransforms = copyBoneTransformMap(sampledBoneTransforms);
            float attackOverlayAlpha = this.resolveAttackOverlayAlpha(state, resolvedAnimation);
            if (attackOverlayAlpha > 1.0E-4F) {
               ResourcePackObjLivingOverrides.ObjModel attackFrame = null;
               Map<String, Matrix4f> attackBones = Map.of();
               List<ResourcePackObjLivingOverrides.ObjModel> attackFrames = this.animations.get("attack");
               ResourcePackMeshLoader.AnimatedMeshData attackRuntime = this.runtimeAnimations.get("attack");
               if (attackRuntime != null) {
                  ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample attackSample = this.sampleRuntimeFrame(
                     "attack", attackRuntime, state, includeBoneTransforms
                  );
                  attackFrame = attackSample.model();
                  attackBones = attackSample.boneTransforms();
               } else if (attackFrames != null && !attackFrames.isEmpty()) {
                  attackFrame = getInterpolatedAnimationFrame("attack", attackFrames, state);
               }

               if (attackFrame != null) {
                  float attackCutoffY = this.resolveAttackOverlayCutoffY(state, targetBoneTransforms, attackBones);
                  targetFrame = ResourcePackObjLivingOverrides.ObjModel.blendUpperBody(targetFrame, attackFrame, attackOverlayAlpha, attackCutoffY);
                  if (attackBones != null && !attackBones.isEmpty()) {
                     if (targetBoneTransforms != null && !targetBoneTransforms.isEmpty()) {
                        targetBoneTransforms = blendBoneTransformsUpperBody(targetBoneTransforms, attackBones, attackOverlayAlpha, attackCutoffY);
                     } else {
                        targetBoneTransforms = copyBoneTransformMap(attackBones);
                     }
                  }
               }
            }

            if (targetFrame == null) {
               return this.staticModel;
            } else if (blendState.lastAnimationName == null) {
               blendState.lastAnimationName = resolvedAnimation;
               blendState.previousRenderedModel = targetFrame;
               if (!targetBoneTransforms.isEmpty()) {
                  blendState.previousBoneTransforms = copyBoneTransformMap(targetBoneTransforms);
                  blendState.lastBoneTransforms = copyBoneTransformMap(targetBoneTransforms);
               }

               return targetFrame;
            } else {
               if (!resolvedAnimation.equals(blendState.lastAnimationName)) {
                  blendState.lastAnimationName = resolvedAnimation;
                  blendState.blendStartAge = state.field_53328;
                  blendState.previousBoneTransforms = copyBoneTransformMap(blendState.lastBoneTransforms);
               }

               float blendAlpha = clamp01((state.field_53328 - blendState.blendStartAge) / 3.0F);
               if ("sit".equals(resolvedAnimation)) {
                  blendAlpha = 1.0F;
               }

               ResourcePackObjLivingOverrides.ObjModel result;
               if (blendState.previousRenderedModel != null && blendAlpha < 1.0F) {
                  result = ResourcePackObjLivingOverrides.ObjModel.blend(blendState.previousRenderedModel, targetFrame, blendAlpha);
               } else {
                  result = targetFrame;
               }

               Map<String, Matrix4f> resolvedBoneTransforms = targetBoneTransforms;
               if (!targetBoneTransforms.isEmpty()
                  && blendState.previousBoneTransforms != null
                  && !blendState.previousBoneTransforms.isEmpty()
                  && blendAlpha < 1.0F) {
                  resolvedBoneTransforms = blendBoneTransforms(blendState.previousBoneTransforms, targetBoneTransforms, blendAlpha);
               } else if (targetBoneTransforms.isEmpty()
                  && blendAlpha < 1.0F
                  && blendState.previousBoneTransforms != null
                  && !blendState.previousBoneTransforms.isEmpty()) {
                  resolvedBoneTransforms = copyBoneTransformMap(blendState.previousBoneTransforms);
               }

               if (resolvedBoneTransforms != null && !resolvedBoneTransforms.isEmpty()) {
                  blendState.lastBoneTransforms = resolvedBoneTransforms;
               }

               if (blendAlpha >= 1.0F && targetBoneTransforms != null && !targetBoneTransforms.isEmpty()) {
                  blendState.previousBoneTransforms = copyBoneTransformMap(targetBoneTransforms);
               }

               blendState.previousRenderedModel = result;
               return result;
            }
         }
      }

      private float resolveAttackOverlayAlpha(class_10042 state, String resolvedAnimation) {
         return 0.0F;
      }

      private float resolveAttackOverlayCutoffY(class_10042 state, Map<String, Matrix4f> baseBones, Map<String, Matrix4f> overlayBones) {
         FbxRigConfig config = ResourcePackObjLivingOverrides.resolveRigConfigForState(state);
         String cutoffBone = config == null ? "" : config.resolveAttackStopBone();
         if (cutoffBone != null && !cutoffBone.isBlank()) {
            Matrix4f fromBase = baseBones == null ? null : baseBones.get(cutoffBone);
            if (fromBase != null && isFinite(fromBase.m31())) {
               return fromBase.m31();
            }

            Matrix4f fromOverlay = overlayBones == null ? null : overlayBones.get(cutoffBone);
            if (fromOverlay != null && isFinite(fromOverlay.m31())) {
               return fromOverlay.m31();
            }
         }

         float baseEstimate = estimateBoneMapCenterY(baseBones);
         if (isFinite(baseEstimate)) {
            return baseEstimate;
         } else {
            float overlayEstimate = estimateBoneMapCenterY(overlayBones);
            return isFinite(overlayEstimate) ? overlayEstimate : 0.0F;
         }
      }

      private static float estimateBoneMapCenterY(Map<String, Matrix4f> bones) {
         if (bones != null && !bones.isEmpty()) {
            float sum = 0.0F;
            int count = 0;

            for (Entry<String, Matrix4f> entry : bones.entrySet()) {
               if (entry != null && entry.getValue() != null && entry.getKey() != null) {
                  String key = entry.getKey().toLowerCase(Locale.ROOT);
                  if (key.contains("spine") || key.contains("chest") || key.contains("hips") || key.contains("pelvis")) {
                     float y = entry.getValue().m31();
                     if (isFinite(y)) {
                        sum += y;
                        count++;
                     }
                  }
               }
            }

            return count <= 0 ? Float.NaN : sum / count;
         } else {
            return Float.NaN;
         }
      }

      private Matrix4f resolveItemAnchorTransform(class_10042 state, class_1306 arm) {
         ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState blendState = this.resolveBlendState(state);
         Map<String, Matrix4f> bones = blendState.lastBoneTransforms;
         if (bones != null && !bones.isEmpty()) {
            FbxRigConfig config = ResourcePackObjLivingOverrides.resolveRigConfigForState(state);
            String configuredItemBone = "";
            String configuredHandBone = "";
            if (config != null) {
               configuredItemBone = arm == class_1306.field_6183 ? config.rightItemBone() : config.leftItemBone();
               configuredHandBone = arm == class_1306.field_6183 ? config.rightHandBone() : config.leftHandBone();
            }

            String preferredKey = arm == class_1306.field_6183 ? "rightItem" : "leftItem";
            String fallbackKey = arm == class_1306.field_6183 ? "rightHand" : "leftHand";
            Matrix4f anchor = null;
            if (configuredItemBone != null && !configuredItemBone.isBlank()) {
               anchor = bones.get(configuredItemBone);
            }

            if (anchor == null && configuredHandBone != null && !configuredHandBone.isBlank()) {
               anchor = bones.get(configuredHandBone);
            }

            if (anchor == null) {
               anchor = bones.get(preferredKey);
            }

            if (anchor == null) {
               anchor = bones.get(fallbackKey);
            }

            if (anchor == null) {
               return null;
            } else {
               Matrix4f resolved = new Matrix4f(anchor);
               if (!isValidAnchorTransform(resolved)) {
                  return null;
               } else {
                  float tx = resolved.m30();
                  float ty = resolved.m31();
                  float tz = resolved.m32();
                  resolved.normalize3x3();
                  resolved.m30(tx);
                  resolved.m31(ty);
                  resolved.m32(tz);
                  return isValidAnchorTransform(resolved) ? resolved : null;
               }
            }
         } else {
            return null;
         }
      }

      private static boolean isValidAnchorTransform(Matrix4f matrix) {
         return isFinite(matrix.m00())
            && isFinite(matrix.m01())
            && isFinite(matrix.m02())
            && isFinite(matrix.m10())
            && isFinite(matrix.m11())
            && isFinite(matrix.m12())
            && isFinite(matrix.m20())
            && isFinite(matrix.m21())
            && isFinite(matrix.m22())
            && isFinite(matrix.m30())
            && isFinite(matrix.m31())
            && isFinite(matrix.m32())
            && Math.abs(matrix.m30()) <= 8.0F
            && Math.abs(matrix.m31()) <= 8.0F
            && Math.abs(matrix.m32()) <= 8.0F;
      }

      private static boolean isFinite(float value) {
         return Float.isFinite(value);
      }

      private boolean hasAnyItemAnchor(class_10042 state) {
         return this.resolveItemAnchorTransform(state, class_1306.field_6183) != null || this.resolveItemAnchorTransform(state, class_1306.field_6182) != null;
      }

      private Matrix4f resolveHeadAnchorTransform(class_10042 state) {
         ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState blendState = this.resolveBlendState(state);
         Map<String, Matrix4f> bones = blendState.lastBoneTransforms;
         if (bones != null && !bones.isEmpty()) {
            Matrix4f head = bones.get("head");
            Matrix4f neck = bones.get("neck");
            if (head == null) {
               return neck == null ? null : new Matrix4f(neck);
            } else {
               Matrix4f adjusted = new Matrix4f(head);
               if (neck != null) {
                  Vector3f headPos = head.transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
                  Vector3f neckPos = neck.transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
                  Vector3f neckToHead = headPos.sub(neckPos, new Vector3f());
                  if (neckToHead.lengthSquared() > 1.0E-6F) {
                     neckToHead.normalize(0.18F);
                     Vector3f eyeLikePos = headPos.add(neckToHead);
                     adjusted.m30(eyeLikePos.x);
                     adjusted.m31(eyeLikePos.y);
                     adjusted.m32(eyeLikePos.z);
                  }
               }

               return adjusted;
            }
         } else {
            return null;
         }
      }

      private ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState resolveBlendState(class_10042 state) {
         int key = this.resolveBlendStateKey(state);
         ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState existing = this.blendStateByEntityId.get(key);
         if (existing != null) {
            return existing;
         } else {
            ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState created = new ResourcePackObjLivingOverrides.AnimatedObjModel.AnimationBlendState();
            this.blendStateByEntityId.put(key, created);
            return created;
         }
      }

      private int resolveBlendStateKey(class_10042 state) {
         if (state instanceof class_10055 playerState) {
            return playerState.field_53528;
         } else {
            Integer remembered = ResourcePackObjLivingOverrides.resolveRememberedRenderStateEntityId(state);
            if (remembered != null) {
               return remembered;
            } else {
               Integer reflected = tryReflectStateId(state);
               if (reflected != null) {
                  return reflected;
               } else {
                  int typeHash = state.field_58171 == null ? 0 : state.field_58171.hashCode();
                  int x = (int)Math.floor(state.field_53325 * 32.0);
                  int y = (int)Math.floor(state.field_53326 * 32.0);
                  int z = (int)Math.floor(state.field_53327 * 32.0);
                  return typeHash * 31 ^ x * 73856093 ^ y * 19349663 ^ z * 83492791;
               }
            }
         }
      }

      private static Integer tryReflectStateId(class_10042 state) {
         if (state == null) {
            return null;
         } else {
            Class<?> type = state.getClass();
            Field field;
            synchronized (STATE_ID_FIELD_CACHE) {
               if (STATE_ID_FIELD_CACHE.containsKey(type)) {
                  field = STATE_ID_FIELD_CACHE.get(type);
               } else {
                  field = findIdField(type);
                  STATE_ID_FIELD_CACHE.put(type, field);
               }
            }

            if (field == null) {
               return null;
            } else {
               try {
                  return field.getInt(state);
               } catch (Throwable var5) {
                  return null;
               }
            }
         }
      }

      private static Field findIdField(Class<?> type) {
         for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
               Field field = current.getDeclaredField("id");
               field.setAccessible(true);
               if (field.getType() == int.class) {
                  return field;
               }
            } catch (Throwable var4) {
            }

            try {
               Field field = current.getDeclaredField("entityId");
               field.setAccessible(true);
               if (field.getType() == int.class) {
                  return field;
               }
            } catch (Throwable var3) {
            }
         }

         return null;
      }

      private boolean hasAnimation(String animationName) {
         List<ResourcePackObjLivingOverrides.ObjModel> frames = this.animations.get(animationName);
         return frames != null && !frames.isEmpty() ? true : this.runtimeAnimations.get(animationName) != null;
      }

      private int resolveAnimationFrameCount(String animationName) {
         List<ResourcePackObjLivingOverrides.ObjModel> frames = this.animations.get(animationName);
         if (frames != null && !frames.isEmpty()) {
            return frames.size();
         } else {
            ResourcePackMeshLoader.AnimatedMeshData runtime = this.runtimeAnimations.get(animationName);
            return runtime == null ? 0 : runtime.frameCount();
         }
      }

      private boolean shouldHoldOneShotAnimation(class_10042 state, String animationName, float playbackRate) {
         if (!this.hasAnimation(animationName)) {
            return false;
         } else {
            int frameCount = this.resolveAnimationFrameCount(animationName);
            return frameCount <= 1 ? false : !ResourcePackObjLivingOverrides.isOneShotPlaybackFinished(state, animationName, frameCount, playbackRate);
         }
      }

      private static ResourcePackObjLivingOverrides.ObjModel getInterpolatedAnimationFrame(
         String animationName, List<ResourcePackObjLivingOverrides.ObjModel> frames, class_10042 state
      ) {
         if (frames.isEmpty()) {
            return null;
         } else if (frames.size() == 1) {
            return frames.get(0);
         } else {
            int currentIndex = ResourcePackObjLivingOverrides.selectFrameIndex(animationName, state, frames.size());
            float progress = ResourcePackObjLivingOverrides.selectFrameProgress(animationName, state, frames.size());
            int nextIndex = (currentIndex + 1) % frames.size();
            return ResourcePackObjLivingOverrides.ObjModel.blend(frames.get(currentIndex), frames.get(nextIndex), progress);
         }
      }

      private int runtimeFrameCacheSize() {
         synchronized (this.runtimeFrameModelCache) {
            return this.runtimeFrameModelCache.size();
         }
      }

      private ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample sampleRuntimeFrame(
         String animationName, ResourcePackMeshLoader.AnimatedMeshData animation, class_10042 state, boolean includeBoneTransforms
      ) {
         int frameCount = animation.frameCount();
         int currentIndex = ResourcePackObjLivingOverrides.selectFrameIndex(animationName, state, frameCount);
         float progress = ResourcePackObjLivingOverrides.selectFrameProgress(animationName, state, frameCount);
         float frame = currentIndex + progress;
         ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection anchorBones = resolveItemAnchorBonesForState(state, includeBoneTransforms);
         double distanceSq = distanceSqToCamera(state);
         boolean localPlayer = ResourcePackObjLivingOverrides.isLocalPlayerState(state);
         Integer entityId = ResourcePackObjLivingOverrides.resolveRememberedRenderStateEntityId(state);
         FbxRigConfig config = ResourcePackObjLivingOverrides.resolveRigConfigForState(state);
         float remoteLipLevel = !localPlayer && entityId != null ? ResourcePackObjLivingOverrides.getNetworkPlayerLipSyncLevel(entityId) : 0.0F;
         Map<String, Vector3f> voiceOffsets = localPlayer
            ? VoiceLipSyncState.buildCurrentBoneOffsets(config)
            : VoiceLipSyncState.buildBoneOffsetsForLevel(config, remoteLipLevel);
         boolean lipActive = !voiceOffsets.isEmpty();
         boolean nearExactSampling = distanceSq <= 1024.0;
         if (!localPlayer && !nearExactSampling && !lipActive) {
            float frameBucket = 0.75F;
            float lookBucket = 10.0F;
            if (distanceSq >= 9216.0) {
               frameBucket = 3.0F;
               lookBucket = 24.0F;
            } else if (distanceSq >= 2304.0) {
               frameBucket = 1.5F;
               lookBucket = 16.0F;
            }

            float quantizedFrame = quantize(frame, frameBucket);
            float quantizedLookPitch = quantize(state.field_53448, lookBucket);
            float quantizedLookYaw = quantize(state.field_53447, lookBucket);
            long cacheKey = composeRuntimeFrameCacheKey(
               animationName,
               quantizedFrame,
               quantizedLookPitch,
               quantizedLookYaw,
               includeBoneTransforms,
               anchorBones.rightBoneHash(),
               anchorBones.leftBoneHash()
            );
            synchronized (this.runtimeFrameModelCache) {
               ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample cached = this.runtimeFrameModelCache.get(cacheKey);
               if (cached != null) {
                  ResourcePackObjLivingOverrides.STATS_RUNTIME_FRAME_CACHE_HITS.incrementAndGet();
                  return cached;
               }
            }

            ResourcePackObjLivingOverrides.STATS_RUNTIME_FRAME_CACHE_MISSES.incrementAndGet();
            ResourcePackMeshLoader.pushExternalBoneRotationOffsets(voiceOffsets);

            ResourcePackMeshLoader.MeshData sampledMesh;
            try {
               sampledMesh = animation.sampleFrame(
                  quantizedFrame, quantizedLookPitch, quantizedLookYaw, includeBoneTransforms, anchorBones.rightBone(), anchorBones.leftBone()
               );
            } finally {
               ResourcePackMeshLoader.clearExternalBoneRotationOffsets();
            }

            ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample var45 = new ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample(
               ResourcePackObjLivingOverrides.toObjModel(sampledMesh), sampledMesh.boneTransforms()
            );
            synchronized (this.runtimeFrameModelCache) {
               this.runtimeFrameModelCache.put(cacheKey, var45);
               return var45;
            }
         } else {
            ResourcePackMeshLoader.pushExternalBoneRotationOffsets(voiceOffsets);

            ResourcePackMeshLoader.MeshData sampledMesh;
            try {
               sampledMesh = animation.sampleFrame(
                  frame, state.field_53448, state.field_53447, includeBoneTransforms, anchorBones.rightBone(), anchorBones.leftBone()
               );
            } finally {
               ResourcePackMeshLoader.clearExternalBoneRotationOffsets();
            }

            ResourcePackObjLivingOverrides.STATS_RUNTIME_FRAME_CACHE_MISSES.incrementAndGet();
            return new ResourcePackObjLivingOverrides.AnimatedObjModel.RuntimeFrameSample(
               ResourcePackObjLivingOverrides.toObjModel(sampledMesh), sampledMesh.boneTransforms()
            );
         }
      }

      private static ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection resolveItemAnchorBonesForState(
         class_10042 state, boolean includeBoneTransforms
      ) {
         if (includeBoneTransforms && state instanceof class_10426) {
            FbxRigConfig config = ResourcePackObjLivingOverrides.resolveRigConfigForState(state);
            if (config == null) {
               return ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection.EMPTY;
            } else {
               String rightBone = normalizeBoneName(firstNonBlank(config.rightItemBone(), config.rightHandBone()));
               String leftBone = normalizeBoneName(firstNonBlank(config.leftItemBone(), config.leftHandBone()));
               return new ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection(rightBone, leftBone, rightBone.hashCode(), leftBone.hashCode());
            }
         } else {
            return ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection.EMPTY;
         }
      }

      private static double distanceSqToCamera(class_10042 state) {
         class_310 client = class_310.method_1551();
         if (client != null && client.field_1773 != null && client.field_1773.method_19418() != null) {
            class_243 camera = client.field_1773.method_19418().method_71156();
            double dx = state.field_53325 - camera.field_1352;
            double dy = state.field_53326 - camera.field_1351;
            double dz = state.field_53327 - camera.field_1350;
            return dx * dx + dy * dy + dz * dz;
         } else {
            return 0.0;
         }
      }

      private static boolean shouldCaptureBoneAnchorsForState(class_10042 state) {
         return state instanceof class_10426;
      }

      private static ResourcePackObjLivingOverrides.ObjModel getInterpolatedTimeFrame(List<ResourcePackObjLivingOverrides.ObjModel> frames, class_10042 state) {
         if (frames.isEmpty()) {
            return null;
         } else if (frames.size() == 1) {
            return frames.get(0);
         } else {
            int currentIndex = ResourcePackObjLivingOverrides.selectTimeBasedFrameIndex(state, frames.size());
            float progress = ResourcePackObjLivingOverrides.selectTimeBasedFrameProgress(state, frames.size());
            int nextIndex = (currentIndex + 1) % frames.size();
            return ResourcePackObjLivingOverrides.ObjModel.blend(frames.get(currentIndex), frames.get(nextIndex), progress);
         }
      }

      private static float clamp01(float value) {
         return Math.max(0.0F, Math.min(1.0F, value));
      }

      private static float quantize(float value, float bucketSize) {
         return !(bucketSize <= 0.0F) && Float.isFinite(value) ? Math.round(value / bucketSize) * bucketSize : value;
      }

      private static Map<String, Matrix4f> copyBoneTransformMap(Map<String, Matrix4f> source) {
         if (source != null && !source.isEmpty()) {
            Map<String, Matrix4f> copy = new HashMap<>(source.size());

            for (Entry<String, Matrix4f> entry : source.entrySet()) {
               Matrix4f matrix = entry.getValue();
               if (matrix != null) {
                  copy.put(entry.getKey(), new Matrix4f(matrix));
               }
            }

            return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
         } else {
            return Map.of();
         }
      }

      private static Map<String, Matrix4f> blendBoneTransforms(Map<String, Matrix4f> from, Map<String, Matrix4f> to, float alpha) {
         if (to != null && !to.isEmpty()) {
            if (from != null && !from.isEmpty()) {
               float clampedAlpha = clamp01(alpha);
               if (clampedAlpha <= 0.001F) {
                  return copyBoneTransformMap(from);
               } else if (clampedAlpha >= 0.999F) {
                  return copyBoneTransformMap(to);
               } else {
                  Map<String, Matrix4f> blended = new HashMap<>(to.size() + from.size());

                  for (Entry<String, Matrix4f> entry : to.entrySet()) {
                     Matrix4f toMatrix = entry.getValue();
                     if (toMatrix != null) {
                        Matrix4f fromMatrix = from.get(entry.getKey());
                        if (fromMatrix == null) {
                           blended.put(entry.getKey(), new Matrix4f(toMatrix));
                        } else {
                           blended.put(entry.getKey(), lerpMatrix(fromMatrix, toMatrix, clampedAlpha));
                        }
                     }
                  }

                  for (Entry<String, Matrix4f> entryx : from.entrySet()) {
                     Matrix4f fromMatrix = entryx.getValue();
                     if (fromMatrix != null) {
                        blended.putIfAbsent(entryx.getKey(), new Matrix4f(fromMatrix));
                     }
                  }

                  return blended.isEmpty() ? Map.of() : Map.copyOf(blended);
               }
            } else {
               return copyBoneTransformMap(to);
            }
         } else {
            return copyBoneTransformMap(from);
         }
      }

      private static Map<String, Matrix4f> blendBoneTransformsUpperBody(Map<String, Matrix4f> from, Map<String, Matrix4f> to, float alpha, float cutoffY) {
         if (to != null && !to.isEmpty()) {
            if (from != null && !from.isEmpty()) {
               float clampedAlpha = clamp01(alpha);
               if (clampedAlpha <= 0.001F) {
                  return copyBoneTransformMap(from);
               } else if (clampedAlpha >= 0.999F) {
                  return copyBoneTransformMap(to);
               } else {
                  Map<String, Matrix4f> blended = new HashMap<>(to.size() + from.size());

                  for (Entry<String, Matrix4f> entry : to.entrySet()) {
                     Matrix4f toMatrix = entry.getValue();
                     if (toMatrix != null) {
                        Matrix4f fromMatrix = from.get(entry.getKey());
                        if (fromMatrix == null) {
                           blended.put(entry.getKey(), new Matrix4f(toMatrix));
                        } else {
                           float y = toMatrix.m31();
                           float localAlpha = isFinite(y) && y <= cutoffY ? 0.0F : clampedAlpha;
                           blended.put(entry.getKey(), localAlpha <= 0.0F ? new Matrix4f(fromMatrix) : lerpMatrix(fromMatrix, toMatrix, localAlpha));
                        }
                     }
                  }

                  for (Entry<String, Matrix4f> entryx : from.entrySet()) {
                     Matrix4f fromMatrix = entryx.getValue();
                     if (fromMatrix != null) {
                        blended.putIfAbsent(entryx.getKey(), new Matrix4f(fromMatrix));
                     }
                  }

                  return blended.isEmpty() ? Map.of() : Map.copyOf(blended);
               }
            } else {
               return copyBoneTransformMap(to);
            }
         } else {
            return copyBoneTransformMap(from);
         }
      }

      private static Matrix4f lerpMatrix(Matrix4f from, Matrix4f to, float alpha) {
         float t = clamp01(alpha);
         return new Matrix4f(
            lerp(from.m00(), to.m00(), t),
            lerp(from.m01(), to.m01(), t),
            lerp(from.m02(), to.m02(), t),
            lerp(from.m03(), to.m03(), t),
            lerp(from.m10(), to.m10(), t),
            lerp(from.m11(), to.m11(), t),
            lerp(from.m12(), to.m12(), t),
            lerp(from.m13(), to.m13(), t),
            lerp(from.m20(), to.m20(), t),
            lerp(from.m21(), to.m21(), t),
            lerp(from.m22(), to.m22(), t),
            lerp(from.m23(), to.m23(), t),
            lerp(from.m30(), to.m30(), t),
            lerp(from.m31(), to.m31(), t),
            lerp(from.m32(), to.m32(), t),
            lerp(from.m33(), to.m33(), t)
         );
      }

      private static float lerp(float a, float b, float t) {
         return a + (b - a) * t;
      }

      private static long composeRuntimeFrameCacheKey(
         String animationName, float frame, float lookPitch, float lookYaw, boolean includeBoneTransforms, int rightItemBoneHash, int leftItemBoneHash
      ) {
         long key = animationName.hashCode() & 4294967295L;
         key = key * 31L ^ Float.floatToIntBits(frame) & 4294967295L;
         key = key * 31L ^ Float.floatToIntBits(lookPitch) & 4294967295L;
         key = key * 31L ^ Float.floatToIntBits(lookYaw) & 4294967295L;
         key = key * 31L ^ (includeBoneTransforms ? 1L : 0L);
         key = key * 31L ^ rightItemBoneHash & 4294967295L;
         return key * 31L ^ leftItemBoneHash & 4294967295L;
      }

      private static String normalizeBoneName(String value) {
         return value == null ? "" : value.trim();
      }

      private static String firstNonBlank(String primary, String fallback) {
         if (primary != null && !primary.isBlank()) {
            return primary;
         } else {
            return fallback == null ? "" : fallback;
         }
      }

      private record AnchorBoneSelection(String rightBone, String leftBone, int rightBoneHash, int leftBoneHash) {
         private static final ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection EMPTY = new ResourcePackObjLivingOverrides.AnimatedObjModel.AnchorBoneSelection(
            "", "", 0, 0
         );
      }

      private static final class AnimationBlendState {
         private String lastAnimationName;
         private ResourcePackObjLivingOverrides.ObjModel previousRenderedModel;
         private float blendStartAge = -9999.0F;
         private Map<String, Matrix4f> previousBoneTransforms = Map.of();
         private Map<String, Matrix4f> lastBoneTransforms = Map.of();
      }

      private record NetworkLipSyncState(float level, long updatedAtMs) {
      }

      private record RuntimeFrameSample(ResourcePackObjLivingOverrides.ObjModel model, Map<String, Matrix4f> boneTransforms) {
      }
   }

   private record AnimationFrame(String animationName, int frameIndex) {
   }

   private record CollisionResolution(double x, double y, double z, float normalX, float normalY, float normalZ, boolean grounded) {
   }

   private record FrameEntry(int index, ResourcePackObjLivingOverrides.ObjModel model) {
   }

   private static final class InMemoryResourceManager implements class_3300 {
      private final Map<class_2960, byte[]> resources;
      private final Set<String> namespaces;
      private final class_3262 resourcePack;
      private final class_3300 fallback;

      private InMemoryResourceManager(Map<class_2960, byte[]> resources, class_3300 fallback) {
         Map<class_2960, byte[]> copied = new HashMap<>();

         for (Entry<class_2960, byte[]> entry : resources.entrySet()) {
            class_2960 id = entry.getKey();
            byte[] bytes = entry.getValue();
            if (id != null && bytes != null && bytes.length != 0) {
               copied.put(id, bytes);
            }
         }

         this.resources = Map.copyOf(copied);
         Set<String> foundNamespaces = new LinkedHashSet<>();

         for (class_2960 id : copied.keySet()) {
            foundNamespaces.add(id.method_12836());
         }

         this.namespaces = Set.copyOf(foundNamespaces);
         this.resourcePack = new ResourcePackObjLivingOverrides.InMemoryResourcePack(this.resources, this.namespaces);
         this.fallback = fallback;
      }

      public Optional<class_3298> method_14486(class_2960 id) {
         byte[] bytes = this.resources.get(id);
         if (bytes == null) {
            return this.fallback != null ? this.fallback.method_14486(id) : Optional.empty();
         } else {
            return Optional.of(new class_3298(this.resourcePack, () -> new ByteArrayInputStream(bytes)));
         }
      }

      public Set<String> method_14487() {
         return this.namespaces;
      }

      public List<class_3298> method_14489(class_2960 id) {
         Optional<class_3298> resource = this.method_14486(id);
         if (resource.isPresent()) {
            return List.of(resource.get());
         } else {
            if (this.fallback != null) {
               try {
                  List<class_3298> fallbackResources = this.fallback.method_14489(id);
                  if (fallbackResources != null && !fallbackResources.isEmpty()) {
                     return fallbackResources;
                  }
               } catch (Exception var4) {
               }
            }

            return List.of();
         }
      }

      public Map<class_2960, class_3298> method_14488(String startingPath, Predicate<class_2960> predicate) {
         String normalizedPrefix = normalizePathPrefix(startingPath);
         Map<class_2960, class_3298> found = new HashMap<>();

         for (Entry<class_2960, byte[]> entry : this.resources.entrySet()) {
            class_2960 id = entry.getKey();
            String path = id.method_12832();
            if ((path.startsWith(normalizedPrefix) || path.equals(startingPath)) && predicate.test(id)) {
               found.put(id, new class_3298(this.resourcePack, () -> new ByteArrayInputStream(entry.getValue())));
            }
         }

         return found;
      }

      public Map<class_2960, List<class_3298>> method_41265(String startingPath, Predicate<class_2960> predicate) {
         String normalizedPrefix = normalizePathPrefix(startingPath);
         Map<class_2960, List<class_3298>> found = new HashMap<>();

         for (Entry<class_2960, byte[]> entry : this.resources.entrySet()) {
            class_2960 id = entry.getKey();
            String path = id.method_12832();
            if ((path.startsWith(normalizedPrefix) || path.equals(startingPath)) && predicate.test(id)) {
               found.put(id, List.of(new class_3298(this.resourcePack, () -> new ByteArrayInputStream(entry.getValue()))));
            }
         }

         return found;
      }

      public Stream<class_3262> method_29213() {
         return Stream.of(this.resourcePack);
      }

      private static String normalizePathPrefix(String path) {
         if (path != null && !path.isBlank()) {
            return path.endsWith("/") ? path : path + "/";
         } else {
            return "";
         }
      }
   }

   private static final class InMemoryResourcePack implements class_3262 {
      private final Map<class_2960, byte[]> resources;
      private final Set<String> namespaces;
      private final class_9224 info = new class_9224(
         "objfbxloader-remote-sync", class_2561.method_43470("Remote Synced Player Models"), class_5352.field_25350, Optional.empty()
      );

      private InMemoryResourcePack(Map<class_2960, byte[]> resources, Set<String> namespaces) {
         this.resources = resources;
         this.namespaces = namespaces;
      }

      public class_7367<InputStream> method_14410(String... segments) {
         return null;
      }

      public class_7367<InputStream> method_14405(class_3264 type, class_2960 id) {
         if (type != class_3264.field_14188) {
            return null;
         } else {
            byte[] bytes = this.resources.get(id);
            return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
         }
      }

      public void method_14408(class_3264 type, String namespace, String prefix, class_7664 consumer) {
      }

      public Set<String> method_14406(class_3264 type) {
         return type == class_3264.field_14188 ? this.namespaces : Set.of();
      }

      public <T> T method_14407(class_7677<T> metaReader) {
         return null;
      }

      public class_9224 method_56926() {
         return this.info;
      }

      public void close() {
      }
   }

   private static final class LocalRagdollInstance {
      private final int id;
      private double x;
      private double y;
      private double z;
      private float velocityX;
      private float velocityY;
      private float velocityZ;
      private float externalForceX;
      private float externalForceY;
      private float externalForceZ;
      private float ageTicks;

      private LocalRagdollInstance(int id, double x, double y, double z) {
         this.id = id;
         this.x = x;
         this.y = y;
         this.z = z;
         this.velocityX = 0.0F;
         this.velocityY = 0.0F;
         this.velocityZ = 0.0F;
         this.externalForceX = 0.0F;
         this.externalForceY = 0.0F;
         this.externalForceZ = 0.0F;
         this.ageTicks = 0.0F;
      }
   }

   private static final class LocomotionPhaseState {
      private float lastAge;
      private float phase;

      private LocomotionPhaseState(float lastAge, float phase) {
         this.lastAge = lastAge;
         this.phase = phase;
      }
   }

   private static final class ModelBuilder {
      private ResourcePackObjLivingOverrides.ObjModel staticModel;
      private final Map<String, List<ResourcePackObjLivingOverrides.FrameEntry>> animations = new HashMap<>();
      private final Map<String, ResourcePackObjLivingOverrides.RuntimeAnimationSource> runtimeAnimationSources = new HashMap<>();
      private final Map<String, ResourcePackMeshLoader.AnimatedMeshData> runtimeAnimations = new HashMap<>();
      private final List<String> timeBasedAnimations = new ArrayList<>();

      private ResourcePackObjLivingOverrides.AnimatedObjModel build() {
         Map<String, List<ResourcePackObjLivingOverrides.ObjModel>> builtAnimations = new HashMap<>();

         for (Entry<String, List<ResourcePackObjLivingOverrides.FrameEntry>> entry : this.animations.entrySet()) {
            List<ResourcePackObjLivingOverrides.FrameEntry> frames = new ArrayList<>(entry.getValue());
            frames.sort(Comparator.comparingInt(ResourcePackObjLivingOverrides.FrameEntry::index));
            List<ResourcePackObjLivingOverrides.ObjModel> models = new ArrayList<>(frames.size());

            for (ResourcePackObjLivingOverrides.FrameEntry frame : frames) {
               models.add(frame.model);
            }

            builtAnimations.put(entry.getKey(), List.copyOf(models));
         }

         return new ResourcePackObjLivingOverrides.AnimatedObjModel(
            this.staticModel, Map.copyOf(builtAnimations), Map.copyOf(this.runtimeAnimations), List.copyOf(this.timeBasedAnimations)
         );
      }
   }

   private record ModelPathParts(String targetPath, String variantPath) {
   }

   private record MotionSample(double x, double y, double z, float age) {
   }

   private record MotionSampleDelta(float verticalSpeed, float horizontalSpeed, float forwardSpeed, float verticalDelta) {
      private static final ResourcePackObjLivingOverrides.MotionSampleDelta ZERO = new ResourcePackObjLivingOverrides.MotionSampleDelta(0.0F, 0.0F, 0.0F, 0.0F);
   }

   private static final class NetworkLipSyncState {
      private final float level;
      private final long updatedAtMs;

      private NetworkLipSyncState(float level, long updatedAtMs) {
         this.level = level;
         this.updatedAtMs = updatedAtMs;
      }

      private float level() {
         return this.level;
      }

      private long updatedAtMs() {
         return this.updatedAtMs;
      }
   }

   public record ObjModel(class_2960 texture, List<ResourcePackMeshLoader.MeshTriangle> triangles, float[] packedRenderStream, int packedTriangleCount) {
      private static ResourcePackObjLivingOverrides.ObjModel empty(class_2960 texture) {
         return new ResourcePackObjLivingOverrides.ObjModel(texture, List.of(), new float[0], 0);
      }

      private void render(class_4587 matrices, class_11659 queue, int light) {
         boolean flatLit = light == 15728880;
         if (this.packedRenderStream != null && this.packedRenderStream.length >= this.packedTriangleCount * 3 * 8 && this.packedTriangleCount > 0) {
            queue.method_73483(
               matrices,
               class_12249.method_75994(this.texture),
               (entry, vertices) -> {
                  for (int triangle = 0; triangle < this.packedTriangleCount; triangle++) {
                     int triOffset = triangle * 3 * 8;

                     for (int corner = 0; corner < 3; corner++) {
                        int base = triOffset + corner * 8;
                        if (flatLit) {
                           ResourcePackMeshLoader.emit(
                              vertices,
                              entry,
                              this.packedRenderStream[base],
                              this.packedRenderStream[base + 1],
                              this.packedRenderStream[base + 2],
                              this.packedRenderStream[base + 3],
                              this.packedRenderStream[base + 4],
                              0.0F,
                              1.0F,
                              0.0F,
                              light
                           );
                        } else {
                           ResourcePackMeshLoader.emit(
                              vertices,
                              entry,
                              this.packedRenderStream[base],
                              this.packedRenderStream[base + 1],
                              this.packedRenderStream[base + 2],
                              this.packedRenderStream[base + 3],
                              this.packedRenderStream[base + 4],
                              this.packedRenderStream[base + 5],
                              this.packedRenderStream[base + 6],
                              this.packedRenderStream[base + 7],
                              light
                           );
                        }
                     }

                     int cBase = triOffset + 16;
                     if (flatLit) {
                        ResourcePackMeshLoader.emit(
                           vertices,
                           entry,
                           this.packedRenderStream[cBase],
                           this.packedRenderStream[cBase + 1],
                           this.packedRenderStream[cBase + 2],
                           this.packedRenderStream[cBase + 3],
                           this.packedRenderStream[cBase + 4],
                           0.0F,
                           1.0F,
                           0.0F,
                           light
                        );
                     } else {
                        ResourcePackMeshLoader.emit(
                           vertices,
                           entry,
                           this.packedRenderStream[cBase],
                           this.packedRenderStream[cBase + 1],
                           this.packedRenderStream[cBase + 2],
                           this.packedRenderStream[cBase + 3],
                           this.packedRenderStream[cBase + 4],
                           this.packedRenderStream[cBase + 5],
                           this.packedRenderStream[cBase + 6],
                           this.packedRenderStream[cBase + 7],
                           light
                        );
                     }
                  }
               }
            );
         } else {
            Map<class_2960, List<ResourcePackMeshLoader.MeshTriangle>> byTexture = new LinkedHashMap<>();

            for (ResourcePackMeshLoader.MeshTriangle triangle : this.triangles) {
               class_2960 triangleTexture = triangle.texture() == null ? this.texture : triangle.texture();
               if (triangleTexture != null) {
                  byTexture.computeIfAbsent(triangleTexture, ignored -> new ArrayList<>()).add(triangle);
               }
            }

            for (Entry<class_2960, List<ResourcePackMeshLoader.MeshTriangle>> textureEntry : byTexture.entrySet()) {
               class_2960 texture = textureEntry.getKey();
               List<ResourcePackMeshLoader.MeshTriangle> textureTriangles = textureEntry.getValue();
               queue.method_73483(matrices, class_12249.method_75994(texture), (entry, vertices) -> {
                  for (ResourcePackMeshLoader.MeshTriangle trianglex : textureTriangles) {
                     if (flatLit) {
                        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.a().pos(), trianglex.a().uv(), up, light);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.b().pos(), trianglex.b().uv(), up, light);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.c().pos(), trianglex.c().uv(), up, light);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.c().pos(), trianglex.c().uv(), up, light);
                     } else {
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.a().pos(), trianglex.a().uv(), trianglex.a().normal(), light);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.b().pos(), trianglex.b().uv(), trianglex.b().normal(), light);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.c().pos(), trianglex.c().uv(), trianglex.c().normal(), light);
                        ResourcePackMeshLoader.emit(vertices, entry, trianglex.c().pos(), trianglex.c().uv(), trianglex.c().normal(), light);
                     }
                  }
               });
            }
         }
      }

      private static ResourcePackObjLivingOverrides.ObjModel blend(
         ResourcePackObjLivingOverrides.ObjModel from, ResourcePackObjLivingOverrides.ObjModel to, float alpha
      ) {
         if (from == null) {
            return to;
         } else if (to == null) {
            return from;
         } else {
            alpha = Math.max(0.0F, Math.min(1.0F, alpha));
            if (alpha <= 0.001F) {
               return from;
            } else if (alpha >= 0.999F) {
               return to;
            } else if (from.packedTriangleCount > 0
               && to.packedTriangleCount > 0
               && from.packedTriangleCount == to.packedTriangleCount
               && from.packedRenderStream != null
               && to.packedRenderStream != null
               && from.packedRenderStream.length == to.packedRenderStream.length) {
               float[] blendedPacked = new float[from.packedRenderStream.length];
               if (NativeAnimationBackend.blendRenderStreams(from.packedRenderStream, to.packedRenderStream, alpha, blendedPacked)) {
                  return new ResourcePackObjLivingOverrides.ObjModel(
                     alpha < 0.5F ? from.texture : to.texture, List.of(), blendedPacked, from.packedTriangleCount
                  );
               } else {
                  int vertexCount = from.packedTriangleCount * 3;

                  for (int v = 0; v < vertexCount; v++) {
                     int base = v * 8;
                     blendedPacked[base] = lerp(from.packedRenderStream[base], to.packedRenderStream[base], alpha);
                     blendedPacked[base + 1] = lerp(from.packedRenderStream[base + 1], to.packedRenderStream[base + 1], alpha);
                     blendedPacked[base + 2] = lerp(from.packedRenderStream[base + 2], to.packedRenderStream[base + 2], alpha);
                     blendedPacked[base + 3] = to.packedRenderStream[base + 3];
                     blendedPacked[base + 4] = to.packedRenderStream[base + 4];
                     blendedPacked[base + 5] = lerp(from.packedRenderStream[base + 5], to.packedRenderStream[base + 5], alpha);
                     blendedPacked[base + 6] = lerp(from.packedRenderStream[base + 6], to.packedRenderStream[base + 6], alpha);
                     blendedPacked[base + 7] = lerp(from.packedRenderStream[base + 7], to.packedRenderStream[base + 7], alpha);
                  }

                  return new ResourcePackObjLivingOverrides.ObjModel(
                     alpha < 0.5F ? from.texture : to.texture, List.of(), blendedPacked, from.packedTriangleCount
                  );
               }
            } else if (from.triangles.size() != to.triangles.size()) {
               return alpha < 0.5F ? from : to;
            } else {
               List<ResourcePackMeshLoader.MeshTriangle> blended = new ArrayList<>(from.triangles.size());

               for (int i = 0; i < from.triangles.size(); i++) {
                  ResourcePackMeshLoader.MeshTriangle a = from.triangles.get(i);
                  ResourcePackMeshLoader.MeshTriangle b = to.triangles.get(i);
                  ResourcePackMeshLoader.MeshVertex va = blendVertex(a.a(), b.a(), alpha);
                  ResourcePackMeshLoader.MeshVertex vb = blendVertex(a.b(), b.b(), alpha);
                  ResourcePackMeshLoader.MeshVertex vc = blendVertex(a.c(), b.c(), alpha);
                  Vector3f normal = new Vector3f(vb.pos()).sub(va.pos()).cross(new Vector3f(vc.pos()).sub(va.pos()));
                  if (normal.lengthSquared() < 1.0E-6F) {
                     normal.set(0.0F, 1.0F, 0.0F);
                  } else {
                     normal.normalize();
                  }

                  class_2960 blendedTexture = b.texture() != null ? b.texture() : a.texture();
                  blended.add(new ResourcePackMeshLoader.MeshTriangle(va, vb, vc, normal, blendedTexture));
               }

               return new ResourcePackObjLivingOverrides.ObjModel(alpha < 0.5F ? from.texture : to.texture, List.copyOf(blended), new float[0], 0);
            }
         }
      }

      private static ResourcePackObjLivingOverrides.ObjModel blendUpperBody(
         ResourcePackObjLivingOverrides.ObjModel from, ResourcePackObjLivingOverrides.ObjModel to, float alpha, float cutoffY
      ) {
         if (from == null) {
            return to;
         } else if (to == null) {
            return from;
         } else {
            alpha = Math.max(0.0F, Math.min(1.0F, alpha));
            if (alpha <= 0.001F) {
               return from;
            } else if (alpha >= 0.999F) {
               return to;
            } else if (from.packedTriangleCount > 0
               && to.packedTriangleCount > 0
               && from.packedTriangleCount == to.packedTriangleCount
               && from.packedRenderStream != null
               && to.packedRenderStream != null
               && from.packedRenderStream.length == to.packedRenderStream.length) {
               float[] blendedPacked = new float[from.packedRenderStream.length];
               int vertexCount = from.packedTriangleCount * 3;

               for (int v = 0; v < vertexCount; v++) {
                  int base = v * 8;
                  float fromY = from.packedRenderStream[base + 1];
                  float toY = to.packedRenderStream[base + 1];
                  boolean upper = Float.isFinite(fromY) && fromY > cutoffY || Float.isFinite(toY) && toY > cutoffY;
                  float localAlpha = upper ? alpha : 0.0F;
                  blendedPacked[base] = lerp(from.packedRenderStream[base], to.packedRenderStream[base], localAlpha);
                  blendedPacked[base + 1] = lerp(fromY, toY, localAlpha);
                  blendedPacked[base + 2] = lerp(from.packedRenderStream[base + 2], to.packedRenderStream[base + 2], localAlpha);
                  blendedPacked[base + 3] = to.packedRenderStream[base + 3];
                  blendedPacked[base + 4] = to.packedRenderStream[base + 4];
                  blendedPacked[base + 5] = lerp(from.packedRenderStream[base + 5], to.packedRenderStream[base + 5], localAlpha);
                  blendedPacked[base + 6] = lerp(from.packedRenderStream[base + 6], to.packedRenderStream[base + 6], localAlpha);
                  blendedPacked[base + 7] = lerp(from.packedRenderStream[base + 7], to.packedRenderStream[base + 7], localAlpha);
               }

               return new ResourcePackObjLivingOverrides.ObjModel(alpha < 0.5F ? from.texture : to.texture, List.of(), blendedPacked, from.packedTriangleCount);
            } else if (from.triangles.size() != to.triangles.size()) {
               return alpha < 0.5F ? from : to;
            } else {
               List<ResourcePackMeshLoader.MeshTriangle> blended = new ArrayList<>(from.triangles.size());

               for (int i = 0; i < from.triangles.size(); i++) {
                  ResourcePackMeshLoader.MeshTriangle a = from.triangles.get(i);
                  ResourcePackMeshLoader.MeshTriangle b = to.triangles.get(i);
                  ResourcePackMeshLoader.MeshVertex va = blendVertexUpper(a.a(), b.a(), alpha, cutoffY);
                  ResourcePackMeshLoader.MeshVertex vb = blendVertexUpper(a.b(), b.b(), alpha, cutoffY);
                  ResourcePackMeshLoader.MeshVertex vc = blendVertexUpper(a.c(), b.c(), alpha, cutoffY);
                  Vector3f normal = new Vector3f(vb.pos()).sub(va.pos()).cross(new Vector3f(vc.pos()).sub(va.pos()));
                  if (normal.lengthSquared() < 1.0E-6F) {
                     normal.set(0.0F, 1.0F, 0.0F);
                  } else {
                     normal.normalize();
                  }

                  class_2960 blendedTexture = b.texture() != null ? b.texture() : a.texture();
                  blended.add(new ResourcePackMeshLoader.MeshTriangle(va, vb, vc, normal, blendedTexture));
               }

               return new ResourcePackObjLivingOverrides.ObjModel(alpha < 0.5F ? from.texture : to.texture, List.copyOf(blended), new float[0], 0);
            }
         }
      }

      private static ResourcePackMeshLoader.MeshVertex blendVertex(ResourcePackMeshLoader.MeshVertex from, ResourcePackMeshLoader.MeshVertex to, float alpha) {
         Vector3f pos = new Vector3f(
            lerp(from.pos().x(), to.pos().x(), alpha), lerp(from.pos().y(), to.pos().y(), alpha), lerp(from.pos().z(), to.pos().z(), alpha)
         );
         Vector2f uv = new Vector2f(lerp(from.uv().x(), to.uv().x(), alpha), lerp(from.uv().y(), to.uv().y(), alpha));
         Vector3f fromNormal = from.normal() == null ? new Vector3f(0.0F, 1.0F, 0.0F) : from.normal();
         Vector3f toNormal = to.normal() == null ? new Vector3f(0.0F, 1.0F, 0.0F) : to.normal();
         Vector3f normal = new Vector3f(
            lerp(fromNormal.x(), toNormal.x(), alpha), lerp(fromNormal.y(), toNormal.y(), alpha), lerp(fromNormal.z(), toNormal.z(), alpha)
         );
         if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(0.0F, 1.0F, 0.0F);
         } else {
            normal.normalize();
         }

         return new ResourcePackMeshLoader.MeshVertex(pos, uv, normal);
      }

      private static ResourcePackMeshLoader.MeshVertex blendVertexUpper(
         ResourcePackMeshLoader.MeshVertex from, ResourcePackMeshLoader.MeshVertex to, float alpha, float cutoffY
      ) {
         float fromY = from.pos().y();
         float toY = to.pos().y();
         boolean upper = Float.isFinite(fromY) && fromY > cutoffY || Float.isFinite(toY) && toY > cutoffY;
         float localAlpha = upper ? alpha : 0.0F;
         Vector3f pos = new Vector3f(
            lerp(from.pos().x(), to.pos().x(), localAlpha), lerp(fromY, toY, localAlpha), lerp(from.pos().z(), to.pos().z(), localAlpha)
         );
         Vector2f uv = new Vector2f(lerp(from.uv().x(), to.uv().x(), localAlpha), lerp(from.uv().y(), to.uv().y(), localAlpha));
         Vector3f fromNormal = from.normal() == null ? new Vector3f(0.0F, 1.0F, 0.0F) : from.normal();
         Vector3f toNormal = to.normal() == null ? new Vector3f(0.0F, 1.0F, 0.0F) : to.normal();
         Vector3f normal = new Vector3f(
            lerp(fromNormal.x(), toNormal.x(), localAlpha), lerp(fromNormal.y(), toNormal.y(), localAlpha), lerp(fromNormal.z(), toNormal.z(), localAlpha)
         );
         if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(0.0F, 1.0F, 0.0F);
         } else {
            normal.normalize();
         }

         return new ResourcePackMeshLoader.MeshVertex(pos, uv, normal);
      }

      private static float lerp(float a, float b, float t) {
         return a + (b - a) * t;
      }
   }

   private record OneShotPlaybackState(String animationName, float startAge) {
   }

   public record OverrideData(
      Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> entityModels,
      Map<class_2960, ResourcePackObjLivingOverrides.AnimatedObjModel> armorModels
   ) {
   }

   private record ProbeCollisionResult(double pushX, double pushY, double pushZ, double normalX, double normalY, double normalZ) {
   }

   private record RagdollCollisionProbe(float offsetX, float offsetY, float offsetZ, float radius, float responseWeight) {
   }

   private record ResolvedTarget(class_2960 id, String configTargetPath) {
   }

   private static enum ResourceKind {
      ENTITY("entity", "models/entity", "entity") {
         @Override
         ResourcePackObjLivingOverrides.ResolvedTarget resolveTarget(String namespace, String path) {
            class_2960 parsed = class_2960.method_43902(namespace, path);
            return new ResourcePackObjLivingOverrides.ResolvedTarget(parsed, path);
         }

         @Override
         boolean isValidId(class_2960 id) {
            return class_2960.method_60656("player").equals(id) || class_7923.field_41177.method_10250(id);
         }
      },
      ARMOR("armor", "models/armor", "armor") {
         @Override
         boolean isValidId(class_2960 id) {
            return class_7923.field_41178.method_10250(id);
         }
      },
      PLAYER_VARIANT("player-variant", "models/players", "entity") {
         @Override
         boolean isValidId(class_2960 id) {
            return id != null;
         }
      };

      private final String logName;
      private final String modelRoot;
      private final String textureFolder;

      private ResourceKind(String logName, String modelRoot, String textureFolder) {
         this.logName = logName;
         this.modelRoot = modelRoot;
         this.textureFolder = textureFolder;
      }

      ResourcePackObjLivingOverrides.ResolvedTarget resolveTarget(String namespace, String path) {
         return new ResourcePackObjLivingOverrides.ResolvedTarget(class_2960.method_43902(namespace, path), path);
      }

      abstract boolean isValidId(class_2960 var1);
   }

   private record RuntimeAnimationSource(class_2960 resourceId, class_3298 resource, String targetPath, String modelPath) {
   }
}
