package com.brckv2.objfbxloader.mixin;

import com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides;
import net.minecraft.class_10034;
import net.minecraft.class_10042;
import net.minecraft.class_1058;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import net.minecraft.class_11683.class_11792;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_922.class})
public class LivingEntityRendererMixin {
   private static final boolean OBJFBXLOADER$NEEDS_DIRECT_OVERRIDE_RENDER = objfbxloader$isForgeOrNeoRuntime();

   @Inject(
      method = {"method_62355"},
      at = {@At("TAIL")}
   )
   private void objfbxloader$rememberEntityIdForRenderState(class_1309 entity, class_10042 state, float tickDelta, CallbackInfo ci) {
      ResourcePackObjLivingOverrides.rememberEntitySittingState(entity, state);
   }

   @Redirect(
      method = {"method_4054"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"
      )
   )
   private void objfbxloader$skipVanillaModelWhenObjOverrideExists(
      class_11659 queue,
      class_3879<?> model,
      Object state,
      class_4587 matrices,
      class_1921 renderLayer,
      int light,
      int overlay,
      int outlineColor,
      class_1058 sprite,
      int color,
      class_11792 crumblingOverlay
   ) {
      if (!(state instanceof class_10042 livingState && objfbxloader$hasEntityOverride(livingState))) {
         queue.method_73490(model, state, matrices, renderLayer, light, overlay, outlineColor, sprite, color, crumblingOverlay);
      }
   }

   @Inject(
      method = {"method_4054"},
      at = {@At("TAIL")}
   )
   private void objfbxloader$renderObjOverridesOnForgeNeo(class_10042 state, class_4587 matrices, class_11659 queue, class_12075 cameraState, CallbackInfo ci) {
      if (OBJFBXLOADER$NEEDS_DIRECT_OVERRIDE_RENDER) {
         ResourcePackObjLivingOverrides.renderEntityOverride(state, matrices, queue);
         if (state instanceof class_10034 bipedState) {
            ResourcePackObjLivingOverrides.renderArmorOverrides(bipedState, matrices, queue);
         }
      }
   }

   private static boolean objfbxloader$hasEntityOverride(class_10042 state) {
      try {
         Class<?> type = Class.forName("com.brckv2.objfbxloader.client.render.ResourcePackObjLivingOverrides");
         return type.getMethod("hasEntityOverride", class_10042.class).invoke(null, state) instanceof Boolean bool && bool;
      } catch (Throwable var4) {
         return false;
      }
   }

   private static boolean objfbxloader$isForgeOrNeoRuntime() {
      return objfbxloader$classExists("net.neoforged.fml.loading.FMLLoader") || objfbxloader$classExists("net.minecraftforge.fml.loading.FMLLoader");
   }

   private static boolean objfbxloader$classExists(String className) {
      try {
         Class.forName(className, false, LivingEntityRendererMixin.class.getClassLoader());
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }
}
