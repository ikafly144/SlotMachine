package net.sabafly.slotmachine.v2.game;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapDisplayProperties;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetText;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract non-sealed class ParaMachine extends SlotDisplay implements Machine {

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack itemStack = getMapItem();
    private final MapDisplayProperties properties = MapDisplayProperties.of(itemStack);
    @Getter(AccessLevel.PROTECTED)
    private final UUID uuid = properties.getUniqueId();
    @Getter
    private long time = 0;

    protected MapWidgetText debugTextWidget = new MapWidgetText().setText(time + "");

    private boolean _setup;

    @Override
    public final void onAttached() {
        if (!_setup) _setup = setup();
        addWidget(debugTextWidget);
    }

    @Override
    public final void onTick() {
        super.onTick();
        debugTextWidget.setText(time + "");
        tick();
        time++;
    }

    @Override
    public final void onRightClick(MapClickEvent event) {
        final MapWidget activatedWidget = getActivatedWidget();
        if (activatedWidget != null) activatedWidget.onRightClick(event);
    }

}
