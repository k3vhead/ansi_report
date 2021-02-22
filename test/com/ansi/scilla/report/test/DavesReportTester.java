package com.ansi.scilla.report.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;

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

		Integer divisionId = 105;
		Integer month = Calendar.OCTOBER;
		Integer year = 2020;
		Calendar startDate = new GregorianCalendar(2020, Calendar.OCTOBER, 1);
		Calendar endDate = new GregorianCalendar(2020, Calendar.DECEMBER, 1);

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
				new MakeCRRSummary(makeXLS, makePDF, makeHTML, startDate, endDate),   		// this is a standard summary
//				new MakeDO(makeXLS, makePDF, makeHTML, divisionId, endDate),					// this is a standard report with banner notes
//				new MakeExpiringDocumentReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeInvoiceRegister(makeXLS, makePDF, makeHTML, divisionId, month, year),   	// this is a standard report with totals
//				new MakeInvoiceRegisterSummaryReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenieDSum(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenieDetailReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenie(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeMonthlyServiceTaxReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeServiceTaxByDayReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakePACDetail("C", makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a compound report
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
