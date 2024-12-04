package net.sabafly.slotmachine.v2.game.util;

import net.sabafly.slotmachine.v2.SlotMachine;
import org.slf4j.Logger;

public class LogUtils {

    public static Logger getLogger() {
        return SlotMachine.getPlugin(SlotMachine.class).getSLF4JLogger();
    }

}
