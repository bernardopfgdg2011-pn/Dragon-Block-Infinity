package com.brckv2.objfbxloader.mixin;

import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import net.minecraft.class_10426;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_989;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_989.class})
public class HeldItemFeatureRendererMixin {
   @Inject(
      method = {"method_4192"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void objfbxloader$renderCustomHeldItemForArmAndCancelVanilla(
      class_10426 state, class_10444 itemState, class_1799 stack, class_1306 arm, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci
   ) {
      boolean renderedCustom = ResourcePackObjLivingOverrides.renderHeldItemOverrideForArm(state, arm, matrices, queue, light);
      boolean suppressVanilla = ResourcePackObjLivingOverrides.shouldSuppressVanillaHeldItemForArm(state, arm);
      if (renderedCustom || suppressVanilla) {
         ci.cancel();
      }
   }
}
