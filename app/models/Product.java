package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.format.Formats;
import play.data.validation.*;

import java.util.Date;

@Entity
public class Product extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String user_id;

    @Constraints.Required
    public String game_id;

    // if the product is displayed/visible in webshop
    @Constraints.Required
    public boolean visible;

    // if the product is disabled from webshop & inventory (but is used for order history etc.)
    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public String title;
    public String description;

    @Constraints.Required
    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date added_since;

    @Constraints.Required
    public boolean can_buy;
    public float buy_price;

    @Constraints.Required
    public boolean can_trade;

    @Constraints.Required
    public String mail_last;

    @Constraints.Required
    public String mail_current;

    @Constraints.Required
    public String password_current;
}