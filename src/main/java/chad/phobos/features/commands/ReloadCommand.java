package chad.phobos.features.commands;

import chad.phobos.Client;
import chad.phobos.api.center.Command;

public class ReloadCommand
        extends Command {
    public ReloadCommand() {
        super("reload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Client.reload();
    }
}

