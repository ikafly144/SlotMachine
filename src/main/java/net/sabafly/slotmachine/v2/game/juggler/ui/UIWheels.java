package net.sabafly.slotmachine.v2.game.juggler.ui;

import com.bergerkiller.bukkit.common.map.MapBlendMode;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import lombok.AccessLevel;
import lombok.Getter;
import net.sabafly.slotmachine.v2.game.juggler.JugglerMachine;
import net.sabafly.slotmachine.v2.game.slot.AssetImage;
import net.sabafly.slotmachine.v2.game.slot.Pos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class UIWheels<T extends JugglerMachine> extends MapWidget {

    @Getter(AccessLevel.PROTECTED)
    private final T machine;

    public UIWheels(T machine) {
        super();
        setPosition(16, 16);
        this.machine= machine;
    }

    net.sabafly.slotmachine.v2.game.slot.Wheel getWheel(Pos pos) {
        return getMachine().getWheel(pos);
    }

    @Override
    public void onTick() {
        super.onTick();
    }

    @Override
    public void onDraw() {
        byte[] buff = MapColorPalette.convertImage(getSlotImage());
        final int width = AssetImage.ImageType.REEL_ICON.width;
        final int height = AssetImage.ImageType.REEL_ICON.height;
        final int length = 3;
        getDisplay().getTopLayer().setBlendMode(MapBlendMode.NONE)
                .clear()
                .drawRawData(0, 0, width * length + length - 1, height * length + length - 1, buff);
    }

    protected BufferedImage getSlotImage() {
        long tick = getMachine().getTime();
        final ArrayList<ArrayList<BufferedImage>> reelImages = new ArrayList<>();
        for (final net.sabafly.slotmachine.v2.game.slot.Pos pos : net.sabafly.slotmachine.v2.game.slot.Pos.values()) {
            ArrayList<BufferedImage> images = new ArrayList<>();
            final net.sabafly.slotmachine.v2.game.slot.Wheel wheel = getWheel(pos);
            if (wheel.isRunning() && (tick + pos.getIndex()) % 2 == 0) images.add(wheel.getImage(3));
            images.add(wheel.getImage(2));
            images.add(wheel.getImage(1));
            images.add(wheel.getImage(0));
            reelImages.add(images);
        }
        final int width = net.sabafly.slotmachine.v2.game.slot.AssetImage.ImageType.REEL_ICON.width;
        final int height = net.sabafly.slotmachine.v2.game.slot.AssetImage.ImageType.REEL_ICON.height;
        final int length = reelImages.size();
        final BufferedImage combinedImage = new BufferedImage(width * length + length - 1, height * length + length - 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics combinedGraphics = combinedImage.getGraphics();
        final boolean shadow = (tick / 5) % 2 == 0 && false; // highlightFlag != null && highlightLine != null;
        for (final net.sabafly.slotmachine.v2.game.slot.Pos pos : net.sabafly.slotmachine.v2.game.slot.Pos.values()) {

            final ArrayList<BufferedImage> images = reelImages.get(pos.getIndex());

            for (int i = 0; i < images.size(); i++) {
                int y = i * height + i;
                if (getWheel(pos).isRunning() && (tick + pos.getIndex()) % 2 == 0) y -= height / 2;
                combinedGraphics.drawImage(images.get(i), pos.getIndex() * width + pos.getIndex(), y, null);
                if (shadow) // && highlightLine.get(pos) == 2 - i && highlightFlag.getWheelPatterns().length > pos.getIndex())
                    combinedGraphics.drawImage(net.sabafly.slotmachine.v2.game.slot.AssetImage.SHADOW.getImage(), pos.getIndex() * width + pos.getIndex(), y, null);
            }

        }

        combinedGraphics.dispose();

        return combinedImage;
    }

}
