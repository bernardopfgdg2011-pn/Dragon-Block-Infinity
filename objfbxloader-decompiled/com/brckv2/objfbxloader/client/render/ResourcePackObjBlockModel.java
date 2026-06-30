package com.brckv2.objfbxloader.client.render;

import com.brckv2.objfbxloader.ObjFbxLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_1058;
import net.minecraft.class_1087;
import net.minecraft.class_10889;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import net.minecraft.class_761;
import net.minecraft.class_7923;
import net.minecraft.class_2338.class_2339;

public final class ResourcePackObjBlockModel {
   private static final float BLOCK_MODEL_NORMALIZED_SIZE = 1.0F;
   private static final float TICKS_PER_SECOND = 20.0F;
   private static final float ANIMATION_SAMPLE_RATE = 24.0F;
   private static final float FRAME_BUCKET = 1.0F;
   private static final int FRAME_CACHE_LIMIT = 256;
   private static final int SCAN_RADIUS_HORIZONTAL = 8;
   private static final int SCAN_RADIUS_VERTICAL = 6;
   private static final int MAX_TRACKED_BLOCKS = 64;
   private static final int MAX_RENDERED_BLOCKS_PER_FRAME = 24;
   private static final float MAX_RENDER_DISTANCE = 12.0F;
   private static final float MAX_RENDER_DISTANCE_SQ = 144.0F;
   private static final long SCAN_INTERVAL_TICKS = 40L;
   private static volatile Map<class_2960, ResourcePackObjBlockModel.BlockOverride> activeOverrides = Map.of();
   private static final List<ResourcePackObjBlockModel.TrackedPlacedBlock> trackedPlacedBlocks = new ArrayList<>();
   private static class_638 trackedWorld = null;
   private static int lastScanChunkX = Integer.MIN_VALUE;
   private static int lastScanChunkZ = Integer.MIN_VALUE;
   private static long nextScanTick = Long.MIN_VALUE;

   private ResourcePackObjBlockModel() {
   }

   public static Map<class_2960, ResourcePackObjBlockModel.BlockOverride> loadOverrides(class_3300 resourceManager) {
      Map<class_2960, ResourcePackObjBlockModel.BlockOverride> overrides = new HashMap<>();
      Map<class_2960, class_3298> resources = resourceManager.method_14488(
         "models/block", id -> id.method_12832().startsWith("models/block/") && ResourcePackMeshLoader.isSupportedMeshModelPath(id.method_12832())
      );

      for (Entry<class_2960, class_3298> entry : resources.entrySet()) {
         class_2960 resourceId = entry.getKey();
         String path = resourceId.method_12832();
         String relativeModelPath = path.substring("models/block/".length(), path.length() - extensionLength(path));
         ResourcePackObjBlockModel.ModelPathParts parts = splitModelPath(relativeModelPath);
         class_2960 blockId = class_2960.method_43902(resourceId.method_12836(), parts.targetPath());
         if (blockId != null && class_7923.field_41175.method_10250(blockId)) {
            ResourcePackObjBlockModel.BlockOverride override = loadOverride(
               resourceManager, resourceId, entry.getValue(), parts.targetPath(), parts.modelPath()
            );
            if (override != null && !override.isEmpty()) {
               overrides.put(blockId, override);
            }
         } else {
            ObjFbxLoader.LOGGER.warn("Skipping mesh block model {} because {} is not a registered block.", resourceId, parts.targetPath());
         }
      }

      Map<class_2960, ResourcePackObjBlockModel.BlockOverride> immutable = Map.copyOf(overrides);
      activeOverrides = immutable;
      clearWorldTracking();
      return immutable;
   }

   public static void tick(class_310 client) {
      if (client != null) {
         class_638 world = client.field_1687;
         class_1657 player = client.field_1724;
         if (world != null && player != null && !activeOverrides.isEmpty()) {
            if (world != trackedWorld) {
               clearWorldTracking();
               trackedWorld = world;
            }

            long now = world.method_75260();
            int chunkX = player.method_24515().method_10263() >> 4;
            int chunkZ = player.method_24515().method_10260() >> 4;
            boolean movedChunk = chunkX != lastScanChunkX || chunkZ != lastScanChunkZ;
            if (movedChunk || now >= nextScanTick) {
               rescanPlacedOverrides(world, player.method_24515());
               lastScanChunkX = chunkX;
               lastScanChunkZ = chunkZ;
               nextScanTick = now + 40L;
            }
         } else {
            if (world != trackedWorld) {
               clearWorldTracking();
               trackedWorld = world;
            }
         }
      }
   }

   public static void renderPlacedOverrides(WorldRenderContext context) {
      if (context != null && !trackedPlacedBlocks.isEmpty() && !activeOverrides.isEmpty()) {
         class_310 client = class_310.method_1551();
         if (client != null && client.field_1687 != null && client.field_1773 != null && client.field_1773.method_19418() != null) {
            class_4587 matrices = context.matrices();
            if (matrices != null) {
               class_11659 queue = context.commandQueue();
               class_243 cameraPos = client.field_1773.method_19418().method_71156();
               float animationTicks = (float)client.field_1687.method_75260();
               Map<ResourcePackObjBlockModel.BlockOverride, ResourcePackMeshLoader.MeshData> renderFrameSamples = new IdentityHashMap<>();
               int rendered = 0;

               for (ResourcePackObjBlockModel.TrackedPlacedBlock tracked : trackedPlacedBlocks) {
                  if (tracked.override() != null) {
                     double centerX = tracked.pos().method_10263() + 0.5;
                     double centerY = tracked.pos().method_10264() + 0.5;
                     double centerZ = tracked.pos().method_10260() + 0.5;
                     double dx = centerX - cameraPos.field_1352;
                     double dy = centerY - cameraPos.field_1351;
                     double dz = centerZ - cameraPos.field_1350;
                     if (!(dx * dx + dy * dy + dz * dz > 144.0)) {
                        ResourcePackMeshLoader.MeshData mesh = renderFrameSamples.get(tracked.override());
                        if (mesh == null) {
                           mesh = tracked.override().sampleMesh(animationTicks);
                           renderFrameSamples.put(tracked.override(), mesh);
                        }

                        if (mesh != null && !mesh.triangles().isEmpty()) {
                           int light = class_761.method_23794(client.field_1687, tracked.pos());
                           matrices.method_22903();
                           matrices.method_46416(
                              (float)(tracked.pos().method_10263() - cameraPos.field_1352),
                              (float)(tracked.pos().method_10264() - cameraPos.field_1351),
                              (float)(tracked.pos().method_10260() - cameraPos.field_1350)
                           );
                           renderMesh(mesh, matrices, queue, light);
                           matrices.method_22909();
                           if (++rendered >= 24) {
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void rescanPlacedOverrides(class_638 world, class_2338 center) {
      trackedPlacedBlocks.clear();
      if (!activeOverrides.isEmpty()) {
         int minX = center.method_10263() - 8;
         int maxX = center.method_10263() + 8;
         int minY = Math.max(world.method_31607(), center.method_10264() - 6);
         int maxY = Math.min(world.method_31600(), center.method_10264() + 6);
         int minZ = center.method_10260() - 8;
         int maxZ = center.method_10260() + 8;
         class_2339 mutable = new class_2339();

         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               for (int x = minX; x <= maxX; x++) {
                  mutable.method_10103(x, y, z);
                  class_2680 state = world.method_8320(mutable);
                  if (state != null && !state.method_26215()) {
                     class_2960 blockId = class_7923.field_41175.method_10221(state.method_26204());
                     ResourcePackObjBlockModel.BlockOverride override = activeOverrides.get(blockId);
                     if (override != null && isLikelyVisible(world, mutable)) {
                        trackedPlacedBlocks.add(new ResourcePackObjBlockModel.TrackedPlacedBlock(mutable.method_10062(), override, distanceSq(center, mutable)));
                        if (trackedPlacedBlocks.size() >= 64) {
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void pruneToNearestTracked() {
      if (trackedPlacedBlocks.size() > 64) {
         trackedPlacedBlocks.sort((a, b) -> Double.compare(a.distanceSqToCenter(), b.distanceSqToCenter()));

         while (trackedPlacedBlocks.size() > 64) {
            trackedPlacedBlocks.remove(trackedPlacedBlocks.size() - 1);
         }
      }
   }

   private static boolean isLikelyVisible(class_638 world, class_2338 pos) {
      class_2339 neighborPos = new class_2339();

      for (class_2350 direction : class_2350.values()) {
         neighborPos.method_10103(
            pos.method_10263() + direction.method_10148(), pos.method_10264() + direction.method_10164(), pos.method_10260() + direction.method_10165()
         );
         class_2680 neighbor = world.method_8320(neighborPos);
         if (neighbor == null || neighbor.method_26215() || !neighbor.method_26225()) {
            return true;
         }
      }

      return false;
   }

   private static double distanceSq(class_2338 center, class_2338 pos) {
      double dx = (double)pos.method_10263() - center.method_10263();
      double dy = (double)pos.method_10264() - center.method_10264();
      double dz = (double)pos.method_10260() - center.method_10260();
      return dx * dx + dy * dy + dz * dz;
   }

   private static ResourcePackObjBlockModel.BlockOverride loadOverride(
      class_3300 resourceManager, class_2960 resourceId, class_3298 resource, String targetPath, String modelPath
   ) {
      if (ResourcePackMeshLoader.isAssimpModelPath(resourceId.method_12832())) {
         ResourcePackMeshLoader.AnimatedMeshData animated = ResourcePackMeshLoader.loadAnimatedFbxModel(
            resourceManager, resourceId, resource, "block", targetPath, modelPath, 1.0F, true, "mesh block"
         );
         return animated != null && !animated.isEmpty() ? ResourcePackObjBlockModel.BlockOverride.forAnimated(animated) : null;
      } else {
         ResourcePackMeshLoader.MeshData staticMesh = ResourcePackMeshLoader.loadModel(
            resourceManager, resourceId, resource, "block", targetPath, modelPath, 1.0F, true, "mesh block"
         );
         return staticMesh != null && !staticMesh.triangles().isEmpty() ? ResourcePackObjBlockModel.BlockOverride.forStatic(staticMesh) : null;
      }
   }

   private static void renderMesh(ResourcePackMeshLoader.MeshData mesh, class_4587 matrices, class_11659 queue, int light) {
      if (mesh != null && !mesh.triangles().isEmpty()) {
         Map<class_2960, List<ResourcePackMeshLoader.MeshTriangle>> byTexture = new LinkedHashMap<>();

         for (ResourcePackMeshLoader.MeshTriangle triangle : mesh.triangles()) {
            class_2960 texture = triangle.texture() == null ? mesh.texture() : triangle.texture();
            if (texture != null) {
               byTexture.computeIfAbsent(texture, ignored -> new ArrayList<>()).add(triangle);
            }
         }

         for (Entry<class_2960, List<ResourcePackMeshLoader.MeshTriangle>> entryByTexture : byTexture.entrySet()) {
            class_2960 texture = entryByTexture.getKey();
            List<ResourcePackMeshLoader.MeshTriangle> textureTriangles = entryByTexture.getValue();
            queue.method_73483(matrices, class_12249.method_75994(texture), (entry, vertices) -> {
               for (ResourcePackMeshLoader.MeshTriangle trianglex : textureTriangles) {
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.a(), trianglex.normal(), light);
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.b(), trianglex.normal(), light);
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.c(), trianglex.normal(), light);
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.c(), trianglex.normal(), light);
               }
            });
         }
      }
   }

   private static void clearWorldTracking() {
      trackedPlacedBlocks.clear();
      lastScanChunkX = Integer.MIN_VALUE;
      lastScanChunkZ = Integer.MIN_VALUE;
      nextScanTick = Long.MIN_VALUE;
   }

   private static int extensionLength(String path) {
      return ResourcePackMeshLoader.modelExtensionLength(path);
   }

   private static ResourcePackObjBlockModel.ModelPathParts splitModelPath(String relativeModelPath) {
      String normalized = relativeModelPath.replace('\\', '/');
      int slash = normalized.indexOf(47);
      return slash < 0
         ? new ResourcePackObjBlockModel.ModelPathParts(normalized, normalized)
         : new ResourcePackObjBlockModel.ModelPathParts(normalized.substring(0, slash), normalized);
   }

   public static final class BlockOverride {
      private final ResourcePackMeshLoader.MeshData staticMesh;
      private final ResourcePackMeshLoader.AnimatedMeshData animatedMesh;
      private final Map<Integer, ResourcePackMeshLoader.MeshData> animatedFrameCache = new LinkedHashMap<Integer, ResourcePackMeshLoader.MeshData>(
         96, 0.75F, true
      ) {
         @Override
         protected boolean removeEldestEntry(Entry<Integer, ResourcePackMeshLoader.MeshData> eldest) {
            return this.size() > 256;
         }
      };

      private BlockOverride(ResourcePackMeshLoader.MeshData staticMesh, ResourcePackMeshLoader.AnimatedMeshData animatedMesh) {
         this.staticMesh = staticMesh;
         this.animatedMesh = animatedMesh;
      }

      public static ResourcePackObjBlockModel.BlockOverride forStatic(ResourcePackMeshLoader.MeshData mesh) {
         return new ResourcePackObjBlockModel.BlockOverride(mesh, null);
      }

      public static ResourcePackObjBlockModel.BlockOverride forAnimated(ResourcePackMeshLoader.AnimatedMeshData animation) {
         return new ResourcePackObjBlockModel.BlockOverride(animation.sampleFrame(0.0F), animation);
      }

      public class_1087 wrap(class_1087 original) {
         return new ResourcePackObjBlockModel.EmptyObjBlockStateModel(original);
      }

      public boolean isEmpty() {
         return this.staticMesh == null || this.staticMesh.triangles().isEmpty();
      }

      private ResourcePackMeshLoader.MeshData sampleMesh(float ticks) {
         if (this.animatedMesh == null) {
            return this.staticMesh;
         } else {
            int frameCount = Math.max(1, this.animatedMesh.frameCount());
            float frame = ticks * 24.0F / 20.0F % frameCount;
            float bucketed = Math.round(frame / 1.0F) * 1.0F;
            int key = Float.floatToIntBits(bucketed);
            synchronized (this.animatedFrameCache) {
               ResourcePackMeshLoader.MeshData cached = this.animatedFrameCache.get(key);
               if (cached != null) {
                  return cached;
               }
            }

            ResourcePackMeshLoader.MeshData sampled = this.animatedMesh.sampleFrame(bucketed);
            synchronized (this.animatedFrameCache) {
               this.animatedFrameCache.put(key, sampled);
               return sampled;
            }
         }
      }
   }

   private static final class EmptyObjBlockStateModel implements class_1087 {
      private final class_1087 fallback;

      private EmptyObjBlockStateModel(class_1087 fallback) {
         this.fallback = fallback;
      }

      public void method_68513(class_5819 random, List<class_10889> parts) {
      }

      public class_1058 method_68511() {
         return this.fallback.method_68511();
      }
   }

   private record ModelPathParts(String targetPath, String modelPath) {
   }

   private record TrackedPlacedBlock(class_2338 pos, ResourcePackObjBlockModel.BlockOverride override, double distanceSqToCenter) {
   }
}
