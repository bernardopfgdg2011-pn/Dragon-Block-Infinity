package com.brckv2.objfbxloader.client.gui;

import com.brckv2.objfbxloader.PlatformPaths;
import com.brckv2.objfbxloader.client.render.FbxBoneTuning;
import com.brckv2.objfbxloader.client.render.FbxRigConfig;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_490;

public final class HeldItemRigEditorScreen extends class_437 {
   private static final class_2960 PLAYER_ID = class_2960.method_60656("player");
   private static final class_2960 PRIMARY_CONFIG_ID = class_2960.method_60656("configs/entity/player.json");
   private static final class_2960 LEGACY_CONFIG_ID = class_2960.method_60656("configs/player.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final int BONE_OPTION_COUNT = 8;
   private static final String[] EDITABLE_KEYS = new String[]{
      "rightHandOffsetX",
      "rightHandOffsetY",
      "rightHandOffsetZ",
      "rightHandRotX",
      "rightHandRotY",
      "rightHandRotZ",
      "rightHandScale",
      "leftHandOffsetX",
      "leftHandOffsetY",
      "leftHandOffsetZ",
      "leftHandRotX",
      "leftHandRotY",
      "leftHandRotZ",
      "leftHandScale"
   };
   private final class_437 parent;
   private final FbxRigConfig originalConfig;
   private final Map<String, class_342> fieldByKey = new LinkedHashMap<>();
   private class_4185 saveButton;
   private class_4185 handAttachmentToggleButton;
   private class_4185 thirdPersonVisibilityToggleButton;
   private class_4185 rightBoneButton;
   private class_4185 leftBoneButton;
   private class_4185 customItemsButton;
   private class_342 boneSearchField;
   private final List<class_4185> boneOptionButtons = new ArrayList<>();
   private final List<String> boneOptionValues = new ArrayList<>();
   private int labelX;
   private int fieldX;
   private int contentTopY;
   private int rowHeight;
   private int fieldWidth;
   private int spinButtonWidth;
   private int spinGap;
   private int controlsStartY;
   private int pickerX;
   private int pickerY;
   private int pickerWidth;
   private boolean previewDragging = false;
   private double previewLastMouseX = 0.0;
   private double previewLastMouseY = 0.0;
   private float previewLookOffsetX = 0.0F;
   private float previewLookOffsetY = 0.0F;
   private boolean handAttachmentEnabled = true;
   private boolean thirdPersonItemVisible = true;
   private String rightAttachmentBone = "";
   private String leftAttachmentBone = "";
   private HeldItemRigEditorScreen.BonePickerSide activeBonePicker = HeldItemRigEditorScreen.BonePickerSide.NONE;
   private String lastBoneSearch = "";
   private FbxRigConfig lastAppliedConfig;
   private Path targetConfigPath;
   private String targetPackName = "unknown";
   private String lastSerializedFields = "";
   private String statusLine = "";
   private boolean saved = false;

   public HeldItemRigEditorScreen(class_437 parent) {
      super(class_2561.method_43470("Item Options"));
      this.parent = parent;
      FbxRigConfig loaded = ResourcePackObjLivingOverrides.getRigConfigForTarget(PLAYER_ID);
      this.originalConfig = loaded == null ? FbxRigConfig.EMPTY : loaded;
      this.handAttachmentEnabled = this.originalConfig.handAttachmentEnabled();
      this.thirdPersonItemVisible = this.originalConfig.thirdPersonItemVisible();
      this.rightAttachmentBone = sanitizeBoneName(this.originalConfig.rightItemBone(), this.originalConfig.rightHandBone());
      this.leftAttachmentBone = sanitizeBoneName(this.originalConfig.leftItemBone(), this.originalConfig.leftHandBone());
      this.lastAppliedConfig = this.originalConfig;
   }

   protected void method_25426() {
      this.fieldByKey.clear();
      this.boneOptionButtons.clear();
      this.boneOptionValues.clear();
      this.lastBoneSearch = "";
      this.activeBonePicker = HeldItemRigEditorScreen.BonePickerSide.NONE;
      this.fieldWidth = 84;
      this.spinButtonWidth = 16;
      this.spinGap = 2;
      this.rowHeight = 20;
      this.contentTopY = 34;
      int rowControlWidth = this.fieldWidth + this.spinGap + this.spinButtonWidth + this.spinGap + this.spinButtonWidth;
      this.fieldX = Math.max(8, Math.min(this.field_22789 - rowControlWidth - 8, this.field_22789 / 2 + 16));
      this.labelX = Math.max(8, this.fieldX - 240);
      int left = Math.max(8, this.field_22789 / 2 - 170);
      this.controlsStartY = this.contentTopY + EDITABLE_KEYS.length * this.rowHeight + 12;
      this.pickerWidth = 282;
      this.pickerX = left;
      this.pickerY = Math.max(34, this.controlsStartY - 190);

      for (int i = 0; i < EDITABLE_KEYS.length; i++) {
         String key = EDITABLE_KEYS[i];
         int y = this.contentTopY + i * this.rowHeight;
         class_342 field = new class_342(this.field_22793, this.fieldX, y - 2, this.fieldWidth, 18, class_2561.method_43470(key));
         field.method_1852(formatValue(getValue(this.originalConfig, key)));
         field.method_1880(32);
         this.fieldByKey.put(key, field);
         this.method_37063(field);
         int minusX = this.fieldX + this.fieldWidth + this.spinGap;
         int plusX = minusX + this.spinButtonWidth + this.spinGap;
         float step = stepForKey(key);
         this.method_37063(
            class_4185.method_46430(class_2561.method_43470("-"), button -> this.nudgeField(key, -step))
               .method_46434(minusX, y - 2, this.spinButtonWidth, 18)
               .method_46431()
         );
         this.method_37063(
            class_4185.method_46430(class_2561.method_43470("+"), button -> this.nudgeField(key, step))
               .method_46434(plusX, y - 2, this.spinButtonWidth, 18)
               .method_46431()
         );
      }

      this.thirdPersonVisibilityToggleButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.thirdPersonToggleLabel(), button -> this.toggleThirdPersonVisibility())
            .method_46434(left, this.controlsStartY, 138, 20)
            .method_46431()
      );
      this.handAttachmentToggleButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.toggleLabel(), button -> this.toggleHandAttachment())
            .method_46434(left + 144, this.controlsStartY, 138, 20)
            .method_46431()
      );
      this.rightBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.rightBoneButtonLabel(), button -> this.toggleBonePicker(HeldItemRigEditorScreen.BonePickerSide.RIGHT))
            .method_46434(left, this.controlsStartY + 24, 138, 20)
            .method_46431()
      );
      this.leftBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.leftBoneButtonLabel(), button -> this.toggleBonePicker(HeldItemRigEditorScreen.BonePickerSide.LEFT))
            .method_46434(left + 144, this.controlsStartY + 24, 138, 20)
            .method_46431()
      );
      this.customItemsButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43470("Custom Item Transforms"), button -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new CustomItemTransformEditorScreen(this));
         }
      }).method_46434(left, this.controlsStartY + 48, 282, 20).method_46431());
      int buttonY = this.controlsStartY + 74;
      this.saveButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Save"), button -> this.saveAndClose()).method_46434(left, buttonY, 90, 20).method_46431()
      );
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Cancel"), button -> this.cancelAndClose()).method_46434(left + 96, buttonY, 90, 20).method_46431()
      );
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Reset"), button -> this.resetToOriginal()).method_46434(left + 192, buttonY, 90, 20).method_46431()
      );
      this.boneSearchField = (class_342)this.method_37063(
         new class_342(this.field_22793, this.pickerX, this.pickerY + 18, this.pickerWidth, 18, class_2561.method_43470("Search bones"))
      );
      this.boneSearchField.method_1880(64);
      this.boneSearchField.method_1852("");
      this.boneSearchField.field_22764 = false;
      this.boneSearchField.field_22763 = false;
      this.boneSearchField.method_25365(false);

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

      this.targetConfigPath = this.resolveEditablePlayerConfigPath();
      if (this.targetConfigPath == null) {
         this.statusLine = "No editable resource-pack player.json found.";
         this.saveButton.field_22763 = false;
      } else {
         this.statusLine = "Editing pack: " + this.targetPackName;
         this.saveButton.field_22763 = true;
      }

      this.lastSerializedFields = this.serializeFieldTexts();
      this.closeBonePicker();
      this.applyPreviewFromFields();
   }

   public void method_25393() {
      super.method_25393();
      String serialized = this.serializeFieldTexts();
      if (!serialized.equals(this.lastSerializedFields)) {
         this.lastSerializedFields = serialized;
         this.applyPreviewFromFields();
      }

      if (this.activeBonePicker != HeldItemRigEditorScreen.BonePickerSide.NONE && this.boneSearchField != null) {
         String currentSearch = this.boneSearchField.method_1882() == null ? "" : this.boneSearchField.method_1882();
         if (!currentSearch.equals(this.lastBoneSearch)) {
            this.lastBoneSearch = currentSearch;
            this.refreshBoneOptionButtons();
         }
      }
   }

   public void method_25419() {
      this.closeBonePicker();
      if (!this.saved) {
         ResourcePackObjLivingOverrides.setRigConfigOverrideForTarget(PLAYER_ID, this.originalConfig);
      }

      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   public boolean method_25402(class_11909 click, boolean doubleClick) {
      if (click != null && click.method_74245() == 0 && this.isPointInPreview(click.comp_4798(), click.comp_4799())) {
         this.previewDragging = true;
         this.previewLastMouseX = click.comp_4798();
         this.previewLastMouseY = click.comp_4799();
         this.method_25398(true);
         return true;
      } else {
         return super.method_25402(click, doubleClick);
      }
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      if (this.previewDragging && click != null && click.method_74245() == 0) {
         double x = click.comp_4798();
         double y = click.comp_4799();
         double dx = x - this.previewLastMouseX;
         double dy = y - this.previewLastMouseY;
         this.previewLastMouseX = x;
         this.previewLastMouseY = y;
         this.previewLookOffsetX += (float)dx;
         this.previewLookOffsetY += (float)dy;
         return true;
      } else {
         return super.method_25403(click, deltaX, deltaY);
      }
   }

   public boolean method_25406(class_11909 click) {
      if (this.previewDragging && click != null && click.method_74245() == 0) {
         this.previewDragging = false;
         this.method_25398(false);
         return true;
      } else {
         return super.method_25406(click);
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      context.method_25294(0, 0, this.field_22789, this.field_22790, -1072689136);
      this.renderPlayerPreview(context);
      this.renderBonePickerPanel(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int left = Math.max(8, this.field_22789 / 2 - 170);
      context.method_51439(this.field_22793, this.field_22785, left, 12, -1, false);
      context.method_51439(
         this.field_22793,
         class_2561.method_43470("Live preview updates immediately. Offset step 0.01, Rot step 1, Scale step 0.05."),
         left,
         22,
         -6250336,
         false
      );
      context.method_51439(this.field_22793, class_2561.method_43470("Right hand"), this.labelX, this.contentTopY - 10, -5711617, false);

      for (int i = 0; i < EDITABLE_KEYS.length; i++) {
         if (i == 7) {
            context.method_51439(
               this.field_22793, class_2561.method_43470("Left hand"), this.labelX, this.contentTopY + i * this.rowHeight - 10, -5701680, false
            );
         }

         String key = EDITABLE_KEYS[i];
         context.method_51439(
            this.field_22793, class_2561.method_43470(labelForKey(key)), this.labelX, this.contentTopY + i * this.rowHeight + 2, -2039584, false
         );
      }

      context.method_51439(this.field_22793, class_2561.method_43470("Item Settings"), left, this.controlsStartY - 10, -1, false);
      if (!this.statusLine.isEmpty()) {
         context.method_51439(this.field_22793, class_2561.method_43470(this.statusLine), left, this.controlsStartY + 76, -6226000, false);
      }
   }

   private void renderPlayerPreview(class_332 context) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         HeldItemRigEditorScreen.PreviewBounds bounds = this.computePreviewBounds();
         if (bounds != null) {
            context.method_25294(bounds.left() - 2, bounds.top() - 2, bounds.right() + 2, bounds.bottom() + 2, -1442840576);
            context.method_73198(bounds.left() - 2, bounds.top() - 2, bounds.right() - bounds.left() + 4, bounds.bottom() - bounds.top() + 4, -7303024);
            context.method_51439(this.field_22793, class_2561.method_43470("Live Preview"), bounds.left(), bounds.top() - 13, -1, false);
            int width = bounds.right() - bounds.left();
            int height = bounds.bottom() - bounds.top();
            int size = Math.max(26, Math.min(width, height) / 2);
            float yOffset = 0.02F;
            float centerX = (bounds.left() + bounds.right()) / 2.0F;
            float centerY = (bounds.top() + bounds.bottom()) / 2.0F;
            float lookX = centerX + this.previewLookOffsetX;
            float lookY = centerY + this.previewLookOffsetY;
            class_490.method_2486(
               context, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), size, yOffset, lookX, lookY, this.field_22787.field_1724
            );
         }
      }
   }

   private void renderBonePickerPanel(class_332 context) {
      if (this.activeBonePicker != HeldItemRigEditorScreen.BonePickerSide.NONE && this.boneSearchField != null && this.boneSearchField.field_22764) {
         int panelTop = this.pickerY;
         int panelBottom = this.pickerY + 42 + 160;
         context.method_25294(this.pickerX - 2, panelTop - 2, this.pickerX + this.pickerWidth + 2, panelBottom + 2, -872415232);
         context.method_73198(this.pickerX - 2, panelTop - 2, this.pickerWidth + 4, panelBottom - panelTop + 4, -7303024);
         context.method_51439(
            this.field_22793,
            class_2561.method_43470(this.activeBonePicker == HeldItemRigEditorScreen.BonePickerSide.RIGHT ? "Right Attachment Bone" : "Left Attachment Bone"),
            this.pickerX,
            panelTop + 4,
            -1,
            false
         );
         context.method_51439(this.field_22793, class_2561.method_43470("Search and click a bone"), this.pickerX + 142, panelTop + 4, -6250336, false);
      }
   }

   private void toggleBonePicker(HeldItemRigEditorScreen.BonePickerSide side) {
      if (side == null || side == HeldItemRigEditorScreen.BonePickerSide.NONE) {
         this.closeBonePicker();
      } else if (this.activeBonePicker == side) {
         this.closeBonePicker();
      } else {
         this.activeBonePicker = side;
         if (this.boneSearchField != null) {
            this.boneSearchField.field_22764 = true;
            this.boneSearchField.field_22763 = true;
            this.boneSearchField.method_1852("");
            this.boneSearchField.method_25365(true);
            this.method_25395(this.boneSearchField);
            this.lastBoneSearch = "";
         }

         this.refreshBoneOptionButtons();
      }
   }

   private void closeBonePicker() {
      this.activeBonePicker = HeldItemRigEditorScreen.BonePickerSide.NONE;
      if (this.boneSearchField != null) {
         this.boneSearchField.field_22764 = false;
         this.boneSearchField.field_22763 = false;
         this.boneSearchField.method_25365(false);
      }

      for (class_4185 button : this.boneOptionButtons) {
         button.field_22764 = false;
         button.field_22763 = false;
         button.method_25355(class_2561.method_43470(""));
      }

      this.boneOptionValues.clear();
   }

   private void refreshBoneOptionButtons() {
      if (this.activeBonePicker != HeldItemRigEditorScreen.BonePickerSide.NONE && this.boneSearchField != null) {
         List<String> allBones = this.resolveAvailableBones();
         String query = this.boneSearchField.method_1882() == null ? "" : this.boneSearchField.method_1882().trim().toLowerCase(Locale.ROOT);
         List<String> matches = new ArrayList<>();

         for (String bone : allBones) {
            if (query.isBlank() || bone.toLowerCase(Locale.ROOT).contains(query)) {
               matches.add(bone);
            }
         }

         this.boneOptionValues.clear();
         int limit = Math.min(8, matches.size());

         for (int i = 0; i < limit; i++) {
            this.boneOptionValues.add(matches.get(i));
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

   private void selectBoneOption(int optionIndex) {
      if (optionIndex >= 0 && optionIndex < this.boneOptionValues.size()) {
         String selectedBone = this.boneOptionValues.get(optionIndex);
         if (this.activeBonePicker == HeldItemRigEditorScreen.BonePickerSide.RIGHT) {
            this.rightAttachmentBone = selectedBone;
            if (this.rightBoneButton != null) {
               this.rightBoneButton.method_25355(this.rightBoneButtonLabel());
            }
         } else if (this.activeBonePicker == HeldItemRigEditorScreen.BonePickerSide.LEFT) {
            this.leftAttachmentBone = selectedBone;
            if (this.leftBoneButton != null) {
               this.leftBoneButton.method_25355(this.leftBoneButtonLabel());
            }
         }

         this.applyPreviewFromFields();
      }
   }

   private List<String> resolveAvailableBones() {
      LinkedHashSet<String> unique = new LinkedHashSet<>();

      for (String bone : FbxBoneTuning.getKnownBones()) {
         if (bone != null && !bone.isBlank()) {
            unique.add(bone);
         }
      }

      addIfNotBlank(unique, this.rightAttachmentBone);
      addIfNotBlank(unique, this.leftAttachmentBone);
      addIfNotBlank(unique, this.originalConfig.rightItemBone());
      addIfNotBlank(unique, this.originalConfig.leftItemBone());
      addIfNotBlank(unique, this.originalConfig.rightHandBone());
      addIfNotBlank(unique, this.originalConfig.leftHandBone());
      List<String> list = new ArrayList<>(unique);
      list.sort(String::compareToIgnoreCase);
      return list;
   }

   private static void addIfNotBlank(LinkedHashSet<String> set, String value) {
      if (value != null && !value.isBlank()) {
         set.add(value);
      }
   }

   private boolean isPointInPreview(double x, double y) {
      HeldItemRigEditorScreen.PreviewBounds bounds = this.computePreviewBounds();
      return bounds == null ? false : x >= bounds.left() && x <= bounds.right() && y >= bounds.top() && y <= bounds.bottom();
   }

   private HeldItemRigEditorScreen.PreviewBounds computePreviewBounds() {
      int top = Math.max(34, this.contentTopY - 8);
      int bottom = this.contentTopY + EDITABLE_KEYS.length * this.rowHeight + 2;
      int right = Math.max(132, this.labelX - 12);
      int left = Math.max(8, right - 190);
      return right - left >= 90 && bottom - top >= 90 ? new HeldItemRigEditorScreen.PreviewBounds(left, top, right, bottom) : null;
   }

   private void resetToOriginal() {
      for (String key : EDITABLE_KEYS) {
         class_342 field = this.fieldByKey.get(key);
         if (field != null) {
            field.method_1852(formatValue(getValue(this.originalConfig, key)));
         }
      }

      this.handAttachmentEnabled = this.originalConfig.handAttachmentEnabled();
      this.thirdPersonItemVisible = this.originalConfig.thirdPersonItemVisible();
      this.rightAttachmentBone = sanitizeBoneName(this.originalConfig.rightItemBone(), this.originalConfig.rightHandBone());
      this.leftAttachmentBone = sanitizeBoneName(this.originalConfig.leftItemBone(), this.originalConfig.leftHandBone());
      if (this.handAttachmentToggleButton != null) {
         this.handAttachmentToggleButton.method_25355(this.toggleLabel());
      }

      if (this.thirdPersonVisibilityToggleButton != null) {
         this.thirdPersonVisibilityToggleButton.method_25355(this.thirdPersonToggleLabel());
      }

      if (this.rightBoneButton != null) {
         this.rightBoneButton.method_25355(this.rightBoneButtonLabel());
      }

      if (this.leftBoneButton != null) {
         this.leftBoneButton.method_25355(this.leftBoneButtonLabel());
      }

      this.closeBonePicker();
      this.lastSerializedFields = this.serializeFieldTexts();
      this.applyPreviewFromFields();
      this.statusLine = "Reset to values loaded from player.json.";
   }

   private void toggleHandAttachment() {
      this.handAttachmentEnabled = !this.handAttachmentEnabled;
      if (this.handAttachmentToggleButton != null) {
         this.handAttachmentToggleButton.method_25355(this.toggleLabel());
      }

      this.applyPreviewFromFields();
   }

   private void toggleThirdPersonVisibility() {
      this.thirdPersonItemVisible = !this.thirdPersonItemVisible;
      if (this.thirdPersonVisibilityToggleButton != null) {
         this.thirdPersonVisibilityToggleButton.method_25355(this.thirdPersonToggleLabel());
      }

      this.applyPreviewFromFields();
   }

   private void nudgeField(String key, float delta) {
      class_342 field = this.fieldByKey.get(key);
      if (field != null) {
         float current = this.parseFloat(key, getValue(this.lastAppliedConfig, key));
         float next = current + delta;
         if (key.endsWith("Scale")) {
            next = Math.max(0.01F, next);
         }

         if (!Float.isFinite(next)) {
            next = current;
         }

         field.method_1852(formatValue(next));
         this.lastSerializedFields = this.serializeFieldTexts();
         this.applyPreviewFromFields();
      }
   }

   private void applyPreviewFromFields() {
      FbxRigConfig preview = this.buildConfigFromFields(this.lastAppliedConfig);
      this.lastAppliedConfig = preview;
      ResourcePackObjLivingOverrides.setRigConfigOverrideForTarget(PLAYER_ID, preview);
   }

   private void cancelAndClose() {
      this.saved = false;
      this.method_25419();
   }

   private void saveAndClose() {
      this.applyPreviewFromFields();
      if (this.targetConfigPath == null) {
         this.statusLine = "Cannot save: no editable pack file found.";
      } else {
         try {
            writeConfigFile(this.targetConfigPath, this.lastAppliedConfig);
            this.saved = true;
            if (this.field_22787 != null && this.field_22787.field_1724 != null) {
               this.field_22787
                  .field_1724
                  .method_7353(class_2561.method_43470("Saved held-item rig settings to " + this.targetPackName + " player.json"), false);
            }

            this.method_25419();
         } catch (IOException var2) {
            this.statusLine = "Save failed: " + var2.getMessage();
         }
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

         if (resource == null) {
            return null;
         } else {
            String packName = extractPackName(resource.method_14480());
            if (packName != null && !packName.isBlank()) {
               Path path = PlatformPaths.gameDir().resolve("resourcepacks").resolve(packName);

               for (String part : relativePath.split("/")) {
                  path = path.resolve(part);
               }

               this.targetPackName = packName;
               return path;
            } else {
               return null;
            }
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

   private static void writeConfigFile(Path path, FbxRigConfig config) throws IOException {
      JsonObject root = readJson(path);
      writeString(root, "headBone", config.headBone());
      writeString(root, "neckBone", config.neckBone());
      writeString(root, "rightHandBone", config.rightHandBone());
      writeString(root, "leftHandBone", config.leftHandBone());
      writeString(root, "rightArmBone", config.rightArmBone());
      writeString(root, "leftArmBone", config.leftArmBone());
      writeString(root, "rightItemBone", config.rightItemBone());
      writeString(root, "leftItemBone", config.leftItemBone());
      writeFloat(root, "rightHandOffsetX", config.rightHandOffsetX());
      writeFloat(root, "rightHandOffsetY", config.rightHandOffsetY());
      writeFloat(root, "rightHandOffsetZ", config.rightHandOffsetZ());
      writeFloat(root, "rightHandRotX", config.rightHandRotX());
      writeFloat(root, "rightHandRotY", config.rightHandRotY());
      writeFloat(root, "rightHandRotZ", config.rightHandRotZ());
      writeFloat(root, "rightHandScale", config.rightHandScale());
      writeFloat(root, "leftHandOffsetX", config.leftHandOffsetX());
      writeFloat(root, "leftHandOffsetY", config.leftHandOffsetY());
      writeFloat(root, "leftHandOffsetZ", config.leftHandOffsetZ());
      writeFloat(root, "leftHandRotX", config.leftHandRotX());
      writeFloat(root, "leftHandRotY", config.leftHandRotY());
      writeFloat(root, "leftHandRotZ", config.leftHandRotZ());
      writeFloat(root, "leftHandScale", config.leftHandScale());
      root.addProperty("handAttachmentEnabled", config.handAttachmentEnabled());
      root.addProperty("thirdPersonItemVisible", config.thirdPersonItemVisible());
      writeString(root, "attackStopBone", config.attackStopBone());
      writeFloat(root, "headLookPitchFactor", config.headLookPitchFactor());
      writeFloat(root, "headLookYawFactor", config.headLookYawFactor());
      writeFloat(root, "neckLookFactor", config.neckLookFactor());
      writeString(root, "voiceUpperLipBone", config.voiceUpperLipBone());
      writeString(root, "voiceLowerLipBone", config.voiceLowerLipBone());
      writeFloat(root, "voiceLipPitchFactor", config.voiceLipPitchFactor());
      writeFloat(root, "voiceLipYawFactor", config.voiceLipYawFactor());
      writeFloat(root, "voiceLipRollFactor", config.voiceLipRollFactor());
      writeFloat(root, "voiceLipMaxDegrees", config.voiceLipMaxDegrees());
      root.remove("upperTorsoBone");
      root.remove("torsoLookBone");
      root.remove("torsoLookPitchFactor");
      root.remove("torsoLookYawFactor");
      root.addProperty("attackUpperBodyOnly", config.attackUpperBodyOnly());
      writeFloat(root, "entityOffsetX", config.entityOffsetX());
      writeFloat(root, "entityOffsetY", config.entityOffsetY());
      writeFloat(root, "entityOffsetZ", config.entityOffsetZ());
      writeFloat(root, "entityRotX", config.entityRotX());
      writeFloat(root, "entityRotY", config.entityRotY());
      writeFloat(root, "entityRotZ", config.entityRotZ());
      writeFloat(root, "entityScale", config.entityScale());
      writeAnimationSpeedMultipliers(root, config.animationSpeedMultipliers());
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

   private static void writeString(JsonObject root, String key, String value) {
      root.addProperty(key, value == null ? "" : value);
   }

   private static void writeFloat(JsonObject root, String key, float value) {
      root.addProperty(key, Float.isFinite(value) ? value : 0.0F);
   }

   private static void writeAnimationSpeedMultipliers(JsonObject root, Map<String, Float> multipliers) {
      JsonObject object = new JsonObject();
      if (multipliers != null) {
         for (Entry<String, Float> entry : multipliers.entrySet()) {
            if (entry != null && entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               float value = entry.getValue();
               if (Float.isFinite(value)) {
                  object.addProperty(entry.getKey(), value);
               }
            }
         }
      }

      root.add("animationSpeedMultipliers", object);
   }

   private FbxRigConfig buildConfigFromFields(FbxRigConfig fallback) {
      FbxRigConfig base = fallback == null ? this.originalConfig : fallback;
      float rightHandOffsetX = this.parseFloat("rightHandOffsetX", base.rightHandOffsetX());
      float rightHandOffsetY = this.parseFloat("rightHandOffsetY", base.rightHandOffsetY());
      float rightHandOffsetZ = this.parseFloat("rightHandOffsetZ", base.rightHandOffsetZ());
      float rightHandRotX = this.parseFloat("rightHandRotX", base.rightHandRotX());
      float rightHandRotY = this.parseFloat("rightHandRotY", base.rightHandRotY());
      float rightHandRotZ = this.parseFloat("rightHandRotZ", base.rightHandRotZ());
      float rightHandScale = this.parseFloat("rightHandScale", base.rightHandScale());
      float leftHandOffsetX = this.parseFloat("leftHandOffsetX", base.leftHandOffsetX());
      float leftHandOffsetY = this.parseFloat("leftHandOffsetY", base.leftHandOffsetY());
      float leftHandOffsetZ = this.parseFloat("leftHandOffsetZ", base.leftHandOffsetZ());
      float leftHandRotX = this.parseFloat("leftHandRotX", base.leftHandRotX());
      float leftHandRotY = this.parseFloat("leftHandRotY", base.leftHandRotY());
      float leftHandRotZ = this.parseFloat("leftHandRotZ", base.leftHandRotZ());
      float leftHandScale = this.parseFloat("leftHandScale", base.leftHandScale());
      return new FbxRigConfig(
         this.originalConfig.headBone(),
         this.originalConfig.neckBone(),
         this.originalConfig.rightHandBone(),
         this.originalConfig.leftHandBone(),
         this.originalConfig.upperTorsoBone(),
         this.originalConfig.rightArmBone(),
         this.originalConfig.leftArmBone(),
         sanitizeBoneName(this.rightAttachmentBone, this.originalConfig.rightHandBone()),
         sanitizeBoneName(this.leftAttachmentBone, this.originalConfig.leftHandBone()),
         rightHandOffsetX,
         rightHandOffsetY,
         rightHandOffsetZ,
         rightHandRotX,
         rightHandRotY,
         rightHandRotZ,
         rightHandScale,
         leftHandOffsetX,
         leftHandOffsetY,
         leftHandOffsetZ,
         leftHandRotX,
         leftHandRotY,
         leftHandRotZ,
         leftHandScale,
         this.handAttachmentEnabled,
         this.thirdPersonItemVisible,
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
         base.customItemTransforms()
      );
   }

   private float parseFloat(String key, float fallback) {
      class_342 field = this.fieldByKey.get(key);
      if (field == null) {
         return fallback;
      } else {
         String raw = field.method_1882();
         if (raw == null) {
            return fallback;
         } else {
            String normalized = raw.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty() && !normalized.equals("-") && !normalized.equals("+") && !normalized.equals(".")) {
               try {
                  float parsed = Float.parseFloat(normalized);
                  return Float.isFinite(parsed) ? parsed : fallback;
               } catch (NumberFormatException var7) {
                  return fallback;
               }
            } else {
               return fallback;
            }
         }
      }
   }

   private static float getValue(FbxRigConfig config, String key) {
      return switch (key) {
         case "rightHandOffsetX" -> config.rightHandOffsetX();
         case "rightHandOffsetY" -> config.rightHandOffsetY();
         case "rightHandOffsetZ" -> config.rightHandOffsetZ();
         case "rightHandRotX" -> config.rightHandRotX();
         case "rightHandRotY" -> config.rightHandRotY();
         case "rightHandRotZ" -> config.rightHandRotZ();
         case "rightHandScale" -> config.rightHandScale();
         case "leftHandOffsetX" -> config.leftHandOffsetX();
         case "leftHandOffsetY" -> config.leftHandOffsetY();
         case "leftHandOffsetZ" -> config.leftHandOffsetZ();
         case "leftHandRotX" -> config.leftHandRotX();
         case "leftHandRotY" -> config.leftHandRotY();
         case "leftHandRotZ" -> config.leftHandRotZ();
         case "leftHandScale" -> config.leftHandScale();
         default -> 0.0F;
      };
   }

   private String serializeFieldTexts() {
      StringBuilder builder = new StringBuilder();

      for (String key : EDITABLE_KEYS) {
         class_342 field = this.fieldByKey.get(key);
         builder.append(key).append('=');
         builder.append(field == null ? "" : field.method_1882());
         builder.append(';');
      }

      return builder.toString();
   }

   private static String formatValue(float value) {
      if (!Float.isFinite(value)) {
         return "0";
      } else {
         float rounded = Math.round(value * 1000.0F) / 1000.0F;
         return Math.abs(rounded - Math.round(rounded)) < 1.0E-4F ? Integer.toString(Math.round(rounded)) : Float.toString(rounded);
      }
   }

   private class_2561 toggleLabel() {
      return class_2561.method_43470("Hand Attachment: " + (this.handAttachmentEnabled ? "ON" : "OFF"));
   }

   private class_2561 thirdPersonToggleLabel() {
      return class_2561.method_43470("Third-Person Item: " + (this.thirdPersonItemVisible ? "ON" : "OFF"));
   }

   private class_2561 rightBoneButtonLabel() {
      return class_2561.method_43470("Right Bone: " + shortenBone(this.rightAttachmentBone));
   }

   private class_2561 leftBoneButtonLabel() {
      return class_2561.method_43470("Left Bone: " + shortenBone(this.leftAttachmentBone));
   }

   private static String shortenBone(String boneName) {
      if (boneName != null && !boneName.isBlank()) {
         return boneName.length() <= 22 ? boneName : boneName.substring(0, 19) + "...";
      } else {
         return "(none)";
      }
   }

   private static String sanitizeBoneName(String boneName, String fallback) {
      if (boneName != null && !boneName.isBlank()) {
         return boneName;
      } else {
         return fallback == null ? "" : fallback;
      }
   }

   private static float stepForKey(String key) {
      if (key == null || key.isBlank()) {
         return 0.01F;
      } else if (key.contains("Rot")) {
         return 1.0F;
      } else {
         return key.endsWith("Scale") ? 0.05F : 0.01F;
      }
   }

   private static String labelForKey(String key) {
      return switch (key) {
         case "rightHandOffsetX" -> "Offset X";
         case "rightHandOffsetY" -> "Offset Y";
         case "rightHandOffsetZ" -> "Offset Z";
         case "rightHandRotX" -> "Rotation X (deg)";
         case "rightHandRotY" -> "Rotation Y (deg)";
         case "rightHandRotZ" -> "Rotation Z (deg)";
         case "rightHandScale" -> "Scale";
         case "leftHandOffsetX" -> "Offset X";
         case "leftHandOffsetY" -> "Offset Y";
         case "leftHandOffsetZ" -> "Offset Z";
         case "leftHandRotX" -> "Rotation X (deg)";
         case "leftHandRotY" -> "Rotation Y (deg)";
         case "leftHandRotZ" -> "Rotation Z (deg)";
         case "leftHandScale" -> "Scale";
         default -> key;
      };
   }

   private static enum BonePickerSide {
      NONE,
      RIGHT,
      LEFT;
   }

   private record PreviewBounds(int left, int top, int right, int bottom) {
   }
}
