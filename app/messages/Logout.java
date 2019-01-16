package messages;

import Reference.UserRef;

public class Logout {
    private final UserRef userRef;
    public Logout(UserRef userRef) {
        this.userRef = userRef;
    }
    public UserRef getUserRef() {
        return userRef;
    }
}
