package com.qclid.portel;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;

public class PlaceholderHook {

    private final PapiParser parser;

    public PlaceholderHook() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.parser = new PapiParserImpl();
        } else {
            this.parser = new NoOpParser();
        }
    }

    public String parse(String text) {
        return parser.parse(text);
    }

    public boolean isEnabled() {
        return !(parser instanceof NoOpParser);
    }

    private interface PapiParser {
        String parse(String text);
    }

    private static class NoOpParser implements PapiParser {
        @Override
        public String parse(String text) {
            return text;
        }
    }

    // This class is only loaded if PlaceholderAPI is present
    private static class PapiParserImpl implements PapiParser {
        @Override
        public String parse(String text) {
            return PlaceholderAPI.setPlaceholders(null, text);
        }
    }
}