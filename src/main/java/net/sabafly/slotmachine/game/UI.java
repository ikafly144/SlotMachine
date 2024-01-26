package net.sabafly.slotmachine.game;

import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.util.Vec2;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.List;

public interface UI {

    interface UIComponent {
        int zIndex();

        void renderOn(MapGraphics<?, ?> graphics);
    }

    interface ClickRunner {
        void run(Player player, Vec2 pos);
    }

    record UIButton(int x, int y, int width, int height, ClickRunner onClick) implements UIComponent {

        public int width() {
            return width - 1;
        }

        public int height() {
            return height - 1;
        }

        public int zIndex() {
            return 0;
        }

        @Override
        public void renderOn(MapGraphics<?, ?> graphics) {
            /* do nothing */
        }
    }

    record UIText(int x, int y, String text, MapFont font, byte color, int zIndex) implements UIComponent {

        @Override
        public void renderOn(MapGraphics<?, ?> graphics) {
            graphics.drawText(x, y, text, font, color, 1);
        }
    }

    record UIImage(int x, int y, BufferedImage image, int zIndex) implements UIComponent {
        @Override
        public void renderOn(MapGraphics<?, ?> graphics) {
            graphics.drawImage(image, x, y);
        }
    }

    record UIRect(int x, int y, int width, int height, byte color, int zIndex) implements UIComponent {
        @Override
        public void renderOn(MapGraphics<?, ?> graphics) {
            graphics.drawRect(x, y, width, height, color, 1);
        }
    }

    default List<UIButton> getButtons() {
        return null;
    }

    default List<UIText> getTexts() {
        return null;
    }

    default List<UIImage> getImages() {
        return null;
    }

    default List<UIRect> getRects() {
        return null;
    }

}
