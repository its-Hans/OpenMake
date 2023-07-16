package chad.phobos.features.commands;

import chad.phobos.api.center.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.Client;
import chad.phobos.api.center.Command;

public class ToggleCommand
        extends Command {
    public ToggleCommand() {
        super("toggle", new String[]{"<module>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            ToggleCommand.sendMessage("Please specify a module.");
            return;
        }
        String moduleName = commands[0];
        Module module = Client.moduleManager.getModuleByName(moduleName);
        if (module == null) {
            ToggleCommand.sendMessage("Unknown module!");
            return;
        }
        /*
        String setto = (ChatFormatting.RED + "module " + ChatFormatting.RESET + module.getName() + " set ");
        if (module.enabled.getValue()) {
            module.enabled.setValue(false);
            sendMessage(setto + "off");
        } else {
            module.enabled.setValue(true);
            sendMessage(setto + "on");
        }
        */
        module.enabled.setValue(!module.enabled.getValue());
        String setMessage = ("module " + ChatFormatting.AQUA + module.getDisplayName() + ChatFormatting.RESET + " now ");
        String green = (ChatFormatting.GREEN + "on");
        String red = (ChatFormatting.RED + "off");
        if (module.enabled.getValue()) {
            sendMessage(setMessage + green);
        } else {
            sendMessage(setMessage + red);
        }
    }
}

