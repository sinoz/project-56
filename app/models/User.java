package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.format.Formats;
import play.data.validation.*;

import java.net.URL;
import java.util.Date;
import java.util.List;

@Entity
public class User extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String password;

//    TODO: optional
//    @Constraints.Required
//    public String password_salt;

    @Constraints.Required
    public String mail;

    @Constraints.Required
    public URL profile_picture;

    public List<String> inventory;
    public List<String> favorites;
    public List<String> order_history;

    @Constraints.Required
    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date member_since;
}
