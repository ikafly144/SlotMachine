package net.sabafly.slotmachine.v2.game.machine;

import com.google.common.base.Preconditions;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.Vec2;
import lombok.Getter;
import net.sabafly.slotmachine.v2.game.interaction.InteractionComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public abstract sealed class BaseMachine<I extends Image> implements StateMachine<I> permits Juggler {

    @Getter
    private long time = 0;

    public static <M extends BaseMachine<I>,I extends BufferedImage> BaseMachine<I> load(M machine, ConfigurationNode node) throws SerializationException {
        machine.setState(State.valueOf(node.node("state").getString()));
        machine.doLoad(node);
        return machine;
    }

    @Override
    public ConfigurationNode save() throws SerializationException {
        ConfigurationNode node = CommentedConfigurationNode.root();
        node.node("id").set(id());
        node.node("state").set(getState().name());
        doSave(node);
        return node;
    }

    protected abstract void doSave(ConfigurationNode node) throws SerializationException;

    protected abstract void doLoad(ConfigurationNode node) throws SerializationException;

    private boolean _setup;

    private final UUID id;

    private State state = State.IDLE;

    private final List<InteractionComponent> interactionComponents = new ArrayList<>();

    public BaseMachine() {
        this(UUID.randomUUID());
    }

    protected BaseMachine(UUID uuid) {
        this.id = uuid;
    }

    @Override
    public final void setup() {
        if (_setup) {
            return;
        }
        doSetup();
        _setup = true;
    }

    public abstract void doSetup();

    @Override
    public final @NotNull State getState() {
        return state;
    }

    @Override
    public final void setState(@NotNull State state) {
        Preconditions.checkNotNull(state, "State cannot be null");
        onStateChange(this.state, this.state = state);
    }

    protected void onStateChange(State from, State to) {}

    @Override
    public final void onClick(Player player, Vec2 vec2) {
        for (InteractionComponent component : interactionComponents) {
            component.onClick(player, vec2);
        }
    }

    protected final void addInteractionComponent(InteractionComponent interactionComponent) {
        Preconditions.checkArgument(!_setup, "Cannot add interaction components after setup");
        interactionComponents.add(interactionComponent);
    }

    @Override
    public final void tick() {
        time++;
        doTick();
    }

    public abstract void doTick();

    private final Set<UUID> viewers = new HashSet<>();
    public static final double MAX_DIST = Math.pow(48, 2);

    @Override
    public final void sendPlayers(MapScreen screen) {
        final Collection<Player> receivers = new HashSet<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(screen.getLocation().getWorld())) {
                final double dist = player.getLocation().distanceSquared(screen.getLocation());
                if (viewers.contains(player.getUniqueId()) && dist > MAX_DIST) {
                    // Remove
                    viewers.remove(player.getUniqueId());
                    screen.destroyFrames(player);
                } else if (!viewers.contains(player.getUniqueId()) && dist < MAX_DIST) {
                    // Add
                    viewers.add(player.getUniqueId());
                    screen.spawnFrames(player);
                    screen.sendMaps(true, player);
                }
            } else {
                viewers.remove(player.getUniqueId());
            }
            if (viewers.contains(player.getUniqueId())) {
                // Update
                receivers.add(player);
            }
        }
        if (!receivers.isEmpty()) {
            screen.sendMaps(false, receivers);
        }
        for (final UUID uuid : Set.copyOf(viewers)) {
            if (Bukkit.getPlayer(uuid) == null) {
                viewers.remove(uuid);
            }
        }
    }

    @Override
    public UUID id() {
        return id;
    }
}
