package net.sabafly.slotmachine.v2.game.slot.juggler;

import net.sabafly.slotmachine.v2.game.slot.Combo;
import static net.sabafly.slotmachine.v2.game.slot.Line.*;
import net.sabafly.slotmachine.v2.game.slot.Role;
import static net.sabafly.slotmachine.v2.game.slot.WheelPattern.*;

import java.util.List;
import java.util.Set;

public class JugglerRoles {

    public static final Role R_BIG = new Role(List.of(new Combo(SEVEN, SEVEN, SEVEN)), 0, 0);
    public static final Role R_REGULAR = new Role(List.of(new Combo(SEVEN, SEVEN, BAR)), 0, 0);
    public static final Role R_CHERRY = new Role(List.of(new Combo(CHERRY, CHERRY, LEFT, RIGHT, TOP, BOTTOM)), 7, 1); // 通常チェリー
    public static final Role R_CHERRY_B = new Role(List.of(new Combo(CHERRY, CENTER, LEFT, BOTTOM)), 7, 1); // ボーナスチェリー
    public static final Role R_CHERRY_R = new Role(List.of(new Combo(CHERRY, CENTER, LEFT, BOTTOM)), 7, 1); // ボーナスチェリー
    public static final Role R_BELL = new Role(List.of(new Combo(BELL, BELL, BELL)), 14, 14);
    public static final Role R_GRAPE = new Role(List.of(new Combo(GRAPE, GRAPE, GRAPE)), 14, 8);
    public static final Role R_CLOWN = new Role(List.of(new Combo(CLOWN, CLOWN, CLOWN)), 10, 10);
    public static final Role R_REPLAY = new Role(List.of(new Combo(REPLAY, REPLAY, REPLAY)), 0, 0);

    public static final Set<Role> R_BONUS = Set.of(R_BIG, R_REGULAR, R_CHERRY_B, R_CHERRY_R);

}
