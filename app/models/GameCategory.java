package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.net.URL;

/**
 * A category of types of games.
 *
 * @author Maurice van Veen
 */
@Entity(name = "game_category")
public final class GameCategory extends Model {
	/** The primary key of the category. */
	@Id
	private String id;

	/** The name of the game */
	@Constraints.Required
	private String name;

	/** The link to the image for this game */
	private URL image;

	/** General information about the game. */
	private String description;

	/** Specific information about the game (ranks, stats, etc.) */
	//  TODO: this may be unnecessary
	//	private String specifications;
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

	//	public String getSpecifications() {
	//		return specifications;
	//	}
	//
	//	public void setSpecifications(String specifications) {
	//		this.specifications = specifications;
	//	}
}