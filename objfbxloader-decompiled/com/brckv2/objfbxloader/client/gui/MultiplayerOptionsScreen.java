package com.brckv2.objfbxloader.client.gui;

import com.brckv2.objfbxloader.ObjFbxLoaderClient;
import com.brckv2.objfbxloader.PlatformPaths;
import com.brckv2.objfbxloader.client.network.PlayerModelSyncClient;
import com.brckv2.objfbxloader.client.render.FbxRigConfig;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import com.brckv2.objfbxloader.client.voice.VoiceLipSyncState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class MultiplayerOptionsScreen extends class_437 {
   private static final int MAX_VISIBLE_CACHE_ROWS = 9;
   private static final int TAB_SYNC = 0;
   private static final int TAB_VOICE = 1;
   private static final int BONE_OPTION_COUNT = 8;
   private static final class_2960 PLAYER_ID = class_2960.method_60656("player");
   private static final class_2960 PLAYER_CONFIG_ID = class_2960.method_60656("configs/entity/player.json");
   private static final class_2960 LEGACY_PLAYER_CONFIG_ID = class_2960.method_60656("configs/player.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private final class_437 parent;
   private class_4185 syncTabButton;
   private class_4185 voiceTabButton;
   private class_342 modelIdField;
   private class_4185 mpModelsToggleButton;
   private class_4185 othersModeledToggleButton;
   private class_4185 saveSyncButton;
   private class_4185 resetModelButton;
   private class_4185 voiceLipSyncToggleButton;
   private class_4185 upperLipBoneButton;
   private class_4185 lowerLipBoneButton;
   private class_4185 upperLipAdvancedButton;
   private class_4185 lowerLipAdvancedButton;
   private class_4185 saveLipButton;
   private class_342 boneSearchField;
   private final List<class_4185> boneOptionButtons = new ArrayList<>();
   private final List<String> boneOptionValues = new ArrayList<>();
   private String lastBoneSearch = "";
   private MultiplayerOptionsScreen.BonePickerTarget activeBonePicker = MultiplayerOptionsScreen.BonePickerTarget.NONE;
   private int pickerX;
   private int pickerY;
   private int pickerWidth;
   private class_4185 closeButton;
   private int activeTab = 0;
   private String statusLine = "";
   private String upperLipBones = "";
   private String lowerLipBones = "";
   private Path playerConfigPath;
   private String playerConfigPackName = "unknown";

   public MultiplayerOptionsScreen(class_437 parent) {
      super(class_2561.method_43470("Multiplayer Options"));
      this.parent = parent;
   }

   protected void method_25426() {
      int centerX = this.field_22789 / 2;
      int top = Math.max(14, this.field_22790 / 2 - 120);
      int fullWidth = 300;
      int left = centerX - fullWidth / 2;
      int tabWidth = 146;
      this.pickerWidth = 300;
      this.pickerX = left;
      this.pickerY = top + 248;
      FbxRigConfig playerConfig = ResourcePackObjLivingOverrides.getRigConfigForTarget(PLAYER_ID);
      this.upperLipBones = playerConfig == null ? "" : playerConfig.voiceUpperLipBone();
      this.lowerLipBones = playerConfig == null ? "" : playerConfig.voiceLowerLipBone();
      this.playerConfigPath = this.resolveEditablePlayerConfigPath();
      this.syncTabButton = (class_4185)this.method_37063(class_4185.method_46430(this.syncTabLabel(), button -> {
         this.activeTab = 0;
         this.closeBonePicker();
         this.updateTabVisibility();
      }).method_46434(left, top + 24, tabWidth, 20).method_46431());
      this.voiceTabButton = (class_4185)this.method_37063(class_4185.method_46430(this.voiceTabLabel(), button -> {
         this.activeTab = 1;
         this.updateTabVisibility();
      }).method_46434(left + 154, top + 24, tabWidth, 20).method_46431());
      this.mpModelsToggleButton = (class_4185)this.method_37063(class_4185.method_46430(this.toggleLabel(), button -> {
         boolean next = !ResourcePackObjLivingOverrides.isMultiplayerPlayerModelsEnabled();
         ObjFbxLoaderClient.applyMpModelsSetting(next, true, this.field_22787 == null ? null : this.field_22787.field_1724);
         button.method_25355(this.toggleLabel());
         this.statusLine = "MP models are now " + (next ? "on." : "off.");
      }).method_46434(left, top + 54, fullWidth, 20).method_46431());
      this.othersModeledToggleButton = (class_4185)this.method_37063(class_4185.method_46430(this.othersModeledLabel(), button -> {
         boolean next = !ResourcePackObjLivingOverrides.isOthersModeledEnabled();
         ObjFbxLoaderClient.applyOthersModeledSetting(next, true, this.field_22787 == null ? null : this.field_22787.field_1724);
         button.method_25355(this.othersModeledLabel());
         this.statusLine = "Others modeled is now " + (next ? "on." : "off.");
      }).method_46434(left, top + 76, fullWidth, 20).method_46431());
      this.modelIdField = (class_342)this.method_37063(
         new class_342(this.field_22793, left, top + 110, fullWidth, 20, class_2561.method_43470("Model ID (namespace:path)"))
      );
      this.modelIdField.method_1880(96);
      this.modelIdField.method_1852(PlayerModelSyncClient.getSelectedPlayerModel().toString());
      this.saveSyncButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Save + Sync Player"), button -> this.saveAndSync())
            .method_46434(left, top + 136, 146, 20)
            .method_46431()
      );
      this.resetModelButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43470("Reset to minecraft:player"), button -> {
         this.modelIdField.method_1852("minecraft:player");
         this.saveAndSync();
      }).method_46434(left + 154, top + 136, 146, 20).method_46431());
      this.voiceLipSyncToggleButton = (class_4185)this.method_37063(class_4185.method_46430(this.voiceLipSyncLabel(), button -> {
         boolean next = !VoiceLipSyncState.isEnabled();
         VoiceLipSyncState.setSettings(next, VoiceLipSyncState.getSensitivity());
         button.method_25355(this.voiceLipSyncLabel());
         this.statusLine = "Voice lip sync is now " + (next ? "on." : "off.");
      }).method_46434(left, top + 54, fullWidth, 20).method_46431());
      this.upperLipBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.upperLipBoneButtonLabel(), button -> this.toggleBonePicker(MultiplayerOptionsScreen.BonePickerTarget.UPPER))
            .method_46434(left, top + 82, 206, 20)
            .method_46431()
      );
      this.upperLipAdvancedButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Advanced"), button -> this.openAdvancedBonePicker(MultiplayerOptionsScreen.BonePickerTarget.UPPER))
            .method_46434(left + 210, top + 82, 90, 20)
            .method_46431()
      );
      this.lowerLipBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.lowerLipBoneButtonLabel(), button -> this.toggleBonePicker(MultiplayerOptionsScreen.BonePickerTarget.LOWER))
            .method_46434(left, top + 106, 206, 20)
            .method_46431()
      );
      this.lowerLipAdvancedButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Advanced"), button -> this.openAdvancedBonePicker(MultiplayerOptionsScreen.BonePickerTarget.LOWER))
            .method_46434(left + 210, top + 106, 90, 20)
            .method_46431()
      );
      this.saveLipButton = (class_4185)this.method_37063(
         class_4185.method_46430(
               class_2561.method_43470("Save Lip Settings"),
               button -> {
                  boolean saved = this.savePlayerLipConfig();
                  VoiceLipSyncState.setSettings(VoiceLipSyncState.isEnabled(), VoiceLipSyncState.getSensitivity());
                  this.voiceLipSyncToggleButton.method_25355(this.voiceLipSyncLabel());
                  if (saved) {
                     PlayerModelSyncClient.syncSelectedPlayerModel(true);
                  }

                  this.statusLine = saved
                     ? "Saved lip bones to " + this.playerConfigPackName + " player.json and synced to server."
                     : "Could not save player.json (editable pack not found).";
               }
            )
            .method_46434(left, top + 156, fullWidth, 20)
            .method_46431()
      );
      this.closeButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Close"), button -> this.method_25419()).method_46434(left, top + 182, fullWidth, 20).method_46431()
      );
      this.boneSearchField = (class_342)this.method_37063(
         new class_342(this.field_22793, this.pickerX, this.pickerY + 18, this.pickerWidth, 18, class_2561.method_43470("Search bones"))
      );
      this.boneSearchField.method_1880(64);
      this.boneSearchField.method_1852("");
      this.boneSearchField.field_22764 = false;
      this.boneSearchField.field_22763 = false;
      this.boneSearchField.method_25365(false);
      this.boneOptionButtons.clear();

      for (int i = 0; i < 8; i++) {
         int optionIndex = i;
         class_4185 optionButton = (class_4185)this.method_37063(
            class_4185.method_46430(class_2561.method_43470(""), button -> this.selectBoneOption(optionIndex))
               .method_46434(this.pickerX, this.pickerY + 42 + i * 20, this.pickerWidth, 18)
               .method_46431()
         );
         optionButton.field_22764 = false;
         optionButton.field_22763 = false;
         this.boneOptionButtons.add(optionButton);
      }

      this.updateTabVisibility();
   }

   public void method_25393() {
      super.method_25393();
      if (this.activeBonePicker != MultiplayerOptionsScreen.BonePickerTarget.NONE && this.boneSearchField != null) {
         String currentSearch = this.boneSearchField.method_1882() == null ? "" : this.boneSearchField.method_1882();
         if (!currentSearch.equals(this.lastBoneSearch)) {
            this.lastBoneSearch = currentSearch;
            this.refreshBoneOptionButtons();
         }
      }
   }

   private class_2561 toggleLabel() {
      boolean enabled = ResourcePackObjLivingOverrides.isMultiplayerPlayerModelsEnabled();
      return class_2561.method_43470("MP Models (per-player sync): " + (enabled ? "ON" : "OFF"));
   }

   private class_2561 othersModeledLabel() {
      boolean enabled = ResourcePackObjLivingOverrides.isOthersModeledEnabled();
      return class_2561.method_43470("Others Modeled (use your model): " + (enabled ? "ON" : "OFF"));
   }

   private class_2561 voiceLipSyncLabel() {
      return class_2561.method_43470("Voice Lip Sync (Simple Voice Chat): " + (VoiceLipSyncState.isEnabled() ? "ON" : "OFF"));
   }

   private class_2561 syncTabLabel() {
      return class_2561.method_43470(this.activeTab == 0 ? "[Model Sync]" : "Model Sync");
   }

   private class_2561 voiceTabLabel() {
      return class_2561.method_43470(this.activeTab == 1 ? "[Voice Chat]" : "Voice Chat");
   }

   private class_2561 upperLipBoneButtonLabel() {
      return class_2561.method_43470("Upper Lip: " + shortenBoneList(this.upperLipBones));
   }

   private class_2561 lowerLipBoneButtonLabel() {
      return class_2561.method_43470("Lower Lip: " + shortenBoneList(this.lowerLipBones));
   }

   private void refreshBoneButtons() {
      if (this.upperLipBoneButton != null) {
         this.upperLipBoneButton.method_25355(this.upperLipBoneButtonLabel());
      }

      if (this.lowerLipBoneButton != null) {
         this.lowerLipBoneButton.method_25355(this.lowerLipBoneButtonLabel());
      }
   }

   private void updateTabVisibility() {
      boolean syncTab = this.activeTab == 0;
      boolean voiceTab = this.activeTab == 1;
      setVisible(this.mpModelsToggleButton, syncTab);
      setVisible(this.othersModeledToggleButton, syncTab);
      setVisible(this.modelIdField, syncTab);
      setVisible(this.saveSyncButton, syncTab);
      setVisible(this.resetModelButton, syncTab);
      setVisible(this.voiceLipSyncToggleButton, voiceTab);
      setVisible(this.upperLipBoneButton, voiceTab);
      setVisible(this.lowerLipBoneButton, voiceTab);
      setVisible(this.upperLipAdvancedButton, voiceTab);
      setVisible(this.lowerLipAdvancedButton, voiceTab);
      setVisible(this.saveLipButton, voiceTab);
      if (!voiceTab) {
         this.closeBonePicker();
      }

      if (this.syncTabButton != null) {
         this.syncTabButton.method_25355(this.syncTabLabel());
      }

      if (this.voiceTabButton != null) {
         this.voiceTabButton.method_25355(this.voiceTabLabel());
      }

      this.refreshBoneButtons();
   }

   private static void setVisible(class_339 widget, boolean visible) {
      if (widget != null) {
         widget.field_22764 = visible;
         widget.field_22763 = visible;
      }
   }

   private void saveAndSync() {
      String raw = this.modelIdField == null ? "" : this.modelIdField.method_1882();
      class_2960 modelId = PlayerModelSyncClient.normalizeModelId(raw);
      if (!PlayerModelSyncClient.isAllowedModelId(modelId)) {
         this.statusLine = "Invalid model ID. Use namespace:path, e.g. minecraft:player";
      } else {
         boolean ok = PlayerModelSyncClient.setSelectedPlayerModel(modelId, true, false, this.field_22787);
         if (!ok) {
            this.statusLine = "Could not save model ID.";
         } else {
            PlayerModelSyncClient.syncSelectedPlayerModel(true);
            this.statusLine = "Synced player model: " + modelId;
            this.modelIdField.method_1852(modelId.toString());
         }
      }
   }

   private void toggleBonePicker(MultiplayerOptionsScreen.BonePickerTarget target) {
      if (target == null || target == MultiplayerOptionsScreen.BonePickerTarget.NONE) {
         this.closeBonePicker();
      } else if (this.activeBonePicker == target) {
         this.closeBonePicker();
      } else {
         this.activeBonePicker = target;
         if (this.boneSearchField != null) {
            this.boneSearchField.field_22764 = true;
            this.boneSearchField.field_22763 = true;
            this.boneSearchField.method_25365(true);
            this.lastBoneSearch = this.boneSearchField.method_1882() == null ? "" : this.boneSearchField.method_1882();
         }

         this.refreshBoneOptionButtons();
      }
   }

   private void closeBonePicker() {
      this.activeBonePicker = MultiplayerOptionsScreen.BonePickerTarget.NONE;
      if (this.boneSearchField != null) {
         this.boneSearchField.field_22764 = false;
         this.boneSearchField.field_22763 = false;
         this.boneSearchField.method_25365(false);
      }

      for (class_4185 optionButton : this.boneOptionButtons) {
         optionButton.field_22764 = false;
         optionButton.field_22763 = false;
         optionButton.method_25355(class_2561.method_43470(""));
      }

      this.boneOptionValues.clear();
   }

   private void refreshBoneOptionButtons() {
      if (this.activeBonePicker != MultiplayerOptionsScreen.BonePickerTarget.NONE && this.boneSearchField != null) {
         this.boneOptionValues.clear();
         String query = this.boneSearchField.method_1882() == null ? "" : this.boneSearchField.method_1882().trim().toLowerCase(Locale.ROOT);

         for (String bone : this.getPlayerBoneNames()) {
            if (query.isBlank() || bone.toLowerCase(Locale.ROOT).contains(query)) {
               this.boneOptionValues.add(bone);
               if (this.boneOptionValues.size() >= 8) {
                  break;
               }
            }
         }

         for (int i = 0; i < this.boneOptionButtons.size(); i++) {
            class_4185 button = this.boneOptionButtons.get(i);
            if (i < this.boneOptionValues.size()) {
               String bonex = this.boneOptionValues.get(i);
               button.field_22764 = true;
               button.field_22763 = true;
               button.method_25355(class_2561.method_43470(bonex));
            } else {
               button.field_22764 = false;
               button.field_22763 = false;
               button.method_25355(class_2561.method_43470(""));
            }
         }
      } else {
         this.closeBonePicker();
      }
   }

   private List<String> getPlayerBoneNames() {
      return ResourcePackObjLivingOverrides.getBoneNamesForTarget(PLAYER_ID);
   }

   private void selectBoneOption(int optionIndex) {
      if (optionIndex >= 0 && optionIndex < this.boneOptionValues.size()) {
         String selected = this.boneOptionValues.get(optionIndex);
         if (this.activeBonePicker == MultiplayerOptionsScreen.BonePickerTarget.UPPER) {
            this.upperLipBones = selected == null ? "" : selected;
         } else if (this.activeBonePicker == MultiplayerOptionsScreen.BonePickerTarget.LOWER) {
            this.lowerLipBones = selected == null ? "" : selected;
         }

         this.refreshBoneButtons();
         this.closeBonePicker();
      }
   }

   private void openAdvancedBonePicker(MultiplayerOptionsScreen.BonePickerTarget target) {
      if (this.field_22787 != null && target != null && target != MultiplayerOptionsScreen.BonePickerTarget.NONE) {
         this.closeBonePicker();
         List<String> allBones = this.getPlayerBoneNames();
         String existingRaw = target == MultiplayerOptionsScreen.BonePickerTarget.UPPER ? this.upperLipBones : this.lowerLipBones;
         List<String> selected = FbxRigConfig.splitBoneTargets(existingRaw);
         class_2561 title = target == MultiplayerOptionsScreen.BonePickerTarget.UPPER
            ? class_2561.method_43470("Advanced Upper Lip Bone Picker")
            : class_2561.method_43470("Advanced Lower Lip Bone Picker");
         this.field_22787.method_1507(new BoneMultiSelectScreen(this, title, allBones, selected, picked -> {
            String serialized = FbxRigConfig.joinBoneTargets(picked);
            if (target == MultiplayerOptionsScreen.BonePickerTarget.UPPER) {
               this.upperLipBones = serialized;
            } else if (target == MultiplayerOptionsScreen.BonePickerTarget.LOWER) {
               this.lowerLipBones = serialized;
            }

            this.refreshBoneButtons();
         }));
      }
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      context.method_25294(0, 0, this.field_22789, this.field_22790, -1072689136);
      this.renderBonePickerPanel(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      int top = Math.max(14, this.field_22790 / 2 - 120);
      String subtitle = this.activeTab == 0 ? "Server-relayed player model sync" : "Simple Voice Chat lip-sync controls";
      context.method_51439(this.field_22793, this.field_22785, centerX - this.field_22793.method_27525(this.field_22785) / 2, top, -1, false);
      context.method_51439(this.field_22793, class_2561.method_43470(subtitle), centerX - this.field_22793.method_1727(subtitle) / 2, top + 12, -5197648, false);
      if (this.activeTab == 0) {
         String helper = "Model IDs must exist in your loaded resource pack";
         String helper2 = "Remote players use synced model IDs; no synced entry = vanilla remote";
         context.method_51439(this.field_22793, class_2561.method_43470("Player model ID"), centerX - 150, top + 100, -3092272, false);
         context.method_51439(this.field_22793, class_2561.method_43470(helper), centerX - this.field_22793.method_1727(helper) / 2, top + 206, -6643546, false);
         context.method_51439(
            this.field_22793, class_2561.method_43470(helper2), centerX - this.field_22793.method_1727(helper2) / 2, top + 218, -6643546, false
         );
         int listTop = top + 232;
         context.method_51439(this.field_22793, class_2561.method_43470("Server Cached Models"), centerX - 150, listTop, -1776412, false);
         List<PlayerModelSyncClient.ServerCachedModelEntry> entries = PlayerModelSyncClient.getServerCachedModelsSnapshot();
         if (entries.isEmpty()) {
            context.method_51439(
               this.field_22793, class_2561.method_43470("No cached entries received from server yet."), centerX - 150, listTop + 12, -6643546, false
            );
         } else {
            int rows = Math.min(9, entries.size());

            for (int i = 0; i < rows; i++) {
               PlayerModelSyncClient.ServerCachedModelEntry entry = entries.get(i);
               String sizeMb = String.format(Locale.ROOT, "%.2f", entry.sizeMb());
               String line = entry.playerName() + " -> " + entry.modelId() + " (" + sizeMb + " MB)";
               context.method_51439(this.field_22793, class_2561.method_43470(line), centerX - 150, listTop + 12 + i * 10, -3615489, false);
            }

            if (entries.size() > rows) {
               int hidden = entries.size() - rows;
               context.method_51439(
                  this.field_22793, class_2561.method_43470("... +" + hidden + " more"), centerX - 150, listTop + 12 + rows * 10, -6643546, false
               );
            }
         }
      } else {
         String helper = "Dropdown picks one bone. Advanced allows multi-select.";
         String helper2 = "Saved into resource-pack player.json (per-pack).";
         context.method_51439(this.field_22793, class_2561.method_43470("Upper/Lower lip bone pickers"), centerX - 150, top + 208, -3092272, false);
         context.method_51439(this.field_22793, class_2561.method_43470(helper), centerX - this.field_22793.method_1727(helper) / 2, top + 222, -6643546, false);
         context.method_51439(
            this.field_22793, class_2561.method_43470(helper2), centerX - this.field_22793.method_1727(helper2) / 2, top + 234, -6643546, false
         );
      }

      if (this.statusLine != null && !this.statusLine.isBlank()) {
         context.method_51439(
            this.field_22793, class_2561.method_43470(this.statusLine), centerX - this.field_22793.method_1727(this.statusLine) / 2, top + 320, -8585348, false
         );
      }
   }

   private void renderBonePickerPanel(class_332 context) {
      if (this.activeBonePicker != MultiplayerOptionsScreen.BonePickerTarget.NONE && this.boneSearchField != null && this.boneSearchField.field_22764) {
         int panelTop = this.pickerY;
         int panelBottom = this.pickerY + 42 + 160;
         context.method_25294(this.pickerX - 2, panelTop - 2, this.pickerX + this.pickerWidth + 2, panelBottom + 2, -872415232);
         context.method_73198(this.pickerX - 2, panelTop - 2, this.pickerWidth + 4, panelBottom - panelTop + 4, -7303024);
         context.method_51439(
            this.field_22793,
            class_2561.method_43470(this.activeBonePicker == MultiplayerOptionsScreen.BonePickerTarget.UPPER ? "Upper Lip Bone" : "Lower Lip Bone"),
            this.pickerX,
            panelTop + 4,
            -1,
            false
         );
      }
   }

   private static float parseFloatField(class_342 field, float fallback, float min, float max) {
      if (field == null) {
         return fallback;
      } else {
         try {
            float value = Float.parseFloat(field.method_1882().trim());
            if (!Float.isFinite(value)) {
               return fallback;
            } else {
               return value < min ? min : Math.min(max, value);
            }
         } catch (Exception var5) {
            return fallback;
         }
      }
   }

   private static String shortenBone(String boneName) {
      if (boneName != null && !boneName.isBlank()) {
         return boneName.length() <= 22 ? boneName : boneName.substring(0, 19) + "...";
      } else {
         return "(none)";
      }
   }

   private static String shortenBoneList(String raw) {
      List<String> bones = FbxRigConfig.splitBoneTargets(raw);
      if (bones.isEmpty()) {
         return "(none)";
      } else {
         return bones.size() == 1 ? shortenBone(bones.get(0)) : shortenBone(bones.get(0)) + " +" + (bones.size() - 1);
      }
   }

   private boolean savePlayerLipConfig() {
      if (this.field_22787 == null) {
         return false;
      } else {
         FbxRigConfig base = ResourcePackObjLivingOverrides.getRigConfigForTarget(PLAYER_ID);
         if (base == null) {
            base = FbxRigConfig.EMPTY;
         }

         FbxRigConfig next = new FbxRigConfig(
            base.headBone(),
            base.neckBone(),
            base.rightHandBone(),
            base.leftHandBone(),
            base.upperTorsoBone(),
            base.rightArmBone(),
            base.leftArmBone(),
            base.rightItemBone(),
            base.leftItemBone(),
            base.rightHandOffsetX(),
            base.rightHandOffsetY(),
            base.rightHandOffsetZ(),
            base.rightHandRotX(),
            base.rightHandRotY(),
            base.rightHandRotZ(),
            base.rightHandScale(),
            base.leftHandOffsetX(),
            base.leftHandOffsetY(),
            base.leftHandOffsetZ(),
            base.leftHandRotX(),
            base.leftHandRotY(),
            base.leftHandRotZ(),
            base.leftHandScale(),
            base.handAttachmentEnabled(),
            base.thirdPersonItemVisible(),
            base.torsoLookBone(),
            base.attackStopBone(),
            base.headLookPitchFactor(),
            base.headLookYawFactor(),
            base.neckLookFactor(),
            base.torsoLookPitchFactor(),
            base.torsoLookYawFactor(),
            this.upperLipBones == null ? "" : this.upperLipBones,
            this.lowerLipBones == null ? "" : this.lowerLipBones,
            base.voiceLipPitchFactor(),
            base.voiceLipYawFactor(),
            base.voiceLipRollFactor(),
            base.voiceLipMaxDegrees(),
            base.attackUpperBodyOnly(),
            base.entityOffsetX(),
            base.entityOffsetY(),
            base.entityOffsetZ(),
            base.entityRotX(),
            base.entityRotY(),
            base.entityRotZ(),
            base.entityScale(),
            base.animationSpeedMultipliers(),
            base.customItemTransforms()
         );
         ResourcePackObjLivingOverrides.setRigConfigOverrideForTarget(PLAYER_ID, next);

         try {
            if (this.playerConfigPath == null) {
               this.playerConfigPath = this.resolveEditablePlayerConfigPath();
            }

            if (this.playerConfigPath == null) {
               return false;
            } else {
               writePlayerConfigFile(this.playerConfigPath, next);
               return true;
            }
         } catch (IOException var4) {
            return false;
         }
      }
   }

   private Path resolveEditablePlayerConfigPath() {
      if (this.field_22787 != null && this.field_22787.method_1478() != null) {
         class_3298 resource = (class_3298)this.field_22787.method_1478().method_14486(PLAYER_CONFIG_ID).orElse(null);
         String relativePath = "assets/minecraft/configs/entity/player.json";
         if (resource == null) {
            resource = (class_3298)this.field_22787.method_1478().method_14486(LEGACY_PLAYER_CONFIG_ID).orElse(null);
            relativePath = "assets/minecraft/configs/player.json";
         }

         String packName = resource == null ? null : extractPackName(resource.method_14480());
         if (packName != null && !packName.isBlank()) {
            this.playerConfigPackName = packName;
            Path path = PlatformPaths.gameDir().resolve("resourcepacks").resolve(packName);

            for (String part : relativePath.split("/")) {
               path = path.resolve(part);
            }

            return path;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String extractPackName(String packId) {
      if (packId != null && !packId.isBlank()) {
         String normalized = packId.replace('\\', '/');
         if (normalized.startsWith("file/")) {
            normalized = normalized.substring("file/".length());
         }

         while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
         }

         while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
         }

         if (normalized.isBlank()) {
            return null;
         } else {
            int slash = normalized.indexOf(47);
            String packName = slash >= 0 ? normalized.substring(0, slash) : normalized;
            return packName.isBlank() ? null : packName;
         }
      } else {
         return null;
      }
   }

   private static void writePlayerConfigFile(Path path, FbxRigConfig config) throws IOException {
      JsonObject root = readJson(path);
      root.addProperty("voiceUpperLipBone", config.voiceUpperLipBone() == null ? "" : config.voiceUpperLipBone());
      root.addProperty("voiceLowerLipBone", config.voiceLowerLipBone() == null ? "" : config.voiceLowerLipBone());
      root.addProperty("voiceLipPitchFactor", config.voiceLipPitchFactor());
      root.addProperty("voiceLipYawFactor", config.voiceLipYawFactor());
      root.addProperty("voiceLipRollFactor", config.voiceLipRollFactor());
      root.addProperty("voiceLipMaxDegrees", config.voiceLipMaxDegrees());
      Path parent = path.getParent();
      if (parent != null) {
         Files.createDirectories(parent);
      }

      Files.writeString(path, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
   }

   private static JsonObject readJson(Path path) {
      if (!Files.exists(path)) {
         return new JsonObject();
      } else {
         try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            JsonElement parsed = JsonParser.parseString(text);
            if (parsed.isJsonObject()) {
               return parsed.getAsJsonObject();
            }
         } catch (Exception var3) {
         }

         return new JsonObject();
      }
   }

   private static enum BonePickerTarget {
      NONE,
      UPPER,
      LOWER;
   }
}
