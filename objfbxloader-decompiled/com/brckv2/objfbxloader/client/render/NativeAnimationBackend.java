package com.brckv2.objfbxloader.client.render;

import com.brckv2.objfbxloader.ObjFbxLoader;

final class NativeAnimationBackend {
   private static boolean initialized = false;
   private static boolean available = false;
   private static boolean failureLogged = false;
   private static boolean renderStreamAvailable = true;
   private static boolean renderStreamFailureLogged = false;
   private static boolean interpolatedSkinAvailable = true;
   private static boolean interpolatedSkinFailureLogged = false;
   private static boolean renderStreamBlendAvailable = true;
   private static boolean renderStreamBlendFailureLogged = false;
   private static boolean ragdollStepAvailable = true;
   private static boolean ragdollStepFailureLogged = false;
   private static boolean interpolatedSkinRenderStreamAvailable = true;
   private static boolean interpolatedSkinRenderStreamFailureLogged = false;

   private NativeAnimationBackend() {
   }

   static synchronized boolean isAvailable() {
      ensureInitialized();
      return available;
   }

   static boolean skinVertices(
      float[] basePositions,
      int[] vertexBoneIndices,
      float[] vertexBoneWeights,
      int[] vertexMeshIndices,
      float[] boneMatrices,
      int[] boneActive,
      float[] meshMatrices,
      float[] outPositions,
      int vertexCount,
      float normalizeCenterX,
      float normalizeCenterY,
      float normalizeCenterZ,
      float normalizeScale,
      boolean centerToCube,
      float modelOffsetX,
      float modelOffsetY,
      float modelOffsetZ,
      float modelRotXDeg,
      float modelRotYDeg,
      float modelRotZDeg,
      float modelScale
   ) {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            return skinVerticesNative(
               basePositions,
               vertexBoneIndices,
               vertexBoneWeights,
               vertexMeshIndices,
               boneMatrices,
               boneActive,
               meshMatrices,
               outPositions,
               vertexCount,
               normalizeCenterX,
               normalizeCenterY,
               normalizeCenterZ,
               normalizeScale,
               centerToCube,
               modelOffsetX,
               modelOffsetY,
               modelOffsetZ,
               modelRotXDeg,
               modelRotYDeg,
               modelRotZDeg,
               modelScale
            );
         } catch (Throwable var22) {
            if (!failureLogged) {
               failureLogged = true;
               ObjFbxLoader.LOGGER.warn("Native animation backend failed at runtime. Disabling native path for this session.", var22);
            }

            available = false;
            return false;
         }
      }
   }

   static boolean buildRenderStream(
      float[] transformedPositions,
      int vertexCount,
      int[] trianglePositionIndices,
      int[] triangleUvIndices,
      float[] packedUvs,
      float[] outRenderStream,
      int triangleCount
   ) {
      if (isAvailable() && renderStreamAvailable) {
         try {
            return buildRenderStreamNative(
               transformedPositions, vertexCount, trianglePositionIndices, triangleUvIndices, packedUvs, outRenderStream, triangleCount
            );
         } catch (Throwable var8) {
            if (!renderStreamFailureLogged) {
               renderStreamFailureLogged = true;
               ObjFbxLoader.LOGGER.warn("Native render-stream backend failed at runtime. Falling back to Java triangle build.", var8);
            }

            renderStreamAvailable = false;
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean skinVerticesInterpolated(
      float[] basePositions,
      int[] vertexBoneIndices,
      float[] vertexBoneWeights,
      int[] vertexMeshIndices,
      float[] boneMatricesA,
      int[] boneActiveA,
      float[] boneMatricesB,
      int[] boneActiveB,
      float[] meshMatricesA,
      float[] meshMatricesB,
      float alpha,
      int headBoneIndex,
      float[] headOverrideMatrix,
      int neckBoneIndex,
      float[] neckOverrideMatrix,
      float[] outPositions,
      int vertexCount,
      float normalizeCenterX,
      float normalizeCenterY,
      float normalizeCenterZ,
      float normalizeScale,
      boolean centerToCube,
      float modelOffsetX,
      float modelOffsetY,
      float modelOffsetZ,
      float modelRotXDeg,
      float modelRotYDeg,
      float modelRotZDeg,
      float modelScale
   ) {
      if (isAvailable() && interpolatedSkinAvailable) {
         try {
            return skinVerticesInterpolatedNative(
               basePositions,
               vertexBoneIndices,
               vertexBoneWeights,
               vertexMeshIndices,
               boneMatricesA,
               boneActiveA,
               boneMatricesB,
               boneActiveB,
               meshMatricesA,
               meshMatricesB,
               alpha,
               headBoneIndex,
               headOverrideMatrix,
               neckBoneIndex,
               neckOverrideMatrix,
               outPositions,
               vertexCount,
               normalizeCenterX,
               normalizeCenterY,
               normalizeCenterZ,
               normalizeScale,
               centerToCube,
               modelOffsetX,
               modelOffsetY,
               modelOffsetZ,
               modelRotXDeg,
               modelRotYDeg,
               modelRotZDeg,
               modelScale
            );
         } catch (Throwable var30) {
            if (!interpolatedSkinFailureLogged) {
               interpolatedSkinFailureLogged = true;
               ObjFbxLoader.LOGGER.warn("Native interpolated skinning backend failed at runtime. Falling back to mixed Java/native path.", var30);
            }

            interpolatedSkinAvailable = false;
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean skinVerticesInterpolatedBuildRenderStream(
      float[] basePositions,
      int[] vertexBoneIndices,
      float[] vertexBoneWeights,
      int[] vertexMeshIndices,
      float[] boneMatricesA,
      int[] boneActiveA,
      float[] boneMatricesB,
      int[] boneActiveB,
      float[] meshMatricesA,
      float[] meshMatricesB,
      float alpha,
      int headBoneIndex,
      float[] headOverrideMatrix,
      int neckBoneIndex,
      float[] neckOverrideMatrix,
      int vertexCount,
      float normalizeCenterX,
      float normalizeCenterY,
      float normalizeCenterZ,
      float normalizeScale,
      boolean centerToCube,
      float modelOffsetX,
      float modelOffsetY,
      float modelOffsetZ,
      float modelRotXDeg,
      float modelRotYDeg,
      float modelRotZDeg,
      float modelScale,
      int[] trianglePositionIndices,
      int[] triangleUvIndices,
      float[] packedUvs,
      float[] outRenderStream,
      int triangleCount
   ) {
      if (isAvailable() && interpolatedSkinRenderStreamAvailable) {
         try {
            return skinVerticesInterpolatedBuildRenderStreamNative(
               basePositions,
               vertexBoneIndices,
               vertexBoneWeights,
               vertexMeshIndices,
               boneMatricesA,
               boneActiveA,
               boneMatricesB,
               boneActiveB,
               meshMatricesA,
               meshMatricesB,
               alpha,
               headBoneIndex,
               headOverrideMatrix,
               neckBoneIndex,
               neckOverrideMatrix,
               vertexCount,
               normalizeCenterX,
               normalizeCenterY,
               normalizeCenterZ,
               normalizeScale,
               centerToCube,
               modelOffsetX,
               modelOffsetY,
               modelOffsetZ,
               modelRotXDeg,
               modelRotYDeg,
               modelRotZDeg,
               modelScale,
               trianglePositionIndices,
               triangleUvIndices,
               packedUvs,
               outRenderStream,
               triangleCount
            );
         } catch (Throwable var34) {
            if (!interpolatedSkinRenderStreamFailureLogged) {
               interpolatedSkinRenderStreamFailureLogged = true;
               ObjFbxLoader.LOGGER.warn("Native interpolated skin/render-stream backend failed at runtime. Falling back to split native path.", var34);
            }

            interpolatedSkinRenderStreamAvailable = false;
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean blendRenderStreams(float[] fromRenderStream, float[] toRenderStream, float alpha, float[] outRenderStream) {
      if (isAvailable() && renderStreamBlendAvailable) {
         try {
            return blendRenderStreamsNative(fromRenderStream, toRenderStream, alpha, outRenderStream);
         } catch (Throwable var5) {
            if (!renderStreamBlendFailureLogged) {
               renderStreamBlendFailureLogged = true;
               ObjFbxLoader.LOGGER.warn("Native render-stream blend backend failed at runtime. Falling back to Java blend.", var5);
            }

            renderStreamBlendAvailable = false;
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean stepRagdoll(
      float[] positionsPacked,
      float[] previousPositionsPacked,
      int[] parentIndices,
      float[] restLengths,
      float[] jointRadii,
      int rootIndex,
      float collapseBiasX,
      float collapseBiasZ,
      float floorY,
      float dt,
      float carryX,
      float carryY,
      float carryZ,
      float gravity,
      float damping,
      float earlyTorqueForce,
      float lateralCollapseForce
   ) {
      if (isAvailable() && ragdollStepAvailable) {
         try {
            return stepRagdollNative(
               positionsPacked,
               previousPositionsPacked,
               parentIndices,
               restLengths,
               jointRadii,
               rootIndex,
               collapseBiasX,
               collapseBiasZ,
               floorY,
               dt,
               carryX,
               carryY,
               carryZ,
               gravity,
               damping,
               earlyTorqueForce,
               lateralCollapseForce
            );
         } catch (Throwable var18) {
            if (!ragdollStepFailureLogged) {
               ragdollStepFailureLogged = true;
               ObjFbxLoader.LOGGER.warn("Native ragdoll step backend failed at runtime. Falling back to Java ragdoll solver.", var18);
            }

            ragdollStepAvailable = false;
            return false;
         }
      } else {
         return false;
      }
   }

   private static void ensureInitialized() {
      if (!initialized) {
         initialized = true;
         available = NativeLibraryLoader.loadAnimationNative();
         if (available) {
            ObjFbxLoader.LOGGER.info("Native animation backend loaded (platform native library).");
         } else {
            ObjFbxLoader.LOGGER.info("Native animation backend library not loaded; using Java animation path.");
         }
      }
   }

   private static native boolean skinVerticesNative(
      float[] var0,
      int[] var1,
      float[] var2,
      int[] var3,
      float[] var4,
      int[] var5,
      float[] var6,
      float[] var7,
      int var8,
      float var9,
      float var10,
      float var11,
      float var12,
      boolean var13,
      float var14,
      float var15,
      float var16,
      float var17,
      float var18,
      float var19,
      float var20
   );

   private static native boolean buildRenderStreamNative(float[] var0, int var1, int[] var2, int[] var3, float[] var4, float[] var5, int var6);

   private static native boolean skinVerticesInterpolatedNative(
      float[] var0,
      int[] var1,
      float[] var2,
      int[] var3,
      float[] var4,
      int[] var5,
      float[] var6,
      int[] var7,
      float[] var8,
      float[] var9,
      float var10,
      int var11,
      float[] var12,
      int var13,
      float[] var14,
      float[] var15,
      int var16,
      float var17,
      float var18,
      float var19,
      float var20,
      boolean var21,
      float var22,
      float var23,
      float var24,
      float var25,
      float var26,
      float var27,
      float var28
   );

   private static native boolean skinVerticesInterpolatedBuildRenderStreamNative(
      float[] var0,
      int[] var1,
      float[] var2,
      int[] var3,
      float[] var4,
      int[] var5,
      float[] var6,
      int[] var7,
      float[] var8,
      float[] var9,
      float var10,
      int var11,
      float[] var12,
      int var13,
      float[] var14,
      int var15,
      float var16,
      float var17,
      float var18,
      float var19,
      boolean var20,
      float var21,
      float var22,
      float var23,
      float var24,
      float var25,
      float var26,
      float var27,
      int[] var28,
      int[] var29,
      float[] var30,
      float[] var31,
      int var32
   );

   private static native boolean blendRenderStreamsNative(float[] var0, float[] var1, float var2, float[] var3);

   private static native boolean stepRagdollNative(
      float[] var0,
      float[] var1,
      int[] var2,
      float[] var3,
      float[] var4,
      int var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      float var15,
      float var16
   );
}
