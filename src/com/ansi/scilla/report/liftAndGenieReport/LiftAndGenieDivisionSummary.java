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

import org.apache.commons.collections.CollectionUtils;
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
	

	private final String DIVISION_SUMMARY_SQL = "select concat(division.division_nbr,'-',division.division_code) as name " +
				"\n, isnull(div.amount,'0.00') as amount " +
				"\n, isnull(div.tax_amt,'0.00') as tax_amt " +
				"\n, isnull(div.total,'0.00') as total " +
				"\nfrom division " +
				"\nleft outer join (select " +
				"\n division_id " +
				"\n	, isnull(sum(ticket_payment.amount),'0.00') as amount " +
				"\n	, isnull(sum(ticket_payment.tax_amt),'0.00') as tax_amt " +
				"\n	, isnull(sum(ticket_payment.amount+ticket_payment.tax_amt),'0.00') as total " +
				"\n	from division " +
				"\n	join ticket on division.division_id = ticket.act_division_id " +
				"\n	join ticket_payment on ticket_payment.ticket_id = ticket.ticket_id " +
				"\n	join payment on payment.payment_id = ticket_payment.payment_id " +
				"\n	where payment_date >= ? and payment_date <= ? " +
				"\n	group by division_id) as div on div.division_id = division.division_id " +
				"\norder by concat(division.division_nbr,'-',division.division_code)";
	

	
	public static final String REPORT_TITLE = "Lift And Genie";
	private final String REPORT_NOTES = null;

	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());
	
	public LiftAndGenieDivisionSummary() {
		super();
		this.setTitle(REPORT_TITLE);
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
		PreparedStatement ps = conn.prepareStatement(DIVISION_SUMMARY_SQL);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));

		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		
		return data;
	}
	
	
	@SuppressWarnings("unchecked")
	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
		super.setHeaderNotes(REPORT_NOTES);

		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("name", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE,null),
				new ColumnHeader("amount", "Lift_DL", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM,null),
//				new ColumnHeader("taxAmt", "Taxes\nPaid\nAmount", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
//				new ColumnHeader("total", "Total\nPayment\nAmount", 2, DataFormats.DECIMAL_FORMAT, SummaryType.SUM)//,
//				new ColumnHeader("excess", "Excess Cash Amount", DataFormats.DECIMAL_FORMAT, SummaryType.SUM)
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
<<<<<<< HEAD
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)});
=======
				//TODO: Replace the "null" row with the "getRunDateMethod" row, after you create a "getRunDateMethod"
//				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)
				new ReportHeaderRow("Created:", null, 0, DataFormats.DATE_TIME_FORMAT)
		});
>>>>>>> e765c74ac07dc9d078fc143b7ae6856c04d58f13
//		List<ReportHeaderRow> headerLeft = new ArrayList<ReportHeaderRow>();
//		super.makeHeaderLeft(headerLeft);
		
//		List<ReportHeaderRow> headerRight = new ArrayList<ReportHeaderRow>();
//		super.makeHeaderRight(headerRight);
		super.makeHeaderLeft(headerLeft);
		
		super.setColumnWidths(new ColumnWidth[] {
				(ColumnWidth)null,
				new ColumnWidth(3750, 45.0F),
//				new ColumnWidth(2866, (Float)null),
//				new ColumnWidth(2866, (Float)null),
//				new ColumnWidth(2866, (Float)null),
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
		private String name;
		private Double amount;
//		private Double taxAmt;
//		private Double total;
//		private Double excess;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.name = rs.getString("name");
			this.amount = rs.getDouble("amount");
//			this.taxAmt = rs.getDouble("tax_amt");
//			this.total = rs.getDouble("total");
//			this.excess = -1.0;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Double getAmount() {
			return amount;
		}
		public void setAmount(Double amount) {
			this.amount = amount;
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
