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
            String parsed = PlaceholderAPI.setPlaceholders(null, text);
            // We need to escape HTML to prevent XSS if placeholders contain user input
            return escapeHtml(parsed);
        }

        private String escapeHtml(String s) {
            if (s == null) return null;
            StringBuilder out = new StringBuilder(Math.max(16, s.length()));
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                    out.append("&#");
                    out.append((int) c);
                    out.append(';');
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }
    }
}