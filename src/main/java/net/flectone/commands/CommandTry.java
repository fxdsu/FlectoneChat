package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandTry extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        Random random = new Random();
        int randomPer = random.nextInt(100);
        randomPer += 1;

        String formatString = Main.locale.getString("try.success_" + (randomPer >= 50))
                .replace("<player>", fCommand.getSenderName())
                .replace("<percent>", String.valueOf(randomPer));

        fCommand.sendGlobalMessage(formatString, ObjectUtil.toString(strings));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isStartsWith(strings[0], "(message)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}