package net.sabafly.slotmachine.v2.game.slot;

public enum Pos {
    LEFT,
    CENTER,
    RIGHT,
    ;

    public int getIndex() {
        return this.ordinal();
    }
    public static Pos valueOf(int index) {
        return Pos.values()[index];
    }
}
