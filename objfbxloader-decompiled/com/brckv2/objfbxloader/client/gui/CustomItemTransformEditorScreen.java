package com.brckv2.objfbxloader.client.gui;

import com.brckv2.objfbxloader.PlatformPaths;
import com.brckv2.objfbxloader.client.render.FbxItemTransform;
import com.brckv2.objfbxloader.client.render.FbxRigConfig;
import com.brckv2.objfbxloader.client.render.ResourcePackObjItemModel;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7923;

public final class CustomItemTransformEditorScreen extends class_437 {
   private static final class_2960 PLAYER_ID = class_2960.method_60656("player");
   private static final class_2960 PRIMARY_CONFIG_ID = class_2960.method_60656("configs/entity/player.json");
   private static final class_2960 LEGACY_CONFIG_ID = class_2960.method_60656("configs/player.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private final class_437 parent;
   private final List<class_2960> customItems = new ArrayList<>();
   private int selectedIndex = 0;
   private Path targetConfigPath;
   private String targetPackName = "unknown";
   private String statusLine = "";
   private class_4185 itemDropdownButton;
   private final List<class_4185> itemOptionButtons = new ArrayList<>();
   private boolean itemDropdownOpen = false;
   private static final int MAX_ITEM_OPTIONS = 8;
   private class_342 rightOffsetXField;
   private class_342 rightOffsetYField;
   private class_342 rightOffsetZField;
   private class_342 rightRotXField;
   private class_342 rightRotYField;
   private class_342 rightRotZField;
   private class_342 rightScaleField;
   private class_342 leftOffsetXField;
   private class_342 leftOffsetYField;
   private class_342 leftOffsetZField;
   private class_342 leftRotXField;
   private class_342 leftRotYField;
   private class_342 leftRotZField;
   private class_342 leftScaleField;

   public CustomItemTransformEditorScreen(class_437 parent) {
      super(class_2561.method_43470("Custom Item Transform Editor"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.targetConfigPath = this.resolveEditablePlayerConfigPath();
      this.collectCustomItemsInUse();
      int left = Math.max(8, this.field_22789 / 2 - 170);
      int y = 34;
      this.itemDropdownButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.itemDropdownLabel(), b -> this.toggleItemDropdown()).method_46434(left, y, 170, 20).method_46431()
      );
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Save"), b -> this.saveCurrent()).method_46434(left + 174, y, 52, 20).method_46431());
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Reset"), b -> this.loadSelectedItemFields()).method_46434(left + 230, y, 52, 20).method_46431()
      );
      this.itemOptionButtons.clear();

      for (int i = 0; i < 8; i++) {
         int optionIndex = i;
         class_4185 option = (class_4185)this.method_37063(
            class_4185.method_46430(class_2561.method_43470(""), b -> this.selectDropdownIndex(optionIndex))
               .method_46434(left, y + 22 + i * 20, 170, 18)
               .method_46431()
         );
         option.field_22764 = false;
         option.field_22763 = false;
         this.itemOptionButtons.add(option);
      }

      int fieldX = left + 114;
      int row = y + 30;
      this.rightOffsetXField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Offset X");
      row += 20;
      this.rightOffsetYField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Offset Y");
      row += 20;
      this.rightOffsetZField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Offset Z");
      row += 20;
      this.rightRotXField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Rot X");
      row += 20;
      this.rightRotYField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Rot Y");
      row += 20;
      this.rightRotZField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Rot Z");
      row += 20;
      this.rightScaleField = this.addField(fieldX, row);
      this.drawLabel(left, row, "Right Scale");
      int rightLabelX = left + 170;
      int rightFieldX = rightLabelX + 108;
      row = y + 30;
      this.leftOffsetXField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Offset X");
      row += 20;
      this.leftOffsetYField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Offset Y");
      row += 20;
      this.leftOffsetZField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Offset Z");
      row += 20;
      this.leftRotXField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Rot X");
      row += 20;
      this.leftRotYField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Rot Y");
      row += 20;
      this.leftRotZField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Rot Z");
      row += 20;
      this.leftScaleField = this.addField(rightFieldX, row);
      this.drawLabel(rightLabelX, row, "Left Scale");
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Back"), b -> this.method_25419()).method_46434(left, y + 180, 282, 20).method_46431());
      this.loadSelectedItemFields();
   }

   private class_342 addField(int x, int y) {
      class_342 field = new class_342(this.field_22793, x, y - 2, 52, 18, class_2561.method_43470(""));
      field.method_1880(24);
      this.method_37063(field);
      return field;
   }

   private void drawLabel(int x, int y, String label) {
   }

   private void collectCustomItemsInUse() {
      this.customItems.clear();
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         LinkedHashSet<class_2960> found = new LinkedHashSet<>();
         class_1661 inventory = this.field_22787.field_1724.method_31548();

         for (int i = 0; i < inventory.method_5439(); i++) {
            addIfCustom(found, inventory.method_5438(i));
         }

         addIfCustom(found, this.field_22787.field_1724.method_6047());
         addIfCustom(found, this.field_22787.field_1724.method_6079());
         this.customItems.addAll(found);
         this.customItems.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));
         if (this.selectedIndex >= this.customItems.size()) {
            this.selectedIndex = Math.max(0, this.customItems.size() - 1);
         }
      }
   }

   private static void addIfCustom(LinkedHashSet<class_2960> out, class_1799 stack) {
      if (stack != null && !stack.method_7960() && stack.method_7909() != null) {
         class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
         if (id != null && ResourcePackObjItemModel.isCustomModelItem(id)) {
            out.add(id);
         }
      }
   }

   private void loadSelectedItemFields() {
      FbxItemTransform t = this.currentTransform();
      if (t == null) {
         t = FbxItemTransform.IDENTITY;
      }

      set(this.rightOffsetXField, t.rightOffsetX());
      set(this.rightOffsetYField, t.rightOffsetY());
      set(this.rightOffsetZField, t.rightOffsetZ());
      set(this.rightRotXField, t.rightRotX());
      set(this.rightRotYField, t.rightRotY());
      set(this.rightRotZField, t.rightRotZ());
      set(this.rightScaleField, t.rightScale());
      set(this.leftOffsetXField, t.leftOffsetX());
      set(this.leftOffsetYField, t.leftOffsetY());
      set(this.leftOffsetZField, t.leftOffsetZ());
      set(this.leftRotXField, t.leftRotX());
      set(this.leftRotYField, t.leftRotY());
      set(this.leftRotZField, t.leftRotZ());
      set(this.leftScaleField, t.leftScale());
      if (this.itemDropdownButton != null) {
         this.itemDropdownButton.method_25355(this.itemDropdownLabel());
      }
   }

   private void saveCurrent() {
      class_2960 selected = this.currentItemId();
      if (selected == null) {
         this.statusLine = "No custom item detected in your inventory/hands.";
      } else if (this.targetConfigPath == null) {
         this.statusLine = "No editable resource-pack player.json found.";
      } else {
         FbxRigConfig base = ResourcePackObjLivingOverrides.getRigConfigForTarget(PLAYER_ID);
         if (base == null) {
            base = FbxRigConfig.EMPTY;
         }

         Map<String, FbxItemTransform> nextMap = new HashMap<>(base.customItemTransforms());
         nextMap.put(
            selected.toString().toLowerCase(Locale.ROOT),
            new FbxItemTransform(
               parse(this.rightOffsetXField, 0.0F),
               parse(this.rightOffsetYField, 0.0F),
               parse(this.rightOffsetZField, 0.0F),
               parse(this.rightRotXField, 0.0F),
               parse(this.rightRotYField, 0.0F),
               parse(this.rightRotZField, 0.0F),
               parse(this.rightScaleField, 1.0F),
               parse(this.leftOffsetXField, 0.0F),
               parse(this.leftOffsetYField, 0.0F),
               parse(this.leftOffsetZField, 0.0F),
               parse(this.leftRotXField, 0.0F),
               parse(this.leftRotYField, 0.0F),
               parse(this.leftRotZField, 0.0F),
               parse(this.leftScaleField, 1.0F)
            )
         );
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
            base.voiceUpperLipBone(),
            base.voiceLowerLipBone(),
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
            Map.copyOf(nextMap)
         );
         ResourcePackObjLivingOverrides.setRigConfigOverrideForTarget(PLAYER_ID, next);

         try {
            writePlayerConfigFile(this.targetConfigPath, next);
            this.statusLine = "Saved custom transform for " + selected + " to " + this.targetPackName + ".";
         } catch (IOException var6) {
            this.statusLine = "Save failed: " + var6.getMessage();
         }
      }
   }

   private class_2960 currentItemId() {
      return !this.customItems.isEmpty() && this.selectedIndex >= 0 && this.selectedIndex < this.customItems.size()
         ? this.customItems.get(this.selectedIndex)
         : null;
   }

   private FbxItemTransform currentTransform() {
      class_2960 id = this.currentItemId();
      if (id == null) {
         return null;
      } else {
         FbxRigConfig config = ResourcePackObjLivingOverrides.getRigConfigForTarget(PLAYER_ID);
         return config == null ? null : config.customItemTransform(id);
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      context.method_25294(0, 0, this.field_22789, this.field_22790, -1072689136);
      super.method_25394(context, mouseX, mouseY, delta);
      int left = Math.max(8, this.field_22789 / 2 - 170);
      context.method_51439(this.field_22793, this.field_22785, left, 12, -1, false);
      String selected = this.currentItemId() == null ? "(none)" : this.currentItemId().toString();
      context.method_51439(
         this.field_22793, class_2561.method_43470("Detected custom items in inventory/hands: " + this.customItems.size()), left, 22, -5197648, false
      );
      context.method_51439(this.field_22793, class_2561.method_43470("Selected: " + selected), left, 56, -1776412, false);
      context.method_51439(this.field_22793, class_2561.method_43470("Per-item transforms apply only to custom-model items."), left, 246, -6643546, false);
      if (!this.statusLine.isBlank()) {
         context.method_51439(this.field_22793, class_2561.method_43470(this.statusLine), left, 260, -6226000, false);
      }
   }

   private class_2561 itemDropdownLabel() {
      class_2960 current = this.currentItemId();
      String text = current == null ? "Select Item (none)" : current.toString();
      if (text.length() > 24) {
         text = text.substring(0, 21) + "...";
      }

      return class_2561.method_43470("Item: " + text + (this.itemDropdownOpen ? " ^" : " v"));
   }

   private void toggleItemDropdown() {
      this.itemDropdownOpen = !this.itemDropdownOpen;
      this.refreshItemDropdownButtons();
      if (this.itemDropdownButton != null) {
         this.itemDropdownButton.method_25355(this.itemDropdownLabel());
      }
   }

   private void refreshItemDropdownButtons() {
      for (int i = 0; i < this.itemOptionButtons.size(); i++) {
         class_4185 button = this.itemOptionButtons.get(i);
         if (this.itemDropdownOpen && i < this.customItems.size()) {
            class_2960 id = this.customItems.get(i);
            String label = id.toString();
            if (label.length() > 24) {
               label = label.substring(0, 21) + "...";
            }

            button.method_25355(class_2561.method_43470((i == this.selectedIndex ? "> " : "") + label));
            button.field_22764 = true;
            button.field_22763 = true;
         } else {
            button.field_22764 = false;
            button.field_22763 = false;
            button.method_25355(class_2561.method_43470(""));
         }
      }
   }

   private void selectDropdownIndex(int optionIndex) {
      if (optionIndex >= 0 && optionIndex < this.customItems.size()) {
         this.selectedIndex = optionIndex;
         this.itemDropdownOpen = false;
         this.refreshItemDropdownButtons();
         this.loadSelectedItemFields();
      }
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   private static void set(class_342 field, float value) {
      if (field != null) {
         field.method_1852(format(value));
      }
   }

   private static void setVisible(class_339 widget, boolean visible) {
      if (widget != null) {
         widget.field_22764 = visible;
         widget.field_22763 = visible;
      }
   }

   private static float parse(class_342 field, float fallback) {
      if (field == null) {
         return fallback;
      } else {
         try {
            float value = Float.parseFloat(field.method_1882().trim());
            return !Float.isFinite(value) ? fallback : value;
         } catch (Exception var3) {
            return fallback;
         }
      }
   }

   private static String format(float value) {
      if (!Float.isFinite(value)) {
         return "0";
      } else {
         float rounded = Math.round(value * 1000.0F) / 1000.0F;
         return Math.abs(rounded - Math.round(rounded)) < 1.0E-4F ? Integer.toString(Math.round(rounded)) : Float.toString(rounded);
      }
   }

   private Path resolveEditablePlayerConfigPath() {
      if (this.field_22787 != null && this.field_22787.method_1478() != null) {
         class_3298 resource = (class_3298)this.field_22787.method_1478().method_14486(PRIMARY_CONFIG_ID).orElse(null);
         String relativePath = "assets/minecraft/configs/entity/player.json";
         if (resource == null) {
            resource = (class_3298)this.field_22787.method_1478().method_14486(LEGACY_CONFIG_ID).orElse(null);
            relativePath = "assets/minecraft/configs/player.json";
         }

         String packName = resource == null ? null : extractPackName(resource.method_14480());
         if (packName != null && !packName.isBlank()) {
            this.targetPackName = packName;
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
      JsonObject transforms = new JsonObject();

      for (Entry<String, FbxItemTransform> entry : config.customItemTransforms().entrySet()) {
         if (entry != null && entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
            FbxItemTransform t = entry.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("rightOffsetX", t.rightOffsetX());
            o.addProperty("rightOffsetY", t.rightOffsetY());
            o.addProperty("rightOffsetZ", t.rightOffsetZ());
            o.addProperty("rightRotX", t.rightRotX());
            o.addProperty("rightRotY", t.rightRotY());
            o.addProperty("rightRotZ", t.rightRotZ());
            o.addProperty("rightScale", t.rightScale());
            o.addProperty("leftOffsetX", t.leftOffsetX());
            o.addProperty("leftOffsetY", t.leftOffsetY());
            o.addProperty("leftOffsetZ", t.leftOffsetZ());
            o.addProperty("leftRotX", t.leftRotX());
            o.addProperty("leftRotY", t.leftRotY());
            o.addProperty("leftRotZ", t.leftRotZ());
            o.addProperty("leftScale", t.leftScale());
            transforms.add(entry.getKey(), o);
         }
      }

      root.add("customItemTransforms", transforms);
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
}
