package net.sabafly.slotmachine.v2.game.slot;

public record Setting(int big, int reg, int big_c, int reg_c, int bell, int cherry,
                      int grape, int replay, int clown) {
    public static Setting Debug = new Setting(0, 0, 32768, 32768, 0, 0, 0, 0, 0);
    public static Setting Bonus = new Setting(0, 0, 0, 0, 0, 0, 57344, 8192, 0);
}
