package net.sabafly.slotmachine.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class Medals {
    public Map<UUID, Long> medalMap = new HashMap<>();

    public Medals() {
    }
    public Medals(Map<UUID, Long> medalMap) {
        this.medalMap = medalMap;
    }
}
