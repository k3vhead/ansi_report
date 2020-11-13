package com.ansi.scilla.report.creditCardFees;

import java.lang.reflect.Method;
import java.math.BigDecimal;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ApplicationPropertyName;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;

public class CreditCardFeesByDayReport extends StandardReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;
	
	/*
	private final String sql = "select concat(division_nbr,'-',division_code) as div,\n" + 
			"		sum(abs(ticket_payment.amount+ticket_payment.tax_amt)) as total,\n" + 
			"		sum(abs(ticket_payment.amount+ticket_payment.tax_amt)*$fee$) as fee\n" + 
			"from payment \n" + 
			"join ticket_payment on ticket_payment.payment_id = payment.payment_id \n" + 
			"join ticket on ticket_payment.ticket_id = ticket.ticket_id \n" + 
			"join division on division.division_id = act_division_id \n" + 
			"where payment_method = 'credit_card' \n" + 
			"and payment_date >= ? and payment_date < ? \n" + 
			"group by concat(division_nbr,'-',division_code)\n" + 
			"order by concat(division_nbr,'-',division_code)";
	*/
	
	private final String sql = "select concat(division_nbr,'-',division_code) as div,\n" + 
			"		payment.*,\n" + 
			"		ticket_payment.*,\n" + 
			"		year(payment_date) as year,\n" + 
			"		month(payment_date) as month,\n" + 
			"		abs(ticket_payment.amount+ticket_payment.tax_amt) as total,\n" + 
			"		abs(ticket_payment.amount+ticket_payment.tax_amt)*$fee$ as fee,\n" + 
			"		day(payment_date) as day \n" + 
			"from payment \n" + 
			"join ticket_payment on ticket_payment.payment_id = payment.payment_id \n" + 
			"join ticket on ticket_payment.ticket_id = ticket.ticket_id \n" + 
			"join division on division.division_id = act_division_id \n" + 
			"where payment_method = 'credit_card' \n" + 
			"and payment_date >= ? and payment_date < ? \n" + 
			"order by div, year, month, day, payment_date";
		

	
	public static final String REPORT_TITLE = "Credit Card Fees Summary";
	public static final String FILENAME = "Credit Card Fees Summary";
//	private final String REPORT_NOTES = "notes go here";
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;
	
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public CreditCardFeesByDayReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database exception
	 * @throws Exception something bad happened
	 */
	protected CreditCardFeesByDayReport(Connection conn) throws Exception {
		this();
		logger.log(Level.DEBUG, "constructor1");
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Calendar.getInstance(new AnsiTime());
		
		this.data = makeData(conn, this, startDate, endDate);

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
	}

	protected CreditCardFeesByDayReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
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
	
	private List<RowData> makeData(Connection conn, CreditCardFeesByDayReport report, Calendar startDate, Calendar endDate) throws Exception {
		
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
//		ApplicationProperties creditFee = new ApplicationProperties();
//		creditFee.setPropertyId(ApplicationPropertyName.CREDIT_CARD_PROCESSING_FEE.fieldName());
//		creditFee.selectOne(conn);
		BigDecimal creditFee = ApplicationPropertyName.CREDIT_CARD_PROCESSING_FEE.getProperty(conn).getValueFloat();
		String feeSql = sql.replaceAll("\\$fee\\$", String.valueOf(creditFee));
		
		PreparedStatement ps = conn.prepareStatement(feeSql);
		logger.log(Level.DEBUG, feeSql);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		RowData newRow;
		while ( rs.next() ) {
			newRow = new RowData(rs);
			data.add(newRow);
		}
		rs.close();
		
		return data;
	}


	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		super.setHeaderRow(new ColumnHeader[] {

				new ColumnHeader("year", "Year", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("month", "Month", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("day","Day", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("fee","Fee Total", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("paymentId","Count Payment-Id", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
		});
		
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
		
		super.setPdfWidthPercentage(50.0F);
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(4000, 25.0F),
				new ColumnWidth(4000, 25.0F),
				new ColumnWidth(4000, 20.0F),
				new ColumnWidth(4000, 20.0F),
				new ColumnWidth(4000, 20.0F),
		});
		
//		super.setColumnWidths(new Integer[] {
//				(Integer)null,
//				ColumnWidth.DATETIME.width(),
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				Math.max(0, ColumnWidth.ADDRESS_NAME.width() - ColumnWidth.DATE.width()),
//		});

	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		String fileName = makeFileName(FILENAME, runDate, division, startDate, endDate);
		logger.log(Level.DEBUG, fileName);
		return fileName;
	}
	
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheet = workbook.createSheet();
		XLSBuilder.build(this, sheet, new ReportStartLoc(0, 0));
	}
	
	public static CreditCardFeesByDayReport buildReport(Connection conn) throws Exception {
		return new CreditCardFeesByDayReport(conn);
	}
	public static CreditCardFeesByDayReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CreditCardFeesByDayReport(conn, startDate, endDate);
	}


	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String year;
		public String month;
		public String day;
		public BigDecimal fee;
		public String paymentId;
	
		public RowData(ResultSet rs) throws SQLException {
			this.year = rs.getString("year");
			this.month = rs.getString("month");
			this.day = rs.getString("day");
			this.fee = rs.getBigDecimal("fee");
			this.paymentId = rs.getString("payment_id");
			
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}

		public String getMonth() {
			return month;
		}

		public void setMonth(String month) {
			this.month = month;
		}

		public String getDay() {
			return day;
		}

		public void setDay(String day) {
			this.day = day;
		}

		public BigDecimal getFee() {
			return fee;
		}

		public void setFee(BigDecimal fee) {
			this.fee = fee;
		}

		public String getPaymentId() {
			return paymentId;
		}

		public void setPaymentId(String paymentId) {
			this.paymentId = paymentId;
		}

		
	}

}