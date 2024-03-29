package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A category of types of games.
 *
 * @author Maurice van Veen
 */
@Entity(name = "game_category")
public final class GameCategory extends Model {
	/** The primary key of the category. */
	@Id
	private int id;

	/** The name of the game */
	@Constraints.Required
	private String name;

	/** The link to the image for this game */
	private String image;

	/** General information about the game. */
	private String description;

	/** Genre of the game. */
	private String genre;

	private int search;

	/** Specific information about the game (ranks, stats, etc.) */
	//  TODO: this may be unnecessary
	//	private String specifications;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getSearch() {
		return search;
	}

	public void setSearch(int search) {
		this.search = search;
	}

	//	public String getSpecifications() {
	//		return specifications;
	//	}
	//
	//	public void setSpecifications(String specifications) {
	//		this.specifications = specifications;
	//	}
}