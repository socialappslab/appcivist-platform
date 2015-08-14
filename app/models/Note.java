package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Note extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long noteId;
	private String title;
	private String text;
	private User creator;

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, Note> find = new Finder<>(Note.class);

	public Note(User creator,String title, String text) {
		this.creator = creator;
		this.title = title;
		this.text = text;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Long getNoteId() {
		return noteId;
	}

	public void setNoteId(Long noteId) {
		this.noteId = noteId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/*
	 * Basic Data operations
	 */
	
	public static Note read(Long id) {
        return find.ref(id);
    }

    public static List<Note> findAll() {
        return find.all();
    }

    public static Note create(Note object) {
        object.save();
        object.refresh();
        return object;
    }

    public static Note createObject(Note object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }



}
