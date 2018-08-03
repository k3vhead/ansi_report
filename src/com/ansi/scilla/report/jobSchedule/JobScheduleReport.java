package com.ansi.scilla.report.jobSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import com.ansi.scilla.report.reportBuilder.DataDumpReport;

public class JobScheduleReport extends DataDumpReport {
	
	private static final long serialVersionUID = 1L;
	
	public static final  String REPORT_TITLE = "Job Schedule";

	protected static final String sql = "select * from job_schedule "
			+ " where start_date>=? and start_date<=?"
			+ " order by job_id, start_date desc, end_date asc";
	
	public JobScheduleReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		PreparedStatement ps = conn.prepareStatement(sql);
		java.sql.Date start = new java.sql.Date(startDate.getTimeInMillis());
		java.sql.Date end = new java.sql.Date(endDate.getTimeInMillis());
		ps.setDate(1, start);
		ps.setDate(2, end);
		ResultSet rs = ps.executeQuery();
		makeReport(rs);
	}
	

	

	public static JobScheduleReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new JobScheduleReport(conn, startDate, endDate);
	}
	
}
