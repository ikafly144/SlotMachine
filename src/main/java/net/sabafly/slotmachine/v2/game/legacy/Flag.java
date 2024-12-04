package net.sabafly.slotmachine.v2.game.legacy;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Getter
public class Flag {
    @NotNull
    private final Set<@NotNull Role> roles;
    @Setter
    @Nullable
    private Line line;

    public Flag() {
        this(new HashSet<>());
    }

    public Flag(@NotNull Set<@NotNull Role> role) {
        this.roles = role;
    }

    public void add(@NotNull Role role) {
        this.roles.add(role);
    }

}
