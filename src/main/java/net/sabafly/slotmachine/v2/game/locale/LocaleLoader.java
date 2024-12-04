package net.sabafly.slotmachine.v2.game.locale;

import com.mojang.logging.LogUtils;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.sabafly.slotmachine.v2.game.data.DataFolderLoader;
import net.sabafly.slotmachine.v2.game.resources.RegistryKey;
import org.slf4j.Logger;

import java.util.Locale;

public class LocaleLoader {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    private static TranslationRegistry registry;

    private LocaleLoader() {
    }

    public static void load() {
        try {
            TranslationRegistry registry = TranslationRegistry.create(RegistryKey.create("locale"));
            DataFolderLoader.getInstance().getPath().resolve("locale").forEach(path -> {
                String basename = path.getFileName().toString();
                String woext = basename.substring(0, basename.lastIndexOf('.'));
                Locale locale = Translator.parseLocale(woext);
                if (locale == null) {
                    return;
                }
                registry.registerAll(locale, path, true);
            });
            GlobalTranslator.translator().removeSource(LocaleLoader.registry);
            GlobalTranslator.translator().addSource(registry);
            LocaleLoader.registry = registry;
        } catch (Throwable e) {
            LOGGER.trace("Unexpected error occurred while loading locale files.", e);
        }
    }

}
