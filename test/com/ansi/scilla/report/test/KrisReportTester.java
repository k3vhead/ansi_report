package com.ansi.scilla.report.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;;


public class KrisReportTester extends AbstractReportTester {

	private final String testResultDirectory = "/home/klandeck/Documents/testr/";
	
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

		Integer divisionId = 101;
		Integer month = Calendar.JULY;
		Integer year = 2019;
		Calendar startDate = new GregorianCalendar(2020, Calendar.FEBRUARY, 1);
		Calendar endDate = new GregorianCalendar(2020, Calendar.FEBRUARY, 28);

		ReportMaker[] reportList = new ReportMaker[] {				
//				new Make6MRV(makeXLS, makePDF, makeHTML, divisionId, month, year),		// this is a custom report
//				new MakeAROver60(makeXLS, makePDF, makeHTML),							// this is a datadump
//				new MakeClientContact(makeXLS, makePDF, makeHTML),						// this is a datadump
//				new MakeCRRDetail(makeXLS, makePDF, makeHTML, startDate, endDate),		// this is a standard report with subtotals
				new MakeCRRSummary(makeXLS, makePDF, makeHTML, startDate, endDate),   		// this is a standard summary
//				new MakeDO(makeXLS, makePDF, makeHTML, divisionId, endDate),					// this is a standard report with banner notes
//				new MakeInvoiceRegister(makeXLS, makePDF, makeHTML, divisionId, month, year),   	// this is a standard report with totals
//				new MakePACListing(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a compound report
//				new makePastDue2(makeXLS, makePDF, makeHTML, divisionId, startDate),					// this is a standard report
//				new MakeTicketStatus(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a standard report
//				new MakeLiftAndGenieDSum(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenieDetailReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeLiftAndGenie(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeMonthlyServiceTaxReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeServiceTaxByDayReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeMonthlyServiceTax(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeExpiringDocumentReport(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeInvoiceRegisterDivisionSummary(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeInvoiceRegisterCompanySummary(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeInvoiceRegisterRegionSummary(makeXLS, makePDF, makeHTML, startDate, endDate),
//				new MakeInvoiceRegisterSummaryReport(makeXLS, makePDF, makeHTML, startDate, endDate),
		};
		super.makeMyReports(reportList);
		
		
		
	}
	
	public static void main(String[] args) {
		try {			
			new KrisReportTester().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	


	@Override
	protected String getTestDirectory() {
		return testResultDirectory;
	}

	
}
