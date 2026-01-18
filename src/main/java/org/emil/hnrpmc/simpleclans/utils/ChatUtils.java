package org.emil.hnrpmc.simpleclans.utils;

import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class ChatUtils {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(%([A-Za-z]+)%)");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    private ChatUtils() {}

    /**
     * Erstellt eine Component aus einem Legacy-String (& oder §)
     */
    public static String parseColors(@NotNull String text) {
        // In NeoForge nutzen wir am besten den LegacyComponentSerializer oder
        // parsen manuell. Hier eine einfache Implementierung für '&'
        return text.replace('&', '§');
    }

    public static String getLastColorCode(String msg) {
        if (msg.length() < 2) {
            return "";
        }

        String one = msg.substring(msg.length() - 2, msg.length() - 1);
        String two = msg.substring(msg.length() - 1);

        if (one.equals("§")) {
            return one + two;
        }

        if (one.equals("&")) {
            return getColorByChar(two.charAt(0));
        }

        return "";
    }

    /**
     * Überträgt die Formatierung der vorherigen Zeile auf die nächste,
     * wenn diese keinen eigenen Farbcode hat.
     *
     * @param lines Die Liste der zu formatierenden Strings
     */
    public static void applyLastColorToFollowingLines(@NotNull List<String> lines) {
        if (lines.isEmpty()) return;

        // Sicherstellen, dass die erste Zeile eine Farbe hat (Standard Weiß)
        if (lines.get(0).isEmpty() || lines.get(0).charAt(0) != '§') {
            lines.set(0, ChatFormatting.WHITE + lines.get(0));
        }

        for (int i = 1; i < lines.size(); i++) {
            String previousLine = lines.get(i - 1);
            String currentLine = lines.get(i);

            // Wenn die aktuelle Zeile nicht mit einem Farbcode beginnt
            if (currentLine.isEmpty() || currentLine.charAt(0) != '§') {
                // Hole alle Formatierungen der vorherigen Zeile und setze sie davor
                lines.set(i, getLastColors(previousLine) + currentLine);
            }
        }
    }

    public static String getColorByChar(char character) {
        ChatFormatting color = ChatFormatting.getByCode(character);
        return color != null ? color.toString() : Character.toString(character);
    }

    /**
     * Sucht alle Formatierungscodes am Ende eines Strings.
     * In Minecraft Native nutzen wir dafür ChatFormatting.
     */
    @NotNull
    public static String getLastColors(@NotNull String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();

        // Wir gehen den String von hinten nach vorne durch
        for (int index = length - 2; index >= 0; index--) {
            if (input.charAt(index) == '§') {
                char code = input.charAt(index + 1);
                ChatFormatting formatting = ChatFormatting.getByCode(code);

                if (formatting != null) {
                    result.insert(0, "§" + code);

                    // Wenn es eine Farbe ist (Resetting), brechen wir ab.
                    // Wenn es ein Format (Bold, Italic) ist, suchen wir weiter nach der Farbe.
                    if (formatting.isColor()) {
                        break;
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Entfernt alle Farbcodes aus einem String
     */
    public static String stripColors(String text) {
        if (text == null) return "";
        // Nutzt die interne Minecraft-Methode zum Säubern
        return ChatFormatting.stripFormatting(text);
    }

    /**
     * Erstellt klickbare Komponenten für NeoForge
     */
    public static Component toComponent(@Nullable ServerPlayer receiver, @NotNull String text) {
        MutableComponent root = Component.empty();

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // Text vor dem Platzhalter
            root.append(Component.literal(text.substring(lastEnd, matcher.start())));

            // Den Platzhalter als klickbare Komponente einfügen
            String placeholderKey = matcher.group(2);
            root.append(createClickablePlaceholder(receiver, placeholderKey));

            lastEnd = matcher.end();
        }

        // Restlicher Text nach dem letzten Platzhalter
        root.append(Component.literal(text.substring(lastEnd)));

        return root;
    }

    private static Component createClickablePlaceholder(@Nullable ServerPlayer receiver, String placeholder) {
        String localizedText = lang("clickable." + placeholder, receiver);
        String hoverText = lang("hover.click.to." + placeholder, receiver);

        return Component.literal(localizedText)
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + placeholder))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText)))
                );
    }
}