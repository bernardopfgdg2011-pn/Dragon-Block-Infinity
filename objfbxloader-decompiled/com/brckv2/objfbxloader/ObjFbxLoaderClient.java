package com.brckv2.objfbxloader;

import com.brckv2.objfbxloader.client.gui.MultiplayerOptionsScreen;
import com.brckv2.objfbxloader.client.gui.RigEditorRootScreen;
import com.brckv2.objfbxloader.client.network.PlayerModelSyncClient;
import com.brckv2.objfbxloader.client.render.ResourcePackMeshLoader;
import com.brckv2.objfbxloader.client.render.ResourcePackObjBlockModel;
import com.brckv2.objfbxloader.client.render.ResourcePackObjItemModel;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingFeatureRenderer;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import com.brckv2.objfbxloader.client.voice.VoiceLipSyncState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBakeBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBakeItem;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_10439;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;

public class ObjFbxLoaderClient {
   private static final String ANIM_CACHE_KEY = "animation_caching";
   private static final Path CLIENT_CONFIG_PATH = PlatformPaths.configDir().resolve("objfbxloader-client.properties");
   private static final Path MP_MODELS_CONFIG_PATH = PlatformPaths.gameDir().resolve("configs").resolve("mpenabled.json");
   private static final Path OTHERS_MODELED_CONFIG_PATH = PlatformPaths.gameDir().resolve("configs").resolve("othersmodeled.json");
   private static class_304 openHeldItemRigEditorKey;

   public void onInitializeClient() {
      applyAnimationCachingSetting(loadPersistedAnimationCachingSetting(), false, null);
      applyMpModelsSetting(loadPersistedMpModelsSetting(), false, null);
      applyOthersModeledSetting(loadPersistedOthersModeledSetting(), false, null);
      PlayerModelSyncClient.initialize();
      VoiceLipSyncState.initialize();
      openHeldItemRigEditorKey = KeyBindingHelper.registerKeyBinding(
         new class_304(
            "key.com.brckv2.objfbxloader.open_fbx_tuning",
            class_307.field_1668,
            79,
            class_11900.method_74698(class_2960.method_60655("com.brckv2.objfbxloader", "general"))
         )
      );
      LivingEntityFeatureRendererRegistrationCallback.EVENT
         .register(
            (LivingEntityFeatureRendererRegistrationCallback)(entityType, entityRenderer, registrationHelper, context) -> registrationHelper.register(
               new ResourcePackObjLivingFeatureRenderer(entityRenderer)
            )
         );
      PreparableModelLoadingPlugin.register(
         (store, executor) -> CompletableFuture.supplyAsync(() -> ResourcePackObjItemModel.loadOverrides(store.method_72361()), executor),
         (objModels, context) -> context.modifyItemModelAfterBake().register((AfterBakeItem)(model, modelContext) -> {
            ResourcePackObjItemModel.RenderableObjModel objModel = (ResourcePackObjItemModel.RenderableObjModel)objModels.get(modelContext.itemId());
            return (class_10439)(objModel == null ? model : new ResourcePackObjItemModel(model, objModel));
         })
      );
      PreparableModelLoadingPlugin.register(
         (store, executor) -> CompletableFuture.supplyAsync(() -> ResourcePackObjBlockModel.loadOverrides(store.method_72361()), executor),
         (objModels, context) -> context.modifyBlockModelAfterBake().register((AfterBakeBlock)(model, modelContext) -> {
            class_2960 blockId = class_7923.field_41175.method_10221(modelContext.state().method_26204());
            ResourcePackObjBlockModel.BlockOverride override = (ResourcePackObjBlockModel.BlockOverride)objModels.get(blockId);
            return override == null ? model : override.wrap(model);
         })
      );
      PreparableModelLoadingPlugin.register(
         (store, executor) -> CompletableFuture.supplyAsync(() -> ResourcePackObjLivingOverrides.load(store.method_72361()), executor),
         (data, context) -> ResourcePackObjLivingOverrides.setData(data)
      );
      WorldRenderEvents.AFTER_ENTITIES.register(ResourcePackObjBlockModel::renderPlacedOverrides);
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("cacheanims")
                        .then(ClientCommandManager.literal("on").executes(context -> {
                           applyAnimationCachingSetting(true, true, ((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                           return 1;
                        })))
                     .then(ClientCommandManager.literal("off").executes(context -> {
                        applyAnimationCachingSetting(false, true, ((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                        return 1;
                     }))
               );
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("cacheanimstats").executes(context -> {
                        sendCacheStats(((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                        return 1;
                     }))
                     .then(
                        ClientCommandManager.literal("reset")
                           .executes(
                              context -> {
                                 ResourcePackMeshLoader.resetCacheStats();
                                 ResourcePackObjLivingOverrides.resetCacheStats();
                                 if (((FabricClientCommandSource)context.getSource()).getClient().field_1724 != null) {
                                    ((FabricClientCommandSource)context.getSource())
                                       .getClient()
                                       .field_1724
                                       .method_7353(class_2561.method_43470("Cache animation stats reset."), false);
                                 }

                                 return 1;
                              }
                           )
                     )
               );
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("mpmodels")
                           .then(ClientCommandManager.literal("on").executes(context -> {
                              applyMpModelsSetting(true, true, ((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                              return 1;
                           })))
                        .then(ClientCommandManager.literal("off").executes(context -> {
                           applyMpModelsSetting(false, true, ((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                           return 1;
                        })))
                     .executes(
                        context -> {
                           class_746 player = ((FabricClientCommandSource)context.getSource()).getClient().field_1724;
                           if (player != null) {
                              boolean enabled = ResourcePackObjLivingOverrides.isMultiplayerPlayerModelsEnabled();
                              player.method_7353(
                                 class_2561.method_43470("MP models (server-synced per-player models) are currently " + (enabled ? "on" : "off") + "."), false
                              );
                           }

                           return 1;
                        }
                     )
               );
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("othersmodeled")
                           .then(ClientCommandManager.literal("on").executes(context -> {
                              applyOthersModeledSetting(true, true, ((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                              return 1;
                           })))
                        .then(ClientCommandManager.literal("off").executes(context -> {
                           applyOthersModeledSetting(false, true, ((FabricClientCommandSource)context.getSource()).getClient().field_1724);
                           return 1;
                        })))
                     .executes(
                        context -> {
                           class_746 player = ((FabricClientCommandSource)context.getSource()).getClient().field_1724;
                           if (player != null) {
                              boolean enabled = ResourcePackObjLivingOverrides.isOthersModeledEnabled();
                              player.method_7353(
                                 class_2561.method_43470(
                                    "Others modeled (force remote players to your selected model) is currently " + (enabled ? "on" : "off") + "."
                                 ),
                                 false
                              );
                           }

                           return 1;
                        }
                     )
               );
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("playermodel")
                        .then(ClientCommandManager.argument("id", StringArgumentType.string()).executes(context -> {
                           String raw = StringArgumentType.getString(context, "id");
                           class_2960 parsed = PlayerModelSyncClient.normalizeModelId(raw);
                           class_746 player = ((FabricClientCommandSource)context.getSource()).getClient().field_1724;
                           class_310 client = ((FabricClientCommandSource)context.getSource()).getClient();
                           if (!PlayerModelSyncClient.setSelectedPlayerModel(parsed, true, false, client)) {
                              if (player != null) {
                                 player.method_7353(class_2561.method_43470("Invalid model id. Use namespace:path (example: minecraft:player)."), false);
                              }

                              return 0;
                           } else {
                              if (player != null) {
                                 player.method_7353(
                                    class_2561.method_43470("Player model ID set to " + PlayerModelSyncClient.getSelectedPlayerModel() + "."), false
                                 );
                                 player.method_7353(class_2561.method_43470("Not synced yet. Run /syncplayermodel to upload manually."), false);
                              }

                              return 1;
                           }
                        })))
                     .executes(context -> {
                        class_746 player = ((FabricClientCommandSource)context.getSource()).getClient().field_1724;
                        if (player != null) {
                           player.method_7353(class_2561.method_43470("Current player model ID: " + PlayerModelSyncClient.getSelectedPlayerModel()), false);
                           player.method_7353(class_2561.method_43470("Channel: " + PlayerModelSyncClient.channelId()), false);
                        }

                        return 1;
                     })
               );
               dispatcher.register(
                  (LiteralArgumentBuilder)ClientCommandManager.literal("syncplayermodel")
                     .executes(
                        context -> {
                           class_746 player = ((FabricClientCommandSource)context.getSource()).getClient().field_1724;
                           PlayerModelSyncClient.syncSelectedPlayerModel(true);
                           if (player != null) {
                              player.method_7353(
                                 class_2561.method_43470("Manual player model sync started for " + PlayerModelSyncClient.getSelectedPlayerModel() + "."), false
                              );
                           }

                           return 1;
                        }
                     )
               );
            }
         );
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               PlayerModelSyncClient.onClientTick(client);
               if (openHeldItemRigEditorKey != null) {
                  while (openHeldItemRigEditorKey.method_1436()) {
                     if (client.field_1724 != null && client.field_1755 == null) {
                        client.method_1507(new RigEditorRootScreen(null));
                     }
                  }
               }

               ResourcePackObjBlockModel.tick(client);
               if (client.field_1724 == null) {
                  ResourcePackObjLivingOverrides.setLocalPlayerEntityId(Integer.MIN_VALUE);
               } else {
                  ResourcePackObjLivingOverrides.setLocalPlayerEntityId(client.field_1724.method_5628());
                  ResourcePackObjLivingOverrides.setNetworkPlayerModelTarget(
                     client.field_1724.method_5628(), PlayerModelSyncClient.getSelectedPlayerModel(), client.field_1724.method_5477().getString()
                  );
               }
            }
         );
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
         ResourcePackMeshLoader.clearRuntimeGeneratedTextures(client);
         PlayerModelSyncClient.clearGeneratedTextureMirrorCache();
      });
   }

   private static void applyAnimationCachingSetting(boolean enabled, boolean persist, class_1657 player) {
      ResourcePackObjLivingOverrides.setAnimationCachingEnabled(false);
      ResourcePackMeshLoader.setAnimationCachingEnabled(false);
      if (persist) {
         savePersistedAnimationCachingSetting(false);
      }

      if (player != null) {
         player.method_7353(class_2561.method_43470("Animation caching is temporarily disabled in this build."), false);
      }
   }

   private static boolean loadPersistedAnimationCachingSetting() {
      return false;
   }

   private static void savePersistedAnimationCachingSetting(boolean enabled) {
      Properties properties = new Properties();
      properties.setProperty("animation_caching", Boolean.toString(enabled));

      try {
         Path parent = CLIENT_CONFIG_PATH.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         try (OutputStream output = Files.newOutputStream(CLIENT_CONFIG_PATH)) {
            properties.store(output, "objfbxloader client settings");
         }
      } catch (IOException var8) {
      }
   }

   private static void sendCacheStats(class_1657 player) {
      if (player != null) {
         player.method_7353(class_2561.method_43470(ResourcePackMeshLoader.cacheStatsSummary()), false);
         player.method_7353(class_2561.method_43470(ResourcePackObjLivingOverrides.cacheStatsSummary()), false);
      }
   }

   public static void applyMpModelsSetting(boolean enabled, boolean persist, class_1657 player) {
      ResourcePackObjLivingOverrides.setMultiplayerPlayerModelsEnabled(enabled);
      if (persist) {
         savePersistedMpModelsSetting(enabled);
      }

      if (player != null) {
         player.method_7353(class_2561.method_43470("MP models (server-synced per-player models) are now " + (enabled ? "on" : "off") + "."), false);
      }
   }

   public static boolean loadPersistedMpModelsSetting() {
      if (!Files.exists(MP_MODELS_CONFIG_PATH)) {
         savePersistedMpModelsSetting(true);
         return true;
      } else {
         try {
            String text = Files.readString(MP_MODELS_CONFIG_PATH, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            if (text.contains("\"no\"") || text.contains(": false")) {
               return false;
            }

            if (text.contains("\"yes\"") || text.contains(": true")) {
               return true;
            }
         } catch (IOException var1) {
         }

         return true;
      }
   }

   public static void savePersistedMpModelsSetting(boolean enabled) {
      String value = enabled ? "yes" : "no";
      String json = "{\n  \"mpenabled\": \"" + value + "\"\n}\n";

      try {
         Path parent = MP_MODELS_CONFIG_PATH.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         Files.writeString(MP_MODELS_CONFIG_PATH, json, StandardCharsets.UTF_8);
      } catch (IOException var4) {
      }
   }

   public static void applyOthersModeledSetting(boolean enabled, boolean persist, class_1657 player) {
      ResourcePackObjLivingOverrides.setOthersModeledEnabled(enabled);
      if (persist) {
         savePersistedOthersModeledSetting(enabled);
      }

      if (player != null) {
         player.method_7353(
            class_2561.method_43470("Others modeled (force remote players to your selected model) is now " + (enabled ? "on" : "off") + "."), false
         );
      }
   }

   public static boolean loadPersistedOthersModeledSetting() {
      if (!Files.exists(OTHERS_MODELED_CONFIG_PATH)) {
         savePersistedOthersModeledSetting(false);
         return false;
      } else {
         try {
            String text = Files.readString(OTHERS_MODELED_CONFIG_PATH, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            if (text.contains("\"yes\"") || text.contains(": true")) {
               return true;
            }

            if (text.contains("\"no\"") || text.contains(": false")) {
               return false;
            }
         } catch (IOException var1) {
         }

         return false;
      }
   }

   public static void savePersistedOthersModeledSetting(boolean enabled) {
      String value = enabled ? "yes" : "no";
      String json = "{\n  \"othersmodeled\": \"" + value + "\"\n}\n";

      try {
         Path parent = OTHERS_MODELED_CONFIG_PATH.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         Files.writeString(OTHERS_MODELED_CONFIG_PATH, json, StandardCharsets.UTF_8);
      } catch (IOException var4) {
      }
   }

   public static void openMultiplayerOptionsScreen(class_437 parent) {
      class_310 client = class_310.method_1551();
      if (client != null) {
         client.method_1507(new MultiplayerOptionsScreen(parent));
      }
   }
}
