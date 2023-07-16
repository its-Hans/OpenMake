package chad.phobos.features.commands;

import chad.phobos.Client;
import chad.phobos.api.center.Command;

public class UnloadCommand
        extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Client.unload(true);
    }
}

