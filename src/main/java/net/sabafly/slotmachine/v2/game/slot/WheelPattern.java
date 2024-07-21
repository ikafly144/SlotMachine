package net.sabafly.slotmachine.v2.game.slot;

import java.awt.image.BufferedImage;

public enum WheelPattern {
    SEVEN(AssetImage.SEVEN),
    BAR(AssetImage.BAR),
    CHERRY(AssetImage.CHERRY),
    BELL(AssetImage.BELL),
    GRAPE(AssetImage.GRAPE),
    CLOWN(AssetImage.CLOWN),
    REPLAY(AssetImage.REPLAY),
    ;

    private final AssetImage image;

    WheelPattern(AssetImage image) {
        this.image = image;
    }

    @Deprecated
    public BufferedImage getImage() {
        return image.getImage();
    }

}
