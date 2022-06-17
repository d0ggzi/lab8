package organizations;

import java.time.ZonedDateTime;

public class DBOrganization {
    private Long id;
    private String name;
    private Integer x;
    private Integer y;
    private ZonedDateTime creationDate;
    private Integer annualTurnover;
    private Long employeesCount;
    private OrganizationType type;
    private String zipCode; //Поле не может быть null
    private Long loc_x;
    private Long loc_y;
    private Integer loc_z;
    private String owner;

    public DBOrganization(long id, String name, int x, int y, ZonedDateTime creationDate, Integer annualTurnover, Long employeesCount, OrganizationType type, String zipCode, long loc_x, long loc_y, int loc_z, String owner){
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.creationDate = creationDate;
        this.annualTurnover = annualTurnover;
        this.employeesCount = employeesCount;
        this.type = type;
        this.zipCode = zipCode;
        this.loc_x = loc_x;
        this.loc_y = loc_y;
        this.loc_z = loc_z;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(Integer annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public Long getEmployeesCount() {
        return employeesCount;
    }

    public void setEmployeesCount(Long employeesCount) {
        this.employeesCount = employeesCount;
    }

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Long getLoc_x() {
        return loc_x;
    }

    public void setLoc_x(long loc_x) {
        this.loc_x = loc_x;
    }

    public Long getLoc_y() {
        return loc_y;
    }

    public void setLoc_y(long loc_y) {
        this.loc_y = loc_y;
    }

    public Integer getLoc_z() {
        return loc_z;
    }

    public void setLoc_z(int loc_z) {
        this.loc_z = loc_z;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
