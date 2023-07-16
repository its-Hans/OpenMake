package chad.phobos.features.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.Client;
import chad.phobos.api.center.Command;

public class PrefixCommand
        extends Command {
    public PrefixCommand() {
        super("prefix", new String[]{"<char>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(ChatFormatting.GREEN + "Current prefix is " + Client.commandManager.getPrefix());
            return;
        }
        Client.commandManager.setPrefixTwo(commands[0]);
        Command.sendMessage("Prefix changed to " + ChatFormatting.GRAY + Client.commandManager.getPrefix());
    }
}

