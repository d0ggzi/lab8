package commands;

import commands.Command;
import organizations.Organization;

import java.io.Serializable;
import java.util.Arrays;

public class ClientMessage implements Serializable {
    public Command command;
    public String arg;
    public Organization org;
    public AuthData authData;


    public ClientMessage(Command c, AuthData a) {
        this.command = c;
        this.authData = a;
    }

    public ClientMessage(Command c, String arg, AuthData a) {
        this.command = c;
        this.arg = arg;
        this.authData = a;
    }

    public ClientMessage(Command c, Organization obj, AuthData a) {
        this.command = c;
        this.org = obj;
        this.authData = a;
    }

    public ClientMessage(Command c, String arg, Organization obj, AuthData a) {
        this.command = c;
        this.arg = arg;
        this.org = obj;
        this.authData = a;
    }


    @Override
    public String toString() {
        return "command=" + command +
                ", arg=" + arg;
    }
}
