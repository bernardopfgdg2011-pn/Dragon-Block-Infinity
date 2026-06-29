package net.dragonblockinfinity.mixin;

import net.dragonblockinfinity.system.ki.KiData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerData {

    @Unique
    private KiData dragonBlockInfinity$kiData = null;

    @Unique
    public KiData dragonBlockInfinity$getKiData() {
        return dragonBlockInfinity$kiData;
    }

    @Unique
    public void dragonBlockInfinity$setKiData(KiData kiData) {
        this.dragonBlockInfinity$kiData = kiData;
    }
}
