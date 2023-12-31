package com.ansi.scilla.report.sixMonthRollingVolume;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.JobFrequency;
import com.ansi.scilla.common.jobticket.JobStatus;
import com.ansi.scilla.common.jobticket.TicketDateGenerator;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.jobticket.TicketType;
import com.ansi.scilla.report.htmlTable.HTMLCell;
import com.ansi.scilla.report.htmlTable.HTMLRow;
import com.ansi.scilla.report.htmlTable.HTMLTable;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLReportFormatter;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.AnsiPCell;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportHeader;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivMonthYear;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;
import com.ansi.scilla.report.reportBuilder.reportType.CustomReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSReportFormatter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Six Month Rolling Volume Report. Static methods make&lt;format&gt; accept a number of 6-month
 * reports to get created. Be careful, because each report requires it's own connection, and
 * system resources can get consumed quickly. All reports are created simultaneously and then
 * combined. 
 *  
 * @author dclewis
 *
 */
public class SixMonthRollingVolumeReport extends CustomReport implements Comparable<SixMonthRollingVolumeReport>, ReportByDivMonthYear, ReportByDivision {

	private static final long serialVersionUID = 1L;
	
	public static final String FILENAME = "SMRV";
	
	
	private Double marginTop = XLSBuilder.marginTopDefault;
	private Double marginBottom = XLSBuilder.marginBottomDefault;
	private Double marginLeft = XLSBuilder.marginLeftDefault;
	private Double marginRight = XLSBuilder.marginRightDefault;
	
	private final SimpleDateFormat columnDateHeaderFormat = new SimpleDateFormat("MM/yyyy");
	private final SimpleDateFormat detailCellDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private final DecimalFormat detailCellNumberFormat = new DecimalFormat("#,##0.00");
	
	private final Font fontStandardWhite;
	private final Font fontStandardBlack;

	final static String sql = "select job_site.name as job_site_name "
			+ "\n\t, job_site.zip "
			+ "\n\t, job_site.address1 "
			+ "\n\t, job.job_id "
			+ "\n\t, job.job_nbr "
			+ "\n\t, job.job_frequency "
			+ "\n\t, job.price_per_cleaning "
			+ "\n\t, job.start_date "
			+ "\n\t, max(t.process_date) as last_run "
//			+ "\n\t, ppcm01.price_per_cleaning as ppcm01	 "
//			+ "\n\t, ppcm02.price_per_cleaning as ppcm02 "
//			+ "\n\t, ppcm03.price_per_cleaning as ppcm03 "
//			+ "\n\t, (isnull(ppcm01.price_per_cleaning,0) + isnull(ppcm02.price_per_cleaning,0) + isnull(ppcm03.price_per_cleaning,0)) as ppcq01 "
//			+ "\n\t, ppcm04.price_per_cleaning as ppcm04 "
//			+ "\n\t, ppcm05.price_per_cleaning as ppcm05 "
//			+ "\n\t, ppcm06.price_per_cleaning as ppcm06 "
//			+ "\n\t, (isnull(ppcm04.price_per_cleaning,0) + isnull(ppcm05.price_per_cleaning,0) + isnull(ppcm06.price_per_cleaning,0)) as ppcq02 "
//			+ "\n\t, (isnull(ppcm01.price_per_cleaning,0) + isnull(ppcm02.price_per_cleaning,0) + isnull(ppcm03.price_per_cleaning,0) + isnull(ppcm04.price_per_cleaning,0) + isnull(ppcm05.price_per_cleaning,0) + isnull(ppcm06.price_per_cleaning,0)) as ppch01 "
			+ "\nfrom job "
			+ "\ninner join quote on quote.quote_id = job.quote_id "
			+ "\ninner join address as job_site on job_site.address_id = quote.job_site_address_id "
			+ "\ninner join division on division.division_id = job.division_id "
			+ "\nleft outer join ticket t on ticket_status in (?,?,?) and ticket_type in (?,?) and t.job_id = job.job_id "
//			+ "\nleft outer join view_monthly_volume ppcm01 on ppcm01.job_id=job.job_id and ppcm01.job_site_address_id=quote.job_site_address_id and ppcm01.ppc_year=? and ppcm01.ppc_month=?  "
//			+ "\nleft outer join view_monthly_volume ppcm02 on ppcm02.job_id=job.job_id and ppcm02.job_site_address_id=quote.job_site_address_id and ppcm02.ppc_year=? and ppcm02.ppc_month=?  "
//			+ "\nleft outer join view_monthly_volume ppcm03 on ppcm03.job_id=job.job_id and ppcm03.job_site_address_id=quote.job_site_address_id and ppcm03.ppc_year=? and ppcm03.ppc_month=?  "
//			+ "\nleft outer join view_monthly_volume ppcm04 on ppcm04.job_id=job.job_id and ppcm04.job_site_address_id=quote.job_site_address_id and ppcm04.ppc_year=? and ppcm04.ppc_month=?  "
//			+ "\nleft outer join view_monthly_volume ppcm05 on ppcm05.job_id=job.job_id and ppcm05.job_site_address_id=quote.job_site_address_id and ppcm05.ppc_year=? and ppcm05.ppc_month=?  "
//			+ "\nleft outer join view_monthly_volume ppcm06 on ppcm06.job_id=job.job_id and ppcm06.job_site_address_id=quote.job_site_address_id and ppcm06.ppc_year=? and ppcm06.ppc_month=?  "
			+ "\nwhere division.division_id = ? "
			+ "\n\tand job.job_status = ?	 "
//			+ "\n\tand (  ppcm01.price_per_cleaning is not null or ppcm02.price_per_cleaning is not null or ppcm03.price_per_cleaning is not null "
//			+ "\n\t\tor ppcm04.price_per_cleaning is not null or ppcm05.price_per_cleaning is not null or ppcm06.price_per_cleaning is not null) "
			+ "\ngroup by job_site.name "
			+ "\n\t, job_site.zip "
			+ "\n\t, job_site.address1 "
			+ "\n\t, job.job_id "
			+ "\n\t, job.job_nbr "
			+ "\n\t, job.job_frequency "
			+ "\n\t, job.price_per_cleaning "
			+ "\n\t, job.start_date "
//			+ "\n\t, ppcm01.price_per_cleaning "
//			+ "\n\t, ppcm02.price_per_cleaning "
//			+ "\n\t, ppcm03.price_per_cleaning "
//			+ "\n\t, ppcm04.price_per_cleaning "
//			+ "\n\t, ppcm05.price_per_cleaning "
//			+ "\n\t, ppcm06.price_per_cleaning "
			+ "\norder by job_site_name, job_site.address1, job_nbr";	
	public static final  String REPORT_TITLE = "Six Month Rolling Volume";
//	private final String REPORT_NOTES = "notes go here";
	private final String[] colHeaders = new String[] {"Building Name", "Zipcode", "Street 1", "Job #", "Last Run", "Job ID", "Freq"};
	
	private String div;
	private Calendar startDate;
	private List<Object> dataRows;
	private int jobCount = 0;   // total row count
	private int contractCount = 0;   // distinct job sites
	private Double[] monthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};

	public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");
	private Logger logger;

	
	
	
	private SixMonthRollingVolumeReport() {
		super();
		this.logger = LogManager.getLogger(this.getClass());
		this.setTitle(REPORT_TITLE);
		this.dataRows = new ArrayList<Object>();
		
		try {
			String calibriTTF = PDFReportFormatter.class.getClassLoader().getResource("resources/calibri.ttf").getFile();
			BaseFont calibri = BaseFont.createFont(calibriTTF, BaseFont.WINANSI, true);
			fontStandardBlack = new Font(calibri, 8F);
			fontStandardWhite = new Font(calibri, 9F);
			fontStandardWhite.setColor(BaseColor.WHITE);
		} catch ( Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private SixMonthRollingVolumeReport(PreparedStatement ps, Division division, Calendar startDate) throws Exception {
		this();
		Calendar startTime = Calendar.getInstance(new AnsiTime());
		this.startDate = startDate;
		this.div =  division.getDivisionNbr() + "-" + division.getDivisionCode();
		
		makeData(ps, division.getDivisionId(), startDate);
		Calendar endTime = Calendar.getInstance(new AnsiTime());

		List<String> x = new ArrayList<String>();
		x.add(sdf.format(startDate.getTime()));
		x.add(String.valueOf(getDataRows().size()));
		x.add(sdf2.format(startTime.getTime()));
		x.add(sdf2.format(endTime.getTime()));
		x.add(String.valueOf((endTime.getTimeInMillis()-startTime.getTimeInMillis())));
		logger.log(Level.DEBUG, StringUtils.join(x, "\t"));
		
	}


	@Override
	public Integer getReportWidth() {
		return colHeaders.length;
	}

	public String getDiv() {
		return div;
	}

	public void setDiv(String div) {
		this.div = div;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}
	public List<Object> getDataRows() {
		return dataRows;
	}
	public void addDataRow(Object dataRow) {
		this.dataRows.add(dataRow);
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	private void makeData(PreparedStatement ps, Integer divisionId, Calendar startDate) throws Exception {
		Integer queryMonth = startDate.get(Calendar.MONTH) + 1; // add 1 because January is 0;
		Integer queryYear = startDate.get(Calendar.YEAR);
		Double[] jobMonthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};
		Integer[] jobQueryMonth = new Integer[] {0,0,0,0,0,0};
		Integer[] jobQueryYear = new Integer[] {0,0,0,0,0,0};
		Calendar jobStartDate = Calendar.getInstance(new AnsiTime());
		Calendar endDate = Calendar.getInstance(new AnsiTime());
		endDate.setTime(startDate.getTime());
		endDate.add(Calendar.MONTH, 6);
//		logger.log(Level.DEBUG, "\t" + "startDate:" + startDate.getTime() + "\t" + "endDate:" + endDate.getTime());
		
		int n = 1;		
		ps.setString(n, TicketStatus.COMPLETED.code());
		n++;
		ps.setString(n, TicketStatus.INVOICED.code());
		n++;
		ps.setString(n, TicketStatus.PAID.code());
		n++;
		
		ps.setString(n, TicketType.JOB.code());
		n++;
		ps.setString(n, TicketType.RUN.code());
		n++;
		
		for ( int i = 0; i < 6; i++ ) {
//			ps.setInt(n, queryYear);
//			n++;
//			ps.setInt(n, queryMonth);
//			n++;
			jobQueryMonth[i] = queryMonth;
			jobQueryYear[i] = queryYear;
//			logger.log(Level.DEBUG, "\t"+ i + "\t" + "jobQueryYear:" + jobQueryYear[i] + "\t" + "jobQueryMonth:" + jobQueryMonth[i]);
			queryMonth++;
			if ( queryMonth > 12 ) {
				queryMonth = 1;
				queryYear++;
			}
		}
		ps.setInt(n, divisionId);
		n++;
		ps.setString(n, JobStatus.ACTIVE.code());
		n++;
		
		ResultSet rs = ps.executeQuery();
		
		List<String> jobSiteNameList = new ArrayList<String>();
		while ( rs.next() ) {
			DataRow dataRow = new DataRow(rs);
			addDataRow(dataRow);
//			logger.log(Level.DEBUG, "\t" + "jobId:" + dataRow.jobId + "\t" + "jobFrequency:" + dataRow.jobFrequency);
			this.jobCount++;
			if (! jobSiteNameList.contains(dataRow.jobSiteName)) {
				jobSiteNameList.add(dataRow.jobSiteName);
			}
			jobStartDate.setTime(dataRow.jobStartDate);
			
			List<Calendar> ticketDates = TicketDateGenerator.generateTicketDates(dataRow.jobFrequency, jobStartDate, endDate);
			Integer ticketMonth = 0; 
			Integer ticketYear = 0;
			for ( Calendar ticketDate : ticketDates ) {
//				logger.log(Level.DEBUG, "\tticketDate:"+ ticketDate.getTime());
				ticketMonth = ticketDate.get(Calendar.MONTH) + 1; // add 1 because January is 0;
				ticketYear = ticketDate.get(Calendar.YEAR);
//				logger.log(Level.DEBUG, "\t" + "\t" + "ticketYear:" + ticketYear + "\t" + "ticketMonth:" + ticketMonth);
//				logger.log(Level.DEBUG, "\t" + "\t" + "PPC:" + dataRow.jobPpc );
				for ( int i = 0; i < 6; i++ ) {
//					logger.log(Level.DEBUG, "\t"+ i + "\t" + "jobQueryYear:" + jobQueryYear[i] + "\t" + "jobQueryMonth:" + jobQueryMonth[i]);
					if ( ticketMonth.equals(jobQueryMonth[i]) && ticketYear.equals(jobQueryYear[i]) ) {
						this.monthlyTotal[i] = dataRow.jobPpc == null ? this.monthlyTotal[i] : this.monthlyTotal[i] + dataRow.jobPpc.doubleValue();
						dataRow.jobMonthlyTotal[i] = dataRow.jobPpc == null ? dataRow.jobMonthlyTotal[i] : dataRow.jobMonthlyTotal[i] + dataRow.jobPpc.doubleValue();
//						logger.log(Level.DEBUG, "\tmonthlyTotal[i]:"+ this.monthlyTotal[i]);
					}
				}
				dataRow.ppcm01 = new BigDecimal( dataRow.jobMonthlyTotal[0]);
				dataRow.ppcm02 = new BigDecimal( dataRow.jobMonthlyTotal[1]);
				dataRow.ppcm03 = new BigDecimal( dataRow.jobMonthlyTotal[2]);
				dataRow.ppcq01 = BigDecimal.ZERO;
				dataRow.ppcq01.add(dataRow.ppcm01);
				dataRow.ppcq01.add(dataRow.ppcm02);
				dataRow.ppcq01.add(dataRow.ppcm03);
				dataRow.ppcm04 = new BigDecimal( dataRow.jobMonthlyTotal[3]);
				dataRow.ppcm05 = new BigDecimal( dataRow.jobMonthlyTotal[4]);
				dataRow.ppcm06 = new BigDecimal( dataRow.jobMonthlyTotal[5]);
				dataRow.ppcq02 = BigDecimal.ZERO;
				dataRow.ppcq02.add(dataRow.ppcm04);
				dataRow.ppcq02.add(dataRow.ppcm05);
				dataRow.ppcq02.add(dataRow.ppcm06);
				dataRow.ppch01 = BigDecimal.ZERO;
				dataRow.ppch01.add(dataRow.ppcq01);
				dataRow.ppch01.add(dataRow.ppcq02);

			}
		}
		this.contractCount = jobSiteNameList.size();
		rs.close();
	}

	private List<SixMonthRollingVolumeReport> makeReportList(List<Connection> connectionList, Integer divisionId, Calendar startDate) throws Exception {
		Calendar everythingstart = Calendar.getInstance(new AnsiTime());
		logger.log(Level.DEBUG, "Starting everything " + sdf2.format(everythingstart.getTime()));
		List<SixMonthRollingVolumeReport> reportList = new ArrayList<SixMonthRollingVolumeReport>();
		
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(connectionList.get(0));

		List<Thread> threadList = new ArrayList<Thread>();
		List<ReportRunnable> volumeList = new ArrayList<ReportRunnable>();
		
		int i = 0;
		for ( Connection conn : connectionList ) {
			PreparedStatement ps = conn.prepareStatement(sql);
			Calendar reportDate = (Calendar)startDate.clone();			
			reportDate.add(Calendar.MONTH, 6*i);
			logger.log(Level.DEBUG, sdf.format(reportDate.getTime()) + " Creating Report Maker");
			volumeList.add(new ReportRunnable(ps, division, reportDate));
			i++;
		}

		Calendar cal2 = Calendar.getInstance(new AnsiTime());
		logger.log(Level.DEBUG, "Starting threads " + sdf2.format(cal2.getTime()));


		for ( ReportRunnable volume : volumeList ) {
			Thread thread = new Thread(volume);
			threadList.add(thread);
		}

		for ( Thread thread : threadList) {
			thread.start();
		}
		
		Calendar cal3 = Calendar.getInstance(new AnsiTime());
		logger.log(Level.DEBUG, "Joining threads " + sdf2.format(cal3.getTime()));


		while ( true ) {
			try {
				for ( Thread thread : threadList ) {
					thread.join();
				}
				break;
			} catch ( InterruptedException e) {
				System.err.println("Interrupted");
			}
		}

		Calendar cal4 = Calendar.getInstance(new AnsiTime());
		logger.log(Level.DEBUG, "Done threads " + sdf2.format(cal4.getTime()));


		for ( ReportRunnable volume : volumeList ) {
			reportList.add(volume.report);
		}
		
		Calendar cal5 = Calendar.getInstance(new AnsiTime());
		logger.log(Level.DEBUG, "Sorting reports " + sdf2.format(cal5.getTime()));


		Collections.sort(reportList);
		return reportList;
	}

	private static List<SixMonthRollingVolumeReport> makeReport(List<Connection> connectionList, Integer divisionId, Calendar startDate) throws Exception {
		Logger logger = LogManager.getLogger(SixMonthRollingVolumeReport.class);
		SixMonthRollingVolumeReport reportMaker = new SixMonthRollingVolumeReport();
		List<SixMonthRollingVolumeReport> reportList = reportMaker.makeReportList(connectionList, divisionId, startDate);
		
		for ( SixMonthRollingVolumeReport report : reportList ) {
			logger.log(Level.DEBUG, sdf.format(report.getStartDate().getTime()) + "\t" + report.getDataRows().size());
		}
		return reportList;
		
	}
		
	
	

	
		
	private void populateXlsSheet(XSSFSheet sheet, XLSReportFormatter reportFormatter) {
		Calendar printDate = Calendar.getInstance(new AnsiTime());
		DateFormatter headerDateFormatter = (DateFormatter)DataFormats.DATE_TIME_FORMAT.formatter();
		String headerDate = headerDateFormatter.format(printDate);
		int lastColumnNumber = 12;
		
		SimpleDateFormat row0DateFormatter = new SimpleDateFormat("MMMM");
		String startMonth = row0DateFormatter.format(this.startDate.getTime());
		Calendar endDate = (Calendar)startDate.clone();
		endDate.add(Calendar.MONTH, 6);
		String endMonth = row0DateFormatter.format(endDate.getTime());
		String row0Date = startMonth + " through " + endMonth;
		this.setSubtitle(row0Date);
		
		sheet.setAutobreaks(true);
		XSSFPrintSetup ps = sheet.getPrintSetup();
		ps.setLandscape(true);
		ps.setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.setMargin(XSSFSheet.BottomMargin, this.marginBottom);
		sheet.setMargin(XSSFSheet.TopMargin, this.marginTop);
		sheet.setMargin(XSSFSheet.RightMargin, this.marginRight);
		sheet.setMargin(XSSFSheet.LeftMargin, this.marginLeft);
		
		
		
		XSSFCell cell = null;
		XSSFRow row = null;
		int rownum = 0;
		int columnIndex = 0;

		row = sheet.createRow(rownum);
		cell = row.createCell(0);
		cell.setCellValue("Created");
		cell.setCellStyle(reportFormatter.cellStyleReportHeaderLabelLeft);
		cell = row.createCell(1);
		cell.setCellValue(runDate);
		cell.setCellStyle(reportFormatter.cellStyleDateTimeLeft);
		cell = row.createCell(2);
		cell.setCellValue(super.getBanner());
		cell.setCellStyle(reportFormatter.cellStyleReportBanner);
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 2, lastColumnNumber));

		rownum++;

		row = sheet.createRow(rownum);
		cell = row.createCell(0);
		cell.setCellValue("Div");
		cell.setCellStyle(reportFormatter.cellStyleReportHeaderLabelLeft);
		cell = row.createCell(1);
		cell.setCellValue(this.div);
		cell.setCellStyle(reportFormatter.cellStyleStandardLeft);
		cell = row.createCell(2);
		cell.setCellValue(REPORT_TITLE);
		cell.setCellStyle(reportFormatter.cellStyleReportTitle);
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 2, lastColumnNumber));
		
		rownum++;
		row = sheet.createRow(rownum);
		cell = row.createCell(2);
		cell.setCellValue(row0Date);
		sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,2,lastColumnNumber));
		cell.setCellStyle(reportFormatter.cellStyleReportSubTitle);
		
		
		sheet.setColumnWidth(0, 3575); // Building name
		sheet.setColumnWidth(1, 4175);
		sheet.setColumnWidth(2, 3000);	// Zip Code
		sheet.setColumnWidth(3, 7750);	// Street
		sheet.setColumnWidth(4, 3000);	// Job #
		sheet.setColumnWidth(5, 3000);	// Last RUn
		sheet.setColumnWidth(6, 3000);	// Job ID
		sheet.setColumnWidth(7, 3000);	// Freq		
		sheet.setColumnWidth(8, 3000);	// month 1
		sheet.setColumnWidth(9, 3000);	// month 2
		sheet.setColumnWidth(10, 3000);	// month 3
		sheet.setColumnWidth(11, 3000);	// month 4
		sheet.setColumnWidth(12, 3000);	// month 5
		sheet.setColumnWidth(13, 3000);	// month 6
		sheet.setColumnWidth(14, 3000);	// Total
		
		
		
		
		
		
		rownum++;
		
		
		row = sheet.createRow(rownum);
		columnIndex = 0;
		for ( int i = 0; i<colHeaders.length; i++ ) {
			if ( i == 1 ) {
				columnIndex++;
				sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, 1));
			}
			cell = row.createCell(columnIndex);
			cell.setCellStyle(reportFormatter.cellStyleColHdrCenter);
			cell.setCellValue(colHeaders[i]);
			columnIndex++;
		}

		for ( int i = 0; i < 6; i++ ) {
			cell = row.createCell(columnIndex);
			cell.setCellStyle(reportFormatter.cellStyleColHdrCenter);
			Calendar columnDate = (Calendar)startDate.clone();
			columnDate.add(Calendar.MONTH, i);
			String columnDateHeader = columnDateHeaderFormat.format(columnDate.getTime());
			cell.setCellValue(columnDateHeader);
			columnIndex++;
		}

		cell = row.createCell(columnIndex);
		cell.setCellStyle(reportFormatter.cellStyleColHdrCenter);
		cell.setCellValue("Totals");

		rownum++;
		
		CellStyle cellStyleStandardLeft = sheet.getWorkbook().createCellStyle();
		cellStyleStandardLeft.cloneStyleFrom(reportFormatter.cellStyleStandardLeft);
		cellStyleStandardLeft.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyleStandardLeft.setBorderLeft(CellStyle.BORDER_THIN);
		
		CellStyle cellStyleStandardCenter = sheet.getWorkbook().createCellStyle();
		cellStyleStandardCenter.cloneStyleFrom(reportFormatter.cellStyleStandardCenter);
		cellStyleStandardCenter.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyleStandardCenter.setBorderLeft(CellStyle.BORDER_THIN);
		
		CellStyle cellStyleNumberCenter = sheet.getWorkbook().createCellStyle();
		cellStyleNumberCenter.cloneStyleFrom(reportFormatter.cellStyleNumberCenter);
		cellStyleNumberCenter.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyleNumberCenter.setBorderLeft(CellStyle.BORDER_THIN);
		
		CellStyle cellStyleDateCenter = sheet.getWorkbook().createCellStyle();
		cellStyleDateCenter.cloneStyleFrom(reportFormatter.cellStyleDateCenter);
		cellStyleDateCenter.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyleDateCenter.setBorderLeft(CellStyle.BORDER_THIN);
		
		CellStyle cellStyleStandardDecimal = sheet.getWorkbook().createCellStyle();
		cellStyleStandardDecimal.cloneStyleFrom(reportFormatter.cellStyleStandardDecimal);
		cellStyleStandardDecimal.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyleStandardDecimal.setBorderLeft(CellStyle.BORDER_THIN);
		
		CellStyle lastColumn = sheet.getWorkbook().createCellStyle();
		lastColumn.cloneStyleFrom(cellStyleStandardDecimal);
		lastColumn.setBorderRight(CellStyle.BORDER_THIN);
		
		for ( Object dataItem : getDataRows() ) {
			row = sheet.createRow(rownum);
			BigDecimal rowTotal = BigDecimal.ZERO;
			DataRow dataRow = (DataRow)dataItem;
			columnIndex = 0;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardLeft);
			cell.setCellValue(dataRow.getJobSiteName());
			sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, 1));
			columnIndex = columnIndex + 2;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardCenter);
			cell.setCellValue(dataRow.getZip());
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardLeft);
			cell.setCellValue(dataRow.getAddress1());
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleNumberCenter);
			cell.setCellValue(dataRow.getJobNbr());
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleDateCenter);			
			if ( dataRow.getLastRun() != null ) {
				cell.setCellValue(dataRow.getLastRun());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleNumberCenter);
			cell.setCellValue(dataRow.getJobId());
			columnIndex++;
			
			cell = row.createCell(columnIndex);			
			cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
			cell.setCellStyle(cellStyleStandardCenter);
			cell.setCellValue(dataRow.getJobFrequency().abbrev());
			columnIndex++;
			
			cell = row.createCell(columnIndex);			
			cell.setCellStyle(cellStyleStandardDecimal);
			if ( dataRow.getPpcm01() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm01());
				cell.setCellValue(dataRow.getPpcm01().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardDecimal);
			if ( dataRow.getPpcm02() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm02());
				cell.setCellValue(dataRow.getPpcm02().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardDecimal);
			if ( dataRow.getPpcm03() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm03());
				cell.setCellValue(dataRow.getPpcm03().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardDecimal);
			if ( dataRow.getPpcm04() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm04());
				cell.setCellValue(dataRow.getPpcm04().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardDecimal);
			if ( dataRow.getPpcm05() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm05());
				cell.setCellValue(dataRow.getPpcm05().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(cellStyleStandardDecimal);
			if ( dataRow.getPpcm06() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm06());
				cell.setCellValue(dataRow.getPpcm06().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);	
			cell.setCellStyle(lastColumn);
			cell.setCellValue(rowTotal.doubleValue());
			columnIndex++;

			rownum++;
		}
		
		row = sheet.createRow(rownum);

		cell = row.createCell(0);
		cell.setCellStyle(reportFormatter.cellStyleStandardLeft);
		cell.setCellValue("Job Count: " + jobCount);			
		
		cell = row.createCell(3);
		cell.setCellStyle(reportFormatter.cellStyleStandardLeft);
		cell.setCellValue("Contract Count: " + contractCount);			
		
		final int monthlyOffset=8; // column to start displaying the monthly totals

		double grandTotal = 0.0D;
		for (int idx = 0; idx < this.monthlyTotal.length; idx++ ) {
			cell = row.createCell(idx+monthlyOffset);
			cell.setCellStyle(reportFormatter.cellStyleStandardDecimal);
			cell.setCellValue(this.monthlyTotal[idx]);
			grandTotal = grandTotal + this.monthlyTotal[idx];
		}
		cell = row.createCell(this.monthlyTotal.length+monthlyOffset);
		cell.setCellStyle(reportFormatter.cellStyleStandardDecimal);
		cell.setCellValue(grandTotal);
		
		
		Footer footer = sheet.getFooter();
		footer.setCenter("Page &P of &N");
		
//		sheet.setRepeatingRows(new CellRangeAddress(0,3,0,15));  //banner + title + subtitle + column header = rows 0-3
		sheet.setRepeatingRows(CellRangeAddress.valueOf("1:4"));

		// fit to 1 page wide
		sheet.setFitToPage(true);
		sheet.getPrintSetup().setFitWidth((short)1);		
		sheet.getPrintSetup().setFitHeight((short)0);
		
	}


	
	/**
	 * Create a 6-Month Rolling Volume report object for a single 6-month period
	 * @param conn Database Connection
	 * @param divisionId Division Filter
	 * @param month Month of the year (1-12)
	 * @param year 4-digit year (eg 2017, not 17)
	 * @return Report Object
	 * @throws Exception something bad happened
	 */
	public static SixMonthRollingVolumeReport buildReport(Connection conn, Integer divisionId, Integer month, Integer year) throws Exception {		
		List<Connection> connectionList = Arrays.asList(new Connection[] {conn});
		Integer startMonth = month - 1;  // because java calendars start with 0
		Calendar startDate = new GregorianCalendar(year, startMonth, 1);
		return buildReport(connectionList, divisionId, startDate).get(0);
	}
	
	/**
	 * Create list of 6-Month Rolling Volume Report objects for sequential 6-month periods. The number
	 * of periods is dependent on the number of connections in the list
	 * @param connectionList Database connections for parallel processing of report builds
	 * @param divisionId Division Filter
	 * @param startDate Start Date
	 * @return Report Object
	 * @throws Exception Something bad happened
	 */
	public static List<SixMonthRollingVolumeReport> buildReport(List<Connection> connectionList, Integer divisionId, Calendar startDate) throws Exception {
		return makeReport(connectionList, divisionId, startDate);
	}
	
	@Override
	public int compareTo(SixMonthRollingVolumeReport o) {
		return this.getStartDate().compareTo(o.getStartDate());
	}

	@Override
	public XSSFWorkbook makeXLS() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		add2XLS(workbook);
		return workbook;
	}

	@Override
	public void add2XLS(XSSFWorkbook workbook) throws Exception {
		XLSReportFormatter reportFormatter = new XLSReportFormatter(workbook);
		SimpleDateFormat sheetNameFormatter = new SimpleDateFormat("MM-dd-yyyy");
		XSSFSheet sheet = workbook.createSheet();
		this.populateXlsSheet(sheet, reportFormatter);
		String sheetname = sheetNameFormatter.format(this.getStartDate().getTime());
		Integer sheetIndex = workbook.getNumberOfSheets() - 1;
		workbook.setSheetName(sheetIndex, sheetname);
	}
	
	@Override
	public String makeHTML() throws Exception {
		int lastColumnNumber = 12;

		List<String> reportLineList = new ArrayList<String>();
		reportLineList.add("<html>\n<head>");
		reportLineList.add("</head>\n<body>");
		
		
		
		/*** ******************************** ***/
		
		Calendar printDate = Calendar.getInstance(new AnsiTime());
		DateFormatter headerDateFormatter = (DateFormatter)DataFormats.DATE_TIME_FORMAT.formatter();
		String headerDate = headerDateFormatter.format(printDate);
		
		SimpleDateFormat row0DateFormatter = new SimpleDateFormat("MMMM");
		String startMonth = row0DateFormatter.format(this.startDate.getTime());
		Calendar endDate = (Calendar)startDate.clone();
		endDate.add(Calendar.MONTH, 6);
		String endMonth = row0DateFormatter.format(endDate.getTime());
		String row0Date = startMonth + " through " + endMonth;
		
		HTMLTable headerTable = new HTMLTable();

		SimpleDateFormat columnDateHeaderFormat = new SimpleDateFormat("MM/yyyy");
		HTMLCell cell = null;
		HTMLRow row = null;
		int rownum = 0;
		int columnIndex = 0;
		
		
		
		
		row = headerTable.createRow(rownum);
		cell = row.createCell(0);
		cell.setCellValue("Created");
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		cell = row.createCell(1);
		cell.setCellValue(runDate);
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT_TEXT);
		cell.setDataFormats(DataFormats.DATE_TIME_FORMAT);
		
		cell = row.createCell(2);
		cell.setCellValue(super.getBanner());
		cell.setCellStyle(HTMLReportFormatter.CSS_BANNER);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_BANNER_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		cell.setColspan(lastColumnNumber - 2);
		

		rownum++;

		row = headerTable.createRow(rownum);
		cell = row.createCell(0);
		cell.setCellValue("Div");
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		cell = row.createCell(1);
		cell.setCellValue(this.div);
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		cell = row.createCell(2);
		cell.setCellValue(REPORT_TITLE);
		cell.setCellStyle(HTMLReportFormatter.CSS_TITLE);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_TITLE_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		cell.setColspan(lastColumnNumber - 2);
		
		rownum++;
		row = headerTable.createRow(rownum);
		cell = row.createCell(2);
		cell.setCellValue(row0Date);
		cell.setColspan(lastColumnNumber - 2);
		cell.setCellStyle(HTMLReportFormatter.CSS_SUBTITLE);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_SUBTITLE_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		
		
		HTMLTable sheet = new HTMLTable();

		
		rownum=0;
		
		
		row = sheet.createRow(rownum);
		for ( columnIndex = 0; columnIndex<colHeaders.length; columnIndex++ ) {
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_COLHDR);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_COLHDR_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
			cell.setCellValue(colHeaders[columnIndex]);
		}
		
		for ( int i = 0; i < 6; i++ ) {
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_COLHDR);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_COLHDR_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
			Calendar columnDate = (Calendar)startDate.clone();
			columnDate.add(Calendar.MONTH, i);
			String columnDateHeader = columnDateHeaderFormat.format(columnDate.getTime());
			cell.setCellValue(columnDateHeader);
			columnIndex++;
		}

		cell = row.createCell(columnIndex);
		cell.setCellStyle(HTMLReportFormatter.CSS_COLHDR);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_COLHDR_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		cell.setCellValue("Totals");

		rownum++;
		
		for ( Object dataItem : getDataRows() ) {
			row = sheet.createRow(rownum);
			BigDecimal rowTotal = BigDecimal.ZERO;
			DataRow dataRow = (DataRow)dataItem;
			columnIndex = 0;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_LEFT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
			cell.setCellValue(dataRow.getJobSiteName());			
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_CENTER);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_CENTER_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
			cell.setCellValue(dataRow.getZip());
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_LEFT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
			cell.setCellValue(dataRow.getAddress1());
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_CENTER);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_CENTER_TEXT);
			cell.setDataFormats(DataFormats.NUMBER_CENTERED);
			cell.setCellValue(dataRow.getJobNbr());
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_CENTER);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_CENTER_TEXT);
			cell.setDataFormats(DataFormats.DATE_FORMAT);
			if ( dataRow.getLastRun() != null ) {
				cell.setCellValue(dataRow.getLastRun());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_CENTER);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_CENTER_TEXT);
			cell.setDataFormats(DataFormats.INTEGER_FORMAT);
			cell.setCellValue(dataRow.getJobId());
			columnIndex++;
			
			cell = row.createCell(columnIndex);			
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_CENTER);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_CENTER_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
			cell.setCellValue(dataRow.getJobFrequency().abbrev());
			columnIndex++;
			
			cell = row.createCell(columnIndex);			
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			if ( dataRow.getPpcm01() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm01());
				cell.setCellValue(dataRow.getPpcm01().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			if ( dataRow.getPpcm02() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm02());
				cell.setCellValue(dataRow.getPpcm02().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			if ( dataRow.getPpcm03() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm03());
				cell.setCellValue(dataRow.getPpcm03().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			if ( dataRow.getPpcm04() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm04());
				cell.setCellValue(dataRow.getPpcm04().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			if ( dataRow.getPpcm05() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm05());
				cell.setCellValue(dataRow.getPpcm05().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			if ( dataRow.getPpcm06() != null ) {
				rowTotal = rowTotal.add(dataRow.getPpcm06());
				cell.setCellValue(dataRow.getPpcm06().doubleValue());
			}
			columnIndex++;
			
			cell = row.createCell(columnIndex);	
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			cell.setCellValue(rowTotal.doubleValue());
			columnIndex++;
			
			rownum++;
		}
		
		row = sheet.createRow(rownum);

		cell = row.createCell(0);
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		cell.setCellValue("Job Count: " + jobCount);			
		
		
		cell = row.createCell(2);
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		cell.setCellValue("Contract Count: " + contractCount);			
		
		final int monthlyOffset=7; // column to start displaying the monthly totals

		for (int idx = 0; idx < this.monthlyTotal.length; idx++ ) {
			cell = row.createCell(idx+monthlyOffset);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			cell.setCellValue(this.monthlyTotal[idx]);
		}
		
		double grandTotal = 0.0D;
		for (int idx = 0; idx < this.monthlyTotal.length; idx++ ) {
			cell = row.createCell(idx+monthlyOffset);
			cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
			cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
			cell.setCellValue(this.monthlyTotal[idx]);
			grandTotal = grandTotal + this.monthlyTotal[idx];
		}
		cell = row.createCell(this.monthlyTotal.length+monthlyOffset);
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_RIGHT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		cell.setDataFormats(DataFormats.DECIMAL_FORMAT);
		cell.setCellValue(grandTotal);
		
		/*** ******************************** ***/
		
		StringBuffer reportHtml = new StringBuffer();
		reportHtml.append(headerTable.makeHTML());
		reportHtml.append("\n");
		reportHtml.append(sheet.makeHTML());
		reportHtml.append("\n");

		reportLineList.add(reportHtml.toString());
		
		
		reportLineList.add("</body>\n</html>");		
		return StringUtils.join(reportLineList, "\n");
	}

	
	
	@Override
	public ByteArrayOutputStream makePDF() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PDFReportHeader header = new PDFReportHeader(this);
		float topMargin = header.getHeaderTable().getTotalHeight() + (PDFReportFormatter.shortSideSize - PDFReportFormatter.headerDefaultPositionY) + 4.0F;
		Document document = new Document(PageSize.LETTER.rotate(), PDFReportFormatter.marginLeft, PDFReportFormatter.marginRight, topMargin, PDFReportFormatter.marginBottom);
		PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
		pdfWriter.setPageEvent(header);
		document.open();

		PdfPTable dataTable = new PdfPTable(14);
		float[] colWidths = new float[14];
		float tableWidth = PDFReportFormatter.longSideSize - (PDFReportFormatter.marginLeft + PDFReportFormatter.marginRight);
		colWidths[0] = 0.15F * tableWidth; // building name
		colWidths[1] = 0.06F * tableWidth; // zip
		colWidths[2] = 0.15F * tableWidth; // Street
		colWidths[3] = 0.055F * tableWidth; // job #
		colWidths[4] = 0.065F * tableWidth; // last run
		colWidths[5] = 0.06F * tableWidth; // job id
		colWidths[6] = 0.053F * tableWidth; // Freq
		colWidths[7] = 0.061F * tableWidth; // month 1
		colWidths[8] = 0.061F * tableWidth; // month 2
		colWidths[9] = 0.061F * tableWidth; // month 3
		colWidths[10] = 0.061F * tableWidth; // month 4
		colWidths[11] = 0.061F * tableWidth; // month 5
		colWidths[12] = 0.061F * tableWidth; // month 6
		colWidths[13] = 0.061F * tableWidth; // total
		
		dataTable.setHeaderRows(1);	// set column headers to repeat on each page
		dataTable.setTotalWidth(colWidths);
		dataTable.setWidthPercentage(100F);
		dataTable.setLockedWidth(true);


		/**
		 * Column Headers
		 */
		PdfPCell headerCell = new AnsiPCell();
		headerCell.setVerticalAlignment(Element.ALIGN_TOP);
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//		headerCell.setBorder(Rectangle.NO_BORDER);
		headerCell.setBorderColor(BaseColor.BLACK);
		headerCell.setBorderWidth(1F);
		headerCell.setIndent(0F);
		headerCell.setPaddingTop(4F);
		headerCell.setPaddingBottom(4F);
		headerCell.setBackgroundColor(BaseColor.BLACK);	
		
		for ( int i = 0; i<colHeaders.length; i++ ) {			
			headerCell.setPhrase(new Phrase(new Chunk(colHeaders[i], fontStandardWhite)));
			dataTable.addCell(headerCell);
		}
		for ( int i = 0; i < 6; i++ ) {
			Calendar columnDate = (Calendar)startDate.clone();
			columnDate.add(Calendar.MONTH, i);
			String columnDateHeader = columnDateHeaderFormat.format(columnDate.getTime());
			headerCell.setPhrase(new Phrase(new Chunk(columnDateHeader, fontStandardWhite)));
			dataTable.addCell(headerCell);
		}
		headerCell.setPhrase(new Phrase("Totals", fontStandardWhite));
		dataTable.addCell(headerCell);


		
		/**
		 * Report Details
		 */
		PdfPCell cell = new AnsiPCell();
		cell.setVerticalAlignment(Element.ALIGN_TOP);
//		detailCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.enableBorderSide(Rectangle.LEFT);
		cell.enableBorderSide(Rectangle.BOTTOM);
		cell.setBorderColor(BaseColor.BLACK);
		cell.setBorderWidth(1F);
		cell.setIndent(0F);
		cell.setPaddingTop(1F);
		cell.setPaddingBottom(2F);
//		detailCell.setBackgroundColor(BaseColor.BLACK);	
		for ( Object dataItem : getDataRows() ) {
			BigDecimal rowTotal = BigDecimal.ZERO;
			DataRow dataRow = (DataRow)dataItem;
			cell.disableBorderSide(Rectangle.RIGHT);
			
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setPhrase(new Phrase(new Chunk(dataRow.getJobSiteName(), fontStandardBlack)));
			dataTable.addCell(cell);
			
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPhrase(new Phrase(new Chunk(dataRow.getZip(), fontStandardBlack)));
			dataTable.addCell(cell);
			
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setPhrase(new Phrase(new Chunk(dataRow.getAddress1(), fontStandardBlack)));
			dataTable.addCell(cell);
			
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPhrase(new Phrase(new Chunk(String.valueOf(dataRow.getJobNbr()), fontStandardBlack)));
			dataTable.addCell(cell);
			
			String value = dataRow.getLastRun() == null ? "" : detailCellDateFormat.format(dataRow.getLastRun());
			cell.setPhrase(new Phrase(new Chunk(value, fontStandardBlack)));		
			dataTable.addCell(cell);
			
			cell.setPhrase(new Phrase(new Chunk(String.valueOf(dataRow.getJobId()), fontStandardBlack)));
			dataTable.addCell(cell);
			
			cell.setPhrase(new Phrase(new Chunk(dataRow.getJobFrequency().abbrev(), fontStandardBlack)));
			dataTable.addCell(cell);
			
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			for ( BigDecimal monthlyValue : new BigDecimal[] {dataRow.getPpcm01(), dataRow.getPpcm02(), dataRow.getPpcm03(),dataRow.getPpcm04(),dataRow.getPpcm05(),dataRow.getPpcm06()} ) {
				if ( dataRow.getPpcm01() == null ) {
					cell.setPhrase(new Phrase(""));
				} else {
					rowTotal = rowTotal.add(monthlyValue);
					cell.setPhrase( new Phrase(new Chunk(detailCellNumberFormat.format(monthlyValue.doubleValue()), fontStandardBlack)));
				}
				dataTable.addCell(cell);
				
			}
			
			cell.enableBorderSide(Rectangle.RIGHT);
			cell.setPhrase( new Phrase(new Chunk(detailCellNumberFormat.format(rowTotal.doubleValue()), fontStandardBlack)));
			dataTable.addCell(cell);
		}

		
		
		
		/**
		 * Summary Row
		 */
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setPhrase( new Phrase(new Chunk("Job Count: " + jobCount, fontStandardBlack)));
		dataTable.addCell(cell);
		
		cell.setPhrase(new Phrase(""));
		dataTable.addCell(cell);  //empty column 
		
		cell.setPhrase(new Phrase(new Chunk("Contract Count: " + contractCount, fontStandardBlack)));
		dataTable.addCell(cell);
		
		cell.setPhrase(new Phrase(""));
		dataTable.addCell(cell);  //empty column Job #
		dataTable.addCell(cell);  //empty column last run
		dataTable.addCell(cell);  //empty column Job Id
		dataTable.addCell(cell);  //empty column Freq
		
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

		double grandTotal = 0.0D;
		for (int idx = 0; idx < this.monthlyTotal.length; idx++ ) {
			cell.setPhrase(new Phrase(new Chunk(detailCellNumberFormat.format(this.monthlyTotal[idx]), fontStandardBlack)));
			grandTotal = grandTotal + this.monthlyTotal[idx];
			dataTable.addCell(cell);
		}
		cell.setPhrase(new Phrase(new Chunk(detailCellNumberFormat.format(grandTotal), fontStandardBlack)));
		dataTable.addCell(cell);
		
		
		/**
		 * Do all the PDF stuff to finish up
		 */
		document.add(dataTable);
		document.close();

		return baos;
	}



	public class DataRow extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		
		private String jobSiteName;
		private String zip;
		private String address1;
		private Integer jobId;
		private Integer jobNbr;
		private JobFrequency jobFrequency;
		private BigDecimal jobPpc;
		private Date jobStartDate;
		private Date lastRun;
		private BigDecimal ppcm01;
		private BigDecimal ppcm02;
		private BigDecimal ppcm03;
		private BigDecimal ppcq01;
		private BigDecimal ppcm04;
		private BigDecimal ppcm05;
		private BigDecimal ppcm06;
		private BigDecimal ppcq02;
		private BigDecimal ppch01;
		private Double[] jobMonthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};

		
		
		public DataRow(ResultSet rs) throws SQLException {
			this.jobSiteName = rs.getString("job_site_name");
			this.zip = rs.getString("zip");
			this.address1 = rs.getString("address1");
			this.jobId = rs.getInt("job_id");
			this.jobNbr = rs.getInt("job_nbr");
			this.jobFrequency = JobFrequency.get(rs.getString("job_frequency"));
			this.jobPpc = rs.getBigDecimal("price_per_cleaning");
			java.sql.Date lastRunDate = rs.getDate("last_run");
			if ( lastRunDate != null ) {
				this.lastRun = new Date(lastRunDate.getTime());
			}
			java.sql.Date jobStartDate = rs.getDate("start_date");
			if ( jobStartDate != null ) {
				this.jobStartDate = new Date(jobStartDate.getTime());
			}
//			this.ppcm01 = rs.getBigDecimal("ppcm01");
//			this.ppcm02 = rs.getBigDecimal("ppcm02");
//			this.ppcm03 = rs.getBigDecimal("ppcm03");
//			this.ppcq01 = rs.getBigDecimal("ppcq01");
//			this.ppcm04 = rs.getBigDecimal("ppcm04");
//			this.ppcm05 = rs.getBigDecimal("ppcm05");
//			this.ppcm06 = rs.getBigDecimal("ppcm06");
//			this.ppcq02 = rs.getBigDecimal("ppcq02");
//			this.ppch01 = rs.getBigDecimal("ppch01");
		}


		public String getJobSiteName() {
			return jobSiteName;
		}
		public String getZip() {
			return zip;
		}
		public String getAddress1() {
			return address1;
		}
		public Integer getJobNbr() {
			return jobNbr;
		}
		public Integer getJobId() {
			return jobId;
		}


		public JobFrequency getJobFrequency() {
			return jobFrequency;
		}
		public BigDecimal getJobPpc() {
			return jobPpc;
		}
		public Date getJobStartDate() {
			return jobStartDate;
		}


		public Date getLastRun() {
			return lastRun;
		}
		public BigDecimal getPpcm01() {
			return ppcm01;
		}
		public BigDecimal getPpcm02() {
			return ppcm02;
		}
		public BigDecimal getPpcm03() {
			return ppcm03;
		}
		public BigDecimal getPpcq01() {
			return ppcq01;
		}
		public BigDecimal getPpcm04() {
			return ppcm04;
		}
		public BigDecimal getPpcm05() {
			return ppcm05;
		}
		public BigDecimal getPpcm06() {
			return ppcm06;
		}
		public BigDecimal getPpcq02() {
			return ppcq02;
		}
		public BigDecimal getPpch01() {
			return ppch01;
		}
	}
	
	
	private class ReportRunnable implements Runnable {

		public PreparedStatement ps;
		public Division division;
		public Calendar startDate;
		public SixMonthRollingVolumeReport report;
		
		public ReportRunnable(PreparedStatement ps, Division division, Calendar startDate) {
			super();
			this.ps = ps;
			this.division = division;
			this.startDate = startDate;
		}
		
		@Override
		public void run() {
			try {
				this.report = new SixMonthRollingVolumeReport(ps, division, startDate);
			} catch ( Exception e) {
				throw new RuntimeException(e);
			}
		}		
	}
	
	
	
	

	
	
}
