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
import com.ansi.scilla.common.jobticket.TicketType;
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

public class CreditCardFeesSummaryReport extends StandardReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;

	private final String sql = "select concat(division_nbr,'-',division_code) as div,\n" + 
			"		payment.*,\n" + 
			"		ticket_payment.*,\n" + 
			"		year(payment_date) as year,\n" + 
			"		month(payment_date) as month,\n" + 
			"		abs(ticket_payment.amount+ticket_payment.tax_amt) as total,\n" + 
			"		abs(ticket_payment.amount+ticket_payment.tax_amt)*0.03 as fee,\n" + 
			"		day(payment_date) as day \n" + 
			"from payment \n" + 
			"join ticket_payment on ticket_payment.payment_id = payment.payment_id \n" + 
			"join ticket on ticket_payment.ticket_id = ticket.ticket_id \n" + 
			"join division on division.division_id = act_division_id \n" + 
			"where payment_method = 'credit_card' \n" + 
			"and payment_date >= ? and payment_date < ? \n" + 
			"order by div, payment_date";
			

	
	public static final String REPORT_TITLE = "WO and Fees Summary";
	public static final String FILENAME = "WOandFeesSummary";
//	private final String REPORT_NOTES = "notes go here";
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;
	
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public CreditCardFeesSummaryReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database exception
	 * @throws Exception something bad happened
	 */
	protected CreditCardFeesSummaryReport(Connection conn) throws Exception {
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

	protected CreditCardFeesSummaryReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
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
	
	private List<RowData> makeData(Connection conn, CreditCardFeesSummaryReport report, Calendar startDate, Calendar endDate) throws Exception {
		
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		logger.log(Level.DEBUG, sql);
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

				new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
//				new ColumnHeader("type", "Type", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("sum","Sum", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				
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
	
	public static CreditCardFeesSummaryReport buildReport(Connection conn) throws Exception {
		return new CreditCardFeesSummaryReport(conn);
	}
	public static CreditCardFeesSummaryReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CreditCardFeesSummaryReport(conn, startDate, endDate);
	}


	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String div;
//		public String ticketType;
		public BigDecimal sum;
		
	
		public RowData(ResultSet rs) throws SQLException {
			this.div = rs.getString("div");
//			String ticketTypeDisplay = rs.getString("ticket_type");
//			try {
//				TicketType ticketType = TicketType.lookup(ticketTypeDisplay);
//				if(ticketType == null) {
//					this.ticketType = ticketTypeDisplay;
//				} else {
//					this.ticketType = ticketType.display();
//				}
//			} catch (Exception e) {
//				this.ticketType = ticketTypeDisplay;
//			}
			this.sum = rs.getBigDecimal("fee");
			
		}


		public String getDiv() {
			return div;
		}

		public void setDiv(String div) {
			this.div = div;
		}

//		public String getType() {
//			return ticketType;
//		}
//
//
//		public void setType(String type) {
//			this.ticketType = type;
//		}

		public BigDecimal getSum() {
			return sum;
		}

		public void setSum(BigDecimal sum) {
			this.sum = sum;
		}

		
	}

}