package com.brckv2.objfbxloader.client.render;

import com.brckv2.objfbxloader.ObjFbxLoader;
import com.brckv2.objfbxloader.PlatformPaths;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AITexel;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.assimp.AIVector3D.Buffer;

public final class ResourcePackMeshLoader {
   private static final boolean ASSIMP_AVAILABLE;
   private static final int FBX_IMPORT_FLAGS = 8391242;
   private static volatile boolean assimpReleaseFailureLogged;
   private static final int ANIMATION_SAMPLE_RATE = 24;
   private static final int[] FBX_MATERIAL_TEXTURE_TYPES;
   private static final int[] FBX_COLOR_TEXTURE_TYPES;
   private static final Set<String> ASSIMP_MODEL_EXTENSIONS;
   private static final String[] TEXTURE_EXTENSIONS;
   private static final Matrix4f IDENTITY_MATRIX;
   private static final ThreadLocal<Map<String, Vector3f>> EXTERNAL_BONE_ROTATION_OFFSETS;
   private static final int BONE_CACHE_FORMAT_VERSION = 1;
   private static final Path BONE_CACHE_DIR;
   private static final AtomicLong STATS_UNCACHED_SAMPLES;
   private static final AtomicLong STATS_DISK_CACHE_LOAD_HITS;
   private static final AtomicLong STATS_DISK_CACHE_LOAD_MISSES;
   private static final AtomicLong STATS_DISK_CACHE_SAVES;
   private static final Map<ResourcePackMeshLoader.MeshBundle, ResourcePackMeshLoader.NativeSkinScratch> NATIVE_SKIN_SCRATCH_BY_BUNDLE;
   private static final Set<class_2960> RUNTIME_GENERATED_TEXTURE_IDS;
   private static final Map<class_2960, byte[]> RUNTIME_GENERATED_TEXTURE_BYTES;

   public static void pushExternalBoneRotationOffsets(Map<String, Vector3f> offsets) {
      if (offsets != null && !offsets.isEmpty()) {
         EXTERNAL_BONE_ROTATION_OFFSETS.set(offsets);
      } else {
         EXTERNAL_BONE_ROTATION_OFFSETS.remove();
      }
   }

   public static void clearExternalBoneRotationOffsets() {
      EXTERNAL_BONE_ROTATION_OFFSETS.remove();
   }

   private ResourcePackMeshLoader() {
   }

   public static void setAnimationCachingEnabled(boolean enabled) {
   }

   public static boolean isAnimationCachingEnabled() {
      return false;
   }

   public static String cacheStatsSummary() {
      return "MeshLoader cache stats: neutralCache=off, lookCache=off, uncached="
         + STATS_UNCACHED_SAMPLES.get()
         + ", diskHit="
         + STATS_DISK_CACHE_LOAD_HITS.get()
         + ", diskMiss="
         + STATS_DISK_CACHE_LOAD_MISSES.get()
         + ", diskSave="
         + STATS_DISK_CACHE_SAVES.get()
         + ", caching=false";
   }

   public static void resetCacheStats() {
      STATS_UNCACHED_SAMPLES.set(0L);
      STATS_DISK_CACHE_LOAD_HITS.set(0L);
      STATS_DISK_CACHE_LOAD_MISSES.set(0L);
      STATS_DISK_CACHE_SAVES.set(0L);
   }

   public static void clearRuntimeGeneratedTextures(class_310 client) {
      List<class_2960> ids = new ArrayList<>(RUNTIME_GENERATED_TEXTURE_IDS);
      if (!ids.isEmpty()) {
         RUNTIME_GENERATED_TEXTURE_IDS.removeAll(ids);

         for (class_2960 id : ids) {
            if (id != null) {
               RUNTIME_GENERATED_TEXTURE_BYTES.remove(id);
            }
         }

         if (client != null && client.method_1531() != null) {
            Runnable clearTask = () -> {
               for (class_2960 idx : ids) {
                  if (idx != null) {
                     try {
                        client.method_1531().method_4615(idx);
                     } catch (Throwable var5) {
                     }
                  }
               }
            };
            if (client.method_18854()) {
               clearTask.run();
            } else {
               client.execute(clearTask);
            }
         }
      }
   }

   public static byte[] getRuntimeGeneratedTextureBytes(class_2960 id) {
      if (id == null) {
         return new byte[0];
      } else {
         byte[] bytes = RUNTIME_GENERATED_TEXTURE_BYTES.get(id);
         return bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
      }
   }

   public static ResourcePackMeshLoader.MeshData loadModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) {
      try {
         return isAssimpModelPath(resourceId.method_12832())
            ? loadFbxModel(resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, centerToCube, logName)
            : loadObjModel(resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, centerToCube, logName);
      } catch (NumberFormatException | IOException var10) {
         ObjFbxLoader.LOGGER.warn("Could not load {} model {}.", new Object[]{logName, resourceId, var10});
         return ResourcePackMeshLoader.MeshData.empty(defaultTexture(resourceId.method_12836(), textureFolder, targetPath));
      }
   }

   public static List<ResourcePackMeshLoader.MeshData> loadAnimatedFbxFrames(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) {
      try {
         return loadFbxAnimatedFrames(resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, centerToCube, logName);
      } catch (NumberFormatException | IOException var10) {
         ObjFbxLoader.LOGGER.warn("Could not load {} animated FBX model {}.", new Object[]{logName, resourceId, var10});
         return List.of(ResourcePackMeshLoader.MeshData.empty(defaultTexture(resourceId.method_12836(), textureFolder, targetPath)));
      }
   }

   public static ResourcePackMeshLoader.AnimatedMeshData loadAnimatedFbxModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) {
      try {
         return loadFbxAnimatedModel(resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, centerToCube, logName);
      } catch (NumberFormatException | IOException var10) {
         ObjFbxLoader.LOGGER.warn("Could not load {} animated FBX model {}.", new Object[]{logName, resourceId, var10});
         return ResourcePackMeshLoader.AnimatedMeshData.empty(defaultTexture(resourceId.method_12836(), textureFolder, targetPath));
      }
   }

   public static class_2960 findTexture(
      class_3300 resourceManager, String namespace, String textureFolder, String targetPath, String modelRoot, String modelPath, String logName
   ) {
      class_2960 mtlId = class_2960.method_43902(namespace, modelRoot + "/" + modelPath + ".mtl");
      if (mtlId != null) {
         class_3298 resource = (class_3298)resourceManager.method_14486(mtlId).orElse(null);
         if (resource != null) {
            class_2960 mtlTexture = readMtlTexture(resourceManager, resource, namespace, textureFolder, targetPath, logName);
            if (mtlTexture != null) {
               return mtlTexture;
            }
         }
      }

      class_2960 broadFallback = resolveBroadTextureFallback(resourceManager, namespace, textureFolder, targetPath, modelPath);
      return broadFallback != null ? broadFallback : defaultTexture(namespace, textureFolder, targetPath);
   }

   public static class_2960 readMtlTexture(
      class_3300 resourceManager, class_3298 resource, String namespace, String textureFolder, String targetPath, String logName
   ) {
      String line;
      try (BufferedReader reader = resource.method_43039()) {
         while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.startsWith("map_Kd ")) {
               class_2960 texture = resolveExistingTextureIdentifier(resourceManager, namespace, textureFolder, trimmed.substring("map_Kd ".length()).trim());
               if (texture != null) {
                  return texture;
               }

               ObjFbxLoader.LOGGER.warn("Ignoring invalid {} texture name for {}.", logName, targetPath);
            }
         }
      } catch (IOException var13) {
         ObjFbxLoader.LOGGER.warn("Could not read material for {} model {}.", new Object[]{logName, targetPath, var13});
      }

      return null;
   }

   public static class_2960 defaultTexture(String namespace, String textureFolder, String targetPath) {
      class_2960 texture = class_2960.method_43902(namespace, "textures/" + textureFolder + "/" + targetPath + ".png");
      return texture != null ? texture : class_2960.method_60656("textures/missingno.png");
   }

   public static void emit(class_4588 vertices, class_4665 entry, ResourcePackMeshLoader.MeshVertex vertex, Vector3f normal, int light) {
      Vector3f resolvedNormal = vertex.normal() != null ? vertex.normal() : normal;
      emit(vertices, entry, vertex.pos, vertex.uv, resolvedNormal, light);
   }

   public static void emit(class_4588 vertices, class_4665 entry, Vector3fc pos, Vector2f uv, Vector3f normal, int light) {
      vertices.method_56824(entry, pos.x(), pos.y(), pos.z())
         .method_1336(255, 255, 255, 255)
         .method_22913(uv.x(), uv.y())
         .method_22922(class_4608.field_21444)
         .method_60803(light)
         .method_60831(entry, normal.x(), normal.y(), normal.z());
   }

   public static void emit(class_4588 vertices, class_4665 entry, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
      vertices.method_56824(entry, x, y, z)
         .method_1336(255, 255, 255, 255)
         .method_22913(u, v)
         .method_22922(class_4608.field_21444)
         .method_60803(light)
         .method_60831(entry, nx, ny, nz);
   }

   private static ResourcePackMeshLoader.MeshData loadObjModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) throws IOException {
      List<Vector3f> positions = new ArrayList<>();
      List<Vector2f> uvs = new ArrayList<>();
      List<ResourcePackMeshLoader.RawTriangle> rawTriangles = new ArrayList<>();

      String line;
      try (BufferedReader reader = resource.method_43039()) {
         while ((line = reader.readLine()) != null) {
            if (line.startsWith("v ")) {
               String[] parts = line.split("\\s+");
               positions.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
            } else if (line.startsWith("vt ")) {
               String[] parts = line.split("\\s+");
               uvs.add(new Vector2f(Float.parseFloat(parts[1]), 1.0F - Float.parseFloat(parts[2])));
            } else if (line.startsWith("f ")) {
               String[] parts = line.split("\\s+");
               if (parts.length >= 4) {
                  ResourcePackMeshLoader.FacePoint first = parseFacePoint(parts[1], positions.size(), uvs.size());

                  for (int i = 2; i < parts.length - 1; i++) {
                     rawTriangles.add(
                        new ResourcePackMeshLoader.RawTriangle(
                           first, parseFacePoint(parts[i], positions.size(), uvs.size()), parseFacePoint(parts[i + 1], positions.size(), uvs.size()), null
                        )
                     );
                  }
               }
            }
         }
      }

      class_2960 texture = findTexture(resourceManager, resourceId.method_12836(), textureFolder, targetPath, modelRootFor(resourceId), modelPath, logName);
      ResourcePackMeshLoader.ModelTransform transformData = loadModelTransform(resourceManager, resourceId);
      return normalizeModel(rawTriangles, positions, uvs, texture, normalizedSize, centerToCube, transformData, Map.of());
   }

   private static ResourcePackMeshLoader.MeshData loadFbxModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) throws IOException {
      ensureAssimpAvailable();

      byte[] bytes;
      try (InputStream stream = resource.method_14482()) {
         bytes = stream.readAllBytes();
      }

      ByteBuffer data = BufferUtils.createByteBuffer(bytes.length);
      data.put(bytes).flip();
      String importFormatHint = assimpFormatHint(resourceId);
      AIScene scene = Assimp.aiImportFileFromMemory(data, 8391242, importFormatHint);
      if (scene == null) {
         throw new IOException("Assimp import failed (" + importFormatHint + "): " + Assimp.aiGetErrorString());
      } else {
         ResourcePackMeshLoader.MeshData var40;
         try {
            List<Vector3f> positions = new ArrayList<>();
            List<Vector2f> uvs = new ArrayList<>();
            List<ResourcePackMeshLoader.RawTriangle> rawTriangles = new ArrayList<>();
            ResourcePackMeshLoader.FbxTextureAssignment textureAssignment = resolveFbxTextureAssignment(
               resourceManager, resourceId, scene, textureFolder, targetPath, modelPath, logName
            );
            PointerBuffer meshPointers = scene.mMeshes();
            if (meshPointers != null) {
               for (int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++) {
                  AIMesh mesh = AIMesh.create(meshPointers.get(meshIndex));
                  class_2960 meshTexture = textureAssignment.materialTextures().get(mesh.mMaterialIndex());
                  int vertexOffset = positions.size();
                  Buffer meshVertices = mesh.mVertices();
                  Buffer texCoords = mesh.mTextureCoords(0);

                  for (int vertexIndex = 0; vertexIndex < mesh.mNumVertices(); vertexIndex++) {
                     AIVector3D vertex = (AIVector3D)meshVertices.get(vertexIndex);
                     positions.add(new Vector3f(vertex.x(), vertex.y(), vertex.z()));
                     if (texCoords != null) {
                        AIVector3D uv = (AIVector3D)texCoords.get(vertexIndex);
                        uvs.add(new Vector2f(uv.x(), uv.y()));
                     } else {
                        uvs.add(new Vector2f());
                     }
                  }

                  org.lwjgl.assimp.AIFace.Buffer faces = mesh.mFaces();

                  for (int faceIndex = 0; faceIndex < mesh.mNumFaces(); faceIndex++) {
                     AIFace face = (AIFace)faces.get(faceIndex);
                     if (face.mNumIndices() == 3) {
                        IntBuffer indices = face.mIndices();
                        rawTriangles.add(
                           new ResourcePackMeshLoader.RawTriangle(
                              new ResourcePackMeshLoader.FacePoint(vertexOffset + indices.get(0), vertexOffset + indices.get(0)),
                              new ResourcePackMeshLoader.FacePoint(vertexOffset + indices.get(1), vertexOffset + indices.get(1)),
                              new ResourcePackMeshLoader.FacePoint(vertexOffset + indices.get(2), vertexOffset + indices.get(2)),
                              meshTexture
                           )
                        );
                     }
                  }
               }
            }

            FbxRigConfig rigConfig = FbxRigConfig.load(resourceManager, resourceId.method_12836(), targetPath);
            Map<String, Matrix4f> boneTransforms = captureBindPoseBones(scene, rigConfig);
            class_2960 texture = textureAssignment.modelTexture();
            ResourcePackMeshLoader.ModelTransform transformData = loadModelTransform(resourceManager, resourceId);
            var40 = normalizeModel(rawTriangles, positions, uvs, texture, normalizedSize, centerToCube, transformData, boneTransforms);
         } finally {
            safeReleaseImport(scene);
         }

         return var40;
      }
   }

   private static List<ResourcePackMeshLoader.MeshData> loadFbxAnimatedFrames(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) throws IOException {
      ResourcePackMeshLoader.AnimatedMeshData animated = loadFbxAnimatedModel(
         resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, centerToCube, logName
      );
      if (animated.isEmpty()) {
         return List.of(ResourcePackMeshLoader.MeshData.empty(defaultTexture(resourceId.method_12836(), textureFolder, targetPath)));
      } else {
         int frameCount = Math.max(1, animated.frameCount());
         List<ResourcePackMeshLoader.MeshData> frames = new ArrayList<>(frameCount);

         for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            frames.add(animated.sampleFrame(frameIndex));
         }

         return List.copyOf(frames);
      }
   }

   private static ResourcePackMeshLoader.AnimatedMeshData loadFbxAnimatedModel(
      class_3300 resourceManager,
      class_2960 resourceId,
      class_3298 resource,
      String textureFolder,
      String targetPath,
      String modelPath,
      float normalizedSize,
      boolean centerToCube,
      String logName
   ) throws IOException {
      ensureAssimpAvailable();

      byte[] bytes;
      try (InputStream stream = resource.method_14482()) {
         bytes = stream.readAllBytes();
      }

      ByteBuffer data = BufferUtils.createByteBuffer(bytes.length);
      data.put(bytes).flip();
      String importFormatHint = assimpFormatHint(resourceId);
      AIScene scene = Assimp.aiImportFileFromMemory(data, 8391242, importFormatHint);
      if (scene == null) {
         throw new IOException("Assimp import failed (" + importFormatHint + "): " + Assimp.aiGetErrorString());
      } else {
         ResourcePackMeshLoader.AnimatedMeshData ticksPerSecond;
         try {
            int animationCount = scene.mNumAnimations();
            AIAnimation animation = selectAnimationClip(scene, resourceId, modelPath);
            ResourcePackMeshLoader.FbxTextureAssignment textureAssignment = resolveFbxTextureAssignment(
               resourceManager, resourceId, scene, textureFolder, targetPath, modelPath, logName
            );
            ResourcePackMeshLoader.MeshBundle bundle = buildMeshBundle(scene, textureAssignment.materialTextures());
            if (bundle.positions.isEmpty() || bundle.rawTriangles.isEmpty()) {
               return ResourcePackMeshLoader.AnimatedMeshData.empty(defaultTexture(resourceId.method_12836(), textureFolder, targetPath));
            }

            FbxRigConfig rigConfig = FbxRigConfig.load(resourceManager, resourceId.method_12836(), targetPath);
            class_2960 texture = textureAssignment.modelTexture();
            ResourcePackMeshLoader.NormalizeTransform transform = computeNormalizeTransform(bundle.positions, normalizedSize, centerToCube);
            ResourcePackMeshLoader.ModelTransform transformData = loadModelTransform(resourceManager, resourceId);
            ResourcePackMeshLoader.SceneNode rootNode = scene.mRootNode() == null ? null : copyNode(scene.mRootNode());
            if (rootNode != null) {
               float ticksPerSecondx = animation != null && animation.mTicksPerSecond() != 0.0 ? (float)animation.mTicksPerSecond() : 25.0F;
               float durationTicks = animation == null ? 0.0F : Math.max(0.0F, (float)animation.mDuration());
               float durationSeconds = durationTicks <= 0.0F ? 0.0F : durationTicks / ticksPerSecondx;
               int frameCount = animation == null ? 1 : Math.max(1, Math.min(240, (int)Math.ceil(durationSeconds * 24.0F)));
               Map<String, ResourcePackMeshLoader.NodeAnimation> channelMap = animation == null ? Map.of() : buildRuntimeChannelMap(animation);
               String bonePoseCacheKey = buildBonePoseCacheKey(resourceId, bytes, frameCount, ticksPerSecondx, durationTicks);
               return ResourcePackMeshLoader.AnimatedMeshData.animated(
                  texture,
                  bundle,
                  transform,
                  centerToCube,
                  transformData,
                  rigConfig,
                  rootNode,
                  channelMap,
                  ticksPerSecondx,
                  durationTicks,
                  frameCount,
                  bonePoseCacheKey
               );
            }

            ticksPerSecond = ResourcePackMeshLoader.AnimatedMeshData.fromStatic(
               loadFbxModel(resourceManager, resourceId, resource, textureFolder, targetPath, modelPath, normalizedSize, centerToCube, logName)
            );
         } finally {
            safeReleaseImport(scene);
         }

         return ticksPerSecond;
      }
   }

   public static ResourcePackMeshLoader.AnimationClipData loadFbxAnimationClip(class_2960 resourceId, class_3298 resource, String logName) {
      try {
         return readFbxAnimationClip(resourceId, resource);
      } catch (NumberFormatException | IOException var4) {
         ObjFbxLoader.LOGGER.warn("Could not load {} FBX animation clip {}.", new Object[]{logName, resourceId, var4});
         return null;
      }
   }

   public static ResourcePackMeshLoader.AnimationClipData loadAssimpAnimationClip(class_2960 resourceId, class_3298 resource, String logName) {
      return loadFbxAnimationClip(resourceId, resource, logName);
   }

   private static ResourcePackMeshLoader.AnimationClipData readFbxAnimationClip(class_2960 resourceId, class_3298 resource) throws IOException {
      ensureAssimpAvailable();

      byte[] bytes;
      try (InputStream stream = resource.method_14482()) {
         bytes = stream.readAllBytes();
      }

      ByteBuffer data = BufferUtils.createByteBuffer(bytes.length);
      data.put(bytes).flip();
      String importFormatHint = assimpFormatHint(resourceId);
      AIScene scene = Assimp.aiImportFileFromMemory(data, 8391242, importFormatHint);
      if (scene == null) {
         throw new IOException("Assimp import failed (" + importFormatHint + "): " + Assimp.aiGetErrorString());
      } else {
         Object ticksPerSecond;
         try {
            AIAnimation animation = selectAnimationClip(scene, resourceId, null);
            if (animation != null) {
               float ticksPerSecondx = animation.mTicksPerSecond() == 0.0 ? 25.0F : (float)animation.mTicksPerSecond();
               float durationTicks = Math.max(0.0F, (float)animation.mDuration());
               float durationSeconds = durationTicks / ticksPerSecondx;
               int frameCount = Math.max(1, Math.min(240, (int)Math.ceil(durationSeconds * 24.0F)));
               Map<String, ResourcePackMeshLoader.NodeAnimation> channelMap = buildRuntimeChannelMap(animation);
               String cacheKey = buildBonePoseCacheKey(resourceId, bytes, frameCount, ticksPerSecondx, durationTicks);
               return new ResourcePackMeshLoader.AnimationClipData(Map.copyOf(channelMap), ticksPerSecondx, durationTicks, frameCount, cacheKey);
            }

            ticksPerSecond = null;
         } finally {
            safeReleaseImport(scene);
         }

         return (ResourcePackMeshLoader.AnimationClipData)ticksPerSecond;
      }
   }

   private static AIAnimation selectAnimationClip(AIScene scene, class_2960 resourceId, String modelPath) {
      if (scene != null && scene.mAnimations() != null && scene.mNumAnimations() > 0) {
         if (scene.mNumAnimations() == 1) {
            return AIAnimation.create(scene.mAnimations().get(0));
         } else {
            List<String> preferredTokens = buildPreferredAnimationTokens(resourceId, modelPath);

            for (String token : preferredTokens) {
               int match = findAnimationIndexByToken(scene, token);
               if (match >= 0) {
                  return AIAnimation.create(scene.mAnimations().get(match));
               }
            }

            boolean prefersIdle = preferredTokens.stream().anyMatch(tokenx -> "idle".equals(tokenx) || "stand".equals(tokenx) || "neutral".equals(tokenx));
            if (prefersIdle) {
               int idleFallbackIndex = -1;
               double shortestDuration = Double.MAX_VALUE;

               for (int i = 0; i < scene.mNumAnimations(); i++) {
                  AIAnimation animation = AIAnimation.create(scene.mAnimations().get(i));
                  if (animation != null) {
                     int channels = Math.max(0, animation.mNumChannels());
                     double duration = Math.max(0.0, animation.mDuration());
                     if (channels > 0 && duration > 0.0 && duration < shortestDuration) {
                        shortestDuration = duration;
                        idleFallbackIndex = i;
                     }
                  }
               }

               if (idleFallbackIndex >= 0) {
                  return AIAnimation.create(scene.mAnimations().get(idleFallbackIndex));
               }
            }

            int fallbackIndex = 0;
            int bestChannels = -1;
            double bestDuration = -1.0;

            for (int ix = 0; ix < scene.mNumAnimations(); ix++) {
               AIAnimation animation = AIAnimation.create(scene.mAnimations().get(ix));
               if (animation != null) {
                  int channels = Math.max(0, animation.mNumChannels());
                  double duration = Math.max(0.0, animation.mDuration());
                  if (channels > bestChannels || channels == bestChannels && duration > bestDuration) {
                     bestChannels = channels;
                     bestDuration = duration;
                     fallbackIndex = ix;
                  }
               }
            }

            return AIAnimation.create(scene.mAnimations().get(fallbackIndex));
         }
      } else {
         return null;
      }
   }

   private static List<String> buildPreferredAnimationTokens(class_2960 resourceId, String modelPath) {
      LinkedHashSet<String> tokens = new LinkedHashSet<>();
      if (modelPath != null && !modelPath.isBlank()) {
         collectAnimationTokens(modelPath, tokens);
      }

      if (resourceId != null && resourceId.method_12832() != null) {
         String path = resourceId.method_12832();
         collectAnimationTokens(path, tokens);
         int slash = path.lastIndexOf(47);
         if (slash >= 0 && slash + 1 < path.length()) {
            collectAnimationTokens(path.substring(slash + 1), tokens);
         }
      }

      return List.copyOf(tokens);
   }

   private static void collectAnimationTokens(String value, Set<String> out) {
      if (value != null && !value.isBlank() && out != null) {
         String normalized = value.replace('\\', '/').toLowerCase(Locale.ROOT);
         int dot = normalized.lastIndexOf(46);
         if (dot > 0) {
            normalized = normalized.substring(0, dot);
         }

         for (String part : normalized.split("[/_\\-\\.]+")) {
            String token = part.trim();
            if (token.length() >= 2) {
               out.add(token);
            }
         }
      }
   }

   private static int findAnimationIndexByToken(AIScene scene, String token) {
      if (token != null && !token.isBlank() && scene != null && scene.mAnimations() != null) {
         String needle = token.toLowerCase(Locale.ROOT);

         for (int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation animation = AIAnimation.create(scene.mAnimations().get(i));
            String name = animation.mName() == null ? "" : animation.mName().dataString();
            if (name != null && !name.isBlank()) {
               String normalized = name.toLowerCase(Locale.ROOT);
               if (normalized.equals(needle) || normalized.endsWith("/" + needle) || normalized.endsWith("_" + needle) || normalized.contains(needle)) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static void ensureAssimpAvailable() throws IOException {
      if (!ASSIMP_AVAILABLE) {
         throw new IOException("Assimp native backend is unavailable on this runtime.");
      }
   }

   public static boolean isAssimpModelPath(String path) {
      if (path == null) {
         return false;
      } else {
         String lowerPath = path.toLowerCase(Locale.ROOT);

         for (String extension : ASSIMP_MODEL_EXTENSIONS) {
            if (lowerPath.endsWith(extension)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isSupportedMeshModelPath(String path) {
      if (path == null) {
         return false;
      } else {
         String lowerPath = path.toLowerCase(Locale.ROOT);
         return lowerPath.endsWith(".obj") || isAssimpModelPath(lowerPath);
      }
   }

   public static int modelExtensionLength(String path) {
      if (path == null) {
         return 0;
      } else {
         String lowerPath = path.toLowerCase(Locale.ROOT);
         if (lowerPath.endsWith(".gltf")) {
            return 5;
         } else {
            return !lowerPath.endsWith(".obj") && !lowerPath.endsWith(".fbx") && !lowerPath.endsWith(".glb") && !lowerPath.endsWith(".dae") ? 0 : 4;
         }
      }
   }

   private static String assimpFormatHint(class_2960 resourceId) {
      String path = resourceId == null ? "" : resourceId.method_12832().toLowerCase(Locale.ROOT);
      if (path.endsWith(".glb")) {
         return "glb";
      } else if (path.endsWith(".gltf")) {
         return "gltf";
      } else {
         return path.endsWith(".dae") ? "dae" : "fbx";
      }
   }

   private static void safeReleaseImport(AIScene scene) {
      if (scene != null) {
         try {
            Assimp.aiReleaseImport(scene);
         } catch (Throwable var2) {
            if (!assimpReleaseFailureLogged) {
               assimpReleaseFailureLogged = true;
               ObjFbxLoader.LOGGER.warn("Assimp scene cleanup failed; continuing with loaded model data. This is usually a LWJGL/Assimp mismatch.", var2);
            }
         }
      }
   }

   private static String buildBonePoseCacheKey(class_2960 resourceId, byte[] bytes, int frameCount, float ticksPerSecond, float durationTicks) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         digest.update("bonepose-cache-v1".getBytes(StandardCharsets.UTF_8));
         digest.update(resourceId.toString().getBytes(StandardCharsets.UTF_8));
         digest.update(bytes);
         digest.update(ByteBuffer.allocate(4).putInt(frameCount).array());
         digest.update(ByteBuffer.allocate(4).putFloat(ticksPerSecond).array());
         digest.update(ByteBuffer.allocate(4).putFloat(durationTicks).array());
         byte[] hash = digest.digest();
         StringBuilder key = new StringBuilder(hash.length * 2);

         for (byte b : hash) {
            key.append(String.format("%02x", b));
         }

         return key.toString();
      } catch (NoSuchAlgorithmException var12) {
         return resourceId.toString().replace(':', '_').replace('/', '_');
      }
   }

   private static Path bonePoseCachePath(String cacheKey) {
      return BONE_CACHE_DIR.resolve(cacheKey + ".bin");
   }

   private static ResourcePackMeshLoader.FbxTextureAssignment resolveFbxTextureAssignment(
      class_3300 resourceManager, class_2960 resourceId, AIScene scene, String textureFolder, String targetPath, String modelPath, String logName
   ) {
      String modelRoot = modelRootFor(resourceId);
      class_2960 folderTexture = resolveExactFolderTexture(resourceManager, resourceId.method_12836(), textureFolder, targetPath);
      if (folderTexture != null) {
         return new ResourcePackMeshLoader.FbxTextureAssignment(folderTexture, Map.of());
      } else {
         ResourcePackMeshLoader.EmbeddedTextureLookup embeddedTextures = resolveEmbeddedTextureLookup(scene, resourceId);
         Map<Integer, class_2960> materialTextures = new HashMap<>();
         PointerBuffer materialPointers = scene.mMaterials();
         if (materialPointers != null) {
            AIString path = AIString.calloc();

            try {
               for (int materialIndex = 0; materialIndex < scene.mNumMaterials(); materialIndex++) {
                  AIMaterial material = AIMaterial.create(materialPointers.get(materialIndex));
                  class_2960 texture = resolveMaterialTexture(
                     resourceManager,
                     resourceId,
                     resourceId.method_12836(),
                     textureFolder,
                     targetPath,
                     modelRoot,
                     material,
                     materialIndex,
                     path,
                     embeddedTextures.byKey()
                  );
                  if (texture != null) {
                     materialTextures.put(materialIndex, texture);
                  }
               }
            } finally {
               path.free();
            }
         }

         class_2960 modelTexture;
         if (!materialTextures.isEmpty()) {
            modelTexture = materialTextures.getOrDefault(0, materialTextures.values().iterator().next());
         } else if (embeddedTextures.firstTexture() != null) {
            modelTexture = embeddedTextures.firstTexture();
         } else {
            modelTexture = findTexture(resourceManager, resourceId.method_12836(), textureFolder, targetPath, modelRoot, modelPath, logName);
         }

         return new ResourcePackMeshLoader.FbxTextureAssignment(modelTexture, Map.copyOf(materialTextures));
      }
   }

   private static class_2960 resolveMaterialTexture(
      class_3300 resourceManager,
      class_2960 resourceId,
      String namespace,
      String textureFolder,
      String targetPath,
      String modelRoot,
      AIMaterial material,
      int materialIndex,
      AIString pathOut,
      Map<String, class_2960> embeddedByKey
   ) {
      if (material != null && pathOut != null) {
         class_2960 fallbackNonColor = null;

         for (int textureType : FBX_COLOR_TEXTURE_TYPES) {
            int slotCount = Math.max(0, Assimp.aiGetMaterialTextureCount(material, textureType));

            for (int textureSlot = 0; textureSlot < slotCount; textureSlot++) {
               int result = Assimp.aiGetMaterialTexture(material, textureType, textureSlot, pathOut, (IntBuffer)null, null, null, null, null, null);
               if (result == 0) {
                  String materialPath = pathOut.dataString();
                  if (materialPath != null && !materialPath.isBlank()) {
                     class_2960 embedded = resolveEmbeddedTextureIdentifier(materialPath, embeddedByKey);
                     if (embedded != null && !isLikelyUtilityTexturePath(materialPath)) {
                        return embedded;
                     }

                     if (embedded != null && fallbackNonColor == null) {
                        fallbackNonColor = embedded;
                     }

                     class_2960 resolved = resolveExistingTextureIdentifier(resourceManager, namespace, textureFolder, targetPath, materialPath, modelRoot);
                     if (resolved != null && !isLikelyUtilityTexturePath(materialPath)) {
                        return resolved;
                     }

                     if (resolved != null && fallbackNonColor == null) {
                        fallbackNonColor = resolved;
                     }

                     class_2960 byBasename = resolvePackImageFallbackTexture(resourceManager, resourceId, namespace, modelRoot, materialPath, materialIndex);
                     if (byBasename != null) {
                        return byBasename;
                     }
                  }
               }
            }
         }

         if (fallbackNonColor != null) {
            return fallbackNonColor;
         } else {
            for (int textureType : FBX_MATERIAL_TEXTURE_TYPES) {
               int slotCount = Math.max(0, Assimp.aiGetMaterialTextureCount(material, textureType));

               for (int textureSlotx = 0; textureSlotx < slotCount; textureSlotx++) {
                  int result = Assimp.aiGetMaterialTexture(material, textureType, textureSlotx, pathOut, (IntBuffer)null, null, null, null, null, null);
                  if (result == 0) {
                     String materialPath = pathOut.dataString();
                     if (materialPath != null && !materialPath.isBlank() && !isLikelyUtilityTexturePath(materialPath)) {
                        class_2960 embeddedx = resolveEmbeddedTextureIdentifier(materialPath, embeddedByKey);
                        if (embeddedx != null) {
                           return embeddedx;
                        }

                        class_2960 resolvedx = resolveExistingTextureIdentifier(resourceManager, namespace, textureFolder, targetPath, materialPath, modelRoot);
                        if (resolvedx != null) {
                           return resolvedx;
                        }

                        class_2960 byBasename = resolvePackImageFallbackTexture(resourceManager, resourceId, namespace, modelRoot, materialPath, materialIndex);
                        if (byBasename != null) {
                           return byBasename;
                        }
                     }
                  }
               }
            }

            class_2960 embeddedByMaterialIndex = resolveEmbeddedTextureByMaterialIndex(materialIndex, embeddedByKey);
            return embeddedByMaterialIndex != null ? embeddedByMaterialIndex : null;
         }
      } else {
         return null;
      }
   }

   private static class_2960 resolveEmbeddedTextureByMaterialIndex(int materialIndex, Map<String, class_2960> embeddedByKey) {
      if (materialIndex >= 0 && embeddedByKey != null && !embeddedByKey.isEmpty()) {
         class_2960 direct = embeddedByKey.get("*" + materialIndex);
         return direct != null ? direct : embeddedByKey.get(Integer.toString(materialIndex));
      } else {
         return null;
      }
   }

   private static class_2960 resolvePackImageFallbackTexture(
      class_3300 resourceManager, class_2960 resourceId, String namespace, String modelRoot, String materialPath, int materialIndex
   ) {
      if (resourceManager != null && resourceId != null && materialPath != null && !materialPath.isBlank()) {
         String normalizedMaterialPath = materialPath.trim().replace('\\', '/');

         while (normalizedMaterialPath.startsWith("./")) {
            normalizedMaterialPath = normalizedMaterialPath.substring(2);
         }

         while (normalizedMaterialPath.startsWith("/")) {
            normalizedMaterialPath = normalizedMaterialPath.substring(1);
         }

         String basename = extractTextureBasename(materialPath);
         if (basename.isBlank()) {
            return null;
         } else {
            String lowerBasename = basename.toLowerCase(Locale.ROOT);
            Set<class_2960> candidates = new LinkedHashSet<>();
            class_2960 parsed = class_2960.method_12829(materialPath.trim());
            if (parsed != null) {
               candidates.add(parsed);
            }

            if (modelRoot != null && !modelRoot.isBlank()) {
               if (!normalizedMaterialPath.isBlank()) {
                  class_2960 relativeToModel = class_2960.method_43902(namespace, modelRoot + "/" + normalizedMaterialPath);
                  if (relativeToModel != null) {
                     candidates.add(relativeToModel);
                  }
               }

               class_2960 inModelRoot = class_2960.method_43902(namespace, modelRoot + "/" + basename);
               if (inModelRoot != null) {
                  candidates.add(inModelRoot);
               }

               String stem = basename;
               int dot = basename.lastIndexOf(46);
               if (dot > 0) {
                  stem = basename.substring(0, dot);
               }

               for (String ext : TEXTURE_EXTENSIONS) {
                  class_2960 alt = class_2960.method_43902(namespace, modelRoot + "/" + stem + ext);
                  if (alt != null) {
                     candidates.add(alt);
                  }
               }
            }

            for (String root : List.of("textures/entity/", "textures/obj/", "textures/models/", "textures/")) {
               class_2960 id = class_2960.method_43902(namespace, root + basename);
               if (id != null) {
                  candidates.add(id);
               }
            }

            for (class_2960 candidate : candidates) {
               class_3298 resource = (class_3298)resourceManager.method_14486(candidate).orElse(null);
               if (resource != null) {
                  byte[] bytes = readResourceBytes(resource);
                  class_2960 registered = registerRuntimeTextureFromBytes(resourceId, materialIndex, lowerBasename, bytes);
                  if (registered != null) {
                     return registered;
                  }
               }
            }

            Map<class_2960, class_3298> textureMatches = resourceManager.method_14488(
               "textures", id -> id != null && namespace.equals(id.method_12836()) && id.method_12832().toLowerCase(Locale.ROOT).endsWith("/" + lowerBasename)
            );

            for (Entry<class_2960, class_3298> entry : textureMatches.entrySet()) {
               byte[] bytes = readResourceBytes(entry.getValue());
               class_2960 registered = registerRuntimeTextureFromBytes(resourceId, materialIndex, lowerBasename, bytes);
               if (registered != null) {
                  return registered;
               }
            }

            Map<class_2960, class_3298> modelMatches = resourceManager.method_14488(
               "models", id -> id != null && namespace.equals(id.method_12836()) && id.method_12832().toLowerCase(Locale.ROOT).endsWith("/" + lowerBasename)
            );

            for (Entry<class_2960, class_3298> entryx : modelMatches.entrySet()) {
               byte[] bytes = readResourceBytes(entryx.getValue());
               class_2960 registered = registerRuntimeTextureFromBytes(resourceId, materialIndex, lowerBasename, bytes);
               if (registered != null) {
                  return registered;
               }
            }

            class_2960 fuzzy = resolveFuzzyTextureCandidate(resourceManager, resourceId, namespace, materialIndex, lowerBasename);
            return fuzzy != null ? fuzzy : null;
         }
      } else {
         return null;
      }
   }

   private static class_2960 resolveFuzzyTextureCandidate(
      class_3300 resourceManager, class_2960 resourceId, String namespace, int materialIndex, String lowerBasename
   ) {
      if (resourceManager != null && resourceId != null && namespace != null && lowerBasename != null && !lowerBasename.isBlank()) {
         Set<String> tokens = tokenizeTextureHint(lowerBasename);
         if (tokens.isEmpty()) {
            return null;
         } else {
            Map<class_2960, class_3298> candidates = resourceManager.method_14488("textures", id -> {
               if (id != null && namespace.equals(id.method_12836())) {
                  String pathx = id.method_12832().toLowerCase(Locale.ROOT);
                  if (!hasAnyTextureExtension(pathx)) {
                     return false;
                  } else if (pathx.startsWith("textures/entity/player/")) {
                     return false;
                  } else {
                     for (String tokenx : tokens) {
                        if (tokenx.length() >= 3 && pathx.contains(tokenx)) {
                           return true;
                        }
                     }

                     return false;
                  }
               } else {
                  return false;
               }
            });
            Entry<class_2960, class_3298> best = null;
            int bestScore = Integer.MIN_VALUE;

            for (Entry<class_2960, class_3298> entry : candidates.entrySet()) {
               String path = entry.getKey().method_12832().toLowerCase(Locale.ROOT);
               int score = 0;

               for (String token : tokens) {
                  if (path.contains(token)) {
                     score += Math.max(1, token.length());
                  }
               }

               byte[] bytes = readResourceBytes(entry.getValue());
               score += Math.min(1024, bytes.length / 64);
               if (score > bestScore) {
                  bestScore = score;
                  best = Map.entry(entry.getKey(), entry.getValue());
               }
            }

            if (best == null) {
               return null;
            } else {
               byte[] bytes = readResourceBytes(best.getValue());
               return registerRuntimeTextureFromBytes(resourceId, materialIndex, lowerBasename + "_fuzzy", bytes);
            }
         }
      } else {
         return null;
      }
   }

   private static class_2960 resolveBroadTextureFallback(
      class_3300 resourceManager, String namespace, String textureFolder, String targetPath, String modelPath
   ) {
      if (resourceManager != null && namespace != null && !namespace.isBlank()) {
         Set<String> hints = new LinkedHashSet<>();
         if (targetPath != null && !targetPath.isBlank()) {
            hints.addAll(tokenizeTextureHint(targetPath));
         }

         if (modelPath != null && !modelPath.isBlank()) {
            hints.addAll(tokenizeTextureHint(modelPath));
         }

         String normalizedFolder = textureFolder == null ? "" : textureFolder.toLowerCase(Locale.ROOT).replace('\\', '/');
         Map<class_2960, class_3298> candidates = resourceManager.method_14488(
            "textures",
            idx -> {
               if (idx != null && namespace.equals(idx.method_12836())) {
                  String pathx = idx.method_12832().toLowerCase(Locale.ROOT);
                  if (!hasAnyTextureExtension(pathx)) {
                     return false;
                  } else if (pathx.startsWith("textures/entity/player/")) {
                     return false;
                  } else {
                     return !normalizedFolder.isBlank() && pathx.startsWith("textures/" + normalizedFolder + "/")
                        ? true
                        : pathx.startsWith("textures/entity/") || pathx.startsWith("textures/obj/") || pathx.startsWith("textures/models/");
                  }
               } else {
                  return false;
               }
            }
         );
         Entry<class_2960, class_3298> best = null;
         int bestScore = Integer.MIN_VALUE;

         for (Entry<class_2960, class_3298> entry : candidates.entrySet()) {
            class_2960 id = entry.getKey();
            String path = id.method_12832().toLowerCase(Locale.ROOT);
            int score = 0;
            if (targetPath != null && !targetPath.isBlank()) {
               String lowerTarget = targetPath.toLowerCase(Locale.ROOT);
               if (path.contains("/" + lowerTarget + "/")) {
                  score += 140;
               }

               if (path.endsWith("/" + lowerTarget + ".png")
                  || path.endsWith("/" + lowerTarget + ".jpg")
                  || path.endsWith("/" + lowerTarget + ".jpeg")
                  || path.endsWith("/" + lowerTarget + ".webp")
                  || path.endsWith("/" + lowerTarget + ".tga")) {
                  score += 220;
               }
            }

            for (String hint : hints) {
               if (hint.length() >= 3 && path.contains(hint)) {
                  score += Math.min(60, hint.length() * 6);
               }
            }

            byte[] bytes = readResourceBytes(entry.getValue());
            if (bytes.length != 0) {
               score += Math.min(320, bytes.length / 2048);
               if (path.endsWith(".png")) {
                  score += 20;
               }

               if (score > bestScore) {
                  bestScore = score;
                  best = Map.entry(id, entry.getValue());
               }
            }
         }

         return best == null ? null : best.getKey();
      } else {
         return null;
      }
   }

   private static Set<String> tokenizeTextureHint(String value) {
      if (value != null && !value.isBlank()) {
         String stem = value;
         int dot = value.lastIndexOf(46);
         if (dot > 0) {
            stem = value.substring(0, dot);
         }

         String[] raw = stem.split("[^a-z0-9]+");
         Set<String> out = new LinkedHashSet<>();

         for (String token : raw) {
            if (token != null && !token.isBlank()) {
               out.add(token.toLowerCase(Locale.ROOT));
            }
         }

         return out;
      } else {
         return Set.of();
      }
   }

   private static class_2960 registerRuntimeTextureFromBytes(class_2960 resourceId, int materialIndex, String textureName, byte[] bytes) {
      if (resourceId != null && bytes != null && bytes.length != 0) {
         try {
            class_1011 image = class_1011.method_49277(bytes);
            if (image == null) {
               return null;
            } else {
               String hash = hashNativeImage(image);
               String safeName = sanitizePathForIdentifier(textureName == null ? "texture" : textureName);
               class_2960 textureId = class_2960.method_43902(
                  "objfbxloader",
                  "material/"
                     + sanitizePathForIdentifier(resourceId.method_12836() + "_" + resourceId.method_12832())
                     + "/"
                     + materialIndex
                     + "_"
                     + safeName
                     + "_"
                     + hash
                     + ".png"
               );
               if (textureId == null) {
                  image.close();
                  return null;
               } else {
                  byte[] cachedBytes = Arrays.copyOf(bytes, bytes.length);
                  class_310 client = class_310.method_1551();
                  if (client != null && client.method_1531() != null) {
                     RUNTIME_GENERATED_TEXTURE_IDS.add(textureId);
                     RUNTIME_GENERATED_TEXTURE_BYTES.put(textureId, cachedBytes);
                     Runnable register = () -> {
                        try {
                           client.method_1531().method_4616(textureId, new class_1043(() -> "objfbxloader-pack-image", image));
                        } catch (Throwable var5x) {
                           image.close();
                           ObjFbxLoader.LOGGER
                              .warn("Failed to register fallback material image texture {} for {}.", new Object[]{textureId, resourceId, var5x});
                        }
                     };
                     if (client.method_18854()) {
                        register.run();
                     } else {
                        CountDownLatch latch = new CountDownLatch(1);
                        client.execute(() -> {
                           try {
                              register.run();
                           } finally {
                              latch.countDown();
                           }
                        });

                        try {
                           latch.await(5L, TimeUnit.SECONDS);
                        } catch (InterruptedException var13) {
                           Thread.currentThread().interrupt();
                        }
                     }

                     return textureId;
                  } else {
                     image.close();
                     return null;
                  }
               }
            }
         } catch (Throwable var14) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String extractTextureBasename(String value) {
      if (value != null && !value.isBlank()) {
         String normalized = value.trim().replace('\\', '/');
         int query = normalized.indexOf(63);
         if (query >= 0) {
            normalized = normalized.substring(0, query);
         }

         int fragment = normalized.indexOf(35);
         if (fragment >= 0) {
            normalized = normalized.substring(0, fragment);
         }

         int slash = normalized.lastIndexOf(47);
         if (slash >= 0 && slash + 1 < normalized.length()) {
            normalized = normalized.substring(slash + 1);
         }

         return normalized.trim();
      } else {
         return "";
      }
   }

   private static byte[] readResourceBytes(class_3298 resource) {
      if (resource == null) {
         return new byte[0];
      } else {
         try {
            byte[] var2;
            try (InputStream input = resource.method_14482()) {
               var2 = input.readAllBytes();
            }

            return var2;
         } catch (IOException var6) {
            return new byte[0];
         }
      }
   }

   private static class_2960 registerMaterialColorTexture(class_2960 resourceId, int materialIndex, AIMaterial material) {
      if (resourceId != null && material != null) {
         AIColor4D color = AIColor4D.calloc();

         class_2960 var27;
         try {
            if (!readMaterialColor(material, "$clr.base", color)
               && !readMaterialColor(material, "$clr.diffuse", color)
               && !readMaterialColor(material, "$clr.emissive", color)) {
               return null;
            }

            int r = clampColor(color.r());
            int g = clampColor(color.g());
            int b = clampColor(color.b());
            int a = clampColor(color.a());
            if (a <= 0) {
               a = 255;
            }

            String hash = String.format("%02x%02x%02x%02x", r, g, b, a);
            class_2960 textureId = class_2960.method_43902(
               "objfbxloader",
               "material/" + sanitizePathForIdentifier(resourceId.method_12836() + "_" + resourceId.method_12832()) + "/" + materialIndex + "_" + hash + ".png"
            );
            if (textureId == null) {
               return null;
            }

            byte[] cachedBytes = solidColorTextureBytes(r, g, b, a);
            class_1011 image = new class_1011(2, 2, false);
            int argb = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;

            for (int y = 0; y < 2; y++) {
               for (int x = 0; x < 2; x++) {
                  image.method_61941(x, y, argb);
               }
            }

            class_310 client = class_310.method_1551();
            if (client == null || client.method_1531() == null) {
               image.close();
               return null;
            }

            RUNTIME_GENERATED_TEXTURE_IDS.add(textureId);
            if (cachedBytes.length > 0) {
               RUNTIME_GENERATED_TEXTURE_BYTES.put(textureId, cachedBytes);
            }

            Runnable register = () -> {
               try {
                  client.method_1531().method_4616(textureId, new class_1043(() -> "objfbxloader-material-color", image));
               } catch (Throwable var5x) {
                  image.close();
                  ObjFbxLoader.LOGGER.warn("Failed to register generated FBX material color texture {} for {}.", new Object[]{textureId, resourceId, var5x});
               }
            };
            if (client.method_18854()) {
               register.run();
            } else {
               CountDownLatch latch = new CountDownLatch(1);
               client.execute(() -> {
                  try {
                     register.run();
                  } finally {
                     latch.countDown();
                  }
               });

               try {
                  latch.await(5L, TimeUnit.SECONDS);
               } catch (InterruptedException var20) {
                  Thread.currentThread().interrupt();
               }
            }

            var27 = textureId;
         } finally {
            color.free();
         }

         return var27;
      } else {
         return null;
      }
   }

   private static boolean readMaterialColor(AIMaterial material, String colorKey, AIColor4D out) {
      if (material != null && colorKey != null && out != null && !colorKey.isBlank()) {
         try {
            int result = Assimp.aiGetMaterialColor(material, colorKey, 0, 0, out);
            return result == 0;
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static int clampColor(float value) {
      if (Float.isNaN(value)) {
         return 255;
      } else {
         float clamped = Math.max(0.0F, Math.min(1.0F, value));
         return Math.max(0, Math.min(255, Math.round(clamped * 255.0F)));
      }
   }

   private static byte[] solidColorTextureBytes(int r, int g, int b, int a) {
      Path temp = null;
      class_1011 image = null;

      byte[] y;
      try {
         image = new class_1011(2, 2, false);
         int argb = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;

         for (int yx = 0; yx < 2; yx++) {
            for (int x = 0; x < 2; x++) {
               image.method_61941(x, yx, argb);
            }
         }

         temp = Files.createTempFile("objfbxloader-material-color-", ".png");
         image.method_4314(temp);
         return Files.readAllBytes(temp);
      } catch (Throwable var17) {
         y = new byte[0];
      } finally {
         if (image != null) {
            image.close();
         }

         if (temp != null) {
            try {
               Files.deleteIfExists(temp);
            } catch (IOException var16) {
            }
         }
      }

      return y;
   }

   private static boolean isLikelyUtilityTexturePath(String texturePath) {
      if (texturePath != null && !texturePath.isBlank()) {
         String lower = texturePath.toLowerCase(Locale.ROOT);
         return lower.contains("normal")
            || lower.contains("_n.")
            || lower.contains("rough")
            || lower.contains("metal")
            || lower.contains("ao")
            || lower.contains("ambientocclusion")
            || lower.contains("height")
            || lower.contains("displacement")
            || lower.contains("spec")
            || lower.contains("opacity")
            || lower.contains("alpha")
            || lower.contains("mask");
      } else {
         return false;
      }
   }

   private static ResourcePackMeshLoader.EmbeddedTextureLookup resolveEmbeddedTextureLookup(AIScene scene, class_2960 resourceId) {
      PointerBuffer texturePointers = scene.mTextures();
      if (texturePointers != null && scene.mNumTextures() > 0) {
         Map<String, class_2960> byKey = new HashMap<>();
         class_2960 first = null;

         for (int index = 0; index < scene.mNumTextures(); index++) {
            AITexture texture = AITexture.create(texturePointers.get(index));
            class_2960 textureId = registerEmbeddedTexture(resourceId, index, texture);
            if (textureId != null) {
               if (first == null) {
                  first = textureId;
               }

               addEmbeddedTextureKey(byKey, "*" + index, textureId);
               addEmbeddedTextureKey(byKey, Integer.toString(index), textureId);
               String filename = texture.mFilename().dataString();
               if (filename != null && !filename.isBlank()) {
                  addEmbeddedTextureKey(byKey, filename, textureId);
                  String normalized = filename.replace('\\', '/');
                  int slash = normalized.lastIndexOf(47);
                  if (slash >= 0 && slash + 1 < normalized.length()) {
                     addEmbeddedTextureKey(byKey, normalized.substring(slash + 1), textureId);
                  }
               }
            }
         }

         return byKey.isEmpty()
            ? ResourcePackMeshLoader.EmbeddedTextureLookup.EMPTY
            : new ResourcePackMeshLoader.EmbeddedTextureLookup(Map.copyOf(byKey), first);
      } else {
         return ResourcePackMeshLoader.EmbeddedTextureLookup.EMPTY;
      }
   }

   private static void addEmbeddedTextureKey(Map<String, class_2960> byKey, String rawKey, class_2960 textureId) {
      if (rawKey != null && !rawKey.isBlank() && textureId != null) {
         String normalized = rawKey.replace('\\', '/').trim().toLowerCase(Locale.ROOT);
         if (!normalized.isBlank()) {
            byKey.putIfAbsent(normalized, textureId);
         }
      }
   }

   private static class_2960 resolveEmbeddedTextureIdentifier(String rawTextureName, Map<String, class_2960> embeddedByKey) {
      if (rawTextureName != null && !rawTextureName.isBlank() && embeddedByKey != null && !embeddedByKey.isEmpty()) {
         String normalized = rawTextureName.replace('\\', '/').trim().toLowerCase(Locale.ROOT);
         class_2960 direct = embeddedByKey.get(normalized);
         if (direct != null) {
            return direct;
         } else if (normalized.startsWith("*")) {
            class_2960 star = embeddedByKey.get(normalized);
            if (star != null) {
               return star;
            } else {
               String numeric = normalized.substring(1);
               return embeddedByKey.get(numeric);
            }
         } else {
            int slash = normalized.lastIndexOf(47);
            if (slash >= 0 && slash + 1 < normalized.length()) {
               class_2960 basename = embeddedByKey.get(normalized.substring(slash + 1));
               if (basename != null) {
                  return basename;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static class_2960 registerEmbeddedTexture(class_2960 resourceId, int index, AITexture aiTexture) {
      class_1011 image = decodeEmbeddedTexture(aiTexture);
      if (image == null) {
         return null;
      } else {
         String contentHash = hashNativeImage(image);
         class_2960 textureId = class_2960.method_43902(
            "objfbxloader",
            "embedded/" + sanitizePathForIdentifier(resourceId.method_12836() + "_" + resourceId.method_12832()) + "/" + index + "_" + contentHash + ".png"
         );
         if (textureId == null) {
            image.close();
            return null;
         } else {
            class_310 client = class_310.method_1551();
            if (client != null && client.method_1531() != null) {
               byte[] embeddedBytes = writeNativeImageToPngBytes(image);
               RUNTIME_GENERATED_TEXTURE_IDS.add(textureId);
               if (embeddedBytes.length > 0) {
                  RUNTIME_GENERATED_TEXTURE_BYTES.put(textureId, embeddedBytes);
               }

               Runnable register = () -> {
                  try {
                     client.method_1531().method_4616(textureId, new class_1043(() -> "objfbxloader-embedded", image));
                  } catch (Throwable var5x) {
                     image.close();
                     ObjFbxLoader.LOGGER.warn("Failed to register embedded FBX texture {} for {}.", new Object[]{textureId, resourceId, var5x});
                  }
               };
               if (client.method_18854()) {
                  register.run();
               } else {
                  CountDownLatch latch = new CountDownLatch(1);
                  client.execute(() -> {
                     try {
                        register.run();
                     } finally {
                        latch.countDown();
                     }
                  });

                  try {
                     latch.await(5L, TimeUnit.SECONDS);
                  } catch (InterruptedException var11) {
                     Thread.currentThread().interrupt();
                  }
               }

               return textureId;
            } else {
               image.close();
               return null;
            }
         }
      }
   }

   private static byte[] writeNativeImageToPngBytes(class_1011 image) {
      if (image == null) {
         return new byte[0];
      } else {
         Path temp = null;

         byte[] var3;
         try {
            temp = Files.createTempFile("objfbxloader-embedded-", ".png");
            image.method_4314(temp);
            return Files.readAllBytes(temp);
         } catch (Throwable var13) {
            var3 = new byte[0];
         } finally {
            if (temp != null) {
               try {
                  Files.deleteIfExists(temp);
               } catch (IOException var12) {
               }
            }
         }

         return var3;
      }
   }

   private static String hashNativeImage(class_1011 image) {
      if (image == null) {
         return "noimg";
      } else {
         try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            int width = image.method_4307();
            int height = image.method_4323();
            digest.update((byte)(width >>> 24));
            digest.update((byte)(width >>> 16));
            digest.update((byte)(width >>> 8));
            digest.update((byte)width);
            digest.update((byte)(height >>> 24));
            digest.update((byte)(height >>> 16));
            digest.update((byte)(height >>> 8));
            digest.update((byte)height);

            for (int y = 0; y < height; y++) {
               for (int x = 0; x < width; x++) {
                  int argb = image.method_61940(x, y);
                  digest.update((byte)(argb >>> 24));
                  digest.update((byte)(argb >>> 16));
                  digest.update((byte)(argb >>> 8));
                  digest.update((byte)argb);
               }
            }

            byte[] hash = digest.digest();
            StringBuilder out = new StringBuilder(16);

            for (int i = 0; i < 8 && i < hash.length; i++) {
               out.append(String.format("%02x", hash[i]));
            }

            return out.toString();
         } catch (NoSuchAlgorithmException var7) {
            return "nohash";
         }
      }
   }

   private static class_1011 decodeEmbeddedTexture(AITexture texture) {
      if (texture == null) {
         return null;
      } else {
         int width = texture.mWidth();
         int height = texture.mHeight();

         try {
            if (height == 0) {
               ByteBuffer compressed = texture.pcDataCompressed();
               if (compressed == null) {
                  return null;
               } else {
                  ByteBuffer copy = compressed.duplicate();
                  int readable = copy.remaining();
                  if (width > 0 && width < readable) {
                     copy.limit(copy.position() + width);
                  }

                  byte[] bytes = new byte[copy.remaining()];
                  copy.get(bytes);
                  return bytes.length == 0 ? null : class_1011.method_49277(bytes);
               }
            } else if (width > 0 && height > 0) {
               org.lwjgl.assimp.AITexel.Buffer pixels = texture.pcData();
               if (pixels != null && pixels.remaining() >= width * height) {
                  class_1011 image = new class_1011(width, height, false);

                  for (int y = 0; y < height; y++) {
                     for (int x = 0; x < width; x++) {
                        AITexel texel = (AITexel)pixels.get(y * width + x);
                        int argb = (texel.a() & 255) << 24 | (texel.r() & 255) << 16 | (texel.g() & 255) << 8 | texel.b() & 255;
                        image.method_61941(x, y, argb);
                     }
                  }

                  return image;
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } catch (Throwable var9) {
            ObjFbxLoader.LOGGER.warn("Failed to decode embedded FBX texture data.", var9);
            return null;
         }
      }
   }

   private static String sanitizePathForIdentifier(String raw) {
      if (raw != null && !raw.isBlank()) {
         String sanitized = raw.toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_").replaceAll("/{2,}", "/");

         while (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
         }

         return sanitized.isBlank() ? "unknown" : sanitized;
      } else {
         return "unknown";
      }
   }

   private static class_2960 resolveExactFolderTexture(class_3300 resourceManager, String namespace, String textureFolder, String targetPath) {
      class_2960 exact = class_2960.method_43902(namespace, "textures/" + textureFolder + "/" + targetPath + ".png");
      return exact != null && resourceManager.method_14486(exact).isPresent() ? exact : null;
   }

   private static String modelRootFor(class_2960 resourceId) {
      String path = resourceId.method_12832();
      return path.substring(0, path.lastIndexOf(47));
   }

   private static class_2960 resolveExistingTextureIdentifier(class_3300 resourceManager, String namespace, String textureFolder, String textureName) {
      return resolveExistingTextureIdentifier(resourceManager, namespace, textureFolder, null, textureName, null);
   }

   private static class_2960 resolveExistingTextureIdentifier(
      class_3300 resourceManager, String namespace, String textureFolder, String targetPath, String textureName
   ) {
      return resolveExistingTextureIdentifier(resourceManager, namespace, textureFolder, targetPath, textureName, null);
   }

   private static class_2960 resolveExistingTextureIdentifier(
      class_3300 resourceManager, String namespace, String textureFolder, String targetPath, String textureName, String modelRoot
   ) {
      for (class_2960 candidate : textureCandidates(namespace, textureFolder, targetPath, textureName, modelRoot)) {
         if (resourceManager.method_14486(candidate).isPresent()) {
            return candidate;
         }
      }

      class_2960 modelFolderFallback = resolveFirstTextureInModelFolder(resourceManager, namespace, textureFolder, targetPath);
      return modelFolderFallback != null ? modelFolderFallback : null;
   }

   private static List<class_2960> textureCandidates(String namespace, String textureFolder, String targetPath, String textureName, String modelRoot) {
      if (textureName != null && !textureName.isBlank()) {
         String normalized = textureName.replace('\\', '/').trim();
         if (normalized.startsWith("\"") && normalized.endsWith("\"") || normalized.startsWith("'") && normalized.endsWith("'")) {
            normalized = normalized.substring(1, normalized.length() - 1);
         }

         int query = normalized.indexOf(63);
         if (query >= 0) {
            normalized = normalized.substring(0, query);
         }

         int fragment = normalized.indexOf(35);
         if (fragment >= 0) {
            normalized = normalized.substring(0, fragment);
         }

         while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
         }

         while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
         }

         if (normalized.isBlank()) {
            return List.of();
         } else {
            String resolvedNamespace = namespace;
            String path = normalized;
            int colon = normalized.indexOf(58);
            if (colon > 0 && isLikelyNamespacedTextureRef(normalized, colon)) {
               resolvedNamespace = normalized.substring(0, colon);
               path = normalized.substring(colon + 1);
            }

            path = path.toLowerCase(Locale.ROOT);
            if (path.startsWith("assets/")) {
               String remainder = path.substring("assets/".length());
               int slash = remainder.indexOf(47);
               if (slash > 0) {
                  String nsCandidate = remainder.substring(0, slash);
                  String afterNamespace = remainder.substring(slash + 1);
                  if (afterNamespace.startsWith("textures/")) {
                     resolvedNamespace = nsCandidate;
                     path = afterNamespace.substring("textures/".length());
                  }
               }
            }

            List<class_2960> out = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            if (path.startsWith("textures/")) {
               addTextureCandidate(out, seen, resolvedNamespace, path);
            } else if (path.contains("/")) {
               addTextureCandidate(out, seen, resolvedNamespace, "textures/" + path);
            }

            String basename = path;
            int slash = path.lastIndexOf(47);
            if (slash >= 0) {
               basename = path.substring(slash + 1);
            }

            if (!basename.isBlank()) {
               if (textureFolder != null && !textureFolder.isBlank()) {
                  addTextureCandidate(out, seen, resolvedNamespace, "textures/" + textureFolder + "/" + basename);
                  if (targetPath != null && !targetPath.isBlank()) {
                     addTextureCandidate(out, seen, resolvedNamespace, "textures/" + textureFolder + "/" + targetPath + "/" + basename);
                  }
               }

               addTextureCandidate(out, seen, resolvedNamespace, "textures/entity/" + basename);
               addTextureCandidate(out, seen, resolvedNamespace, "textures/block/" + basename);
               addTextureCandidate(out, seen, resolvedNamespace, "textures/item/" + basename);
               addTextureCandidate(out, seen, resolvedNamespace, "textures/armor/" + basename);
            }

            if (modelRoot != null && !modelRoot.isBlank()) {
               addTextureCandidate(out, seen, resolvedNamespace, modelRoot + "/" + path);
               if (!basename.isBlank()) {
                  addTextureCandidate(out, seen, resolvedNamespace, modelRoot + "/" + basename);
               }
            }

            return out;
         }
      } else {
         return List.of();
      }
   }

   private static class_2960 resolveFirstTextureInModelFolder(class_3300 resourceManager, String namespace, String textureFolder, String targetPath) {
      if (resourceManager != null
         && namespace != null
         && !namespace.isBlank()
         && textureFolder != null
         && !textureFolder.isBlank()
         && targetPath != null
         && !targetPath.isBlank()) {
         String prefix = ("textures/" + textureFolder + "/" + targetPath + "/").toLowerCase(Locale.ROOT);
         Map<class_2960, class_3298> candidates = resourceManager.method_14488("textures", id -> {
            if (!namespace.equals(id.method_12836())) {
               return false;
            } else {
               String path = id.method_12832().toLowerCase(Locale.ROOT);
               return path.startsWith(prefix) && hasAnyTextureExtension(path);
            }
         });
         return candidates.isEmpty()
            ? null
            : candidates.keySet().stream().sorted((a, b) -> a.method_12832().compareToIgnoreCase(b.method_12832())).findFirst().orElse(null);
      } else {
         return null;
      }
   }

   private static boolean isLikelyNamespacedTextureRef(String value, int colonIndex) {
      if (value != null && colonIndex > 0 && colonIndex < value.length() - 1) {
         if (colonIndex == 1 && Character.isLetter(value.charAt(0))) {
            char next = value.charAt(2);
            if (next == '/' || next == '\\') {
               return false;
            }
         }

         String lower = value.toLowerCase(Locale.ROOT);
         if (!lower.startsWith("file:") && !lower.startsWith("http:") && !lower.startsWith("https:")) {
            int slash = value.indexOf(47);
            if (slash >= 0 && slash < colonIndex) {
               return false;
            } else {
               int backslash = value.indexOf(92);
               return backslash < 0 || backslash >= colonIndex;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void addTextureCandidate(List<class_2960> out, Set<String> seen, String namespace, String path) {
      if (namespace != null && !namespace.isBlank() && path != null && !path.isBlank()) {
         String normalizedPath = path.replace('\\', '/').toLowerCase(Locale.ROOT);
         if (hasAnyTextureExtension(normalizedPath)) {
            class_2960 id = class_2960.method_43902(namespace, normalizedPath);
            if (id != null) {
               String key = id.toString();
               if (seen.add(key)) {
                  out.add(id);
               }
            }
         } else {
            for (String extension : TEXTURE_EXTENSIONS) {
               class_2960 id = class_2960.method_43902(namespace, normalizedPath + extension);
               if (id != null) {
                  String key = id.toString();
                  if (seen.add(key)) {
                     out.add(id);
                  }
               }
            }
         }
      }
   }

   private static boolean hasAnyTextureExtension(String path) {
      if (path != null && !path.isBlank()) {
         for (String extension : TEXTURE_EXTENSIONS) {
            if (path.endsWith(extension)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static ResourcePackMeshLoader.ModelTransform loadModelTransform(class_3300 resourceManager, class_2960 resourceId) {
      String path = resourceId.method_12832();
      int dot = path.lastIndexOf(46);
      if (dot < 0) {
         return ResourcePackMeshLoader.ModelTransform.IDENTITY;
      } else {
         String jsonPath = path.substring(0, dot) + ".json";
         class_2960 jsonId = class_2960.method_43902(resourceId.method_12836(), jsonPath);
         if (jsonId == null) {
            return ResourcePackMeshLoader.ModelTransform.IDENTITY;
         } else {
            class_3298 resource = (class_3298)resourceManager.method_14486(jsonId).orElse(null);
            if (resource == null) {
               return ResourcePackMeshLoader.ModelTransform.IDENTITY;
            } else {
               try {
                  ResourcePackMeshLoader.ModelTransform var11;
                  try (BufferedReader reader = resource.method_43039()) {
                     StringBuilder builder = new StringBuilder();

                     String line;
                     while ((line = reader.readLine()) != null) {
                        builder.append(line);
                     }

                     String json = builder.toString();
                     var11 = new ResourcePackMeshLoader.ModelTransform(
                        readJsonFloat(json, "offsetX", 0.0F),
                        readJsonFloat(json, "offsetY", 0.0F),
                        readJsonFloat(json, "offsetZ", 0.0F),
                        readJsonFloat(json, "rotX", 0.0F),
                        readJsonFloat(json, "rotY", 0.0F),
                        readJsonFloat(json, "rotZ", 0.0F),
                        readJsonFloat(json, "scale", 1.0F)
                     );
                  }

                  return var11;
               } catch (Exception var14) {
                  ObjFbxLoader.LOGGER.warn("Could not read transform json for model {}.", resourceId, var14);
                  return ResourcePackMeshLoader.ModelTransform.IDENTITY;
               }
            }
         }
      }
   }

   private static float readJsonFloat(String json, String key, float fallback) {
      String search = "\"" + key + "\"";
      int keyIndex = json.indexOf(search);
      if (keyIndex < 0) {
         return fallback;
      } else {
         int colonIndex = json.indexOf(58, keyIndex);
         if (colonIndex < 0) {
            return fallback;
         } else {
            int start = colonIndex + 1;

            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
               start++;
            }

            int end;
            for (end = start; end < json.length(); end++) {
               char c = json.charAt(end);
               if ((c < '0' || c > '9') && c != '-' && c != '+' && c != '.' && c != 'e' && c != 'E') {
                  break;
               }
            }

            if (start == end) {
               return fallback;
            } else {
               try {
                  return Float.parseFloat(json.substring(start, end));
               } catch (NumberFormatException var9) {
                  return fallback;
               }
            }
         }
      }
   }

   private static Map<String, Matrix4f> captureBindPoseBones(AIScene scene, FbxRigConfig config) {
      Map<String, Matrix4f> nodeTransforms = new HashMap<>();
      Map<Integer, Matrix4f> meshTransforms = new HashMap<>();
      AINode root = scene.mRootNode();
      if (root != null) {
         buildNodeTransforms(root, new Matrix4f().identity(), 0.0F, Map.of(), nodeTransforms, meshTransforms, root.mName().dataString());
      }

      return captureConfiguredBones(config, nodeTransforms);
   }

   private static Map<String, Matrix4f> captureConfiguredBones(FbxRigConfig config, Map<String, Matrix4f> nodeTransforms) {
      return captureConfiguredBones(config, nodeTransforms, "", "");
   }

   private static Map<String, Matrix4f> captureConfiguredBones(
      FbxRigConfig config, Map<String, Matrix4f> nodeTransforms, String rightItemBoneOverride, String leftItemBoneOverride
   ) {
      Map<String, Matrix4f> result = new HashMap<>();
      putBone(result, "head", config.headBone(), nodeTransforms);
      putBone(result, "neck", config.neckBone(), nodeTransforms);
      putBone(result, "rightHand", config.rightHandBone(), nodeTransforms);
      putBone(result, "leftHand", config.leftHandBone(), nodeTransforms);
      String resolvedRightItemBone = firstNonBlank(firstNonBlank(rightItemBoneOverride, config.rightItemBone()), config.rightHandBone());
      String resolvedLeftItemBone = firstNonBlank(firstNonBlank(leftItemBoneOverride, config.leftItemBone()), config.leftHandBone());
      putItemBone(result, "rightItem", resolvedRightItemBone, nodeTransforms, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      putItemBone(result, "leftItem", resolvedLeftItemBone, nodeTransforms, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      return Map.copyOf(result);
   }

   private static void putBone(Map<String, Matrix4f> out, String key, String boneName, Map<String, Matrix4f> nodeTransforms) {
      for (String candidate : FbxRigConfig.splitBoneTargets(boneName)) {
         Matrix4f matrix = lookupNodeTransform(nodeTransforms, candidate);
         if (matrix != null) {
            out.put(key, new Matrix4f(matrix));
            return;
         }
      }
   }

   private static void putItemBone(
      Map<String, Matrix4f> out,
      String key,
      String boneName,
      Map<String, Matrix4f> nodeTransforms,
      float offsetX,
      float offsetY,
      float offsetZ,
      float rotX,
      float rotY,
      float rotZ,
      float scale
   ) {
      if (boneName != null && !boneName.isBlank()) {
         Matrix4f base = lookupNodeTransform(nodeTransforms, boneName);
         if (base != null) {
            Matrix4f matrix = new Matrix4f(base);
            float safeScale = Float.isFinite(scale) && scale != 0.0F ? scale : 1.0F;
            if (offsetX != 0.0F || offsetY != 0.0F || offsetZ != 0.0F || rotX != 0.0F || rotY != 0.0F || rotZ != 0.0F || safeScale != 1.0F) {
               matrix.translate(offsetX, offsetY, offsetZ);
               if (rotX != 0.0F || rotY != 0.0F || rotZ != 0.0F) {
                  matrix.rotateXYZ((float)Math.toRadians(rotX), (float)Math.toRadians(rotY), (float)Math.toRadians(rotZ));
               }

               if (safeScale != 1.0F) {
                  matrix.scale(safeScale);
               }
            }

            out.put(key, matrix);
         }
      }
   }

   private static Matrix4f lookupNodeTransform(Map<String, Matrix4f> nodeTransforms, String candidate) {
      if (nodeTransforms != null && !nodeTransforms.isEmpty() && candidate != null && !candidate.isBlank()) {
         Matrix4f direct = nodeTransforms.get(candidate);
         if (direct != null) {
            return direct;
         } else {
            for (Entry<String, Matrix4f> entry : nodeTransforms.entrySet()) {
               String key = entry.getKey();
               if (key != null && entry.getValue() != null && boneNamesEquivalent(candidate, key)) {
                  return entry.getValue();
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean boneNamesEquivalent(String a, String b) {
      if (a == null || b == null) {
         return false;
      } else if (a.equalsIgnoreCase(b)) {
         return true;
      } else {
         String ca = canonicalBoneName(a);
         String cb = canonicalBoneName(b);
         return !ca.isBlank() && !cb.isBlank() ? ca.equals(cb) || ca.endsWith(cb) || cb.endsWith(ca) : false;
      }
   }

   private static String canonicalBoneName(String value) {
      if (value == null) {
         return "";
      } else {
         String lower = value.trim().toLowerCase(Locale.ROOT);
         int slash = lower.lastIndexOf(47);
         if (slash >= 0 && slash + 1 < lower.length()) {
            lower = lower.substring(slash + 1);
         }

         int colon = lower.lastIndexOf(58);
         if (colon >= 0 && colon + 1 < lower.length()) {
            lower = lower.substring(colon + 1);
         }

         StringBuilder sb = new StringBuilder(lower.length());

         for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
               sb.append(ch);
            }
         }

         return sb.toString();
      }
   }

   private static String firstNonBlank(String primary, String fallback) {
      if (primary != null && !primary.isBlank()) {
         return primary;
      } else {
         return fallback == null ? "" : fallback;
      }
   }

   private static ResourcePackMeshLoader.MeshData normalizeModel(
      List<ResourcePackMeshLoader.RawTriangle> rawTriangles,
      List<Vector3f> positions,
      List<Vector2f> uvs,
      class_2960 texture,
      float normalizedSize,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform transformData,
      Map<String, Matrix4f> boneTransforms
   ) {
      if (!rawTriangles.isEmpty() && !positions.isEmpty()) {
         ResourcePackMeshLoader.NormalizeTransform transform = computeNormalizeTransform(positions, normalizedSize, centerToCube);
         Map<String, Matrix4f> normalizedBones = normalizeBoneTransforms(boneTransforms, transform, centerToCube, transformData);
         return buildMeshFromRaw(rawTriangles, positions, uvs, texture, transform, centerToCube, transformData, normalizedBones);
      } else {
         return ResourcePackMeshLoader.MeshData.empty(texture);
      }
   }

   private static ResourcePackMeshLoader.NormalizeTransform computeNormalizeTransform(List<Vector3f> positions, float normalizedSize, boolean centerToCube) {
      Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
      Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

      for (Vector3f position : positions) {
         min.min(position);
         max.max(position);
      }

      float centerX = (min.x() + max.x()) * 0.5F;
      float centerY = centerToCube ? (min.y() + max.y()) * 0.5F : min.y();
      float centerZ = (min.z() + max.z()) * 0.5F;
      float size = Math.max(Math.max(max.x() - min.x(), max.y() - min.y()), max.z() - min.z());
      float scale = size == 0.0F ? 1.0F : normalizedSize / size;
      return new ResourcePackMeshLoader.NormalizeTransform(centerX, centerY, centerZ, scale);
   }

   private static ResourcePackMeshLoader.MeshData buildMeshFromRaw(
      List<ResourcePackMeshLoader.RawTriangle> rawTriangles,
      List<Vector3f> positions,
      List<Vector2f> uvs,
      class_2960 texture,
      ResourcePackMeshLoader.NormalizeTransform transform,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform transformData,
      Map<String, Matrix4f> boneTransforms
   ) {
      if (!rawTriangles.isEmpty() && !positions.isEmpty()) {
         Vector3f[] transformedPositions = buildTransformedPositions(positions, transform, centerToCube, transformData);
         Vector3f[] smoothedNormals = computeSmoothedNormals(transformedPositions, rawTriangles);
         Vector2f[] uvLookup = buildUvLookup(uvs);
         Vector2f emptyUv = new Vector2f();
         List<ResourcePackMeshLoader.MeshTriangle> triangles = new ArrayList<>(rawTriangles.size());
         Vector3fc[] bounds = new Vector3fc[rawTriangles.size() * 3];
         int boundsIndex = 0;

         for (ResourcePackMeshLoader.RawTriangle raw : rawTriangles) {
            ResourcePackMeshLoader.MeshVertex a = new ResourcePackMeshLoader.MeshVertex(
               transformedPositions[raw.a.positionIndex], resolveUv(raw.a.uvIndex, uvLookup, emptyUv), smoothedNormals[raw.a.positionIndex]
            );
            ResourcePackMeshLoader.MeshVertex b = new ResourcePackMeshLoader.MeshVertex(
               transformedPositions[raw.b.positionIndex], resolveUv(raw.b.uvIndex, uvLookup, emptyUv), smoothedNormals[raw.b.positionIndex]
            );
            ResourcePackMeshLoader.MeshVertex c = new ResourcePackMeshLoader.MeshVertex(
               transformedPositions[raw.c.positionIndex], resolveUv(raw.c.uvIndex, uvLookup, emptyUv), smoothedNormals[raw.c.positionIndex]
            );
            Vector3f normal = new Vector3f(b.pos).sub(a.pos).cross(new Vector3f(c.pos).sub(a.pos));
            if (normal.lengthSquared() < 1.0E-6F) {
               normal.set(0.0F, 1.0F, 0.0F);
            } else {
               normal.normalize();
            }

            ResourcePackMeshLoader.MeshTriangle triangle = new ResourcePackMeshLoader.MeshTriangle(a, b, c, normal, raw.texture());
            triangles.add(triangle);
            bounds[boundsIndex++] = triangle.a.pos;
            bounds[boundsIndex++] = triangle.b.pos;
            bounds[boundsIndex++] = triangle.c.pos;
         }

         return new ResourcePackMeshLoader.MeshData(texture, List.copyOf(triangles), bounds, Map.copyOf(boneTransforms), new float[0], 0);
      } else {
         return ResourcePackMeshLoader.MeshData.empty(texture);
      }
   }

   private static ResourcePackMeshLoader.MeshData buildMeshFromRawPacked(
      List<ResourcePackMeshLoader.RawTriangle> rawTriangles,
      float[] positions,
      int vertexCount,
      List<Vector2f> uvs,
      class_2960 texture,
      ResourcePackMeshLoader.NormalizeTransform transform,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform transformData,
      boolean positionsAlreadyTransformed,
      Map<String, Matrix4f> boneTransforms
   ) {
      if (!rawTriangles.isEmpty() && vertexCount > 0 && positions.length >= vertexCount * 3) {
         Vector3f[] transformedPositions = positionsAlreadyTransformed
            ? copyPackedPositions(positions, vertexCount)
            : buildTransformedPositions(positions, vertexCount, transform, centerToCube, transformData);
         Vector3f[] smoothedNormals = computeSmoothedNormals(transformedPositions, rawTriangles);
         Vector2f[] uvLookup = buildUvLookup(uvs);
         Vector2f emptyUv = new Vector2f();
         List<ResourcePackMeshLoader.MeshTriangle> triangles = new ArrayList<>(rawTriangles.size());
         Vector3fc[] bounds = new Vector3fc[rawTriangles.size() * 3];
         int boundsIndex = 0;

         for (ResourcePackMeshLoader.RawTriangle raw : rawTriangles) {
            ResourcePackMeshLoader.MeshVertex a = new ResourcePackMeshLoader.MeshVertex(
               transformedPositions[raw.a.positionIndex], resolveUv(raw.a.uvIndex, uvLookup, emptyUv), smoothedNormals[raw.a.positionIndex]
            );
            ResourcePackMeshLoader.MeshVertex b = new ResourcePackMeshLoader.MeshVertex(
               transformedPositions[raw.b.positionIndex], resolveUv(raw.b.uvIndex, uvLookup, emptyUv), smoothedNormals[raw.b.positionIndex]
            );
            ResourcePackMeshLoader.MeshVertex c = new ResourcePackMeshLoader.MeshVertex(
               transformedPositions[raw.c.positionIndex], resolveUv(raw.c.uvIndex, uvLookup, emptyUv), smoothedNormals[raw.c.positionIndex]
            );
            Vector3f normal = new Vector3f(b.pos).sub(a.pos).cross(new Vector3f(c.pos).sub(a.pos));
            if (normal.lengthSquared() < 1.0E-6F) {
               normal.set(0.0F, 1.0F, 0.0F);
            } else {
               normal.normalize();
            }

            ResourcePackMeshLoader.MeshTriangle triangle = new ResourcePackMeshLoader.MeshTriangle(a, b, c, normal, raw.texture());
            triangles.add(triangle);
            bounds[boundsIndex++] = triangle.a.pos;
            bounds[boundsIndex++] = triangle.b.pos;
            bounds[boundsIndex++] = triangle.c.pos;
         }

         return new ResourcePackMeshLoader.MeshData(texture, List.copyOf(triangles), bounds, Map.copyOf(boneTransforms), new float[0], 0);
      } else {
         return ResourcePackMeshLoader.MeshData.empty(texture);
      }
   }

   private static Vector3f[] computeSmoothedNormals(Vector3f[] positions, List<ResourcePackMeshLoader.RawTriangle> rawTriangles) {
      Vector3f[] accumulated = new Vector3f[positions.length];

      for (int i = 0; i < positions.length; i++) {
         accumulated[i] = new Vector3f();
      }

      for (ResourcePackMeshLoader.RawTriangle raw : rawTriangles) {
         int ia = raw.a.positionIndex;
         int ib = raw.b.positionIndex;
         int ic = raw.c.positionIndex;
         if (ia >= 0 && ib >= 0 && ic >= 0 && ia < positions.length && ib < positions.length && ic < positions.length) {
            Vector3f a = positions[ia];
            Vector3f b = positions[ib];
            Vector3f c = positions[ic];
            Vector3f faceNormal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));
            if (!(faceNormal.lengthSquared() < 1.0E-6F)) {
               accumulated[ia].add(faceNormal);
               accumulated[ib].add(faceNormal);
               accumulated[ic].add(faceNormal);
            }
         }
      }

      for (int i = 0; i < accumulated.length; i++) {
         Vector3f normal = accumulated[i];
         if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(0.0F, 1.0F, 0.0F);
         } else {
            normal.normalize();
         }
      }

      return accumulated;
   }

   private static float[] buildSmoothPackedRenderStream(
      float[] positions, int vertexCount, int[] trianglePositionIndices, int[] triangleUvIndices, float[] packedUvs, int triangleCount
   ) {
      if (positions != null && trianglePositionIndices != null && triangleUvIndices != null && packedUvs != null) {
         if (positions.length >= vertexCount * 3 && trianglePositionIndices.length >= triangleCount * 3 && triangleUvIndices.length >= triangleCount * 3) {
            float[] smoothedNormals = new float[vertexCount * 3];

            for (int triangle = 0; triangle < triangleCount; triangle++) {
               int triBase = triangle * 3;
               int ia = trianglePositionIndices[triBase];
               int ib = trianglePositionIndices[triBase + 1];
               int ic = trianglePositionIndices[triBase + 2];
               if (ia < 0 || ib < 0 || ic < 0 || ia >= vertexCount || ib >= vertexCount || ic >= vertexCount) {
                  return null;
               }

               int a = ia * 3;
               int b = ib * 3;
               int c = ic * 3;
               float ax = positions[a];
               float ay = positions[a + 1];
               float az = positions[a + 2];
               float bx = positions[b];
               float by = positions[b + 1];
               float bz = positions[b + 2];
               float cx = positions[c];
               float cy = positions[c + 1];
               float cz = positions[c + 2];
               float abx = bx - ax;
               float aby = by - ay;
               float abz = bz - az;
               float acx = cx - ax;
               float acy = cy - ay;
               float acz = cz - az;
               float nx = aby * acz - abz * acy;
               float ny = abz * acx - abx * acz;
               float nz = abx * acy - aby * acx;
               float lenSq = nx * nx + ny * ny + nz * nz;
               if (!(lenSq < 1.0E-6F)) {
                  smoothedNormals[a] += nx;
                  smoothedNormals[a + 1] = smoothedNormals[a + 1] + ny;
                  smoothedNormals[a + 2] = smoothedNormals[a + 2] + nz;
                  smoothedNormals[b] += nx;
                  smoothedNormals[b + 1] = smoothedNormals[b + 1] + ny;
                  smoothedNormals[b + 2] = smoothedNormals[b + 2] + nz;
                  smoothedNormals[c] += nx;
                  smoothedNormals[c + 1] = smoothedNormals[c + 1] + ny;
                  smoothedNormals[c + 2] = smoothedNormals[c + 2] + nz;
               }
            }

            for (int vertex = 0; vertex < vertexCount; vertex++) {
               int n = vertex * 3;
               float nx = smoothedNormals[n];
               float ny = smoothedNormals[n + 1];
               float nz = smoothedNormals[n + 2];
               float lenSq = nx * nx + ny * ny + nz * nz;
               if (lenSq < 1.0E-6F) {
                  smoothedNormals[n] = 0.0F;
                  smoothedNormals[n + 1] = 1.0F;
                  smoothedNormals[n + 2] = 0.0F;
               } else {
                  float invLen = 1.0F / (float)Math.sqrt(lenSq);
                  smoothedNormals[n] = nx * invLen;
                  smoothedNormals[n + 1] = ny * invLen;
                  smoothedNormals[n + 2] = nz * invLen;
               }
            }

            int uvCount = packedUvs.length / 2;
            float[] out = new float[triangleCount * 3 * 8];

            for (int triangle = 0; triangle < triangleCount; triangle++) {
               int triBasex = triangle * 3;

               for (int corner = 0; corner < 3; corner++) {
                  int vertexIndex = trianglePositionIndices[triBasex + corner];
                  int vertexOffset = vertexIndex * 3;
                  int uvIndex = triangleUvIndices[triBasex + corner];
                  float u = 0.0F;
                  float v = 0.0F;
                  if (uvIndex >= 0 && uvIndex < uvCount) {
                     int uvOffset = uvIndex * 2;
                     u = packedUvs[uvOffset];
                     v = packedUvs[uvOffset + 1];
                  }

                  int outOffset = (triangle * 3 + corner) * 8;
                  out[outOffset] = positions[vertexOffset];
                  out[outOffset + 1] = positions[vertexOffset + 1];
                  out[outOffset + 2] = positions[vertexOffset + 2];
                  out[outOffset + 3] = u;
                  out[outOffset + 4] = v;
                  out[outOffset + 5] = smoothedNormals[vertexOffset];
                  out[outOffset + 6] = smoothedNormals[vertexOffset + 1];
                  out[outOffset + 7] = smoothedNormals[vertexOffset + 2];
               }
            }

            return out;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Vector3f[] copyPackedPositions(float[] packedPositions, int vertexCount) {
      Vector3f[] out = new Vector3f[vertexCount];

      for (int i = 0; i < vertexCount; i++) {
         int base = i * 3;
         out[i] = new Vector3f(packedPositions[base], packedPositions[base + 1], packedPositions[base + 2]);
      }

      return out;
   }

   private static Vector3f[] buildTransformedPositions(
      List<Vector3f> positions, ResourcePackMeshLoader.NormalizeTransform transform, boolean centerToCube, ResourcePackMeshLoader.ModelTransform transformData
   ) {
      Vector3f[] out = new Vector3f[positions.size()];
      float radX = transformData.rotX() != 0.0F ? (float)Math.toRadians(transformData.rotX()) : 0.0F;
      float radY = transformData.rotY() != 0.0F ? (float)Math.toRadians(transformData.rotY()) : 0.0F;
      float radZ = transformData.rotZ() != 0.0F ? (float)Math.toRadians(transformData.rotZ()) : 0.0F;
      boolean rotateX = radX != 0.0F;
      boolean rotateY = radY != 0.0F;
      boolean rotateZ = radZ != 0.0F;

      for (int i = 0; i < positions.size(); i++) {
         Vector3f original = positions.get(i);
         Vector3f pos = new Vector3f(
            (original.x() - transform.centerX) * transform.scale,
            (original.y() - transform.centerY) * transform.scale,
            (original.z() - transform.centerZ) * transform.scale
         );
         if (centerToCube) {
            pos.add(0.5F, 0.5F, 0.5F);
         }

         pos.mul(transformData.scale());
         if (rotateX) {
            pos.rotateX(radX);
         }

         if (rotateY) {
            pos.rotateY(radY);
         }

         if (rotateZ) {
            pos.rotateZ(radZ);
         }

         pos.x = pos.x + transformData.offsetX();
         pos.y = pos.y + transformData.offsetY();
         pos.z = pos.z + transformData.offsetZ();
         out[i] = pos;
      }

      return out;
   }

   private static Vector3f[] buildTransformedPositions(
      float[] packedPositions,
      int vertexCount,
      ResourcePackMeshLoader.NormalizeTransform transform,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform transformData
   ) {
      Vector3f[] out = new Vector3f[vertexCount];
      float radX = transformData.rotX() != 0.0F ? (float)Math.toRadians(transformData.rotX()) : 0.0F;
      float radY = transformData.rotY() != 0.0F ? (float)Math.toRadians(transformData.rotY()) : 0.0F;
      float radZ = transformData.rotZ() != 0.0F ? (float)Math.toRadians(transformData.rotZ()) : 0.0F;
      boolean rotateX = radX != 0.0F;
      boolean rotateY = radY != 0.0F;
      boolean rotateZ = radZ != 0.0F;

      for (int i = 0; i < vertexCount; i++) {
         int base = i * 3;
         Vector3f pos = new Vector3f(
            (packedPositions[base] - transform.centerX) * transform.scale,
            (packedPositions[base + 1] - transform.centerY) * transform.scale,
            (packedPositions[base + 2] - transform.centerZ) * transform.scale
         );
         if (centerToCube) {
            pos.add(0.5F, 0.5F, 0.5F);
         }

         pos.mul(transformData.scale());
         if (rotateX) {
            pos.rotateX(radX);
         }

         if (rotateY) {
            pos.rotateY(radY);
         }

         if (rotateZ) {
            pos.rotateZ(radZ);
         }

         pos.x = pos.x + transformData.offsetX();
         pos.y = pos.y + transformData.offsetY();
         pos.z = pos.z + transformData.offsetZ();
         out[i] = pos;
      }

      return out;
   }

   private static Map<String, Matrix4f> normalizeBoneTransforms(
      Map<String, Matrix4f> boneTransforms,
      ResourcePackMeshLoader.NormalizeTransform transform,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform transformData
   ) {
      if (boneTransforms != null && !boneTransforms.isEmpty()) {
         Matrix4f normalizeMatrix = buildNormalizeMatrix(transform, centerToCube, transformData);
         Map<String, Matrix4f> normalized = new HashMap<>(boneTransforms.size());

         for (Entry<String, Matrix4f> entry : boneTransforms.entrySet()) {
            if (entry.getValue() != null) {
               normalized.put(entry.getKey(), new Matrix4f(normalizeMatrix).mul((Matrix4fc)entry.getValue()));
            }
         }

         return normalized.isEmpty() ? Map.of() : Map.copyOf(normalized);
      } else {
         return Map.of();
      }
   }

   private static Matrix4f buildNormalizeMatrix(
      ResourcePackMeshLoader.NormalizeTransform transform, boolean centerToCube, ResourcePackMeshLoader.ModelTransform transformData
   ) {
      Matrix4f matrix = new Matrix4f().identity();
      matrix.translate(transformData.offsetX(), transformData.offsetY(), transformData.offsetZ());
      if (transformData.rotZ() != 0.0F) {
         matrix.rotateZ((float)Math.toRadians(transformData.rotZ()));
      }

      if (transformData.rotY() != 0.0F) {
         matrix.rotateY((float)Math.toRadians(transformData.rotY()));
      }

      if (transformData.rotX() != 0.0F) {
         matrix.rotateX((float)Math.toRadians(transformData.rotX()));
      }

      matrix.scale(transformData.scale());
      if (centerToCube) {
         matrix.translate(0.5F, 0.5F, 0.5F);
      }

      matrix.scale(transform.scale());
      matrix.translate(-transform.centerX(), -transform.centerY(), -transform.centerZ());
      return matrix;
   }

   private static Vector2f[] buildUvLookup(List<Vector2f> uvs) {
      Vector2f[] out = new Vector2f[uvs.size()];

      for (int i = 0; i < uvs.size(); i++) {
         out[i] = new Vector2f((Vector2fc)uvs.get(i));
      }

      return out;
   }

   private static Vector2f resolveUv(int uvIndex, Vector2f[] uvLookup, Vector2f fallback) {
      return uvIndex >= 0 && uvIndex < uvLookup.length ? uvLookup[uvIndex] : fallback;
   }

   private static ResourcePackMeshLoader.FacePoint parseFacePoint(String token, int positionCount, int uvCount) {
      String[] parts = token.split("/");
      int positionIndex = parseObjIndex(parts[0], positionCount);
      int uvIndex = parts.length > 1 && !parts[1].isBlank() ? parseObjIndex(parts[1], uvCount) : -1;
      return new ResourcePackMeshLoader.FacePoint(positionIndex, uvIndex);
   }

   private static int parseObjIndex(String value, int size) {
      int index = Integer.parseInt(value);
      return index < 0 ? size + index : index - 1;
   }

   private static ResourcePackMeshLoader.RagdollSkeleton buildRagdollSkeleton(
      ResourcePackMeshLoader.MeshBundle bundle, ResourcePackMeshLoader.SceneNode rootNode
   ) {
      if (bundle != null && rootNode != null && bundle.bones != null && !bundle.bones.isEmpty()) {
         Map<String, Integer> indexByName = new HashMap<>();
         List<String> names = new ArrayList<>();

         for (ResourcePackMeshLoader.MeshBone bone : bundle.bones) {
            if (bone != null && bone.name != null && !bone.name.isBlank() && !indexByName.containsKey(bone.name)) {
               indexByName.put(bone.name, names.size());
               names.add(bone.name);
            }
         }

         if (names.size() < 2) {
            return null;
         } else {
            Map<String, String> parentByName = new HashMap<>();
            collectNodeParents(rootNode, null, parentByName);
            int boneCount = names.size();
            int[] parentIndices = new int[boneCount];
            Arrays.fill(parentIndices, -1);

            for (int i = 0; i < boneCount; i++) {
               String parent = parentByName.get(names.get(i));

               while (parent != null && !indexByName.containsKey(parent)) {
                  parent = parentByName.get(parent);
               }

               if (parent != null) {
                  parentIndices[i] = indexByName.get(parent);
               }
            }

            List<List<Integer>> childLists = new ArrayList<>(boneCount);

            for (int i = 0; i < boneCount; i++) {
               childLists.add(new ArrayList<>());
            }

            for (int i = 0; i < boneCount; i++) {
               int parent = parentIndices[i];
               if (parent >= 0) {
                  childLists.get(parent).add(i);
               }
            }

            int[] primaryChildren = new int[boneCount];
            Arrays.fill(primaryChildren, -1);

            for (int ix = 0; ix < boneCount; ix++) {
               List<Integer> children = childLists.get(ix);
               if (!children.isEmpty()) {
                  primaryChildren[ix] = children.get(0);
               }
            }

            Map<String, Matrix4f> bindNodeTransforms = new HashMap<>();
            buildStaticNodeTransforms(rootNode, new Matrix4f().identity(), bindNodeTransforms);
            Vector3f[] bindPositions = new Vector3f[boneCount];

            for (int ixx = 0; ixx < boneCount; ixx++) {
               Matrix4f bind = bindNodeTransforms.get(names.get(ixx));
               Vector3f pos = new Vector3f();
               if (bind != null) {
                  bind.getTranslation(pos);
               }

               bindPositions[ixx] = pos;
            }

            float[] restLengths = new float[boneCount];
            float[] jointRadii = new float[boneCount];

            for (int ixx = 0; ixx < boneCount; ixx++) {
               float rest = 0.06F;
               int parent = parentIndices[ixx];
               if (parent >= 0) {
                  rest = bindPositions[ixx].distance(bindPositions[parent]);
               }

               if (!Float.isFinite(rest) || rest < 0.015F) {
                  rest = 0.06F;
               }

               restLengths[ixx] = rest;
               float radius = rest * 0.3F;
               if (parent < 0 && primaryChildren[ixx] >= 0) {
                  radius = restLengths[primaryChildren[ixx]] * 0.35F;
               }

               jointRadii[ixx] = clampFloat(radius, 0.015F, 0.085F);
            }

            int rootIndex = 0;

            for (int ixx = 0; ixx < boneCount; ixx++) {
               if (parentIndices[ixx] < 0) {
                  rootIndex = ixx;
                  break;
               }
            }

            boolean resolvedCenterRoot = false;

            for (int ixxx = 0; ixxx < boneCount; ixxx++) {
               String lower = names.get(ixxx).toLowerCase();
               if (lower.contains("hips") || lower.contains("pelvis")) {
                  rootIndex = ixxx;
                  resolvedCenterRoot = true;
                  break;
               }
            }

            if (!resolvedCenterRoot) {
               for (int ixxxx = 0; ixxxx < boneCount; ixxxx++) {
                  String lower = names.get(ixxxx).toLowerCase();
                  if (lower.contains("spine")) {
                     rootIndex = ixxxx;
                     break;
                  }
               }
            }

            return new ResourcePackMeshLoader.RagdollSkeleton(names.toArray(new String[0]), parentIndices, primaryChildren, restLengths, jointRadii, rootIndex);
         }
      } else {
         return null;
      }
   }

   private static void collectNodeParents(ResourcePackMeshLoader.SceneNode node, String parentName, Map<String, String> parentByName) {
      parentByName.put(node.name, parentName);

      for (ResourcePackMeshLoader.SceneNode child : node.children) {
         collectNodeParents(child, node.name, parentByName);
      }
   }

   private static void buildStaticNodeTransforms(ResourcePackMeshLoader.SceneNode node, Matrix4f parent, Map<String, Matrix4f> nodeTransforms) {
      Matrix4f global = new Matrix4f(parent).mul(node.localTransform);
      nodeTransforms.put(node.name, global);

      for (ResourcePackMeshLoader.SceneNode child : node.children) {
         buildStaticNodeTransforms(child, global, nodeTransforms);
      }
   }

   private static Map<String, Matrix4f> applyRagdollPhysics(
      ResourcePackMeshLoader.RagdollSkeleton skeleton,
      Map<Integer, ResourcePackMeshLoader.RagdollState> ragdollStates,
      int entityId,
      float ageTicks,
      Map<String, Matrix4f> animatedNodeTransforms,
      float entityHeightScale,
      float externalVelocityX,
      float externalVelocityY,
      float externalVelocityZ,
      float externalForceX,
      float externalForceY,
      float externalForceZ
   ) {
      if (skeleton != null && !animatedNodeTransforms.isEmpty()) {
         ResourcePackMeshLoader.RagdollState state = ragdollStates.computeIfAbsent(
            entityId, ignored -> new ResourcePackMeshLoader.RagdollState(skeleton.boneNames.length, entityId)
         );
         Vector3f[] animatedPositions = new Vector3f[skeleton.boneNames.length];
         Quaternionf[] animatedRotations = new Quaternionf[skeleton.boneNames.length];
         Vector3f[] animatedScales = new Vector3f[skeleton.boneNames.length];

         for (int i = 0; i < skeleton.boneNames.length; i++) {
            Matrix4f transform = animatedNodeTransforms.get(skeleton.boneNames[i]);
            if (transform != null) {
               Vector3f pos = new Vector3f();
               Quaternionf rot = new Quaternionf();
               Vector3f scale = new Vector3f();
               transform.getTranslation(pos);
               transform.getUnnormalizedRotation(rot).normalize();
               transform.getScale(scale);
               if (!Float.isFinite(scale.x) || scale.x < 1.0E-4F) {
                  scale.x = 1.0F;
               }

               if (!Float.isFinite(scale.y) || scale.y < 1.0E-4F) {
                  scale.y = 1.0F;
               }

               if (!Float.isFinite(scale.z) || scale.z < 1.0E-4F) {
                  scale.z = 1.0F;
               }

               animatedPositions[i] = pos;
               animatedRotations[i] = rot;
               animatedScales[i] = scale;
            }
         }

         if (!state.initialized) {
            initializeRagdollState(skeleton, state, animatedPositions, ageTicks);
         }

         float dtTicks = ageTicks - state.lastAge;
         if (!Float.isFinite(dtTicks)) {
            dtTicks = 0.0F;
         }

         if (dtTicks < -1.0E-4F) {
            initializeRagdollState(skeleton, state, animatedPositions, ageTicks);
            dtTicks = 0.0F;
         }

         dtTicks = clampFloat(dtTicks, 0.0F, 2.0F);
         boolean shouldAdvanceSimulation = dtTicks > 1.0E-4F;
         float dt = shouldAdvanceSimulation ? dtTicks / 20.0F : 0.0F;
         float safeHeightScale = entityHeightScale <= 1.0E-4F ? 1.95F : entityHeightScale;
         Vector3f externalModelVelocity = new Vector3f(
            externalVelocityX / safeHeightScale, externalVelocityY / safeHeightScale, externalVelocityZ / safeHeightScale
         );
         Vector3f externalModelForce = new Vector3f(externalForceX / safeHeightScale, externalForceY / safeHeightScale, externalForceZ / safeHeightScale);
         if (shouldAdvanceSimulation) {
            state.lastAge = ageTicks;
            advanceRagdollState(skeleton, state, dt, externalModelVelocity, externalModelForce);
            int rootIndex = Math.max(0, Math.min(skeleton.rootIndex, skeleton.boneNames.length - 1));
            Vector3f rootPosition = state.positions[rootIndex];
            if (!state.hasLastRootPosition) {
               state.lastRootPosition.set(rootPosition);
               state.hasLastRootPosition = true;
            } else {
               Vector3f rootDeltaModel = new Vector3f(rootPosition).sub(state.lastRootPosition);
               state.lastRootPosition.set(rootPosition);
               state.pendingWorldDelta.add(rootDeltaModel.x * safeHeightScale, rootDeltaModel.y * safeHeightScale, rootDeltaModel.z * safeHeightScale);
            }
         }

         Map<String, Matrix4f> ragdollTransforms = new HashMap<>(animatedNodeTransforms);

         for (int ix = 0; ix < skeleton.boneNames.length; ix++) {
            Matrix4f animated = animatedNodeTransforms.get(skeleton.boneNames[ix]);
            if (animated != null && animatedScales[ix] != null && animatedRotations[ix] != null && state.positions[ix] != null) {
               Quaternionf finalRotation = new Quaternionf(animatedRotations[ix]);
               int child = skeleton.primaryChildren[ix];
               if (child >= 0
                  && child < skeleton.boneNames.length
                  && animatedPositions[ix] != null
                  && animatedPositions[child] != null
                  && state.positions[child] != null) {
                  Vector3f fromDir = new Vector3f(animatedPositions[child]).sub(animatedPositions[ix]);
                  Vector3f toDir = new Vector3f(state.positions[child]).sub(state.positions[ix]);
                  if (fromDir.lengthSquared() > 1.0E-6F && toDir.lengthSquared() > 1.0E-6F) {
                     fromDir.normalize();
                     toDir.normalize();
                     Quaternionf delta = new Quaternionf().rotateTo(fromDir, toDir);
                     finalRotation = delta.mul(finalRotation, new Quaternionf()).normalize();
                  }
               }

               Matrix4f ragdoll = new Matrix4f().identity().translate(state.positions[ix]).rotate(finalRotation).scale(animatedScales[ix]);
               ragdollTransforms.put(skeleton.boneNames[ix], ragdoll);
            }
         }

         return ragdollTransforms;
      } else {
         return animatedNodeTransforms;
      }
   }

   private static void initializeRagdollState(
      ResourcePackMeshLoader.RagdollSkeleton skeleton, ResourcePackMeshLoader.RagdollState state, Vector3f[] animatedPositions, float ageTicks
   ) {
      float minY = Float.POSITIVE_INFINITY;

      for (int i = 0; i < skeleton.boneNames.length; i++) {
         Vector3f animatedPos = animatedPositions[i];
         Vector3f seed = animatedPos == null ? new Vector3f() : new Vector3f(animatedPos);
         state.positions[i].set(seed);
         state.previousPositions[i].set(seed);
         minY = Math.min(minY, seed.y);
      }

      if (Float.isFinite(minY)) {
         float shoulderLine = minY + 0.28F;

         for (int i = 0; i < skeleton.boneNames.length; i++) {
            Vector3f pos = state.positions[i];
            if (pos.y > shoulderLine) {
               float heightFactor = clampFloat((pos.y - minY) * 1.25F, 0.0F, 1.0F);
               float impulse = 0.025F + heightFactor * 0.075F;
               state.previousPositions[i].x = state.previousPositions[i].x - state.collapseBiasX * impulse;
               state.previousPositions[i].z = state.previousPositions[i].z - state.collapseBiasZ * impulse;
            }
         }
      }

      state.floorY = Float.isFinite(minY) ? minY - 0.01F : 0.0F;
      state.lastAge = ageTicks;
      int rootIndex = Math.max(0, Math.min(skeleton.rootIndex, skeleton.boneNames.length - 1));
      state.lastRootPosition.set(state.positions[rootIndex]);
      state.hasLastRootPosition = true;
      state.pendingWorldDelta.zero();
      state.linearVelocity.zero();
      state.simTicks = 0;
      state.wakeTicks = 0;
      state.initialized = true;
   }

   private static void advanceRagdollState(
      ResourcePackMeshLoader.RagdollSkeleton skeleton,
      ResourcePackMeshLoader.RagdollState state,
      float dt,
      Vector3f externalModelVelocity,
      Vector3f externalModelForce
   ) {
      float gravity = -7.5F;
      float damping = 0.985F;
      float forceInfluence = 11.0F;
      state.linearVelocity.fma(forceInfluence * dt, externalModelForce);
      if (externalModelVelocity.lengthSquared() > 1.0E-6F) {
         float align = clampFloat(dt * 4.5F, 0.0F, 1.0F);
         state.linearVelocity.lerp(externalModelVelocity, align * 0.5F);
      }

      state.linearVelocity.mul(0.972F);
      float carryX = state.linearVelocity.x * 1.35F;
      float carryY = state.linearVelocity.y * 0.7F;
      float carryZ = state.linearVelocity.z * 1.35F;
      float lateralCollapseForce = 6.4F;
      float earlyTorqueForce = state.simTicks < 35 ? 9.5F : 3.5F;
      state.simTicks++;
      float interactX = externalModelVelocity.x + externalModelForce.x * 0.45F;
      float interactY = externalModelVelocity.y + externalModelForce.y * 0.45F;
      float interactZ = externalModelVelocity.z + externalModelForce.z * 0.45F;
      float interactLenSq = interactX * interactX + interactY * interactY + interactZ * interactZ;
      if (interactLenSq > 1.8E-4F) {
         float interactLen = (float)Math.sqrt(interactLenSq);
         float invLen = 1.0F / Math.max(1.0E-4F, interactLen);
         float dirX = interactX * invLen;
         float dirZ = interactZ * invLen;
         float torqueX = dirZ;
         float torqueZ = -dirX;
         int root = Math.max(0, Math.min(skeleton.rootIndex, skeleton.boneNames.length - 1));
         Vector3f rootPos = state.positions[root];
         float rootX = rootPos.x;
         float rootY = rootPos.y;
         float rootZ = rootPos.z;
         float impulseBase = clampFloat(interactLen * dt * 1.25F, 0.0025F, 0.05F);

         for (int i = 0; i < skeleton.boneNames.length; i++) {
            if (i != root) {
               Vector3f current = state.positions[i];
               Vector3f previous = state.previousPositions[i];
               float relX = current.x - rootX;
               float relY = current.y - rootY;
               float relZ = current.z - rootZ;
               float upper = clampFloat((relY + 0.45F) / 1.45F, 0.0F, 1.0F);
               float lateral = clampFloat((float)Math.sqrt(relX * relX + relZ * relZ), 0.0F, 1.3F);
               float impulse = impulseBase * (0.85F + upper * 0.95F) * (0.75F + lateral * 0.35F);
               float sweep = impulse * (0.35F + upper * 0.65F);
               previous.x -= dirX * impulse + torqueX * sweep;
               previous.y -= impulse * (0.1F + upper * 0.35F);
               previous.z -= dirZ * impulse + torqueZ * sweep;
            }
         }

         state.wakeTicks = Math.max(state.wakeTicks, 12);
      }

      float activeDamping = state.wakeTicks > 0 ? 0.965F : damping;
      float activeLateralCollapse = state.wakeTicks > 0 ? lateralCollapseForce * 1.4F : lateralCollapseForce;
      float activeEarlyTorque = state.wakeTicks > 0 ? earlyTorqueForce * 1.85F : earlyTorqueForce;
      if (state.wakeTicks > 0) {
         state.wakeTicks--;
      }

      if (!stepRagdollNative(skeleton, state, dt, carryX, carryY, carryZ, gravity, activeDamping, activeEarlyTorque, activeLateralCollapse)) {
         for (int ix = 0; ix < skeleton.boneNames.length; ix++) {
            Vector3f current = state.positions[ix];
            Vector3f previous = state.previousPositions[ix];
            float velocityX = (current.x - previous.x) * activeDamping;
            float velocityY = (current.y - previous.y) * activeDamping;
            float velocityZ = (current.z - previous.z) * activeDamping;
            previous.set(current);
            previous.x -= carryX * dt;
            previous.y -= carryY * dt;
            previous.z -= carryZ * dt;
            current.x += velocityX;
            current.y += velocityY + gravity * dt * dt;
            current.z += velocityZ;
            float heightFactor = clampFloat((current.y - state.floorY) * 1.35F, 0.0F, 1.0F);
            float upperBodyFactor = clampFloat((heightFactor - 0.15F) / 0.85F, 0.0F, 1.0F);
            float lowerBodyFactor = 1.0F - upperBodyFactor;
            if (upperBodyFactor > 0.0F) {
               float collapseSlide = activeLateralCollapse * upperBodyFactor * dt * dt;
               current.x = current.x + state.collapseBiasX * collapseSlide;
               current.z = current.z + state.collapseBiasZ * collapseSlide;
            }

            float torque = activeEarlyTorque * (upperBodyFactor - lowerBodyFactor * 0.4F) * dt * dt;
            if (Math.abs(torque) > 1.0E-6F) {
               current.x = current.x + state.collapseBiasX * torque;
               current.z = current.z + state.collapseBiasZ * torque;
            }
         }

         for (int iteration = 0; iteration < 8; iteration++) {
            solveLengthConstraints(skeleton, state);
            solveFoldConstraints(skeleton, state);
            solveSelfCollisions(skeleton, state);
            solveGroundConstraints(skeleton, state);
         }
      }
   }

   private static boolean stepRagdollNative(
      ResourcePackMeshLoader.RagdollSkeleton skeleton,
      ResourcePackMeshLoader.RagdollState state,
      float dt,
      float carryX,
      float carryY,
      float carryZ,
      float gravity,
      float damping,
      float earlyTorqueForce,
      float lateralCollapseForce
   ) {
      if (!NativeAnimationBackend.isAvailable()) {
         return false;
      } else {
         int boneCount = skeleton.boneNames.length;
         if (boneCount <= 0) {
            return false;
         } else {
            int packedLength = boneCount * 3;
            if (state.nativePositionsPacked == null || state.nativePositionsPacked.length != packedLength) {
               state.nativePositionsPacked = new float[packedLength];
            }

            if (state.nativePreviousPositionsPacked == null || state.nativePreviousPositionsPacked.length != packedLength) {
               state.nativePreviousPositionsPacked = new float[packedLength];
            }

            for (int i = 0; i < boneCount; i++) {
               int base = i * 3;
               Vector3f pos = state.positions[i];
               Vector3f prev = state.previousPositions[i];
               state.nativePositionsPacked[base] = pos.x;
               state.nativePositionsPacked[base + 1] = pos.y;
               state.nativePositionsPacked[base + 2] = pos.z;
               state.nativePreviousPositionsPacked[base] = prev.x;
               state.nativePreviousPositionsPacked[base + 1] = prev.y;
               state.nativePreviousPositionsPacked[base + 2] = prev.z;
            }

            boolean nativeAdvanced = NativeAnimationBackend.stepRagdoll(
               state.nativePositionsPacked,
               state.nativePreviousPositionsPacked,
               skeleton.parentIndices,
               skeleton.restLengths,
               skeleton.jointRadii,
               skeleton.rootIndex,
               state.collapseBiasX,
               state.collapseBiasZ,
               state.floorY,
               dt,
               carryX,
               carryY,
               carryZ,
               gravity,
               damping,
               earlyTorqueForce,
               lateralCollapseForce
            );
            if (!nativeAdvanced) {
               return false;
            } else {
               for (int i = 0; i < boneCount; i++) {
                  int base = i * 3;
                  state.positions[i].set(state.nativePositionsPacked[base], state.nativePositionsPacked[base + 1], state.nativePositionsPacked[base + 2]);
                  state.previousPositions[i]
                     .set(
                        state.nativePreviousPositionsPacked[base], state.nativePreviousPositionsPacked[base + 1], state.nativePreviousPositionsPacked[base + 2]
                     );
               }

               return true;
            }
         }
      }
   }

   private static void solveLengthConstraints(ResourcePackMeshLoader.RagdollSkeleton skeleton, ResourcePackMeshLoader.RagdollState state) {
      for (int i = 0; i < skeleton.boneNames.length; i++) {
         int parent = skeleton.parentIndices[i];
         if (parent >= 0) {
            Vector3f parentPos = state.positions[parent];
            Vector3f childPos = state.positions[i];
            Vector3f delta = new Vector3f(childPos).sub(parentPos);
            float distance = delta.length();
            if (!(distance < 1.0E-5F)) {
               float target = skeleton.restLengths[i];
               float error = (distance - target) / distance;
               float parentWeight = parent == skeleton.rootIndex ? 0.25F : 0.5F;
               float childWeight = 1.0F - parentWeight;
               parentPos.fma(error * parentWeight, delta);
               childPos.fma(-error * childWeight, delta);
            }
         }
      }
   }

   private static void solveFoldConstraints(ResourcePackMeshLoader.RagdollSkeleton skeleton, ResourcePackMeshLoader.RagdollState state) {
      for (int i = 0; i < skeleton.boneNames.length; i++) {
         int parent = skeleton.parentIndices[i];
         if (parent >= 0) {
            int grandParent = skeleton.parentIndices[parent];
            if (grandParent >= 0) {
               float minSeparation = (skeleton.restLengths[i] + skeleton.restLengths[parent]) * 0.55F;
               Vector3f grandParentPos = state.positions[grandParent];
               Vector3f childPos = state.positions[i];
               Vector3f delta = new Vector3f(childPos).sub(grandParentPos);
               float distance = delta.length();
               if (!(distance >= minSeparation) && !(distance < 1.0E-5F)) {
                  float correction = (minSeparation - distance) / distance;
                  childPos.fma(correction * 0.85F, delta);
                  state.positions[parent].fma(-correction * 0.25F, delta);
               }
            }
         }
      }
   }

   private static void solveSelfCollisions(ResourcePackMeshLoader.RagdollSkeleton skeleton, ResourcePackMeshLoader.RagdollState state) {
      for (int i = 0; i < skeleton.boneNames.length; i++) {
         for (int j = i + 1; j < skeleton.boneNames.length; j++) {
            if (!areDirectlyConnected(skeleton, i, j)) {
               Vector3f a = state.positions[i];
               Vector3f b = state.positions[j];
               float minDistance = skeleton.jointRadii[i] + skeleton.jointRadii[j];
               Vector3f delta = new Vector3f(b).sub(a);
               float distance = delta.length();
               if (!(distance >= minDistance)) {
                  if (distance < 1.0E-5F) {
                     delta.set(1.0F, 0.0F, 0.0F);
                     distance = 1.0F;
                  }

                  float correction = (minDistance - distance) / distance;
                  a.fma(-0.5F * correction, delta);
                  b.fma(0.5F * correction, delta);
               }
            }
         }
      }
   }

   private static boolean areDirectlyConnected(ResourcePackMeshLoader.RagdollSkeleton skeleton, int a, int b) {
      int parentA = skeleton.parentIndices[a];
      int parentB = skeleton.parentIndices[b];
      return parentA == b || parentB == a || parentA >= 0 && parentA == parentB;
   }

   private static void solveGroundConstraints(ResourcePackMeshLoader.RagdollSkeleton skeleton, ResourcePackMeshLoader.RagdollState state) {
      for (int i = 0; i < skeleton.boneNames.length; i++) {
         Vector3f pos = state.positions[i];
         float floor = state.floorY + skeleton.jointRadii[i];
         if (!(pos.y >= floor)) {
            float penetration = floor - pos.y;
            float oldY = pos.y;
            pos.y = floor;
            Vector3f previous = state.previousPositions[i];
            float vy = oldY - previous.y;
            if (vy < 0.0F) {
               previous.y = floor + vy * -0.1F;
            } else {
               previous.y = floor;
            }

            float wakeReduction = state.wakeTicks > 0 ? 0.2F : 0.0F;
            float groundFriction = clampFloat(0.25F + penetration * 0.6F - wakeReduction, 0.1F, 0.75F);
            previous.x = previous.x + (pos.x - previous.x) * groundFriction;
            previous.z = previous.z + (pos.z - previous.z) * groundFriction;
         }
      }
   }

   private static float clampFloat(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   private static ResourcePackMeshLoader.MeshBundle buildMeshBundle(AIScene scene, Map<Integer, class_2960> materialTextures) {
      List<Vector3f> positions = new ArrayList<>();
      List<Vector2f> uvs = new ArrayList<>();
      List<ResourcePackMeshLoader.RawTriangle> rawTriangles = new ArrayList<>();
      List<ResourcePackMeshLoader.MeshBone> bones = new ArrayList<>();
      List<List<ResourcePackMeshLoader.VertexWeight>> vertexWeights = new ArrayList<>();
      List<Integer> vertexMeshIndices = new ArrayList<>();
      Map<Integer, Matrix4f> meshBindTransforms = new HashMap<>();
      Matrix4f globalInverse = new Matrix4f().identity();
      PointerBuffer meshPointers = scene.mMeshes();
      if (meshPointers != null) {
         for (int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++) {
            AIMesh mesh = AIMesh.create(meshPointers.get(meshIndex));
            class_2960 meshTexture = materialTextures.get(mesh.mMaterialIndex());
            int vertexOffset = positions.size();
            Buffer meshVertices = mesh.mVertices();
            Buffer texCoords = mesh.mTextureCoords(0);

            for (int vertexIndex = 0; vertexIndex < mesh.mNumVertices(); vertexIndex++) {
               AIVector3D vertex = (AIVector3D)meshVertices.get(vertexIndex);
               positions.add(new Vector3f(vertex.x(), vertex.y(), vertex.z()));
               if (texCoords != null) {
                  AIVector3D uv = (AIVector3D)texCoords.get(vertexIndex);
                  uvs.add(new Vector2f(uv.x(), uv.y()));
               } else {
                  uvs.add(new Vector2f());
               }

               vertexWeights.add(new ArrayList<>());
               vertexMeshIndices.add(meshIndex);
            }

            org.lwjgl.assimp.AIFace.Buffer faces = mesh.mFaces();

            for (int faceIndex = 0; faceIndex < mesh.mNumFaces(); faceIndex++) {
               AIFace face = (AIFace)faces.get(faceIndex);
               if (face.mNumIndices() == 3) {
                  IntBuffer indices = face.mIndices();
                  rawTriangles.add(
                     new ResourcePackMeshLoader.RawTriangle(
                        new ResourcePackMeshLoader.FacePoint(vertexOffset + indices.get(0), vertexOffset + indices.get(0)),
                        new ResourcePackMeshLoader.FacePoint(vertexOffset + indices.get(1), vertexOffset + indices.get(1)),
                        new ResourcePackMeshLoader.FacePoint(vertexOffset + indices.get(2), vertexOffset + indices.get(2)),
                        meshTexture
                     )
                  );
               }
            }

            if (mesh.mNumBones() > 0) {
               for (int boneIndex = 0; boneIndex < mesh.mNumBones(); boneIndex++) {
                  AIBone aiBone = AIBone.create(mesh.mBones().get(boneIndex));
                  String name = aiBone.mName().dataString();
                  FbxBoneTuning.registerBone(name);
                  Matrix4f offset = toJoml(aiBone.mOffsetMatrix());
                  int globalBoneIndex = bones.size();
                  bones.add(new ResourcePackMeshLoader.MeshBone(name, offset, meshIndex));
                  org.lwjgl.assimp.AIVertexWeight.Buffer weights = aiBone.mWeights();

                  for (int weightIndex = 0; weightIndex < aiBone.mNumWeights(); weightIndex++) {
                     AIVertexWeight weight = (AIVertexWeight)weights.get(weightIndex);
                     int vertexId = vertexOffset + weight.mVertexId();
                     if (vertexId >= 0 && vertexId < vertexWeights.size()) {
                        vertexWeights.get(vertexId).add(new ResourcePackMeshLoader.VertexWeight(globalBoneIndex, weight.mWeight()));
                     }
                  }
               }
            }
         }
      }

      AINode root = scene.mRootNode();
      if (root != null) {
         Matrix4f rootTransform = toJoml(root.mTransformation());
         globalInverse = new Matrix4f(rootTransform).invert();
         buildMeshBindTransforms(root, new Matrix4f().identity(), meshBindTransforms);
      }

      normalizeVertexWeights(vertexWeights);
      int[] packedMeshIndices = vertexMeshIndices.stream().mapToInt(Integer::intValue).toArray();
      float[] packedBasePositions = packBasePositions(positions);
      ResourcePackMeshLoader.PackedBoneInfluences packedInfluences = packBoneInfluences(vertexWeights);
      int[] packedTrianglePositionIndices = packTrianglePositionIndices(rawTriangles);
      int[] packedTriangleUvIndices = packTriangleUvIndices(rawTriangles);
      float[] packedUvs = packUvs(uvs);
      return new ResourcePackMeshLoader.MeshBundle(
         positions,
         uvs,
         rawTriangles,
         packedTrianglePositionIndices,
         packedTriangleUvIndices,
         packedUvs,
         bones,
         vertexWeights,
         packedMeshIndices,
         scene.mNumMeshes(),
         packedBasePositions,
         packedInfluences.boneIndices(),
         packedInfluences.boneWeights(),
         meshBindTransforms,
         globalInverse
      );
   }

   private static void normalizeVertexWeights(List<List<ResourcePackMeshLoader.VertexWeight>> vertexWeights) {
      for (List<ResourcePackMeshLoader.VertexWeight> weights : vertexWeights) {
         if (!weights.isEmpty()) {
            if (weights.size() > 4) {
               weights.sort((a, b) -> Float.compare(b.weight, a.weight));
               weights.subList(4, weights.size()).clear();
            }

            float total = 0.0F;

            for (ResourcePackMeshLoader.VertexWeight weight : weights) {
               if (weight.weight > 0.0F && !Float.isNaN(weight.weight)) {
                  total += weight.weight;
               }
            }

            if (total <= 1.0E-5F) {
               weights.clear();
            } else {
               for (int i = 0; i < weights.size(); i++) {
                  ResourcePackMeshLoader.VertexWeight weightx = weights.get(i);
                  float normalized = weightx.weight > 0.0F && !Float.isNaN(weightx.weight) ? weightx.weight / total : 0.0F;
                  weights.set(i, new ResourcePackMeshLoader.VertexWeight(weightx.boneIndex, normalized));
               }
            }
         }
      }
   }

   private static float[] packBasePositions(List<Vector3f> positions) {
      float[] packed = new float[positions.size() * 3];

      for (int i = 0; i < positions.size(); i++) {
         Vector3f pos = positions.get(i);
         int base = i * 3;
         packed[base] = pos.x();
         packed[base + 1] = pos.y();
         packed[base + 2] = pos.z();
      }

      return packed;
   }

   private static int[] packTrianglePositionIndices(List<ResourcePackMeshLoader.RawTriangle> rawTriangles) {
      int[] packed = new int[rawTriangles.size() * 3];

      for (int i = 0; i < rawTriangles.size(); i++) {
         ResourcePackMeshLoader.RawTriangle raw = rawTriangles.get(i);
         int base = i * 3;
         packed[base] = raw.a().positionIndex();
         packed[base + 1] = raw.b().positionIndex();
         packed[base + 2] = raw.c().positionIndex();
      }

      return packed;
   }

   private static int[] packTriangleUvIndices(List<ResourcePackMeshLoader.RawTriangle> rawTriangles) {
      int[] packed = new int[rawTriangles.size() * 3];

      for (int i = 0; i < rawTriangles.size(); i++) {
         ResourcePackMeshLoader.RawTriangle raw = rawTriangles.get(i);
         int base = i * 3;
         packed[base] = raw.a().uvIndex();
         packed[base + 1] = raw.b().uvIndex();
         packed[base + 2] = raw.c().uvIndex();
      }

      return packed;
   }

   private static float[] packUvs(List<Vector2f> uvs) {
      float[] packed = new float[uvs.size() * 2];

      for (int i = 0; i < uvs.size(); i++) {
         Vector2f uv = uvs.get(i);
         int base = i * 2;
         packed[base] = uv.x();
         packed[base + 1] = uv.y();
      }

      return packed;
   }

   private static ResourcePackMeshLoader.PackedBoneInfluences packBoneInfluences(List<List<ResourcePackMeshLoader.VertexWeight>> vertexWeights) {
      int vertexCount = vertexWeights.size();
      int[] packedIndices = new int[vertexCount * 4];
      float[] packedWeights = new float[vertexCount * 4];
      Arrays.fill(packedIndices, -1);

      for (int i = 0; i < vertexCount; i++) {
         List<ResourcePackMeshLoader.VertexWeight> weights = vertexWeights.get(i);
         int base = i * 4;
         int limit = Math.min(4, weights.size());

         for (int slot = 0; slot < limit; slot++) {
            ResourcePackMeshLoader.VertexWeight weight = weights.get(slot);
            packedIndices[base + slot] = weight.boneIndex();
            packedWeights[base + slot] = weight.weight();
         }
      }

      return new ResourcePackMeshLoader.PackedBoneInfluences(packedIndices, packedWeights);
   }

   private static void buildMeshBindTransforms(AINode node, Matrix4f parent, Map<Integer, Matrix4f> meshTransforms) {
      Matrix4f current = new Matrix4f(parent).mul(toJoml(node.mTransformation()));
      IntBuffer meshes = node.mMeshes();
      if (meshes != null) {
         for (int i = 0; i < node.mNumMeshes(); i++) {
            meshTransforms.put(meshes.get(i), new Matrix4f(current));
         }
      }

      for (int i = 0; i < node.mNumChildren(); i++) {
         buildMeshBindTransforms(AINode.create(node.mChildren().get(i)), current, meshTransforms);
      }
   }

   private static Map<String, AINodeAnim> buildChannelMap(AIAnimation animation) {
      Map<String, AINodeAnim> channels = new HashMap<>();

      for (int i = 0; i < animation.mNumChannels(); i++) {
         AINodeAnim channel = AINodeAnim.create(animation.mChannels().get(i));
         channels.put(channel.mNodeName().dataString(), channel);
      }

      return channels;
   }

   private static ResourcePackMeshLoader.SceneNode copyNode(AINode node) {
      List<ResourcePackMeshLoader.SceneNode> children = new ArrayList<>(node.mNumChildren());

      for (int i = 0; i < node.mNumChildren(); i++) {
         children.add(copyNode(AINode.create(node.mChildren().get(i))));
      }

      return new ResourcePackMeshLoader.SceneNode(node.mName().dataString(), toJoml(node.mTransformation()), readMeshIndices(node), List.copyOf(children));
   }

   private static int[] readMeshIndices(AINode node) {
      IntBuffer meshes = node.mMeshes();
      if (meshes != null && node.mNumMeshes() > 0) {
         int[] out = new int[node.mNumMeshes()];

         for (int i = 0; i < node.mNumMeshes(); i++) {
            out[i] = meshes.get(i);
         }

         return out;
      } else {
         return new int[0];
      }
   }

   private static Map<String, ResourcePackMeshLoader.NodeAnimation> buildRuntimeChannelMap(AIAnimation animation) {
      Map<String, ResourcePackMeshLoader.NodeAnimation> channels = new HashMap<>();

      for (int i = 0; i < animation.mNumChannels(); i++) {
         AINodeAnim channel = AINodeAnim.create(animation.mChannels().get(i));
         channels.put(
            channel.mNodeName().dataString(),
            new ResourcePackMeshLoader.NodeAnimation(
               readVectorKeys(channel.mPositionKeys(), channel.mNumPositionKeys()),
               readVectorKeys(channel.mScalingKeys(), channel.mNumScalingKeys()),
               readRotationKeys(channel.mRotationKeys(), channel.mNumRotationKeys())
            )
         );
      }

      return channels;
   }

   private static List<ResourcePackMeshLoader.VecKey> readVectorKeys(org.lwjgl.assimp.AIVectorKey.Buffer keys, int count) {
      if (keys != null && count > 0) {
         List<ResourcePackMeshLoader.VecKey> out = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            AIVectorKey key = (AIVectorKey)keys.get(i);
            out.add(new ResourcePackMeshLoader.VecKey((float)key.mTime(), toVec3(key.mValue())));
         }

         return List.copyOf(out);
      } else {
         return List.of();
      }
   }

   private static List<ResourcePackMeshLoader.QuatKey> readRotationKeys(org.lwjgl.assimp.AIQuatKey.Buffer keys, int count) {
      if (keys != null && count > 0) {
         List<ResourcePackMeshLoader.QuatKey> out = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            AIQuatKey key = (AIQuatKey)keys.get(i);
            AIQuaternion value = key.mValue();
            out.add(new ResourcePackMeshLoader.QuatKey((float)key.mTime(), new Quaternionf(value.x(), value.y(), value.z(), value.w()).normalize()));
         }

         return List.copyOf(out);
      } else {
         return List.of();
      }
   }

   private static void buildRuntimeNodeTransforms(
      ResourcePackMeshLoader.SceneNode node,
      Matrix4f parent,
      float timeTicks,
      Map<String, ResourcePackMeshLoader.NodeAnimation> channels,
      Map<String, Vector3f> lookOffsets,
      Map<String, Matrix4f> nodeTransforms,
      Map<Integer, Matrix4f> meshTransforms
   ) {
      Matrix4f local = buildRuntimeLocalTransform(node, channels.get(node.name), timeTicks, lookOffsets.get(node.name));
      Matrix4f global = new Matrix4f(parent).mul(local);
      nodeTransforms.put(node.name, global);

      for (int meshIndex : node.meshIndices) {
         meshTransforms.put(meshIndex, new Matrix4f(global));
      }

      for (ResourcePackMeshLoader.SceneNode child : node.children) {
         buildRuntimeNodeTransforms(child, global, timeTicks, channels, lookOffsets, nodeTransforms, meshTransforms);
      }
   }

   private static Matrix4f buildRuntimeLocalTransform(
      ResourcePackMeshLoader.SceneNode node, ResourcePackMeshLoader.NodeAnimation channel, float timeTicks, Vector3f lookOffsetDegrees
   ) {
      Matrix4f base = new Matrix4f(node.localTransform);
      if (channel == null) {
         return base;
      } else {
         Vector3f baseTranslation = new Vector3f();
         Vector3f baseScale = new Vector3f();
         Quaternionf baseRotation = new Quaternionf();
         base.getTranslation(baseTranslation);
         base.getScale(baseScale);
         base.getUnnormalizedRotation(baseRotation).normalize();
         if (baseScale.x == 0.0F) {
            baseScale.x = 1.0F;
         }

         if (baseScale.y == 0.0F) {
            baseScale.y = 1.0F;
         }

         if (baseScale.z == 0.0F) {
            baseScale.z = 1.0F;
         }

         Vector3f translation = new Vector3f(baseTranslation);
         Vector3f scale = new Vector3f(baseScale);
         Quaternionf rotation = new Quaternionf(baseRotation);
         String nodeName = node.name == null ? "" : node.name.toLowerCase(Locale.ROOT);
         boolean allowTranslation = nodeName.equals("hips")
            || nodeName.equals("root")
            || nodeName.equals("armature")
            || nodeName.contains("pelvis")
            || nodeName.contains("hip");
         if (!channel.positionKeys.isEmpty() && allowTranslation) {
            translation.set(interpolateVector(channel.positionKeys, timeTicks));
         }

         if (!channel.rotationKeys.isEmpty()) {
            rotation.set(interpolateRotation(channel.rotationKeys, timeTicks)).normalize();
         }

         Vector3f tuneDegrees = new Vector3f(FbxBoneTuning.getRotationOffset(node.name));
         if (lookOffsetDegrees != null && lookOffsetDegrees.lengthSquared() > 1.0E-6F) {
            tuneDegrees.add(lookOffsetDegrees);
         }

         Matrix4f transform = new Matrix4f().identity();
         transform.translate(translation);
         transform.rotate(rotation);
         if (tuneDegrees.lengthSquared() > 1.0E-6F) {
            transform.rotateXYZ((float)Math.toRadians(tuneDegrees.x()), (float)Math.toRadians(tuneDegrees.y()), (float)Math.toRadians(tuneDegrees.z()));
         }

         transform.scale(scale);
         return transform;
      }
   }

   private static Vector3f interpolateVector(List<ResourcePackMeshLoader.VecKey> keys, float timeTicks) {
      if (keys.isEmpty()) {
         return new Vector3f();
      } else if (keys.size() == 1) {
         return new Vector3f(keys.get(0).value);
      } else {
         int index = findVecKeyIndex(keys, timeTicks);
         int next = Math.min(index + 1, keys.size() - 1);
         ResourcePackMeshLoader.VecKey start = keys.get(index);
         ResourcePackMeshLoader.VecKey end = keys.get(next);
         float delta = end.time - start.time;
         float factor = delta == 0.0F ? 0.0F : (timeTicks - start.time) / delta;
         factor = Math.max(0.0F, Math.min(1.0F, factor));
         return new Vector3f(start.value).lerp(end.value, factor);
      }
   }

   private static Quaternionf interpolateRotation(List<ResourcePackMeshLoader.QuatKey> keys, float timeTicks) {
      if (keys.isEmpty()) {
         return new Quaternionf();
      } else if (keys.size() == 1) {
         return new Quaternionf(keys.get(0).value).normalize();
      } else {
         int index = findQuatKeyIndex(keys, timeTicks);
         int next = Math.min(index + 1, keys.size() - 1);
         ResourcePackMeshLoader.QuatKey start = keys.get(index);
         ResourcePackMeshLoader.QuatKey end = keys.get(next);
         float delta = end.time - start.time;
         float factor = delta == 0.0F ? 0.0F : (timeTicks - start.time) / delta;
         factor = Math.max(0.0F, Math.min(1.0F, factor));
         return new Quaternionf(start.value).slerp(end.value, factor).normalize();
      }
   }

   private static int findVecKeyIndex(List<ResourcePackMeshLoader.VecKey> keys, float timeTicks) {
      for (int i = 0; i < keys.size() - 1; i++) {
         if (timeTicks < keys.get(i + 1).time) {
            return i;
         }
      }

      return keys.size() - 2;
   }

   private static int findQuatKeyIndex(List<ResourcePackMeshLoader.QuatKey> keys, float timeTicks) {
      for (int i = 0; i < keys.size() - 1; i++) {
         if (timeTicks < keys.get(i + 1).time) {
            return i;
         }
      }

      return keys.size() - 2;
   }

   private static void buildNodeTransforms(
      AINode node,
      Matrix4f parent,
      float timeTicks,
      Map<String, AINodeAnim> channels,
      Map<String, Matrix4f> nodeTransforms,
      Map<Integer, Matrix4f> meshTransforms,
      String rootName
   ) {
      String name = node.mName().dataString();
      Matrix4f local = buildLocalTransform(node, channels.get(name), timeTicks, rootName);
      Matrix4f global = new Matrix4f(parent).mul(local);
      nodeTransforms.put(name, global);
      IntBuffer meshes = node.mMeshes();
      if (meshes != null) {
         for (int i = 0; i < node.mNumMeshes(); i++) {
            meshTransforms.put(meshes.get(i), new Matrix4f(global));
         }
      }

      for (int i = 0; i < node.mNumChildren(); i++) {
         buildNodeTransforms(AINode.create(node.mChildren().get(i)), global, timeTicks, channels, nodeTransforms, meshTransforms, rootName);
      }
   }

   private static Matrix4f buildLocalTransform(AINode node, AINodeAnim channel, float timeTicks, String rootName) {
      Matrix4f base = toJoml(node.mTransformation());
      if (channel == null) {
         return base;
      } else {
         Vector3f baseTranslation = new Vector3f();
         Vector3f baseScale = new Vector3f();
         Quaternionf baseRotation = new Quaternionf();
         base.getTranslation(baseTranslation);
         base.getScale(baseScale);
         base.getUnnormalizedRotation(baseRotation).normalize();
         if (baseScale.x == 0.0F) {
            baseScale.x = 1.0F;
         }

         if (baseScale.y == 0.0F) {
            baseScale.y = 1.0F;
         }

         if (baseScale.z == 0.0F) {
            baseScale.z = 1.0F;
         }

         Vector3f translation = new Vector3f(baseTranslation);
         Vector3f scale = new Vector3f(baseScale);
         Quaternionf rotation = new Quaternionf(baseRotation);
         if (channel.mNumPositionKeys() > 0) {
            translation.set(interpolateVector(channel.mPositionKeys(), channel.mNumPositionKeys(), timeTicks));
         }

         if (channel.mNumRotationKeys() > 0) {
            rotation.set(interpolateRotation(channel, timeTicks)).normalize();
         }

         if (channel.mNumScalingKeys() > 0) {
            scale.set(interpolateVector(channel.mScalingKeys(), channel.mNumScalingKeys(), timeTicks));
         }

         if (scale.x < 1.0E-4F || Float.isNaN(scale.x)) {
            scale.x = 1.0F;
         }

         if (scale.y < 1.0E-4F || Float.isNaN(scale.y)) {
            scale.y = 1.0F;
         }

         if (scale.z < 1.0E-4F || Float.isNaN(scale.z)) {
            scale.z = 1.0F;
         }

         Vector3f tuneDegrees = FbxBoneTuning.getRotationOffset(node.mName().dataString());
         Matrix4f transform = new Matrix4f().identity();
         transform.translate(translation);
         transform.rotate(rotation);
         if (tuneDegrees.lengthSquared() > 1.0E-6F) {
            float radX = (float)Math.toRadians(tuneDegrees.x());
            float radY = (float)Math.toRadians(tuneDegrees.y());
            float radZ = (float)Math.toRadians(tuneDegrees.z());
            transform.rotateXYZ(radX, radY, radZ);
         }

         transform.scale(scale);
         return transform;
      }
   }

   private static Vector3f interpolateVector(org.lwjgl.assimp.AIVectorKey.Buffer keys, int count, float timeTicks) {
      if (count == 0) {
         return new Vector3f();
      } else if (count == 1) {
         return toVec3(((AIVectorKey)keys.get(0)).mValue());
      } else {
         int index = findKeyIndex(keys, count, timeTicks);
         int next = Math.min(index + 1, count - 1);
         AIVectorKey key = (AIVectorKey)keys.get(index);
         AIVectorKey keyNext = (AIVectorKey)keys.get(next);
         float delta = (float)(keyNext.mTime() - key.mTime());
         float factor = delta == 0.0F ? 0.0F : (timeTicks - (float)key.mTime()) / delta;
         factor = Math.max(0.0F, Math.min(1.0F, factor));
         Vector3f start = toVec3(key.mValue());
         Vector3f end = toVec3(keyNext.mValue());
         return start.lerp(end, factor, new Vector3f());
      }
   }

   private static Quaternionf interpolateRotation(AINodeAnim channel, float timeTicks) {
      int count = channel.mNumRotationKeys();
      org.lwjgl.assimp.AIQuatKey.Buffer keys = channel.mRotationKeys();
      if (count == 0) {
         return new Quaternionf();
      } else if (count == 1) {
         AIQuaternion single = ((AIQuatKey)keys.get(0)).mValue();
         return new Quaternionf(single.x(), single.y(), single.z(), single.w()).normalize();
      } else {
         int index = findKeyIndex(keys, count, timeTicks);
         int next = Math.min(index + 1, count - 1);
         AIQuatKey key = (AIQuatKey)keys.get(index);
         AIQuatKey keyNext = (AIQuatKey)keys.get(next);
         float delta = (float)(keyNext.mTime() - key.mTime());
         float factor = delta == 0.0F ? 0.0F : (timeTicks - (float)key.mTime()) / delta;
         factor = Math.max(0.0F, Math.min(1.0F, factor));
         AIQuaternion start = key.mValue();
         AIQuaternion end = keyNext.mValue();
         Quaternionf startQ = new Quaternionf(start.x(), start.y(), start.z(), start.w()).normalize();
         Quaternionf endQ = new Quaternionf(end.x(), end.y(), end.z(), end.w()).normalize();
         return startQ.slerp(endQ, factor, new Quaternionf()).normalize();
      }
   }

   private static int findKeyIndex(org.lwjgl.assimp.AIVectorKey.Buffer keys, int count, float timeTicks) {
      for (int i = 0; i < count - 1; i++) {
         if (timeTicks < ((AIVectorKey)keys.get(i + 1)).mTime()) {
            return i;
         }
      }

      return count - 2;
   }

   private static int findKeyIndex(org.lwjgl.assimp.AIQuatKey.Buffer keys, int count, float timeTicks) {
      for (int i = 0; i < count - 1; i++) {
         if (timeTicks < ((AIQuatKey)keys.get(i + 1)).mTime()) {
            return i;
         }
      }

      return count - 2;
   }

   private static ResourcePackMeshLoader.SkinnedPositions skinPositions(
      ResourcePackMeshLoader.MeshBundle bundle,
      Map<String, Matrix4f> nodeTransforms,
      Map<Integer, Matrix4f> meshTransforms,
      ResourcePackMeshLoader.NormalizeTransform normalizeTransform,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform modelTransform
   ) {
      Matrix4f[] finalBoneTransforms = buildFinalBoneTransforms(bundle, nodeTransforms);
      float[] nativeSkinned = tryNativeSkinning(bundle, finalBoneTransforms, meshTransforms, normalizeTransform, centerToCube, modelTransform);
      if (nativeSkinned != null) {
         return new ResourcePackMeshLoader.SkinnedPositions(List.of(), nativeSkinned, bundle.positions.size(), true);
      } else {
         List<Vector3f> skinned = new ArrayList<>(bundle.positions.size());
         Vector3f transformedScratch = new Vector3f();

         for (int vertexIndex = 0; vertexIndex < bundle.positions.size(); vertexIndex++) {
            List<ResourcePackMeshLoader.VertexWeight> weights = bundle.vertexWeights.get(vertexIndex);
            Vector3f base = bundle.positions.get(vertexIndex);
            Matrix4f meshTransform = resolveMeshTransform(bundle, meshTransforms, vertexIndex);
            if (weights.isEmpty()) {
               Vector3f transformed = transformPosition(base, meshTransform, new Vector3f());
               skinned.add(transformed);
            } else {
               Vector3f blended = new Vector3f();
               boolean applied = false;

               for (ResourcePackMeshLoader.VertexWeight weight : weights) {
                  if (!(weight.weight <= 0.0F)) {
                     Matrix4f finalTransform = finalBoneTransforms[weight.boneIndex];
                     if (finalTransform != null) {
                        transformPosition(base, finalTransform, transformedScratch);
                        blended.fma(weight.weight, transformedScratch);
                        applied = true;
                     }
                  }
               }

               if (!applied || blended.lengthSquared() < 1.0E-7F) {
                  blended.set(transformPosition(base, meshTransform, blended));
               } else if (meshTransform != null) {
                  blended.set(transformPosition(blended, meshTransform, blended));
               }

               skinned.add(blended);
            }
         }

         return new ResourcePackMeshLoader.SkinnedPositions(List.copyOf(skinned), null, skinned.size(), false);
      }
   }

   private static float[] tryNativeSkinning(
      ResourcePackMeshLoader.MeshBundle bundle,
      Matrix4f[] finalBoneTransforms,
      Map<Integer, Matrix4f> meshTransforms,
      ResourcePackMeshLoader.NormalizeTransform normalizeTransform,
      boolean centerToCube,
      ResourcePackMeshLoader.ModelTransform modelTransform
   ) {
      if (!NativeAnimationBackend.isAvailable()) {
         return null;
      } else if (requiresPostSkinMeshTransform(bundle, meshTransforms)) {
         return null;
      } else {
         int vertexCount = bundle.positions.size();
         if (vertexCount == 0) {
            return new float[0];
         } else {
            ResourcePackMeshLoader.NativeSkinScratch scratch = borrowNativeScratch(
               bundle, finalBoneTransforms.length, Math.max(1, bundle.meshCount()), vertexCount
            );
            Arrays.fill(scratch.packedBoneActive, 0);

            for (int i = 0; i < finalBoneTransforms.length; i++) {
               Matrix4f matrix = finalBoneTransforms[i];
               if (matrix != null) {
                  scratch.packedBoneActive[i] = 1;
                  writeMatrixArray(scratch.packedBoneMatrices, i * 16, matrix);
               }
            }

            int meshCount = Math.max(1, bundle.meshCount());

            for (int meshIndex = 0; meshIndex < meshCount; meshIndex++) {
               Matrix4f matrix = meshTransforms.get(meshIndex);
               if (matrix == null) {
                  matrix = bundle.meshBindTransforms.get(meshIndex);
               }

               if (matrix == null) {
                  matrix = IDENTITY_MATRIX;
               }

               writeMatrixArray(scratch.packedMeshMatrices, meshIndex * 16, matrix);
            }

            boolean nativeSuccess = NativeAnimationBackend.skinVertices(
               bundle.packedBasePositions(),
               bundle.packedBoneIndices(),
               bundle.packedBoneWeights(),
               bundle.vertexMeshIndices(),
               scratch.packedBoneMatrices,
               scratch.packedBoneActive,
               scratch.packedMeshMatrices,
               scratch.outputPositions,
               vertexCount,
               normalizeTransform.centerX(),
               normalizeTransform.centerY(),
               normalizeTransform.centerZ(),
               normalizeTransform.scale(),
               centerToCube,
               modelTransform.offsetX(),
               modelTransform.offsetY(),
               modelTransform.offsetZ(),
               modelTransform.rotX(),
               modelTransform.rotY(),
               modelTransform.rotZ(),
               modelTransform.scale()
            );
            return !nativeSuccess ? null : scratch.outputPositions;
         }
      }
   }

   private static boolean requiresPostSkinMeshTransform(ResourcePackMeshLoader.MeshBundle bundle, Map<Integer, Matrix4f> meshTransforms) {
      for (int vertexIndex = 0; vertexIndex < bundle.positions.size(); vertexIndex++) {
         List<ResourcePackMeshLoader.VertexWeight> weights = bundle.vertexWeights.get(vertexIndex);
         if (weights != null && !weights.isEmpty()) {
            Matrix4f meshTransform = resolveMeshTransform(bundle, meshTransforms, vertexIndex);
            if (!isIdentityTransform(meshTransform)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean isIdentityTransform(Matrix4f matrix) {
      if (matrix == null) {
         return true;
      } else {
         float eps = 1.0E-5F;
         return Math.abs(matrix.m00() - 1.0F) < 1.0E-5F
            && Math.abs(matrix.m11() - 1.0F) < 1.0E-5F
            && Math.abs(matrix.m22() - 1.0F) < 1.0E-5F
            && Math.abs(matrix.m33() - 1.0F) < 1.0E-5F
            && Math.abs(matrix.m01()) < 1.0E-5F
            && Math.abs(matrix.m02()) < 1.0E-5F
            && Math.abs(matrix.m03()) < 1.0E-5F
            && Math.abs(matrix.m10()) < 1.0E-5F
            && Math.abs(matrix.m12()) < 1.0E-5F
            && Math.abs(matrix.m13()) < 1.0E-5F
            && Math.abs(matrix.m20()) < 1.0E-5F
            && Math.abs(matrix.m21()) < 1.0E-5F
            && Math.abs(matrix.m23()) < 1.0E-5F
            && Math.abs(matrix.m30()) < 1.0E-5F
            && Math.abs(matrix.m31()) < 1.0E-5F
            && Math.abs(matrix.m32()) < 1.0E-5F;
      }
   }

   private static ResourcePackMeshLoader.NativeSkinScratch borrowNativeScratch(
      ResourcePackMeshLoader.MeshBundle bundle, int boneCount, int meshCount, int vertexCount
   ) {
      synchronized (NATIVE_SKIN_SCRATCH_BY_BUNDLE) {
         ResourcePackMeshLoader.NativeSkinScratch scratch = NATIVE_SKIN_SCRATCH_BY_BUNDLE.get(bundle);
         if (scratch == null) {
            scratch = new ResourcePackMeshLoader.NativeSkinScratch();
            NATIVE_SKIN_SCRATCH_BY_BUNDLE.put(bundle, scratch);
         }

         scratch.ensureCapacity(boneCount, meshCount, vertexCount);
         return scratch;
      }
   }

   private static Matrix4f[] buildFinalBoneTransforms(ResourcePackMeshLoader.MeshBundle bundle, Map<String, Matrix4f> nodeTransforms) {
      Matrix4f[] finalTransforms = new Matrix4f[bundle.bones.size()];

      for (int i = 0; i < bundle.bones.size(); i++) {
         ResourcePackMeshLoader.MeshBone bone = bundle.bones.get(i);
         Matrix4f nodeTransform = nodeTransforms.get(bone.name);
         if (nodeTransform != null) {
            finalTransforms[i] = new Matrix4f(bundle.globalInverse).mul(nodeTransform).mul(bone.offset);
         }
      }

      return finalTransforms;
   }

   private static Matrix4f resolveMeshTransform(ResourcePackMeshLoader.MeshBundle bundle, Map<Integer, Matrix4f> meshTransforms, int vertexIndex) {
      int meshIndex = bundle.vertexMeshIndices[vertexIndex];
      Matrix4f animated = meshTransforms.get(meshIndex);
      return animated != null ? animated : bundle.meshBindTransforms.getOrDefault(meshIndex, IDENTITY_MATRIX);
   }

   private static Vector3f transformPosition(Vector3f pos, Matrix4f transform, Vector3f out) {
      return transform.transformPosition(pos.x(), pos.y(), pos.z(), out);
   }

   private static void writeMatrixArray(float[] packed, int offset, Matrix4f matrix) {
      packed[offset] = matrix.m00();
      packed[offset + 1] = matrix.m01();
      packed[offset + 2] = matrix.m02();
      packed[offset + 3] = matrix.m03();
      packed[offset + 4] = matrix.m10();
      packed[offset + 5] = matrix.m11();
      packed[offset + 6] = matrix.m12();
      packed[offset + 7] = matrix.m13();
      packed[offset + 8] = matrix.m20();
      packed[offset + 9] = matrix.m21();
      packed[offset + 10] = matrix.m22();
      packed[offset + 11] = matrix.m23();
      packed[offset + 12] = matrix.m30();
      packed[offset + 13] = matrix.m31();
      packed[offset + 14] = matrix.m32();
      packed[offset + 15] = matrix.m33();
   }

   private static Matrix4f readPackedMatrix(float[] packed, int offset) {
      return new Matrix4f(
         packed[offset],
         packed[offset + 1],
         packed[offset + 2],
         packed[offset + 3],
         packed[offset + 4],
         packed[offset + 5],
         packed[offset + 6],
         packed[offset + 7],
         packed[offset + 8],
         packed[offset + 9],
         packed[offset + 10],
         packed[offset + 11],
         packed[offset + 12],
         packed[offset + 13],
         packed[offset + 14],
         packed[offset + 15]
      );
   }

   private static Matrix4f toJoml(AIMatrix4x4 aiMatrix) {
      Matrix4f matrix = new Matrix4f();
      matrix.m00(aiMatrix.a1());
      matrix.m10(aiMatrix.a2());
      matrix.m20(aiMatrix.a3());
      matrix.m30(aiMatrix.a4());
      matrix.m01(aiMatrix.b1());
      matrix.m11(aiMatrix.b2());
      matrix.m21(aiMatrix.b3());
      matrix.m31(aiMatrix.b4());
      matrix.m02(aiMatrix.c1());
      matrix.m12(aiMatrix.c2());
      matrix.m22(aiMatrix.c3());
      matrix.m32(aiMatrix.c4());
      matrix.m03(aiMatrix.d1());
      matrix.m13(aiMatrix.d2());
      matrix.m23(aiMatrix.d3());
      matrix.m33(aiMatrix.d4());
      return matrix;
   }

   private static Vector3f toVec3(AIVector3D vec) {
      return new Vector3f(vec.x(), vec.y(), vec.z());
   }

   static {
      boolean available;
      try {
         NativeLibraryLoader.loadAssimpNatives();
         Assimp.aiGetVersionMajor();
         available = true;
      } catch (Throwable var2) {
         available = false;
         ObjFbxLoader.LOGGER.warn("Assimp native backend unavailable on this runtime. Assimp-backed models (FBX/GLTF/GLB/DAE) will be skipped.", var2);
      }

      ASSIMP_AVAILABLE = available;
      FBX_MATERIAL_TEXTURE_TYPES = new int[]{12, 1, 4, 18};
      FBX_COLOR_TEXTURE_TYPES = new int[]{12, 1, 4};
      ASSIMP_MODEL_EXTENSIONS = Set.of(".fbx", ".gltf", ".glb", ".dae");
      TEXTURE_EXTENSIONS = new String[]{".png", ".jpg", ".jpeg", ".tga", ".webp", ".bmp", ".gif", ".dds"};
      IDENTITY_MATRIX = new Matrix4f().identity();
      EXTERNAL_BONE_ROTATION_OFFSETS = new ThreadLocal<>();
      BONE_CACHE_DIR = PlatformPaths.configDir().resolve("objfbxloader-cache").resolve("boneposes");
      STATS_UNCACHED_SAMPLES = new AtomicLong();
      STATS_DISK_CACHE_LOAD_HITS = new AtomicLong();
      STATS_DISK_CACHE_LOAD_MISSES = new AtomicLong();
      STATS_DISK_CACHE_SAVES = new AtomicLong();
      NATIVE_SKIN_SCRATCH_BY_BUNDLE = new WeakHashMap<>();
      RUNTIME_GENERATED_TEXTURE_IDS = Collections.newSetFromMap(new ConcurrentHashMap<>());
      RUNTIME_GENERATED_TEXTURE_BYTES = new ConcurrentHashMap<>();
   }

   public static final class AnimatedMeshData {
      private final ResourcePackMeshLoader.MeshData staticMesh;
      private final class_2960 texture;
      private final ResourcePackMeshLoader.MeshBundle bundle;
      private final ResourcePackMeshLoader.NormalizeTransform normalizeTransform;
      private final boolean centerToCube;
      private final ResourcePackMeshLoader.ModelTransform modelTransform;
      private final FbxRigConfig rigConfig;
      private final ResourcePackMeshLoader.SceneNode rootNode;
      private final Map<String, ResourcePackMeshLoader.NodeAnimation> channelMap;
      private final float ticksPerSecond;
      private final float durationTicks;
      private final int frameCount;
      private final String bonePoseCacheKey;
      private final ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] cachedBonePoses;
      private final float[][] precomputedFinalBoneMatricesByFrame;
      private final int[][] precomputedBoneActiveByFrame;
      private final float[][] precomputedMeshMatricesByFrame;
      private final boolean hasTriangleTextureOverrides;
      private final int headBoneIndex;
      private final int neckBoneIndex;
      private final int[] headBoneIndices;
      private final int[] neckBoneIndices;
      private final int torsoLookBoneIndex;
      private final int[] torsoLookSubtreeBoneIndices;
      private static final float LOOK_NEUTRAL_DEADZONE_DEGREES = 3.0F;
      private final ResourcePackMeshLoader.RagdollSkeleton ragdollSkeleton;
      private final Map<Integer, ResourcePackMeshLoader.RagdollState> ragdollStates;
      private final Map<String, String> parentNodeByName;

      private AnimatedMeshData(
         ResourcePackMeshLoader.MeshData staticMesh,
         class_2960 texture,
         ResourcePackMeshLoader.MeshBundle bundle,
         ResourcePackMeshLoader.NormalizeTransform normalizeTransform,
         boolean centerToCube,
         ResourcePackMeshLoader.ModelTransform modelTransform,
         FbxRigConfig rigConfig,
         ResourcePackMeshLoader.SceneNode rootNode,
         Map<String, ResourcePackMeshLoader.NodeAnimation> channelMap,
         float ticksPerSecond,
         float durationTicks,
         int frameCount,
         String bonePoseCacheKey
      ) {
         this.staticMesh = staticMesh;
         this.texture = texture;
         this.bundle = bundle;
         this.normalizeTransform = normalizeTransform;
         this.centerToCube = centerToCube;
         this.modelTransform = modelTransform;
         this.rigConfig = rigConfig;
         this.rootNode = rootNode;
         this.channelMap = channelMap;
         this.ticksPerSecond = ticksPerSecond;
         this.durationTicks = durationTicks;
         this.frameCount = Math.max(1, frameCount);
         this.bonePoseCacheKey = bonePoseCacheKey;
         this.cachedBonePoses = this.staticMesh != null ? new ResourcePackMeshLoader.AnimatedMeshData.CachedPose[0] : this.buildBonePoseCache();
         this.hasTriangleTextureOverrides = this.bundle != null && hasTriangleTextureOverrides(this.bundle.rawTriangles(), this.texture);
         this.headBoneIndex = resolveBoneIndex(this.bundle, this.rigConfig == null ? "" : this.rigConfig.headBone());
         this.neckBoneIndex = resolveBoneIndex(this.bundle, this.rigConfig == null ? "" : this.rigConfig.neckBone());
         this.headBoneIndices = resolveBoneIndices(this.bundle, this.rigConfig == null ? "" : this.rigConfig.headBone());
         this.neckBoneIndices = resolveBoneIndices(this.bundle, this.rigConfig == null ? "" : this.rigConfig.neckBone());
         this.parentNodeByName = buildParentNodeLookup(this.rootNode);
         this.torsoLookBoneIndex = resolveTorsoLookBoneIndex(this.bundle, this.rigConfig, this.neckBoneIndex, this.headBoneIndex, this.parentNodeByName);
         this.torsoLookSubtreeBoneIndices = buildBoneSubtreeIndices(this.bundle, this.parentNodeByName, this.torsoLookBoneIndex);
         ResourcePackMeshLoader.AnimatedMeshData.PrecomputedNativeFrames nativeFrames = buildPrecomputedNativeFrames(
            this.bundle, this.cachedBonePoses, this.rigConfig, this.frameCount
         );
         this.precomputedFinalBoneMatricesByFrame = nativeFrames.boneMatricesByFrame();
         this.precomputedBoneActiveByFrame = nativeFrames.boneActiveByFrame();
         this.precomputedMeshMatricesByFrame = nativeFrames.meshMatricesByFrame();
         this.ragdollSkeleton = ResourcePackMeshLoader.buildRagdollSkeleton(bundle, rootNode);
         this.ragdollStates = new HashMap<>();
      }

      private static ResourcePackMeshLoader.AnimatedMeshData empty(class_2960 texture) {
         return fromStatic(ResourcePackMeshLoader.MeshData.empty(texture));
      }

      private static ResourcePackMeshLoader.AnimatedMeshData fromStatic(ResourcePackMeshLoader.MeshData mesh) {
         return new ResourcePackMeshLoader.AnimatedMeshData(
            mesh, mesh.texture(), null, null, false, ResourcePackMeshLoader.ModelTransform.IDENTITY, FbxRigConfig.EMPTY, null, Map.of(), 0.0F, 0.0F, 1, null
         );
      }

      private static ResourcePackMeshLoader.AnimatedMeshData animated(
         class_2960 texture,
         ResourcePackMeshLoader.MeshBundle bundle,
         ResourcePackMeshLoader.NormalizeTransform normalizeTransform,
         boolean centerToCube,
         ResourcePackMeshLoader.ModelTransform modelTransform,
         FbxRigConfig rigConfig,
         ResourcePackMeshLoader.SceneNode rootNode,
         Map<String, ResourcePackMeshLoader.NodeAnimation> channelMap,
         float ticksPerSecond,
         float durationTicks,
         int frameCount,
         String bonePoseCacheKey
      ) {
         return new ResourcePackMeshLoader.AnimatedMeshData(
            null,
            texture,
            bundle,
            normalizeTransform,
            centerToCube,
            modelTransform,
            rigConfig,
            rootNode,
            Map.copyOf(channelMap),
            ticksPerSecond,
            durationTicks,
            frameCount,
            bonePoseCacheKey
         );
      }

      public ResourcePackMeshLoader.AnimatedMeshData withAnimationClip(ResourcePackMeshLoader.AnimationClipData clipData) {
         return clipData != null && this.staticMesh == null && this.bundle != null && this.rootNode != null
            ? new ResourcePackMeshLoader.AnimatedMeshData(
               null,
               this.texture,
               this.bundle,
               this.normalizeTransform,
               this.centerToCube,
               this.modelTransform,
               this.rigConfig,
               this.rootNode,
               Map.copyOf(clipData.channelMap()),
               clipData.ticksPerSecond(),
               clipData.durationTicks(),
               clipData.frameCount(),
               null
            )
            : this;
      }

      public boolean isEmpty() {
         return this.staticMesh != null
            ? this.staticMesh.triangles().isEmpty()
            : this.bundle == null || this.bundle.positions.isEmpty() || this.bundle.rawTriangles.isEmpty();
      }

      public int frameCount() {
         return this.frameCount;
      }

      public List<String> availableBoneNames() {
         LinkedHashSet<String> names = new LinkedHashSet<>();
         if (this.bundle != null && this.bundle.bones() != null) {
            for (ResourcePackMeshLoader.MeshBone bone : this.bundle.bones()) {
               if (bone != null && bone.name() != null && !bone.name().isBlank()) {
                  names.add(bone.name());
               }
            }
         }

         collectSceneNodeNames(this.rootNode, names);
         if (names.isEmpty()) {
            return List.of();
         } else {
            List<String> sorted = new ArrayList<>(names);
            sorted.sort(String::compareToIgnoreCase);
            return List.copyOf(sorted);
         }
      }

      public ResourcePackMeshLoader.MeshData sampleFrame(float framePosition) {
         return this.sampleFrameUncached(framePosition, 0.0F, 0.0F, true, "", "");
      }

      public ResourcePackMeshLoader.MeshData sampleFrame(float framePosition, float lookPitchDegrees, float lookYawDegrees) {
         return this.sampleFrameUncached(framePosition, lookPitchDegrees, lookYawDegrees, true, "", "");
      }

      public ResourcePackMeshLoader.MeshData sampleFrame(float framePosition, float lookPitchDegrees, float lookYawDegrees, boolean includeBoneTransforms) {
         return this.sampleFrameUncached(framePosition, lookPitchDegrees, lookYawDegrees, includeBoneTransforms, "", "");
      }

      public ResourcePackMeshLoader.MeshData sampleFrame(
         float framePosition,
         float lookPitchDegrees,
         float lookYawDegrees,
         boolean includeBoneTransforms,
         String rightItemBoneOverride,
         String leftItemBoneOverride
      ) {
         return this.sampleFrameUncached(framePosition, lookPitchDegrees, lookYawDegrees, includeBoneTransforms, rightItemBoneOverride, leftItemBoneOverride);
      }

      public ResourcePackMeshLoader.MeshData sampleFrame(
         float framePosition,
         int ragdollId,
         float ageTicks,
         float lookPitchDegrees,
         float lookYawDegrees,
         float entityHeightScale,
         float externalVelocityX,
         float externalVelocityY,
         float externalVelocityZ,
         float externalForceX,
         float externalForceY,
         float externalForceZ
      ) {
         if (this.staticMesh != null) {
            return this.staticMesh;
         } else if (this.bundle == null || this.rootNode == null) {
            return ResourcePackMeshLoader.MeshData.empty(this.texture);
         } else if (this.ragdollSkeleton == null) {
            return this.sampleFrameUncached(framePosition, lookPitchDegrees, lookYawDegrees, true, "", "");
         } else {
            float maxFrame = Math.max(0.0F, this.frameCount - 1.0E-4F);
            float clampedFrame = Math.max(0.0F, Math.min(maxFrame, framePosition));
            float timeTicks = this.frameToTimeTicks(clampedFrame);
            Map<String, Matrix4f> nodeTransforms = new HashMap<>();
            Map<Integer, Matrix4f> meshTransforms = new HashMap<>();
            Map<String, Vector3f> lookOffsets = buildLookOffsets(this.rigConfig, lookPitchDegrees, lookYawDegrees);
            ResourcePackMeshLoader.buildRuntimeNodeTransforms(
               this.rootNode, new Matrix4f().identity(), timeTicks, this.channelMap, lookOffsets, nodeTransforms, meshTransforms
            );
            Map<String, Matrix4f> ragdollNodeTransforms = ResourcePackMeshLoader.applyRagdollPhysics(
               this.ragdollSkeleton,
               this.ragdollStates,
               ragdollId,
               ageTicks,
               nodeTransforms,
               entityHeightScale,
               externalVelocityX,
               externalVelocityY,
               externalVelocityZ,
               externalForceX,
               externalForceY,
               externalForceZ
            );
            return this.buildMeshFromPose(ragdollNodeTransforms, meshTransforms, true);
         }
      }

      public void clearRagdollState(int ragdollId) {
         this.ragdollStates.remove(ragdollId);
      }

      public void clearAllRagdollStates() {
         this.ragdollStates.clear();
      }

      private ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] buildBonePoseCache() {
         if (this.staticMesh != null) {
            return new ResourcePackMeshLoader.AnimatedMeshData.CachedPose[0];
         } else if (this.bundle != null && this.rootNode != null) {
            if (this.bonePoseCacheKey != null) {
               ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] diskCached = loadBonePoseCacheFromDisk(this.bonePoseCacheKey, this.frameCount);
               if (diskCached != null) {
                  ResourcePackMeshLoader.STATS_DISK_CACHE_LOAD_HITS.incrementAndGet();
                  return diskCached;
               }

               ResourcePackMeshLoader.STATS_DISK_CACHE_LOAD_MISSES.incrementAndGet();
            }

            ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] frames = new ResourcePackMeshLoader.AnimatedMeshData.CachedPose[this.frameCount];

            for (int frameIndex = 0; frameIndex < this.frameCount; frameIndex++) {
               float timeTicks = this.frameToTimeTicks(frameIndex);
               Map<String, Matrix4f> nodeTransforms = new HashMap<>();
               Map<Integer, Matrix4f> meshTransforms = new HashMap<>();
               ResourcePackMeshLoader.buildRuntimeNodeTransforms(
                  this.rootNode, new Matrix4f().identity(), timeTicks, this.channelMap, Map.of(), nodeTransforms, meshTransforms
               );
               frames[frameIndex] = new ResourcePackMeshLoader.AnimatedMeshData.CachedPose(
                  copyTransforms(nodeTransforms), copyTransformsByIndex(meshTransforms)
               );
            }

            if (this.bonePoseCacheKey != null) {
               saveBonePoseCacheToDisk(this.bonePoseCacheKey, frames);
            }

            return frames;
         } else {
            return new ResourcePackMeshLoader.AnimatedMeshData.CachedPose[0];
         }
      }

      private ResourcePackMeshLoader.MeshData sampleFrameUncached(
         float framePosition,
         float lookPitchDegrees,
         float lookYawDegrees,
         boolean includeBoneTransforms,
         String rightItemBoneOverride,
         String leftItemBoneOverride
      ) {
         ResourcePackMeshLoader.STATS_UNCACHED_SAMPLES.incrementAndGet();
         if (this.staticMesh != null) {
            return this.staticMesh;
         } else if (this.bundle != null && this.rootNode != null) {
            float clampedFrame = wrapFramePosition(framePosition, this.frameCount);
            float timeTicks = this.frameToTimeTicks(clampedFrame);
            Map<String, Matrix4f> nodeTransforms = new HashMap<>();
            Map<Integer, Matrix4f> meshTransforms = new HashMap<>();
            Map<String, Vector3f> lookOffsets = buildLookOffsets(this.rigConfig, lookPitchDegrees, lookYawDegrees);
            ResourcePackMeshLoader.buildRuntimeNodeTransforms(
               this.rootNode, new Matrix4f().identity(), timeTicks, this.channelMap, lookOffsets, nodeTransforms, meshTransforms
            );
            return this.buildMeshFromPose(nodeTransforms, meshTransforms, includeBoneTransforms, rightItemBoneOverride, leftItemBoneOverride);
         } else {
            return ResourcePackMeshLoader.MeshData.empty(this.texture);
         }
      }

      private ResourcePackMeshLoader.MeshData sampleFrameFromPrecomputedNative(
         float clampedFrame,
         float lookPitchDegrees,
         float lookYawDegrees,
         boolean includeBoneTransforms,
         String rightItemBoneOverride,
         String leftItemBoneOverride
      ) {
         if (!NativeAnimationBackend.isAvailable()) {
            return null;
         } else if (this.precomputedFinalBoneMatricesByFrame.length != 0
            && this.precomputedBoneActiveByFrame.length != 0
            && this.precomputedMeshMatricesByFrame.length != 0) {
            int availableFrames = this.precomputedFinalBoneMatricesByFrame.length;
            if (availableFrames <= 0) {
               return null;
            } else {
               float wrappedFrame = wrapFramePosition(clampedFrame, availableFrames);
               int frameA = (int)Math.floor(wrappedFrame);
               if (frameA < 0 || frameA >= availableFrames) {
                  frameA = 0;
               }

               int frameB = (frameA + 1) % availableFrames;
               float alpha = Math.max(0.0F, Math.min(1.0F, wrappedFrame - frameA));
               int boneCount = this.bundle.bones().size();
               int meshCount = Math.max(1, this.bundle.meshCount());
               int vertexCount = this.bundle.positions().size();
               ResourcePackMeshLoader.NativeSkinScratch scratch = ResourcePackMeshLoader.borrowNativeScratch(this.bundle, boneCount, meshCount, vertexCount);
               float[] bonesA = this.precomputedFinalBoneMatricesByFrame[frameA];
               float[] bonesB = this.precomputedFinalBoneMatricesByFrame[frameB];
               int[] activeA = this.precomputedBoneActiveByFrame[frameA];
               int[] activeB = this.precomputedBoneActiveByFrame[frameB];
               float[] meshA = this.precomputedMeshMatricesByFrame[frameA];
               float[] meshB = this.precomputedMeshMatricesByFrame[frameB];
               boolean canUseFastInterpolatedLookPath = !this.requiresTorsoSubtreeLookPath(lookPitchDegrees, lookYawDegrees)
                  && this.headBoneIndices.length <= 1
                  && this.neckBoneIndices.length <= 1;
               ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides lookOverrides = canUseFastInterpolatedLookPath
                  ? this.buildLookBoneOverrides(frameA, frameB, alpha, lookPitchDegrees, lookYawDegrees)
                  : ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides.empty();
               boolean nativeSuccess = false;
               if (canUseFastInterpolatedLookPath) {
                  nativeSuccess = NativeAnimationBackend.skinVerticesInterpolated(
                     this.bundle.packedBasePositions(),
                     this.bundle.packedBoneIndices(),
                     this.bundle.packedBoneWeights(),
                     this.bundle.vertexMeshIndices(),
                     bonesA,
                     activeA,
                     bonesB,
                     activeB,
                     meshA,
                     meshB,
                     alpha,
                     lookOverrides.headBoneIndex(),
                     lookOverrides.headOverrideMatrix(),
                     lookOverrides.nativeSecondaryBoneIndex(),
                     lookOverrides.nativeSecondaryOverrideMatrix(),
                     scratch.outputPositions,
                     vertexCount,
                     this.normalizeTransform.centerX(),
                     this.normalizeTransform.centerY(),
                     this.normalizeTransform.centerZ(),
                     this.normalizeTransform.scale(),
                     this.centerToCube,
                     this.modelTransform.offsetX(),
                     this.modelTransform.offsetY(),
                     this.modelTransform.offsetZ(),
                     this.modelTransform.rotX(),
                     this.modelTransform.rotY(),
                     this.modelTransform.rotZ(),
                     this.modelTransform.scale()
                  );
               }

               int triangleCount = this.bundle.rawTriangles().size();
               boolean allowPackedRenderStream = !this.hasTriangleTextureOverrides;
               if (canUseFastInterpolatedLookPath && nativeSuccess && allowPackedRenderStream && triangleCount > 0) {
                  float[] packedRenderStream = ResourcePackMeshLoader.buildSmoothPackedRenderStream(
                     scratch.outputPositions,
                     vertexCount,
                     this.bundle.packedTrianglePositionIndices(),
                     this.bundle.packedTriangleUvIndices(),
                     this.bundle.packedUvs(),
                     triangleCount
                  );
                  if (packedRenderStream != null) {
                     Map<String, Matrix4f> normalizedFrameBones;
                     if (includeBoneTransforms) {
                        Map<String, Matrix4f> frameBones = this.buildConfiguredBonesForFrame(
                           frameA, frameB, alpha, lookPitchDegrees, lookYawDegrees, rightItemBoneOverride, leftItemBoneOverride
                        );
                        normalizedFrameBones = ResourcePackMeshLoader.normalizeBoneTransforms(
                           frameBones, this.normalizeTransform, this.centerToCube, this.modelTransform
                        );
                     } else {
                        normalizedFrameBones = Map.of();
                     }

                     return ResourcePackMeshLoader.MeshData.packed(this.texture, packedRenderStream, triangleCount, normalizedFrameBones);
                  }
               }

               if (!nativeSuccess) {
                  if (!(alpha <= 1.0E-4F) && frameA != frameB) {
                     blendPackedMatrices(bonesA, bonesB, alpha, scratch.packedBoneMatrices, boneCount * 16);

                     for (int i = 0; i < boneCount; i++) {
                        scratch.packedBoneActive[i] = activeA[i] == 0 && activeB[i] == 0 ? 0 : 1;
                     }

                     blendPackedMatrices(meshA, meshB, alpha, scratch.packedMeshMatrices, meshCount * 16);
                  } else {
                     System.arraycopy(bonesA, 0, scratch.packedBoneMatrices, 0, boneCount * 16);
                     System.arraycopy(activeA, 0, scratch.packedBoneActive, 0, boneCount);
                     System.arraycopy(meshA, 0, scratch.packedMeshMatrices, 0, meshCount * 16);
                  }

                  this.applyLiveLookToPackedBones(frameA, frameB, alpha, scratch.packedBoneMatrices, scratch.packedBoneActive, lookPitchDegrees, lookYawDegrees);
                  nativeSuccess = NativeAnimationBackend.skinVertices(
                     this.bundle.packedBasePositions(),
                     this.bundle.packedBoneIndices(),
                     this.bundle.packedBoneWeights(),
                     this.bundle.vertexMeshIndices(),
                     scratch.packedBoneMatrices,
                     scratch.packedBoneActive,
                     scratch.packedMeshMatrices,
                     scratch.outputPositions,
                     vertexCount,
                     this.normalizeTransform.centerX(),
                     this.normalizeTransform.centerY(),
                     this.normalizeTransform.centerZ(),
                     this.normalizeTransform.scale(),
                     this.centerToCube,
                     this.modelTransform.offsetX(),
                     this.modelTransform.offsetY(),
                     this.modelTransform.offsetZ(),
                     this.modelTransform.rotX(),
                     this.modelTransform.rotY(),
                     this.modelTransform.rotZ(),
                     this.modelTransform.scale()
                  );
               }

               if (!nativeSuccess) {
                  return null;
               } else {
                  Map<String, Matrix4f> normalizedFrameBones;
                  if (includeBoneTransforms) {
                     Map<String, Matrix4f> frameBones = this.buildConfiguredBonesForFrame(
                        frameA, frameB, alpha, lookPitchDegrees, lookYawDegrees, rightItemBoneOverride, leftItemBoneOverride
                     );
                     normalizedFrameBones = ResourcePackMeshLoader.normalizeBoneTransforms(
                        frameBones, this.normalizeTransform, this.centerToCube, this.modelTransform
                     );
                  } else {
                     normalizedFrameBones = Map.of();
                  }

                  if (allowPackedRenderStream && triangleCount > 0) {
                     float[] packedRenderStream = ResourcePackMeshLoader.buildSmoothPackedRenderStream(
                        scratch.outputPositions,
                        vertexCount,
                        this.bundle.packedTrianglePositionIndices(),
                        this.bundle.packedTriangleUvIndices(),
                        this.bundle.packedUvs(),
                        triangleCount
                     );
                     if (packedRenderStream != null) {
                        return ResourcePackMeshLoader.MeshData.packed(this.texture, packedRenderStream, triangleCount, normalizedFrameBones);
                     }
                  }

                  return ResourcePackMeshLoader.buildMeshFromRawPacked(
                     this.bundle.rawTriangles(),
                     scratch.outputPositions,
                     vertexCount,
                     this.bundle.uvs(),
                     this.texture,
                     this.normalizeTransform,
                     this.centerToCube,
                     this.modelTransform,
                     true,
                     normalizedFrameBones
                  );
               }
            }
         } else {
            return null;
         }
      }

      private Map<String, Matrix4f> buildConfiguredBonesForFrame(
         int frameA, int frameB, float alpha, float lookPitchDegrees, float lookYawDegrees, String rightItemBoneOverride, String leftItemBoneOverride
      ) {
         if (this.cachedBonePoses.length != 0 && this.rigConfig != null) {
            Map<String, Matrix4f> sampledNodes = new HashMap<>();
            float headPitchFactor = sanitizeLookFactor(this.rigConfig.headLookPitchFactor(), 0.55F);
            float headYawFactor = sanitizeLookFactor(this.rigConfig.headLookYawFactor(), 0.55F);
            float neckFactor = sanitizeLookFactor(this.rigConfig.neckLookFactor(), 0.35F);
            float pitch = clamp(lookPitchDegrees, -70.0F, 70.0F) * headPitchFactor;
            float yaw = -clamp(lookYawDegrees, -80.0F, 80.0F) * headYawFactor;
            float neckPitch = pitch * neckFactor;
            float neckYaw = yaw * neckFactor;
            String resolvedRightItemBone = firstNonBlank(rightItemBoneOverride, this.rigConfig.rightItemBone(), this.rigConfig.rightHandBone());
            String resolvedLeftItemBone = firstNonBlank(leftItemBoneOverride, this.rigConfig.leftItemBone(), this.rigConfig.leftHandBone());
            this.addInterpolatedNode(sampledNodes, this.rigConfig.headBone(), frameA, frameB, alpha, pitch, yaw);
            this.addInterpolatedNode(sampledNodes, this.rigConfig.neckBone(), frameA, frameB, alpha, neckPitch, neckYaw);
            this.addInterpolatedNode(sampledNodes, this.rigConfig.rightHandBone(), frameA, frameB, alpha, 0.0F, 0.0F);
            this.addInterpolatedNode(sampledNodes, this.rigConfig.leftHandBone(), frameA, frameB, alpha, 0.0F, 0.0F);
            this.addInterpolatedNode(sampledNodes, resolvedRightItemBone, frameA, frameB, alpha, 0.0F, 0.0F);
            this.addInterpolatedNode(sampledNodes, resolvedLeftItemBone, frameA, frameB, alpha, 0.0F, 0.0F);
            return sampledNodes.isEmpty()
               ? Map.of()
               : ResourcePackMeshLoader.captureConfiguredBones(this.rigConfig, sampledNodes, resolvedRightItemBone, resolvedLeftItemBone);
         } else {
            return Map.of();
         }
      }

      private void addInterpolatedNode(Map<String, Matrix4f> out, String nodeName, int frameA, int frameB, float alpha, float lookPitch, float lookYaw) {
         for (String candidate : FbxRigConfig.splitBoneTargets(nodeName)) {
            Matrix4f matrixA = ResourcePackMeshLoader.lookupNodeTransform(this.cachedBonePoses[frameA].nodeTransforms(), candidate);
            Matrix4f matrixB = ResourcePackMeshLoader.lookupNodeTransform(this.cachedBonePoses[frameB].nodeTransforms(), candidate);
            Matrix4f blended = interpolateMatrix(matrixA, matrixB, alpha);
            if (blended != null) {
               if (Math.abs(lookPitch) >= 0.001F || Math.abs(lookYaw) >= 0.001F) {
                  blended = rotateMatrixPreserveTrs(blended, lookPitch, lookYaw);
               }

               out.put(candidate, blended);
            }
         }
      }

      private static Matrix4f interpolateMatrix(Matrix4f a, Matrix4f b, float alpha) {
         if (a == null && b == null) {
            return null;
         } else if (a == null) {
            return new Matrix4f(b);
         } else if (b == null) {
            return new Matrix4f(a);
         } else if (alpha <= 1.0E-4F) {
            return new Matrix4f(a);
         } else if (alpha >= 0.9999F) {
            return new Matrix4f(b);
         } else {
            Vector3f translationA = a.getTranslation(new Vector3f());
            Vector3f translationB = b.getTranslation(new Vector3f());
            Vector3f scaleA = a.getScale(new Vector3f());
            Vector3f scaleB = b.getScale(new Vector3f());
            Quaternionf rotationA = a.getUnnormalizedRotation(new Quaternionf()).normalize();
            Quaternionf rotationB = b.getUnnormalizedRotation(new Quaternionf()).normalize();
            Vector3f blendedTranslation = new Vector3f(
               lerp(translationA.x, translationB.x, alpha), lerp(translationA.y, translationB.y, alpha), lerp(translationA.z, translationB.z, alpha)
            );
            Vector3f blendedScale = new Vector3f(
               safeScale(lerp(scaleA.x, scaleB.x, alpha)), safeScale(lerp(scaleA.y, scaleB.y, alpha)), safeScale(lerp(scaleA.z, scaleB.z, alpha))
            );
            Quaternionf blendedRotation = new Quaternionf(rotationA).slerp(rotationB, alpha).normalize();
            return new Matrix4f().identity().translate(blendedTranslation).rotate(blendedRotation).scale(blendedScale);
         }
      }

      private static float lerp(float a, float b, float alpha) {
         return a + (b - a) * alpha;
      }

      private static void collectSceneNodeNames(ResourcePackMeshLoader.SceneNode node, Set<String> out) {
         if (node != null && out != null) {
            if (node.name() != null && !node.name().isBlank()) {
               out.add(node.name());
            }

            if (node.children() != null && !node.children().isEmpty()) {
               for (ResourcePackMeshLoader.SceneNode child : node.children()) {
                  collectSceneNodeNames(child, out);
               }
            }
         }
      }

      private static float safeScale(float value) {
         return Float.isFinite(value) && !(Math.abs(value) < 1.0E-4F) ? value : 1.0F;
      }

      private static void blendPackedMatrices(float[] a, float[] b, float alpha, float[] out, int length) {
         for (int i = 0; i < length; i++) {
            out[i] = a[i] + (b[i] - a[i]) * alpha;
         }
      }

      private void applyLiveLookToPackedBones(
         int frameA, int frameB, float alpha, float[] packedBoneMatrices, int[] packedBoneActive, float lookPitchDegrees, float lookYawDegrees
      ) {
         if (this.headBoneIndices.length != 0 || this.neckBoneIndices.length != 0) {
            float headPitchFactor = this.rigConfig == null ? 0.55F : sanitizeLookFactor(this.rigConfig.headLookPitchFactor(), 0.55F);
            float headYawFactor = this.rigConfig == null ? 0.55F : sanitizeLookFactor(this.rigConfig.headLookYawFactor(), 0.55F);
            float neckFactor = this.rigConfig == null ? 0.35F : sanitizeLookFactor(this.rigConfig.neckLookFactor(), 0.35F);
            float basePitch = clamp(lookPitchDegrees, -70.0F, 70.0F) * headPitchFactor;
            float baseYaw = -clamp(lookYawDegrees, -80.0F, 80.0F) * headYawFactor;
            float neckPitch = basePitch * neckFactor;
            float neckYaw = baseYaw * neckFactor;
            Set<Integer> appliedBoneIndices = new HashSet<>();

            for (int boneIndex : this.headBoneIndices) {
               if (boneIndex >= 0 && appliedBoneIndices.add(boneIndex)) {
                  boolean overridden = this.overwritePackedBoneFromPoseLook(
                     boneIndex, frameA, frameB, alpha, basePitch, baseYaw, packedBoneMatrices, packedBoneActive
                  );
                  if (!overridden) {
                     rotatePackedBoneMatrix(packedBoneMatrices, boneIndex, basePitch, baseYaw);
                     markPackedBoneActive(packedBoneActive, boneIndex);
                  }
               }
            }

            for (int boneIndexx : this.neckBoneIndices) {
               if (boneIndexx >= 0 && appliedBoneIndices.add(boneIndexx)) {
                  boolean overridden = this.overwritePackedBoneFromPoseLook(
                     boneIndexx, frameA, frameB, alpha, neckPitch, neckYaw, packedBoneMatrices, packedBoneActive
                  );
                  if (!overridden) {
                     rotatePackedBoneMatrix(packedBoneMatrices, boneIndexx, neckPitch, neckYaw);
                     markPackedBoneActive(packedBoneActive, boneIndexx);
                  }
               }
            }
         }
      }

      private ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides buildLookBoneOverrides(
         int frameA, int frameB, float alpha, float lookPitchDegrees, float lookYawDegrees
      ) {
         float headPitchFactor = this.rigConfig == null ? 0.55F : sanitizeLookFactor(this.rigConfig.headLookPitchFactor(), 0.55F);
         float headYawFactor = this.rigConfig == null ? 0.55F : sanitizeLookFactor(this.rigConfig.headLookYawFactor(), 0.55F);
         float neckFactor = this.rigConfig == null ? 0.35F : sanitizeLookFactor(this.rigConfig.neckLookFactor(), 0.35F);
         float pitch = clamp(lookPitchDegrees, -70.0F, 70.0F) * headPitchFactor;
         float yaw = -clamp(lookYawDegrees, -80.0F, 80.0F) * headYawFactor;
         float neckPitch = pitch * neckFactor;
         float neckYaw = yaw * neckFactor;
         if (Math.abs(pitch) < 0.001F && Math.abs(yaw) < 0.001F) {
            return ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides.empty();
         } else {
            float[] headOverride = this.computeLookOverrideBoneMatrix(this.headBoneIndex, frameA, frameB, alpha, pitch, yaw);
            float[] neckOverride = this.computeLookOverrideBoneMatrix(this.neckBoneIndex, frameA, frameB, alpha, neckPitch, neckYaw);
            int headIndex = headOverride == null ? -1 : this.headBoneIndex;
            int neckIndex = neckOverride == null ? -1 : this.neckBoneIndex;
            return new ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides(headIndex, headOverride, neckIndex, neckOverride, -1, null);
         }
      }

      private boolean applyPreciseLiveLookToPackedBones(
         int frameA, int frameB, float alpha, float[] packedBoneMatrices, int[] packedBoneActive, float lookPitchDegrees, float lookYawDegrees
      ) {
         return false;
      }

      private boolean overwritePackedBoneFromPoseLook(
         int boneIndex, int frameA, int frameB, float alpha, float lookPitchDegrees, float lookYawDegrees, float[] packedBoneMatrices, int[] packedBoneActive
      ) {
         float[] override = this.computeLookOverrideBoneMatrix(boneIndex, frameA, frameB, alpha, lookPitchDegrees, lookYawDegrees);
         if (override == null) {
            return false;
         } else {
            System.arraycopy(override, 0, packedBoneMatrices, boneIndex * 16, 16);
            if (packedBoneActive != null && boneIndex < packedBoneActive.length) {
               packedBoneActive[boneIndex] = 1;
            }

            return true;
         }
      }

      private float[] computeLookOverrideBoneMatrix(int boneIndex, int frameA, int frameB, float alpha, float lookPitchDegrees, float lookYawDegrees) {
         if (boneIndex >= 0 && this.bundle != null && this.cachedBonePoses.length != 0) {
            List<ResourcePackMeshLoader.MeshBone> bones = this.bundle.bones();
            if (boneIndex >= bones.size()) {
               return null;
            } else {
               ResourcePackMeshLoader.MeshBone bone = bones.get(boneIndex);
               String boneName = bone.name();
               Matrix4f nodeTransformA = ResourcePackMeshLoader.lookupNodeTransform(this.cachedBonePoses[frameA].nodeTransforms(), boneName);
               Matrix4f nodeTransformB = ResourcePackMeshLoader.lookupNodeTransform(this.cachedBonePoses[frameB].nodeTransforms(), boneName);
               Matrix4f nodeGlobal = interpolateMatrix(nodeTransformA, nodeTransformB, alpha);
               if (nodeGlobal == null) {
                  return null;
               } else {
                  String parentName = this.parentNodeByName.get(boneName);
                  Matrix4f parentGlobal = null;
                  if (parentName != null && !parentName.isBlank()) {
                     Matrix4f parentA = ResourcePackMeshLoader.lookupNodeTransform(this.cachedBonePoses[frameA].nodeTransforms(), parentName);
                     Matrix4f parentB = ResourcePackMeshLoader.lookupNodeTransform(this.cachedBonePoses[frameB].nodeTransforms(), parentName);
                     parentGlobal = interpolateMatrix(parentA, parentB, alpha);
                  }

                  Matrix4f local = parentGlobal == null ? new Matrix4f(nodeGlobal) : new Matrix4f(parentGlobal).invert().mul(nodeGlobal);
                  Vector3f translation = new Vector3f();
                  Vector3f scale = new Vector3f();
                  Quaternionf rotation = new Quaternionf();
                  local.getTranslation(translation);
                  local.getScale(scale);
                  local.getUnnormalizedRotation(rotation).normalize();
                  if (scale.x < 1.0E-4F || Float.isNaN(scale.x)) {
                     scale.x = 1.0F;
                  }

                  if (scale.y < 1.0E-4F || Float.isNaN(scale.y)) {
                     scale.y = 1.0F;
                  }

                  if (scale.z < 1.0E-4F || Float.isNaN(scale.z)) {
                     scale.z = 1.0F;
                  }

                  Quaternionf lookRotation = new Quaternionf()
                     .rotationXYZ((float)Math.toRadians(lookPitchDegrees), (float)Math.toRadians(lookYawDegrees), 0.0F);
                  rotation.mul(lookRotation).normalize();
                  Matrix4f localLook = new Matrix4f().identity().translate(translation).rotate(rotation).scale(scale);
                  Matrix4f nodeLook = parentGlobal == null ? localLook : new Matrix4f(parentGlobal).mul(localLook);
                  Matrix4f finalBoneTransform = new Matrix4f(this.bundle.globalInverse()).mul(nodeLook).mul(bone.offset());
                  float[] out = new float[16];
                  ResourcePackMeshLoader.writeMatrixArray(out, 0, finalBoneTransform);
                  return out;
               }
            }
         } else {
            return null;
         }
      }

      private static float wrapFramePosition(float framePosition, int frameCount) {
         if (frameCount > 1 && Float.isFinite(framePosition)) {
            float wrapped = framePosition % frameCount;
            if (wrapped < 0.0F) {
               wrapped += frameCount;
            }

            return wrapped >= frameCount ? 0.0F : wrapped;
         } else {
            return 0.0F;
         }
      }

      private static Map<String, String> buildParentNodeLookup(ResourcePackMeshLoader.SceneNode rootNode) {
         if (rootNode == null) {
            return Map.of();
         } else {
            Map<String, String> parentByName = new HashMap<>();
            ResourcePackMeshLoader.collectNodeParents(rootNode, "", parentByName);
            return parentByName.isEmpty() ? Map.of() : Map.copyOf(parentByName);
         }
      }

      private static void rotatePackedBoneMatrix(float[] packedBoneMatrices, int boneIndex, float pitchDegrees, float yawDegrees) {
         if (boneIndex >= 0 && (!(Math.abs(pitchDegrees) < 0.001F) || !(Math.abs(yawDegrees) < 0.001F))) {
            int offset = boneIndex * 16;
            Matrix4f matrix = ResourcePackMeshLoader.readPackedMatrix(packedBoneMatrices, offset);
            Matrix4f rotated = rotateMatrixPreserveTrs(matrix, pitchDegrees, yawDegrees);
            ResourcePackMeshLoader.writeMatrixArray(packedBoneMatrices, offset, rotated);
         }
      }

      private static void rotatePackedBoneMatrixRotationOnly(float[] packedBoneMatrices, int boneIndex, float pitchDegrees, float yawDegrees) {
         if (boneIndex >= 0 && (!(Math.abs(pitchDegrees) < 0.001F) || !(Math.abs(yawDegrees) < 0.001F))) {
            int offset = boneIndex * 16;
            Matrix4f matrix = ResourcePackMeshLoader.readPackedMatrix(packedBoneMatrices, offset);
            Matrix4f rotated = rotateMatrixRotationOnly(matrix, pitchDegrees, yawDegrees);
            ResourcePackMeshLoader.writeMatrixArray(packedBoneMatrices, offset, rotated);
         }
      }

      private boolean requiresTorsoSubtreeLookPath(float lookPitchDegrees, float lookYawDegrees) {
         if (this.headBoneIndex < 0 && this.neckBoneIndex < 0) {
            return false;
         } else {
            float pitch = clamp(lookPitchDegrees, -70.0F, 70.0F);
            float yaw = clamp(lookYawDegrees, -80.0F, 80.0F);
            return Math.abs(pitch) > 0.001F || Math.abs(yaw) > 0.001F;
         }
      }

      private void applyFallbackTorsoLookToSubtree(float[] packedBoneMatrices, int[] packedBoneActive, float pitchDegrees, float yawDegrees) {
         if (this.torsoLookBoneIndex >= 0) {
            this.applyFallbackLookToSubtree(
               this.torsoLookBoneIndex, this.torsoLookSubtreeBoneIndices, packedBoneMatrices, packedBoneActive, pitchDegrees, yawDegrees
            );
         }
      }

      private boolean applyFallbackLookToSubtree(
         int rootBoneIndex, int[] subtreeBoneIndices, float[] packedBoneMatrices, int[] packedBoneActive, float pitchDegrees, float yawDegrees
      ) {
         if (rootBoneIndex >= 0 && (!(Math.abs(pitchDegrees) < 0.001F) || !(Math.abs(yawDegrees) < 0.001F))) {
            int offset = rootBoneIndex * 16;
            Matrix4f current = ResourcePackMeshLoader.readPackedMatrix(packedBoneMatrices, offset);
            Matrix4f overridden = rotateMatrixPreserveTrs(current, pitchDegrees, yawDegrees);
            float[] overrideArray = new float[16];
            ResourcePackMeshLoader.writeMatrixArray(overrideArray, 0, overridden);
            return this.applyPackedOverrideToSubtree(rootBoneIndex, subtreeBoneIndices, overrideArray, packedBoneMatrices, packedBoneActive);
         } else {
            return false;
         }
      }

      private static Matrix4f rotateMatrixPreserveTrs(Matrix4f source, float pitchDegrees, float yawDegrees) {
         Vector3f translation = new Vector3f();
         Vector3f scale = new Vector3f();
         Quaternionf rotation = new Quaternionf();
         source.getTranslation(translation);
         source.getScale(scale);
         source.getUnnormalizedRotation(rotation).normalize();
         if (!Float.isFinite(scale.x) || Math.abs(scale.x) < 1.0E-4F) {
            scale.x = 1.0F;
         }

         if (!Float.isFinite(scale.y) || Math.abs(scale.y) < 1.0E-4F) {
            scale.y = 1.0F;
         }

         if (!Float.isFinite(scale.z) || Math.abs(scale.z) < 1.0E-4F) {
            scale.z = 1.0F;
         }

         Quaternionf lookRotation = new Quaternionf().rotationXYZ((float)Math.toRadians(pitchDegrees), (float)Math.toRadians(yawDegrees), 0.0F);
         rotation.mul(lookRotation).normalize();
         return new Matrix4f().identity().translate(translation).rotate(rotation).scale(scale);
      }

      private static Matrix4f rotateMatrixRotationOnly(Matrix4f source, float pitchDegrees, float yawDegrees) {
         Vector3f translation = new Vector3f();
         Quaternionf rotation = new Quaternionf();
         source.getTranslation(translation);
         source.getNormalizedRotation(rotation).normalize();
         Quaternionf lookRotation = new Quaternionf().rotationXYZ((float)Math.toRadians(pitchDegrees), (float)Math.toRadians(yawDegrees), 0.0F);
         rotation.mul(lookRotation).normalize();
         return new Matrix4f().identity().translate(translation).rotate(rotation);
      }

      private boolean applyPackedTorsoOverrideToSubtree(float[] torsoOverrideMatrix, float[] packedBoneMatrices, int[] packedBoneActive) {
         return this.applyPackedOverrideToSubtree(
            this.torsoLookBoneIndex, this.torsoLookSubtreeBoneIndices, torsoOverrideMatrix, packedBoneMatrices, packedBoneActive
         );
      }

      private boolean applyPackedOverrideToSubtree(
         int rootBoneIndex, int[] subtreeBoneIndices, float[] rootOverrideMatrix, float[] packedBoneMatrices, int[] packedBoneActive
      ) {
         if (rootBoneIndex >= 0 && rootOverrideMatrix != null && rootOverrideMatrix.length >= 16) {
            int rootOffset = rootBoneIndex * 16;
            Matrix4f currentRoot = ResourcePackMeshLoader.readPackedMatrix(packedBoneMatrices, rootOffset);
            Matrix4f desiredRoot = ResourcePackMeshLoader.readPackedMatrix(rootOverrideMatrix, 0);
            Matrix4f invCurrentRoot = new Matrix4f(currentRoot).invert();
            Matrix4f delta = new Matrix4f(desiredRoot).mul(invCurrentRoot);
            if (subtreeBoneIndices != null && subtreeBoneIndices.length != 0) {
               for (int boneIndex : subtreeBoneIndices) {
                  if (boneIndex >= 0) {
                     int offset = boneIndex * 16;
                     Matrix4f original = ResourcePackMeshLoader.readPackedMatrix(packedBoneMatrices, offset);
                     Matrix4f transformed = new Matrix4f(delta).mul(original);
                     ResourcePackMeshLoader.writeMatrixArray(packedBoneMatrices, offset, transformed);
                     markPackedBoneActive(packedBoneActive, boneIndex);
                  }
               }

               return true;
            } else {
               ResourcePackMeshLoader.writeMatrixArray(packedBoneMatrices, rootOffset, desiredRoot);
               markPackedBoneActive(packedBoneActive, rootBoneIndex);
               return true;
            }
         } else {
            return false;
         }
      }

      private static void markPackedBoneActive(int[] packedBoneActive, int boneIndex) {
         if (packedBoneActive != null && boneIndex >= 0 && boneIndex < packedBoneActive.length) {
            packedBoneActive[boneIndex] = 1;
         }
      }

      private ResourcePackMeshLoader.MeshData buildMeshFromPose(
         Map<String, Matrix4f> nodeTransforms, Map<Integer, Matrix4f> meshTransforms, boolean includeBoneTransforms
      ) {
         return this.buildMeshFromPose(nodeTransforms, meshTransforms, includeBoneTransforms, "", "");
      }

      private ResourcePackMeshLoader.MeshData buildMeshFromPose(
         Map<String, Matrix4f> nodeTransforms,
         Map<Integer, Matrix4f> meshTransforms,
         boolean includeBoneTransforms,
         String rightItemBoneOverride,
         String leftItemBoneOverride
      ) {
         ResourcePackMeshLoader.SkinnedPositions skinnedPositions = ResourcePackMeshLoader.skinPositions(
            this.bundle, nodeTransforms, meshTransforms, this.normalizeTransform, this.centerToCube, this.modelTransform
         );
         Map<String, Matrix4f> frameBones = includeBoneTransforms
            ? ResourcePackMeshLoader.normalizeBoneTransforms(
               ResourcePackMeshLoader.captureConfiguredBones(this.rigConfig, nodeTransforms, rightItemBoneOverride, leftItemBoneOverride),
               this.normalizeTransform,
               this.centerToCube,
               this.modelTransform
            )
            : Map.of();
         return skinnedPositions.packedPositions() != null
            ? ResourcePackMeshLoader.buildMeshFromRawPacked(
               this.bundle.rawTriangles,
               skinnedPositions.packedPositions(),
               skinnedPositions.vertexCount(),
               this.bundle.uvs,
               this.texture,
               this.normalizeTransform,
               this.centerToCube,
               this.modelTransform,
               skinnedPositions.positionsAlreadyTransformed(),
               frameBones
            )
            : ResourcePackMeshLoader.buildMeshFromRaw(
               this.bundle.rawTriangles,
               skinnedPositions.positions(),
               this.bundle.uvs,
               this.texture,
               this.normalizeTransform,
               this.centerToCube,
               this.modelTransform,
               frameBones
            );
      }

      private float frameToTimeTicks(float framePosition) {
         float timeSeconds = framePosition / 24.0F;
         float timeTicks = timeSeconds * this.ticksPerSecond;
         if (this.durationTicks > 0.0F) {
            timeTicks = Math.max(0.0F, Math.min(this.durationTicks, timeTicks));
         }

         return timeTicks;
      }

      private static int resolveBoneIndex(ResourcePackMeshLoader.MeshBundle bundle, String boneName) {
         if (bundle != null && bundle.bones() != null) {
            for (String candidate : FbxRigConfig.splitBoneTargets(boneName)) {
               for (int i = 0; i < bundle.bones().size(); i++) {
                  ResourcePackMeshLoader.MeshBone bone = bundle.bones().get(i);
                  if (bone != null && bone.name() != null && ResourcePackMeshLoader.boneNamesEquivalent(candidate, bone.name())) {
                     return i;
                  }
               }

               String canonicalCandidate = ResourcePackMeshLoader.canonicalBoneName(candidate);
               if (!canonicalCandidate.isBlank()) {
                  for (int ix = 0; ix < bundle.bones().size(); ix++) {
                     ResourcePackMeshLoader.MeshBone bone = bundle.bones().get(ix);
                     if (bone != null && bone.name() != null) {
                        String canonicalBone = ResourcePackMeshLoader.canonicalBoneName(bone.name());
                        if (!canonicalBone.isBlank() && (canonicalBone.contains(canonicalCandidate) || canonicalCandidate.contains(canonicalBone))) {
                           return ix;
                        }
                     }
                  }
               }
            }

            return -1;
         } else {
            return -1;
         }
      }

      private static int[] resolveBoneIndices(ResourcePackMeshLoader.MeshBundle bundle, String boneName) {
         if (bundle != null && bundle.bones() != null) {
            LinkedHashSet<Integer> indices = new LinkedHashSet<>();

            for (String candidate : FbxRigConfig.splitBoneTargets(boneName)) {
               boolean matchedExact = false;

               for (int i = 0; i < bundle.bones().size(); i++) {
                  ResourcePackMeshLoader.MeshBone bone = bundle.bones().get(i);
                  if (bone != null && bone.name() != null && ResourcePackMeshLoader.boneNamesEquivalent(candidate, bone.name())) {
                     indices.add(i);
                     matchedExact = true;
                     break;
                  }
               }

               if (!matchedExact) {
                  String canonicalCandidate = ResourcePackMeshLoader.canonicalBoneName(candidate);
                  if (!canonicalCandidate.isBlank()) {
                     for (int ix = 0; ix < bundle.bones().size(); ix++) {
                        ResourcePackMeshLoader.MeshBone bone = bundle.bones().get(ix);
                        if (bone != null && bone.name() != null) {
                           String canonicalBone = ResourcePackMeshLoader.canonicalBoneName(bone.name());
                           if (!canonicalBone.isBlank() && (canonicalBone.contains(canonicalCandidate) || canonicalCandidate.contains(canonicalBone))) {
                              indices.add(ix);
                           }
                        }
                     }
                  }
               }
            }

            if (indices.isEmpty()) {
               return new int[0];
            } else {
               int[] out = new int[indices.size()];
               int outIndex = 0;

               for (Integer index : indices) {
                  if (index != null) {
                     out[outIndex++] = index;
                  }
               }

               return outIndex == out.length ? out : Arrays.copyOf(out, outIndex);
            }
         } else {
            return new int[0];
         }
      }

      private static int[] buildBoneSubtreeIndices(ResourcePackMeshLoader.MeshBundle bundle, Map<String, String> parentByName, int rootBoneIndex) {
         if (bundle == null || bundle.bones() == null || bundle.bones().isEmpty() || rootBoneIndex < 0 || rootBoneIndex >= bundle.bones().size()) {
            return new int[0];
         } else if (parentByName != null && !parentByName.isEmpty()) {
            ResourcePackMeshLoader.MeshBone rootBone = bundle.bones().get(rootBoneIndex);
            if (rootBone != null && rootBone.name() != null && !rootBone.name().isBlank()) {
               String rootName = rootBone.name();
               List<Integer> subtree = new ArrayList<>();

               for (int i = 0; i < bundle.bones().size(); i++) {
                  ResourcePackMeshLoader.MeshBone candidate = bundle.bones().get(i);
                  if (candidate != null && candidate.name() != null && !candidate.name().isBlank()) {
                     for (String current = candidate.name(); current != null && !current.isBlank(); current = parentByName.get(current)) {
                        if (current.equals(rootName)) {
                           subtree.add(i);
                           break;
                        }
                     }
                  }
               }

               if (subtree.isEmpty()) {
                  return new int[]{rootBoneIndex};
               } else {
                  int[] out = new int[subtree.size()];

                  for (int ix = 0; ix < subtree.size(); ix++) {
                     out[ix] = subtree.get(ix);
                  }

                  return out;
               }
            } else {
               return new int[]{rootBoneIndex};
            }
         } else {
            return new int[]{rootBoneIndex};
         }
      }

      private static int resolveTorsoLookBoneIndex(
         ResourcePackMeshLoader.MeshBundle bundle, FbxRigConfig rigConfig, int neckBoneIndex, int headBoneIndex, Map<String, String> parentLookup
      ) {
         if (bundle != null && bundle.bones() != null && !bundle.bones().isEmpty()) {
            String configuredTorso = rigConfig == null ? "" : rigConfig.resolveTorsoLookBone();
            int direct = resolveBoneIndex(bundle, configuredTorso);
            if (direct >= 0) {
               return direct;
            } else {
               String configuredUpper = rigConfig == null ? "" : rigConfig.upperTorsoBone();
               direct = resolveBoneIndex(bundle, configuredUpper);
               if (direct >= 0) {
                  return direct;
               } else {
                  String configuredNeck = rigConfig == null ? "" : rigConfig.resolvePrimaryNeckBone();
                  direct = resolveBoneIndex(bundle, inferTorsoBoneFromNeck(configuredNeck));
                  if (direct >= 0) {
                     return direct;
                  } else {
                     direct = resolveTorsoIndexFromParentChain(bundle, neckBoneIndex, parentLookup);
                     if (direct >= 0) {
                        return direct;
                     } else {
                        direct = resolveTorsoIndexFromParentChain(bundle, headBoneIndex, parentLookup);
                        if (direct >= 0) {
                           return direct;
                        } else {
                           int contains = resolveBoneIndexByContains(bundle, "spine2");
                           if (contains >= 0) {
                              return contains;
                           } else {
                              contains = resolveBoneIndexByContains(bundle, "upperchest");
                              if (contains >= 0) {
                                 return contains;
                              } else {
                                 contains = resolveBoneIndexByContains(bundle, "chest");
                                 if (contains >= 0) {
                                    return contains;
                                 } else {
                                    contains = resolveBoneIndexByContains(bundle, "spine1");
                                    return contains >= 0 ? contains : resolveBoneIndexByContains(bundle, "spine");
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            return -1;
         }
      }

      private static int resolveTorsoIndexFromParentChain(ResourcePackMeshLoader.MeshBundle bundle, int startBoneIndex, Map<String, String> parentLookup) {
         if (bundle != null && bundle.bones() != null && startBoneIndex >= 0 && startBoneIndex < bundle.bones().size()) {
            ResourcePackMeshLoader.MeshBone startBone = bundle.bones().get(startBoneIndex);
            if (startBone != null && startBone.name() != null && !startBone.name().isBlank()) {
               String cursor = startBone.name();

               for (int depth = 0; depth < 8; depth++) {
                  String parent = parentLookup == null ? null : parentLookup.get(cursor);
                  if (parent == null || parent.isBlank() || parent.equalsIgnoreCase(cursor)) {
                     return -1;
                  }

                  int parentIndex = resolveBoneIndex(bundle, parent);
                  if (parentIndex >= 0) {
                     String lowered = parent.toLowerCase(Locale.ROOT);
                     if (lowered.contains("spine") || lowered.contains("chest") || lowered.contains("torso") || lowered.contains("pelvis")) {
                        return parentIndex;
                     }
                  }

                  cursor = parent;
               }

               return -1;
            } else {
               return -1;
            }
         } else {
            return -1;
         }
      }

      private static int resolveBoneIndexByContains(ResourcePackMeshLoader.MeshBundle bundle, String token) {
         if (bundle != null && bundle.bones() != null && token != null && !token.isBlank()) {
            String needle = token.toLowerCase(Locale.ROOT);

            for (int i = 0; i < bundle.bones().size(); i++) {
               ResourcePackMeshLoader.MeshBone bone = bundle.bones().get(i);
               if (bone != null && bone.name() != null && bone.name().toLowerCase(Locale.ROOT).contains(needle)) {
                  return i;
               }
            }

            return -1;
         } else {
            return -1;
         }
      }

      private static boolean hasTriangleTextureOverrides(List<ResourcePackMeshLoader.RawTriangle> rawTriangles, class_2960 baseTexture) {
         if (rawTriangles != null && !rawTriangles.isEmpty()) {
            for (ResourcePackMeshLoader.RawTriangle triangle : rawTriangles) {
               class_2960 triangleTexture = triangle.texture();
               if (triangleTexture != null && !triangleTexture.equals(baseTexture)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      private static ResourcePackMeshLoader.AnimatedMeshData.PrecomputedNativeFrames buildPrecomputedNativeFrames(
         ResourcePackMeshLoader.MeshBundle bundle, ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] poses, FbxRigConfig rigConfig, int frameCount
      ) {
         if (bundle != null && poses != null && poses.length != 0 && frameCount > 0) {
            int usableFrames = Math.min(frameCount, poses.length);
            int boneCount = bundle.bones().size();
            int meshCount = Math.max(1, bundle.meshCount());
            float[][] boneMatricesByFrame = new float[usableFrames][boneCount * 16];
            int[][] boneActiveByFrame = new int[usableFrames][boneCount];
            float[][] meshMatricesByFrame = new float[usableFrames][meshCount * 16];

            for (int frameIndex = 0; frameIndex < usableFrames; frameIndex++) {
               ResourcePackMeshLoader.AnimatedMeshData.CachedPose pose = poses[frameIndex];
               Matrix4f[] finalBoneTransforms = ResourcePackMeshLoader.buildFinalBoneTransforms(bundle, pose.nodeTransforms());

               for (int boneIndex = 0; boneIndex < boneCount; boneIndex++) {
                  Matrix4f matrix = finalBoneTransforms[boneIndex];
                  if (matrix == null) {
                     boneActiveByFrame[frameIndex][boneIndex] = 0;
                     matrix = ResourcePackMeshLoader.IDENTITY_MATRIX;
                  } else {
                     boneActiveByFrame[frameIndex][boneIndex] = 1;
                  }

                  ResourcePackMeshLoader.writeMatrixArray(boneMatricesByFrame[frameIndex], boneIndex * 16, matrix);
               }

               Map<Integer, Matrix4f> poseMeshTransforms = pose.meshTransforms();

               for (int meshIndex = 0; meshIndex < meshCount; meshIndex++) {
                  Matrix4f matrix = poseMeshTransforms.get(meshIndex);
                  if (matrix == null) {
                     matrix = bundle.meshBindTransforms().get(meshIndex);
                  }

                  if (matrix == null) {
                     matrix = ResourcePackMeshLoader.IDENTITY_MATRIX;
                  }

                  ResourcePackMeshLoader.writeMatrixArray(meshMatricesByFrame[frameIndex], meshIndex * 16, matrix);
               }
            }

            return new ResourcePackMeshLoader.AnimatedMeshData.PrecomputedNativeFrames(boneMatricesByFrame, boneActiveByFrame, meshMatricesByFrame);
         } else {
            return ResourcePackMeshLoader.AnimatedMeshData.PrecomputedNativeFrames.empty();
         }
      }

      private static Map<String, Matrix4f> copyTransforms(Map<String, Matrix4f> source) {
         Map<String, Matrix4f> copy = new HashMap<>(source.size());

         for (Entry<String, Matrix4f> entry : source.entrySet()) {
            copy.put(entry.getKey(), new Matrix4f((Matrix4fc)entry.getValue()));
         }

         return Map.copyOf(copy);
      }

      private static Map<Integer, Matrix4f> copyTransformsByIndex(Map<Integer, Matrix4f> source) {
         Map<Integer, Matrix4f> copy = new HashMap<>(source.size());

         for (Entry<Integer, Matrix4f> entry : source.entrySet()) {
            copy.put(entry.getKey(), new Matrix4f((Matrix4fc)entry.getValue()));
         }

         return Map.copyOf(copy);
      }

      private static ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] loadBonePoseCacheFromDisk(String cacheKey, int expectedFrameCount) {
         Path cachePath = ResourcePackMeshLoader.bonePoseCachePath(cacheKey);
         if (!Files.exists(cachePath)) {
            return null;
         } else {
            try {
               ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] var19;
               try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(cachePath)))) {
                  int version = in.readInt();
                  if (version != 1) {
                     return null;
                  }

                  int frameCount = in.readInt();
                  if (frameCount != expectedFrameCount || frameCount <= 0) {
                     return null;
                  }

                  ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] frames = new ResourcePackMeshLoader.AnimatedMeshData.CachedPose[frameCount];

                  for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                     int nodeCount = in.readInt();
                     Map<String, Matrix4f> nodeTransforms = new HashMap<>(Math.max(0, nodeCount));

                     for (int i = 0; i < nodeCount; i++) {
                        String name = in.readUTF();
                        nodeTransforms.put(name, readMatrix(in));
                     }

                     int meshCount = in.readInt();
                     Map<Integer, Matrix4f> meshTransforms = new HashMap<>(Math.max(0, meshCount));

                     for (int i = 0; i < meshCount; i++) {
                        int meshIndex = in.readInt();
                        meshTransforms.put(meshIndex, readMatrix(in));
                     }

                     frames[frameIndex] = new ResourcePackMeshLoader.AnimatedMeshData.CachedPose(Map.copyOf(nodeTransforms), Map.copyOf(meshTransforms));
                  }

                  var19 = frames;
               }

               return var19;
            } catch (Exception var16) {
               return null;
            }
         }
      }

      private static void saveBonePoseCacheToDisk(String cacheKey, ResourcePackMeshLoader.AnimatedMeshData.CachedPose[] frames) {
         Path cachePath = ResourcePackMeshLoader.bonePoseCachePath(cacheKey);

         try {
            Files.createDirectories(cachePath.getParent());

            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(cachePath)))) {
               out.writeInt(1);
               out.writeInt(frames.length);

               for (ResourcePackMeshLoader.AnimatedMeshData.CachedPose frame : frames) {
                  Map<String, Matrix4f> nodeTransforms = frame.nodeTransforms();
                  out.writeInt(nodeTransforms.size());

                  for (Entry<String, Matrix4f> entry : nodeTransforms.entrySet()) {
                     out.writeUTF(entry.getKey());
                     writeMatrix(out, entry.getValue());
                  }

                  Map<Integer, Matrix4f> meshTransforms = frame.meshTransforms();
                  out.writeInt(meshTransforms.size());

                  for (Entry<Integer, Matrix4f> entry : meshTransforms.entrySet()) {
                     out.writeInt(entry.getKey());
                     writeMatrix(out, entry.getValue());
                  }
               }
            }

            ResourcePackMeshLoader.STATS_DISK_CACHE_SAVES.incrementAndGet();
         } catch (IOException var14) {
         }
      }

      private static Matrix4f readMatrix(DataInputStream in) throws IOException {
         return new Matrix4f(
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat(),
            in.readFloat()
         );
      }

      private static void writeMatrix(DataOutputStream out, Matrix4f matrix) throws IOException {
         out.writeFloat(matrix.m00());
         out.writeFloat(matrix.m01());
         out.writeFloat(matrix.m02());
         out.writeFloat(matrix.m03());
         out.writeFloat(matrix.m10());
         out.writeFloat(matrix.m11());
         out.writeFloat(matrix.m12());
         out.writeFloat(matrix.m13());
         out.writeFloat(matrix.m20());
         out.writeFloat(matrix.m21());
         out.writeFloat(matrix.m22());
         out.writeFloat(matrix.m23());
         out.writeFloat(matrix.m30());
         out.writeFloat(matrix.m31());
         out.writeFloat(matrix.m32());
         out.writeFloat(matrix.m33());
      }

      private static String combineCacheKeys(String baseKey, String clipKey) {
         if (baseKey == null || baseKey.isBlank()) {
            return clipKey;
         } else {
            return clipKey != null && !clipKey.isBlank() ? baseKey + "_" + clipKey : baseKey;
         }
      }

      private static Map<String, Vector3f> buildLookOffsets(FbxRigConfig rigConfig, float lookPitchDegrees, float lookYawDegrees) {
         Map<String, Vector3f> offsets = new HashMap<>();
         if (rigConfig != null) {
            float headPitchFactor = sanitizeLookFactor(rigConfig.headLookPitchFactor(), 0.55F);
            float headYawFactor = sanitizeLookFactor(rigConfig.headLookYawFactor(), 0.55F);
            float neckFactor = sanitizeLookFactor(rigConfig.neckLookFactor(), 0.35F);
            float pitch = clamp(lookPitchDegrees, -70.0F, 70.0F) * headPitchFactor;
            float yaw = -clamp(lookYawDegrees, -80.0F, 80.0F) * headYawFactor;
            if (Math.abs(pitch) >= 0.001F || Math.abs(yaw) >= 0.001F) {
               addBoneOffset(offsets, rigConfig.headBone(), pitch, yaw, 0.0F);
               addBoneOffset(offsets, rigConfig.neckBone(), pitch * neckFactor, yaw * neckFactor, 0.0F);
            }
         }

         Map<String, Vector3f> externalOffsets = ResourcePackMeshLoader.EXTERNAL_BONE_ROTATION_OFFSETS.get();
         if (externalOffsets != null && !externalOffsets.isEmpty()) {
            for (Entry<String, Vector3f> entry : externalOffsets.entrySet()) {
               String bone = entry.getKey();
               Vector3f rotation = entry.getValue();
               if (bone != null && !bone.isBlank() && rotation != null) {
                  addBoneOffset(offsets, bone, rotation.x, rotation.y, rotation.z);
               }
            }
         }

         return offsets.isEmpty() ? Map.of() : Map.copyOf(offsets);
      }

      private static float sanitizeLookFactor(float value, float fallback) {
         return !Float.isFinite(value) ? fallback : clamp(value, -2.0F, 2.0F);
      }

      private static float sanitizeTorsoLookFactor(float value, float fallback) {
         if (!Float.isFinite(value)) {
            return fallback;
         } else {
            return Math.abs(value) < 1.0E-4F ? fallback : clamp(value, -2.0F, 2.0F);
         }
      }

      private static void addBoneOffset(Map<String, Vector3f> offsets, String boneName, float pitch, float yaw, float roll) {
         for (String candidate : FbxRigConfig.splitBoneTargets(boneName)) {
            Vector3f existing = offsets.get(candidate);
            if (existing == null) {
               offsets.put(candidate, new Vector3f(pitch, yaw, roll));
            } else {
               existing.add(pitch, yaw, roll);
            }
         }
      }

      private static String firstNonBlank(String... candidates) {
         if (candidates == null) {
            return "";
         } else {
            for (String candidate : candidates) {
               if (candidate != null && !candidate.isBlank()) {
                  return candidate;
               }
            }

            return "";
         }
      }

      private static String inferArmBoneFromHand(String handBoneName) {
         if (handBoneName != null && !handBoneName.isBlank()) {
            String[] inferredCandidates = new String[]{
               handBoneName.replace("Hand", "ForeArm"),
               handBoneName.replace("hand", "forearm"),
               handBoneName.replace("Hand", "LowerArm"),
               handBoneName.replace("hand", "lowerarm"),
               handBoneName.replace("Hand", "Arm"),
               handBoneName.replace("hand", "arm")
            };

            for (String inferred : inferredCandidates) {
               if (!inferred.equals(handBoneName)) {
                  return inferred;
               }
            }

            return "";
         } else {
            return "";
         }
      }

      private static String inferUpperArmFromLowerArm(String armBoneName) {
         if (armBoneName != null && !armBoneName.isBlank()) {
            String[] inferredCandidates = new String[]{
               armBoneName.replace("ForeArm", "Arm"),
               armBoneName.replace("forearm", "arm"),
               armBoneName.replace("LowerArm", "Arm"),
               armBoneName.replace("lowerarm", "arm")
            };

            for (String inferred : inferredCandidates) {
               if (!inferred.equals(armBoneName)) {
                  return inferred;
               }
            }

            return armBoneName;
         } else {
            return "";
         }
      }

      private static String inferShoulderFromArm(String armBoneName) {
         if (armBoneName != null && !armBoneName.isBlank()) {
            String[] inferredCandidates = new String[]{armBoneName.replace("Arm", "Shoulder"), armBoneName.replace("arm", "shoulder")};

            for (String inferred : inferredCandidates) {
               if (!inferred.equals(armBoneName)) {
                  return inferred;
               }
            }

            return "";
         } else {
            return "";
         }
      }

      private static String inferTorsoBoneFromNeck(String neckBoneName) {
         if (neckBoneName != null && !neckBoneName.isBlank()) {
            String[] inferredCandidates = new String[]{
               neckBoneName.replace("Neck", "Spine2"),
               neckBoneName.replace("Neck", "Spine1"),
               neckBoneName.replace("Neck", "Chest"),
               neckBoneName.replace("neck", "spine2"),
               neckBoneName.replace("neck", "spine1"),
               neckBoneName.replace("neck", "chest")
            };

            for (String inferred : inferredCandidates) {
               if (!inferred.equals(neckBoneName)) {
                  return inferred;
               }
            }

            return "";
         } else {
            return "";
         }
      }

      private static float clamp(float value, float min, float max) {
         return Math.max(min, Math.min(max, value));
      }

      private record CachedPose(Map<String, Matrix4f> nodeTransforms, Map<Integer, Matrix4f> meshTransforms) {
      }

      private record LookBoneOverrides(
         int headBoneIndex, float[] headOverrideMatrix, int neckBoneIndex, float[] neckOverrideMatrix, int torsoBoneIndex, float[] torsoOverrideMatrix
      ) {
         private static ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides empty() {
            return new ResourcePackMeshLoader.AnimatedMeshData.LookBoneOverrides(-1, null, -1, null, -1, null);
         }

         private int nativeSecondaryBoneIndex() {
            return this.torsoBoneIndex >= 0 && this.torsoOverrideMatrix != null ? this.torsoBoneIndex : this.neckBoneIndex;
         }

         private float[] nativeSecondaryOverrideMatrix() {
            return this.torsoBoneIndex >= 0 && this.torsoOverrideMatrix != null ? this.torsoOverrideMatrix : this.neckOverrideMatrix;
         }
      }

      private record PrecomputedNativeFrames(float[][] boneMatricesByFrame, int[][] boneActiveByFrame, float[][] meshMatricesByFrame) {
         private static ResourcePackMeshLoader.AnimatedMeshData.PrecomputedNativeFrames empty() {
            return new ResourcePackMeshLoader.AnimatedMeshData.PrecomputedNativeFrames(new float[0][], new int[0][], new float[0][]);
         }
      }
   }

   public record AnimationClipData(
      Map<String, ResourcePackMeshLoader.NodeAnimation> channelMap, float ticksPerSecond, float durationTicks, int frameCount, String cacheKey
   ) {
   }

   private record EmbeddedTextureLookup(Map<String, class_2960> byKey, class_2960 firstTexture) {
      private static final ResourcePackMeshLoader.EmbeddedTextureLookup EMPTY = new ResourcePackMeshLoader.EmbeddedTextureLookup(Map.of(), null);
   }

   private record FacePoint(int positionIndex, int uvIndex) {
   }

   private record FbxTextureAssignment(class_2960 modelTexture, Map<Integer, class_2960> materialTextures) {
   }

   private record MeshBone(String name, Matrix4f offset, int meshIndex) {
   }

   private record MeshBundle(
      List<Vector3f> positions,
      List<Vector2f> uvs,
      List<ResourcePackMeshLoader.RawTriangle> rawTriangles,
      int[] packedTrianglePositionIndices,
      int[] packedTriangleUvIndices,
      float[] packedUvs,
      List<ResourcePackMeshLoader.MeshBone> bones,
      List<List<ResourcePackMeshLoader.VertexWeight>> vertexWeights,
      int[] vertexMeshIndices,
      int meshCount,
      float[] packedBasePositions,
      int[] packedBoneIndices,
      float[] packedBoneWeights,
      Map<Integer, Matrix4f> meshBindTransforms,
      Matrix4f globalInverse
   ) {
   }

   public record MeshData(
      class_2960 texture,
      List<ResourcePackMeshLoader.MeshTriangle> triangles,
      Vector3fc[] vertices,
      Map<String, Matrix4f> boneTransforms,
      float[] packedRenderStream,
      int packedTriangleCount
   ) {
      private static ResourcePackMeshLoader.MeshData empty(class_2960 texture) {
         return new ResourcePackMeshLoader.MeshData(texture, List.of(), new Vector3fc[0], Map.of(), new float[0], 0);
      }

      private static ResourcePackMeshLoader.MeshData packed(
         class_2960 texture, float[] packedRenderStream, int packedTriangleCount, Map<String, Matrix4f> boneTransforms
      ) {
         return new ResourcePackMeshLoader.MeshData(
            texture,
            List.of(),
            new Vector3fc[0],
            Map.copyOf(boneTransforms),
            packedRenderStream == null ? new float[0] : packedRenderStream,
            Math.max(0, packedTriangleCount)
         );
      }
   }

   public record MeshTriangle(
      ResourcePackMeshLoader.MeshVertex a, ResourcePackMeshLoader.MeshVertex b, ResourcePackMeshLoader.MeshVertex c, Vector3f normal, class_2960 texture
   ) {
   }

   public record MeshVertex(Vector3f pos, Vector2f uv, Vector3f normal) {
   }

   private record ModelTransform(float offsetX, float offsetY, float offsetZ, float rotX, float rotY, float rotZ, float scale) {
      private static final ResourcePackMeshLoader.ModelTransform IDENTITY = new ResourcePackMeshLoader.ModelTransform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   private static final class NativeSkinScratch {
      private float[] packedBoneMatrices = new float[0];
      private int[] packedBoneActive = new int[0];
      private float[] packedMeshMatrices = new float[0];
      private float[] outputPositions = new float[0];
      private float[] outputRenderStream = new float[0];

      private void ensureCapacity(int boneCount, int meshCount, int vertexCount) {
         int boneMatrixSize = boneCount * 16;
         if (this.packedBoneMatrices.length < boneMatrixSize) {
            this.packedBoneMatrices = new float[boneMatrixSize];
         }

         if (this.packedBoneActive.length < boneCount) {
            this.packedBoneActive = new int[boneCount];
         }

         int meshMatrixSize = meshCount * 16;
         if (this.packedMeshMatrices.length < meshMatrixSize) {
            this.packedMeshMatrices = new float[meshMatrixSize];
         }

         int outputSize = vertexCount * 3;
         if (this.outputPositions.length < outputSize) {
            this.outputPositions = new float[outputSize];
         }
      }

      private void ensureRenderStreamCapacity(int triangleCount) {
         int renderStreamSize = Math.max(0, triangleCount) * 3 * 8;
         if (this.outputRenderStream.length < renderStreamSize) {
            this.outputRenderStream = new float[renderStreamSize];
         }
      }
   }

   private record NodeAnimation(
      List<ResourcePackMeshLoader.VecKey> positionKeys, List<ResourcePackMeshLoader.VecKey> scalingKeys, List<ResourcePackMeshLoader.QuatKey> rotationKeys
   ) {
   }

   private record NormalizeTransform(float centerX, float centerY, float centerZ, float scale) {
   }

   private record PackedBoneInfluences(int[] boneIndices, float[] boneWeights) {
   }

   private record QuatKey(float time, Quaternionf value) {
   }

   private record RagdollSkeleton(String[] boneNames, int[] parentIndices, int[] primaryChildren, float[] restLengths, float[] jointRadii, int rootIndex) {
   }

   private static final class RagdollState {
      private final Vector3f[] positions;
      private final Vector3f[] previousPositions;
      private final float collapseBiasX;
      private final float collapseBiasZ;
      private final Vector3f pendingWorldDelta;
      private final Vector3f lastRootPosition;
      private final Vector3f linearVelocity;
      private boolean hasLastRootPosition;
      private boolean initialized;
      private int simTicks;
      private int wakeTicks;
      private float floorY;
      private float lastAge;
      private float[] nativePositionsPacked;
      private float[] nativePreviousPositionsPacked;

      private RagdollState(int boneCount, int entityId) {
         this.positions = new Vector3f[boneCount];
         this.previousPositions = new Vector3f[boneCount];

         for (int i = 0; i < boneCount; i++) {
            this.positions[i] = new Vector3f();
            this.previousPositions[i] = new Vector3f();
         }

         int seed = entityId * 1103515245 + 12345;
         float angle = (seed & 65535) / 65535.0F * (float) (Math.PI * 2);
         float biasX = (float)Math.cos(angle);
         float biasZ = (float)Math.sin(angle);
         if (Math.abs(biasX) < 0.25F && Math.abs(biasZ) < 0.25F) {
            biasX = 0.7071F;
            biasZ = 0.7071F;
         }

         this.collapseBiasX = biasX;
         this.collapseBiasZ = biasZ;
         this.pendingWorldDelta = new Vector3f();
         this.lastRootPosition = new Vector3f();
         this.linearVelocity = new Vector3f();
         this.hasLastRootPosition = false;
         this.initialized = false;
         this.simTicks = 0;
         this.wakeTicks = 0;
         this.floorY = 0.0F;
         this.lastAge = 0.0F;
         int packedLength = boneCount * 3;
         this.nativePositionsPacked = new float[packedLength];
         this.nativePreviousPositionsPacked = new float[packedLength];
      }
   }

   private record RawTriangle(ResourcePackMeshLoader.FacePoint a, ResourcePackMeshLoader.FacePoint b, ResourcePackMeshLoader.FacePoint c, class_2960 texture) {
   }

   private record SceneNode(String name, Matrix4f localTransform, int[] meshIndices, List<ResourcePackMeshLoader.SceneNode> children) {
   }

   private record SkinnedPositions(List<Vector3f> positions, float[] packedPositions, int vertexCount, boolean positionsAlreadyTransformed) {
   }

   private record VecKey(float time, Vector3f value) {
   }

   private record VertexWeight(int boneIndex, float weight) {
   }
}
