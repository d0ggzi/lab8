package Controllers;

import commands.*;
import database.DBConnect;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import organizations.*;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MainScene implements Initializable {
    private Parent root;
    private Stage stage;
    private Scene scene;
    private ClientSender clientSender;
    private ClientReceiver clientReceiver;
    private AuthData authData;
    private ResourceBundle resources;
    private String hex;
    private List<DBOrganization> dbOrganizations = Collections.synchronizedList(new LinkedList<>());
    private ClientMessage clientMessage;

    @FXML
    private TextField filter;

    @FXML
    private Label helloText;

    @FXML
    private Label userColor;

    @FXML
    private ChoiceBox<String> filterChoice;

    @FXML
    private ChoiceBox<Command> commandChoice;

    @FXML
    private Button executeBtn;

    @FXML
    private TableView<DBOrganization> orgTable;

    @FXML
    private TableColumn<DBOrganization, Long> id;

    @FXML
    private TableColumn<DBOrganization, String> name;

    @FXML
    private TableColumn<DBOrganization, Integer> locx;

    @FXML
    private TableColumn<DBOrganization, Integer> locy;

    @FXML
    private TableColumn<DBOrganization, Long> locz;

    @FXML
    private TableColumn<DBOrganization, OrganizationType> orgType;

    @FXML
    private TableColumn<DBOrganization, String> owner;

    @FXML
    private TableColumn<DBOrganization, Integer> x;

    @FXML
    private TableColumn<DBOrganization, Integer> y;

    @FXML
    private TableColumn<DBOrganization, String> zipCode;

    @FXML
    private TableColumn<DBOrganization, ZonedDateTime> creationDate;

    @FXML
    private TableColumn<DBOrganization, Integer> annualTurnover;

    @FXML
    private TableColumn<DBOrganization, Long> employeesCount;

    @FXML
    private Label helpLabel;

    @FXML
    private Canvas canvas;

    @FXML
    private ChoiceBox<String> languageChoice;

    @FXML
    private Button logoutBtn;

    private String[] table_names;
    private String[] languages = {"RU", "EN", "FI", "IT"};

    ArrayList<Command> commands = new ArrayList<>();

    private String helpText;
    private Connection con;
    private boolean iter;


    public void setData(ClientReceiver clientReceiver, ClientSender clientSender, AuthData authData){
        this.clientReceiver = clientReceiver;
        this.clientSender = clientSender;
        this.authData = authData;
        this.hex = getColor(authData.getLogin());

        helloText.setText(resources.getString("hello") + ", " + authData.getLogin());
        userColor.setStyle("-fx-background-color: #" + hex);
    }

    public void showHelp(){
        Tooltip tooltip = new Tooltip();
        tooltip.setText(helpText);
        helpLabel.setTooltip(tooltip);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.con = new DBConnect().connect();
        loadTable();

        commands.add(new AddCmd());
        commands.add(new UpdateCmd());
        commands.add(new RemoveByIdCmd());
        commands.add(new ClearCmd());
        commands.add(new AddIfMinCmd());
        commands.add(new RemoveLowerCmd());
        commands.add(new FilterContainsNameCmd());
        commands.add(new FilterGreaterThanType());
        commands.add(new PrintFieldDescendingTypeCmd());

        helpText = resources.getString("helpText");
        table_names = resources.getString("table_names").split("-");
        filterChoice.getItems().addAll(table_names);
        commandChoice.getItems().addAll(commands);
        languageChoice.getItems().addAll(languages);
        languageChoice.setOnAction(this::changeLanguage);
        orgTable.setOnMouseClicked(this::doubleClickOnRow);

        updating_table();
    }

    private void loadTable() {
        loadOrg();

        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawCoordinatesSystem();

        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        x.setCellValueFactory(new PropertyValueFactory<>("x"));
        y.setCellValueFactory(new PropertyValueFactory<>("y"));
        creationDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        annualTurnover.setCellValueFactory(new PropertyValueFactory<>("annualTurnover"));
        employeesCount.setCellValueFactory(new PropertyValueFactory<>("employeesCount"));
        orgType.setCellValueFactory(new PropertyValueFactory<>("type"));
        zipCode.setCellValueFactory(new PropertyValueFactory<>("zipCode"));
        locx.setCellValueFactory(new PropertyValueFactory<>("loc_x"));
        locy.setCellValueFactory(new PropertyValueFactory<>("loc_y"));
        locz.setCellValueFactory(new PropertyValueFactory<>("loc_z"));
        owner.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ObservableList list = FXCollections.observableList(dbOrganizations);

        iter = !iter;
        try{
            for (int i = 0; i < dbOrganizations.size(); i++){
                int size = dbOrganizations.get(i).getAnnualTurnover()/10;
                if (size > 50) size = 50;
                if (size < 25) size = 25;
                if (dbOrganizations.get(i).getOwner().equals(authData.getLogin())) {
                    animate(dbOrganizations.get(i).getX(), dbOrganizations.get(i).getY(), size, dbOrganizations.get(i).getOwner());
                } else {
                    drawRectange(dbOrganizations.get(i).getX(), dbOrganizations.get(i).getY(), size, dbOrganizations.get(i).getOwner());
                }
            }
        } catch (Exception e){
        }


        try {
            FilteredList<DBOrganization> filteredList = new FilteredList<>(list, b -> true);
            try {
                filter.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredList.setPredicate(organization -> {
                        String keyword = newValue.toLowerCase();
                        if (filterChoice.getValue() == null) {
                            return false;
                        } else
                            if (organization.getId().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("id")) {
                                return true;
                            } else if (organization.getName().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals(resources.getString("org.name"))) {
                                return true;
                            } else if (organization.getX().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("x")) {
                                return true;
                            } else if (organization.getY().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("y")) {
                                return true;
                            } else if (organization.getCreationDate().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals(resources.getString("creationdate"))) {
                                return true;
                            } else if (organization.getAnnualTurnover().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals(resources.getString("annualturnover"))) {
                                return true;
                            } else if (organization.getEmployeesCount().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals(resources.getString("employeescount"))) {
                                return true;
                            } else if (organization.getType().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals(resources.getString("orgtype"))) {
                                return true;
                            } else if (organization.getLoc_x().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("loc: x")) {
                                return true;
                            } else if (organization.getZipCode().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("ZipCode")) {
                                return true;
                            } else if (organization.getLoc_y().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("loc: y")) {
                                return true;
                            } else if (organization.getLoc_z().toString().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals("loc: z")) {
                                return true;
                            } else if (organization.getOwner().toLowerCase().trim().indexOf(keyword) > -1 && filterChoice.getValue().equals(resources.getString("owner"))) {
                                return true;
                            } else return false;
                    });
                });
                SortedList<DBOrganization> sortedList = new SortedList<>(filteredList);
                sortedList.comparatorProperty().bind(orgTable.comparatorProperty());
                if (filter.getText().equals("")){
                    orgTable.setItems(sortedList);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        canvas.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                for (int i = 0; i < list.size(); i++) {
                    double res = -1;
                    double dist = distance(dbOrganizations.get(i).getX(), dbOrganizations.get(i).getY(), mouseEvent.getX() - canvas.getWidth() / 2, (-1) * (mouseEvent.getY() - canvas.getHeight() / 2));
                    if (dist < 25 * Math.sqrt(2)) {
                        ArrayList<Double> values = new ArrayList<>();
                        values.add(dist);
                        res = Collections.min(values);
                    }
                    if (res == dist) {
                        try {
                            if (dbOrganizations.get(i).getOwner().equals(authData.getLogin())) {
                                try {
                                    updatingOrg(dbOrganizations.get(i));
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-org.fxml"), resources);
                                    root = loader.load();

                                    NewOrgController newOrgController = loader.getController();
                                    newOrgController.setOrg(dbOrganizations.get(i), "info");

                                    Stage stage2 = new Stage();
                                    scene = new Scene(root);
                                    stage2.setScene(scene);
                                    stage2.showAndWait();
                                } catch (IOException | NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
        });
    }

    public double distance(double x1, double y1, double x2, double y2){
        double result = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        return result;
    }

    public void drawRectange(int x, int y, int size, String login){
        double x0 = canvas.getWidth();
        double y0 = canvas.getHeight();
        GraphicsContext context = canvas.getGraphicsContext2D();
        String color = String.valueOf(getColor(login));
        context.setFill(Color.web("#"+color));
        Rectangle rectangle = new Rectangle(x0/2-25+x, y0/2-25-y,size,size);
        draw(context, rectangle);
    }

    public void draw(GraphicsContext context, Rectangle rectangle){
        context.fillRect(rectangle.getX(), rectangle.getY(),rectangle.getHeight(), rectangle.getWidth());
    }

    public void animate(int x, int y, int size, String login){
        double x0 = canvas.getWidth();
        double y0 = canvas.getHeight();
        GraphicsContext context = canvas.getGraphicsContext2D();
        String color = String.valueOf(getColor(login));
        context.setFill(Color.web("#"+color));
        Rectangle rectangle = new Rectangle(x0/2-25+x, y0/2-25-y,size,size);
        drawAnimate(context, rectangle);
    }

    public void drawAnimate(GraphicsContext context, Rectangle rectangle){
        if (iter){
            context.fillRect(rectangle.getX(), rectangle.getY(),rectangle.getHeight(), rectangle.getWidth());
        }
        else {
            context.fillRect(rectangle.getX()+12.5, rectangle.getY()+12.5,rectangle.getHeight()/2, rectangle.getWidth()/2);
        }
    }

    public void doubleClickOnRow(MouseEvent event) {
        orgTable.setOnMouseClicked(mouseEvent -> {
            DBOrganization dbOrg = orgTable.getSelectionModel().getSelectedItem();
            try {
                if (mouseEvent.getClickCount() == 2) {
                    try {
                        if (dbOrg.getOwner().equals(authData.getLogin())) {
                            try {
                                updatingOrg(dbOrg);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NullPointerException e) {
                    }
                }
            }catch (NullPointerException e){

            }
        });
    }

    public void reload(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-scene.fxml"), resources);
            root = loader.load();

            MainScene mainScene = loader.getController();
            mainScene.setData(clientReceiver, clientSender, authData);

            stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void changeLanguage(ActionEvent event){
        String lang = languageChoice.getValue();
        this.resources = ResourceBundle.getBundle("bundles.Locale", new Locale(lang));
        reload(event);
    }

    public void logout(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"), resources);
            root = loader.load();

            Login login = loader.getController();
            login.setClient(clientReceiver, clientSender);

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void updating_table() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> loadTable());
            }
        };

        timer.schedule(task, 0, 2000);
    }

    public void updatingOrg(DBOrganization dbOrg){
        DBOrganization updOrg = null;
        String answer3 = null;
        if (dbOrg == null){
                try{
                    boolean flagId = false;
                    answer3 = GetArgument.getArg(resources.getString("enter.id"), resources.getString("send.btn"));
                    long id2 = Long.parseLong(answer3);
                    for (DBOrganization el: dbOrganizations){
                        if (el.getId() == id2){
                            if (!el.getOwner().equals(authData.getLogin())){
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle(resources.getString("try.again"));
                                alert.setContentText(resources.getString("not.yours"));
                                alert.showAndWait();
                                return;
                            }
                            flagId = true;
                            updOrg = el;
                            break;
                        }
                    }
                    if (!flagId){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(resources.getString("try.again"));
                        alert.setContentText(resources.getString("not.found.id"));
                        alert.showAndWait();
                        return;
                    }
                }catch (Exception e){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(resources.getString("try.again"));
                    alert.setContentText(resources.getString("not.number"));
                    alert.showAndWait();
                    return;
                }
        } else {
            updOrg = dbOrg;
            answer3 = dbOrg.getId().toString();
        }


        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-org.fxml"), resources);
            root = loader.load();

            NewOrgController newOrgController = loader.getController();
            newOrgController.setOrg(updOrg, "update");

            Stage stage2 = new Stage();
            scene = new Scene(root);
            stage2.setScene(scene);
            stage2.showAndWait();

            Organization newOrg = newOrgController.getReturnOrg();

            if (newOrg != null) {
                clientMessage = new ClientMessage(new UpdateCmd(), answer3, newOrg, authData);
                clientSender.sendMessage(clientMessage);
                clientReceiver.getMessage();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void executeCmd(ActionEvent event){
        Command cmd = commandChoice.getValue();
        String userCmd = cmd.toString();
        switch (userCmd) {
            case "Info":
            case "PrintFieldDescendingType":
            case "Clear":
                clientMessage = new ClientMessage(cmd, authData);
                clientSender.sendMessage(clientMessage);
                clientReceiver.getMessage();
                break;
            case "RemoveById":
            case "FilterGreaterThanType":
                String answer = GetArgument.getArg(resources.getString("enter.value"), resources.getString("send.btn"));
                clientMessage = new ClientMessage(cmd, answer, authData);
                clientSender.sendMessage(clientMessage);
                clientReceiver.getMessage();
                break;
            case "FilterContainsName":
                String answer2 = GetArgument.getArg(resources.getString("enter.name"), resources.getString("send.btn"));
                clientMessage = new ClientMessage(cmd, answer2, authData);
                clientSender.sendMessage(clientMessage);
                clientReceiver.getMessage();
                break;
            case "AddIfMin":
            case "RemoveLower":
            case "Add":
                try{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-org.fxml"), resources);
                    root = loader.load();

                    NewOrgController newOrgController = loader.getController();

                    Stage stage2 = new Stage();
                    scene = new Scene(root);
                    stage2.setScene(scene);
                    stage2.showAndWait();

                    Organization newOrg = newOrgController.getReturnOrg();

                    clientMessage = new ClientMessage(cmd, newOrg, authData);
                    clientSender.sendMessage(clientMessage);
                    clientReceiver.getMessage();
                    break;
                }catch (IOException e){
                    e.printStackTrace();
                }
            case "Update":
                updatingOrg(null);
//            case "execute_script":
//                ScriptCmd scriptCmd = new ScriptCmd();
//                scriptCmd.startClient(userCommand[1], this);
//                break;
        }
    }

    public void drawCoordinatesSystem(){
        canvas.setHeight(387);
        canvas.setWidth(641);
        GraphicsContext context = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        context.setStroke(Color.BLACK);
        context.setLineWidth(1);
        context.strokeLine(w / 2, 0, w / 2, h);
        context.strokeLine(0, h / 2, w, h / 2);
        context.strokeLine(0, 0, w, 0);
        context.strokeLine(0, h, w, h);
        context.strokeLine(0, 0, 0, h);
        context.strokeLine(w, 0, w, h);
    }

    public String getColor(String username){
        int code = username.hashCode();
        if (code < 0) code = -code;
        String code2 = String.valueOf(code);
        if (code2.length() > 6){
            code2 = code2.substring(0,6);
        } else {
            while (code2.length() < 6){
                code2 += "0";
            }
        }
        return code2;
    }

    public void loadOrg(){
        dbOrganizations.clear();
        PreparedStatement ps;
        ResultSet resultSet;
        try {
            String getOneRow = "select id, name, x, y, creation_date, annual_turnover, employees_count,  org_type, zip_code, loc_x, loc_y, loc_z, login from \"Organizations\" as O inner join \"Address\" A on A.addr_id = O.address inner join \"User\" U on U.user_id = O.owner";
            ps = con.prepareStatement(getOneRow);
            ps.execute();
            resultSet = ps.getResultSet();
            while (resultSet.next()){
                LocalDateTime time = resultSet.getTimestamp(5).toLocalDateTime();
                ZonedDateTime zonedDateTime = time.atZone(ZoneId.systemDefault());
                DBOrganization dbOrg = new DBOrganization(resultSet.getLong(1), resultSet.getString(2),
                        resultSet.getInt(3), resultSet.getInt(4),
                        zonedDateTime, resultSet.getInt(6), resultSet.getLong(7), OrganizationType.values()[resultSet.getInt(8)],
                        resultSet.getString(9), resultSet.getLong(10), resultSet.getLong(11), resultSet.getInt(12), resultSet.getString(13));
                dbOrganizations.add(dbOrg);
            }
        }
        catch (SQLException e){
            System.out.println("Не удается загрузить данные");
        }
    }
}
