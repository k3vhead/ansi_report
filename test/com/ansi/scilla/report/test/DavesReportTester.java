package com.ansi.scilla.report.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;

public class DavesReportTester extends AbstractReportTester {

	private final String testResultDirectory = "/home/dclewis/Documents/Dropbox/webthing_v2/projects/ANSI/testresults/report_pdf/";

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

		Integer divisionId = 118;
		Integer month = Calendar.JUNE;
		Integer year = 2022;
		Calendar startDate = new GregorianCalendar(2022, Calendar.JUNE, 1);
		Calendar endDate = new GregorianCalendar(2022, Calendar.JUNE, 30);

		List<Thread> threadList = new ArrayList<Thread>();
		
//		threadList.add(new Thread(new MakeAROver60Detail()));
//			threadList.add(new Thread(new Make6mrv()));
//		threadList.add(new Thread(new MakeAROver60()));
//			threadList.add(new Thread(new MakeClientUsage()));
//			threadList.add(new Thread(new Make6mrv(conn)));
//		threadList.add(new Thread(new MakeAROver60()));
//			threadList.add(new Thread(new MakeClientUsage(conn)));
//		threadList.add(new Thread(new MakeCRRDetail()));
//		threadList.add(new Thread(new MakeExpiringDocument()));
//		threadList.add(new Thread(new MakeDO()));
//		threadList.add(new Thread(new MakeInvoiceRegister()));
//		threadList.add(new Thread(new MakePACListing()));
//			threadList.add(new Thread(new MakePastDue2()));
//			threadList.add(new Thread(new MakePastDue2(conn)));
//		threadList.add(new Thread(new MakeTicketStatus()));


//		MakeLiftAndGenie prodLiftAndGenie = new MakeLiftAndGenie( makeXLS, makePDF, makeHTML, startDate, endDate);
//		prodLiftAndGenie.setReportConn(ReportConn.PROD);
		
		ReportMaker[] reportList = new ReportMaker[] {				
//				new Make6MRV(makeXLS, makePDF, makeHTML, divisionId, month, year),		// this is a custom report
//				new MakeAROver60(makeXLS, makePDF, makeHTML),							// this is a datadump
//				new MakeARTotalsSummary(makeXLS, makePDF, makeHTML),
//				new MakeARTotalByDiv(makeXLS, makePDF, makeHTML, divisionId),
//				new MakeARTotals(makeXLS, makePDF, makeHTML),
//				new MakeCreditCardFeesSummaryReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeClientContact(makeXLS, makePDF, makeHTML),						// this is a datadump
//				new MakeCRRDetail(makeXLS, makePDF, makeHTML, startDate, endDate),		// this is a standard report with subtotals
//				new MakeCRRSummary(makeXLS, makePDF, makeHTML, startDate, endDate),   		// this is a standard summary
				new MakeDO(makeXLS, makePDF, makeHTML, 115, endDate),					// this is a standard report with banner notes
				new MakeDO(makeXLS, makePDF, makeHTML, 116, endDate),					// this is a standard report with banner notes
				new MakeDO(makeXLS, makePDF, makeHTML, 117, endDate),					// this is a standard report with banner notes
				new MakeDO(makeXLS, makePDF, makeHTML, 118, endDate),					// this is a standard report with banner notes
//				new MakeExpiringDocumentReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeInvoiceRegister(makeXLS, makePDF, makeHTML, divisionId, month, year),   	// this is a standard report with totals
//				new MakeInvoiceRegisterSummaryReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenieDSum(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenieDetailReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenie(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeMonthlyServiceTaxReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeServiceTaxByDayReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakePACDetail("P", makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a compound report. "Which Report" is P|A|C
//				new MakePACListing(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a compound report
//				new MakePACSummary(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a standard report (includd in PAC Listing)
//				new MakePastDue2(makeXLS, makePDF, makeHTML, divisionId, startDate),					// this is a standard report
//				new MakeReportDistribution(makeXLS, makePDF, makeHTML),					// this is a standard report
//				new MakeSubscriptionChangeReport(makeXLS, makePDF, makeHTML, startDate, endDate),			// this is a standard report
//				new MakeTicketStatus(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a standard report
//				new MakeWOandFeesDetail(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),
//				new MakeWOandFeesSummary(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),
//				new MakeWOandFeesReport(makeXLS, makePDF, makeHTML, startDate, endDate),
		};
		super.makeMyReports(reportList);
	}
	
	@Override
	protected String getTestDirectory() {
		return testResultDirectory;
	}

	public static void main(String[] args) {
		try {			
			new DavesReportTester().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

}
