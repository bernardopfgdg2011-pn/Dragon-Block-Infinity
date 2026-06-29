package net.dragonblockinfinity.system.ki;

public interface IKiSystem {
    // --- Existentes ---
    void useKi(KiData kiData);
    int getKiAmount(KiData kiData);
    void setMaxKiAmount(KiData kiData, int maxKiAmount);
    void setKiAmount(KiData kiData, int kiAmount);
    void addKiAmount(KiData kiData, int kiAmount);
    void removeKiAmount(KiData kiData, int kiAmount);
    void resetKiAmount(KiData kiData);
    void percentageKiAmount(KiData kiData, double percentage);
    double getPercentageKiAmount(KiData kiData);
    void setPercentageKiAmount(KiData kiData, double percentage);
    double getMaxPercentageKiAmount(KiData kiData);
    void setMaxPercentageKiAmount(KiData kiData, double percentage);

    // --- KI Máximo ---
    int getTotalMaxKi(KiData kiData);
    void setBaseMaxKi(KiData kiData, int base);
    void applyBonusMaxKi(KiData kiData, int bonus);
    void removeBonusMaxKi(KiData kiData, int bonus);

    // --- Regeneração ---
    void tickRegen(KiData kiData);
    void setRegenRate(KiData kiData, float rate);
    void setRegenEnabled(KiData kiData, boolean enabled);

    // --- Drain ---
    void drainForFlight(KiData kiData);
    void drainForAttack(KiData kiData, int amount);

    // --- Supressão ---
    void suppressKi(KiData kiData, int fakePowerLevel);
    void unsuppressKi(KiData kiData);
    int getVisiblePowerLevel(KiData kiData);

    // --- KI Sense ---
    void activateKiSense(KiData kiData, float range);
    void deactivateKiSense(KiData kiData);
    boolean isKiSenseActive(KiData kiData);
}
