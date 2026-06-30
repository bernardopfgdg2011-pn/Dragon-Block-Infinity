package com.brckv2.objfbxloader.client.render;

import net.minecraft.class_10034;
import net.minecraft.class_10042;
import net.minecraft.class_11659;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_583;

public class ResourcePackObjLivingFeatureRenderer<S extends class_10042, M extends class_583<? super S>> extends class_3887<S, M> {
   public ResourcePackObjLivingFeatureRenderer(class_3883<S, M> context) {
      super(context);
   }

   public void render(class_4587 matrices, class_11659 queue, int light, S state, float limbAngle, float limbDistance) {
      ResourcePackObjLivingOverrides.renderEntityOverride(state, matrices, queue);
      if (state instanceof class_10034 bipedState) {
         ResourcePackObjLivingOverrides.renderArmorOverrides(bipedState, matrices, queue);
      }
   }
}
