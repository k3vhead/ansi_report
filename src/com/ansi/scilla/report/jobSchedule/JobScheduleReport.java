package com.ansi.scilla.report.jobSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.DataDumpReport;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class JobScheduleReport extends DataDumpReport implements ReportByStartEnd {
	
	private static final long serialVersionUID = 1L;
	
	public static final  String REPORT_TITLE = "Job Schedule";
	public static final String FILENAME = "Job Schedule";

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
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
}
