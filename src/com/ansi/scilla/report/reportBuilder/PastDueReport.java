package com.ansi.scilla.report.reportBuilder;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

public class PastDueReport extends StandardReport {
	private static final long serialVersionUID = 1L;
	
	private final String sql = "select division_nbr, " +	//division_nbr
				"bill_to.name, bill_to.address1, bill_to.address2, bill_to.city, bill_to.state," + //name, address1, address2, city, state
				"contract_contact.first_name, contract_contact.last_name," + //first_name, last_name
				"case when contract_contact.preferred_contact = 'business_phone' then contract_contact.business_phone" + //contract_preferred_contact
					"when contract_contact.preferred_contact = 'mobile_phone' then contract_contact.mobile_phone" +
					"when contract_contact.preferred_contact = 'fax' then contract_contact.fax" +
					"when contract_contact.preferred_contact = 'email' then contract_contact.email" +
					"else contract_contact.business_phone" +
					"end as contract_preferred_contact," +
				"case when billing_contact.preferred_contact = 'business_phone' then billing_contact.business_phone" +
					"when billing_contact.preferred_contact = 'mobile_phone' then billing_contact.mobile_phone" +
					"when billing_contact.preferred_contact = 'fax' then billing_contact.fax" +
					"when billing_contact.preferred_contact = 'email' then billing_contact.email" +
					"else billing_contact.business_phone" +
					"end as contract_preferred_contact," +
				"job.job_id, ticket.ticket_id, ticket_status, ticket_type," + //job_id, ticket_id, ticket_status, ticket_type
				"ticket.invoice_id, ticket.process_date, ticket.invoice_date, ticket.act_price_per_cleaning," + //invoice_id, process_date, invoice_date, act_price_per_cleaning
				"isnull(ticket_payment_totals.amount, '0.00') as amount_paid, ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') as amount_due," + //amount_paid, amount_due
				"case when ticket.invoice_date < @past_due_date then ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00')" + 
				"else '0.00' " +
				"end as amount_past_due, " + //amount_past_due
				"job_site.name," + //job_site.name
				"oldest_invoice_date, oldest_ticket" +
				"from ticket" +
				"join job on ticket.job_id = job.job_id" +
				"join quote on job.quote_id = quote.quote_id" +
				"join division on division.division_id = act_division_id" +
				"join address as bill_to on bill_to.address_id = bill_to_address_id" +
				"join address as job_site on job_site.address_id = job_site_address_id" + //job_site.address_id
				"join contact as contract_contact on contract_contact.contact_id = job.contract_contact_id" +
				"join contact as billing_contact on billing_contact.contact_id = job.contract_contact_id" +
				"left outer join (select ticket_id, sum(amount) as amount, sum(tax_amt) as tax_amt from ticket_payment group by ticket_id) " +
					 "as ticket_payment_totals on ticket_payment_totals.ticket_id = ticket.ticket_id" +
				"left outer join (select ticket.act_division_id as div, bill_to_address_id as bill_to_id, min(invoice_date) as oldest_invoice_date," +
					"min(ticket_id) as oldest_ticket" +
					"from ticket" +
					"join job on job.job_id = ticket.job_id" +
					"join quote on quote.quote_id = job.quote_id" +
					"where ticket_status = 'I'" +
					"group by act_division_id, bill_to_address_id) as bt_oldest_invoice on bt_oldest_invoice.bill_to_id = bill_to_address_id and bt_oldest_invoice.div = ticket.act_division_id" +
				"where oldest_invoice_date < @past_due_date and ticket_status = 'I' and ticket.act_price_per_cleaning - isnull(ticket_payment_totals.amount,'0.00') <> '0.00'" +
				"and division.division_nbr = 12" +
				"order by division_nbr, bill_to.name, invoice_date";
	
	public static final String REPORT_TITLE = "Six Month Rolling Volume Summary";
	
	private PastDueReport(Connection conn) throws Exception {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.LANDSCAPE);
		makeData(conn);
	}
	
	private Calendar startDate;
	
	public Calendar getStartDate(){
		startDate = Calendar.getInstance();
		return startDate;
	}
	
	private void makeData(Connection conn) throws Exception {
		//super.setSubtitle(makeSubtitle());
		super.setHeaderRow(new ColumnHeader[] {
			new ColumnHeader("billToName", "BILL TO NAME", DataFormats.STRING_FORMAT, SummaryType.NONE),//BILL TO NAME
			new ColumnHeader("jobId","JOB #", DataFormats.STRING_FORMAT, SummaryType.NONE),//JOB#
			new ColumnHeader("invoiceDate", "Contracts", DataFormats.DATE_FORMAT, SummaryType.NONE),//completed invoiced dates
			new ColumnHeader("jobId", "JOB", DataFormats.STRING_CENTERED, SummaryType.NONE),//job number
			new ColumnHeader("actPPC", "PPC", DataFormats.NUMBER_CENTERED, SummaryType.NONE),//actPPC
			new ColumnHeader("amountPaid", "PAID AMOUNT", DataFormats.NUMBER_FORMAT, SummaryType.NONE),//Paid Amount
			new ColumnHeader("amountDue", "AMOUNT DUE", DataFormats.NUMBER_FORMAT, SummaryType.NONE),//amountDue
			new ColumnHeader("jobSiteAddress", "SITE ADDRESS", DataFormats.STRING_FORMAT, SummaryType.NONE),//siteAddress
		});		
		
		
		Statement psData = conn.createStatement();
		
		ResultSet rsData = psData.executeQuery(sql);
		
		while ( rsData.next() ) {
			super.addDataRow(new RowData(rsData));
		}
		rsData.close();
		
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created: ", getStartDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
//				new ReportHeaderRow("Aging Date: ", getEndDateMethod, 0, DataFormats.DATE_FORMAT),
//				new ReportHeaderRow("Days Past Due: ", dataSizeMethod, 2, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
	}
	
	public XSSFWorkbook makeXLS() {
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
	    String previousClient = null;
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
			cell.setCellStyle(cellStyleDateComplete);
			cell.setCellValue(rowData.getBillToName());
			colNum++;
			
			

			rowNum++;
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
	
	public static PastDueReport buildReport(Connection conn) throws Exception {
		return new PastDueReport(conn);
	}
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		private String divNbr;
		private String billToName;
		private String address1;
		private String address2;
		private String city;
		private String state;
		private String firstName;
		private String lastName;
		private String preferredContact;
		private String jobId;
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
		
		public RowData(ResultSet rs) throws SQLException {
			this.divNbr = rs.getString("division_nbr");
			this.billToName = rs.getString("bill_to.name");
			this.address1 = rs.getString("address1");
			this.address2 = rs.getString("address2");
			this.city = rs.getString("city");
			this.state = rs.getString("state");
			this.firstName = rs.getString("first_name");
			this.lastName = rs.getString("last_name");
			this.preferredContact = rs.getString("contract_preferred_contact");
			this.jobId = rs.getString("job_id");
			this.ticketId = rs.getString("ticket_id");
			this.ticketStatus = rs.getString("ticket_status");
			this.ticketType = rs.getString("ticket_type");
			this.invoiceId = rs.getString("invoice_id");
			this.processDate = rs.getDate("process_date");
			this.invoiceDate = rs.getDate("invoice_date");
			this.actPPC = rs.getInt("act_price_per_cleaning");
			this.amountPaid = rs.getInt("amount_paid");
			this.amountDue = rs.getInt("amount_due");
			this.amountPastDue = rs.getInt("amount_past_due");
			this.jobSiteName = rs.getString("job_site.name");
			this.jobSiteAddress = rs.getString("job_site.address_id");
		}

		public String getDivNbr() {
			return divNbr;
		}

		public String getBillToName() {
			return billToName;
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


	}
	
}




