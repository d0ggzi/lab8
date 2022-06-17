import Controllers.ClientReceiver;
import Controllers.ClientSender;
import commands.AuthCmd;
import commands.AuthData;
import commands.ClientMessage;

import java.io.IOException;
import java.util.Scanner;

public class Authorisation {
    private final Scanner scanner = new Scanner(System.in);
    private String login;
    private String password;
    private boolean log_or_reg = false;

    public void setLog_or_reg(boolean x){
        log_or_reg = x;
    }
    public boolean isLog_or_reg(){
        return log_or_reg;
    }

    public String getLogin() {
        return login;
    }
    public String getPassword(){
        return password;
    }

    public AuthData authorize (ClientSender clientSender, ClientReceiver clientReceiver) throws IOException {
        while (!log_or_reg) {
            String user = scanner.nextLine();
            if (user.trim().equals("register")){
                System.out.println("Введите логин: ");
                this.login = scanner.nextLine().trim();
                System.out.println("Введите пароль: ");
                this.password = scanner.nextLine().trim();
                ClientMessage userData = new ClientMessage(new AuthCmd(), new AuthData(user, this.login, this.password));
                clientSender.sendMessage(userData);
                log_or_reg = clientReceiver.getMessage();
                if (!log_or_reg){
                    System.out.println("Пользователь с таким именем уже существует");
                }
            }
            else if (user.trim().equals("login")) {
                System.out.println("Введите логин: ");
                this.login = scanner.nextLine().trim();
                System.out.println("Введите пароль: ");
                this.password = scanner.nextLine().trim();
                ClientMessage userData = new ClientMessage(new AuthCmd(), new AuthData(user, this.login, this.password));
                clientSender.sendMessage(userData);
                log_or_reg = clientReceiver.getMessage();
                if (!log_or_reg){
                    System.out.println("Неверный логин или пароль");
                }
            }else System.out.println("Сначала необходимо авторизоваться");
        }
        System.out.println("Вы успешно вошли в систему!");
        AuthData authData = new AuthData(this.login, this.password);
        return authData;
    }
}
