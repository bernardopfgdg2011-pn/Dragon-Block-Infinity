package com.brckv2.objfbxloader.client.gui;

import com.brckv2.objfbxloader.ObjFbxLoaderClient;
import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class RigEditorRootScreen extends class_437 {
   private final class_437 parent;

   public RigEditorRootScreen(class_437 parent) {
      super(class_2561.method_43470("Rig Editor"));
      this.parent = parent;
   }

   protected void method_25426() {
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Entity Options"), button -> {
         if (this.field_22787 != null) {
            List<class_2960> targets = ResourcePackObjLivingOverrides.getDetectedEntityTargets();
            if (targets.size() <= 1) {
               class_2960 targetId = targets.isEmpty() ? class_2960.method_60656("player") : targets.get(0);
               this.field_22787.method_1507(new EntityRigEditorScreen(this, targetId));
            } else {
               this.field_22787.method_1507(new EntityRigTargetPickerScreen(this, targets));
            }
         }
      }).method_46434(centerX - 90, centerY - 24, 180, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Item Options"), button -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new HeldItemRigEditorScreen(this));
         }
      }).method_46434(centerX - 90, centerY + 2, 180, 20).method_46431());
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Multiplayer Options"), button -> ObjFbxLoaderClient.openMultiplayerOptionsScreen(this))
            .method_46434(centerX - 90, centerY + 28, 180, 20)
            .method_46431()
      );
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Close"), button -> this.method_25419())
            .method_46434(centerX - 90, centerY + 54, 180, 20)
            .method_46431()
      );
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
      int titleY = this.field_22790 / 2 - 58;
      int subtitleY = this.field_22790 / 2 - 44;
      String subtitle = "Choose what to edit";
      context.method_51439(this.field_22793, this.field_22785, centerX - this.field_22793.method_27525(this.field_22785) / 2, titleY, -1, false);
      context.method_51439(
         this.field_22793, class_2561.method_43470(subtitle), centerX - this.field_22793.method_1727(subtitle) / 2, subtitleY, -5197648, false
      );
   }
}
