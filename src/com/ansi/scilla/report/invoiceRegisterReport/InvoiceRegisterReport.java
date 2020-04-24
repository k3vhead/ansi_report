package com.ansi.scilla.report.invoiceRegisterReport;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.TicketType;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivMonthYear;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ColumnWidth;

public class InvoiceRegisterReport extends StandardReport implements ReportByDivMonthYear, ReportByDivision {

	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "IR";

	private final String sql = "select bill_to.address_id, "
		+ "\n\tbill_to.name as client_name, "
		+ "\n\tjob.job_id, "
		+ "\n\tticket.ticket_id,  "
		+ "\n\tticket.ticket_type, "
		+ "\n\tticket.process_date as date_complete, "
		+ "\n\tticket.invoice_id, "
		+ "\n\tticket.invoice_date, "
		+ "\n\tticket.act_price_per_cleaning as invoice_amount, "
		+ "\n\tjob_site.name as building_name "
		+ "\nfrom ticket "
//		+ "\ninner join job on job.job_id=ticket.job_id and job.division_id=? "
		+ "\ninner join job on job.job_id=ticket.job_id "
		+ "\ninner join quote on quote.quote_id=job.quote_id "
		+ "\ninner join address bill_to on bill_to.address_id=quote.bill_to_address_id "
		+ "\ninner join address job_site on job_site.address_id = quote.job_site_address_id "
		+ "\nwhere ticket.act_division_id=? and ticket.invoice_date is not null and year(ticket.invoice_date)=? and month(ticket.invoice_date)=? "
		+ "\n ";
	
	public static final String REPORT_TITLE = "Invoice Register";
//	private final String REPORT_NOTES = "notes go here";
	
	private Integer divisionId;
	private Calendar startDate;
	private Calendar endDate;
//	private List<RowData> data;
	private Calendar runDate;
	private String div;
	private BigDecimal totalInvoiced;
	private Integer ticketCount;
	
	private InvoiceRegisterReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.LANDSCAPE);
	}
	/**
	 * Default date range is current month-to-date
	 * @param conn
	 * @param divisionId
	 * @throws Exception
	 */
	private InvoiceRegisterReport(Connection conn,  Integer divisionId, Calendar startDate) throws Exception {
		this();
		this.divisionId = divisionId;
		this.div = makeDivision(conn, divisionId);
		makeDates(startDate);
		makeData(conn);	
	}

	public Calendar getStartDate() {
		return startDate;
	}
//	public List<RowData> getData() {
//		return data;
//	}
	public Integer getDivisionId() {
		return divisionId;
	}
	public String getDiv() {
		return div;
	}

	public Calendar getEndDate() {
		return endDate;
	}
	public Calendar getRunDate() {
		return runDate;
	}
	public BigDecimal getTotalInvoiced() {
		return totalInvoiced;
	}
	public Integer getTicketCount() {
		return ticketCount;
	}
	private String makeDivision(Connection conn, Integer divisionId) throws Exception {
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(conn);
		return division.getDivisionNbr() + "-" + division.getDivisionCode();
	}

	private void makeDates(Calendar startDate) {
		this.runDate = Calendar.getInstance(new AnsiTime());
		
		Calendar workDate = (Calendar)startDate.clone();
		workDate.set(Calendar.DAY_OF_MONTH, 1);
		workDate.set(Calendar.HOUR_OF_DAY, 0);
		workDate.set(Calendar.MINUTE, 0);
		workDate.set(Calendar.SECOND, 0);
		workDate.set(Calendar.MILLISECOND, 0);		
		this.startDate = (Calendar)workDate.clone();
		
		workDate.set(Calendar.DAY_OF_MONTH, workDate.getActualMaximum(Calendar.DAY_OF_MONTH));
		this.endDate = (Calendar)workDate.clone();
		
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	

	private void makeData(Connection conn) throws Exception {
		super.setSubtitle(makeSubtitle());
		super.setHeaderRow(new ColumnHeader[] {
			new ColumnHeader("clientName","Client Name", 3, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 40),
			new ColumnHeader("jobId", "Job ID", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("ticketId", "Ticket ID", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
			new ColumnHeader("ticketType", "Type", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
			new ColumnHeader("dateComplete", "Date Complete", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
			new ColumnHeader("invoiceId", "Invoice #", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("invoiceDate","Invoice Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
			new ColumnHeader("invoiceAmount", "Invoice Amount", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM, "clientName"),
			new ColumnHeader("buildingName","Building Name", 2, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 40)
		});
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		Method getTotalInvoicedMethod = this.getClass().getMethod("getTotalInvoiced", (Class<?>[])null);
		Method getTicketCountMethod = this.getClass().getMethod("getTicketCount", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
			new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
			new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
			new ReportHeaderRow("To:", getEndDateMethod, 3, DataFormats.DATE_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		

		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
			new ReportHeaderRow("Division:", getDivMethod, 0, DataFormats.STRING_FORMAT),
			new ReportHeaderRow("Total Invoiced:", getTotalInvoicedMethod, 0, DataFormats.DECIMAL_FORMAT),
			new ReportHeaderRow("Tickets:", getTicketCountMethod, 0, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderRight(headerRight);
		
		
		super.setColumnWidths(new Integer[] {
				ColumnWidth.HEADER_COL1.width(),
				ColumnWidth.DATETIME.width(),
				ColumnWidth.CONTACT_NAME.width() - ColumnWidth.DATETIME.width() - ColumnWidth.HEADER_COL1.width(),
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
				ColumnWidth.ADDRESS_NAME.width() - ColumnWidth.HEADER_COL_RIGHT.width(),
				ColumnWidth.HEADER_COL_RIGHT.width()
		});
		
		PreparedStatement psData = conn.prepareStatement(sql + "\norder by bill_to.name, ticket.invoice_date");
		int n=1;
		psData.setInt(n, divisionId);
		n++;
		psData.setInt(n, this.startDate.get(Calendar.YEAR));
		n++;
		psData.setInt(n, this.startDate.get(Calendar.MONTH)+1);  // because JANUARY is 0
		
		ResultSet rsData = psData.executeQuery();
		
//		this.data = new ArrayList<RowData>();
		
		while ( rsData.next() ) {
//			this.data.add(new RowData(rsData));
			super.addDataRow(new RowData(rsData));
		}
		rsData.close();

		
		PreparedStatement psMeta = conn.prepareStatement( 
				"select count(*) as ticket_count, sum(invoice_amount) as total_invoiced from ( "
				+ sql
				+ ") t");
		n=1;
		psMeta.setInt(n, divisionId);
		n++;
		psMeta.setInt(n, this.startDate.get(Calendar.YEAR));
		n++;
		psMeta.setInt(n, this.startDate.get(Calendar.MONTH)+1);  // because JANUARY is 0		
		ResultSet rsMeta = psMeta.executeQuery();
		
		if ( rsMeta.next() ) {
			this.ticketCount = rsMeta.getInt("ticket_count");
			this.totalInvoiced = rsMeta.getBigDecimal("total_invoiced");
		} 
		rsMeta.close();
		
		if ( this.ticketCount == null ) {
			this.ticketCount = 0;
		}
		if ( this.totalInvoiced == null ) {
			this.totalInvoiced = BigDecimal.ZERO;
		}
	}


	
//	private void makeReport(String div, Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {
//
//		super.setTitle(REPORT_TITLE);	
//		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
//		
//		super.setHeaderRow(new ColumnHeader[] {
//				new ColumnHeader("processDate", "Date Completed", DataFormats.DATE_FORMAT, SummaryType.NONE),
//				new ColumnHeader("jobId", "Job Id", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
//				new ColumnHeader("ticketId","Ticket #", DataFormats.NUMBER_FORMAT, SummaryType.COUNT),
//				new ColumnHeader("ticketStatus","Status", DataFormats.STRING_FORMAT, SummaryType.NONE),
//				new ColumnHeader("actPricePerCleaning","PPC", DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
//				new ColumnHeader("invoiceDate","Invoiced", DataFormats.DATE_FORMAT, SummaryType.NONE),
//				new ColumnHeader("jobNbr","Job #", DataFormats.NUMBER_FORMAT, SummaryType.COUNT_DISTINCT),
//				new ColumnHeader("name","Site Name", DataFormats.STRING_FORMAT, SummaryType.NONE),
//				new ColumnHeader("address1","Site Address", DataFormats.STRING_FORMAT, SummaryType.NONE),
//		});
//		
//		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
//		super.setDataRows(oData);
//		
//		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
//		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
//		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
//		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
//		
//		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
//				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
//				new ReportHeaderRow("Division:", getDivMethod, 1, DataFormats.STRING_FORMAT),
//				new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
//				new ReportHeaderRow("To:", getEndDateMethod, 3, DataFormats.DATE_FORMAT)
//		});
//		super.makeHeaderLeft(headerLeft);
//		
//		
//		Method dataSizeMethod = this.getClass().getMethod("makeDataSize", (Class<?>[])null);
//		Method getCompletedPPC = this.getClass().getMethod("getCompletedPPC", (Class<?>[])null);
//		Method getCompletedINV = this.getClass().getMethod("getCompletedINV", (Class<?>[])null);
//		
//		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
//				new ReportHeaderRow("Completed PPC:", getCompletedPPC, 0, DataFormats.CURRENCY_FORMAT),
//				new ReportHeaderRow("Completed INV:", getCompletedINV, 1, DataFormats.CURRENCY_FORMAT),
//				new ReportHeaderRow("Tickets:", dataSizeMethod, 2, DataFormats.INTEGER_FORMAT)
//		});
//		super.makeHeaderRight(headerRight);
//	}
	
	private XSSFWorkbook makeXLS() {
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
	    row = sheet.createRow(rowNum);
	    row.setHeight((short)400);
	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,1,2));
	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,3,7));
	    cell = row.createCell(colNum);	
//	    cell.setCellStyle(cellStyleHeaderLabel);
	    cell.setCellStyle(cellStyleCreatedLabel);	    
	    cell.setCellValue("Created:");
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleRunDate);
	    cell.setCellValue(this.runDate);
	    colNum++;
	    colNum++;  // make up for merged rows
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleAnsi);
	    cell.setCellValue("American National Skyline, Inc.");
	    colNum = 9;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleCreatedLabel);
	    cell.setCellValue("Division:");
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleHeaderDivision);
	    cell.setCellValue(this.div);
	    
	    
	    rowNum++;
	    colNum = 0;
	    row = sheet.createRow(rowNum);
	    row.setHeight((short)300);
	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,3,7));
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleCreatedLabel);
	    cell.setCellValue("From:");
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleHeaderDate);
	    cell.setCellValue(this.startDate);
	    colNum++;
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleReportTitle);
	    cell.setCellValue("Invoice Register");
	    colNum = 9;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleCreatedLabel);
	    cell.setCellValue("Total Invoiced:");
	    colNum++;
	    cell = row.createCell(colNum);	    
	    cell.setCellStyle(cellStyleHeaderDecimal);
	    cell.setCellValue(this.totalInvoiced.doubleValue());
	    
	    rowNum++;
	    colNum = 0;
	    row = sheet.createRow(rowNum);
	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,3,7));
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleCreatedLabel);
	    cell.setCellValue("To:");
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleHeaderDate);
	    cell.setCellValue(this.endDate);
	    colNum++;
	    colNum++;
	    XSSFCell mycell = row.createCell(colNum);
	    mycell.setCellStyle(cellStyleSubtitle);
	    mycell.setCellValue(subtitle);
	    colNum=9;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleCreatedLabel);
	    cell.setCellValue("Tickets:");
	    colNum++;
	    cell = row.createCell(colNum);	   
	    cell.setCellStyle(cellStyleHeaderInteger);
	    cell.setCellValue(this.ticketCount);
	    
	    rowNum++;
	    row = sheet.createRow(rowNum);
	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,0,1));
	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,9,10));
	    colNum = 0;
	    cell = row.createCell(colNum);
    	cell.setCellStyle(cellStyleColHdr);
    	cell.setCellValue("Client Name");
    	colNum++;	//colNum 1
    	colNum++; 	//colNum 2
	    for ( String colHdr : new String[] {
	    		"Job ID",			"Ticket ID",	"Type",
	    		"Date Complete",	"Invoice #",	"Invoice Date",
	    		"Invoice Amount",	"Building Name"}) {
	    	cell = row.createCell(colNum);
	    	cell.setCellStyle(cellStyleColHdr);
	    	cell.setCellValue(colHdr);
	    	colNum++; //colNum 3-10
	    }
	    rowNum++;
	    
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
			if ( ! StringUtils.isBlank(previousClient) && ! rowData.getClientName().equals(previousClient)) {
				row = sheet.createRow(rowNum);
			    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,0,1));
			    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,9,10));
				cell = row.createCell(colNum);
				cell.setCellStyle(cellStyleSummaryRowName);
				cell.setCellValue(previousClient + " sum");
				
				cell = row.createCell(8);
				cell.setCellStyle(cellStyleSummaryAmt);;
				cell.setCellValue(clientTotal);
				
				clientTotal = 0.0D;
				rowNum++;
			}
			previousClient = rowData.getClientName();
			clientTotal = clientTotal + rowData.getInvoiceAmount().doubleValue();
			
			
			row = sheet.createRow(rowNum);
		    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,0,1));
		    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,9,10));
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getClientName());
			colNum++;
			colNum++;    // add 2 because we're merging cells to get the header to line up
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleJobId);
			cell.setCellValue(rowData.getJobId());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleTicketId);
			cell.setCellValue(rowData.getTicketId());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleTicketType);
			cell.setCellValue(rowData.getTicketType());
			colNum++;
			
			cell = row.createCell(colNum);
		    cell.setCellStyle(cellStyleDateComplete);
			if ( rowData.getDateComplete() != null ) {
				cell.setCellValue(rowData.getDateComplete());
			}
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleInvoiceId);
			cell.setCellValue(rowData.getInvoiceId());
			colNum++;

			cell = row.createCell(colNum);
		    cell.setCellStyle(cellStyleInvoiceDate);
			cell.setCellValue(rowData.getInvoiceDate());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleInvoiceAmount);
			cell.setCellValue(rowData.getInvoiceAmount().doubleValue());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleBuildingName);
			cell.setCellValue(rowData.getBuildingName());
			colNum++;

			rowNum++;
		}
		
		// show the last group total
		if ( previousClient != null ) {
			row = sheet.createRow(rowNum);
		    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,0,1));
		    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,9,10));
			cell = row.createCell(0);
			cell.setCellStyle(cellStyleSummaryRowName);
			cell.setCellValue(previousClient + " sum");
			
			cell = row.createCell(8);
			cell.setCellStyle(cellStyleSummaryAmt);;
			cell.setCellValue(clientTotal);
			
			rowNum++;
		}
		
		
		// show the grand total
		row = sheet.createRow(rowNum);
		cell = row.createCell(0);
		cell.setCellStyle(cellStyleSummaryRowName);
		cell.setCellValue("Grand Total:");
		cell = row.createCell(8);
		cell.setCellStyle(cellStyleSummaryAmt);
		cell.setCellValue(this.totalInvoiced.doubleValue());
		
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 3700);
		sheet.setColumnWidth(5, 2500);
		sheet.setColumnWidth(7, 2500);
		sheet.setColumnWidth(9, 3400);
		sheet.setColumnWidth(10, 4200);
		return workbook;
	}
	
	private String makeSubtitle() {
		SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		
		List<String> subtitle = new ArrayList<String>();
		subtitle.add("IR for Division");
		subtitle.add(this.div);
		subtitle.add("for");
		subtitle.add(yyyyMM.format(this.startDate.getTime()));
		subtitle.add("as of");
		subtitle.add(yyyyMMdd.format(getRunDate().getTime()));
		
		return StringUtils.join(subtitle, " ");
	}

	public static XSSFWorkbook makeReport(Connection conn, Integer divisionId, Calendar startDate) throws Exception {
		InvoiceRegisterReport report = new InvoiceRegisterReport(conn, divisionId, startDate);
		return report.makeXLS();
	}
	
	public static InvoiceRegisterReport buildReport(Connection conn, Integer divisionId, Integer month, Integer year) throws Exception {
		GregorianCalendar startDate = new GregorianCalendar(year, month-1, 1);
		return new InvoiceRegisterReport(conn, divisionId, startDate);
	}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		private Integer addressId;
		private String clientName;
		private Integer jobId;
		private Integer ticketId;
		private String ticketType;
		private Date dateComplete;
		private Integer invoiceId;
		private Date invoiceDate;
		private BigDecimal invoiceAmount;
		private String buildingName;
		
		public RowData(ResultSet rs) throws SQLException {
			java.sql.Date dateComplete = rs.getDate("date_complete");
			this.addressId = rs.getInt("address_id");
			this.clientName = rs.getString("client_name");
			this.jobId = rs.getInt("job_id");
			this.ticketId = rs.getInt("ticket_id");
			if (dateComplete != null ) {
				this.dateComplete = new Date(dateComplete.getTime());
			}
			this.invoiceId = rs.getInt("invoice_id");
			String typeText = StringUtils.trimToEmpty(rs.getString("ticket_type"));
			TicketType ticketType = TicketType.lookup(typeText); 
			this.ticketType = ticketType == null ? typeText : ticketType.display();
			this.invoiceDate = new Date(rs.getDate("invoice_date").getTime());
			this.invoiceAmount = rs.getBigDecimal("invoice_amount");
			this.buildingName = rs.getString("building_name");
		}

		public Integer getAddressId() {
			return addressId;
		}
		public String getClientName() {
			return clientName;
		}
		public Integer getJobId() {
			return jobId;
		}
		public Integer getTicketId() {
			return ticketId;
		}
		public String getTicketType() {
			return this.ticketType;
		}
		public Date getDateComplete() {
			return dateComplete;
		}
		public Integer getInvoiceId() {
			return invoiceId;
		}
		public Date getInvoiceDate() {
			return invoiceDate;
		}
		public BigDecimal getInvoiceAmount() {
			return invoiceAmount;
		}
		public String getBuildingName() {
			return buildingName;
		}


	}
}
