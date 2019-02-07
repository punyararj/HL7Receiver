package net.jtreemer.labolink.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LinkData {

	private String patientHN;
	
	private String patientCID;
	
	private String patientVN;
	
	private Date dataDateTime;
	
	private String deviceId;
	
	private List<ResultItem> results = new ArrayList<>();

	public String getPatientHN() {
		return patientHN;
	}

	public void setPatientHN(String patientHN) {
		this.patientHN = patientHN;
	}

	public String getPatientCID() {
		return patientCID;
	}

	public void setPatientCID(String patientCID) {
		this.patientCID = patientCID;
	}

	public String getPatientVN() {
		return patientVN;
	}

	public void setPatientVN(String patientVN) {
		this.patientVN = patientVN;
	}

	public Date getDataDateTime() {
		return dataDateTime;
	}

	public void setDataDateTime(Date dataDateTime) {
		this.dataDateTime = dataDateTime;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public List<ResultItem> getResults() {
		return results;
	}

	public void setResults(List<ResultItem> results) {
		this.results = results;
	}
	
	
	
}
