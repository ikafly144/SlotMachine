package net.sabafly.slotmachine.game;

import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.util.Vec2;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.game.slot.AssetImage;
import net.sabafly.slotmachine.game.slot.SlotRegistry.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

public class Slot extends ParaMachine implements Listener {
    private final Wheels wheels;
    private Status status = Status.IDLE;
    private boolean isDebug = false;
    private long coin = 0L;
    private int payOut = 0;
    private Flag estFlag = null;
    private int gameCount = 0;
    private int bonusCoinCount = 0;
    private Flag bonusFlag = null;
    private boolean bonusAnnounced = false;
    private Vec2 lastClickedPos = null;

    public Slot(final BukkitScheduler scheduler, final Wheels wheels) {
        super();
        this.wheels = wheels;
        scheduler.runTaskAsynchronously(SlotMachine.getPlugin(), this);
    }

    public boolean isBonus() {
        return bonusFlag != null;
    }

    public boolean isBonusAnnounced() {
        return bonusAnnounced;
    }

    @Override
    public void run() {
        final MapGraphics<?, ?> graphics = screen.getGraphics();
        switch (status) {
            case IDLE -> {
                graphics.fillComplete(ColorCache.rgbToMap(0xff,0xff,0xff));
                graphics.drawImage(AssetImage.IDLE.getImage(), 0, 0);
            }
            case PLAYING -> {
                graphics.fillRect(0, 0, 128, 128, ColorCache.rgbToMap(0xff, 0xff, 0xff), 0xff);
                graphics.drawImage(AssetImage.BASE.getImage(), 0, 0);
                graphics.drawImage(wheels.getImage(tick), 16, 16);
                if (isDebug) graphics.drawText(0, 0, "t:" + tick + "\nf:" + (estFlag != null ? estFlag.toString() : "") + "\ng:" + gameCount + "\ncredit:" + coin, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0, 0, 0xff), 1);
                graphics.drawText(25, 81, "" + coin, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                if (isBonus()) graphics.drawText(55, 81, "" + bonusCoinCount, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                graphics.drawText(88, 81, "" + payOut, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                graphics.drawText(40, 3, "JUGGLER", MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                graphics.drawImage(AssetImage.LIGHTING.getImage(), 0, 0);
                if (isBonus()) graphics.drawImage(AssetImage.GOGO.getImage(), 0, 47);
                tick++;
                wheels.step();
            }
            case MAINTENANCE -> {
                graphics.fillComplete(ColorCache.rgbToMap(0xff,0xff,0xff));
                graphics.drawText(0, 48, "CALL STAFF", ColorCache.rgbToMap(0xff, 0xff, 0xff), 0);
            }
        }
        if (isDebug && lastClickedPos != null) graphics.drawRect(lastClickedPos.x, lastClickedPos.y, 0, 0, ColorCache.rgbToMap(0xff, 0x00, 0xff), 1);
        super.sendPlayers();
    }

    public enum Status {
        IDLE(false),
        PLAYING(false),
        MAINTENANCE(false),
        ;
        Status(boolean play) {}
    }

}
