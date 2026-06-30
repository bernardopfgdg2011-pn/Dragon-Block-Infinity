package com.brckv2.objfbxloader.client.render;

import com.brckv2.objfbxloader.ObjFbxLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.class_10439;
import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_10515;
import net.minecraft.class_11566;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import net.minecraft.class_10444.class_10445;
import net.minecraft.class_10444.class_10446;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ResourcePackObjItemModel extends WrapperBakedItemModel {
   private static final float ITEM_MODEL_NORMALIZED_SIZE = 0.82F;
   private static final float TICKS_PER_SECOND = 20.0F;
   private static final float ANIMATION_SAMPLE_RATE = 24.0F;
   private final ResourcePackObjItemModel.RenderableObjModel model;
   private final Supplier<Vector3fc[]> vertices;
   private static volatile Set<class_2960> loadedCustomItemIds = Set.of();

   public ResourcePackObjItemModel(class_10439 wrapped, ResourcePackObjItemModel.RenderableObjModel model) {
      super(wrapped);
      this.model = model;
      this.vertices = model::vertices;
   }

   public static Map<class_2960, ResourcePackObjItemModel.RenderableObjModel> loadOverrides(class_3300 resourceManager) {
      Map<class_2960, ResourcePackObjItemModel.RenderableObjModel> overrides = new HashMap<>();
      loadRootOverrides(resourceManager, "models/item", ResourcePackObjItemModel.RootKind.ITEM, "item", 0.82F, overrides);
      loadRootOverrides(resourceManager, "models/block", ResourcePackObjItemModel.RootKind.BLOCK, "block", 0.82F, overrides);
      loadedCustomItemIds = overrides.isEmpty() ? Set.of() : Set.copyOf(new LinkedHashSet<>(overrides.keySet()));
      return Map.copyOf(overrides);
   }

   public static Set<class_2960> getLoadedCustomItemIds() {
      return loadedCustomItemIds;
   }

   public static boolean isCustomModelItem(class_2960 itemId) {
      return itemId != null && loadedCustomItemIds.contains(itemId);
   }

   private static void loadRootOverrides(
      class_3300 resourceManager,
      String root,
      ResourcePackObjItemModel.RootKind rootKind,
      String textureFolder,
      float normalizedSize,
      Map<class_2960, ResourcePackObjItemModel.RenderableObjModel> out
   ) {
      Map<class_2960, class_3298> resources = resourceManager.method_14488(
         root, id -> id.method_12832().startsWith(root + "/") && ResourcePackMeshLoader.isSupportedMeshModelPath(id.method_12832())
      );

      for (Entry<class_2960, class_3298> entry : resources.entrySet()) {
         class_2960 resourceId = entry.getKey();
         String path = resourceId.method_12832();
         String relativeModelPath = path.substring((root + "/").length(), path.length() - extensionLength(path));
         ResourcePackObjItemModel.ModelPathParts parts = splitModelPath(relativeModelPath);
         class_2960 itemId = rootKind.resolveItemId(resourceId.method_12836(), parts.targetPath());
         if (itemId == null) {
            ObjFbxLoader.LOGGER
               .warn(
                  "Skipping mesh {} model {} because target {} is not a registered item/block.", new Object[]{rootKind.logName, resourceId, parts.targetPath()}
               );
         } else {
            ResourcePackObjItemModel.RenderableObjModel candidate = loadRenderableModel(
               resourceManager, resourceId, entry.getValue(), textureFolder, parts.targetPath(), parts.modelPath(), rootKind.logName, normalizedSize
            );
            if (candidate != null && candidate.triangleCount() != 0) {
               ResourcePackObjItemModel.RenderableObjModel existing = out.get(itemId);
               if (existing == null || !existing.isAnimated() && candidate.isAnimated()) {
                  out.put(itemId, candidate);
               }
            }
         }
      }
   }

   public void method_65584(
      class_10444 state, class_1799 stack, class_10442 resolver, class_811 displayContext, class_638 world, class_11566 heldItemContext, int seed
   ) {
      state.method_70946(this);
      class_10446 layer = state.method_65601();
      layer.method_67995(this.vertices);
      Object renderData = this.model.createRenderData(world);
      layer.method_65617(this.model, renderData);
      if (stack.method_7958()) {
         layer.method_65615(class_10445.field_55342);
      }

      if (stack.method_7958() || this.model.isAnimated()) {
         state.method_70947();
      }
   }

   private static ResourcePackObjItemModel.RenderableObjModel loadRenderableModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      String logName,
      float normalizedSize
   ) {
      if (ResourcePackMeshLoader.isAssimpModelPath(resourceId.method_12832())) {
         ResourcePackMeshLoader.AnimatedMeshData animation = ResourcePackMeshLoader.loadAnimatedFbxModel(
            resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, true, logName
         );
         return animation != null && !animation.isEmpty() ? new ResourcePackObjItemModel.AnimatedObjModel(animation) : null;
      } else {
         ResourcePackMeshLoader.MeshData mesh = ResourcePackMeshLoader.loadModel(
            resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, true, logName
         );
         ResourcePackObjItemModel.StaticObjModel model = toStaticObjModel(mesh);
         return model.triangleCount() == 0 ? null : model;
      }
   }

   private static ResourcePackObjItemModel.StaticObjModel toStaticObjModel(ResourcePackMeshLoader.MeshData mesh) {
      List<ResourcePackObjItemModel.Triangle> triangles = new ArrayList<>(mesh.triangles().size());

      for (ResourcePackMeshLoader.MeshTriangle triangle : mesh.triangles()) {
         triangles.add(
            new ResourcePackObjItemModel.Triangle(
               new ResourcePackObjItemModel.ObjVertex(triangle.a().pos(), triangle.a().uv()),
               new ResourcePackObjItemModel.ObjVertex(triangle.b().pos(), triangle.b().uv()),
               new ResourcePackObjItemModel.ObjVertex(triangle.c().pos(), triangle.c().uv()),
               triangle.normal(),
               triangle.texture()
            )
         );
      }

      return new ResourcePackObjItemModel.StaticObjModel(mesh.texture(), List.copyOf(triangles), mesh.vertices());
   }

   private static int extensionLength(String path) {
      return ResourcePackMeshLoader.modelExtensionLength(path);
   }

   private static ResourcePackObjItemModel.ModelPathParts splitModelPath(String relativeModelPath) {
      String normalized = relativeModelPath.replace('\\', '/');
      int slash = normalized.indexOf(47);
      return slash < 0
         ? new ResourcePackObjItemModel.ModelPathParts(normalized, normalized)
         : new ResourcePackObjItemModel.ModelPathParts(normalized.substring(0, slash), normalized);
   }

   private static float resolveAnimationTicks(class_638 world) {
      return world != null ? (float)world.method_75260() : (float)(System.nanoTime() / 1.0E9 * 20.0);
   }

   private static void renderTriangles(
      class_2960 defaultTexture, List<ResourcePackObjItemModel.Triangle> triangles, class_811 displayContext, class_4587 matrices, class_11659 queue, int light
   ) {
      if (!triangles.isEmpty()) {
         matrices.method_22903();
         applyDisplayTweaks(displayContext, matrices);
         Map<class_2960, List<ResourcePackObjItemModel.Triangle>> byTexture = new LinkedHashMap<>();

         for (ResourcePackObjItemModel.Triangle triangle : triangles) {
            class_2960 texture = triangle.texture() == null ? defaultTexture : triangle.texture();
            if (texture != null) {
               byTexture.computeIfAbsent(texture, ignored -> new ArrayList<>()).add(triangle);
            }
         }

         for (Entry<class_2960, List<ResourcePackObjItemModel.Triangle>> entryByTexture : byTexture.entrySet()) {
            class_2960 texture = entryByTexture.getKey();
            List<ResourcePackObjItemModel.Triangle> textureTriangles = entryByTexture.getValue();
            queue.method_73483(matrices, class_12249.method_75994(texture), (entry, vertices) -> {
               for (ResourcePackObjItemModel.Triangle trianglex : textureTriangles) {
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.a().pos(), trianglex.a().uv(), trianglex.normal(), light);
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.b().pos(), trianglex.b().uv(), trianglex.normal(), light);
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.c().pos(), trianglex.c().uv(), trianglex.normal(), light);
                  ResourcePackMeshLoader.emit(vertices, entry, trianglex.c().pos(), trianglex.c().uv(), trianglex.normal(), light);
               }
            });
         }

         matrices.method_22909();
      }
   }

   private static void applyDisplayTweaks(class_811 displayContext, class_4587 matrices) {
      if (displayContext == class_811.field_4317) {
         matrices.method_49278(class_7833.field_40714.rotationDegrees(18.0F), 0.5F, 0.5F, 0.5F);
         matrices.method_49278(class_7833.field_40716.rotationDegrees(210.0F), 0.5F, 0.5F, 0.5F);
         matrices.method_22905(1.1F, 1.1F, 1.1F);
      } else if (displayContext.method_29998()) {
         matrices.method_49278(class_7833.field_40716.rotationDegrees(displayContext.method_67675() ? 25.0F : -25.0F), 0.5F, 0.5F, 0.5F);
         matrices.method_22905(0.95F, 0.95F, 0.95F);
      } else if (displayContext == class_811.field_4318) {
         matrices.method_22905(0.55F, 0.55F, 0.55F);
         matrices.method_46416(0.42F, 0.05F, 0.42F);
      }
   }

   public static final class AnimatedObjModel implements ResourcePackObjItemModel.RenderableObjModel {
      private static final float FRAME_BUCKET = 1.0F;
      private static final int FRAME_CACHE_LIMIT = 128;
      private final ResourcePackMeshLoader.AnimatedMeshData animation;
      private final Vector3fc[] vertices;
      private final Map<Integer, ResourcePackObjItemModel.StaticObjModel> cachedFrames = new LinkedHashMap<Integer, ResourcePackObjItemModel.StaticObjModel>(
         128, 0.75F, true
      ) {
         @Override
         protected boolean removeEldestEntry(Entry<Integer, ResourcePackObjItemModel.StaticObjModel> eldest) {
            return this.size() > 128;
         }
      };

      private AnimatedObjModel(ResourcePackMeshLoader.AnimatedMeshData animation) {
         this.animation = animation;
         this.vertices = animation.sampleFrame(0.0F).vertices();
      }

      @Override
      public Vector3fc[] vertices() {
         return this.vertices;
      }

      @Override
      public Object createRenderData(class_638 world) {
         int frameCount = Math.max(1, this.animation.frameCount());
         float ticks = ResourcePackObjItemModel.resolveAnimationTicks(world);
         float frame = ticks * 24.0F / 20.0F % frameCount;
         return new ResourcePackObjItemModel.AnimatedObjModel.AnimationRenderData(frame);
      }

      @Override
      public boolean isAnimated() {
         return true;
      }

      @Override
      public int triangleCount() {
         ResourcePackObjItemModel.StaticObjModel frame0 = this.resolveFrame(0.0F);
         return frame0 == null ? 0 : frame0.triangleCount();
      }

      public void method_65694(Object data, class_811 displayContext, class_4587 matrices, class_11659 queue, int light, int overlay, boolean glint, int tint) {
         float framePosition;
         if (data instanceof ResourcePackObjItemModel.AnimatedObjModel.AnimationRenderData animationRenderData) {
            framePosition = animationRenderData.framePosition();
         } else {
            framePosition = ResourcePackObjItemModel.resolveAnimationTicks(class_310.method_1551().field_1687) * 24.0F / 20.0F;
         }

         ResourcePackObjItemModel.StaticObjModel model = this.resolveFrame(framePosition);
         if (model != null) {
            ResourcePackObjItemModel.renderTriangles(model.defaultTexture, model.triangles, displayContext, matrices, queue, light);
         }
      }

      public void method_72175(Consumer<Vector3fc> consumer) {
         for (Vector3fc vertex : this.vertices) {
            consumer.accept(vertex);
         }
      }

      public Object method_65695(class_1799 stack) {
         return null;
      }

      private ResourcePackObjItemModel.StaticObjModel resolveFrame(float framePosition) {
         int frameCount = Math.max(1, this.animation.frameCount());
         float wrapped = framePosition;
         if (frameCount > 0) {
            wrapped = framePosition % frameCount;
            if (wrapped < 0.0F) {
               wrapped += frameCount;
            }
         }

         float bucketed = Math.round(wrapped / 1.0F) * 1.0F;
         int key = Float.floatToIntBits(bucketed);
         synchronized (this.cachedFrames) {
            ResourcePackObjItemModel.StaticObjModel cached = this.cachedFrames.get(key);
            if (cached != null) {
               return cached;
            }
         }

         ResourcePackMeshLoader.MeshData sampled = this.animation.sampleFrame(bucketed);
         ResourcePackObjItemModel.StaticObjModel built = ResourcePackObjItemModel.toStaticObjModel(sampled);
         synchronized (this.cachedFrames) {
            this.cachedFrames.put(key, built);
            return built;
         }
      }

      private record AnimationRenderData(float framePosition) {
      }
   }

   private record ModelPathParts(String targetPath, String modelPath) {
   }

   public record ObjVertex(Vector3f pos, Vector2f uv) {
   }

   public interface RenderableObjModel extends class_10515<Object> {
      Vector3fc[] vertices();

      Object createRenderData(class_638 var1);

      boolean isAnimated();

      int triangleCount();
   }

   private static enum RootKind {
      ITEM("item") {
         @Override
         class_2960 resolveItemId(String namespace, String targetPath) {
            class_2960 id = class_2960.method_43902(namespace, targetPath);
            return id != null && class_7923.field_41178.method_10250(id) ? id : null;
         }
      },
      BLOCK("block") {
         @Override
         class_2960 resolveItemId(String namespace, String targetPath) {
            class_2960 blockId = class_2960.method_43902(namespace, targetPath);
            if (blockId != null && class_7923.field_41175.method_10250(blockId)) {
               class_1792 asItem = ((class_2248)class_7923.field_41175.method_63535(blockId)).method_8389();
               return asItem == class_1802.field_8162 ? null : class_7923.field_41178.method_10221(asItem);
            } else {
               return null;
            }
         }
      };

      private final String logName;

      private RootKind(String logName) {
         this.logName = logName;
      }

      abstract class_2960 resolveItemId(String var1, String var2);
   }

   public static final class StaticObjModel implements ResourcePackObjItemModel.RenderableObjModel {
      private final class_2960 defaultTexture;
      private final List<ResourcePackObjItemModel.Triangle> triangles;
      private final Vector3fc[] vertices;

      private StaticObjModel(class_2960 texture, List<ResourcePackObjItemModel.Triangle> triangles, Vector3fc[] vertices) {
         this.defaultTexture = texture;
         this.triangles = triangles;
         this.vertices = vertices;
      }

      @Override
      public Vector3fc[] vertices() {
         return this.vertices;
      }

      @Override
      public Object createRenderData(class_638 world) {
         return null;
      }

      @Override
      public boolean isAnimated() {
         return false;
      }

      @Override
      public int triangleCount() {
         return this.triangles.size();
      }

      public void method_65694(Object data, class_811 displayContext, class_4587 matrices, class_11659 queue, int light, int overlay, boolean glint, int tint) {
         ResourcePackObjItemModel.renderTriangles(this.defaultTexture, this.triangles, displayContext, matrices, queue, light);
      }

      public void method_72175(Consumer<Vector3fc> consumer) {
         for (Vector3fc vertex : this.vertices) {
            consumer.accept(vertex);
         }
      }

      public Object method_65695(class_1799 stack) {
         return null;
      }
   }

   public record Triangle(
      ResourcePackObjItemModel.ObjVertex a, ResourcePackObjItemModel.ObjVertex b, ResourcePackObjItemModel.ObjVertex c, Vector3f normal, class_2960 texture
   ) {
   }
}
