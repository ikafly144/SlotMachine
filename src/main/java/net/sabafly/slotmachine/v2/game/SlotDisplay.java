package net.sabafly.slotmachine.v2.game;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public sealed class SlotDisplay extends MapDisplay permits ParaMachine {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    protected SlotDisplay() {
        super();
    }

    @Override
    public void onAttached() {
        LOGGER.info("Map display attached");
    }

    @Override
    public void onDetached() {
        LOGGER.info("Map display detached");
    }

    @Override
    public void onTick() {
        super.onTick();
    }

}
