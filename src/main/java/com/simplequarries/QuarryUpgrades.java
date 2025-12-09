package com.simplequarries;

import net.minecraft.util.math.MathHelper;

public final class QuarryUpgrades {
    private QuarryUpgrades() {}

    public static final int BASE_AREA = 5;
    public static final int MAX_AREA = 15;
    public static final int UPGRADE_STEP = 2;
    public static final int MAX_UPGRADES = (MAX_AREA - BASE_AREA) / UPGRADE_STEP; // 5 upgrades -> 15x15

    public static int clampUpgradeCount(int value) {
        return MathHelper.clamp(value, 0, MAX_UPGRADES);
    }

    public static int areaForCount(int upgradeCount) {
        return Math.min(MAX_AREA, BASE_AREA + (clampUpgradeCount(upgradeCount) * UPGRADE_STEP));
    }
}
