package net.sabafly.slotmachine.v2.game.asset;

import java.io.IOException;

public class AssetException extends Exception {

    public static AssetException ASSET_LOAD_FAILED(String string, IOException e) {
        return new AssetException("Failed to createId asset: " + string, e);
    }

    private AssetException(String message) {
        super(message);
    }

    private AssetException(String message, Throwable cause) {
        super(message, cause);
    }

}
