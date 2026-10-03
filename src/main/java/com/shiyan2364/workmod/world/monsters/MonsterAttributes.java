package com.shiyan2364.workmod.world.monsters;

/**
 * 怪物属性数据（按宏伟度计算）
 * 基准：原版僵尸 血量20 伤害3
 */
public class MonsterAttributes {
    private final int health;
    private final int damage;
    private final int armor;
    private final float speed;
    private final int magnificence;

    public MonsterAttributes(int health, int damage, int armor, float speed, int magnificence) {
        this.health = health;
        this.damage = damage;
        this.armor = armor;
        this.speed = speed;
        this.magnificence = magnificence;
    }

    public int getHealth() { return health; }
    public int getDamage() { return damage; }
    public int getArmor() { return armor; }
    public float getSpeed() { return speed; }
    public int getMagnificence() { return magnificence; }

    /**
     * 根据宏伟度计算属性
     * 血量倍率：宏1=1x ... 宏10=32x
     * 伤害倍率：宏1=1x ... 宏10=6x
     */
    public static MonsterAttributes fromMagnificence(int magnificence) {
        // 血量倍率
        int healthMultiplier = switch (magnificence) {
            case 1 -> 1; case 2 -> 2; case 3 -> 3; case 4 -> 4;
            case 5 -> 6; case 6 -> 8; case 7 -> 12; case 8 -> 18;
            case 9 -> 25; default -> 32;
        };
        // 伤害倍率
        int damageMultiplier = switch (magnificence) {
            case 1 -> 1; case 2 -> 1; case 3 -> 1; case 4 -> 2;
            case 5 -> 2; case 6 -> 3; case 7 -> 3; case 8 -> 4;
            case 9 -> 5; default -> 6;
        };
        // 护甲
        int armor = switch (magnificence) {
            case 1, 2 -> 0; case 3, 4 -> 4; case 5, 6 -> 8;
            case 7, 8 -> 12; case 9 -> 16; default -> 20;
        };
        // 移速
        float speed = 1.0f + (magnificence - 1) * 0.03f;

        return new MonsterAttributes(
                20 * healthMultiplier,
                3 * damageMultiplier,
                armor,
                speed,
                magnificence
        );
    }
}
