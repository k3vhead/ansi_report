package com.ansi.scilla.report.pastDue;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.invoice.InvoiceTerm;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivEnd;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSReportBuilderUtils;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSReportFormatter;

public class PastDueReport2 extends StandardReport implements ReportByDivEnd, ReportByDivision {
	private static final long serialVersionUID = 1L;
	
	public static final String FILENAME = "Past Due";
	
	private final String sql = 
		"select bill_to.name as bill_to_name, " +
		"bill_to.address1, bill_to.address2, bill_to.city, bill_to.state, \n" +
			"\tcontract_contact.first_name as contract_first_name, contract_contact.last_name as contract_last_name, \n" +
		"case \n" +
			"\twhen contract_contact.preferred_contact = 'business_phone' then contract_contact.business_phone \n" +
		  	"\twhen contract_contact.preferred_contact = 'mobile_phone' then contract_contact.mobile_phone \n" +
		  	"\twhen contract_contact.preferred_contact = 'fax' then contract_contact.fax \n" +
		  	"\twhen contract_contact.preferred_contact = 'email' then contract_contact.email \n" +
		  	"\telse contract_contact.business_phone \n" +
		"end as contract_preferred_contact, \n" +
		"\tbilling_contact.first_name as billing_first_name, billing_contact.last_name as billing_last_name, \n" +
		"case \n" +
			"\twhen billing_contact.preferred_contact = 'business_phone' then billing_contact.business_phone \n" +
		  	"\twhen billing_contact.preferred_contact = 'mobile_phone' then billing_contact.mobile_phone \n" +
		  	"\twhen billing_contact.preferred_contact = 'fax' then billing_contact.fax \n" +
		  	"\twhen billing_contact.preferred_contact = 'email' then billing_contact.email \n" +
		  	"\telse billing_contact.business_phone \n" +
		"end as billing_preferred_contact, \n" +
		"job.job_id, \n" +
		"job.invoice_terms, \n" +
		"ticket.ticket_id, ticket.ticket_status, ticket_type,ticket.invoice_id, ticket.process_date, ticket.invoice_date, ticket.act_price_per_cleaning, \n" +
		"isnull(ticket_payment_totals.amount, '0.00') as amount_paid, \n" +
		"ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') as amount_due, \n" +
		"case \n" +
			"\twhen ticket.invoice_date < ? then ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') \n" +
		  	"\telse '0.00' \n" +
		"end as amount_past_due, \n" +
		"job_site.name as job_site_name, job_site.address1 as job_site_address1, \n" +
		"oldest_invoice_date, oldest_ticket, \n" +
		"quote.job_site_address_id \n" + 
		"from ticket \n" +
		"join job on ticket.job_id = job.job_id \n" +
		"join quote on job.quote_id = quote.quote_id \n" +
		"join division on division.division_id = act_division_id \n" +
		"join address as bill_to on bill_to.address_id = bill_to_address_id \n" +
		"join address as job_site on job_site.address_id = quote.job_site_address_id \n" +
		"join contact as contract_contact on contract_contact.contact_id = job.contract_contact_id \n" +
		"join contact as billing_contact on billing_contact.contact_id = job.contract_contact_id \n" +
		"left outer join (\n" +
			"\tselect ticket_id, \n" +
				"\t\tsum(amount) as amount, \n" +
				"\t\tsum(tax_amt) as tax_amt \n" +
				"\t\tfrom ticket_payment group by ticket_id) as ticket_payment_totals \n" +
			"\ton ticket_payment_totals.ticket_id = ticket.ticket_id \n" +
		"left outer join (\n" +
			"\tselect ticket.act_division_id as div, \n" +
				"\t\tbill_to_address_id as bill_to_id, \n" +
				"\t\tmin(invoice_date) as oldest_invoice_date, \n" +
				"\t\tmin(ticket_id) as oldest_ticket \n" +
				"\t\tfrom ticket \n" +
				"\t\tjoin job on job.job_id = ticket.job_id \n" +
				"\t\tjoin quote on quote.quote_id = job.quote_id where ticket_status = 'I' \n" +
				"\t\tgroup by act_division_id, bill_to_address_id) as bt_oldest_invoice \n" +
				"\t\ton bt_oldest_invoice.bill_to_id = bill_to_address_id and bt_oldest_invoice.div = ticket.act_division_id \n" +
			 	"\t\twhere oldest_invoice_date < ? \n" +
			 	"\t\tand ticket_status = 'I' \n" +
			 	"\t\tand ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') <> '0.00' \n" +
			 	"\t\tand ticket.act_division_id = ? \n" +
			 "\torder by division.division_nbr, bill_to.name, invoice_date";
	
	public static final String REPORT_TITLE = "Past Due Report";
	
	private Calendar pastDueDate;
	private Calendar createdDate;
	private Integer daysPastDue;
	private String div;
	private List<List<RowData>> reportRows;
	private XSSFWorkbook xls;
	Logger logger;
	
	private PastDueReport2(Connection conn, Calendar pastDueDate, Integer divisionId) throws Exception {
		super();
		this.logger = LogManager.getLogger("com.ansi.scilla.report.reportBuilder");
		this.pastDueDate = pastDueDate;
		this.div = makeDiv(conn, divisionId);
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.LANDSCAPE);
		makeData(conn, pastDueDate, divisionId);
	}
	
	private String makeDiv(Connection conn, Integer divisionId) throws Exception{
		Division d = new Division();
		d.setDivisionId(divisionId);
		d.selectOne(conn);
		return d.getDivisionNbr() + "-" + d.getDivisionCode();
	}
	
	public Calendar getStartDate(){
		createdDate = Calendar.getInstance();
		return createdDate;
	}
	
	public Calendar getAgingDate(){
		//pastDueDate = Calendar.getInstance();
		return pastDueDate;
	}
	
	public Integer getDaysPastDue(){
		long end = createdDate.getTimeInMillis();
		long start = pastDueDate.getTimeInMillis();
		daysPastDue = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end-start));
		return daysPastDue;
	}
	
	public String getDiv() {
		return this.div;
	}
	
	private void makeData(Connection conn, Calendar pastDueDate, Integer divisionId) throws Exception {
		//super.setSubtitle(makeSubtitle());
		super.setHeaderRow(new ColumnHeader[] {
			new ColumnHeader("billToName", "BILL TO NAME", 2, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 20),//BILL TO NAME
			new ColumnHeader("ticketId","Ticket\nInvoice", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),//JOB#
			new ColumnHeader("invoiceDate", "Completed\nInvoiced", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),//completed invoiced dates
			new ColumnHeader("jobId", "JOB", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),//job number
			new ColumnHeader("actPPC", "PPC", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//actPPC
			new ColumnHeader("amountPaid", "PAID", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//Paid Amount
			new ColumnHeader("amountDue", "DUE", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//amountDue
			new ColumnHeader("amountPastDue", "PAST DUE", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//amountDue
			new ColumnHeader("jobSiteAddress", "SITE ADDRESS", 2, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 20),//siteAddress
		});		
		
		java.sql.Date myDate = new java.sql.Date(pastDueDate.getTimeInMillis());
		PreparedStatement psData = conn.prepareStatement(sql);
		psData.setDate(1, myDate);
		psData.setDate(2, myDate);
		psData.setInt(3, divisionId);
		
		ResultSet rsData = psData.executeQuery();
		
		while ( rsData.next() ) {
			super.addDataRow(new RowData(rsData));
		}
		rsData.close();
		
		this.reportRows = makeReportRows();	
		this.xls = makeXLS();
		
		
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getAgingDateMethod = this.getClass().getMethod("getAgingDate", (Class<?>[])null);
		Method daysPastDueMethod = this.getClass().getMethod("getDaysPastDue", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created: ", getStartDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("Aging Date: ", getAgingDateMethod, 1, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("Days Past Due: ", daysPastDueMethod, 2, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Division: ", getDivMethod, 0, DataFormats.STRING_FORMAT)
		});
		super.makeHeaderRight(headerRight);
	
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(11000, 200.0F),			//Bill TO Name (colspan 2)
				(ColumnWidth)null,
				new ColumnWidth(2500, 32.0F),		// Ticket Invoice
				new ColumnWidth(2750, 57.0F),			// Completed Invoice
				new ColumnWidth(2500, 32.0F),	// Job
				new ColumnWidth(2500, 46.0F),			// PPC
				new ColumnWidth(2500, 46.0F),			// Paid
				new ColumnWidth(2500, 46.0F),	// Due
				new ColumnWidth(2500, 46.0F),	// Past Due
				new ColumnWidth(11000, 200.0F),	// Site Address (colspan 2)
				(ColumnWidth)null,			// Site Address
		});
	}
	
		
	
	
	

	private List<List<RowData>> makeReportRows() {
		List<List<RowData>> reportRows = new ArrayList<List<RowData>>();
		List<RowData> billToGroup = new ArrayList<RowData>();
		String previousBillTo = null;
		for ( int i = 0; i < super.getDataRows().size(); i++ ) {
			RowData rowData = (RowData)super.getDataRows().get(i);
			if ( previousBillTo != null && ! rowData.getBillToName().equals(previousBillTo)) {
				reportRows.add(billToGroup);
				billToGroup = new ArrayList<RowData>();
			}
			billToGroup.add(rowData);
			previousBillTo = rowData.getBillToName();
		}
		return reportRows;
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	
	public XSSFWorkbook makeXLS() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		sheet.getPrintSetup().setLandscape(true);
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setFitWidth((short)1);
		XLSReportFormatter rf = new XLSReportFormatter(workbook);
		RowData rowData = null;
		
		XSSFRow row = null;
		XSSFCell cell = null;
		int rowNum = XLSReportBuilderUtils.makeHeaderRowCount(this) + 2;
		
		Integer fontHeight = 9;
		XSSFFont fontDefaultFont = workbook.createFont();
		fontDefaultFont.setFontHeight(fontHeight);
		
		CellStyle cellStyleLeft = workbook.createCellStyle();
		cellStyleLeft.cloneStyleFrom(rf.cellStyleStandardLeft);
	    cellStyleLeft.setAlignment(CellStyle.ALIGN_LEFT);
	    
	    CellStyle cellStyleDate = workbook.createCellStyle();
	    cellStyleDate.cloneStyleFrom(rf.cellStyleDateLeft);
	    
	    CellStyle cellStyleDecimal = workbook.createCellStyle();
	    cellStyleDecimal.cloneStyleFrom(rf.cellStyleStandardCurrency);
	    cellStyleDecimal.setAlignment(CellStyle.ALIGN_RIGHT);
	    
	    CellStyle cellStyleSummary = sheet.getWorkbook().createCellStyle();
		cellStyleSummary.cloneStyleFrom(rf.cellStyleStandardCurrency);
		cellStyleSummary.setBorderBottom(CellStyle.BORDER_THICK);
		cellStyleSummary.setBorderTop(CellStyle.BORDER_THIN);
	    
	    CellStyle cellStyleDateTime = workbook.createCellStyle();
	    cellStyleDateTime.cloneStyleFrom(rf.cellStyleDateTimeLeft);
	    
	    HashMap<String, CellStyle> styleMap = new HashMap<String, CellStyle>();
	    styleMap.put("cellStyleLeft", cellStyleLeft);
	    styleMap.put("cellStyleDate", cellStyleDate);
	    styleMap.put("cellStyleDecimal", cellStyleDecimal);
	    
	    ReportStartLoc reportStartLoc = new ReportStartLoc(0,0);
	    XLSReportBuilderUtils.makeStandardHeader(this, reportStartLoc, sheet);
	    XLSReportBuilderUtils.makeColumnHeader(this, reportStartLoc, sheet, rf);
	    
		for ( List<RowData> billToGroup : this.reportRows ) {
			Double ppcTotal = 0.0D;
			Double paidAmt = 0.0D;
			Double amtDue = 0.0D;
			Double amtPastDue = 0.0D;
			Double pastDueTotal = 0.0D;
			
			int colNum = 0;
			
			rowData = billToGroup.get(0);
			ppcTotal += rowData.getActPPC();	
			paidAmt += rowData.getAmountPaid();
			amtDue += rowData.getAmountDue();
			amtPastDue += rowData.getAmountPastDue();
			makeRow0(sheet, rowNum, billToGroup.get(0).getBillToName(), billToGroup.get(0).getJobSiteName(), rowData, styleMap);
			rowNum++;
			makeRow1(sheet, rowNum, billToGroup.get(0).getAddress1(), billToGroup.get(0).getJobSiteAddress(), rowData, styleMap);
			rowNum++;

			
			if ( billToGroup.size() > 1 ) {
				rowData = billToGroup.get(1);
				ppcTotal += rowData.getActPPC();	
				paidAmt += rowData.getAmountPaid();
				amtDue += rowData.getAmountDue();
				amtPastDue += rowData.getAmountPastDue();
			} else {
				rowData = null;
			}
			makeRow0(sheet, rowNum, billToGroup.get(0).getCity() + ", " + billToGroup.get(0).getState(), "", rowData, styleMap);
			rowNum++;
			makeRow1(sheet, rowNum, billToGroup.get(0).getContractFirstName() + " " + billToGroup.get(0).getContractLastName()
					+ ", " + billToGroup.get(0).getContractPreferredContact(), "", rowData, styleMap);
			rowNum++;
			
			
			if ( billToGroup.size() > 2 ) {
				rowData = billToGroup.get(2);
				ppcTotal += rowData.getActPPC();	
				paidAmt += rowData.getAmountPaid();
				amtDue += rowData.getAmountDue();
				amtPastDue += rowData.getAmountPastDue();
			} else {
				rowData = null;
			}
			makeRow0(sheet, rowNum, billToGroup.get(0).getBillingFirstName() + " " + billToGroup.get(0).getBillingLastName()
					+ ", " + billToGroup.get(0).getBillingPreferredContact(), "", rowData, styleMap);
			rowNum++;
			makeRow1(sheet, rowNum, "", "", rowData, styleMap);
			rowNum++;
			
			for ( int i = 3; i < billToGroup.size(); i++) {
				rowData = billToGroup.get(2);
				ppcTotal += rowData.getActPPC();	
				paidAmt += rowData.getAmountPaid();
				amtDue += rowData.getAmountDue();
				amtPastDue += rowData.getAmountPastDue();
				
				makeRow0(sheet, rowNum, "", billToGroup.get(i).getJobSiteName(), billToGroup.get(i), styleMap);
				rowNum++;
				makeRow1(sheet, rowNum, "", billToGroup.get(i).getJobSiteAddress(), billToGroup.get(i), styleMap);
				rowNum++;
			}
			
			colNum = 5;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellValue("PPC: " + ppcTotal);
			cell.setCellStyle(cellStyleSummary);
			colNum++; //col 6
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellValue("Paid: " + paidAmt);
			cell.setCellStyle(cellStyleSummary);
			colNum++; //col 7
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellValue("Total Due: " + amtDue);
			cell.setCellStyle(cellStyleSummary);
			colNum++; //col 8
//			colNum++; //col 8
			
			pastDueTotal = amtPastDue;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleSummary);
			cell.setCellValue("Past Due Total: " + pastDueTotal);
			colNum++; //col 9
			
			ppcTotal = 0.0D;
			paidAmt = 0.0D;
			amtDue = 0.0D;
			amtPastDue = 0.0D;
			rowNum++;
		}
		return workbook;
	}
		
		
	private void makeRow0(XSSFSheet sheet, int rowNum, String column0, String column7, RowData rowData, HashMap<String,CellStyle> styleMap) {
		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell cell = null;		
		int colNum = 0;
		CellStyle cellStyleLeft = styleMap.get("cellStyleLeft");
		CellStyle cellStyleCenter = styleMap.get("cellStyleCenter");
		CellStyle cellStyleDate = styleMap.get("cellStyleDate");
		CellStyle cellStyleDecimal = styleMap.get("cellStyleDecimal");
		
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(column0);
		colNum = 2; //col 2
		
		if ( rowData != null ) {
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleLeft);
			cell.setCellValue(rowData.getTicketId());
			colNum++; //col 3
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDate);
			if ( rowData.getProcessDate() != null ) {
				cell.setCellValue(rowData.getProcessDate());
			}
			colNum++; //col 4
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleLeft);
			cell.setCellValue(rowData.getJobId());
			colNum++; //col 5
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDecimal);
			cell.setCellValue(rowData.getActPPC());		
			colNum++; //col 6
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDecimal);
			cell.setCellValue(rowData.getAmountPaid());
			colNum++; //col 7
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDecimal);
			cell.setCellValue(rowData.getAmountDue());
			colNum++; //col 8
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDecimal);
			cell.setCellValue(0.00);
			colNum++; //col 9
		}
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 8, 9));
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(column7);
		colNum++; //col 10
	}

	
	private void makeRow1(XSSFSheet sheet, int rowNum, String column0, String column7, RowData rowData, HashMap<String,CellStyle> styleMap) {
		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell cell = null;		
		int colNum = 0;
		CellStyle cellStyleLeft = styleMap.get("cellStyleLeft");
		CellStyle cellStyleDate = styleMap.get("cellStyleDate");
		CellStyle cellStyleDecimal = styleMap.get("cellStyleDecimal");
		
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(column0);
		colNum = 2; //col 2
		
		if ( rowData != null ) {
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleLeft);
			cell.setCellValue(rowData.getInvoiceId());
			colNum++; //col 3
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDate);
			cell.setCellValue(rowData.getInvoiceDate());
			colNum++; //col 4
			
			colNum++; //col 5
			
			colNum++; //col 6
			
			colNum++; //col 7
			
			colNum++; //col 8
		}
		
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 8, 9));
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(column7);
		colNum++; //col 9
		
			
	}

	


	
	public static PastDueReport2 buildReport(Connection conn, Calendar pastDueDate, Integer divisionId) throws Exception {
		return new PastDueReport2(conn, pastDueDate, divisionId);
	}
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		
		private String billToName;
		private String preBillToName;
		private String address1;
		private String address2;
		private String city;
		private String state;
		private String contractFirstName;
		private String contractLastName;
		private String contractPreferredContact;
		private String billingFirstName;
		private String billingLastName;
		private String billingPreferredContact;
		private String jobId;
		private String jobNbr;
		private String invoiceTerms;
		private String ticketId;
		private String ticketStatus;
		private String ticketType;
		private String invoiceId;
		private Date processDate;
		private Date invoiceDate;
		private Integer actPPC;
		private Integer amountPaid;
		private Integer amountDue;
		private Integer amountPastDue;
		private String jobSiteName;
		private String jobSiteAddress;
		private Integer totalPPC;
		
		public RowData(ResultSet rs) throws SQLException {
			this.billToName = rs.getString("bill_to_name");
			this.address1 = rs.getString("address1");
			this.address2 = rs.getString("address2");
			this.city = rs.getString("city");
			this.state = rs.getString("state");
			this.contractFirstName = rs.getString("contract_first_name");
			this.contractLastName = rs.getString("contract_last_name");
			this.contractPreferredContact = rs.getString("contract_preferred_contact");
			this.billingFirstName = rs.getString("billing_first_name");
			this.billingLastName = rs.getString("billing_last_name");
			this.billingPreferredContact = rs.getString("billing_preferred_contact");
			this.jobId = rs.getString("job_id");
			this.ticketId = rs.getString("ticket_id");
			this.ticketStatus = rs.getString("ticket_status");
			this.ticketType = rs.getString("ticket_type");
			this.invoiceId = rs.getString("invoice_id");
			java.sql.Date processDate = rs.getDate("process_date");
			if ( processDate != null ) {
				this.processDate = new Date(processDate.getTime());
			}
			java.sql.Date invoiceDate = rs.getDate("invoice_date");
			this.invoiceDate = new Date(invoiceDate.getTime());
			this.actPPC = rs.getInt("act_price_per_cleaning");
			this.amountPaid = rs.getInt("amount_paid");
			this.amountDue = rs.getInt("amount_due");
			this.amountPastDue = rs.getInt("amount_past_due");
			this.jobSiteName = rs.getString("job_site_name");
			this.jobSiteAddress = rs.getString("job_site_address1");
			String invoiceTerms = rs.getString("invoice_terms");
			if ( ! StringUtils.isBlank(invoiceTerms)) {
				try {
					this.invoiceTerms = InvoiceTerm.valueOf(invoiceTerms).display();
				} catch ( Exception e) {
					this.invoiceTerms = invoiceTerms;
				}
			}
		}
		
		public String getBillToName() {
			return billToName;
		}
		
		public String getPreBillToName(){
			return preBillToName;
		}

		public String getAddress1() {
			return address1;
		}

		public String getAddress2() {
			return address2;
		}

		public String getCity() {
			return city;
		}

		public String getState() {
			return state;
		}

		public String getContractFirstName() {
			return contractFirstName;
		}

		public String getContractLastName() {
			return contractLastName;
		}

		public String getContractPreferredContact() {
			return contractPreferredContact;
		}

		public String getBillingFirstName() {
			return billingFirstName;
		}

		public String getBillingLastName() {
			return billingLastName;
		}

		public String getBillingPreferredContact() {
			return billingPreferredContact;
		}

		public String getJobId() {
			return jobId;
		}
		
		public String getJobNbr(){
			return jobNbr;
		}

		public String getTicketId() {
			return ticketId;
		}

		public String getTicketStatus() {
			return ticketStatus;
		}

		public String getTicketType() {
			return ticketType;
		}

		public String getInvoiceId() {
			return invoiceId;
		}

		public Date getProcessDate() {
			return processDate;
		}

		public Date getInvoiceDate() {
			return invoiceDate;
		}

		public Integer getActPPC() {
			return actPPC;
		}

		public Integer getAmountPaid() {
			return amountPaid;
		}

		public Integer getAmountDue() {
			return amountDue;
		}

		public Integer getAmountPastDue() {
			return amountPastDue;
		}

		public String getJobSiteName() {
			return jobSiteName;
		}
		
		public String getJobSiteAddress(){
			return jobSiteAddress;
		}
		
		public Integer getTotalPPC(){
			totalPPC = getActPPC();
			return totalPPC;
		}

		public String getInvoiceTerms() {
			return invoiceTerms;
		}

		public void setInvoiceTerms(String invoiceTerms) {
			this.invoiceTerms = invoiceTerms;
		}
		
		
	}
	
}




