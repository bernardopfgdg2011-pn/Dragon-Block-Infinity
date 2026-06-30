package com.brckv2.objfbxloader.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class BoneMultiSelectScreen extends class_437 {
   private static final int MAX_VISIBLE_OPTIONS = 12;
   private final class_437 parent;
   private final class_2561 titleText;
   private final List<String> allBones;
   private final List<String> filteredBones = new ArrayList<>();
   private final LinkedHashSet<String> selectedBones = new LinkedHashSet<>();
   private final List<class_4185> optionButtons = new ArrayList<>();
   private final Consumer<List<String>> onSave;
   private class_342 searchField;
   private class_4185 previousPageButton;
   private class_4185 nextPageButton;
   private int pageStart = 0;
   private String lastSearch = "";

   public BoneMultiSelectScreen(class_437 parent, class_2561 titleText, List<String> allBones, List<String> initiallySelected, Consumer<List<String>> onSave) {
      super((class_2561)(titleText == null ? class_2561.method_43470("Advanced Bone Picker") : titleText));
      this.parent = parent;
      this.titleText = (class_2561)(titleText == null ? class_2561.method_43470("Advanced Bone Picker") : titleText);
      this.allBones = sanitizeBoneList(allBones);
      this.filteredBones.addAll(this.allBones);
      if (initiallySelected != null) {
         for (String bone : initiallySelected) {
            if (bone != null && !bone.isBlank()) {
               this.selectedBones.add(bone.trim());
            }
         }
      }

      this.onSave = onSave;
   }

   protected void method_25426() {
      this.optionButtons.clear();
      int centerX = this.field_22789 / 2;
      int top = 34;
      int listLeft = centerX - 170;
      int listWidth = 340;
      this.searchField = (class_342)this.method_37063(new class_342(this.field_22793, listLeft, top, listWidth, 20, class_2561.method_43470("Search bones")));
      this.searchField.method_1880(128);
      this.searchField.method_1852(this.lastSearch);
      int optionsTop = top + 28;

      for (int i = 0; i < 12; i++) {
         int optionIndex = i;
         class_4185 optionButton = (class_4185)this.method_37063(
            class_4185.method_46430(class_2561.method_43470(""), button -> this.toggleSelectedOption(optionIndex))
               .method_46434(listLeft, optionsTop + i * 21, listWidth, 20)
               .method_46431()
         );
         this.optionButtons.add(optionButton);
      }

      int controlsY = optionsTop + 252 + 6;
      this.previousPageButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43470("Prev"), button -> {
         this.pageStart = Math.max(0, this.pageStart - 12);
         this.refreshOptions();
      }).method_46434(listLeft, controlsY, 72, 20).method_46431());
      this.nextPageButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43470("Next"), button -> {
         this.pageStart = Math.min(Math.max(0, this.filteredBones.size() - 12), this.pageStart + 12);
         this.refreshOptions();
      }).method_46434(listLeft + 76, controlsY, 72, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Clear"), button -> {
         this.selectedBones.clear();
         this.refreshOptions();
      }).method_46434(listLeft + 152, controlsY, 72, 20).method_46431());
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Done"), button -> this.saveAndClose()).method_46434(listLeft + 228, controlsY, 54, 20).method_46431()
      );
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Cancel"), button -> this.method_25419())
            .method_46434(listLeft + 286, controlsY, 54, 20)
            .method_46431()
      );
      this.applySearchFilter();
   }

   public void method_25393() {
      super.method_25393();
      if (this.searchField != null) {
         String current = this.searchField.method_1882() == null ? "" : this.searchField.method_1882();
         if (!current.equals(this.lastSearch)) {
            this.lastSearch = current;
            this.applySearchFilter();
         }
      }
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      context.method_25294(0, 0, this.field_22789, this.field_22790, -1072689136);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      context.method_51439(this.field_22793, this.titleText, centerX - this.field_22793.method_27525(this.titleText) / 2, 12, -1, false);
      context.method_51439(
         this.field_22793,
         class_2561.method_43470("Select one or more bones. These are saved as a multi-bone target list."),
         centerX - 168,
         22,
         -6250336,
         false
      );
      String selectedText = "Selected: " + this.selectedBones.size();
      context.method_51439(
         this.field_22793, class_2561.method_43470(selectedText), centerX - this.field_22793.method_1727(selectedText) / 2, 328, -5177424, false
      );
      if (this.filteredBones.isEmpty()) {
         context.method_51439(this.field_22793, class_2561.method_43470("No bones match your search."), centerX - 76, 74, -32640, false);
      } else {
         int page = this.pageStart / 12 + 1;
         int maxPage = Math.max(1, (int)Math.ceil(this.filteredBones.size() / 12.0));
         String pageText = "Page "
            + page
            + " / "
            + maxPage
            + "   ("
            + this.filteredBones.size()
            + " result"
            + (this.filteredBones.size() == 1 ? "" : "s")
            + ")";
         context.method_51439(this.field_22793, class_2561.method_43470(pageText), centerX - this.field_22793.method_1727(pageText) / 2, 314, -5197648, false);
      }
   }

   private void saveAndClose() {
      if (this.onSave != null) {
         this.onSave.accept(List.copyOf(this.selectedBones));
      }

      this.method_25419();
   }

   private void applySearchFilter() {
      this.filteredBones.clear();
      String query = this.lastSearch == null ? "" : this.lastSearch.trim().toLowerCase(Locale.ROOT);

      for (String bone : this.allBones) {
         String lower = bone.toLowerCase(Locale.ROOT);
         if (query.isBlank() || lower.contains(query)) {
            this.filteredBones.add(bone);
         }
      }

      this.pageStart = 0;
      this.refreshOptions();
   }

   private void refreshOptions() {
      int maxStart = Math.max(0, this.filteredBones.size() - 12);
      if (this.pageStart > maxStart) {
         this.pageStart = maxStart;
      }

      for (int i = 0; i < this.optionButtons.size(); i++) {
         int optionIndex = this.pageStart + i;
         class_4185 button = this.optionButtons.get(i);
         if (optionIndex < this.filteredBones.size()) {
            String bone = this.filteredBones.get(optionIndex);
            boolean selected = this.selectedBones.contains(bone);
            button.field_22764 = true;
            button.field_22763 = true;
            button.method_25355(class_2561.method_43470((selected ? "[x] " : "[ ] ") + bone));
         } else {
            button.field_22764 = false;
            button.field_22763 = false;
            button.method_25355(class_2561.method_43470(""));
         }
      }

      if (this.previousPageButton != null) {
         this.previousPageButton.field_22763 = this.pageStart > 0;
      }

      if (this.nextPageButton != null) {
         this.nextPageButton.field_22763 = this.pageStart + 12 < this.filteredBones.size();
      }
   }

   private void toggleSelectedOption(int visibleIndex) {
      int optionIndex = this.pageStart + visibleIndex;
      if (optionIndex >= 0 && optionIndex < this.filteredBones.size()) {
         String bone = this.filteredBones.get(optionIndex);
         if (this.selectedBones.contains(bone)) {
            this.selectedBones.remove(bone);
         } else {
            this.selectedBones.add(bone);
         }

         this.refreshOptions();
      }
   }

   private static List<String> sanitizeBoneList(List<String> raw) {
      LinkedHashSet<String> unique = new LinkedHashSet<>();
      if (raw != null) {
         for (String bone : raw) {
            if (bone != null) {
               String trimmed = bone.trim();
               if (!trimmed.isBlank()) {
                  unique.add(trimmed);
               }
            }
         }
      }

      return List.copyOf(unique);
   }
}
