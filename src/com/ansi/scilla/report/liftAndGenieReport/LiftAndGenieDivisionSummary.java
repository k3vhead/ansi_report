package com.ansi.scilla.report.liftAndGenieReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class LiftAndGenieDivisionSummary extends StandardReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "LiftAndGenieDivisionSummary";
	public static final String TAB_LABEL = "Summary";
	

	private final String sql = "select concat(division.division_nbr,'-',division.division_code) as div,\n" + 
			"case \n" + 
			"when lift.lift_dl is null then 0\n" + 
			"else lift.lift_dl\n" + 
			"end as lift_dl \n" + 
			"from division \n" + 
			"left outer join (select division.division_id,\n" + 
			"isnull(sum(act_dl_amt),0.00) as lift_dl\n" + 
			"from division\n" + 
			"join ticket on division.division_id = act_division_id\n" + 
			"join job on job.job_id = ticket.job_id\n" + 
			"where (service_description like '%lift%'\n" + 
			"	or service_description like '%genie%'\n" + 
			"	or equipment like '%lift%'\n" + 
			"	or equipment like '%genie%')\n" + 
			"	and ticket_status in ('c','i','p')\n" + 
			"	and ticket_type in ('run','job')\n" + 
			"	and process_date >= ? and process_date < ?\n" + 
			"group by division.division_id) lift on lift.division_id = division.division_id\n" + 
			"order by division_nbr, division_code";

	
	public static final String REPORT_TITLE = "Lift And Genie";
	private final String REPORT_NOTES = null;

	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());
	
	public LiftAndGenieDivisionSummary() {
		super();
		this.setTitle(REPORT_TITLE);
		this.setTabLabel(TAB_LABEL);
	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database connection
	 * @throws Exception Something bad happened
	 */
	protected LiftAndGenieDivisionSummary(Connection conn) throws Exception {
		this();

		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Midnight.getInstance(new AnsiTime());
		
		this.data = makeData(conn, this, startDate, endDate);
		logger.info("LiftAndGenieSummaryReport:this.data:"+data+"\tStart:"+startDate.getTime()+"\tEnd:"+endDate.getTime());

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
	}

	protected LiftAndGenieDivisionSummary(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		this();
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.startDate = startDate;
		this.endDate = endDate;
		this.data = makeData(conn, this, startDate, endDate);
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
	}
	
	
	
	private List<RowData> makeData(Connection conn, LiftAndGenieDivisionSummary report, Calendar startDate, Calendar endDate) throws SQLException {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);

		List<RowData> data = new ArrayList<RowData>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		logger.log(Level.DEBUG,sql);
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		
		return data;
	}
	
	

	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
		super.setHeaderNotes(REPORT_NOTES);

		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE,null),
				new ColumnHeader("liftDl", "Lift DL", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM,null),
//				new ColumnHeader("taxAmt", "Taxes\nPaid\nAmount", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
//				new ColumnHeader("total", "Total\nPayment\nAmount", 2, DataFormats.DECIMAL_FORMAT, SummaryType.SUM)//,
//				new ColumnHeader("excess", "Excess Cash Amount", DataFormats.DECIMAL_FORMAT, SummaryType.SUM)
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)
		});
//		List<ReportHeaderRow> headerRight = new ArrayList<ReportHeaderRow>();
//		super.makeHeaderRight(headerRight);
		super.makeHeaderLeft(headerLeft);
		
		super.setFirstDetailColumn(2);
		super.setPdfWidthPercentage(40.0F);
		
		super.setColumnWidths(new ColumnWidth[] {
				(ColumnWidth)null,
				new ColumnWidth(3950, 45.0F),
				new ColumnWidth(3250, (Float)null),
				new ColumnWidth(3250, (Float)null),
				new ColumnWidth(3250, (Float)null),
				(ColumnWidth)null,
				new ColumnWidth(2750, 57.0F),
		});
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, null, startDate, endDate);
	}



	public static LiftAndGenieDivisionSummary buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new LiftAndGenieDivisionSummary(conn, startDate, endDate);
	}



	public static LiftAndGenieDivisionSummary buildReport(Connection conn) throws Exception {
		return new LiftAndGenieDivisionSummary(conn);
	}



	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		private String div;
		private Double liftDl;
//		private Double taxAmt;
//		private Double total;
//		private Double excess;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.div = rs.getString("div");
			this.liftDl = rs.getDouble("lift_dl");
//			this.taxAmt = rs.getDouble("tax_amt");
//			this.total = rs.getDouble("total");
//			this.excess = -1.0;
		}

		public String getDiv() {
			return div;
		}
		public void setDiv(String div) {
			this.div = div;
		}
		public Double getLiftDl() {
			return liftDl;
		}
		public void setLiftDl(Double liftDl) {
			this.liftDl = liftDl;
		}
//		public Double getTaxAmt() {
//			return taxAmt;
//		}
//		public void setTaxAmt(Double taxAmt) {
//			this.taxAmt = taxAmt;
//		}
//		public Double getTotal() {
//			return total;
//		}
//		public void setTotal(Double total) {
//			this.total = total;
//		}
//		public Double getExcess() {
//			return excess;
//		}
//		public void setExcess(Double excess) {
//			this.excess = excess;
//		}		
	}
	

}
