package net.dragonblockinfinity.system.ki;

public abstract class KiData {
    // --- Existentes ---
    public abstract void useKi();
    public abstract int getKiAmount();
    public abstract void setKiAmount(int kiAmount);
    public abstract boolean canUseKi();
    public abstract void addKiAmount(int kiAmount);
    public abstract void removeKiAmount(int kiAmount);
    public abstract void resetKiAmount();
    public abstract void percentageKiAmount(double percentage);
    public abstract double getPercentageKiAmount();
    public abstract void setPercentageKiAmount(double percentage);
    public abstract double getMaxPercentageKiAmount();
    public abstract void setMaxPercentageKiAmount(double percentage);

    // --- KI Máximo (base + bônus de transformação) ---
    public abstract int getBaseMaxKi();
    public abstract void setBaseMaxKi(int baseMaxKi);
    public abstract int getBonusMaxKi();
    public abstract void setBonusMaxKi(int bonusMaxKi);
    public int getTotalMaxKi() {
        return getBaseMaxKi() + getBonusMaxKi();
    }

    // --- Regeneração passiva ---
    public abstract float getRegenRate();
    public abstract void setRegenRate(float rate);
    public abstract boolean isRegenEnabled();
    public abstract void setRegenEnabled(boolean enabled);
    /** Acumulador de regen fracionada entre ticks */
    public abstract float getRegenAccumulator();
    public abstract void setRegenAccumulator(float value);

    // --- Supressão de KI ---
    public abstract boolean isKiSuppressed();
    public abstract void setKiSuppressed(boolean suppressed);
    /** Power level falso exibido para outros quando suprimido */
    public abstract int getSuppressedPowerLevel();
    public abstract void setSuppressedPowerLevel(int level);

    // --- KI Sense ---
    public abstract float getKiSenseRange();
    public abstract void setKiSenseRange(float range);
    public abstract boolean isKiSenseActive();
    public abstract void setKiSenseActive(boolean active);

    // --- Drain de voo ---
    public abstract boolean isFlightDrainEnabled();
    public abstract void setFlightDrainEnabled(boolean enabled);
}
