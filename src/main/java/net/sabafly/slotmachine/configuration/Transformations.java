package net.sabafly.slotmachine.configuration;

import net.sabafly.slotmachine.SlotMachine;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static org.spongepowered.configurate.NodePath.path;

public final class Transformations {

    public static final int VERSION_LATEST = 3;

    private Transformations() {
    }

    public static ConfigurationTransformation.Versioned create() {
        return ConfigurationTransformation.versionedBuilder()
                .versionKey(Configuration.VERSION_FIELD)
                .addVersion(VERSION_LATEST, initialTransform())
                .addVersion(2, oneToTwo())
                .addVersion(1, initialTransform())
                .build();
    }

    public static ConfigurationTransformation oneToTwo() {
        return ConfigurationTransformation.builder()
                .addAction(path("prize"), (inputPath, valueAtPath) -> {
                    valueAtPath.node("large-sell").set(50);
                    valueAtPath.node("medium-sell").set(25);
                    valueAtPath.node("small-sell").set(5);
                    return null;
                })
                .build();
    }

    public static ConfigurationTransformation initialTransform() {
        return ConfigurationTransformation.builder()
                .addAction(path(), (inputPath, valueAtPath) -> {
                    Configurations config = valueAtPath.get(Configurations.class, new Configurations());
                    valueAtPath.set(config);
                    return null;
                })
                .build();
    }

    public static <N extends ConfigurationNode> N updateNode(final N node) throws ConfigurateException {
        if (!node.virtual()) { // we only want to migrate existing data
            final ConfigurationTransformation.Versioned trans = create();
            final int startVersion = trans.version(node);
            trans.apply(node);
            final int endVersion = trans.version(node);
            if (startVersion != endVersion) { // we might not have made any changes
                SlotMachine.getPlugin().getLogger().info("Updated config schema from " + startVersion + " to " + endVersion);
            }
        }
        return node;
    }
}
