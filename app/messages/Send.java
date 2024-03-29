package messages;

import network.data.Command;
import Reference.UserRef;

public class Send {
    private final Command command;
    private final UserRef userRef;
    public Send(Command command, UserRef userRef) {
        this.command = command;
        this.userRef = userRef;
    }

    public Command getCommand() {
        return command;
    }

    public UserRef getUserRef() {
        return userRef;
    }
}
