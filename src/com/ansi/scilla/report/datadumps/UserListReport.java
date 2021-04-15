package com.ansi.scilla.report.datadumps;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.DataDumpReport;

public class UserListReport extends DataDumpReport implements ReportByNoInput {

	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "User List";

	private final String sql = "select ansi_user.user_id, ansi_user.last_name, ansi_user.first_name, ansi_user.user_status, ansi_user.title, " +  
	"\n\t permission_group.name as permission_group, ansi_user.email, ansi_user.phone,   " +
	"\n\t ansi_user.address1, ansi_user.address2, ansi_user.city, ansi_user.state, ansi_user.zip, concat(added.first_name, ' ', added.last_name) as added_by, ansi_user.added_date, " +  
	"\n\t concat(updated.first_name, ' ', updated.last_name) as updated_by, ansi_user.updated_date  " +
	"\n from ansi_user   " +
	"\n left outer join ansi_user as added on added.user_id = ansi_user.added_by " +
	"\n left outer join ansi_user as updated on updated.user_id = ansi_user.updated_by " +
	"\n inner join permission_group on permission_group.permission_group_id = ansi_user.permission_group_id " + 
	"\n order by last_name, first_name";



	public static final  String REPORT_TITLE = "User List Report";

	protected UserListReport(Connection conn) throws Exception {
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		makeReport(conn);
	}

	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	

	public static UserListReport buildReport(Connection conn) throws Exception {
		return new UserListReport(conn);
	}

	
	
}
