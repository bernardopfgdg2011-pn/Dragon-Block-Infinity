package com.brckv2.objfbxloader.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class EntityRigTargetPickerScreen extends class_437 {
   private static final int MAX_VISIBLE_OPTIONS = 10;
   private final class_437 parent;
   private final List<class_2960> allTargets;
   private final List<class_2960> filteredTargets = new ArrayList<>();
   private final List<class_4185> optionButtons = new ArrayList<>();
   private class_342 searchField;
   private class_4185 previousPageButton;
   private class_4185 nextPageButton;
   private int pageStart = 0;
   private String lastSearch = "";

   public EntityRigTargetPickerScreen(class_437 parent, List<class_2960> detectedTargets) {
      super(class_2561.method_43470("Entity Options - Choose Entity"));
      this.parent = parent;
      this.allTargets = sanitizeTargets(detectedTargets);
      this.filteredTargets.addAll(this.allTargets);
   }

   protected void method_25426() {
      this.optionButtons.clear();
      int centerX = this.field_22789 / 2;
      int top = 34;
      int listLeft = centerX - 140;
      int listWidth = 280;
      this.searchField = (class_342)this.method_37063(new class_342(this.field_22793, listLeft, top, listWidth, 20, class_2561.method_43470("Search entities")));
      this.searchField.method_1880(128);
      this.searchField.method_1852(this.lastSearch);
      int optionsTop = top + 28;

      for (int i = 0; i < 10; i++) {
         int optionIndex = i;
         class_4185 optionButton = (class_4185)this.method_37063(
            class_4185.method_46430(class_2561.method_43470(""), button -> this.openSelectedOption(optionIndex))
               .method_46434(listLeft, optionsTop + i * 21, listWidth, 20)
               .method_46431()
         );
         this.optionButtons.add(optionButton);
      }

      int controlsY = optionsTop + 210 + 6;
      this.previousPageButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43470("Prev"), button -> {
         this.pageStart = Math.max(0, this.pageStart - 10);
         this.refreshOptions();
      }).method_46434(listLeft, controlsY, 90, 20).method_46431());
      this.nextPageButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43470("Next"), button -> {
         this.pageStart = Math.min(Math.max(0, this.filteredTargets.size() - 10), this.pageStart + 10);
         this.refreshOptions();
      }).method_46434(listLeft + 95, controlsY, 90, 20).method_46431());
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Back"), button -> this.method_25419()).method_46434(listLeft + 190, controlsY, 90, 20).method_46431()
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
      context.method_51439(this.field_22793, this.field_22785, centerX - this.field_22793.method_27525(this.field_22785) / 2, 12, -1, false);
      context.method_51439(this.field_22793, class_2561.method_43470("Pick which detected entity config to edit"), centerX - 122, 22, -6250336, false);
      if (this.filteredTargets.isEmpty()) {
         context.method_51439(this.field_22793, class_2561.method_43470("No entity matches your search."), centerX - 88, 68, -32640, false);
      } else {
         int page = this.pageStart / 10 + 1;
         int maxPage = Math.max(1, (int)Math.ceil(this.filteredTargets.size() / 10.0));
         String pageText = "Page "
            + page
            + " / "
            + maxPage
            + "   ("
            + this.filteredTargets.size()
            + " result"
            + (this.filteredTargets.size() == 1 ? "" : "s")
            + ")";
         context.method_51439(this.field_22793, class_2561.method_43470(pageText), centerX - this.field_22793.method_1727(pageText) / 2, 282, -5197648, false);
      }
   }

   private void applySearchFilter() {
      this.filteredTargets.clear();
      String query = this.lastSearch == null ? "" : this.lastSearch.trim().toLowerCase(Locale.ROOT);

      for (class_2960 id : this.allTargets) {
         String raw = id.toString().toLowerCase(Locale.ROOT);
         String path = id.method_12832().toLowerCase(Locale.ROOT);
         if (query.isBlank() || raw.contains(query) || path.contains(query)) {
            this.filteredTargets.add(id);
         }
      }

      this.pageStart = 0;
      this.refreshOptions();
   }

   private void refreshOptions() {
      int maxStart = Math.max(0, this.filteredTargets.size() - 10);
      if (this.pageStart > maxStart) {
         this.pageStart = maxStart;
      }

      for (int i = 0; i < this.optionButtons.size(); i++) {
         int targetIndex = this.pageStart + i;
         class_4185 optionButton = this.optionButtons.get(i);
         if (targetIndex < this.filteredTargets.size()) {
            class_2960 targetId = this.filteredTargets.get(targetIndex);
            optionButton.field_22764 = true;
            optionButton.field_22763 = true;
            optionButton.method_25355(class_2561.method_43470(targetLabel(targetId)));
         } else {
            optionButton.field_22764 = false;
            optionButton.field_22763 = false;
            optionButton.method_25355(class_2561.method_43470(""));
         }
      }

      if (this.previousPageButton != null) {
         this.previousPageButton.field_22763 = this.pageStart > 0;
      }

      if (this.nextPageButton != null) {
         this.nextPageButton.field_22763 = this.pageStart + 10 < this.filteredTargets.size();
      }
   }

   private void openSelectedOption(int visibleIndex) {
      int targetIndex = this.pageStart + visibleIndex;
      if (targetIndex >= 0 && targetIndex < this.filteredTargets.size()) {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new EntityRigEditorScreen(this, this.filteredTargets.get(targetIndex)));
         }
      }
   }

   private static List<class_2960> sanitizeTargets(List<class_2960> detectedTargets) {
      LinkedHashSet<class_2960> unique = new LinkedHashSet<>();
      if (detectedTargets != null) {
         for (class_2960 id : detectedTargets) {
            if (id != null) {
               unique.add(id);
            }
         }
      }

      if (unique.isEmpty()) {
         unique.add(class_2960.method_60656("player"));
      }

      return List.copyOf(unique);
   }

   private static String targetLabel(class_2960 targetId) {
      if (targetId == null) {
         return "(unknown)";
      } else {
         return class_2960.method_60656("player").equals(targetId) ? "minecraft:player (Local Player)" : targetId.toString();
      }
   }
}
