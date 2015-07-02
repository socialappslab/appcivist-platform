package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Hashtag extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long hashtagId;
	private String hashtag; // TODO limit the lenght of the hashtag to <

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Hashtag> find = new Finder<Long, Hashtag>(
			Long.class, Hashtag.class);

	public Hashtag(String hashtag) {
		super();
		this.hashtag = hashtag;
	}

	/*
	 * Getters and Setters
	 */

	public Long getHashtagId() {
		return hashtagId;
	}

	public void setHashtagId(Long hashtagId) {
		this.hashtagId = hashtagId;
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	/*
	 * Basic Data operations
	 */

	public static Hashtag read(Long id) {
		return find.ref(id);
	}

	public static List<Hashtag> findAll() {
		return find.all();
	}

	public static Hashtag create(Hashtag object) {
		Hashtag h = findByText(object.getHashtag());
		if (h != null) {
			object.save();
			object.refresh();
			return object;
		} else {
			return h;
		}
	}

	public static Hashtag createObject(Hashtag object) {
		Hashtag h = findByText(object.getHashtag());
		if (h != null) {
			object.save();
			return object;
		} else {
			return h;
		}
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	/*
	 * Other operations
	 */

	public static Hashtag findByText(String value) {
		return find.where().eq("hashtag", value).findUnique();
	}
}
