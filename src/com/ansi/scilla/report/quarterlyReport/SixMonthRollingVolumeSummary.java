package com.ansi.scilla.report.quarterlyReport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;

public class SixMonthRollingVolumeSummary extends StandardReport {
	
	private static final long serialVersionUID = 1L;
	
	private final String sql = "select * from (select top(43)" +
				"\nCASE" +
				"\nwhen month(QUARTER) in (1,2,3) then DATEFROMPARTS(year(quarter), 3, 31)" +
				"\nwhen month(QUARTER) in (4,5,6) then DATEFROMPARTS(year(quarter), 6, 30)" +
				"\nwhen month(QUARTER) in (7,8,9) then DATEFROMPARTS(year(quarter), 9, 30)" +
				"\nwhen month(QUARTER) in (10,11,12) then DATEFROMPARTS(year(quarter), 12, 31)" +
				"\nEND as quarter_end," +
				"\nsum(job_count) as job_count," +
				"\nsum(job_site_count) as contracts," +
				"\nsum(vol_forecast_oct) as vol_forecast_oct," +
				"\nsum(vol_forecast_nov) as vol_forecast_nov," +
				"\nsum(vol_forecast_dec) as vol_forecast_dec," +
				"\nsum(vol_forecast_jan) as vol_forecast_jan," +
				"\nsum(vol_forecast_feb) as vol_forecast_feb," +
				"\nsum(vol_forecast_mar) as vol_forecast_mar," +
				"\nsum(vol_forecast_apr) as vol_forecast_apr," +
				"\nsum(vol_forecast_may) as vol_forecast_may," +
				"\nsum(vol_forecast_jun) as vol_forecast_jun," +
				"\nsum(vol_forecast_jul) as vol_forecast_jul," +
				"\nsum(vol_forecast_aug) as vol_forecast_aug," +
				"\nsum(vol_forecast_sep) as vol_forecast_sep" +
				"\nfrom quarterly_snapshot" +
				"\ngroup by" +
				"\nCASE" +
				"\nwhen month(QUARTER) in (1,2,3) then DATEFROMPARTS(year(quarter), 3, 31)" +
				"\nwhen month(QUARTER) in (4,5,6) then DATEFROMPARTS(year(quarter), 6, 30)" +
				"\nwhen month(QUARTER) in (7,8,9) then DATEFROMPARTS(year(quarter), 9, 30)" +
				"\nwhen month(QUARTER) in (10,11,12) then DATEFROMPARTS(year(quarter), 12, 31)" +
				"\nEND" +
				"\norder by quarter_end desc) x order by quarter_end asc";
	
	public static final String REPORT_TITLE = "Six Month Rolling Volume Summary";
	
	private SixMonthRollingVolumeSummary(Connection conn) throws Exception {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.LANDSCAPE);
		makeData(conn);
	}
	
//	private SixMonthRollingVolumeSummary(Connection conn,  Integer divisionId, Calendar startDate) throws Exception {
//	this();
//	this.divisionId = divisionId;
//	this.div = makeDivision(conn, divisionId);
//	makeDates(startDate);
//	makeData(conn);	
//}

	private void makeData(Connection conn) throws Exception {
		//super.setSubtitle(makeSubtitle());
		super.setHeaderRow(new ColumnHeader[] {
			new ColumnHeader("quarterEnd", "Quarter End", DataFormats.DATE_FORMAT, SummaryType.NONE),
			new ColumnHeader("jobCount","Job Count", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("contracts", "Contracts", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastOct", "Oct", DataFormats.NUMBER_CENTERED, SummaryType.NONE),
			new ColumnHeader("volForecastNov", "Nov", DataFormats.NUMBER_CENTERED, SummaryType.NONE),
			new ColumnHeader("volForecastDec", "Dec", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("quarter1", "Quarter", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastJan", "Jan", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastFeb", "Feb", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastMar", "Mar", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("quarter2", "Quarter", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastApr", "Apr", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastMay", "May", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastJun", "Jun", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("quarter3", "Quarter", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastJul", "Jul", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastAug", "Aug", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("volForecastSep", "Sep", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("quarter4", "Quarter", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("annual", "Annual", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("jobAvg", "Ave $/Job", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
			new ColumnHeader("contractAvg", "Ave $/Cont", DataFormats.NUMBER_FORMAT, SummaryType.NONE)
		});		
		
		
		Statement psData = conn.createStatement();
		
		ResultSet rsData = psData.executeQuery(sql);
		
		while ( rsData.next() ) {
			super.addDataRow(new RowData(rsData));
		}
		rsData.close();

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
			cell.setCellValue(rowData.getQuarterEnd());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getJobCount());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getContracts());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastOct());
			colNum++;
			
			cell = row.createCell(colNum);
		    cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastNov());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastDec());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getQuarter1());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastJan());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastFeb());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastMar());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getQuarter2());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastApr());
			colNum++;
			
			cell = row.createCell(colNum);
		    cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastMay());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastJun());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getQuarter3());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastJul());
			colNum++;
			
			cell = row.createCell(colNum);
		    cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastAug());
			colNum++;

			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getVolForecastSep());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getQuarter4());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getAnnual());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getJobAvg());
			colNum++;
			
			cell = row.createCell(colNum);
			cell.setCellStyle(cellStyleClientName);
			cell.setCellValue(rowData.getContractAvg());
			colNum++;

			rowNum++;
		}
		
		return workbook;
	}
	
	private String makeSubtitle() {
		SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		
		List<String> subtitle = new ArrayList<String>();
		subtitle.add("IR for Division");
		//subtitle.add(this.div);
		subtitle.add("for");
		//subtitle.add(yyyyMM.format(this.startDate.getTime()));
		subtitle.add("as of");
		subtitle.add(yyyyMMdd.format(getRunDate().getTime()));
		
		return StringUtils.join(subtitle, " ");
	}
	
	public static SixMonthRollingVolumeSummary buildReport(Connection conn) throws Exception {
		return new SixMonthRollingVolumeSummary(conn);
	}
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		private Integer jobCount;
		private Integer contracts;
		private Date quarterEnd;
		private Integer volForecastOct;
		private Integer volForecastNov;
		private Integer volForecastDec;
		private Integer quarter1;
		private Integer volForecastJan;
		private Integer volForecastFeb;
		private Integer volForecastMar;
		private Integer quarter2;
		private Integer volForecastApr;
		private Integer volForecastMay;
		private Integer volForecastJun;
		private Integer quarter3;
		private Integer volForecastJul;
		private Integer volForecastAug;
		private Integer volForecastSep;
		private Integer quarter4;
		private Integer annual;
		private Integer jobAvg;
		private Integer contractAvg;
		
		public RowData(ResultSet rs) throws SQLException {
			java.sql.Date quarterEndTime = rs.getDate("quarter_end");
			if (quarterEndTime != null ) {
				this.quarterEnd = new Date(quarterEndTime.getTime());
			}
			this.jobCount = rs.getInt("job_count");
			this.contracts = rs.getInt("contracts");
			this.volForecastOct = rs.getInt("vol_forecast_oct");
			this.volForecastNov = rs.getInt("vol_forecast_nov");
			this.volForecastDec = rs.getInt("vol_forecast_dec");
			this.volForecastJan = rs.getInt("vol_forecast_jan");
			this.volForecastFeb = rs.getInt("vol_forecast_feb");
			this.volForecastMar = rs.getInt("vol_forecast_mar");
			this.volForecastApr = rs.getInt("vol_forecast_apr");
			this.volForecastMay = rs.getInt("vol_forecast_may");
			this.volForecastJun = rs.getInt("vol_forecast_jun");
			this.volForecastJul = rs.getInt("vol_forecast_jul");
			this.volForecastAug = rs.getInt("vol_forecast_aug");
			this.volForecastSep = rs.getInt("vol_forecast_sep");
		}

		public Integer getJobCount() {
			return jobCount;
		}

		public Integer getContracts() {
			return contracts;
		}

		public Date getQuarterEnd() {
			return quarterEnd;
		}

		public Integer getVolForecastOct() {
			return volForecastOct;
		}

		public Integer getVolForecastNov() {
			return volForecastNov;
		}

		public Integer getVolForecastDec() {
			return volForecastDec;
		}
		
		public Integer getQuarter1() {
			this.quarter1 = getVolForecastOct() + getVolForecastNov() + getVolForecastDec();
			return quarter1;
		}

		public Integer getVolForecastJan() {
			return volForecastJan;
		}

		public Integer getVolForecastFeb() {
			return volForecastFeb;
		}

		public Integer getVolForecastMar() {
			return volForecastMar;
		}
		
		public Integer getQuarter2() {
			this.quarter2 = getVolForecastJan() + getVolForecastFeb() + getVolForecastMar();
			return quarter2;
		}

		public Integer getVolForecastApr() {
			return volForecastApr;
		}

		public Integer getVolForecastMay() {
			return volForecastMay;
		}

		public Integer getVolForecastJun() {
			return volForecastJun;
		}
		
		public Integer getQuarter3() {
			this.quarter3 = getVolForecastApr() + getVolForecastMay() + getVolForecastJun();
			return quarter3;
		}

		public Integer getVolForecastJul() {
			return volForecastJul;
		}

		public Integer getVolForecastAug() {
			return volForecastAug;
		}

		public Integer getVolForecastSep() {
			return volForecastSep;
		}

		public Integer getQuarter4() {
			this.quarter4 = getVolForecastJul() + getVolForecastAug() + getVolForecastSep();
			return quarter4;
		}
		
		public Integer getAnnual() {
			this.annual = getQuarter1() + getQuarter2() + getQuarter3() + getQuarter4();
			return annual;
		}
		
		public Integer getJobAvg() {
			this.jobAvg = getAnnual()/getJobCount();
			return jobAvg;
		}
		
		public Integer getContractAvg() {
			this.contractAvg = getAnnual()/getContracts();
			return contractAvg;
		}


	}
}


