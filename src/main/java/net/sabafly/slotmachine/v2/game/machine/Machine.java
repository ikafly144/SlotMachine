package net.sabafly.slotmachine.v2.game.machine;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.Vec2;
import net.sabafly.slotmachine.v2.game.render.Renderer;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.awt.*;
import java.util.UUID;

public sealed interface Machine<I extends Image> extends Renderer<I> permits StateMachine {

    void setup();

    void tick();

    void onClick(Player player, Vec2 vec2);

    void sendPlayers(MapScreen screen);

    ConfigurationNode save() throws SerializationException;

    UUID id();

}
