package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.validation.*;

import java.net.URL;

@Entity
public class GameCategory extends Model {

    @Id
    private String id;

    // name of the game
    @Constraints.Required
    private String name;

    // link to the image for this game
    private URL image;

    // general information about the game
    private String description;

    // specific information about the game (ranks, stats, etc.)
    private String specifications;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getImage() {
        return image;
    }

    public void setImage(URL image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
}