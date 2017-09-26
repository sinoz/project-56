package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.validation.*;

import java.net.URL;

@Entity
public class GameCategory extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String name;

    public URL image;
    public String description;
    public String specifications;
}