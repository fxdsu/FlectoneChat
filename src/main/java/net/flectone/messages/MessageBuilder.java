package net.flectone.messages;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class MessageBuilder {

    private static final Pattern urlPattern = Pattern.compile("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?+-=\\\\.&]*)", Pattern.CASE_INSENSITIVE);
    private static final HashMap<String, String> patternMap = new HashMap<>();

    static {
        loadPatterns();
    }

    private final LinkedHashMap<Integer, WordParams> messageHashMap = new LinkedHashMap<>();
    private final ComponentBuilder componentBuilder = new ComponentBuilder();
    private final ItemStack itemStack;
    private final String command;

    public MessageBuilder(@NotNull String command, @NotNull String text, @Nullable ItemStack itemStack, boolean clickable) {
        this.itemStack = itemStack;
        this.command = command;

        String pingPrefix = locale.getString("chat.ping.prefix");

        AtomicInteger index = new AtomicInteger();

        Arrays.stream(text.split(" ")).parallel().map(word -> {
            WordParams wordParams = new WordParams();

            word = replacePattern(word);

            if (itemStack != null && word.equalsIgnoreCase("%item%")) {
                wordParams.setItem(true);
                wordParams.setText("\uD83D\uDD32");
                return wordParams;
            }

            if (word.startsWith(pingPrefix)) {
                String playerName = word.replaceFirst(pingPrefix, "");

                FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);
                if (fPlayer != null && fPlayer.isOnline() && fPlayer.getPlayer() != null) {
                    Player player = fPlayer.getPlayer();

                    word = locale.getString("chat.ping.message")
                            .replace("<player>", player.getName())
                            .replace("<prefix>", pingPrefix);

                    wordParams.setClickable(clickable, player.getName());
                    wordParams.setPlayerPing(true);

                    if (command.equals("globalchat")) ObjectUtil.playSound(player, "chatping");

                }
            }

            if (word.startsWith("||") && word.endsWith("||") && !word.replace("||", "").isEmpty()) {
                word = word.replace("||", "");

                wordParams.setHideMessage(word);
                wordParams.setHide(true);

                word = locale.getString("chat.hide.message")
                        .repeat(word.length());
            }

            Matcher urlMatcher = urlPattern.matcher(word);
            if (urlMatcher.find()) {
                wordParams.setUrl(word.substring(urlMatcher.start(0), urlMatcher.end(0)));

                word = locale.getString("chat.url.message")
                        .replace("<url>", word);
            }

            wordParams.setText(word);
            return wordParams;

        })
        .forEachOrdered(wordParams -> messageHashMap.put(index.getAndIncrement(), wordParams));
    }

    public static void loadPatterns() {
        patternMap.clear();

        config.getStringList("chat.patterns")
                .forEach(patternString -> {
                    String[] patternComponents = patternString.split(" , ");
                    if (patternComponents.length < 2) return;

                    patternMap.put(patternComponents[0], patternComponents[1]);
                });
    }

    @NotNull
    public String getMessage() {
        return messageHashMap.values().parallelStream()
                .map(wordParams -> {
                    String word = wordParams.getText();
                    if (wordParams.isEdited()) {
                        word = ObjectUtil.formatString(word, null);
                        word = ChatColor.stripColor(word);
                    }
                    return word;
                })
                .collect(Collectors.joining(" "));
    }

    @NotNull
    public BaseComponent[] build(@NotNull String format, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        String[] formats = ObjectUtil.formatString(format, recipient, sender).split("<message>");
        componentBuilder.append(FComponent.createPlayer(recipient, sender, formats[0]).get());

        String color = ChatColor.getLastColors(formats[0]);

        componentBuilder.append(buildMessage(color, recipient, sender), ComponentBuilder.FormatRetention.NONE);

        if (formats.length > 1)
            componentBuilder.append(TextComponent.fromLegacyText(color + formats[1]), ComponentBuilder.FormatRetention.NONE);

        return componentBuilder.create();
    }

    @NotNull
    private BaseComponent[] buildMessage(@NotNull String lastColor, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        for (Map.Entry<Integer, WordParams> entry : messageHashMap.entrySet()) {
            String word = entry.getValue().getText();
            WordParams wordParams = entry.getValue();

            if ((sender.isOp() || sender.hasPermission("flectonechat.formatting")) && !wordParams.isEdited()) {
                String color1 = ChatColor.getLastColors(word);
                word = ObjectUtil.formatString(true, word, recipient, sender);
                String color2 = ChatColor.getLastColors(word);

                wordParams.setFormatted(!color1.equals(color2));
                wordParams.setText(word);
            }

            word = ObjectUtil.formatString(lastColor + word, recipient, sender);
            FComponent wordComponent = new FComponent(word);

            if (!wordParams.isEdited() || wordParams.isFormatted())
                lastColor = ChatColor.getLastColors(lastColor + word);

            if (wordParams.isItem()) {
                componentBuilder.append(createItemComponent(itemStack, lastColor, recipient, sender));
                continue;
            }

            if (wordParams.isClickable()) {
                wordComponent = FComponent.createPlayer(recipient, FPlayerManager.getPlayerFromName(wordParams.getPlayerPingName()).getPlayer(), word);
            }

            if (wordParams.isUrl()) {
                wordComponent = FComponent.createURL(recipient, sender, word, wordParams.getUrl());
            }

            if (wordParams.isHide()) {
                wordComponent.addHoverText(lastColor + wordParams.getHideMessage());
            }

            componentBuilder
                    .append(wordComponent.get(), ComponentBuilder.FormatRetention.NONE)
                    .append(" ");
        }

        return componentBuilder.create();
    }

    @NotNull
    private BaseComponent[] createItemComponent(@NotNull ItemStack itemStack, @NotNull String lastColor, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder itemBuilder = new ComponentBuilder();

        String[] formattedItemArray = NMSUtil.getFormattedStringItem(itemStack);

        FComponent byItemComponent = new FComponent(new TranslatableComponent(formattedItemArray[0]))
                .addHoverItem(formattedItemArray[1]);

        String[] componentsStrings = locale.getFormatString("chat.tooltip.message", recipient, sender).split("<tooltip>");
        BaseComponent[] color = TextComponent.fromLegacyText(lastColor);

        return itemBuilder
                .append(color)
                .append(TextComponent.fromLegacyText(componentsStrings[0]))
                .append(byItemComponent.get())
                .append(TextComponent.fromLegacyText(componentsStrings[1]))
                .append(color)
                .append(" ")
                .create();
    }

    @NotNull
    public BaseComponent[] create() {
        return componentBuilder.create();
    }

    @NotNull
    private String replacePattern(@NotNull String word) {
        String wordLowerCased = word.toLowerCase();

        Map.Entry<String, String> pattern = patternMap.entrySet()
                .parallelStream()
                .filter(entry -> wordLowerCased.contains(entry.getKey().toLowerCase()))
                .findFirst()
                .orElse(null);

        if (pattern == null) return word;

        return wordLowerCased.replace(pattern.getKey().toLowerCase(), pattern.getValue());
    }
}
