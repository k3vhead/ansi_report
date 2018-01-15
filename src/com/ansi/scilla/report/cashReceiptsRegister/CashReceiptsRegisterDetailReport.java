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

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.DateFormatter;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;
import com.thewebthing.commons.lang.StringUtils;

public class CashReceiptsRegisterDetailReport extends StandardReport {

	private static final long serialVersionUID = 1L;

	private final String sql = "select bill_to.name as 'bill_to_name'\r\n" + 
			", ticket.job_id\r\n" + 
			", ticket.ticket_id\r\n" + 
			", ticket.fleetmatics_id\r\n" + 
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
			"join division on division.division_id = job.division_id\r\n" + 
			"join quote on quote.quote_id = job.quote_id\r\n" + 
			"join address as job_site on job_site.address_id = quote.job_site_address_id\r\n" + 
			"join address as bill_to on bill_to.address_id = quote.bill_to_address_id\r\n" + 
			"join ticket_payment on ticket_payment.ticket_id = ticket.ticket_id\r\n" + 
			"join payment on payment.payment_id = ticket_payment.payment_id\r\n" + 
			"where payment_date >= ?\r\n" + 
			"and payment_date <= ?\r\n" + 
			"order by division_nbr, bill_to_name";
	
	public static final String REPORT_TITLE = "Cash Receipts Register Detail";
	private final String REPORT_NOTES = "notes go here";
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;

//	private Double totalAmount;
//	private Double totalTaxAmt;
//	private Double totalTotal;

//	private Double divAmount;
//	private Double divTaxAmt;
//	private Double divTotal;
	
	private List<RowData> data;
	
	Logger logger = Logger.getLogger("com.ansi.scilla.common.report");
	
	public CashReceiptsRegisterDetailReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn
	 * @param divisionId
	 * @throws Exception
	 */
	protected CashReceiptsRegisterDetailReport(Connection conn) throws Exception {
		this();

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
	
	private List<RowData> makeData(Connection conn, CashReceiptsRegisterDetailReport report, Calendar startDate, Calendar endDate) throws Exception {
		
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
//		this.lastDivision = -1;
//		this.totalAmount=0.00;
//		this.totalTaxAmt=0.00;
//		this.totalTotal=0.00;

//		this.divAmount=0.00;
//		this.divTaxAmt=0.00;
//		this.divTotal=0.00;

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		RowData newRow;
		RowData subTotalRow;
		while ( rs.next() ) {
			newRow = new RowData(rs, report);
//			if (newRow.divisionNbr == report.lastDivision) {
//				report.divAmount = report.divAmount + newRow.amount;
//				report.divTaxAmt = report.divTaxAmt + newRow.taxAmt;
//				report.divTotal = report.divTotal + newRow.total;
//			} else {
//				if (report.lastDivision != -1) {
//					subTotalRow = new RowData();
//					subTotalRow.setPaymentNote("Div "+lastDivision+" Result");
//					subTotalRow.setAmount(report.divAmount);
//					subTotalRow.setTaxAmt(report.divTaxAmt);
//					subTotalRow.setTotal(report.divTotal);
//					data.add(subTotalRow);
//				}
//				report.divAmount = newRow.amount;
//				report.divTaxAmt = newRow.taxAmt;
//				report.divTotal = newRow.total;
//				report.lastDivision = newRow.divisionNbr;
//			}
//			report.totalAmount = report.totalAmount + newRow.amount;
//			report.totalTaxAmt = report.totalTaxAmt + newRow.taxAmt;
//			report.totalTotal = report.totalTotal + newRow.total;

			data.add(newRow);
		}
//		if (report.lastDivision != -1) {
//			subTotalRow = new RowData();
//			subTotalRow.setPaymentNote("Div "+lastDivision+" Result");
//			subTotalRow.setAmount(report.divAmount);
//			subTotalRow.setTaxAmt(report.divTaxAmt);
//			subTotalRow.setTotal(report.divTotal);
//			data.add(subTotalRow);
//		}
//		RowData totalRow = new RowData();
//		totalRow.setPaymentNote("Grand Total");
//		totalRow.setAmount(report.totalAmount);
//		totalRow.setTaxAmt(report.totalTaxAmt);
//		totalRow.setTotal(report.totalTotal);
//		data.add(totalRow);
		rs.close();
		
		return data;
	}

	@SuppressWarnings("unchecked")	
	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("billToName","Client Name", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobId", "Job Code", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticketId", "Ticket", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("fleetmaticsId", "FM Ticket", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoiceDate", "Invoice Date", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("divisionDisplay", "Div", DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("paymentNote","Payment Notes", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("paymentDate", "Payment Date", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("checkNbr", "Check Number", DataFormats.INTEGER_FORMAT, SummaryType.NONE),
				new ColumnHeader("checkDate", "Check Date", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("amount","PPC\nPaid", DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("taxAmt","Taxes\nPaid", DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("total","Total\nPaid", DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("jobSiteName","Site Name", DataFormats.STRING_FORMAT, SummaryType.NONE),
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
		public String fleetmaticsId;
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
			this.fleetmaticsId = rs.getString("fleetmatics_id");
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
		public String getFleetmaticsId() {
			return fleetmaticsId;
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