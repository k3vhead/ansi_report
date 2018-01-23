package com.ansi.scilla.report.cashReceiptsRegister;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.DateFormatter;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.StandardSummaryReport;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;
import com.ansi.scilla.report.reportBuilder.XLSReportBuilderUtils;

public class CashReceiptsRegisterSummaryReport extends StandardSummaryReport {

	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Cash Receipts Register Summary";
//	private final String REPORT_NOTES = null;

	private Calendar startDate;
	private Calendar endDate;

	Logger logger = LogManager.getLogger(this.getClass());
	
	public CashReceiptsRegisterSummaryReport() {
		super((StandardReport)null, (StandardReport)null, (StandardReport)null);
		this.setTitle(REPORT_TITLE);
		setReportOrientation(ReportOrientation.LANDSCAPE);
	}
	
	/**
	 * Default date range is current month-to-date
	 * @param conn Database Connection
	 * @throws Exception Something bad happened
	 */
	protected CashReceiptsRegisterSummaryReport(Connection conn) throws Exception {
		super(
			(StandardReport)(new CashReceiptsRegisterDivisionSummary(conn)),
			(StandardReport)(new CashReceiptsRegisterRegionSummary(conn)),
			(StandardReport)(new CashReceiptsRegisterCompanySummary(conn))
			);

		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);	
		
		endDate = (Calendar)Calendar.getInstance(new AnsiTime());
		makeReport();
		setReportOrientation(ReportOrientation.PORTRAIT);

	}

	protected CashReceiptsRegisterSummaryReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(
			(StandardReport)(new CashReceiptsRegisterDivisionSummary(conn, startDate, endDate)),
			(StandardReport)(new CashReceiptsRegisterRegionSummary(conn, startDate, endDate)),
			(StandardReport)(new CashReceiptsRegisterCompanySummary(conn, startDate, endDate))
			);
		
		this.startDate = startDate;
		this.endDate = endDate;
		makeReport();
	}
	
	
	public Calendar getCreatedDate() {
		Calendar today = Calendar.getInstance(new AnsiTime());
		return today;
	}
	
	public String getDateRange() {
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());

		return DateUtils.isSameDay(startDate, endDate) ? startTitle : startTitle + "-" + endTitle;
	}
	
	private void makeReport() throws NoSuchMethodException, SecurityException {
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());

		String subtitle = "";
		if ( DateUtils.isSameDay(startDate, endDate) ) {
			subtitle = "For the Day of " + startTitle;
		} else {
			subtitle = startTitle + " through " + endTitle;
		}
		
		this.setTitle(REPORT_TITLE);
		this.setSubtitle(subtitle);

		Method getCreatedDate = this.getClass().getMethod("getCreatedDate", (Class<?>[])null);
		Method getDateRange = this.getClass().getMethod("getDateRange", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getCreatedDate, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("For:", getDateRange, 1, DataFormats.STRING_FORMAT),
		});
		super.makeHeaderLeft(headerLeft);		
	}
	
	public Calendar getStartDate() {
		return startDate;
	}
	public Calendar getEndDate() {
		return endDate;
	}
	
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheet = workbook.createSheet();
		StandardReport companySummary = this.getCompanySummary();
		StandardReport regionSummary = this.getRegionSummary();
		StandardReport divisionSummary = this.getDivisionSummary();
		
		Integer nbrOfHeaderRows = XLSReportBuilderUtils.makeSummaryHeader(this, new ReportStartLoc(0,0), sheet);
		
		Integer companyStartRow = nbrOfHeaderRows + 1;
		Integer companyStartCol = 0;
		
		Integer regionStartRow = companyStartRow + companySummary.getReportHeight() + 2;
		Integer regionStartCol = 0;
		
		Integer divisionStartRow = nbrOfHeaderRows + 1;
		Integer divisionStartCol = Math.max(companySummary.getReportWidth(), regionSummary.getReportWidth()) + 1; 
		
		XLSBuilder.build(companySummary, sheet, new ReportStartLoc(companyStartCol, companyStartRow));		
		XLSBuilder.build(regionSummary, sheet, new ReportStartLoc(regionStartCol, regionStartRow));
		XLSBuilder.build(divisionSummary, sheet, new ReportStartLoc(divisionStartCol, divisionStartRow));
	}
	
	
	public static CashReceiptsRegisterSummaryReport buildReport(Connection conn) throws Exception {
		return new CashReceiptsRegisterSummaryReport(conn);
	}
	
	public static CashReceiptsRegisterSummaryReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CashReceiptsRegisterSummaryReport(conn, startDate, endDate);
	}

}
