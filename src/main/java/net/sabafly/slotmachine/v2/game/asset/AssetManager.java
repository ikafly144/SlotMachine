package net.sabafly.slotmachine.v2.game.asset;

import com.google.common.collect.MapMaker;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentMap;

public class AssetManager {

    private static final AssetManager INSTANCE = new AssetManager();

    public static AssetManager getInstance() {
        return INSTANCE;
    }

    public static final BufferedImage FALLBACK_IMAGE;

    static {
        FALLBACK_IMAGE = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = FALLBACK_IMAGE.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 8, 8);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(8, 8, 8, 8);
        graphics.setColor(Color.MAGENTA);
        graphics.fillRect(8, 0, 8, 8);
        graphics.setColor(Color.MAGENTA);
        graphics.fillRect(0, 8, 8, 8);
        graphics.dispose();
        FALLBACK_IMAGE.flush();
    }

    private final ConcurrentMap<Key, BufferedImage> VALUES = new MapMaker().weakValues().makeMap();

    private AssetManager() {
    }

    public @NotNull BufferedImage getAsset(Key key) throws AssetException {
        BufferedImage image;
        if (!VALUES.containsKey(key)) {
            InputStream stream = getClass().getResourceAsStream(getAssetPath(key));
            if (stream != null) {
                try {
                    VALUES.put(key, ImageIO.read(stream));
                } catch (IOException e) {
                    throw AssetException.ASSET_LOAD_FAILED(getAssetPath(key), e);
                }
            }
        }
        image = VALUES.get(key);
        if (image == null) {
            return FALLBACK_IMAGE;
        }
        return image;
    }

    private String getAssetPath(Key key) {
        return "/assets/" + key.namespace() + "/" + key.value() + ".png";
    }

}
