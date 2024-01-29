package net.sabafly.slotmachine.game.slot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

public enum AssetImage {
    BASE("/assets/base.png", ImageType.PALETTE),
    SEVEN("/assets/seven_icon.png", ImageType.REEL_ICON),
    CHERRY("/assets/cherry_icon.png", ImageType.REEL_ICON),
    BELL("/assets/bell_icon.png", ImageType.REEL_ICON),
    GRAPE("/assets/grape_icon.png", ImageType.REEL_ICON),
    CLOWN("/assets/clown_icon.png", ImageType.REEL_ICON),
    BAR("/assets/bar_icon.png", ImageType.REEL_ICON),
    REPLAY("/assets/replay_icon.png", ImageType.REEL_ICON),
    GOGO("/assets/gogo.png", ImageType.GOGO),
    SHADOW("/assets/shadow.png", ImageType.REEL_ICON),
    LIGHTING("/assets/lighting.png", ImageType.PALETTE),
    ;

    private final BufferedImage image;
    private final ImageType type;

    public BufferedImage getImage() {
        return image;
    }

    public enum ImageType {
        PALETTE(128,128),
        REEL_ICON(31,14),
        GOGO(16, 17)
        ;
        public final Integer width;
        public final Integer height;
        ImageType(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }
    }

    AssetImage(String path, ImageType type) {
        InputStream stream = getClass().getResourceAsStream(path);
        Objects.requireNonNull(stream);
        BufferedImage image = null;
        try {
            image = ImageIO.read(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.image = image;
        this.type = type;
    }

    public ImageType getType() {
        return type;
    }
}
