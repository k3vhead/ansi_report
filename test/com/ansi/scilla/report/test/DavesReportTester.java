package com.ansi.scilla.report.test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterSummaryReport;
import com.ansi.scilla.report.datadumps.AccountsReceivableTotalsOver60Detail;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;
import com.ansi.scilla.report.sixMonthRollingVolume.SixMonthRollingVolumeReport;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;
import com.ansi.scilla.report.ticket.TicketStatusReport;;


public class DavesReportTester extends AbstractReportTester {

	private final String testResultDirectory = "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/";
	
	protected ReportType reportType;
	protected Calendar startDate;
	protected Calendar endDate;
	protected Integer divisionId;
	protected Integer month;
	protected Integer year;
	protected HashMap<String, String> reportDisplay;
	protected Logger logger;

	
	public void go() throws Exception {
		boolean makePDF = true;
		boolean makeHTML = false;
		boolean makeXLS = true;
		
		ReportMaker[] reportList = new ReportMaker[] {
				new Make6MRV(makeXLS, makePDF, makeHTML),
				new MakeAROver60(makeXLS, makePDF, makeHTML)
		};
		super.makeMyReport(reportList);
	}
	
	public static void main(String[] args) {
		try {			
			new DavesReportTester().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
//	private void makeMyReport() throws Exception {
//		this.logger = LogManager.getLogger("com.ansi.scilla.report.reportBuilder");
//		logger.info("Start");
//
//		List<Thread> threadList = new ArrayList<Thread>();
//		
//		threadList.add(new Thread(new Make6MRV()));					// this is a custom report
//		threadList.add(new Thread(new MakeAROver60()));   			// this is a datadump
//		threadList.add(new Thread(new MakeClientUsage()));
//		threadList.add(new Thread(new MakeCRRDetail()));
//		threadList.add(new Thread(new MakeCRRSummary()));   		// this is a standard summary
//		threadList.add(new Thread(new MakeDO()));					// this is a standard report with banner notes
//		threadList.add(new Thread(new MakeInvoiceRegister()));   	// this is a standard report with totals
//		threadList.add(new Thread(new MakePACListing()));			// this is a compound report
//		threadList.add(new Thread(new makePastDue2()));	
//		threadList.add(new Thread(new MakeTicketStatus()));			// this is a standard report
//
//		for ( Thread thread : threadList ) {
//			thread.start();
//		}
//		
//		while ( true ) {
//			try {
//				for ( Thread thread : threadList ) {
//					thread.join();
//				}
//				break;
//			} catch ( InterruptedException e) {
//				System.err.println("Interrupted");
//			}
//		}
//		logger.info("Done");		
//	}

	@Override
	protected String getTestDirectory() {
		return testResultDirectory;
	}

	
}
