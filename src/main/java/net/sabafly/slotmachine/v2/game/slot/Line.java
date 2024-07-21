package net.sabafly.slotmachine.v2.game.slot;

import org.jetbrains.annotations.Range;

public enum Line {
    // スロットの揃うライン
    TOP(2, 2, 2),
    CENTER(1, 1, 1),
    BOTTOM(0, 0, 0),
    LEFT(0, 1, 2),
    RIGHT(2, 1, 0),
    ;
    final int left;
    final int center;
    final int right;

    Line(int left, int center, int right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    public int get(Pos pos) {
        return get(pos.getIndex());
    }

    public int get(@Range(from = 0, to = 2) int i) {
        return switch (i) {
            case 0 -> left;
            case 1 -> center;
            case 2 -> right;
            default -> throw new IllegalArgumentException("Unexpected value: " + i);
        };
    }

}
