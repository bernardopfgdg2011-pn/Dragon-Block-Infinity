package net.dragonblockinfinity.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resources.Identifier;

public class CharacterCreation extends Screen {
    private static final Identifier BG = new Identifier("dragonblockinfinity", "textures/gui/gui.png");

    protected CharacterCreation(GuiGraphics graphics) {
        super(graphics);
    }
    private static final int WIDTH = 255;
    private static final int HEIGHT = 159;

    protected void renderBackground (GuiGraphics graphics) {
        graphics.drawTexture(BG, (this.width - WIDTH) / 2, (this.height - HEIGHT) / 2, 0, 0, WIDTH, HEIGHT);

        
    }
}
