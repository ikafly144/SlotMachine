package net.sabafly.slotmachine.v2.game.slot;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

public enum AssetImage {
    BASE("/assets/base.png", ImageType.PALETTE),
    SEVEN("/assets/seven_icon.png", ImageType.REEL_ICON),
    CHERRY("/assets/cherry_icon.png", ImageType.REEL_ICON),
    BELL("/assets/bell_icon.png", ImageType.REEL_ICON),
    GRAPE(("/assets/grape_icon.png"), ImageType.REEL_ICON),
    CLOWN(("/assets/clown_icon.png"), ImageType.REEL_ICON),
    BAR(("/assets/bar_icon.png"), ImageType.REEL_ICON),
    REPLAY(("/assets/replay_icon.png"), ImageType.REEL_ICON),
    GOGO(("/assets/gogo.png"), ImageType.GOGO),
    SHADOW("/assets/shadow.png", ImageType.REEL_ICON),
    LIGHTING("/assets/lighting.png", ImageType.PALETTE),
    ;

    @NotNull
    private final String path;
    private BufferedImage image;
    @Getter
    private final ImageType type;

    public BufferedImage getImage() {
        if (image == null) {
            InputStream stream = getClass().getResourceAsStream(path);
            Objects.requireNonNull(stream);
            try {
                image = ImageIO.read(stream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }

    public enum ImageType {
        PALETTE(128, 128),
        REEL_ICON(31, 14),
        GOGO(16, 17);
        public final Integer width;
        public final Integer height;

        ImageType(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }
    }

    AssetImage(@NotNull String path, ImageType type) {
        this.path = path;
        this.type = type;
    }

}
