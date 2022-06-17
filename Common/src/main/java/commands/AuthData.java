package commands;

import java.io.Serializable;

public class AuthData implements Serializable {
    private String method;
    private String login;
    private String password;

    public AuthData(String m, String l, String p){
        this.method = m;
        this.login = l;
        this.password = p;
    }

    public AuthData(String l, String p){
        this.login = l;
        this.password = p;
    }

    public String getLogin() {
        return login;
    }

    public String getMethod() {
        return method;
    }

    public String getPassword() {
        return password;
    }
}
