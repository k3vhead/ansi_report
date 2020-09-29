package com.ansi.scilla.report.test.subtotals;

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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.woAndFees.WOAndFeesSummaryReport;

public class SubtotalTester extends StandardReport implements ReportByNoInput {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "SubtotalExample";
	public static final String REPORT_TITLE = "Subtotal Example";
	
	
	protected final String sql = "select ticket_id, job_id, concat(division.division_nbr,'-',division.division_code ) as div, act_price_per_cleaning, act_dl_amt\n" + 
			"from ticket\n" + 
			"inner join division on division.division_id=ticket.act_division_id\n" + 
			"where ticket_status='P' and start_date>'01-01-2020' and start_date<'02-01-2020'\n" + 
			"order by div, job_id, ticket_id"
			;

	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());

	protected SubtotalTester(Connection conn, Calendar runDate) throws Exception {
		super();
		this.runDate = runDate;
		this.setTitle(REPORT_TITLE);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		this.setSubtitle("As of " + sdf.format(this.runDate.getTime()));
		super.setReportOrientation(ReportOrientation.PORTRAIT);
		this.data = this.makeData(conn);
		makeReport(data);
	}
	
	
	private List<RowData> makeData(Connection conn) throws SQLException {
		List<RowData> data = new ArrayList<RowData>();
		Statement ps = conn.createStatement();
		ResultSet rs = ps.executeQuery(sql);
		
		this.data = new ArrayList<RowData>();
		RowData newRow;
		while ( rs.next() ) {
			newRow = new RowData(rs);
			data.add(newRow);
		}
		rs.close();
		return data;
	}

	

	private void makeReport(List<RowData> data) throws NoSuchMethodException, SecurityException, CloneNotSupportedException {
		super.setTitle(REPORT_TITLE);	
		
		ColumnHeader ppcHeader = new ColumnHeader("ppc", "Act PPC", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM);
		ppcHeader.setSubTotalTrigger("jobId");
		ColumnHeader dlHeader = new ColumnHeader("actDlAmt", "Act DL", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM);
		dlHeader.setSubTotalTrigger("jobId");
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticketId", "Ticket", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("jobId","Job", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				ppcHeader,
				dlHeader,				
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);

		
		

		
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
		});
		super.makeHeaderLeft(headerLeft);
		
		
//		super.setColumnWidths(new ColumnWidth[] {
//				new ColumnWidth(2750,32.8F), 	// div
//				new ColumnWidth(3000,32.8F),	// total
//				new ColumnWidth(3000,32.8F),	// current
//				new ColumnWidth(3000,32.8F),	// 30
//				new ColumnWidth(3000,32.8F),	// 60
//				new ColumnWidth(3000,32.8F),	// 90
//				new ColumnWidth(3000,32.8F),	// 120
//				new ColumnWidth(3000,32.8F),	// 180
//		});

	}


	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static SubtotalTester buildReport(Connection conn) throws Exception {
		return SubtotalTester.buildReport(conn, Calendar.getInstance());
	}

	public static SubtotalTester buildReport(Connection conn, Calendar runDate) throws Exception {
		return new SubtotalTester(conn, runDate);
	}

	
	public class RowData extends ApplicationObject implements Comparable<RowData> {
		private static final long serialVersionUID = 1L;
		private Integer ticketId;
		private Integer jobId;
		private String div;
		private Double ppc;
		private Double actDlAmt;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.div = rs.getString("div");
			this.ticketId = rs.getInt("ticket_id");
			this.jobId = rs.getInt("job_id");
			this.ppc = rs.getDouble("act_price_per_cleaning");
			this.actDlAmt = rs.getDouble("act_dl_amt");			
		}

		

		public Integer getTicketId() {
			return ticketId;
		}



		public void setTicketId(Integer ticketId) {
			this.ticketId = ticketId;
		}



		public Integer getJobId() {
			return jobId;
		}



		public void setJobId(Integer jobId) {
			this.jobId = jobId;
		}



		public String getDiv() {
			return div;
		}



		public void setDiv(String div) {
			this.div = div;
		}



		public Double getPpc() {
			return ppc;
		}



		public void setPpc(Double ppc) {
			this.ppc = ppc;
		}



		public Double getActDlAmt() {
			return actDlAmt;
		}



		public void setActDlAmt(Double actDlAmt) {
			this.actDlAmt = actDlAmt;
		}



		@Override
		public int compareTo(RowData o) {
			return this.div.compareTo(o.getDiv());
		}
		
	}

	
	public static void main(String[] args) {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			SubtotalTester report = SubtotalTester.buildReport(conn);
			AnsiReportBuilder.writeXLS(report, "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/SubTotals.xlsx");
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			AppUtils.closeQuiet(conn);
		}
	}

}
