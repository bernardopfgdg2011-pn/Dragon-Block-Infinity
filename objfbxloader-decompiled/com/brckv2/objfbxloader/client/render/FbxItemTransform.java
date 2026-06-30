package com.brckv2.objfbxloader.client.render;

public record FbxItemTransform(
   float rightOffsetX,
   float rightOffsetY,
   float rightOffsetZ,
   float rightRotX,
   float rightRotY,
   float rightRotZ,
   float rightScale,
   float leftOffsetX,
   float leftOffsetY,
   float leftOffsetZ,
   float leftRotX,
   float leftRotY,
   float leftRotZ,
   float leftScale
) {
   public static final FbxItemTransform IDENTITY = new FbxItemTransform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);

   public float offsetX(boolean left) {
      return left ? this.leftOffsetX : this.rightOffsetX;
   }

   public float offsetY(boolean left) {
      return left ? this.leftOffsetY : this.rightOffsetY;
   }

   public float offsetZ(boolean left) {
      return left ? this.leftOffsetZ : this.rightOffsetZ;
   }

   public float rotX(boolean left) {
      return left ? this.leftRotX : this.rightRotX;
   }

   public float rotY(boolean left) {
      return left ? this.leftRotY : this.rightRotY;
   }

   public float rotZ(boolean left) {
      return left ? this.leftRotZ : this.rightRotZ;
   }

   public float scale(boolean left) {
      return left ? this.leftScale : this.rightScale;
   }
}
