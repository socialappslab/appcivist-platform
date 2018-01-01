package models.transfer;

import enums.ThemeTypes;

public class ThemeTransfer {
	private String title;
	private String description;
	private ThemeTypes type;
	
	public ThemeTransfer() {
		super();
	}
	
	public ThemeTransfer(Long id, String title, String description) {
		super();
		this.title = title;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ThemeTypes getType() {
		return type;
	}
	public void setType(ThemeTypes type) {
		this.type = type;
	}
}
