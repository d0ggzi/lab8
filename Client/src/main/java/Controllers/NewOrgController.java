package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import organizations.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class NewOrgController implements Initializable {

    @FXML
    private TextField newOrgAnn;

    @FXML
    private Button newOrgBtn;

    @FXML
    private Label newOrgText;

    @FXML
    private TextField newOrgEmpl;

    @FXML
    private TextField newOrgLocX;

    @FXML
    private TextField newOrgLocY;

    @FXML
    private TextField newOrgLocZ;

    @FXML
    private TextField newOrgName;

    @FXML
    private ChoiceBox<OrganizationType> newOrgType;

    @FXML
    private TextField newOrgX;

    @FXML
    private TextField newOrgY;

    @FXML
    private TextField newOrgZip;

    private ResourceBundle resources;
    private Organization returnOrg;
    private DBOrganization updOrg = null;
    private ArrayList<OrganizationType> types = new ArrayList<>();

    public void getNewOrg(ActionEvent event){
        long id2 = (long) (Math.random() * 1000000);
        String addName;
        int addCoordX = 0;
        int addCoordY = 0;
        int addAnnualTurnover = 0;
        long addEmployeesCount = 0;
        String addZipCode = "";
        long addLocationX = 0;
        long addLocationY = 0;
        int addLocationZ = 0;


        addName = newOrgName.getText();
        if (addName.equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("empty.line"));
            alert.showAndWait();
            return;
        }

        try{
            addCoordX = Integer.parseInt(newOrgX.getText());
            if (addCoordX <= 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(resources.getString("try.again"));
                alert.setContentText(resources.getString("bigger.zero"));
                alert.showAndWait();
                return;
            }
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }

        try{
            addCoordY = Integer.parseInt(newOrgY.getText());
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }

        try{
            addAnnualTurnover = Integer.parseInt(newOrgAnn.getText());
            if (addAnnualTurnover <= 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(resources.getString("try.again"));
                alert.setContentText(resources.getString("bigger.zero"));
                alert.showAndWait();
                return;
            }
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }


        try{
            addEmployeesCount = Long.parseLong(newOrgEmpl.getText());
            if (addEmployeesCount <= 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(resources.getString("try.again"));
                alert.setContentText(resources.getString("bigger.zero"));
                alert.showAndWait();
                return;
            }
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }

        addZipCode = newOrgZip.getText();
        if (addZipCode.equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("empty.line"));
            alert.showAndWait();
            return;
        }

        try{
            addLocationX = Integer.parseInt(newOrgLocX.getText());
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }


        try{
            addLocationY = Integer.parseInt(newOrgLocY.getText());
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }

        try{
            addLocationZ = Integer.parseInt(newOrgLocZ.getText());
        }
        catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("not.number"));
            alert.showAndWait();
            return;
        }

        try{
            returnOrg = new Organization(id2, addName, new Coordinates(addCoordX, addCoordY), addAnnualTurnover, addEmployeesCount, newOrgType.getValue(), new Address(addZipCode, new Location(addLocationX, addLocationY, addLocationZ)));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.close();
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("try.again"));
            alert.setContentText(resources.getString("fill.fields"));
            alert.showAndWait();
            return;
        }
    }

    public void setOrg(DBOrganization updOrg, String whatToDo){
        if (whatToDo.equals("info")){
            newOrgBtn.disableProperty();
            newOrgBtn.disarm();
            newOrgBtn.cancelButtonProperty();
            newOrgBtn.setOpacity(0);
            newOrgText.setText(resources.getString("info.text"));
        } else newOrgText.setText(resources.getString("updOrgText"));
        this.updOrg = updOrg;
        if (updOrg != null) getUpdatedOrg();
    }

    public void getUpdatedOrg(){
        newOrgBtn.setText(resources.getString("updOrgBtn"));
        newOrgName.setText(updOrg.getName());
        newOrgX.setText(String.valueOf(updOrg.getX()));
        newOrgY.setText(String.valueOf(updOrg.getY()));
        newOrgAnn.setText(String.valueOf(updOrg.getAnnualTurnover()));
        newOrgEmpl.setText(String.valueOf(updOrg.getEmployeesCount()));
        newOrgType.setValue(updOrg.getType());
        newOrgZip.setText(updOrg.getZipCode());
        newOrgLocX.setText(String.valueOf(updOrg.getLoc_x()));
        newOrgLocY.setText(String.valueOf(updOrg.getLoc_y()));
        newOrgLocZ.setText(String.valueOf(updOrg.getLoc_z()));
    }

    public Organization getReturnOrg() {
        return returnOrg;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        types.add(OrganizationType.COMMERCIAL);
        types.add(OrganizationType.TRUST);
        types.add(OrganizationType.OPEN_JOINT_STOCK_COMPANY);
        newOrgType.getItems().addAll(types);
    }
}
