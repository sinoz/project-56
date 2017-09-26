package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.validation.*;

@Entity
public class Order extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String user_id;

    @Constraints.Required
    public String product_id;

    @Constraints.Required
    public float price;

    public String coupon_code;

    @Constraints.Required
    public int status;
}