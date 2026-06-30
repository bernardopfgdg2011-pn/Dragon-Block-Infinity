package com.brckv2.objfbxloader.client.network;

import com.brckv2.objfbxloader.ObjFbxLoader;
import com.brckv2.objfbxloader.PlatformPaths;
import com.brckv2.objfbxloader.client.render.ResourcePackMeshLoader;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import com.brckv2.objfbxloader.client.voice.VoiceLipSyncState;
import io.netty.buffer.ByteBuf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10537;
import net.minecraft.class_10539;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;
import net.minecraft.class_8710.class_9154;

public final class PlayerModelSyncClient {
   private static final class_2960 CHANNEL_ID = class_2960.method_60655("objfbxloader", "player_model_sync");
   private static final byte MSG_SET_MODEL = 1;
   private static final byte MSG_REMOVE_MODEL = 2;
   private static final byte MSG_FULL_SYNC = 3;
   private static final byte MSG_UPLOAD_BUNDLE_START = 10;
   private static final byte MSG_UPLOAD_BUNDLE_CHUNK = 11;
   private static final byte MSG_UPLOAD_BUNDLE_FINISH = 12;
   private static final byte MSG_BUNDLE_START = 20;
   private static final byte MSG_BUNDLE_CHUNK = 21;
   private static final byte MSG_BUNDLE_FINISH = 22;
   private static final byte MSG_CLIENT_LIP_SYNC = 30;
   private static final byte MSG_REMOTE_LIP_SYNC = 31;
   private static final int MAX_MODEL_ID_LENGTH = 96;
   private static final int MAX_PLAYER_NAME_LENGTH = 32;
   private static final int MAX_BUNDLE_BYTES = 100663296;
   private static final int CHUNK_BYTES = 28672;
   private static final int MAX_CHUNK_BYTES = 32768;
   private static final Set<String> MODEL_RESOURCE_EXTENSIONS = Set.of(".obj", ".fbx", ".mtl", ".gltf", ".glb", ".dae", ".json");
   private static final Set<String> TEXTURE_RESOURCE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".tga", ".webp", ".bmp", ".gif", ".dds");
   private static final List<String> MODEL_TARGET_ROOTS = List.of("models/entity/", "models/entities/", "models/player/", "models/players/");
   private static final Set<String> MTL_TEXTURE_KEYS = Set.of(
      "map_kd", "map_ka", "map_ks", "map_ke", "map_d", "map_bump", "map_ns", "bump", "norm", "disp", "decal", "refl"
   );
   private static final Pattern MODEL_ID_PATTERN = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");
   private static final Path PLAYER_MODEL_CONFIG_PATH = PlatformPaths.gameDir().resolve("configs").resolve("player_model_id.json");
   private static final Path AUTO_TEXTURE_EXPORT_PACK_ROOT = PlatformPaths.gameDir().resolve("resourcepacks").resolve("objfbxloader_auto_textures");
   private static final Path AUTO_TEXTURE_EXPORT_ASSETS_ROOT = AUTO_TEXTURE_EXPORT_PACK_ROOT.resolve("assets");
   private static final Map<Integer, PlayerModelSyncClient.ServerCachedModelEntry> serverCachedModelsByEntityId = new HashMap<>();
   private static final Map<Integer, PlayerModelSyncClient.IncomingBundleState> incomingBundlesByEntityId = new HashMap<>();
   private static class_2960 selectedPlayerModel = class_2960.method_60656("player");
   private static boolean initialized;
   private static boolean warnedChannelUnavailable;
   private static boolean loggedChannelReady;
   private static int lastAutoTextureResourceManagerIdentity = Integer.MIN_VALUE;
   private static long lastAutoTextureMirrorAt;
   private static int lastAutoTextureMirroredModelCount = -1;
   private static long lastLipSyncPublishAt = 0L;
   private static int lastPublishedLipSyncQuantized = -1;

   private PlayerModelSyncClient() {
   }

   public static void initialize() {
      if (!initialized) {
         initialized = true;
         selectedPlayerModel = loadSelectedPlayerModel();
         PayloadTypeRegistry.playC2S().register(PlayerModelSyncClient.RawChannelPayload.ID, PlayerModelSyncClient.RawChannelPayload.CODEC);
         PayloadTypeRegistry.playS2C().register(PlayerModelSyncClient.RawChannelPayload.ID, PlayerModelSyncClient.RawChannelPayload.CODEC);
         ClientPlayNetworking.registerGlobalReceiver(PlayerModelSyncClient.RawChannelPayload.ID, (payload, context) -> {
            byte[] bytes = payload.bytes();
            if (bytes != null && bytes.length != 0) {
               context.client().execute(() -> handleServerMessage(context.client(), bytes));
            }
         });
         ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> client.execute(() -> {
            warnedChannelUnavailable = false;
            loggedChannelReady = false;
            ResourcePackObjLivingOverrides.clearAllNetworkPlayerModelTargets();
            synchronized (serverCachedModelsByEntityId) {
               serverCachedModelsByEntityId.clear();
            }

            synchronized (incomingBundlesByEntityId) {
               incomingBundlesByEntityId.clear();
            }

            lastLipSyncPublishAt = 0L;
            lastPublishedLipSyncQuantized = -1;
            applyLocalPlayerSelection(client);
         }));
         ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> client.execute(() -> {
            warnedChannelUnavailable = false;
            loggedChannelReady = false;
            ResourcePackObjLivingOverrides.clearAllNetworkPlayerModelTargets();
            synchronized (serverCachedModelsByEntityId) {
               serverCachedModelsByEntityId.clear();
            }

            synchronized (incomingBundlesByEntityId) {
               incomingBundlesByEntityId.clear();
            }

            lastLipSyncPublishAt = 0L;
            lastPublishedLipSyncQuantized = -1;
         }));
      }
   }

   public static class_2960 getSelectedPlayerModel() {
      return selectedPlayerModel;
   }

   public static List<PlayerModelSyncClient.ServerCachedModelEntry> getServerCachedModelsSnapshot() {
      synchronized (serverCachedModelsByEntityId) {
         List<PlayerModelSyncClient.ServerCachedModelEntry> list = new ArrayList<>(serverCachedModelsByEntityId.values());
         list.sort((a, b) -> {
            String an = a.playerName() == null ? "" : a.playerName().toLowerCase(Locale.ROOT);
            String bn = b.playerName() == null ? "" : b.playerName().toLowerCase(Locale.ROOT);
            int cmp = an.compareTo(bn);
            return cmp != 0 ? cmp : Integer.compare(a.entityId(), b.entityId());
         });
         return list;
      }
   }

   public static class_2960 normalizeModelId(String raw) {
      if (raw == null) {
         return null;
      } else {
         String normalized = raw.trim().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty()) {
            return null;
         } else {
            class_2960 parsed = class_2960.method_12829(normalized);
            if (parsed != null) {
               return parsed;
            } else {
               return !normalized.contains(":") ? class_2960.method_12829("minecraft:" + normalized) : null;
            }
         }
      }
   }

   public static boolean setSelectedPlayerModel(class_2960 modelId, boolean persist, boolean publishNow, class_310 client) {
      if (!isAllowedModelId(modelId)) {
         return false;
      } else {
         selectedPlayerModel = modelId;
         if (persist) {
            saveSelectedPlayerModel(modelId);
         }

         if (client != null) {
            applyLocalPlayerSelection(client);
         }

         if (publishNow) {
            syncSelectedPlayerModel(true);
         }

         return true;
      }
   }

   public static void syncSelectedPlayerModel(boolean includeEmbeddedTextureExtraction) {
      publishSelectedModel(includeEmbeddedTextureExtraction);
   }

   public static void onClientTick(class_310 client) {
      if (initialized && client != null) {
         class_3300 resourceManager = client.method_1478();
         if (resourceManager != null) {
            int identity = System.identityHashCode(resourceManager);
            long now = System.currentTimeMillis();
            boolean managerChanged = identity != lastAutoTextureResourceManagerIdentity;
            boolean retryAfterEmpty = lastAutoTextureMirroredModelCount <= 0 && now - lastAutoTextureMirrorAt >= 2500L;
            if (!managerChanged && !retryAfterEmpty) {
               publishLipSyncState();
            } else {
               if (managerChanged) {
                  ResourcePackMeshLoader.clearRuntimeGeneratedTextures(client);
               }

               lastAutoTextureResourceManagerIdentity = identity;
               mirrorLoadedModelTexturesToGeneratedPack(client, resourceManager);
               publishLipSyncState();
            }
         }
      }
   }

   public static void clearGeneratedTextureMirrorCache() {
      try {
         deleteGeneratedTexturePackRoot();
      } catch (IOException var1) {
      }

      lastAutoTextureMirroredModelCount = 0;
      lastAutoTextureMirrorAt = 0L;
      lastAutoTextureResourceManagerIdentity = -1;
   }

   public static class_2960 channelId() {
      return CHANNEL_ID;
   }

   public static boolean isAllowedModelId(class_2960 modelId) {
      if (modelId == null) {
         return false;
      } else {
         String serialized = modelId.toString().toLowerCase(Locale.ROOT);
         return serialized.length() > 96 ? false : MODEL_ID_PATTERN.matcher(serialized).matches();
      }
   }

   private static void publishSelectedModel(boolean includeEmbeddedTextureExtraction) {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1562() != null) {
         if (!ClientPlayNetworking.canSend(PlayerModelSyncClient.RawChannelPayload.ID)) {
            if (!warnedChannelUnavailable) {
               warnedChannelUnavailable = true;
               ObjFbxLoader.LOGGER.warn("Player model sync channel {} is not available on this server yet; model selection not sent.", CHANNEL_ID);
            }
         } else {
            if (!loggedChannelReady) {
               loggedChannelReady = true;
               ObjFbxLoader.LOGGER.info("Player model sync channel {} is available; publishing selected model updates.", CHANNEL_ID);
            }

            byte[] bundleBytes = buildSelectedModelBundle(client, selectedPlayerModel, includeEmbeddedTextureExtraction);
            long sizeBytes = bundleBytes.length > 0 ? bundleBytes.length : estimateSelectedModelSizeBytes(client, selectedPlayerModel);
            byte[] payload = buildSetModelClientPacket(selectedPlayerModel, sizeBytes);
            if (payload.length > 0) {
               ClientPlayNetworking.send(new PlayerModelSyncClient.RawChannelPayload(payload));
               ObjFbxLoader.LOGGER.info("Sent player model selection to server: modelId={}, estimatedSizeBytes={}", selectedPlayerModel, sizeBytes);
            }

            if (bundleBytes.length > 0) {
               sendBundleUpload(selectedPlayerModel, bundleBytes);
            } else {
               ObjFbxLoader.LOGGER
                  .warn(
                     "No transferable bundle resources found for selected model {} (embeddedTextureExtraction={}); remote clients may render vanilla.",
                     selectedPlayerModel,
                     includeEmbeddedTextureExtraction
                  );
            }
         }
      }
   }

   private static void handleServerMessage(class_310 client, byte[] payloadBytes) {
      ByteArrayInputStream in = new ByteArrayInputStream(payloadBytes);
      int first = in.read();
      if (first >= 0) {
         byte messageType = (byte)first;
         if (messageType == 1) {
            int entityId = readVarInt(in);
            class_2960 modelId = parseModelId(readString(in, 96));
            String playerName = in.available() > 0 ? safePlayerName(readString(in, 32)) : "unknown";
            long sizeBytes = in.available() > 0 ? Math.max(0, readVarInt(in)) : 0L;
            if (modelId != null) {
               ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(entityId, modelId, playerName);
               synchronized (serverCachedModelsByEntityId) {
                  serverCachedModelsByEntityId.put(entityId, new PlayerModelSyncClient.ServerCachedModelEntry(entityId, playerName, modelId, sizeBytes));
               }

               ObjFbxLoader.LOGGER
                  .info("Received model sync update: entityId={} player={} modelId={} sizeBytes={}", new Object[]{entityId, playerName, modelId, sizeBytes});
            }
         } else if (messageType == 2) {
            int entityId = readVarInt(in);
            ResourcePackObjLivingOverrides.clearNetworkPlayerModelTarget(entityId);
            synchronized (serverCachedModelsByEntityId) {
               serverCachedModelsByEntityId.remove(entityId);
            }

            ObjFbxLoader.LOGGER.info("Received model sync removal for entityId={}", entityId);
         } else if (messageType == 20) {
            int entityId = readVarInt(in);
            int transferId = readVarInt(in);
            class_2960 modelId = parseModelId(readString(in, 96));
            String playerName = safePlayerName(readString(in, 32));
            int expectedSize = Math.max(0, readVarInt(in));
            if (expectedSize > 100663296) {
               ObjFbxLoader.LOGGER
                  .warn("Ignoring oversized incoming bundle for entityId={} player={} ({} bytes).", new Object[]{entityId, playerName, expectedSize});
            } else {
               if (modelId != null) {
                  ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(entityId, modelId, playerName);
                  synchronized (serverCachedModelsByEntityId) {
                     PlayerModelSyncClient.ServerCachedModelEntry previous = serverCachedModelsByEntityId.get(entityId);
                     long size = previous == null ? expectedSize : Math.max(previous.sizeBytes(), (long)expectedSize);
                     serverCachedModelsByEntityId.put(entityId, new PlayerModelSyncClient.ServerCachedModelEntry(entityId, playerName, modelId, size));
                  }
               }

               synchronized (incomingBundlesByEntityId) {
                  incomingBundlesByEntityId.put(
                     entityId, new PlayerModelSyncClient.IncomingBundleState(entityId, transferId, modelId, playerName, expectedSize)
                  );
               }
            }
         } else if (messageType == 21) {
            int entityId = readVarInt(in);
            int transferId = readVarInt(in);
            byte[] chunk = readByteArray(in, 32768);
            synchronized (incomingBundlesByEntityId) {
               PlayerModelSyncClient.IncomingBundleState state = incomingBundlesByEntityId.get(entityId);
               if (state != null && state.transferId() == transferId && chunk.length != 0) {
                  if (!state.append(chunk)) {
                     incomingBundlesByEntityId.remove(entityId);
                     ObjFbxLoader.LOGGER.warn("Dropped incoming bundle for entityId={} because it exceeded size limits.", entityId);
                  }
               }
            }
         } else if (messageType == 22) {
            int entityId = readVarInt(in);
            int transferId = readVarInt(in);
            String playerName = safePlayerName(readString(in, 32));
            PlayerModelSyncClient.IncomingBundleState state;
            synchronized (incomingBundlesByEntityId) {
               state = incomingBundlesByEntityId.get(entityId);
               if (state == null || state.transferId() != transferId) {
                  return;
               }

               incomingBundlesByEntityId.remove(entityId);
            }

            byte[] bundleBytes = state.bytes();
            if (bundleBytes.length != 0) {
               boolean installed = ResourcePackObjLivingOverrides.installRemotePlayerModelBundle(playerName, bundleBytes);
               if (installed) {
                  if (state.modelId() != null) {
                     ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(entityId, state.modelId(), playerName);
                  }

                  ObjFbxLoader.LOGGER
                     .info("Installed synced remote model bundle for entityId={} player={} ({} bytes).", new Object[]{entityId, playerName, bundleBytes.length});
               } else {
                  ObjFbxLoader.LOGGER.warn("Failed to install synced remote model bundle for entityId={} player={}.", entityId, playerName);
               }
            }
         } else if (messageType == 3) {
            int count = readVarInt(in);
            ResourcePackObjLivingOverrides.clearAllNetworkPlayerModelTargets();
            synchronized (serverCachedModelsByEntityId) {
               serverCachedModelsByEntityId.clear();
            }

            synchronized (incomingBundlesByEntityId) {
               incomingBundlesByEntityId.clear();
            }

            for (int i = 0; i < count; i++) {
               int entityId = readVarInt(in);
               class_2960 modelId = parseModelId(readString(in, 96));
               String playerName = in.available() > 0 ? safePlayerName(readString(in, 32)) : "unknown";
               long sizeBytes = in.available() > 0 ? Math.max(0, readVarInt(in)) : 0L;
               if (modelId != null) {
                  ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(entityId, modelId, playerName);
                  synchronized (serverCachedModelsByEntityId) {
                     serverCachedModelsByEntityId.put(entityId, new PlayerModelSyncClient.ServerCachedModelEntry(entityId, playerName, modelId, sizeBytes));
                  }
               }
            }

            applyLocalPlayerSelection(client);
            ObjFbxLoader.LOGGER.info("Received full player model sync from server with {} entries.", count);
         } else {
            if (messageType == 31) {
               int entityId = readVarInt(in);
               int quantized = readVarInt(in);
               float level = Math.max(0.0F, Math.min(1.0F, quantized / 255.0F));
               ResourcePackObjLivingOverrides.setNetworkPlayerLipSyncLevel(entityId, level);
            }
         }
      }
   }

   private static void publishLipSyncState() {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1562() != null) {
         if (ClientPlayNetworking.canSend(PlayerModelSyncClient.RawChannelPayload.ID)) {
            long now = System.currentTimeMillis();
            if (now - lastLipSyncPublishAt >= 50L) {
               lastLipSyncPublishAt = now;
               int quantized = Math.round(Math.max(0.0F, Math.min(1.0F, VoiceLipSyncState.currentLipSyncLevel())) * 255.0F);
               if (quantized != lastPublishedLipSyncQuantized) {
                  byte[] payload = buildClientLipSyncPacket(quantized);
                  if (payload.length > 0) {
                     ClientPlayNetworking.send(new PlayerModelSyncClient.RawChannelPayload(payload));
                     lastPublishedLipSyncQuantized = quantized;
                  }
               }
            }
         }
      }
   }

   private static void sendBundleUpload(class_2960 modelId, byte[] bundleBytes) {
      if (modelId != null && bundleBytes != null && bundleBytes.length != 0) {
         if (bundleBytes.length > 100663296) {
            ObjFbxLoader.LOGGER.warn("Skipping upload of selected model {} because bundle is too large ({} bytes).", modelId, bundleBytes.length);
         } else {
            int transferId = nextTransferId();
            byte[] start = buildUploadStartPacket(modelId, transferId, bundleBytes.length);
            if (start.length != 0) {
               ClientPlayNetworking.send(new PlayerModelSyncClient.RawChannelPayload(start));

               for (int offset = 0; offset < bundleBytes.length; offset += 28672) {
                  int len = Math.min(28672, bundleBytes.length - offset);
                  byte[] chunk = buildUploadChunkPacket(transferId, bundleBytes, offset, len);
                  if (chunk.length == 0) {
                     return;
                  }

                  ClientPlayNetworking.send(new PlayerModelSyncClient.RawChannelPayload(chunk));
               }

               byte[] finish = buildUploadFinishPacket(transferId);
               if (finish.length > 0) {
                  ClientPlayNetworking.send(new PlayerModelSyncClient.RawChannelPayload(finish));
                  ObjFbxLoader.LOGGER
                     .info("Uploaded player model bundle to server: modelId={}, transferId={}, bytes={}", new Object[]{modelId, transferId, bundleBytes.length});
               }
            }
         }
      }
   }

   private static byte[] buildSelectedModelBundle(class_310 client, class_2960 modelId, boolean includeEmbeddedTextureExtraction) {
      if (client != null && modelId != null && client.method_1478() != null) {
         try {
            class_3300 resourceManager = client.method_1478();
            PlayerModelSyncClient.BundleResourceCollection collection = collectBundleResources(resourceManager, modelId, includeEmbeddedTextureExtraction);
            Map<class_2960, byte[]> bundleResources = collection.resources();
            if (bundleResources.isEmpty()) {
               return new byte[0];
            } else {
               byte[] zipped = zipBundleResources(bundleResources);
               if (!collection.temporaryMirroredTextureIds().isEmpty()) {
                  for (class_2960 temporary : collection.temporaryMirroredTextureIds()) {
                     bundleResources.remove(temporary);
                  }
               }

               if (zipped.length > 100663296) {
                  ObjFbxLoader.LOGGER.warn("Built model bundle for {} exceeded max size: {} bytes.", modelId, zipped.length);
                  return new byte[0];
               } else {
                  return zipped;
               }
            }
         } catch (Exception var9) {
            ObjFbxLoader.LOGGER.warn("Failed to build upload bundle for selected model {}.", modelId, var9);
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   private static PlayerModelSyncClient.BundleResourceCollection collectBundleResources(
      class_3300 resourceManager, class_2960 modelId, boolean includeEmbeddedTextureExtraction
   ) {
      LinkedHashMap<class_2960, byte[]> out = new LinkedHashMap<>();
      String selectedNamespace = modelId.method_12836();
      String targetPath = modelId.method_12832();
      Set<class_2960> temporaryMirroredTextureIds = new LinkedHashSet<>();
      List<String> modelPrefixes = new ArrayList<>();
      modelPrefixes.add("models/players/" + targetPath + "/");
      modelPrefixes.add("models/player/" + targetPath + "/");
      modelPrefixes.add("models/entities/" + targetPath + "/");
      modelPrefixes.add("models/entity/" + targetPath + "/");
      Map<class_2960, class_3298> modelResources = findResourcesByPrefixes(resourceManager, "models", null, modelPrefixes, MODEL_RESOURCE_EXTENSIONS);
      if (modelResources.isEmpty() && !"player".equals(targetPath)) {
         List<String> fallback = List.of("models/players/player/", "models/player/player/", "models/entities/player/", "models/entity/player/");
         modelResources = findResourcesByPrefixes(resourceManager, "models", null, fallback, MODEL_RESOURCE_EXTENSIONS);
      }

      Set<String> modelNamespaces = new LinkedHashSet<>();
      modelNamespaces.add(selectedNamespace);
      Set<class_2960> pendingMtl = new LinkedHashSet<>();
      Set<String> pendingTextures = new LinkedHashSet<>();
      Map<String, class_2960> resolvedTextureByRef = new LinkedHashMap<>();

      for (Entry<class_2960, class_3298> entry : modelResources.entrySet()) {
         class_2960 id = entry.getKey();
         byte[] bytes = readResourceBytes(entry.getValue());
         if (bytes.length != 0) {
            out.put(id, bytes);
            modelNamespaces.add(id.method_12836());
            String lowerPath = id.method_12832().toLowerCase(Locale.ROOT);
            if (lowerPath.endsWith(".obj")) {
               pendingMtl.addAll(parseObjMaterialResourceIds(id, bytes));
            }

            if (lowerPath.endsWith(".mtl")) {
               pendingTextures.addAll(parseMtlTextureRefs(bytes));
            }
         }
      }

      for (class_2960 mtlId : pendingMtl) {
         if (mtlId != null && !out.containsKey(mtlId)) {
            class_3298 resource = (class_3298)resourceManager.method_14486(mtlId).orElse(null);
            if (resource != null) {
               byte[] bytes = readResourceBytes(resource);
               if (bytes.length != 0) {
                  out.put(mtlId, bytes);
                  modelNamespaces.add(mtlId.method_12836());
                  pendingTextures.addAll(parseMtlTextureRefs(bytes));
               }
            }
         }
      }

      List<String> texturePrefixes = new ArrayList<>();
      texturePrefixes.add("textures/entity/" + targetPath + "/");
      texturePrefixes.add("textures/obj/" + targetPath + "/");
      Map<class_2960, class_3298> groupedTextures = findResourcesByPrefixes(resourceManager, "textures", null, texturePrefixes, TEXTURE_RESOURCE_EXTENSIONS);

      for (Entry<class_2960, class_3298> entryx : groupedTextures.entrySet()) {
         out.putIfAbsent(entryx.getKey(), readResourceBytes(entryx.getValue()));
         modelNamespaces.add(entryx.getKey().method_12836());
      }

      for (String namespace : modelNamespaces) {
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/entity/" + targetPath + ".png"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/entity/" + targetPath + ".jpg"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/entity/" + targetPath + ".jpeg"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/entity/" + targetPath + ".tga"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/entity/" + targetPath + ".webp"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/obj/" + targetPath + ".png"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/obj/" + targetPath + ".jpg"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/obj/" + targetPath + ".jpeg"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/obj/" + targetPath + ".tga"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "textures/obj/" + targetPath + ".webp"));
      }

      for (String textureRef : pendingTextures) {
         class_2960 textureId = resolveTextureResourceId(resourceManager, modelNamespaces, textureRef);
         if (textureId != null) {
            resolvedTextureByRef.putIfAbsent(textureRef, textureId);
            if (!out.containsKey(textureId)) {
               class_3298 texture = (class_3298)resourceManager.method_14486(textureId).orElse(null);
               if (texture != null) {
                  out.put(textureId, readResourceBytes(texture));
               }
            }
         }
      }

      Set<class_2960> resolvedLocalTextures = ResourcePackObjLivingOverrides.getResolvedIdleTextureIdsForModel(modelId);
      class_310 client = class_310.method_1551();

      for (class_2960 textureId : resolvedLocalTextures) {
         if (textureId != null && !out.containsKey(textureId) && !isGeneratedMaterialTextureId(textureId)) {
            byte[] bytes = readTextureBytes(resourceManager, client, textureId, includeEmbeddedTextureExtraction);
            if (bytes.length != 0) {
               out.put(textureId, bytes);
               modelNamespaces.add(textureId.method_12836());
            }
         }
      }

      if (includeEmbeddedTextureExtraction) {
         temporaryMirroredTextureIds.addAll(
            mirrorModelTexturesForBundle(resourceManager, client, out, selectedNamespace, targetPath, resolvedTextureByRef, resolvedLocalTextures, true)
         );
      }

      int textureCount = countTextureEntries(out);
      if (textureCount == 0) {
         Set<String> textureHints = collectTextureHints(targetPath, modelResources.keySet(), pendingTextures);
         addHintedTextures(resourceManager, out, modelNamespaces, textureHints);
         textureCount = countTextureEntries(out);
      }

      if (textureCount == 0) {
         addBroadTextureFallback(resourceManager, out, modelNamespaces);
      }

      for (String namespace : modelNamespaces) {
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "configs/entity/" + targetPath + ".json"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "configs/" + targetPath + ".json"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "configs/entity/player.json"));
         addIfPresent(resourceManager, out, class_2960.method_43902(namespace, "configs/player.json"));
      }

      return new PlayerModelSyncClient.BundleResourceCollection(out, temporaryMirroredTextureIds);
   }

   private static void addHintedTextures(class_3300 resourceManager, Map<class_2960, byte[]> out, Set<String> modelNamespaces, Set<String> hints) {
      if (!modelNamespaces.isEmpty() && !hints.isEmpty()) {
         Map<class_2960, class_3298> textures = resourceManager.method_14488("textures", id -> {
            if (!modelNamespaces.contains(id.method_12836())) {
               return false;
            } else {
               String path = id.method_12832().toLowerCase(Locale.ROOT);
               if (!hasAnyExtension(path, TEXTURE_RESOURCE_EXTENSIONS)) {
                  return false;
               } else {
                  for (String hint : hints) {
                     if (!hint.isBlank() && path.contains(hint)) {
                        return true;
                     }
                  }

                  return false;
               }
            }
         });

         for (Entry<class_2960, class_3298> entry : textures.entrySet()) {
            out.putIfAbsent(entry.getKey(), readResourceBytes(entry.getValue()));
         }
      }
   }

   private static void addBroadTextureFallback(class_3300 resourceManager, Map<class_2960, byte[]> out, Set<String> modelNamespaces) {
      if (!modelNamespaces.isEmpty()) {
         Map<class_2960, class_3298> textures = resourceManager.method_14488(
            "textures",
            id -> {
               if (!modelNamespaces.contains(id.method_12836())) {
                  return false;
               } else {
                  String path = id.method_12832().toLowerCase(Locale.ROOT);
                  return !hasAnyExtension(path, TEXTURE_RESOURCE_EXTENSIONS)
                     ? false
                     : path.startsWith("textures/entity/") || path.startsWith("textures/obj/") || path.startsWith("textures/models/");
               }
            }
         );

         for (Entry<class_2960, class_3298> entry : textures.entrySet()) {
            out.putIfAbsent(entry.getKey(), readResourceBytes(entry.getValue()));
         }
      }
   }

   private static Set<String> collectTextureHints(String targetPath, Set<class_2960> modelResourceIds, Set<String> pendingTextureRefs) {
      Set<String> hints = new LinkedHashSet<>();
      if (targetPath != null && !targetPath.isBlank()) {
         String normalized = targetPath.toLowerCase(Locale.ROOT).replace('\\', '/');
         hints.add(normalized);
         int slash = normalized.lastIndexOf(47);
         if (slash >= 0 && slash + 1 < normalized.length()) {
            hints.add(normalized.substring(slash + 1));
         }
      }

      if (modelResourceIds != null) {
         for (class_2960 id : modelResourceIds) {
            if (id != null) {
               String path = id.method_12832().toLowerCase(Locale.ROOT);
               int slash = path.lastIndexOf(47);
               String filename = slash >= 0 ? path.substring(slash + 1) : path;
               int dot = filename.lastIndexOf(46);
               if (dot > 0) {
                  filename = filename.substring(0, dot);
               }

               if (!filename.isBlank()) {
                  hints.add(filename);
               }
            }
         }
      }

      if (pendingTextureRefs != null) {
         for (String ref : pendingTextureRefs) {
            if (ref != null && !ref.isBlank()) {
               String normalized = ref.toLowerCase(Locale.ROOT).replace('\\', '/');
               hints.add(normalized);
               int slashx = normalized.lastIndexOf(47);
               String basename = slashx >= 0 ? normalized.substring(slashx + 1) : normalized;
               int dotx = basename.lastIndexOf(46);
               if (dotx > 0) {
                  basename = basename.substring(0, dotx);
               }

               if (!basename.isBlank()) {
                  hints.add(basename);
               }
            }
         }
      }

      hints.removeIf(String::isBlank);
      return hints;
   }

   private static int countTextureEntries(Map<class_2960, byte[]> resources) {
      int count = 0;

      for (class_2960 id : resources.keySet()) {
         if (id != null && isTextureResourcePath(id.method_12832())) {
            count++;
         }
      }

      return count;
   }

   private static Set<class_2960> mirrorModelTexturesForBundle(
      class_3300 resourceManager,
      class_310 client,
      Map<class_2960, byte[]> out,
      String namespace,
      String targetPath,
      Map<String, class_2960> resolvedTextureByRef,
      Set<class_2960> resolvedLocalTextures,
      boolean includeEmbeddedTextureExtraction
   ) {
      Set<class_2960> mirroredTextureIds = new LinkedHashSet<>();
      if (resourceManager != null && out != null && namespace != null && !namespace.isBlank() && targetPath != null && !targetPath.isBlank()) {
         Set<String> usedNames = new LinkedHashSet<>();
         Set<class_2960> mirroredSources = new LinkedHashSet<>();
         int sequence = 1;
         if (resolvedTextureByRef != null) {
            for (Entry<String, class_2960> entry : resolvedTextureByRef.entrySet()) {
               class_2960 sourceTextureId = entry.getValue();
               if (sourceTextureId != null) {
                  sequence = addMirroredModelTexture(
                     resourceManager,
                     client,
                     out,
                     namespace,
                     targetPath,
                     extractTextureBasename(entry.getKey()),
                     sourceTextureId,
                     usedNames,
                     sequence,
                     includeEmbeddedTextureExtraction,
                     mirroredTextureIds
                  );
                  mirroredSources.add(sourceTextureId);
               }
            }
         }

         if (resolvedLocalTextures != null) {
            for (class_2960 sourceTextureId : resolvedLocalTextures) {
               if (sourceTextureId != null && !mirroredSources.contains(sourceTextureId)) {
                  sequence = addMirroredModelTexture(
                     resourceManager,
                     client,
                     out,
                     namespace,
                     targetPath,
                     extractTextureBasename(sourceTextureId.method_12832()),
                     sourceTextureId,
                     usedNames,
                     sequence,
                     includeEmbeddedTextureExtraction,
                     mirroredTextureIds
                  );
               }
            }
         }

         return mirroredTextureIds;
      } else {
         return mirroredTextureIds;
      }
   }

   private static int addMirroredModelTexture(
      class_3300 resourceManager,
      class_310 client,
      Map<class_2960, byte[]> out,
      String namespace,
      String targetPath,
      String preferredFileName,
      class_2960 sourceTextureId,
      Set<String> usedNames,
      int sequence,
      boolean includeEmbeddedTextureExtraction,
      Set<class_2960> mirroredTextureIds
   ) {
      if (sourceTextureId == null) {
         return sequence;
      } else {
         byte[] bytes = out.get(sourceTextureId);
         if (bytes == null || bytes.length == 0) {
            bytes = readTextureBytes(resourceManager, client, sourceTextureId, includeEmbeddedTextureExtraction);
         }

         if (bytes == null || bytes.length == 0) {
            return sequence;
         } else if (isTextureInModelFolder(sourceTextureId, targetPath)) {
            return sequence;
         } else {
            String chosen = buildMirroredTextureFileName(preferredFileName, sourceTextureId, sequence);
            if (chosen == null || chosen.isBlank()) {
               chosen = "texture_" + sequence + ".png";
            }

            String unique = uniquifyTextureFileName(chosen, usedNames);
            String normalizedTargetPath = targetPath.replace('\\', '/').toLowerCase(Locale.ROOT);
            class_2960 mirrorId = class_2960.method_43902(namespace, "textures/entity/" + normalizedTargetPath + "/" + unique);
            if (mirrorId != null) {
               out.putIfAbsent(mirrorId, bytes);
               usedNames.add(unique.toLowerCase(Locale.ROOT));
               if (mirroredTextureIds != null) {
                  mirroredTextureIds.add(mirrorId);
               }
            }

            return sequence + 1;
         }
      }
   }

   private static boolean isTextureInModelFolder(class_2960 textureId, String targetPath) {
      if (textureId != null && targetPath != null && !targetPath.isBlank()) {
         String path = textureId.method_12832().toLowerCase(Locale.ROOT);
         String normalizedTargetPath = targetPath.replace('\\', '/').toLowerCase(Locale.ROOT);
         return path.startsWith("textures/entity/" + normalizedTargetPath + "/") || path.startsWith("textures/obj/" + normalizedTargetPath + "/");
      } else {
         return false;
      }
   }

   private static String buildMirroredTextureFileName(String preferredFileName, class_2960 sourceTextureId, int sequence) {
      String candidate = preferredFileName;
      if (preferredFileName == null || preferredFileName.isBlank()) {
         candidate = sourceTextureId == null ? "" : sourceTextureId.method_12832();
      }

      candidate = extractTextureBasename(candidate);
      String extension = extractTextureExtension(candidate);
      if (extension == null || extension.isBlank()) {
         extension = extractTextureExtension(sourceTextureId == null ? null : sourceTextureId.method_12832());
      }

      if (extension == null || extension.isBlank()) {
         extension = ".png";
      }

      String stem = candidate == null ? "" : candidate;
      int dot = stem.lastIndexOf(46);
      if (dot > 0) {
         stem = stem.substring(0, dot);
      }

      stem = sanitizeTextureStem(stem);
      if (stem.isBlank()) {
         stem = "texture_" + sequence;
      }

      return stem + extension;
   }

   private static String extractTextureBasename(String ref) {
      if (ref != null && !ref.isBlank()) {
         String normalized = ref.trim().replace('\\', '/');
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

         int slash = normalized.lastIndexOf(47);
         if (slash >= 0 && slash + 1 < normalized.length()) {
            normalized = normalized.substring(slash + 1);
         }

         return normalized.trim();
      } else {
         return "";
      }
   }

   private static String extractTextureExtension(String value) {
      if (value != null && !value.isBlank()) {
         String normalized = value.trim().toLowerCase(Locale.ROOT);

         for (String ext : TEXTURE_RESOURCE_EXTENSIONS) {
            if (normalized.endsWith(ext)) {
               return ext;
            }
         }

         int dot = normalized.lastIndexOf(46);
         if (dot >= 0 && dot + 1 < normalized.length()) {
            String extx = normalized.substring(dot);
            if (extx.length() <= 5) {
               return extx;
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static String sanitizeTextureStem(String stem) {
      if (stem != null && !stem.isBlank()) {
         String sanitized = stem.toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9._-]", "_").replaceAll("_+", "_");

         while (sanitized.startsWith(".")) {
            sanitized = sanitized.substring(1);
         }

         while (sanitized.endsWith(".")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
         }

         return sanitized;
      } else {
         return "";
      }
   }

   private static String uniquifyTextureFileName(String fileName, Set<String> usedNames) {
      if (fileName != null && !fileName.isBlank()) {
         String normalized = fileName.toLowerCase(Locale.ROOT);
         if (usedNames != null && usedNames.contains(normalized)) {
            String stem = fileName;
            String extension = "";
            int dot = fileName.lastIndexOf(46);
            if (dot > 0) {
               stem = fileName.substring(0, dot);
               extension = fileName.substring(dot);
            }

            int suffix = 2;

            while (true) {
               String candidate = stem + "_" + suffix + extension;
               if (!usedNames.contains(candidate.toLowerCase(Locale.ROOT))) {
                  return candidate;
               }

               suffix++;
            }
         } else {
            return fileName;
         }
      } else {
         return "texture.png";
      }
   }

   private static boolean isTextureResourcePath(String path) {
      if (path == null) {
         return false;
      } else {
         String lower = path.toLowerCase(Locale.ROOT);
         return (lower.startsWith("textures/") || lower.startsWith("embedded/")) && hasAnyExtension(lower, TEXTURE_RESOURCE_EXTENSIONS);
      }
   }

   private static boolean isGeneratedMaterialTextureId(class_2960 textureId) {
      if (textureId == null) {
         return false;
      } else if (!"objfbxloader".equals(textureId.method_12836())) {
         return false;
      } else {
         String path = textureId.method_12832();
         return path != null && path.startsWith("material/");
      }
   }

   private static byte[] readTextureBytes(class_3300 resourceManager, class_310 client, class_2960 textureId, boolean includeEmbeddedTextureExtraction) {
      if (textureId == null) {
         return new byte[0];
      } else {
         byte[] cachedRuntime = ResourcePackMeshLoader.getRuntimeGeneratedTextureBytes(textureId);
         if (cachedRuntime.length > 0) {
            return cachedRuntime;
         } else {
            if (includeEmbeddedTextureExtraction) {
               byte[] runtimePng = exportRuntimeTextureAsPng(resourceManager, client, textureId);
               if (runtimePng.length > 0) {
                  return runtimePng;
               }
            }

            class_3298 resource = (class_3298)resourceManager.method_14486(textureId).orElse(null);
            return resource == null ? new byte[0] : readResourceBytes(resource);
         }
      }
   }

   private static byte[] exportRuntimeTextureAsPng(class_3300 resourceManager, class_310 client, class_2960 textureId) {
      if (client != null && client.method_1531() != null && textureId != null) {
         class_1044 texture = client.method_1531().method_4619(textureId);
         if (texture instanceof class_1043 imageTexture) {
            return writeNativeImageToPngBytes(textureId, imageTexture.method_4525());
         } else {
            if (texture instanceof class_10537 reloadableTexture && resourceManager != null) {
               try {
                  class_10539 contents = reloadableTexture.method_65809(resourceManager);

                  byte[] var6;
                  label73: {
                     try {
                        if (contents != null) {
                           var6 = writeNativeImageToPngBytes(textureId, contents.comp_3447());
                           break label73;
                        }
                     } catch (Throwable var9) {
                        if (contents != null) {
                           try {
                              contents.close();
                           } catch (Throwable var8) {
                              var9.addSuppressed(var8);
                           }
                        }

                        throw var9;
                     }

                     if (contents != null) {
                        contents.close();
                     }

                     return new byte[0];
                  }

                  if (contents != null) {
                     contents.close();
                  }

                  return var6;
               } catch (Exception var10) {
                  ObjFbxLoader.LOGGER.debug("Could not export reloadable texture {} from local resource manager.", textureId, var10);
               }
            }

            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   private static byte[] writeNativeImageToPngBytes(class_2960 textureId, class_1011 image) {
      if (image == null) {
         return new byte[0];
      } else {
         Path temp = null;

         byte[] var4;
         try {
            temp = Files.createTempFile("objfbxloader-runtime-texture-", ".png");
            image.method_4314(temp);
            byte[] bytes = Files.readAllBytes(temp);
            if (bytes.length > 0) {
               ObjFbxLoader.LOGGER.info("Captured runtime texture {} for model sync upload ({} bytes).", textureId, bytes.length);
            }

            return bytes;
         } catch (Exception var14) {
            ObjFbxLoader.LOGGER.warn("Failed to export runtime texture {} for model sync upload.", textureId, var14);
            var4 = new byte[0];
         } finally {
            if (temp != null) {
               try {
                  Files.deleteIfExists(temp);
               } catch (IOException var13) {
               }
            }
         }

         return var4;
      }
   }

   private static void mirrorLoadedModelTexturesToGeneratedPack(class_310 client, class_3300 resourceManager) {
      long startedAt = System.currentTimeMillis();

      try {
         recreateGeneratedTexturePackRoot();
         writeGeneratedPackMeta();
         List<Path> outputAssetRoots = collectWritableTextureAssetRoots(client);
         if (outputAssetRoots.isEmpty()) {
            outputAssetRoots = List.of(AUTO_TEXTURE_EXPORT_ASSETS_ROOT);
         }

         for (Path root : outputAssetRoots) {
            Files.createDirectories(root);
         }

         Set<class_2960> modelIds = new LinkedHashSet<>(ResourcePackObjLivingOverrides.getLoadedEntityModelIds());
         if (modelIds.isEmpty()) {
            modelIds.addAll(discoverModelIdsForAutoTextureExtraction(resourceManager));
         }

         if (modelIds.isEmpty()) {
            modelIds.add(selectedPlayerModel);
            modelIds.add(class_2960.method_60656("player"));
         }

         int writtenTextures = 0;
         int writtenModels = 0;

         for (class_2960 modelId : modelIds) {
            if (modelId != null) {
               Set<class_2960> resolvedTextures = new LinkedHashSet<>(ResourcePackObjLivingOverrides.getResolvedTextureIdsForModel(modelId));
               PlayerModelSyncClient.BundleResourceCollection collection = collectBundleResources(resourceManager, modelId, true);

               for (class_2960 resourceId : collection.resources().keySet()) {
                  if (resourceId != null && isTextureResourcePath(resourceId.method_12832())) {
                     resolvedTextures.add(resourceId);
                  }
               }

               if (!resolvedTextures.isEmpty()) {
                  Set<String> usedNames = new LinkedHashSet<>();
                  int sequence = 1;
                  int modelWritten = 0;

                  for (class_2960 sourceTextureId : resolvedTextures) {
                     if (sourceTextureId != null && !isGeneratedMaterialTextureId(sourceTextureId)) {
                        byte[] textureBytes = readTextureBytes(resourceManager, client, sourceTextureId, true);
                        if (textureBytes.length != 0) {
                           writeTextureByIdentifier(outputAssetRoots, sourceTextureId, textureBytes);
                           String preferredName = sourceTextureId.method_12832();
                           String fileName = buildMirroredTextureFileName(preferredName, sourceTextureId, sequence);
                           if (isPngBytes(textureBytes)) {
                              int dot = fileName.lastIndexOf(46);
                              String stem = dot > 0 ? fileName.substring(0, dot) : fileName;
                              fileName = stem + ".png";
                           }

                           fileName = uniquifyTextureFileName(fileName, usedNames);
                           usedNames.add(fileName.toLowerCase(Locale.ROOT));
                           sequence++;
                           writeMirroredModelTexture(outputAssetRoots, modelId, fileName, textureBytes);
                           modelWritten++;
                        }
                     }
                  }

                  if (modelWritten > 0) {
                     writtenModels++;
                     writtenTextures += modelWritten;
                  }
               }
            }
         }

         long elapsed = System.currentTimeMillis() - startedAt;
         lastAutoTextureMirrorAt = System.currentTimeMillis();
         lastAutoTextureMirroredModelCount = writtenModels;
         ObjFbxLoader.LOGGER
            .info(
               "Auto-generated {} mirrored model texture file(s) for {} model(s) in {} ms at {}",
               new Object[]{writtenTextures, writtenModels, elapsed, AUTO_TEXTURE_EXPORT_PACK_ROOT}
            );
      } catch (Exception var22) {
         lastAutoTextureMirroredModelCount = 0;
         lastAutoTextureMirrorAt = System.currentTimeMillis();
         ObjFbxLoader.LOGGER.warn("Failed to auto-generate mirrored textures for loaded resource packs.", var22);
      }
   }

   private static void writeTextureByIdentifier(List<Path> outputAssetRoots, class_2960 textureId, byte[] textureBytes) throws IOException {
      if (textureId != null && textureBytes != null && textureBytes.length != 0) {
         if (outputAssetRoots == null || outputAssetRoots.isEmpty()) {
            outputAssetRoots = List.of(AUTO_TEXTURE_EXPORT_ASSETS_ROOT);
         }

         for (Path root : outputAssetRoots) {
            Path exactPath = root.resolve(textureId.method_12836()).resolve(textureId.method_12832().replace('\\', '/')).normalize();
            Files.createDirectories(exactPath.getParent());
            Files.write(exactPath, textureBytes);
         }
      }
   }

   private static void writeMirroredModelTexture(List<Path> outputAssetRoots, class_2960 modelId, String fileName, byte[] textureBytes) throws IOException {
      if (modelId != null && fileName != null && !fileName.isBlank() && textureBytes != null && textureBytes.length != 0) {
         if (outputAssetRoots == null || outputAssetRoots.isEmpty()) {
            outputAssetRoots = List.of(AUTO_TEXTURE_EXPORT_ASSETS_ROOT);
         }

         String normalizedModelPath = modelId.method_12832().replace('\\', '/');

         for (Path root : outputAssetRoots) {
            Path modelTextureDir = root.resolve(modelId.method_12836()).resolve("textures").resolve("entity").resolve(normalizedModelPath).normalize();
            Files.createDirectories(modelTextureDir);
            Path destination = modelTextureDir.resolve(fileName);
            Files.write(destination, textureBytes);
         }
      }
   }

   private static Set<class_2960> discoverModelIdsForAutoTextureExtraction(class_3300 resourceManager) {
      Set<class_2960> discovered = new LinkedHashSet<>();
      if (resourceManager == null) {
         return discovered;
      } else {
         Map<class_2960, class_3298> models = resourceManager.method_14488("models", id -> {
            if (id == null) {
               return false;
            } else {
               String path = id.method_12832().toLowerCase(Locale.ROOT);
               boolean rootMatch = false;

               for (String root : MODEL_TARGET_ROOTS) {
                  if (path.startsWith(root)) {
                     rootMatch = true;
                     break;
                  }
               }

               return rootMatch && hasAnyExtension(path, MODEL_RESOURCE_EXTENSIONS);
            }
         });

         for (class_2960 modelResourceId : models.keySet()) {
            if (modelResourceId != null) {
               String lowerPath = modelResourceId.method_12832().toLowerCase(Locale.ROOT);
               String matchedRoot = null;

               for (String root : MODEL_TARGET_ROOTS) {
                  if (lowerPath.startsWith(root)) {
                     matchedRoot = root;
                     break;
                  }
               }

               if (matchedRoot != null) {
                  String remainder = lowerPath.substring(matchedRoot.length());
                  int slash = remainder.indexOf(47);
                  if (slash > 0) {
                     String targetPath = remainder.substring(0, slash);
                     class_2960 targetId = class_2960.method_43902(modelResourceId.method_12836(), targetPath);
                     if (targetId != null) {
                        discovered.add(targetId);
                     }
                  }
               }
            }
         }

         return discovered;
      }
   }

   private static List<Path> collectWritableTextureAssetRoots(class_310 client) {
      LinkedHashSet<Path> roots = new LinkedHashSet<>();
      roots.add(AUTO_TEXTURE_EXPORT_ASSETS_ROOT);
      if (client == null) {
         return new ArrayList<>(roots);
      } else {
         Path resourcepacksDir = PlatformPaths.gameDir().resolve("resourcepacks");

         for (String id : readSelectedPackIdsFromRepository(client)) {
            if (id != null && id.startsWith("file/")) {
               String packName = id.substring("file/".length());
               if (!packName.isBlank()) {
                  Path packDir = resourcepacksDir.resolve(packName);
                  if (Files.isDirectory(packDir)) {
                     Path assetsDir = packDir.resolve("assets");
                     roots.add(assetsDir);
                  }
               }
            }
         }

         return new ArrayList<>(roots);
      }
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

            if (invokeNoArgs(repository, "getSelectedIds") instanceof Iterable<?> iterable) {
               List<String> out = new ArrayList<>();

               for (Object entry : iterable) {
                  if (entry != null) {
                     out.add(String.valueOf(entry));
                  }
               }

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

   private static void recreateGeneratedTexturePackRoot() throws IOException {
      deleteGeneratedTexturePackRoot();
      Files.createDirectories(AUTO_TEXTURE_EXPORT_ASSETS_ROOT);
   }

   private static void deleteGeneratedTexturePackRoot() throws IOException {
      if (Files.exists(AUTO_TEXTURE_EXPORT_PACK_ROOT)) {
         try (Stream<Path> walk = Files.walk(AUTO_TEXTURE_EXPORT_PACK_ROOT)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
               try {
                  Files.deleteIfExists(path);
               } catch (IOException var2) {
               }
            });
         }
      }
   }

   private static void writeGeneratedPackMeta() throws IOException {
      Path meta = AUTO_TEXTURE_EXPORT_PACK_ROOT.resolve("pack.mcmeta");
      String json = "{\n  \"pack\": {\n    \"pack_format\": 64,\n    \"description\": \"OBJ/FBX Loader auto-generated mirrored textures\"\n  }\n}\n";
      Files.writeString(meta, json, StandardCharsets.UTF_8);
   }

   private static boolean isPngBytes(byte[] bytes) {
      return bytes != null && bytes.length >= 8
         ? (bytes[0] & 255) == 137
            && (bytes[1] & 255) == 80
            && (bytes[2] & 255) == 78
            && (bytes[3] & 255) == 71
            && (bytes[4] & 255) == 13
            && (bytes[5] & 255) == 10
            && (bytes[6] & 255) == 26
            && (bytes[7] & 255) == 10
         : false;
   }

   private static boolean hasAnyExtension(String path, Set<String> extensions) {
      if (path != null && extensions != null) {
         for (String extension : extensions) {
            if (path.endsWith(extension)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static Map<class_2960, class_3298> findResourcesByPrefixes(
      class_3300 resourceManager, String basePath, String namespace, List<String> prefixes, Set<String> suffixes
   ) {
      return resourceManager.method_14488(basePath, id -> {
         if (namespace != null && !namespace.equals(id.method_12836())) {
            return false;
         } else {
            String path = id.method_12832().toLowerCase(Locale.ROOT);
            boolean prefixMatch = false;

            for (String prefix : prefixes) {
               if (path.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                  prefixMatch = true;
                  break;
               }
            }

            if (!prefixMatch) {
               return false;
            } else {
               for (String suffix : suffixes) {
                  if (path.endsWith(suffix)) {
                     return true;
                  }
               }

               return false;
            }
         }
      });
   }

   private static Set<class_2960> parseObjMaterialResourceIds(class_2960 modelId, byte[] objBytes) {
      Set<class_2960> out = new LinkedHashSet<>();
      String text = new String(objBytes, StandardCharsets.UTF_8);
      String baseDir = "";
      String modelPath = modelId.method_12832().replace('\\', '/');
      int slash = modelPath.lastIndexOf(47);
      if (slash >= 0) {
         baseDir = modelPath.substring(0, slash + 1);
      }

      for (String line : text.split("\\R")) {
         String trimmed = line.trim();
         if (trimmed.startsWith("mtllib ")) {
            String ref = trimmed.substring("mtllib ".length()).trim().replace('\\', '/');
            if (!ref.isEmpty()) {
               class_2960 parsed = null;
               if (ref.contains(":")) {
                  parsed = class_2960.method_12829(ref);
               }

               if (parsed == null) {
                  parsed = class_2960.method_43902(modelId.method_12836(), baseDir + ref);
               }

               if (parsed != null) {
                  out.add(parsed);
               }
            }
         }
      }

      return out;
   }

   private static Set<String> parseMtlTextureRefs(byte[] mtlBytes) {
      Set<String> out = new LinkedHashSet<>();
      String text = new String(mtlBytes, StandardCharsets.UTF_8);

      for (String line : text.split("\\R")) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            int firstSpace = trimmed.indexOf(32);
            if (firstSpace > 0 && firstSpace < trimmed.length() - 1) {
               String key = trimmed.substring(0, firstSpace).toLowerCase(Locale.ROOT);
               if (MTL_TEXTURE_KEYS.contains(key)) {
                  String candidate = extractMtlTextureRef(trimmed.substring(firstSpace + 1).trim());
                  if (!candidate.isEmpty()) {
                     out.add(candidate);
                  }
               }
            }
         }
      }

      return out;
   }

   private static String extractMtlTextureRef(String remainder) {
      if (remainder != null && !remainder.isBlank()) {
         String value = remainder.trim();
         int quotedStart = value.indexOf(34);
         if (quotedStart >= 0) {
            int quotedEnd = value.indexOf(34, quotedStart + 1);
            if (quotedEnd > quotedStart + 1) {
               return value.substring(quotedStart + 1, quotedEnd).trim();
            }
         }

         String[] tokens = value.split("\\s+");

         for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i].trim();
            if (!token.isEmpty() && !token.startsWith("-")) {
               String lower = token.toLowerCase(Locale.ROOT);
               if (hasTextureExtension(lower) || token.contains("/") || token.contains("\\") || token.contains(":") || token.contains(".")) {
                  return token;
               }
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static class_2960 resolveTextureResourceId(class_3300 resourceManager, Set<String> candidateNamespaces, String textureRef) {
      if (textureRef != null && !textureRef.isBlank()) {
         String normalized = textureRef.trim().replace('\\', '/');
         List<class_2960> candidates = new ArrayList<>();
         int colon = normalized.indexOf(58);
         if (colon > 0 && isLikelyNamespacedTextureRef(normalized, colon)) {
            class_2960 parsed = class_2960.method_12829(normalized);
            if (parsed != null) {
               if (parsed.method_12832().startsWith("textures/")) {
                  candidates.add(parsed);
               } else {
                  candidates.add(class_2960.method_43902(parsed.method_12836(), "textures/entity/" + parsed.method_12832()));
                  candidates.add(class_2960.method_43902(parsed.method_12836(), "textures/" + parsed.method_12832()));
               }
            }
         } else {
            Set<String> namespaces = new LinkedHashSet<>();
            namespaces.add("minecraft");
            if (candidateNamespaces != null) {
               namespaces.addAll(candidateNamespaces);
            }

            for (String namespace : namespaces) {
               if (normalized.startsWith("textures/")) {
                  candidates.add(class_2960.method_43902(namespace, normalized));
               } else {
                  candidates.add(class_2960.method_43902(namespace, "textures/entity/" + normalized));
                  candidates.add(class_2960.method_43902(namespace, "textures/" + normalized));
               }

               if (!hasTextureExtension(normalized)) {
                  if (normalized.startsWith("textures/")) {
                     candidates.add(class_2960.method_43902(namespace, normalized + ".png"));
                  } else {
                     candidates.add(class_2960.method_43902(namespace, "textures/entity/" + normalized + ".png"));
                     candidates.add(class_2960.method_43902(namespace, "textures/" + normalized + ".png"));
                  }
               }
            }
         }

         for (class_2960 candidate : candidates) {
            if (candidate != null && resourceManager.method_14486(candidate).isPresent()) {
               return candidate;
            }
         }

         return null;
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

   private static boolean hasTextureExtension(String path) {
      String lower = path.toLowerCase(Locale.ROOT);
      return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".tga") || lower.endsWith(".webp");
   }

   private static void addIfPresent(class_3300 resourceManager, Map<class_2960, byte[]> out, class_2960 id) {
      if (id != null && !out.containsKey(id)) {
         class_3298 resource = (class_3298)resourceManager.method_14486(id).orElse(null);
         if (resource != null) {
            byte[] bytes = readResourceBytes(resource);
            if (bytes.length > 0) {
               out.put(id, bytes);
            }
         }
      }
   }

   private static byte[] readResourceBytes(class_3298 resource) {
      if (resource == null) {
         return new byte[0];
      } else {
         try {
            byte[] var2;
            try (InputStream in = resource.method_14482()) {
               var2 = in.readAllBytes();
            }

            return var2;
         } catch (IOException var6) {
            return new byte[0];
         }
      }
   }

   private static byte[] zipBundleResources(Map<class_2960, byte[]> resources) {
      if (resources.isEmpty()) {
         return new byte[0];
      } else {
         try {
            ByteArrayOutputStream raw = new ByteArrayOutputStream(65536);

            try (ZipOutputStream zip = new ZipOutputStream(raw, StandardCharsets.UTF_8)) {
               List<Entry<class_2960, byte[]>> sorted = new ArrayList<>(resources.entrySet());
               sorted.sort(Entry.comparingByKey((a, b) -> a.toString().compareToIgnoreCase(b.toString())));

               for (Entry<class_2960, byte[]> entry : sorted) {
                  class_2960 id = entry.getKey();
                  byte[] bytes = entry.getValue();
                  if (id != null && bytes != null && bytes.length != 0) {
                     String entryName = "assets/" + id.method_12836() + "/" + id.method_12832();
                     ZipEntry zipEntry = new ZipEntry(entryName);
                     zip.putNextEntry(zipEntry);
                     zip.write(bytes);
                     zip.closeEntry();
                  }
               }
            }

            return raw.toByteArray();
         } catch (IOException var12) {
            return new byte[0];
         }
      }
   }

   private static int nextTransferId() {
      return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
   }

   private static void applyLocalPlayerSelection(class_310 client) {
      if (client != null && client.field_1724 != null) {
         int localId = client.field_1724.method_5628();
         String localName = safePlayerName(client.field_1724.method_5477().getString());
         ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(localId, selectedPlayerModel, localName);
         synchronized (serverCachedModelsByEntityId) {
            PlayerModelSyncClient.ServerCachedModelEntry previous = serverCachedModelsByEntityId.get(localId);
            String name = client.field_1724.method_5477().getString();
            long size = previous == null ? 0L : previous.sizeBytes();
            serverCachedModelsByEntityId.put(
               localId, new PlayerModelSyncClient.ServerCachedModelEntry(localId, safePlayerName(name), selectedPlayerModel, size)
            );
         }
      }
   }

   private static class_2960 parseModelId(String raw) {
      class_2960 parsed = normalizeModelId(raw);
      return !isAllowedModelId(parsed) ? null : parsed;
   }

   private static String safePlayerName(String raw) {
      if (raw != null && !raw.isBlank()) {
         String trimmed = raw.trim();
         return trimmed.length() > 32 ? trimmed.substring(0, 32) : trimmed;
      } else {
         return "unknown";
      }
   }

   private static class_2960 loadSelectedPlayerModel() {
      class_2960 fallback = class_2960.method_60656("player");
      if (!Files.exists(PLAYER_MODEL_CONFIG_PATH)) {
         saveSelectedPlayerModel(fallback);
         return fallback;
      } else {
         try {
            String text = Files.readString(PLAYER_MODEL_CONFIG_PATH, StandardCharsets.UTF_8).trim().toLowerCase(Locale.ROOT);
            int keyPos = text.indexOf("\"model\"");
            if (keyPos >= 0) {
               int colon = text.indexOf(58, keyPos);
               int firstQuote = colon < 0 ? -1 : text.indexOf(34, colon + 1);
               int secondQuote = firstQuote < 0 ? -1 : text.indexOf(34, firstQuote + 1);
               if (firstQuote >= 0 && secondQuote > firstQuote) {
                  class_2960 parsed = parseModelId(text.substring(firstQuote + 1, secondQuote));
                  if (parsed != null) {
                     return parsed;
                  }
               }
            }

            class_2960 parsedWhole = parseModelId(text.replace("\"", "").replace("{", "").replace("}", "").trim());
            return parsedWhole == null ? fallback : parsedWhole;
         } catch (IOException var7) {
            return fallback;
         }
      }
   }

   private static void saveSelectedPlayerModel(class_2960 modelId) {
      String safe = modelId == null ? "minecraft:player" : modelId.toString();
      String json = "{\n  \"model\": \"" + safe + "\"\n}\n";

      try {
         Path parent = PLAYER_MODEL_CONFIG_PATH.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         Files.writeString(PLAYER_MODEL_CONFIG_PATH, json, StandardCharsets.UTF_8);
      } catch (IOException var4) {
      }
   }

   private static byte[] buildSetModelClientPacket(class_2960 modelId, long sizeBytes) {
      if (modelId == null) {
         return new byte[0];
      } else {
         try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(48);
            out.write(1);
            writeString(out, modelId.toString());
            writeVarInt(out, (int)Math.min(2147483647L, Math.max(0L, sizeBytes)));
            return out.toByteArray();
         } catch (Exception var4) {
            return new byte[0];
         }
      }
   }

   private static byte[] buildUploadStartPacket(class_2960 modelId, int transferId, int sizeBytes) {
      if (modelId != null && transferId > 0 && sizeBytes > 0) {
         try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(96);
            out.write(10);
            writeVarInt(out, transferId);
            writeString(out, modelId.toString());
            writeVarInt(out, sizeBytes);
            return out.toByteArray();
         } catch (Exception var4) {
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   private static byte[] buildUploadChunkPacket(int transferId, byte[] allBytes, int offset, int len) {
      if (transferId > 0 && allBytes != null && len > 0 && offset >= 0 && offset + len <= allBytes.length) {
         try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(len + 24);
            out.write(11);
            writeVarInt(out, transferId);
            writeVarInt(out, len);
            out.write(allBytes, offset, len);
            return out.toByteArray();
         } catch (Exception var5) {
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   private static byte[] buildUploadFinishPacket(int transferId) {
      if (transferId <= 0) {
         return new byte[0];
      } else {
         try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(12);
            out.write(12);
            writeVarInt(out, transferId);
            return out.toByteArray();
         } catch (Exception var2) {
            return new byte[0];
         }
      }
   }

   private static byte[] buildClientLipSyncPacket(int quantizedLevel) {
      if (quantizedLevel < 0) {
         quantizedLevel = 0;
      }

      if (quantizedLevel > 255) {
         quantizedLevel = 255;
      }

      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream(8);
         out.write(30);
         writeVarInt(out, quantizedLevel);
         return out.toByteArray();
      } catch (Exception var2) {
         return new byte[0];
      }
   }

   private static long estimateSelectedModelSizeBytes(class_310 client, class_2960 modelId) {
      if (client != null && modelId != null && client.method_1478() != null) {
         class_3300 resourceManager = client.method_1478();
         String namespace = modelId.method_12836();
         String path = modelId.method_12832();
         long total = sumModelResourceBytes(
            resourceManager,
            namespace,
            List.of("models/players/" + path + "/", "models/player/" + path + "/", "models/entities/" + path + "/", "models/entity/" + path + "/")
         );
         if (total == 0L && !"player".equals(path)) {
            total = sumModelResourceBytes(resourceManager, namespace, List.of("models/entities/player/", "models/entity/player/"));
         }

         return total;
      } else {
         return 0L;
      }
   }

   private static long sumModelResourceBytes(class_3300 resourceManager, String namespace, List<String> prefixes) {
      long total = 0L;

      for (String prefix : prefixes) {
         String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
         Map<class_2960, class_3298> resources = resourceManager.method_14488(
            "models",
            id -> {
               if (!namespace.equals(id.method_12836())) {
                  return false;
               } else {
                  String path = id.method_12832().toLowerCase(Locale.ROOT);
                  return path.startsWith(normalizedPrefix)
                     && (
                        path.endsWith(".fbx")
                           || path.endsWith(".obj")
                           || path.endsWith(".mtl")
                           || path.endsWith(".gltf")
                           || path.endsWith(".glb")
                           || path.endsWith(".dae")
                     );
               }
            }
         );

         for (class_3298 resource : resources.values()) {
            total += resourceByteLength(resource);
         }
      }

      return total;
   }

   private static long resourceByteLength(class_3298 resource) {
      if (resource == null) {
         return 0L;
      } else {
         byte[] buffer = new byte[8192];
         long total = 0L;

         try (InputStream in = resource.method_14482()) {
            while (true) {
               int read = in.read(buffer);
               if (read < 0) {
                  return total;
               }

               total += read;
            }
         } catch (IOException var9) {
            return 0L;
         }
      }
   }

   private static String readString(ByteArrayInputStream in, int maxLength) {
      int byteLength = readVarInt(in);
      if (byteLength >= 0 && byteLength <= maxLength * 4 && byteLength <= in.available()) {
         byte[] data = new byte[byteLength];
         int read = in.read(data, 0, byteLength);
         if (read != byteLength) {
            throw new IllegalArgumentException("Unexpected end of string payload");
         } else {
            String decoded = new String(data, StandardCharsets.UTF_8);
            if (decoded.length() > maxLength) {
               throw new IllegalArgumentException("String too long");
            } else {
               return decoded;
            }
         }
      } else {
         throw new IllegalArgumentException("Invalid string length");
      }
   }

   private static byte[] readByteArray(ByteArrayInputStream in, int maxLength) {
      int byteLength = readVarInt(in);
      if (byteLength > 0 && byteLength <= maxLength && byteLength <= in.available()) {
         byte[] data = new byte[byteLength];
         int read = in.read(data, 0, byteLength);
         if (read != byteLength) {
            throw new IllegalArgumentException("Unexpected end of byte payload");
         } else {
            return data;
         }
      } else {
         throw new IllegalArgumentException("Invalid byte array length");
      }
   }

   private static void writeString(ByteArrayOutputStream out, String value) {
      byte[] data = value.getBytes(StandardCharsets.UTF_8);
      writeVarInt(out, data.length);
      out.writeBytes(data);
   }

   private static int readVarInt(ByteArrayInputStream in) {
      int numRead = 0;
      int result = 0;

      int read;
      do {
         read = in.read();
         if (read == -1) {
            throw new IllegalArgumentException("Unexpected end of varint");
         }

         int value = read & 127;
         result |= value << 7 * numRead;
         if (++numRead > 5) {
            throw new IllegalArgumentException("VarInt too long");
         }
      } while ((read & 128) != 0);

      return result;
   }

   private static void writeVarInt(ByteArrayOutputStream out, int value) {
      int current;
      for (current = value; (current & -128) != 0L; current >>>= 7) {
         out.write(current & 127 | 128);
      }

      out.write(current & 127);
   }

   private record BundleResourceCollection(Map<class_2960, byte[]> resources, Set<class_2960> temporaryMirroredTextureIds) {
   }

   private static final class IncomingBundleState {
      private final int entityId;
      private final int transferId;
      private final class_2960 modelId;
      private final String playerName;
      private final int expectedSize;
      private final ByteArrayOutputStream out = new ByteArrayOutputStream(16384);

      private IncomingBundleState(int entityId, int transferId, class_2960 modelId, String playerName, int expectedSize) {
         this.entityId = entityId;
         this.transferId = transferId;
         this.modelId = modelId;
         this.playerName = playerName;
         this.expectedSize = Math.max(0, expectedSize);
      }

      private boolean append(byte[] chunk) {
         if (chunk != null && chunk.length != 0) {
            if (this.out.size() + chunk.length > 100663296) {
               return false;
            } else if (this.expectedSize > 0 && this.out.size() + chunk.length > this.expectedSize) {
               return false;
            } else {
               this.out.writeBytes(chunk);
               return true;
            }
         } else {
            return true;
         }
      }

      private int entityId() {
         return this.entityId;
      }

      private int transferId() {
         return this.transferId;
      }

      private class_2960 modelId() {
         return this.modelId;
      }

      private String playerName() {
         return this.playerName;
      }

      private byte[] bytes() {
         return this.out.toByteArray();
      }
   }

   public record RawChannelPayload(byte[] bytes) implements class_8710 {
      public static final class_9154<PlayerModelSyncClient.RawChannelPayload> ID = new class_9154(PlayerModelSyncClient.CHANNEL_ID);
      public static final class_9139<class_9129, PlayerModelSyncClient.RawChannelPayload> CODEC = class_9139.method_56438(
         (value, buf) -> writeRawBytes(buf, value.bytes), buf -> new PlayerModelSyncClient.RawChannelPayload(readRawBytes(buf))
      );

      public class_9154<? extends class_8710> method_56479() {
         return ID;
      }

      private static byte[] readRawBytes(ByteBuf buf) {
         int readable = buf.readableBytes();
         if (readable <= 0) {
            return new byte[0];
         } else {
            byte[] bytes = new byte[readable];
            buf.readBytes(bytes);
            return bytes;
         }
      }

      private static void writeRawBytes(ByteBuf buf, byte[] bytes) {
         if (bytes != null && bytes.length != 0) {
            buf.writeBytes(bytes);
         }
      }
   }

   public record ServerCachedModelEntry(int entityId, String playerName, class_2960 modelId, long sizeBytes) {
      public double sizeMb() {
         return this.sizeBytes / 1048576.0;
      }
   }
}
