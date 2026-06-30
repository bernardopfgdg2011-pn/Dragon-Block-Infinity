package com.brckv2.objfbxloader.mixin;

import com.brckv2.objfbxloader.client.render.RenderStateEntityIdAccessor;
import net.minecraft.class_10042;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({class_10042.class})
public class LivingEntityRenderStateMixin implements RenderStateEntityIdAccessor {
   @Unique
   private int objfbxloader$entityId = Integer.MIN_VALUE;

   @Override
   public int objfbxloader$getEntityId() {
      return this.objfbxloader$entityId;
   }

   @Override
   public void objfbxloader$setEntityId(int entityId) {
      this.objfbxloader$entityId = entityId;
   }
}
