package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.validation.*;

@Entity
public class Review extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String username_receiver_id;

    @Constraints.Required
    public String username_sender_id;

    @Constraints.Required
    public String title;

    public String description;

    @Constraints.Required
    public int rating;

}