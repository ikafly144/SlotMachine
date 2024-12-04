package net.sabafly.slotmachine.v2.game.legacy.juggler;

import net.sabafly.slotmachine.v2.game.legacy.Flag;
import net.sabafly.slotmachine.v2.game.legacy.FlagGenerator;
import net.sabafly.slotmachine.v2.game.legacy.Role;
import net.sabafly.slotmachine.v2.game.legacy.Setting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class JFlagGenerator implements FlagGenerator {

    private final RandomGenerator rng = RandomGeneratorFactory.all()
            .max(Comparator.comparingInt(RandomGeneratorFactory::stateBits))
            .orElse(RandomGeneratorFactory.of("Random"))
            .create();

    @Override
    public Flag generate(Setting setting) {
        int i = rng.nextInt(1 << 16);
        Set<@NotNull Role> roles = new HashSet<>();

        i -= setting.cherry();
        if (i < 0) {
            roles.add(JugglerRoles.R_CHERRY);
            return new Flag(roles);
        }
        i -= setting.grape();
        if (i < 0) {
            roles.add(JugglerRoles.R_GRAPE);
            return new Flag(roles);
        }
        i -= setting.bell();
        if (i < 0) {
            roles.add(JugglerRoles.R_BELL);
            return new Flag(roles);
        }
        i -= setting.replay();
        if (i < 0) {
            roles.add(JugglerRoles.R_REPLAY);
            return new Flag(roles);
        }
        i -= setting.clown();
        if (i < 0) {
            roles.add(JugglerRoles.R_CLOWN);
            return new Flag(roles);
        }
        i -= setting.big();
        if (i < 0) {
            roles.add(JugglerRoles.R_BIG);
            return new Flag(roles);
        }
        i -= setting.reg();
        if (i < 0) {
            roles.add(JugglerRoles.R_REGULAR);
            return new Flag(roles);
        }
        i -= setting.big_c();
        if (i < 0) {
            roles.add(JugglerRoles.R_BIG);
            roles.add(JugglerRoles.R_CHERRY_B);
            return new Flag(roles);
        }
        i -= setting.reg_c();
        if (i < 0) {
            roles.add(JugglerRoles.R_REGULAR);
            roles.add(JugglerRoles.R_CHERRY_B);
            return new Flag(roles);
        }

        return new Flag(roles);

    }
}
