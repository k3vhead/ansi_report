package com.ansi.scilla.report.cashReceiptsRegister;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;
import com.thewebthing.commons.lang.StringUtils;

public class CashReceiptsRegisterDetailReport extends StandardReport implements ReportByStartEnd {

	public static final String FILENAME = "CRR_Detail";
	
	private static final long serialVersionUID = 1L;

	private final String sql = "select bill_to.name as 'bill_to_name'\r\n" + 
			", ticket.job_id\r\n" + 
			", ticket.ticket_id\r\n" + 
			", ticket.invoice_id\r\n" + 
			", invoice_date\r\n" + 
			", division.division_nbr\r\n" + 
			", division.division_code\r\n" +
			", payment.payment_note\r\n" + 
			", payment.payment_date\r\n" + 
			", ticket_payment.amount\r\n" + 
			", ticket_payment.tax_amt\r\n" + 
			", ticket_payment.amount + ticket_payment.tax_amt as total\r\n" + 
			", job_site.name as job_site_name\r\n" + 
			", payment.check_nbr\r\n" +
			", payment.check_date\r\n" +
			"from ticket \r\n" + 
			"join job on job.job_id = ticket.job_id\r\n" + 
			"join division on division.division_id = ticket.act_division_id\r\n" + 
			"join quote on quote.quote_id = job.quote_id\r\n" + 
			"join address as job_site on job_site.address_id = quote.job_site_address_id\r\n" + 
			"join address as bill_to on bill_to.address_id = quote.bill_to_address_id\r\n" + 
			"join ticket_payment on ticket_payment.ticket_id = ticket.ticket_id\r\n" + 
			"join payment on payment.payment_id = ticket_payment.payment_id\r\n" + 
			"where payment_date >= ?\r\n" + 
			"and payment_date <= ?\r\n" + 
			"order by division_nbr, bill_to_name, ticket.ticket_id";
	
	public static final String REPORT_TITLE = "Cash Receipts Register Detail";
//	private final String REPORT_NOTES = "notes go here";
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;
	
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public CashReceiptsRegisterDetailReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database exception
	 * @throws Exception something bad happened
	 */
	protected CashReceiptsRegisterDetailReport(Connection conn) throws Exception {
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

	protected CashReceiptsRegisterDetailReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
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
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	private List<RowData> makeData(Connection conn, CashReceiptsRegisterDetailReport report, Calendar startDate, Calendar endDate) throws Exception {
		
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		RowData newRow;
		while ( rs.next() ) {
			newRow = new RowData(rs, report);
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
				new ColumnHeader("billToName","Client Name", 3, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobId", "Job Code", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticketId", "Ticket", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoiceDate", "Invoice Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoiceId", "Invoice", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("divisionDisplay", "Div", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("paymentNote","Payment Notes", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("paymentDate", "Payment Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("checkNbr", "Check Number", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("checkDate", "Check Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("amount","PPC\nPaid", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("taxAmt","Taxes\nPaid", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("total","Total\nPaid", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("jobSiteName","Site Name", 2, DataFormats.STRING_FORMAT, SummaryType.NONE),
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
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(null, 44.33F),	// client name 1
				new ColumnWidth(3750, 44.33F),	// client name 2
				new ColumnWidth(null, 44.33F),	// client name 3
				new ColumnWidth(null, 32.0F),	// job code
				new ColumnWidth(null, 35.0F),	// ticket
				new ColumnWidth(null, 49.0F),	// invoicedate
				new ColumnWidth(null, 32.8F),	// invoice
				new ColumnWidth(null, 32.8F),	// div
				new ColumnWidth(null, 46.0F),	// pmt notes
				new ColumnWidth(null, 49.0F),	// payment date
				new ColumnWidth(null, 66.0F),	// check number
				new ColumnWidth(null, 49.0F),	// check date
				new ColumnWidth(null, 43.0F),	// ppc paid
				new ColumnWidth(null, 43.0F),	// taxes paid
				new ColumnWidth(null, 43.0F),	// total paid
				new ColumnWidth(7250, 57.0F),	// site name 1
				new ColumnWidth(null, 57.0F),	// site name 2
		});
	}
	
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheet = workbook.createSheet();
		XLSBuilder.build(this, sheet, new ReportStartLoc(0, 0));
	}
	
	public static CashReceiptsRegisterDetailReport buildReport(Connection conn) throws Exception {
		return new CashReceiptsRegisterDetailReport(conn);
	}
	public static CashReceiptsRegisterDetailReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CashReceiptsRegisterDetailReport(conn, startDate, endDate);
	}


	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String billToName;
		public Integer jobId;
		public Integer ticketId;
		public Integer invoiceId;
		public Date invoiceDate;
		public Integer divisionNbr;
		public String divisionCode;
		public String paymentNote;
		public Date paymentDate;
		public Double amount;
		public Double taxAmt;
		public Double total;
		public String jobSiteName;
		public String checkNbr;
		public Date checkDate;
		
//		", payment.check_nbr\r\n" +
//		", payment.check_date\r\n" +

		public RowData(ResultSet rs, CashReceiptsRegisterDetailReport report) throws SQLException {
			this.billToName = StringUtils.substring(rs.getString("bill_to_name"), 0, 25);
//			this.billToName = this.billToName.substring(0, Math.min(this.billToName.length(), 25));
			this.jobId = rs.getInt("job_id");
			this.ticketId = rs.getInt("ticket_id");
			this.invoiceId = rs.getInt("invoice_id");
			this.invoiceDate = new Date( rs.getDate("invoice_date").getTime());
			this.divisionNbr = rs.getInt("division_nbr");
			this.paymentNote = StringUtils.substring(rs.getString("payment_note"), 0, 15);
//			this.paymentNote = this.paymentNote.substring(0, Math.min(this.paymentNote.length(), 15));
			this.paymentDate = new Date( rs.getDate("payment_date").getTime());
			this.checkNbr = rs.getString("check_nbr");
			this.checkDate = new Date(rs.getDate("check_date").getTime());
			this.amount = rs.getBigDecimal("amount").doubleValue();
			this.taxAmt = rs.getBigDecimal("tax_amt").doubleValue();
			this.total = rs.getBigDecimal("total").doubleValue();
			this.jobSiteName = StringUtils.substring(rs.getString("job_site_name"), 0, 20);
//			this.jobSiteName = this.jobSiteName.substring(0, Math.min(this.jobSiteName.length(), 20));
			this.divisionCode = rs.getString("division_code");
		}

		public RowData() {
			super();
		}

		public String getBillToName() {
			return billToName;
		}
		public Integer getJobId() {
			return jobId;
		}
		public Integer getTicketId() {
			return ticketId;
		}
		public Integer getInvoiceId() {
			return invoiceId;
		}
		public Date getInvoiceDate() {
			return invoiceDate;
		}
		public Integer getDivisionNbr() {
			return divisionNbr;
		}
		public void setDivisionNbr(Integer divisionNbr) {
			this.divisionNbr = divisionNbr;
		}
		public String getPaymentNote() {
			return paymentNote;
		}
		public void setPaymentNote(String paymentNote) {
			this.paymentNote = paymentNote;
		}
		public Date getPaymentDate() {
			return paymentDate;
		}
		public Double getAmount() {
			return amount;
		}
		public void setAmount(Double amount) {
			this.amount = amount;
		}
		public Double getTaxAmt() {
			return taxAmt;
		}
		public void setTaxAmt(Double taxAmt) {
			this.taxAmt = taxAmt;
		}
		public Double getTotal() {
			return total;
		}
		public void setTotal(Double total) {
			this.total = total;
		}
		public String getJobSiteName() {
			return jobSiteName;
		}
		public String getDivisionCode() {
			return divisionCode;
		}
		public String getDivisionDisplay() {
			return this.getDivisionNbr() + "-" + this.getDivisionCode();
		}
		public String getCheckNbr(){
			return checkNbr;
		}
		public Date getCheckDate(){
			return checkDate;
		}
	}

}