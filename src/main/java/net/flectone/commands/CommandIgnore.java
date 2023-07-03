package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandIgnore extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isInsufficientArgs(1)) return true;

        if(fCommand.isSelfCommand()){
            fCommand.sendMeMessage("ignore.myself");
            return true;
        }

        if(!fCommand.isOfflinePlayer(strings[0])){
            fCommand.sendMeMessage("ignore.no_player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        String playerName = strings[0];
        OfflinePlayer ignoredPlayer = Bukkit.getOfflinePlayer(playerName);

        List<String> ignoreList = fCommand.getFPlayer().getIgnoreList();
        String ignoredPlayerUUID = ignoredPlayer.getUniqueId().toString();

        if(ignoreList.contains(ignoredPlayerUUID)){
            fCommand.sendMeMessage("ignore.success_unignore", "<player>", ignoredPlayer.getName());
            ignoreList.remove(ignoredPlayerUUID);
        } else {
            fCommand.sendMeMessage("ignore.success_ignore", "<player>", ignoredPlayer.getName());
            ignoreList.add(ignoredPlayerUUID);
        }

        fCommand.getFPlayer().saveIgnoreList(ignoreList);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOfflinePlayer(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}