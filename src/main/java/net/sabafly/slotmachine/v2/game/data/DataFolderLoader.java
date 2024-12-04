package net.sabafly.slotmachine.v2.game.data;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DataFolderLoader {

    private static DataFolderLoader INSTANCE;
    private final Path path;

    public static DataFolderLoader getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("DataFolderLoader is not initialized.");
        }
        return INSTANCE;
    }

    public static void init(Path path) {
        if (INSTANCE != null) {
            throw new IllegalStateException("DataFolderLoader is already initialized.");
        }
        INSTANCE = new DataFolderLoader(path);
    }

    private DataFolderLoader(Path path) {
        this.path = path;
    }

}
