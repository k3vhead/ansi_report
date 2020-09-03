package com.ansi.scilla.report.monthlyServiceTaxReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeaderExtended;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;

public class MonthlyServiceTaxByDayReport extends StandardReport implements ReportByStartEnd {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Monthly Service Tax By Day";
	
	public static final  String REPORT_TITLE = "Monthly Service Tax By Day Report";

	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;

	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public MonthlyServiceTaxByDayReport() throws Exception {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database exception
	 * @throws Exception something bad happened
	 */
	protected MonthlyServiceTaxByDayReport(Connection conn) throws Exception {
		this();
		logger.log(Level.DEBUG, "constructor1");
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Calendar.getInstance(new AnsiTime());
		List<String> divList = makeMonthlyDivList(conn, startDate, endDate);
		this.data = makeData(conn, divList, startDate, endDate);

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, divList, subtitle);
	}

	protected MonthlyServiceTaxByDayReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		this();
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.startDate = startDate;
		this.endDate = endDate;
		List<String> divList = makeMonthlyDivList(conn, startDate, endDate);
		this.data = makeData(conn, divList, startDate, endDate);
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, divList, subtitle);
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

	public Integer getLastDivision() {
		return lastDivision;
	}

	public void setLastDivision(Integer lastDivision) {
		this.lastDivision = lastDivision;
	}

	public Integer makeDataSize() {
		return this.data.size();
	}
	
	private List<String> makeMonthlyDivList(Connection conn, Calendar startDate, Calendar endDate) throws SQLException {
		String sql = "select distinct division.division_id, concat(division.division_nbr, '-',division.division_code ) as div\n" + 
				"from ticket_payment\n" + 
				"inner join ticket on ticket.ticket_id=ticket_payment.ticket_id\n" + 
				"inner join payment on payment.payment_id=ticket_payment.payment_id and payment_date >= ? and payment_date <= ?\n" + 
				"inner join division on division.division_id=ticket.act_division_id\n" + 
				"where tax_amt > 0\n" + 
				"order by concat(division.division_nbr, '-',division.division_code )";
		
		List<String> divList = new ArrayList<String>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTime().getTime()));
		ps.setDate(2, new java.sql.Date(endDate.getTime().getTime()));
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			divList.add("[" + rs.getString("div") + "]");
		}
		rs.close();
		return divList;
	}
	
	private String makeSql(List<String> divList) {
		StringBuffer sql = new StringBuffer();
		List<String> selectors = new ArrayList<String>();
		selectors.add("select payment_date");
		for ( String div : divList ) {
			selectors.add("\tisnull(" + div + ",0.00) as " + div);
		}
		sql.append( StringUtils.join(selectors, ",\n"));
		
		sql.append("\nfrom \n" + 
				"			(select tax_amt, concat(division_nbr,'-',division_code) as div, payment_date \n" + 
				"				from ticket_payment\n" + 
				"				join ticket on ticket.ticket_id = ticket_payment.ticket_id\n" + 
				"				join payment on payment.payment_id = ticket_payment.payment_id \n" + 
				"				join division on ticket.act_division_id = division.division_id) as sourceTable\n" + 
				"		PIVOT\n" + 
				"			(sum(tax_amt)\n" + 
				"				for div in (");
		sql.append(StringUtils.join(divList, ","));
		sql.append(")\n" + 
				"			) as pivotTable\n" + 
				"		where payment_date >= ? and payment_date <= ?\n" + 
				"		order by payment_date");
		
		return sql.toString();
	}
	private List<RowData> makeData(Connection conn, List<String> divList, Calendar startDate, Calendar endDate) throws SQLException {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);

		List<RowData> data = new ArrayList<RowData>();
		
		String sql = makeSql (divList);
		logger.log(Level.DEBUG,sql);
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();
		while ( rs.next() ) {
			data.add(new RowData(rs, metaData));
		}
		rs.close();
		
		return data;
	}
	
	private ColumnHeader[] makeColumnHeaders(List<String> divList) throws NoSuchMethodException, SecurityException {
		Method getterMethod = RowData.class.getMethod("getValues", (Class<?>[])null);
		ColumnHeader[] columnHeaders = new ColumnHeader[divList.size()+1];  // a column for payment date and 1 for each div
		columnHeaders[0] = new ColumnHeader("paymentDate", "Payment Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE);
		for ( int i = 0; i < divList.size(); i++ ) {
			String div = divList.get(i);
			columnHeaders[i+1] = new ColumnHeaderExtended(getterMethod, div, div, 1, DataFormats.DATE_FORMAT, SummaryType.NONE);
		}
		return columnHeaders;
	}
	
	
	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, List<String> divList, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
		});
		super.makeHeaderLeft(headerLeft);
		
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("To:", getEndDateMethod, 3, DataFormats.DATE_FORMAT)
		});
		super.makeHeaderRight(headerRight);
		
//		super.setColumnWidths(makeDivColumnWidths());
		

	}
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheet = workbook.createSheet();
		XLSBuilder.build(this, sheet, new ReportStartLoc(0, 0));
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	public static MonthlyServiceTaxByDayReport buildReport(Connection conn) throws Exception {
		return new MonthlyServiceTaxByDayReport(conn);
	}
	public static MonthlyServiceTaxByDayReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new MonthlyServiceTaxByDayReport(conn, startDate, endDate);
	}
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		private java.sql.Date paymentDate;
		private HashMap<String, Double> values = new HashMap<String, Double>();
		
		public RowData(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
			super();
			this.paymentDate = rs.getDate("payment_date");
			for ( int i = 0; i < rsmd.getColumnCount(); i++ ) {
				String columnName = rsmd.getColumnName(i+1);   // because sql starts with 1 and nobody knows why
				if ( ! columnName.equalsIgnoreCase("payment_date") ) {
					Double value = rs.getDouble(columnName);
//					System.out.println("Putting\t" + columnName + "\t" + value);
					values.put("[" + columnName + "]", value);  // wrap with brackets to match "makeDivList()"
				}
			}
		}

		public java.sql.Date getPaymentDate() {
			return paymentDate;
		}

		public void setPaymentDate(java.sql.Date paymentDate) {
			this.paymentDate = paymentDate;
		}

		public HashMap<String, Double> getValues() {
			return values;
		}

		public void setValues(HashMap<String, Double> values) {
			this.values = values;
		}
		
	}
	
	
	
}
