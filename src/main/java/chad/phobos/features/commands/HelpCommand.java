package chad.phobos.features.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.Client;
import chad.phobos.api.center.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("Commands: ");
        for (Command command : Client.commandManager.getCommands()) {
            HelpCommand.sendMessage(ChatFormatting.GRAY + Client.commandManager.getPrefix() + command.getName());
        }
    }
}

