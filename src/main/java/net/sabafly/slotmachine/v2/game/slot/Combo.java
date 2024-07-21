package net.sabafly.slotmachine.v2.game.slot;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static net.sabafly.slotmachine.v2.game.slot.Line.*;

public class Combo {

    @NotNull
    private final WheelPattern[] patterns;
    @Nullable
    @Getter
    private final Line[] lines;

    public Combo(WheelPattern pattern, @Nullable Line... line) {
        this(new WheelPattern[]{pattern}, line);
    }

    public Combo(WheelPattern pattern1, WheelPattern pattern2, @Nullable Line... line) {
        this(new WheelPattern[]{pattern1, pattern2}, line);
    }

    public Combo(WheelPattern pattern1, WheelPattern pattern2, WheelPattern pattern3, @Nullable Line... line) {
        this(new WheelPattern[]{pattern1, pattern2, pattern3}, line);
    }

    private Combo(WheelPattern[] patterns, @Nullable Line... lines) {
        this.patterns = patterns;
        this.lines = lines == null || lines.length == 0 ? new Line[]{TOP,CENTER,BOTTOM,LEFT,RIGHT} : lines ;
    }

    @Unmodifiable
    @Range(from = 1, to = 3)
    public List<WheelPattern> getPatterns() {
        return List.of(patterns);
    }

    public boolean match(WheelPattern[] l, WheelPattern[] c, WheelPattern[] r) {
        return match(l, c, r, 0);
    }

    public boolean match(WheelPattern[] l, WheelPattern[] c, WheelPattern[] r, int range) {
        for (int i = 0; i < patterns.length; i++) {
            switch (Pos.valueOf(i)) {
                case LEFT -> {
                    if (l == null || !patterns[i].equals(l[range])) return false;
                }
                case CENTER -> {
                    if (c == null || !patterns[i].equals(c[range])) return false;
                }
                case RIGHT -> {
                    if (r == null || !patterns[i].equals(r[range])) return false;
                }
            }
        }
        return true;
    }

}
