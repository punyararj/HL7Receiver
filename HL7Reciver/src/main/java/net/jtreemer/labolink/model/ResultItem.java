package net.jtreemer.labolink.model;

import java.util.Date;

public class ResultItem {

	private String name;
	
	private String value;
	
	private String unit;
	
	private Date reciveDateTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Date getReciveDateTime() {
		return reciveDateTime;
	}

	public void setReciveDateTime(Date reciveDateTime) {
		this.reciveDateTime = reciveDateTime;
	}
	
	
}
