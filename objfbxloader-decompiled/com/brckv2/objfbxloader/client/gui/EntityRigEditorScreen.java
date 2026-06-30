package com.brckv2.objfbxloader.client.gui;

import com.brckv2.objfbxloader.PlatformPaths;
import com.brckv2.objfbxloader.client.render.FbxRigConfig;
import com.brckv2.objfbxloader.client.render.ResourcePackMeshLoader;
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
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_3730;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_490;
import net.minecraft.class_7923;

public final class EntityRigEditorScreen extends class_437 {
   private static final class_2960 LEGACY_CONFIG_ID = class_2960.method_60656("configs/player.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final int BONE_OPTION_COUNT = 8;
   private static final int MAX_VISIBLE_ANIMATION_FIELDS = 10;
   private static final boolean ATTACK_UPPER_BODY_TOGGLE_ENABLED = false;
   private static final String[] EDITABLE_KEYS = new String[]{
      "headLookPitchFactor",
      "headLookYawFactor",
      "neckLookFactor",
      "voiceLipPitchFactor",
      "voiceLipYawFactor",
      "voiceLipRollFactor",
      "voiceLipMaxDegrees",
      "entityOffsetX",
      "entityOffsetY",
      "entityOffsetZ",
      "entityRotX",
      "entityRotY",
      "entityRotZ",
      "entityScale"
   };
   private final class_437 parent;
   private final class_2960 targetEntityId;
   private final FbxRigConfig originalConfig;
   private final Map<String, class_342> fieldByKey = new LinkedHashMap<>();
   private final Map<String, class_342> animationFieldByName = new LinkedHashMap<>();
   private final List<String> visibleAnimationNames = new ArrayList<>();
   private final List<class_4185> boneOptionButtons = new ArrayList<>();
   private final List<String> boneOptionValues = new ArrayList<>();
   private final List<String> detectedAnimationNames = new ArrayList<>();
   private class_4185 saveButton;
   private class_4185 attackUpperBodyToggleButton;
   private class_4185 headBoneButton;
   private class_4185 neckBoneButton;
   private class_4185 headBoneAdvancedButton;
   private class_4185 neckBoneAdvancedButton;
   private class_4185 attackStopBoneButton;
   private class_342 boneSearchField;
   private int labelX;
   private int fieldX;
   private int contentTopY;
   private int rowHeight;
   private int fieldWidth;
   private int spinButtonWidth;
   private int spinGap;
   private int controlsStartY;
   private int animationSectionStartY;
   private int pickerX;
   private int pickerY;
   private int pickerWidth;
   private boolean previewDragging = false;
   private double previewLastMouseX = 0.0;
   private double previewLastMouseY = 0.0;
   private float previewLookOffsetX = 0.0F;
   private float previewLookOffsetY = 0.0F;
   private boolean attackUpperBodyOnly = true;
   private String headBone = "";
   private String neckBone = "";
   private String torsoBone = "";
   private String attackStopBone = "";
   private EntityRigEditorScreen.BonePickerTarget activeBonePicker = EntityRigEditorScreen.BonePickerTarget.NONE;
   private String lastBoneSearch = "";
   private FbxRigConfig lastAppliedConfig;
   private Path targetConfigPath;
   private String targetPackName = "unknown";
   private String lastSerializedFields = "";
   private String statusLine = "";
   private boolean saved = false;
   private boolean animationsTruncated = false;
   private class_1309 previewEntity;

   public EntityRigEditorScreen(class_437 parent) {
      this(parent, class_2960.method_60656("player"));
   }

   public EntityRigEditorScreen(class_437 parent, class_2960 targetEntityId) {
      super(class_2561.method_43470("Entity Options"));
      this.parent = parent;
      this.targetEntityId = targetEntityId == null ? class_2960.method_60656("player") : targetEntityId;
      FbxRigConfig loaded = ResourcePackObjLivingOverrides.getRigConfigForTarget(this.targetEntityId);
      this.originalConfig = loaded == null ? FbxRigConfig.EMPTY : loaded;
      this.attackUpperBodyOnly = this.originalConfig.attackUpperBodyOnly();
      this.headBone = sanitizeBoneName(this.originalConfig.headBone(), "");
      this.neckBone = sanitizeBoneName(this.originalConfig.neckBone(), "");
      this.torsoBone = sanitizeBoneName(this.originalConfig.resolveTorsoLookBone(), this.originalConfig.upperTorsoBone());
      this.attackStopBone = sanitizeBoneName(this.originalConfig.resolveAttackStopBone(), this.neckBone);
      this.lastAppliedConfig = this.originalConfig;
      this.previewEntity = null;
   }

   protected void method_25426() {
      this.fieldByKey.clear();
      this.animationFieldByName.clear();
      this.visibleAnimationNames.clear();
      this.detectedAnimationNames.clear();
      this.boneOptionButtons.clear();
      this.boneOptionValues.clear();
      this.lastBoneSearch = "";
      this.activeBonePicker = EntityRigEditorScreen.BonePickerTarget.NONE;
      this.animationsTruncated = false;
      this.fieldWidth = 84;
      this.spinButtonWidth = 16;
      this.spinGap = 2;
      this.rowHeight = 20;
      this.contentTopY = 34;
      int rowControlWidth = this.fieldWidth + this.spinGap + this.spinButtonWidth + this.spinGap + this.spinButtonWidth;
      this.fieldX = Math.max(8, Math.min(this.field_22789 - rowControlWidth - 8, this.field_22789 / 2 + 18));
      this.labelX = Math.max(8, this.fieldX - 240);
      int left = Math.max(8, this.field_22789 / 2 - 170);
      this.controlsStartY = this.contentTopY + EDITABLE_KEYS.length * this.rowHeight + 10;
      this.animationSectionStartY = this.controlsStartY + 98;
      this.pickerWidth = 282;
      this.pickerX = left;
      this.pickerY = Math.max(34, this.controlsStartY - 210);

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

      this.headBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.headBoneButtonLabel(), button -> this.toggleBonePicker(EntityRigEditorScreen.BonePickerTarget.HEAD))
            .method_46434(left, this.controlsStartY, 188, 20)
            .method_46431()
      );
      this.headBoneAdvancedButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Advanced"), button -> this.openAdvancedBonePicker(EntityRigEditorScreen.BonePickerTarget.HEAD))
            .method_46434(left + 192, this.controlsStartY, 90, 20)
            .method_46431()
      );
      this.neckBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.neckBoneButtonLabel(), button -> this.toggleBonePicker(EntityRigEditorScreen.BonePickerTarget.NECK))
            .method_46434(left, this.controlsStartY + 24, 188, 20)
            .method_46431()
      );
      this.neckBoneAdvancedButton = (class_4185)this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Advanced"), button -> this.openAdvancedBonePicker(EntityRigEditorScreen.BonePickerTarget.NECK))
            .method_46434(left + 192, this.controlsStartY + 24, 90, 20)
            .method_46431()
      );
      this.attackStopBoneButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.attackStopBoneButtonLabel(), button -> this.toggleBonePicker(EntityRigEditorScreen.BonePickerTarget.ATTACK_STOP))
            .method_46434(left, this.controlsStartY + 48, 282, 20)
            .method_46431()
      );
      this.attackUpperBodyToggleButton = (class_4185)this.method_37063(
         class_4185.method_46430(this.attackToggleLabel(), button -> this.toggleAttackUpperBodyOnly())
            .method_46434(left, this.controlsStartY + 72, 282, 20)
            .method_46431()
      );
      this.attackUpperBodyToggleButton.field_22763 = false;
      List<String> detected = ResourcePackObjLivingOverrides.getAnimationNamesForTarget(this.targetEntityId);
      this.detectedAnimationNames.addAll(detected);
      int visibleCount = Math.min(10, detected.size());

      for (int i = 0; i < visibleCount; i++) {
         String animationName = detected.get(i);
         this.visibleAnimationNames.add(animationName);
         int y = this.animationSectionStartY + i * this.rowHeight;
         class_342 field = new class_342(this.field_22793, this.fieldX, y - 2, this.fieldWidth, 18, class_2561.method_43470(animationName));
         float configured = this.originalConfig.animationSpeedMultiplier(animationName);
         field.method_1852(formatValue(configured));
         field.method_1880(32);
         this.animationFieldByName.put(animationName, field);
         this.method_37063(field);
         int minusX = this.fieldX + this.fieldWidth + this.spinGap;
         int plusX = minusX + this.spinButtonWidth + this.spinGap;
         this.method_37063(
            class_4185.method_46430(class_2561.method_43470("-"), button -> this.nudgeAnimationField(animationName, -0.05F))
               .method_46434(minusX, y - 2, this.spinButtonWidth, 18)
               .method_46431()
         );
         this.method_37063(
            class_4185.method_46430(class_2561.method_43470("+"), button -> this.nudgeAnimationField(animationName, 0.05F))
               .method_46434(plusX, y - 2, this.spinButtonWidth, 18)
               .method_46431()
         );
      }

      this.animationsTruncated = detected.size() > visibleCount;
      int buttonY = this.animationSectionStartY + visibleCount * this.rowHeight + 12;
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

      this.targetConfigPath = this.resolveEditableTargetConfigPath();
      if (this.targetConfigPath == null) {
         this.statusLine = "No editable resource-pack config found for " + this.targetEntityId + ".";
         this.saveButton.field_22763 = false;
      } else {
         boolean created = this.ensureTargetConfigExists();
         if (created) {
            this.statusLine = "Created config for " + this.targetEntityId + " in pack: " + this.targetPackName;
         } else {
            this.statusLine = "Editing pack: " + this.targetPackName;
         }

         this.saveButton.field_22763 = true;
      }

      this.previewEntity = this.createPreviewEntity();
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

      if (this.activeBonePicker != EntityRigEditorScreen.BonePickerTarget.NONE && this.boneSearchField != null) {
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
         ResourcePackObjLivingOverrides.setRigConfigOverrideForTarget(this.targetEntityId, this.originalConfig);
      }

      this.previewEntity = null;
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
      context.method_51439(this.field_22793, class_2561.method_43470("Target: " + this.targetEntityId), left, 22, -6250336, false);
      context.method_51439(
         this.field_22793, class_2561.method_43470("Live preview updates immediately. Save writes the target config JSON."), left, 32, -7303024, false
      );

      for (int i = 0; i < EDITABLE_KEYS.length; i++) {
         String key = EDITABLE_KEYS[i];
         context.method_51439(
            this.field_22793, class_2561.method_43470(labelForKey(key)), this.labelX, this.contentTopY + i * this.rowHeight + 2, -2039584, false
         );
      }

      context.method_51439(this.field_22793, class_2561.method_43470("Bone Targets"), left, this.controlsStartY - 10, -1, false);
      context.method_51439(this.field_22793, class_2561.method_43470("Animation Speed Multipliers"), left, this.animationSectionStartY - 10, -1, false);

      for (int i = 0; i < this.visibleAnimationNames.size(); i++) {
         String animationName = this.visibleAnimationNames.get(i);
         context.method_51439(
            this.field_22793, class_2561.method_43470(animationName), this.labelX, this.animationSectionStartY + i * this.rowHeight + 2, -2039584, false
         );
      }

      if (this.animationsTruncated) {
         context.method_51439(
            this.field_22793,
            class_2561.method_43470("Only first 10 shown here; edit JSON for more."),
            left,
            this.animationSectionStartY + this.visibleAnimationNames.size() * this.rowHeight + 2,
            -16256,
            false
         );
      }

      if (!this.statusLine.isEmpty()) {
         int statusY = this.animationSectionStartY + this.visibleAnimationNames.size() * this.rowHeight + (this.animationsTruncated ? 16 : 4) + 26;
         context.method_51439(this.field_22793, class_2561.method_43470(this.statusLine), left, statusY, -6226000, false);
      }
   }

   private void renderPlayerPreview(class_332 context) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         EntityRigEditorScreen.PreviewBounds bounds = this.computePreviewBounds();
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
            class_1309 renderTarget = this.resolvePreviewEntityForRender();
            if (renderTarget != null) {
               class_490.method_2486(context, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), size, yOffset, lookX, lookY, renderTarget);
            }
         }
      }
   }

   private class_1309 resolvePreviewEntityForRender() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         if (class_2960.method_60656("player").equals(this.targetEntityId)) {
            return this.field_22787.field_1724;
         } else {
            if (this.previewEntity == null
               || this.previewEntity.method_5864() == null
               || !this.targetEntityId.equals(class_7923.field_41177.method_10221(this.previewEntity.method_5864()))) {
               this.previewEntity = this.createPreviewEntity();
            }

            return (class_1309)(this.previewEntity == null ? this.field_22787.field_1724 : this.previewEntity);
         }
      } else {
         return null;
      }
   }

   private class_1309 createPreviewEntity() {
      if (this.field_22787 != null && this.field_22787.field_1687 != null && this.field_22787.field_1724 != null) {
         if (class_2960.method_60656("player").equals(this.targetEntityId)) {
            return this.field_22787.field_1724;
         } else {
            class_1299<?> type = (class_1299<?>)class_7923.field_41177.method_63535(this.targetEntityId);
            if (type == null) {
               return this.field_22787.field_1724;
            } else {
               class_1297 entity;
               try {
                  entity = type.method_5883(this.field_22787.field_1687, class_3730.field_16462);
               } catch (Throwable var4) {
                  entity = null;
               }

               return (class_1309)(entity instanceof class_1309 living ? living : this.field_22787.field_1724);
            }
         }
      } else {
         return null;
      }
   }

   private void renderBonePickerPanel(class_332 context) {
      if (this.activeBonePicker != EntityRigEditorScreen.BonePickerTarget.NONE && this.boneSearchField != null && this.boneSearchField.field_22764) {
         int panelTop = this.pickerY;
         int panelBottom = this.pickerY + 42 + 160;
         context.method_25294(this.pickerX - 2, panelTop - 2, this.pickerX + this.pickerWidth + 2, panelBottom + 2, -872415232);
         context.method_73198(this.pickerX - 2, panelTop - 2, this.pickerWidth + 4, panelBottom - panelTop + 4, -7303024);
         context.method_51439(this.field_22793, class_2561.method_43470(this.bonePickerTitle()), this.pickerX, panelTop + 4, -1, false);
         context.method_51439(this.field_22793, class_2561.method_43470("Search and click a bone"), this.pickerX + 142, panelTop + 4, -6250336, false);
      }
   }

   private String bonePickerTitle() {
      return switch (this.activeBonePicker) {
         case NONE -> "";
         case HEAD -> "Head Look Bone";
         case NECK -> "Neck Bone";
         case ATTACK_STOP -> "Attack Stop Bone";
      };
   }

   private void toggleBonePicker(EntityRigEditorScreen.BonePickerTarget target) {
      if (target == null || target == EntityRigEditorScreen.BonePickerTarget.NONE) {
         this.closeBonePicker();
      } else if (this.activeBonePicker == target) {
         this.closeBonePicker();
      } else {
         this.activeBonePicker = target;
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
      this.activeBonePicker = EntityRigEditorScreen.BonePickerTarget.NONE;
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
      if (this.activeBonePicker != EntityRigEditorScreen.BonePickerTarget.NONE && this.boneSearchField != null) {
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
         switch (this.activeBonePicker) {
            case HEAD:
               this.headBone = selectedBone;
               break;
            case NECK:
               this.neckBone = selectedBone;
               break;
            case ATTACK_STOP:
               this.attackStopBone = selectedBone;
         }

         this.refreshBoneButtons();
         this.applyPreviewFromFields();
      }
   }

   private void openAdvancedBonePicker(EntityRigEditorScreen.BonePickerTarget target) {
      if (this.field_22787 != null
         && target != null
         && target != EntityRigEditorScreen.BonePickerTarget.NONE
         && target != EntityRigEditorScreen.BonePickerTarget.ATTACK_STOP) {
         this.closeBonePicker();
         List<String> allBones = this.resolveAvailableBones();

         List<String> selected = switch (target) {
            case HEAD -> FbxRigConfig.splitBoneTargets(this.headBone);
            case NECK -> FbxRigConfig.splitBoneTargets(this.neckBone);
            default -> List.of();
         };

         class_2561 title = switch (target) {
            case HEAD -> class_2561.method_43470("Head Bone - Advanced");
            case NECK -> class_2561.method_43470("Neck Bone - Advanced");
            default -> class_2561.method_43470("Advanced Bone Picker");
         };
         this.field_22787.method_1507(new BoneMultiSelectScreen(this, title, allBones, selected, picked -> {
            String serialized = FbxRigConfig.joinBoneTargets(picked);
            if (target == EntityRigEditorScreen.BonePickerTarget.HEAD) {
               this.headBone = serialized;
            } else if (target == EntityRigEditorScreen.BonePickerTarget.NECK) {
               this.neckBone = serialized;
            }

            this.refreshBoneButtons();
            this.lastSerializedFields = this.serializeFieldTexts();
            this.applyPreviewFromFields();
         }));
      }
   }

   private List<String> resolveAvailableBones() {
      LinkedHashSet<String> unique = new LinkedHashSet<>();

      for (String bone : ResourcePackObjLivingOverrides.getBoneNamesForTarget(this.targetEntityId)) {
         if (bone != null && !bone.isBlank()) {
            unique.add(bone);
         }
      }

      addBoneTargetsIfPresent(unique, this.headBone);
      addBoneTargetsIfPresent(unique, this.neckBone);
      addIfNotBlank(unique, this.attackStopBone);
      addBoneTargetsIfPresent(unique, this.originalConfig.headBone());
      addBoneTargetsIfPresent(unique, this.originalConfig.neckBone());
      addIfNotBlank(unique, this.originalConfig.resolveAttackStopBone());
      List<String> list = new ArrayList<>(unique);
      list.sort(String::compareToIgnoreCase);
      return list;
   }

   private static void addBoneTargetsIfPresent(LinkedHashSet<String> set, String raw) {
      for (String bone : FbxRigConfig.splitBoneTargets(raw)) {
         addIfNotBlank(set, bone);
      }
   }

   private static void addIfNotBlank(LinkedHashSet<String> set, String value) {
      if (value != null && !value.isBlank()) {
         set.add(value);
      }
   }

   private boolean isPointInPreview(double x, double y) {
      EntityRigEditorScreen.PreviewBounds bounds = this.computePreviewBounds();
      return bounds == null ? false : x >= bounds.left() && x <= bounds.right() && y >= bounds.top() && y <= bounds.bottom();
   }

   private EntityRigEditorScreen.PreviewBounds computePreviewBounds() {
      int top = Math.max(34, this.contentTopY - 8);
      int bottom = this.contentTopY + EDITABLE_KEYS.length * this.rowHeight + 2;
      int right = Math.max(132, this.labelX - 12);
      int left = Math.max(8, right - 190);
      return right - left >= 90 && bottom - top >= 90 ? new EntityRigEditorScreen.PreviewBounds(left, top, right, bottom) : null;
   }

   private void resetToOriginal() {
      for (String key : EDITABLE_KEYS) {
         class_342 field = this.fieldByKey.get(key);
         if (field != null) {
            field.method_1852(formatValue(getValue(this.originalConfig, key)));
         }
      }

      for (String animationName : this.visibleAnimationNames) {
         class_342 field = this.animationFieldByName.get(animationName);
         if (field != null) {
            field.method_1852(formatValue(this.originalConfig.animationSpeedMultiplier(animationName)));
         }
      }

      this.attackUpperBodyOnly = this.originalConfig.attackUpperBodyOnly();
      this.headBone = sanitizeBoneName(this.originalConfig.headBone(), "");
      this.neckBone = sanitizeBoneName(this.originalConfig.neckBone(), "");
      this.attackStopBone = sanitizeBoneName(this.originalConfig.resolveAttackStopBone(), this.neckBone);
      this.refreshBoneButtons();
      if (this.attackUpperBodyToggleButton != null) {
         this.attackUpperBodyToggleButton.method_25355(this.attackToggleLabel());
      }

      this.closeBonePicker();
      this.lastSerializedFields = this.serializeFieldTexts();
      this.applyPreviewFromFields();
      this.statusLine = "Reset to values loaded from " + this.targetEntityId + " config.";
   }

   private void toggleAttackUpperBodyOnly() {
      this.attackUpperBodyOnly = !this.attackUpperBodyOnly;
      if (this.attackUpperBodyToggleButton != null) {
         this.attackUpperBodyToggleButton.method_25355(this.attackToggleLabel());
      }

      this.applyPreviewFromFields();
   }

   private void nudgeField(String key, float delta) {
      class_342 field = this.fieldByKey.get(key);
      if (field != null) {
         float current = this.parseFloatField(field, getValue(this.lastAppliedConfig, key));
         float next = current + delta;
         if ("entityScale".equals(key)) {
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

   private void nudgeAnimationField(String animationName, float delta) {
      class_342 field = this.animationFieldByName.get(animationName);
      if (field != null) {
         float current = this.parseFloatField(field, this.originalConfig.animationSpeedMultiplier(animationName));
         float next = Math.max(0.05F, Math.min(4.0F, current + delta));
         field.method_1852(formatValue(next));
         this.lastSerializedFields = this.serializeFieldTexts();
         this.applyPreviewFromFields();
      }
   }

   private void applyPreviewFromFields() {
      FbxRigConfig preview = this.buildConfigFromFields(this.lastAppliedConfig);
      this.lastAppliedConfig = preview;
      ResourcePackObjLivingOverrides.setRigConfigOverrideForTarget(this.targetEntityId, preview);
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
                  .method_7353(class_2561.method_43470("Saved entity rig settings for " + this.targetEntityId + " in pack " + this.targetPackName + "."), false);
            }

            this.method_25419();
         } catch (IOException var2) {
            this.statusLine = "Save failed: " + var2.getMessage();
         }
      }
   }

   private Path resolveEditableTargetConfigPath() {
      if (this.field_22787 != null && this.field_22787.method_1478() != null) {
         String namespace = this.targetEntityId == null ? "minecraft" : this.targetEntityId.method_12836();
         String targetPath = this.targetEntityId == null ? "player" : this.targetEntityId.method_12832();
         class_2960 primaryConfigId = class_2960.method_43902(namespace, "configs/entity/" + targetPath + ".json");
         class_3298 resource = primaryConfigId == null ? null : (class_3298)this.field_22787.method_1478().method_14486(primaryConfigId).orElse(null);
         String relativePath = "assets/" + namespace + "/configs/entity/" + targetPath + ".json";
         if (resource == null && class_2960.method_60656("player").equals(this.targetEntityId)) {
            resource = (class_3298)this.field_22787.method_1478().method_14486(LEGACY_CONFIG_ID).orElse(null);
            relativePath = "assets/minecraft/configs/player.json";
         }

         String packName = resource == null ? null : extractPackName(resource.method_14480());
         if (packName == null || packName.isBlank()) {
            packName = this.resolvePackNameFromTargetModels(namespace, targetPath);
         }

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
      } else {
         return null;
      }
   }

   private String resolvePackNameFromTargetModels(String namespace, String targetPath) {
      if (this.field_22787 != null && this.field_22787.method_1478() != null && namespace != null && targetPath != null && !targetPath.isBlank()) {
         String[] roots = new String[]{"models/entity/" + targetPath + "/", "models/entities/" + targetPath + "/"};

         for (String root : roots) {
            Map<class_2960, class_3298> resources = this.field_22787
               .method_1478()
               .method_14488(
                  root.substring(0, root.length() - 1),
                  id -> id.method_12836().equals(namespace)
                     && id.method_12832().startsWith(root)
                     && ResourcePackMeshLoader.isSupportedMeshModelPath(id.method_12832())
               );

            for (class_3298 resource : resources.values()) {
               String packName = extractPackName(resource.method_14480());
               if (packName != null && !packName.isBlank()) {
                  return packName;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean ensureTargetConfigExists() {
      if (this.targetConfigPath == null) {
         return false;
      } else if (Files.exists(this.targetConfigPath)) {
         return false;
      } else {
         try {
            writeConfigFile(this.targetConfigPath, this.lastAppliedConfig == null ? this.originalConfig : this.lastAppliedConfig);
            return true;
         } catch (IOException var2) {
            this.statusLine = "Could not create config: " + var2.getMessage();
            return false;
         }
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
      float headLookPitchFactor = this.parseFloat("headLookPitchFactor", base.headLookPitchFactor());
      float headLookYawFactor = this.parseFloat("headLookYawFactor", base.headLookYawFactor());
      float neckLookFactor = this.parseFloat("neckLookFactor", base.neckLookFactor());
      float voiceLipPitchFactor = this.parseFloat("voiceLipPitchFactor", base.voiceLipPitchFactor());
      float voiceLipYawFactor = this.parseFloat("voiceLipYawFactor", base.voiceLipYawFactor());
      float voiceLipRollFactor = this.parseFloat("voiceLipRollFactor", base.voiceLipRollFactor());
      float voiceLipMaxDegrees = Math.max(0.0F, this.parseFloat("voiceLipMaxDegrees", base.voiceLipMaxDegrees()));
      float entityOffsetX = this.parseFloat("entityOffsetX", base.entityOffsetX());
      float entityOffsetY = this.parseFloat("entityOffsetY", base.entityOffsetY());
      float entityOffsetZ = this.parseFloat("entityOffsetZ", base.entityOffsetZ());
      float entityRotX = this.parseFloat("entityRotX", base.entityRotX());
      float entityRotY = this.parseFloat("entityRotY", base.entityRotY());
      float entityRotZ = this.parseFloat("entityRotZ", base.entityRotZ());
      float entityScale = Math.max(0.01F, this.parseFloat("entityScale", base.entityScale()));
      Map<String, Float> multipliers = new LinkedHashMap<>();

      for (Entry<String, class_342> entry : this.animationFieldByName.entrySet()) {
         String animationName = entry.getKey();
         class_342 field = entry.getValue();
         if (animationName != null && !animationName.isBlank() && field != null) {
            float value = this.parseFloatField(field, base.animationSpeedMultiplier(animationName));
            value = Math.max(0.05F, Math.min(4.0F, value));
            multipliers.put(animationName.trim().toLowerCase(Locale.ROOT), value);
         }
      }

      if (base.animationSpeedMultipliers() != null) {
         for (Entry<String, Float> entryx : base.animationSpeedMultipliers().entrySet()) {
            if (entryx != null && entryx.getKey() != null && !entryx.getKey().isBlank() && entryx.getValue() != null) {
               multipliers.putIfAbsent(entryx.getKey(), entryx.getValue());
            }
         }
      }

      return new FbxRigConfig(
         sanitizeBoneName(this.headBone, this.originalConfig.headBone()),
         sanitizeBoneName(this.neckBone, this.originalConfig.neckBone()),
         base.rightHandBone(),
         base.leftHandBone(),
         "",
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
         "",
         sanitizeBoneName(this.attackStopBone, base.resolveAttackStopBone()),
         headLookPitchFactor,
         headLookYawFactor,
         neckLookFactor,
         0.0F,
         0.0F,
         base.voiceUpperLipBone(),
         base.voiceLowerLipBone(),
         voiceLipPitchFactor,
         voiceLipYawFactor,
         voiceLipRollFactor,
         voiceLipMaxDegrees,
         this.attackUpperBodyOnly,
         entityOffsetX,
         entityOffsetY,
         entityOffsetZ,
         entityRotX,
         entityRotY,
         entityRotZ,
         entityScale,
         Map.copyOf(multipliers),
         base.customItemTransforms()
      );
   }

   private float parseFloat(String key, float fallback) {
      class_342 field = this.fieldByKey.get(key);
      return this.parseFloatField(field, fallback);
   }

   private float parseFloatField(class_342 field, float fallback) {
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
               } catch (NumberFormatException var6) {
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
         case "headLookPitchFactor" -> config.headLookPitchFactor();
         case "headLookYawFactor" -> config.headLookYawFactor();
         case "neckLookFactor" -> config.neckLookFactor();
         case "voiceLipPitchFactor" -> config.voiceLipPitchFactor();
         case "voiceLipYawFactor" -> config.voiceLipYawFactor();
         case "voiceLipRollFactor" -> config.voiceLipRollFactor();
         case "voiceLipMaxDegrees" -> config.voiceLipMaxDegrees();
         case "torsoLookPitchFactor" -> config.torsoLookPitchFactor();
         case "torsoLookYawFactor" -> config.torsoLookYawFactor();
         case "entityOffsetX" -> config.entityOffsetX();
         case "entityOffsetY" -> config.entityOffsetY();
         case "entityOffsetZ" -> config.entityOffsetZ();
         case "entityRotX" -> config.entityRotX();
         case "entityRotY" -> config.entityRotY();
         case "entityRotZ" -> config.entityRotZ();
         case "entityScale" -> config.entityScale();
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

      builder.append("attackUpperBodyOnly=").append(this.attackUpperBodyOnly).append(';');
      builder.append("headBone=").append(this.headBone).append(';');
      builder.append("neckBone=").append(this.neckBone).append(';');
      builder.append("attackStopBone=").append(this.attackStopBone).append(';');

      for (String animationName : this.visibleAnimationNames) {
         class_342 field = this.animationFieldByName.get(animationName);
         builder.append(animationName).append('=');
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

   private class_2561 attackToggleLabel() {
      String base = "Attack Upper-Body Only: " + (this.attackUpperBodyOnly ? "ON" : "OFF");
      base = base + " (Disabled)";
      return class_2561.method_43470(base);
   }

   private class_2561 headBoneButtonLabel() {
      return class_2561.method_43470("Head Bone: " + shortenBoneList(this.headBone));
   }

   private class_2561 neckBoneButtonLabel() {
      return class_2561.method_43470("Neck Bone: " + shortenBoneList(this.neckBone));
   }

   private class_2561 attackStopBoneButtonLabel() {
      return class_2561.method_43470("Attack Stop: " + shortenBone(this.attackStopBone));
   }

   private void refreshBoneButtons() {
      if (this.headBoneButton != null) {
         this.headBoneButton.method_25355(this.headBoneButtonLabel());
      }

      if (this.neckBoneButton != null) {
         this.neckBoneButton.method_25355(this.neckBoneButtonLabel());
      }

      if (this.attackStopBoneButton != null) {
         this.attackStopBoneButton.method_25355(this.attackStopBoneButtonLabel());
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
      } else if (bones.size() == 1) {
         return shortenBone(bones.get(0));
      } else {
         String first = shortenBone(bones.get(0));
         return first + " +" + (bones.size() - 1);
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
      } else if (key.startsWith("entityRot")) {
         return 1.0F;
      } else if ("entityScale".equals(key)) {
         return 0.05F;
      } else if ("voiceLipMaxDegrees".equals(key)) {
         return 1.0F;
      } else {
         return key.endsWith("Factor") ? 0.05F : 0.01F;
      }
   }

   private static String labelForKey(String key) {
      return switch (key) {
         case "headLookPitchFactor" -> "Head Look Pitch Factor";
         case "headLookYawFactor" -> "Head Look Yaw Factor";
         case "neckLookFactor" -> "Neck Look Factor";
         case "voiceLipPitchFactor" -> "Voice Lip Pitch Factor";
         case "voiceLipYawFactor" -> "Voice Lip Yaw Factor";
         case "voiceLipRollFactor" -> "Voice Lip Roll Factor";
         case "voiceLipMaxDegrees" -> "Voice Lip Max Degrees";
         case "entityOffsetX" -> "Entity Offset X";
         case "entityOffsetY" -> "Entity Offset Y";
         case "entityOffsetZ" -> "Entity Offset Z";
         case "entityRotX" -> "Entity Rotation X (deg)";
         case "entityRotY" -> "Entity Rotation Y (deg)";
         case "entityRotZ" -> "Entity Rotation Z (deg)";
         case "entityScale" -> "Entity Scale";
         default -> key;
      };
   }

   private static enum BonePickerTarget {
      NONE,
      HEAD,
      NECK,
      ATTACK_STOP;
   }

   private record PreviewBounds(int left, int top, int right, int bottom) {
   }
}
