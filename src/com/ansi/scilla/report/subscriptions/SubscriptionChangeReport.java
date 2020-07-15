package com.ansi.scilla.report.subscriptions;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class SubscriptionChangeReport extends StandardReport implements ReportByStartEnd {
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "subscriptionChangeReport";

	private final String reportSql = "select * from (\n" + 
			"select ansi_user.first_name, ansi_user.last_name, ansi_user.email, \n" + 
			"	report_subscription.division_id, report_subscription.report_id,\n" + 
			"	CASE \n" + 
			"		when report_subscription.division_id is not null then concat(division.division_nbr,'-',division.division_code)\n" + 
			"		else null\n" + 
			"	end as div,\n" + 
			"	'ADDED' as trx,\n" + 
			"	report_subscription.added_date as trx_date\n" + 
			"from report_subscription\n" + 
			"inner join ansi_user on ansi_user.user_id=report_subscription.user_id\n" + 
			"left outer join division on division.division_id=report_subscription.division_id\n" + 
			"where (report_subscription.added_date >= ? and report_subscription.added_date <= ?)\n" + 
			"union\n" + 
			"select ansi_user.first_name, ansi_user.last_name, ansi_user.email, \n" + 
			"	report_subscription_history.division_id, report_subscription_history.report_id,\n" + 
			"	CASE \n" + 
			"		when report_subscription_history.division_id is not null then concat(division.division_nbr,'-',division.division_code)\n" + 
			"		else null\n" + 
			"	end as div,\n" + 
			"	'DELETED' as trx,\n" + 
			"	report_subscription_history.history_date as trx_date\n" + 
			"from report_subscription_history\n" + 
			"inner join ansi_user on ansi_user.user_id=report_subscription_history.user_id\n" + 
			"left outer join division on division.division_id=report_subscription_history.division_id\n" +
			"left outer join report_subscription_history rsh2 on \n" +
			"    rsh2.user_id=report_subscription_history.user_id and \n" +
			"    rsh2.division_id=report_subscription_history.division_id and\n" + 
			"    rsh2.report_id=report_subscription_history.report_id and \n" +
			"    rsh2.history_date>report_subscription_history.history_date\n" +
			"where (report_subscription_history.history_date >= ? and report_subscription_history.history_date <= ?) \n" +
			"    and report_subscription_history.history_cmd ='DELETE' \n" +
			"    and rsh2.history_date is null \n" +
			") subscription_change\n" + 
			"order by last_name, \n" + 
			"		first_name, \n" + 
			"		report_id, \n" + 
			"		div,\n" + 
			"		trx_date";
	
	
	public static final String REPORT_TITLE = "Subscription Change Report";
	private Calendar startDate;
	private Calendar endDate;
	private Calendar runDate;
	
	
	public SubscriptionChangeReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		this(conn, startDate, endDate, Calendar.getInstance(new AnsiTime()));		
	}
	
	public SubscriptionChangeReport(Connection conn, Calendar startDate, Calendar endDate, Calendar runDate) throws Exception {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.setTitle(REPORT_TITLE);
		this.runDate = runDate;
		makeData(conn);
	}
	
	
	public Calendar getStartDate() {
		return startDate;
	}
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}
	public Calendar getEndDate() {
		return endDate;
	}
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
	public Calendar getRunDate() {
		return runDate;
	}
	public void setRunDate(Calendar runDate) {
		this.runDate = runDate;
	}

	
	private void makeData(Connection conn) throws Exception {
		super.setSubtitle(makeSubTitle());
		super.setHeaderRow(new ColumnHeader[] {
			new ColumnHeader("lastName","Last Name", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("firstName", "First Name", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("email", "Email", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("reportId", "Report", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("trx", "Action", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("trxDate", "Date", 1, DataFormats.DATE_TIME_FORMAT, SummaryType.NONE),
		});
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);		
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
			new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		

		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {});
		super.makeHeaderRight(headerRight);
		
		super.setColumnWidths(new ColumnWidth[] {
			new ColumnWidth(5000, 200.0F),		// last name
			new ColumnWidth(5000, 200.0F),		// first name
			new ColumnWidth(6000, 200.0F),		// email
			new ColumnWidth(5500, 200.0F),		// report id
			new ColumnWidth(3500, 200.0F),		// Div
			new ColumnWidth(3500, 200.0F),		// action
			new ColumnWidth(6000, 200.0F),		// date
		});
		
		
		PreparedStatement ps = conn.prepareStatement(reportSql);
		ps.setDate(1, new java.sql.Date(this.startDate.getTime().getTime()));
		ps.setDate(2, new java.sql.Date(this.endDate.getTime().getTime()));
		ps.setDate(3, new java.sql.Date(this.startDate.getTime().getTime()));
		ps.setDate(4, new java.sql.Date(this.endDate.getTime().getTime()));
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			super.addDataRow(new RowData(rs));
		}
		rs.close();
	}

	private String makeSubTitle() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");		
		return sdf.format(this.startDate.getTime()) + " to " + sdf.format(this.getEndDate().getTime());
	}
	
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division ,startDate, endDate);
	}

	public static SubscriptionChangeReport buildReport(Connection conn, Calendar startDate,Calendar endDate, Calendar runDate) throws Exception {	
		return new SubscriptionChangeReport(conn, startDate, endDate, runDate);
	}
	
	public static SubscriptionChangeReport buildReport(Connection conn, Calendar startDate,Calendar endDate) throws Exception {	
		return buildReport(conn, startDate, endDate, Calendar.getInstance());
	}
	
	
	
	public class RowData extends ApplicationObject implements Comparable<RowData> {

		private static final long serialVersionUID = 1L;
		private String firstName;
		private String lastName;
		private String email;
		private String reportId;
		private String div;
		private String trx;
		private Timestamp trxDate;
		
		public RowData(ResultSet rs) throws SQLException {
			this.firstName = rs.getString("first_name");
			this.lastName = rs.getString("last_name");
			this.email = rs.getString("email");
			this.reportId = rs.getString("report_id");
			this.div = rs.getString("div");
			this.trx = rs.getString("trx");
			this.trxDate = rs.getTimestamp("trx_date");
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getReportId() {
			return reportId;
		}

		public void setReportId(String reportId) {
			this.reportId = reportId;
		}

		public String getDiv() {
			return div;
		}

		public void setDiv(String div) {
			this.div = div;
		}

		public String getTrx() {
			return trx;
		}

		public void setTrx(String trx) {
			this.trx = trx;
		}

		public Timestamp getTrxDate() {
			return trxDate;
		}

		public void setTrxDate(Timestamp trxDate) {
			this.trxDate = trxDate;
		}

		@Override
		public int compareTo(RowData o) {
			int retValue = this.lastName.compareTo(o.getLastName());
			if ( retValue == 0 ) {
				retValue = this.firstName.compareTo(o.getFirstName());				
			}
			if ( retValue == 0 ) {
				retValue = this.reportId.compareTo(o.getReportId());
			}
			if ( retValue == 0 ) {
				retValue = this.div.compareTo(o.getDiv());
			}
			if ( retValue == 0 ) {
				retValue = this.trxDate.compareTo(o.getTrxDate());
			}
			return retValue;
		}

		
		
		
	}
}
