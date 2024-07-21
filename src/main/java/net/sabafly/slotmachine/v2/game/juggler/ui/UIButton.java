package net.sabafly.slotmachine.v2.game.juggler.ui;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;

import java.util.function.Consumer;

public class UIButton extends MapWidgetButton {

    private final Consumer<MapClickEvent> onClick;

    public UIButton(Consumer<MapClickEvent> onClick) {
        super();
        this.onClick = onClick;
    }

    @Override
    public void onRightClick(MapClickEvent event) {
        onClick.accept(event);
    }

}
