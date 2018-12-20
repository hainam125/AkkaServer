package messages;

import models.UserRef;

public class Login {
    private final UserRef userRef;
    public Login(UserRef userRef) {
        this.userRef = userRef;
    }

    public UserRef getUserRef() {
        return userRef;
    }
}
