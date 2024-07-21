package net.sabafly.slotmachine.v2.game.slot;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Role(@NotNull List<Combo> combos, int medal2, int medal3) {

}
