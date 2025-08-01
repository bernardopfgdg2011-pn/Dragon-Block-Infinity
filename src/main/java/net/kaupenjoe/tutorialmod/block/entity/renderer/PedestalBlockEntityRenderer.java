package net.kaupenjoe.tutorialmod.block.entity.renderer;

import net.kaupenjoe.tutorialmod.block.entity.custom.PedestalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    @SuppressWarnings("unused")
    public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(PedestalBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!(blockEntity.getWorld() instanceof ClientWorld clientWorld)) return;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack stack = blockEntity.getStack(0);

        matrices.push();
        matrices.translate(0.5f, 1.15f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(blockEntity.getSyncedComponent().getRenderingRotation(tickDelta)));

        itemRenderer.renderItem(stack, ModelTransformationMode.GUI, getLightLevel(clientWorld,
                blockEntity.getPos()), OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, blockEntity.getWorld(), 1);
        matrices.pop();
    }

    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}
