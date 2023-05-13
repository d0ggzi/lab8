import commands.*;
import database.DBAuthorize;
import database.DBConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import organizations.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Класс выполнения команд, работы с коллекцией
 *
 * @author Max Laptev
 * @version 1.0
 */

public class CollectionManager implements Runnable{
    private LinkedHashSet<Organization> organizations;
    protected static HashMap<String, String> commands;
    private Date initiazitionDate;
    Connection con;
    File workFile = null;
    private static final Logger logger = LogManager.getLogger(CollectionManager.class);
    Command cmd;
    ClientMessage clientMessage;
    DatagramChannel channel;
    InetSocketAddress socketAddress;
    ExecutorService executorService;
    private ReadWriteLock lock;
    private Lock writeLock;

    {
        lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        organizations = new LinkedHashSet<>();
        this.con = new DBConnect().connect();
        commands = new HashMap<>();
        this.initiazitionDate = new Date();
        executorService = Executors.newFixedThreadPool(4);
        commands.put("help", "Вывести справку по доступным командам.");
        commands.put("info", "Вывести в стандартный поток вывода информацию о коллекции.");
        commands.put("show", "Вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        commands.put("add {element}", "Добавить новый элемент в коллекцию.");
        commands.put("update id {element}", "Обновить значение элемента коллекции, id которого равен заданному.");
        commands.put("remove_by_id id", "Удалить элемент из коллекции по его id.");
        commands.put("clear", "Очистить коллекцию.");
        commands.put("execute_script file_name", "Считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же.");
        commands.put("exit", "Завершить программу (без сохранения в файл).");
        commands.put("add_if_min {element}", "Добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.");
        commands.put("remove_lower {element}", "Удалить из коллекции все элементы, превышающие заданный.");
        commands.put("history", "Вывести последние 5 команд.");
        commands.put("filter_contains_name name", "Вывести элементы, значение поля name которых содержит заданную подстроку.");
        commands.put("filter_greater_than_type type", "Вывести элементы, значение поля type которых больше заданного.");
        commands.put("print_field_descending_type", "Вывести значения поля type всех элементов в порядке убывания.");
    }

    public CollectionManager(){
        loadOrg();
    }

    public void setData(DatagramChannel channel, InetSocketAddress socketAddress, ClientMessage clientMessage){
        this.cmd = clientMessage.command;
        this.socketAddress = socketAddress;
        this.channel = channel;
        this.clientMessage = clientMessage;
    }

    @Override
    public void run(){
        if (cmd instanceof HelpCmd){
            execute((HelpCmd) cmd);
        }if (cmd instanceof AuthCmd){
            execute((AuthCmd) cmd);
        }if (cmd instanceof InfoCmd){
            execute((InfoCmd) cmd);
        }if (cmd instanceof ShowCmd){
            execute((ShowCmd) cmd);
        }if (cmd instanceof AddCmd){
            execute((AddCmd) cmd);
        }if (cmd instanceof ClearCmd){
            execute((ClearCmd) cmd);
        }if (cmd instanceof AddIfMinCmd){
            execute((AddIfMinCmd) cmd);
        }if (cmd instanceof FilterContainsNameCmd){
            execute((FilterContainsNameCmd) cmd);
        }if (cmd instanceof FilterGreaterThanType){
            execute((FilterGreaterThanType) cmd);
        }if (cmd instanceof PrintFieldDescendingTypeCmd){
            execute((PrintFieldDescendingTypeCmd) cmd);
        }if (cmd instanceof RemoveLowerCmd){
            execute((RemoveLowerCmd) cmd);
        }if (cmd instanceof RemoveByIdCmd){
            execute((RemoveByIdCmd) cmd);
        }if (cmd instanceof UpdateCmd){
            execute((UpdateCmd) cmd);
        }
    }

    public void loadOrg(){
        lock.readLock().lock();
        organizations.clear();
        PreparedStatement ps;
        ResultSet resultSet;
        try {
            String getOneRow = "select id, name, x, y, creation_date, annual_turnover, employees_count,  org_type, zip_code, loc_x, loc_y, loc_z from \"Organizations\" as O inner join \"Address\" A on A.addr_id = O.address";
            ps = con.prepareStatement(getOneRow);
            ps.execute();
            resultSet = ps.getResultSet();
            while (resultSet.next()){
                LocalDateTime time = resultSet.getTimestamp(5).toLocalDateTime();
                ZonedDateTime zonedDateTime = time.atZone(ZoneId.systemDefault());
                Organization org = new Organization(resultSet.getLong(1), resultSet.getString(2),
                        new Coordinates(resultSet.getInt(3), resultSet.getInt(4)),
                        zonedDateTime, resultSet.getInt(6), resultSet.getLong(7), OrganizationType.values()[resultSet.getInt(8)],
                        new Address(resultSet.getString(9), new Location(resultSet.getLong(10), resultSet.getLong(11), resultSet.getInt(12))));
                organizations.add(org);
            }
        }
        catch (SQLException e){
            System.out.println(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String read(){
        try{
            Scanner scan = new Scanner(System.in);
            return scan.nextLine();
        }catch (NoSuchElementException e){
            System.exit(1);
        }
        return null;
    }

    public int getDBLogin(String login){
        try{
            PreparedStatement ps = con.prepareStatement("SELECT user_id FROM \"User\" WHERE login = ?");
            ps.setString(1, login);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                int user_id = rs.getInt(1);
                return user_id;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }


    public void execute(HelpCmd c){
        {
            String helpText = "Команды: \n";
            for (String s: commands.keySet()){
                helpText += s + " - " + commands.get(s) + "\n";
            }
            ServerMessage serverMessage = new ServerMessage(helpText);
            ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
            executorService.execute(serverSender);
            logger.info("Команда help успешно отправлена!");

        }
    }

    public void execute(AuthCmd c){
        logger.info("Попытка пользователя авторизироваться");
        String result = new DBAuthorize().dbAuthorize(clientMessage.authData, this.con);
        ServerMessage serverMessage = new ServerMessage(result);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
    }

    public void execute(InfoCmd c){
        String infoText = "Тип коллекции - " + organizations.getClass() + "\n" + "Дата инициализации - " + initiazitionDate + "\n" + "Количество элементов - " + organizations.size();
        ServerMessage serverMessage = new ServerMessage(infoText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда info успешно отправлена!");
    }

    public void execute(ShowCmd c){
        String showText = "";
        if (organizations.isEmpty()){
            showText = "Коллекция пуста.";
        }
        else{
            for (Organization org: organizations) {
                showText += org.toString() + "\n";
            }
        }
        ServerMessage serverMessage = new ServerMessage(showText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда show успешно отправлена!");
    }

    public void execute(PrintFieldDescendingTypeCmd c){
        String descendingText = "";
        boolean flag = true;
        TreeSet<Organization> sortedSet = new TreeSet<>(organizations);
        for (Organization el : sortedSet){
            flag = false;
            descendingText += el + "\n";
        }
        if (flag){
            descendingText = "Извините, элементов нет";
        }
        ServerMessage serverMessage = new ServerMessage(descendingText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда print_field_descending_type успешно отправлена!");
    }

    public void execute(AddCmd c){
        writeLock.lock();
        try{
            String add = "insert into \"Address\" (zip_code, loc_x, loc_y, loc_z) VALUES (?, ?, ?, ?) returning addr_id";
            PreparedStatement ps = con.prepareStatement(add);
            Organization newOrg = clientMessage.org;
            ps.setString(1, newOrg.getPostalAddress().getZipCode());
            ps.setLong(2, newOrg.getPostalAddress().getTown().getX());
            ps.setLong(3, newOrg.getPostalAddress().getTown().getY());
            ps.setInt(4, newOrg.getPostalAddress().getTown().getZ());
            ps.execute();
            ResultSet resultSet = ps.getResultSet();
            if (resultSet.next()){
                int address = resultSet.getInt(1);
                String sql = "insert into \"Organizations\" (name, x, y, creation_date, annual_turnover, employees_count, org_type, address, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, newOrg.getName());
                ps.setInt(2, newOrg.getCoordinates().getX());
                ps.setInt(3, newOrg.getCoordinates().getY());
                ZonedDateTime time = newOrg.getCreationDate();
                Timestamp timestamp = Timestamp.valueOf(time.toLocalDateTime());
                ps.setTimestamp(4, timestamp);
                ps.setInt(5, newOrg.getAnnualTurnover());
                ps.setLong(6, newOrg.getEmployeesCount());
                ps.setInt(7, newOrg.getType().ordinal());
                ps.setInt(8, address);
                ps.setInt(9, getDBLogin(clientMessage.authData.getLogin()));
                ps.execute();
            }
        }catch (Exception e){
            System.out.println(e);
        }finally {
            writeLock.unlock();
        }


        ServerMessage serverMessage = new ServerMessage("Организация успешно добавлена.");
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда add успешно выполнена!");

        loadOrg();
    }

    public void execute(UpdateCmd c) {
        String updateText = "";
        long id = Long.parseLong(clientMessage.arg);
        AuthData authData = clientMessage.authData;
        Organization updatedOrg = clientMessage.org;
        writeLock.lock();
        try{
            if (checkExisting(authData)){
                String getID = "select address from \"Organizations\" where owner = ? and id = ?";
                PreparedStatement ps = con.prepareStatement(getID);
                ps.setInt(1, getDBLogin(authData.getLogin()) );
                ps.setLong(2, id);
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                if (resultSet.next()) {
                    String sql = "update \"Organizations\" set name = ?, x = ?, y = ?, creation_date = ?, annual_turnover = ?," +
                            "employees_count = ?, org_type = ? where owner = ? and id = ? returning address";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, updatedOrg.getName());
                    ps.setInt(2, updatedOrg.getCoordinates().getX());
                    ps.setInt(3, updatedOrg.getCoordinates().getY());
                    ZonedDateTime time = updatedOrg.getCreationDate();
                    Timestamp timestamp = Timestamp.valueOf(time.toLocalDateTime());
                    ps.setTimestamp(4, timestamp);
                    ps.setInt(5, updatedOrg.getAnnualTurnover());
                    ps.setLong(6, updatedOrg.getEmployeesCount());
                    ps.setInt(7, updatedOrg.getType().ordinal());
                    ps.setInt(8, getDBLogin(authData.getLogin()));
                    ps.setLong(9, id);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()){
                        ps = con.prepareStatement("update \"Address\" set zip_code = ?, loc_x = ?, loc_y = ?, loc_z = ? where addr_id = ?");
                        ps.setString(1, updatedOrg.getPostalAddress().getZipCode());
                        ps.setLong(2, updatedOrg.getPostalAddress().getTown().getX());
                        ps.setLong(3, updatedOrg.getPostalAddress().getTown().getY());
                        ps.setInt(4, updatedOrg.getPostalAddress().getTown().getZ());
                        ps.setInt(5, rs.getInt(1));
                        ps.execute();
                    }
                    updateText = "Объект с id " + id + " успешно обновлен";
                }
                loadOrg();
            }
            else updateText = "Нет объектов, принадлежащих Вам";
        }catch (Exception e){
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }
        ServerMessage serverMessage = new ServerMessage(updateText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда update успешно выполнена!");
    }

    public void execute(RemoveByIdCmd c){
        String removeText = "";
        long id = Long.parseLong(clientMessage.arg);
        AuthData authData = clientMessage.authData;
        writeLock.lock();
        try{
            if (checkExisting(authData)){
                String getID = "select address from \"Organizations\" where owner = ? and id = ?";
                PreparedStatement ps = con.prepareStatement(getID);
                ps.setInt(1, getDBLogin(authData.getLogin()));
                ps.setLong(2, id);
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                if (resultSet.next()) {
                    String sql = "delete from \"Organizations\" where owner = ? and id = ?";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, getDBLogin(authData.getLogin()));
                    ps.setLong(2, id);
                    ps.execute();
                    ps = con.prepareStatement("delete from \"Address\" where addr_id = ?");
                    ps.setLong(1, resultSet.getLong(1));
                    ps.execute();
                    removeText = "Ваши объекты удалены";
                }else{
                    removeText = "Этот объект не принадлежит Вам";
                }
                loadOrg();
            }
            else removeText = "Нет объектов, принадлежащих Вам";
        }catch (Exception e){
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }

        ServerMessage serverMessage = new ServerMessage(removeText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда remove_by_id успешно выполнена!");

    }

    public void execute(ClearCmd c){
        String clearText = "";
        AuthData authData = clientMessage.authData;
        writeLock.lock();
        try{
            if (checkExisting(authData)){
                String getID = "select address from \"Organizations\" where owner = ?";
                PreparedStatement ps = con.prepareStatement(getID);
                ps.setInt(1, getDBLogin(authData.getLogin()));
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                while (resultSet.next()) {
                    long id = resultSet.getInt(1);
                    System.out.println(id);
                    String sql = "delete from \"Organizations\" where owner = ?";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, getDBLogin(authData.getLogin()));
                    ps.execute();
                    ps = con.prepareStatement("delete from \"Address\" where addr_id = ?");
                    ps.setLong(1, id);
                    ps.execute();
                    clearText = "Ваши объекты удалены";
                }
                loadOrg();
            }
            else clearText = "Нет объектов, принадлежащих вам";
        }catch (Exception e){
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }

        ServerMessage serverMessage = new ServerMessage(clearText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда clear успешно выполнена!");

    }

    public boolean checkExisting(AuthData authData){
        lock.readLock().lock();
        try{
            int c = -1;
            String check = "select count(*) from \"Organizations\" where owner = ?";
            PreparedStatement ps = con.prepareStatement(check);
            ps.setInt(1, getDBLogin(authData.getLogin()));
            ps.execute();
            ResultSet resultSet = ps.getResultSet();
            if (resultSet.next()){
                c = resultSet.getInt(1);
            }
            return (c>0);
        }catch (SQLException e){
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void save(){
        StringBuilder toXmlFile = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        for (Organization el : organizations){
            toXmlFile.append("<organization>\n");
            toXmlFile.append(el.toXmlFormat());
            toXmlFile.append("</organization>\n");
        }

        try {
            FileWriter fileWriter = new FileWriter(workFile);
            fileWriter.write(toXmlFile.toString());
            fileWriter.flush();
            System.out.println("Коллекция успешно сохранена.");
        } catch (IOException e) {
            if (!workFile.canWrite()) {
                System.out.println("У файла нет доступа на запись.");
            }
            System.out.println("Не удалось сохранить коллекцию в файл.");
        }
    }


    public void execute(AddIfMinCmd c){
        String addIfMinText = "";
        Organization newOrg = clientMessage.org;
        writeLock.lock();
        int minAnnTurn = 999999;
        try{
            PreparedStatement ps2 = con.prepareStatement("SELECT MIN(annual_turnover) as min FROM \"Organizations\"");
            ps2.execute();
            ResultSet resultSet2 = ps2.getResultSet();
            if (resultSet2.next()){
                minAnnTurn = resultSet2.getInt(1);
            }
            if (minAnnTurn > newOrg.getAnnualTurnover()){
                String add = "insert into \"Address\" (zip_code, loc_x, loc_y, loc_z) VALUES (?, ?, ?, ?) returning addr_id";
                PreparedStatement ps = con.prepareStatement(add);
                ps.setString(1, newOrg.getPostalAddress().getZipCode());
                ps.setLong(2, newOrg.getPostalAddress().getTown().getX());
                ps.setLong(3, newOrg.getPostalAddress().getTown().getY());
                ps.setInt(4, newOrg.getPostalAddress().getTown().getZ());
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                if (resultSet.next()){
                    int address = resultSet.getInt(1);
                    String sql = "insert into \"Organizations\" (name, x, y, creation_date, annual_turnover, employees_count, org_type, address, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, newOrg.getName());
                    ps.setInt(2, newOrg.getCoordinates().getX());
                    ps.setInt(3, newOrg.getCoordinates().getY());
                    ZonedDateTime time = newOrg.getCreationDate();
                    Timestamp timestamp = Timestamp.valueOf(time.toLocalDateTime());
                    ps.setTimestamp(4, timestamp);
                    ps.setInt(5, newOrg.getAnnualTurnover());
                    ps.setLong(6, newOrg.getEmployeesCount());
                    ps.setInt(7, newOrg.getType().ordinal());
                    ps.setInt(8, address);
                    ps.setInt(9, getDBLogin(clientMessage.authData.getLogin()));
                    ps.execute();
                }
                addIfMinText = "Организация успешно добавлена.";
                loadOrg();
            }else{
                addIfMinText = "Организация не была добавлена.";
            }
        }catch (Exception e){
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }

        ServerMessage serverMessage = new ServerMessage(addIfMinText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда add_if_min успешно выполнена!");

    }

    public void execute(RemoveLowerCmd c){
        String removeLowerText = "Таких объектов, принадлежащих Вам, нет";
        boolean flag = false;
        AuthData authData = clientMessage.authData;
        Organization newOrg = clientMessage.org;
        writeLock.lock();
        try{
            if (checkExisting(authData)){
                String getID = "select address from \"Organizations\" where owner = ?";
                PreparedStatement ps = con.prepareStatement(getID);
                ps.setInt(1, getDBLogin(authData.getLogin()));
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                while (resultSet.next()) {
                    flag = true;
                    long x = resultSet.getLong(1);
                    String sql = "delete from \"Organizations\" where owner = ? and annual_turnover < ?";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, getDBLogin(authData.getLogin()));
                    ps.setInt(2, newOrg.getAnnualTurnover());
                    ps.execute();
                    ps = con.prepareStatement("delete from \"Address\" where addr_id = ?");
                    ps.setLong(1, x);
                    ps.execute();
                }
                loadOrg();
                if (flag) removeLowerText = "Организации удалены";
            }
            else removeLowerText = "Нет объектов, принадлежащих вам";
        }catch (Exception e){
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }

        ServerMessage serverMessage = new ServerMessage(removeLowerText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда remove_lower успешно выполнена!");

    }

    public void execute(FilterContainsNameCmd c){
        String containsNameText = "";
        String name = clientMessage.arg;
        boolean flag = true;
        for (Organization el : organizations){
            if (el.getName().equals(name)){
                flag = false;
                containsNameText += el + "\n";
            }
        }
        if (flag){
            containsNameText = "Извините, таких элементов нет";
        }
        ServerMessage serverMessage = new ServerMessage(containsNameText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда filter_contains_name успешно выполнена!");

    }

    public void execute(FilterGreaterThanType c){
        String greaterThanText = "";
        int inputAnnualTurnover = Integer.parseInt(clientMessage.arg);
        boolean flag = true;
        for (Organization el : organizations){
            if (el.getAnnualTurnover() > inputAnnualTurnover){
                flag = false;
                greaterThanText += el + "\n";
            }
        }
        if (flag){
            greaterThanText = "Извините, таких элементов нет";
        }
        ServerMessage serverMessage = new ServerMessage(greaterThanText);
        ServerSender serverSender = new ServerSender(serverMessage, channel, socketAddress);
        executorService.execute(serverSender);
        logger.info("Команда filter_greater_than_type успешно выполнена!");

    }

}
