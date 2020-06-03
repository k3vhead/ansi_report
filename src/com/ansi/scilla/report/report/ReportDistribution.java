package com.ansi.scilla.report.report;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class ReportDistribution extends StandardReport implements ReportByNoInput {

	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Report Distribution";
	public static final String FILENAME = "reportDistribution";

	private List<RowData> data;
	
	private ReportDistribution() {		
		super();
		this.setTitle(REPORT_TITLE);
	}
	private ReportDistribution(Connection conn) throws Exception {
		this();
		
		this.data = makeData(conn);				
		makeReport(data);
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	private List<RowData> makeData(Connection conn) throws SQLException {
		String sql = "select report_subscription.user_id, report_subscription.report_id, report_subscription.division_id,\n" + 
				"		ansi_user.first_name, ansi_user.last_name, ansi_user.email,\n" + 
				"		CASE \n" + 
				"			when division.division_nbr is null then null\n" + 
				"			else concat(division.division_nbr, '-', division.division_code)\n" + 
				"		end as div\n" + 
				"from report_subscription\n" + 
				"inner join ansi_user on ansi_user.user_id=report_subscription.user_id\n" + 
				"left outer join division on division.division_id=report_subscription.division_id\n" + 
				"order by report_subscription.report_id, ansi_user.last_name, ansi_user.first_name";
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sql);
		List<RowData> data = new ArrayList<RowData>();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		return data;
	}
	
	private void makeReport(List<RowData> data) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Calendar today = Calendar.getInstance();
		String subtitle = "As of " + sdf.format(today.getTime());
		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("reportId", "Report", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("userName", "Subscriber", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("email","Email", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("division","Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		super.makeHeaderLeft(new ArrayList<ReportHeaderRow>());
		super.makeHeaderRight(new ArrayList<ReportHeaderRow>());
		
//		super.setColumnWidths(new ColumnWidth[] {
//				ColumnWidth.DATE,			//Completed
//				ColumnWidth.DATETIME,		// Job ID
//				ColumnWidth.TICKET_NBR,			// Ticket #
//				ColumnWidth.TICKET_STATUS,	// Status
//				ColumnWidth.JOB_PPC,			// PPC
//				ColumnWidth.TICKET_INVOICED,			// Invoiced
//				ColumnWidth.JOB_JOB_NBR,	// Job #
//				ColumnWidth.ADDRESS_NAME,	// Site Name
//				ColumnWidth.ADDRESS_ADDRESS1,	// Site Address (colspan 2)
//				(ColumnWidth)null,			// Site Address
//		});		
	}
	
	
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


	public static ReportDistribution buildReport(Connection conn) throws Exception {		
		return new ReportDistribution(conn);
	}
}
