package net.sabafly.slotmachine.game;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.Vec2;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.v2.game.legacy.juggler.JugglerSlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.*;

@Deprecated(forRemoval = true)
public interface Machine<T extends Machine<T>> extends Runnable, UI {
    void save(final ConfigurationLoader<? extends  ConfigurationNode> loader) throws ConfigurateException;

    UUID getUniqueId();

    MapScreen getScreen();

    default void onClick(Player clicked, Vec2 pos) {
        List<UIButton> buttons = this.getButtons();
        if (buttons != null) {
            for (UIButton button : buttons) {
                if (!(button.x() <= pos.x && pos.x <= button.x() + button.width() && button.y() <= pos.y && pos.y <= button.y() + button.height())) {
                    continue;
                }
                button.onClick().run(clicked, pos);
            }
        }
    }

    default void render(final MapGraphics<?, ?> graphics) {
        List<UIImage> images = this.getImages();
        List<UIRect> rects = this.getRects();
        List<UIText> texts = this.getTexts();

        List<UIComponent> components = new ArrayList<>();
        if (images != null) components.addAll(images);
        if (rects != null) components.addAll(rects);
        if (texts != null) components.addAll(texts);

        components.sort(Comparator.comparingInt(UIComponent::zIndex));
        for (UIComponent component : components) {
            component.renderOn(graphics);
        }
    }

    default void destroy() {
        ScreenManager.destroyMachine(this);

        File file = new File(SlotMachine.getPlugin().getDataFolder(), "data/"+ filename());
        if (!file.delete()) {
            SlotMachine.getPlugin().getLogger().warning("failed to delete file: " + file);
            file.deleteOnExit();
        }

        final Collection<Player> receivers = new HashSet<>();
        final Set<UUID> viewers = ScreenManager.getViewerMap().computeIfAbsent(this.getUniqueId(), $ -> new HashSet<>());
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (viewers.contains(player.getUniqueId())) {
                // Update
                receivers.add(player);
            }
        }

        getScreen().destroyFrames(receivers.toArray(Player[]::new));

        MapScreenRegistry.removeScreen(getScreen().getId());
    }

    static Machine<?> of(File file) throws ConfigurateException {
        CommentedConfigurationNode node = YamlConfigurationLoader.builder()
                .path(file.toPath())
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.NONE)
                .build()
                .load();
        Type type = node.node("type").get(Type.class);
        if (type == null) throw new IllegalArgumentException("type is null");
        Machine<?> machine = switch (type) {
            case SLOT -> Slot.load(node, UUID.fromString(file.getName().replace(".yml", "")));
            case JUGGLER -> JugglerSlot.load(node, UUID.fromString(file.getName().replace(".yml", "")));
        };
        return machine;
    }

    String filename();

    Type type();

    enum Type {
        @Deprecated
        SLOT,
        JUGGLER,
    }

}
