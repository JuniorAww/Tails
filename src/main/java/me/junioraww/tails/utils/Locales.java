package me.junioraww.tails.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class Locales {
    private static MiniMessage serializer = MiniMessage.miniMessage();
    private static YamlConfiguration config;

    public static void init() {
        config = new YamlConfiguration();
        try {
            config.load("plugins/Tails/locales.yml");
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        Text.loadAll();
    }

    public enum Text {
        Balance("balance"),
        Payment("payment"),
        Inventory("inventory-title"),
        ScrollUp("scroll-up"),
        ScrollDown("scroll-down"),
        ScrollUpMax("scroll-up-max"),
        ScrollDownMax("scroll-down-max");

        private final String key;
        private Component[] translations;

        Text(String key) {
            this.key = key;
        }

        private static void loadAll() {
            for (Text text : values()) {
                String baseString = config.getString(text.key + ".en");
                if (baseString == null)
                    baseString = "<white>Empty Translation";
                Component base = serializer.deserialize(baseString);
                text.translations = new Component[]{
                        base,
                        serializer.deserialize(Objects.requireNonNullElse(config.getString(text.key + ".ru"), baseString)),
                        serializer.deserialize(Objects.requireNonNullElse(config.getString(text.key + ".ua"), baseString))
                };
            }
        }

        public Component get(Player player) {
            return switch (player.locale().getLanguage()) {
                case "ru" -> translations[1];
                case "ua" -> translations[2];
                default -> translations[0];
            };
        }

        public Component get(Player player, Object arg) {
            var replace = TextReplacementConfig.builder().match("%0").replacement(arg.toString()).build();
            return switch (player.locale().getLanguage()) {
                case "ru" -> translations[1].replaceText(replace);
                case "ua" -> translations[2].replaceText(replace);
                default -> translations[0].replaceText(replace);
            };
        }

        public Component get(Player player, Object[] args) {
            var builder = TextReplacementConfig.builder();
            for (int i = 0; i < args.length; i++) {
                builder.match("%" + i).replacement(args[i].toString());
            }
            var replace = builder.build();
            return switch (player.locale().getLanguage()) {
                case "ru" -> translations[1].replaceText(replace);
                case "ua" -> translations[2].replaceText(replace);
                default -> translations[0].replaceText(replace);
            };
        }

        public void send(Player player, Object arg) {
            player.sendMessage(get(player, arg));
        }

        public void send(Player player, Object[] args) {
            player.sendMessage(get(player, args));
        }

        public void send(Player player) {
            player.sendMessage(get(player));
        }

        public Component get(Locale locale) {
            return switch (locale.getLanguage()) {
                case "ru" -> translations[1];
                case "ua" -> translations[2];
                default -> translations[0];
            };
        }

        public String getAmpersand(Locale locale) {
            Component translation = switch (locale.getLanguage()) {
                case "ru" -> translations[1];
                case "ua" -> translations[2];
                default -> translations[0];
            };

            return LegacyComponentSerializer.legacyAmpersand().serialize(translation);
        }
    }
}
