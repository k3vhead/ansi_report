package com.ansi.scilla.report.reportDistribution;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ansi.scilla.common.ApplicationObject;

public class RowData extends ApplicationObject {
	private static final long serialVersionUID = 1L;
	public String reportId;
	public String userName;
	public String email;
	public String division;
	public Integer userId;
	public Integer divisionId;
	
	public RowData(ResultSet rs) throws SQLException {
		super();
		this.reportId = rs.getString("report_id");
		this.userName = rs.getString("last_name") + ", " + rs.getString("first_name");
		this.email = rs.getString("email");
		Object division = rs.getObject("div");
		if ( division != null ) {
			this.division = (String)division;
		}			
		this.userId = rs.getInt("user_id");
		Object divisionId = rs.getObject("division_id");
		if ( divisionId != null ) {
			this.divisionId = (Integer)divisionId;
		}
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
