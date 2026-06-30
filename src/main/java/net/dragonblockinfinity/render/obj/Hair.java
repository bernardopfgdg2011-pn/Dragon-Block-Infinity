package net.dragonblockinfinity.render.obj;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import com.brockv2.objfbxloader.client.render.ResourcePackObjLivingFeatureRenderer;

public class Hair extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    // Instância do carregador de OBJ do mod BrockV2
    private final ResourcePackObjLivingFeatureRenderer objLoader;

    public Hair(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        // Inicializa o carregador apontando para o seu ID registrado
        this.objLoader = new ResourcePackObjLivingFeatureRenderer("hair");
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        // Só renderiza se o jogador não estiver invisível
        if (player.isInvisible()) return;

        matrices.push();

        // 1. Vincula o modelo ao movimento da cabeça do modelo do Player (Steve/Alex)
        // Isso faz o cabelo girar perfeitamente quando o player olha para os lados ou para cima/baixo
        this.getContextModel().head.rotate(matrices);

        // 2. Ajustes manuais de posição (Gambiarras de Java se o modelo nascer torto)
        // Se o cabelo ficar muito alto, mude o segundo valor (Y). Se ficar muito para frente, mude o terceiro (Z).
        // Exemplo: matrices.translate(0.0F, -0.25F, 0.0F); // Sobe ou desce o cabelo em frações de bloco

        // 3. Renderiza o modelo OBJ carregado pelo mod BrockV2
        this.objLoader.render(matrices, vertexConsumers, light, player, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        matrices.pop();
    }
}
