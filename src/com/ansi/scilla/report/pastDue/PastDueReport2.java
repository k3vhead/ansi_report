package com.ansi.scilla.report.pastDue;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;
import com.ansi.scilla.report.reportBuilder.XLSReportBuilderUtils;
import com.ansi.scilla.report.reportBuilder.XLSReportFormatter;

public class PastDueReport2 extends StandardReport {
	private static final long serialVersionUID = 1L;
	
	private final String sql = 
		"select bill_to.name as bill_to_name, " +
		"bill_to.address1, bill_to.address2, bill_to.city, bill_to.state, \n" +
			"\tcontract_contact.first_name, contract_contact.last_name, \n" +
		"case \n" +
			"\twhen contract_contact.preferred_contact = 'business_phone' then contract_contact.business_phone \n" +
		  	"\twhen contract_contact.preferred_contact = 'mobile_phone' then contract_contact.mobile_phone \n" +
		  	"\twhen contract_contact.preferred_contact = 'fax' then contract_contact.fax \n" +
		  	"\twhen contract_contact.preferred_contact = 'email' then contract_contact.email \n" +
		  	"\telse contract_contact.business_phone \n" +
		"end as contract_preferred_contact, \n" +
		"case \n" +
			"\twhen billing_contact.preferred_contact = 'business_phone' then billing_contact.business_phone \n" +
		  	"\twhen billing_contact.preferred_contact = 'mobile_phone' then billing_contact.mobile_phone \n" +
		  	"\twhen billing_contact.preferred_contact = 'fax' then billing_contact.fax \n" +
		  	"\twhen billing_contact.preferred_contact = 'email' then billing_contact.email \n" +
		  	"\telse billing_contact.business_phone \n" +
		"end as contract_preferred_contact, \n" +
		"job.job_id, \n" +
		"ticket.ticket_id, ticket.ticket_status, ticket_type,ticket.invoice_id, ticket.process_date, ticket.invoice_date, ticket.act_price_per_cleaning, \n" +
		"isnull(ticket_payment_totals.amount, '0.00') as amount_paid, \n" +
		"ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') as amount_due, \n" +
		"case \n" +
			"\twhen ticket.invoice_date < ? then ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') \n" +
		  	"\telse '0.00' \n" +
		"end as amount_past_due, \n" +
		"job_site.name as job_site_name, oldest_invoice_date, oldest_ticket, \n" +
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
	
	public static final String REPORT_TITLE = "Six Month Rolling Volume Summary";
	
	private Calendar pastDueDate;
	private Calendar createdDate;
	private Integer daysPastDue;
	private String div;
	private List<List<RowData>> reportRows;
	
	private PastDueReport2(Connection conn, Calendar pastDueDate, Integer divisionId) throws Exception {
		super();
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
			new ColumnHeader("billToName", "BILL TO NAME", DataFormats.STRING_FORMAT, SummaryType.NONE),//BILL TO NAME
			new ColumnHeader("jobId","JOB #", DataFormats.STRING_FORMAT, SummaryType.NONE),//JOB#
			new ColumnHeader("invoiceDate", "Contracts", DataFormats.DATE_FORMAT, SummaryType.NONE),//completed invoiced dates
			new ColumnHeader("jobId", "JOB", DataFormats.STRING_CENTERED, SummaryType.NONE),//job number
			new ColumnHeader("actPPC", "PPC", DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//actPPC
			new ColumnHeader("amountPaid", "PAID AMOUNT", DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//Paid Amount
			new ColumnHeader("amountDue", "AMOUNT DUE", DataFormats.DECIMAL_FORMAT, SummaryType.NONE),//amountDue
			new ColumnHeader("jobSiteAddress", "SITE ADDRESS", DataFormats.STRING_FORMAT, SummaryType.NONE),//siteAddress
		});		
		
		java.sql.Date myDate = new java.sql.Date(pastDueDate.getTimeInMillis());
		
		PreparedStatement psData = conn.prepareStatement(sql);
		psData.setDate(1, myDate);
		psData.setDate(2, myDate);
		psData.setInt(3, divisionId);
		
		System.out.println(sql);
		
		ResultSet rsData = psData.executeQuery();
		
		while ( rsData.next() ) {
			super.addDataRow(new RowData(rsData));
		}
		rsData.close();
		
		// JWL: THis is new:
		this.reportRows = makeReportRows();	
		// JWL: End of new
		
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getAgingDateMethod = this.getClass().getMethod("getAgingDate", (Class<?>[])null);
		Method daysPastDueMethod = this.getClass().getMethod("getDaysPastDue", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created: ", getStartDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("Aging Date: ", getAgingDateMethod, 0, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("Days Past Due: ", daysPastDueMethod, 2, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Division: ", getDivMethod, 0, DataFormats.STRING_FORMAT)
		});
		super.makeHeaderRight(headerRight);
		
	}
		
	
	
	

	// JWL: THis is new
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
	
	
	
	public XSSFWorkbook makeXLS() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		sheet.getPrintSetup().setLandscape(true);
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setFitWidth((short)1);
		XLSReportFormatter rf = new XLSReportFormatter(workbook);
		//this.populateXlsSheet(sheet, rf);
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
			Double pastDueTotal = 0.0D;
			
			int colNum = 0;
			
			rowData = billToGroup.get(0);
			ppcTotal += rowData.getActPPC();	
			paidAmt += rowData.getAmountPaid();
			amtDue += rowData.getAmountDue();
			makeRow0(sheet, rowNum, billToGroup.get(0).getBillToName(), billToGroup.get(0).getJobSiteName(), rowData, styleMap);
			rowNum++;
			makeRow1(sheet, rowNum, billToGroup.get(0).getAddress1(), billToGroup.get(0).getJobSiteAddress(), rowData, styleMap);
			rowNum++;

			
			if ( billToGroup.size() > 1 ) {
				rowData = billToGroup.get(1);
				ppcTotal += rowData.getActPPC();	
				paidAmt += rowData.getAmountPaid();
				amtDue += rowData.getAmountDue();
			} else {
				rowData = null;
			}
			makeRow0(sheet, rowNum, billToGroup.get(0).getCity() + ", " + billToGroup.get(0).getState(), "", rowData, styleMap);
			rowNum++;
			makeRow1(sheet, rowNum, billToGroup.get(0).getFirstName() + ", " + billToGroup.get(0).getLastName(), "", rowData, styleMap);
			rowNum++;
			
			
			if ( billToGroup.size() > 2 ) {
				rowData = billToGroup.get(2);
				ppcTotal += rowData.getActPPC();	
				paidAmt += rowData.getAmountPaid();
				amtDue += rowData.getAmountDue();
			} else {
				rowData = null;
			}
			makeRow0(sheet, rowNum, billToGroup.get(0).getPreferredContact(), "", rowData, styleMap);
			rowNum++;
			makeRow1(sheet, rowNum, "", "", rowData, styleMap);
			rowNum++;
			
			for ( int i = 3; i < billToGroup.size(); i++) {
				rowData = billToGroup.get(2);
				ppcTotal += rowData.getActPPC();	
				paidAmt += rowData.getAmountPaid();
				amtDue += rowData.getAmountDue();
				
				makeRow0(sheet, rowNum, "", "", billToGroup.get(i), styleMap);
				rowNum++;
				makeRow1(sheet, rowNum, "", "", billToGroup.get(i), styleMap);
				rowNum++;
			}
			
			colNum = 4;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleSummary);
			cell.setCellValue(ppcTotal);
			colNum++; //col 5
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleSummary);
			cell.setCellValue(paidAmt);
			colNum++; //col 6
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleSummary);
			cell.setCellValue(amtDue);
			colNum++; //col 7
			
			pastDueTotal = amtDue - paidAmt;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleSummary);
			cell.setCellValue("Past Due Total: " + pastDueTotal);
			colNum++; //col 8
			
			ppcTotal = 0.0D;
			paidAmt = 0.0D;
			amtDue = 0.0D;
			rowNum++;
		}
		return workbook;
	}
		
		
	private void makeRow0(XSSFSheet sheet, int rowNum, String column0, String column7, RowData rowData, HashMap<String,CellStyle> styleMap) {
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
			cell.setCellValue(rowData.getJobId());
			colNum++; //col 3
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleDate);
			cell.setCellValue(rowData.getProcessDate());
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
		}
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 8, 9));
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(column7);
		colNum++; //col 9
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

	

	// JWL: ENd of new

	
	
	
	
	
	
	private void makeRow3(XSSFSheet sheet, int rowNum, RowData rowData, HashMap<String, CellStyle> styleMap) {
		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell cell = null;		
		int colNum = 0;
		CellStyle cellStyleLeft = styleMap.get("cellStyleLeft");	
		CellStyle cellStyleDate = styleMap.get("cellStyleDate");
		CellStyle cellStyleDecimal = styleMap.get("cellStyleDecimal");
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(rowData.getFirstName() + ", " + rowData.getLastName());
		colNum++; //col 1
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(rowData.getInvoiceId());
		colNum++; //col 2
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleDate);
		cell.setCellValue(rowData.getInvoiceDate());
		colNum++; //col 3
		
		colNum++; //col 4
		
		colNum++; //col 5
		
		colNum++; //col 6
		
		colNum++; //col 7
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(rowData.getJobSiteAddress());
		colNum++; //col 8
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(rowData.getAddress1());
		colNum++; //col 1
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleLeft);
		cell.setCellValue(rowData.getInvoiceId());
		colNum++; //col 2
		
		cell = row.createCell(colNum);
		cell.setCellStyle(cellStyleDate);
		cell.setCellValue(rowData.getInvoiceDate());
		colNum++; //col 3
	}

	public XSSFWorkbook makeXLS_jwl() {
		String subtitle = makeSubtitle();
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		sheet.getPrintSetup().setLandscape(true);
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setFitWidth((short)1);
		
		CreationHelper createHelper = workbook.getCreationHelper();
		//Date today = new Date();
		int rowNum = 0;
		int colNum = 0;
		XSSFRow row = null;
		XSSFCell cell = null;
		
	    //Bold and Underline
		Integer fontHeight = 9;
		XSSFFont fontDefaultFont = workbook.createFont();
		fontDefaultFont.setFontHeight(fontHeight);
	    XSSFFont fontStyleBold = workbook.createFont();
	    fontStyleBold.setBold(true);
	    fontStyleBold.setFontHeight(fontHeight);
		XSSFFont fontWhite = workbook.createFont();
		fontWhite.setColor(HSSFColor.WHITE.index);
		fontWhite.setFontHeight(fontHeight);

		
	    short dataFormatDate = createHelper.createDataFormat().getFormat("mm/dd/yyyy");
	    short dataFormatDateTime = createHelper.createDataFormat().getFormat("mm/dd/yyyy hh:mm:ss");
	    short dataFormatDecimal = createHelper.createDataFormat().getFormat("#,##0.00");
	    short dataFormatInteger = createHelper.createDataFormat().getFormat("#,##0");
	    
	    XSSFCellStyle cellStyleAnsi = workbook.createCellStyle();
	    cellStyleAnsi.setAlignment(CellStyle.ALIGN_CENTER);
	    XSSFFont fontAnsi = workbook.createFont();
	    fontAnsi.setBold(true);
	    fontAnsi.setFontHeight(16);
	    cellStyleAnsi.setFont(fontAnsi);
	    

	    XSSFCellStyle cellStyleReportTitle = workbook.createCellStyle();
	    cellStyleReportTitle.setAlignment(CellStyle.ALIGN_CENTER);
	    XSSFFont fontReportTitle = workbook.createFont();
	    fontReportTitle.setBold(true);
	    fontReportTitle.setFontHeight(12);
	    cellStyleReportTitle.setFont(fontAnsi);
	    
	    XSSFCellStyle cellStyleSubtitle = workbook.createCellStyle();
	    cellStyleSubtitle.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleSubtitle.setFont(fontStyleBold);
	    
	    
	    CellStyle cellStyleHeaderLabel = workbook.createCellStyle();
	    cellStyleHeaderLabel.setFont(fontStyleBold);
	    cellStyleHeaderLabel.setAlignment(CellStyle.ALIGN_LEFT);
	    
	    
	    CellStyle cellStyleCreatedLabel = workbook.createCellStyle();
	    XSSFFont fontCreated = workbook.createFont();
	    fontCreated.setBold(true);
	    fontCreated.setFontHeight(fontHeight);
	    cellStyleCreatedLabel.setFont(fontCreated);
	    cellStyleCreatedLabel.setAlignment(CellStyle.ALIGN_LEFT);

	    CellStyle cellStyleRunDate = workbook.createCellStyle();
	    cellStyleRunDate.setDataFormat(dataFormatDateTime);
	    cellStyleRunDate.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleRunDate.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleHeaderDate = workbook.createCellStyle();
	    cellStyleHeaderDate.setDataFormat(dataFormatDate);
	    cellStyleHeaderDate.setAlignment(CellStyle.ALIGN_LEFT);

	    CellStyle cellStyleHeaderDecimal = workbook.createCellStyle();
	    cellStyleHeaderDecimal.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleHeaderDecimal.setDataFormat(dataFormatDecimal);
	    cellStyleHeaderDecimal.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleHeaderInteger = workbook.createCellStyle();
	    cellStyleHeaderInteger.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleHeaderInteger.setDataFormat(dataFormatInteger);
	    cellStyleHeaderInteger.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleHeaderDivision = workbook.createCellStyle();
	    cellStyleHeaderDivision.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleHeaderDivision.setDataFormat(dataFormatInteger);
	    cellStyleHeaderDivision.setFont(fontDefaultFont);
	    
		CellStyle cellStyleColHdr = workbook.createCellStyle();
		cellStyleColHdr.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
	    cellStyleColHdr.setFillPattern(CellStyle.ALIGN_FILL);
	    cellStyleColHdr.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleColHdr.setFont(fontWhite);
	    //cellStyleColHdr.setIndention((short)2);

	    XSSFFont fontSummary = workbook.createFont();
	    fontSummary.setBold(true);
	    fontSummary.setFontHeight(fontHeight);
	    CellStyle cellStyleSummaryRowName = workbook.createCellStyle();	    
	    cellStyleSummaryRowName.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleSummaryRowName.setFont(fontSummary);
	    
	    CellStyle cellStyleSummaryAmt = workbook.createCellStyle();
	    cellStyleSummaryAmt.setDataFormat(dataFormatDecimal);
	    cellStyleSummaryAmt.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleSummaryAmt.setFont(fontStyleBold);
	    
	    CellStyle cellStyleClientName = workbook.createCellStyle();
	    cellStyleClientName.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleClientName.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleJobId = workbook.createCellStyle();
	    cellStyleJobId.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleJobId.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleTicketId = workbook.createCellStyle();
	    cellStyleTicketId.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleTicketId.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleTicketType = workbook.createCellStyle();
	    cellStyleTicketType.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleTicketType.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleDateComplete = workbook.createCellStyle();
	    cellStyleDateComplete.setDataFormat(dataFormatDate);
	    cellStyleDateComplete.setFont(fontDefaultFont);
	    cellStyleDateComplete.setAlignment(CellStyle.ALIGN_LEFT);
	    
	    CellStyle cellStyleInvoiceId = workbook.createCellStyle();
	    cellStyleInvoiceId.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleInvoiceId.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleInvoiceDate = workbook.createCellStyle();
	    cellStyleInvoiceDate.setDataFormat(dataFormatDate);
	    cellStyleInvoiceDate.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleInvoiceDate.setFont(fontDefaultFont);
	    
	    CellStyle cellStyleInvoiceAmount = workbook.createCellStyle();
	    cellStyleInvoiceAmount.setDataFormat(dataFormatDecimal);
	    cellStyleInvoiceAmount.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleInvoiceAmount.setFont(fontDefaultFont);
	    
	    XSSFFont fontBuildingName = workbook.createFont();
	    fontBuildingName.setFontHeight(fontHeight);
	    CellStyle cellStyleBuildingName = workbook.createCellStyle();	    
	    cellStyleBuildingName.setAlignment(CellStyle.ALIGN_LEFT);
//	    cellStyleBuildingName.setIndention((short)20);
	    cellStyleBuildingName.setFont(fontBuildingName);
	    
	    rowNum = 0;
	    colNum = 0;
	    int ppcTotal = 0;
	    int amountPaidTotal = 0;
	    int amountDueTotal = 0;
	    String previousClient = "Previous";
	    Double clientTotal = 0.0D;
	    
//	    if ( this.data.size() == 0 ) {
	    if ( super.getDataRows().size() == 0 ) {
	    	sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,0,10));
	    	row = sheet.createRow(rowNum);
	    	cell = row.createCell(0);
	    	cell.setCellValue("No Data for this report");
	    	rowNum++;
	    }
		for ( Object rowObject : super.getDataRows() ) {
			RowData rowData = (RowData)rowObject;
			
			colNum = 0;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getBillToName());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleJobId);
			cell.setCellValue(rowData.getJobId());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleHeaderDate);
			cell.setCellValue(rowData.getInvoiceDate());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleJobId);
			cell.setCellValue(rowData.getJobId());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleInvoiceAmount);
			cell.setCellValue(rowData.getActPPC());
			ppcTotal += rowData.getActPPC();
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleInvoiceAmount);
			cell.setCellValue(rowData.getAmountPaid());
			amountPaidTotal += rowData.getAmountPaid();
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleInvoiceAmount);
			cell.setCellValue(rowData.getAmountPastDue());
			amountDueTotal += rowData.getAmountPastDue();
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getJobSiteName());
			colNum++;
			
			colNum = 0;
			rowNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getAddress1());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleInvoiceId);
			cell.setCellValue(rowData.getInvoiceId());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleHeaderDate);
			cell.setCellValue(rowData.getInvoiceDate());
			colNum++;//job id
			colNum++;//ppc
			colNum++;//paid amount
			colNum++;//amount due
			colNum++;//site address
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getJobSiteAddress());
			colNum++;
			
			colNum = 0;
			rowNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getAddress2());
			colNum++;
			
			colNum = 0;
			rowNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getBillToName());
			colNum++;
			
			colNum = 0;
			rowNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getPreferredContact());
			colNum++;
			
			colNum = 0;
			rowNum++;
			
			if(rowData.getBillToName().equals(previousClient)){
				for ( Object rowO : super.getDataRows() ) {
					rowData = (RowData)rowO;
					
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleJobId);
					cell.setCellValue(rowData.getJobId());
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleHeaderDate);
					cell.setCellValue(rowData.getInvoiceDate());
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleJobId);
					cell.setCellValue(rowData.getJobId());
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleInvoiceAmount);
					cell.setCellValue(rowData.getActPPC());
					ppcTotal += rowData.getActPPC();
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleInvoiceAmount);
					cell.setCellValue(rowData.getAmountPaid());
					amountPaidTotal += rowData.getAmountPaid();
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleInvoiceAmount);
					cell.setCellValue(rowData.getAmountPastDue());
					amountDueTotal += rowData.getAmountPastDue();
					colNum++;

					cell = row.createCell(colNum);
					cell.setCellStyle(cellStyleBuildingName);
					cell.setCellValue(rowData.getJobSiteName());
					colNum++;

					colNum = 0;
					rowNum++;

					previousClient = rowData.getBillToName();
					
					if(!rowData.getBillToName().equals(previousClient)){
						break;
					}
					
//					rowData = (RowData)rowObject;
				}
			}
			if(!rowData.getBillToName().equals(previousClient)){
				colNum++; //bill to name
				colNum++; //job id
				colNum++; // completed date
				colNum++; // job #
			
				cell = row.createCell(colNum);
				cell.setCellStyle(cellStyleSummaryAmt);
				cell.setCellValue(ppcTotal);
				colNum++;
			
				cell = row.createCell(colNum);
				cell.setCellStyle(cellStyleSummaryAmt);
				cell.setCellValue(amountPaidTotal);
				colNum++;
			
				cell = row.createCell(colNum);
				cell.setCellStyle(cellStyleSummaryAmt);
				cell.setCellValue(amountDueTotal);
				colNum++;
			}
			rowNum++;
			
			previousClient = rowData.getBillToName();
			
		}
		
		return workbook;
	}
	
	private String makeSubtitle() {
//		SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyy-MM");
//		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
//		
		List<String> subtitle = new ArrayList<String>();
//		subtitle.add("IR for Division");
//		//subtitle.add(this.div);
//		subtitle.add("for");
//		//subtitle.add(yyyyMM.format(this.startDate.getTime()));
//		subtitle.add("as of");
//		subtitle.add(yyyyMMdd.format(getRunDate().getTime()));
		
		subtitle.add("Past Due Report");
		
		return StringUtils.join(subtitle, " ");
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
		private String firstName;
		private String lastName;
		private String preferredContact;
		private String jobId;
		private String jobNbr;
		private Integer ticketId;
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
			this.firstName = rs.getString("first_name");
			this.lastName = rs.getString("last_name");
			this.preferredContact = rs.getString("contract_preferred_contact");
			this.jobId = rs.getString("job_id");
			this.ticketId = rs.getInt("ticket_id");
			this.ticketStatus = rs.getString("ticket_status");
			this.ticketType = rs.getString("ticket_type");
			this.invoiceId = rs.getString("invoice_id");
			this.processDate = rs.getDate("process_date");
			this.invoiceDate = rs.getDate("invoice_date");
			this.actPPC = rs.getInt("act_price_per_cleaning");
			this.amountPaid = rs.getInt("amount_paid");
			this.amountDue = rs.getInt("amount_due");
			this.amountPastDue = rs.getInt("amount_past_due");
			this.jobSiteName = rs.getString("job_site_name");
			this.jobSiteAddress = rs.getString("job_site_address_id");
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

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public String getPreferredContact() {
			return preferredContact;
		}

		public String getJobId() {
			return jobId;
		}
		
		public String getJobNbr(){
			return jobNbr;
		}

		public Integer getTicketId() {
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
		
		
	}
	
}




