package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.AppUtils;;


public class JoshuasReportTester extends AbstractReportTester {

	private final String joshuasTestResultDirectory = "/home/jwlewis/Documents/xlsWorks";
	
	
	public static void main(String[] args) {
		try {
			new JoshuasReportTester().makeMyReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makeMyReport() throws Exception {
		this.logger = LogManager.getLogger("com.ansi.scilla.report.reportBuilder");
		logger.info("Start");
		Connection conn = null;
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			
			this.divisionId = 101;
			this.month=07;
			this.year=2018;
			this.startDate = new Midnight(2017, Calendar.JULY, 20);
			this.endDate = new Midnight(2018, Calendar.SEPTEMBER, 31);
			
			boolean makeXLS = true;
			boolean makePDF = false;
			boolean makeHTML = true;
			
			ReportMaker[] reportList = new ReportMaker[] {				
//					new Make6MRV(makeXLS, makePDF, makeHTML, divisionId, month, year),		// this is a custom report
//					new MakeAROver60(makeXLS, makePDF, makeHTML),							// this is a datadump
//					new MakeClientUsage(makeXLS, makePDF, makeHTML, divisionId, startDate),
//					new MakeCRRDetail(makeXLS, makePDF, makeHTML, startDate, endDate),
//					new MakeCRRSummary(makeXLS, makePDF, makeHTML, startDate, endDate),   		// this is a standard summary
//					new MakeDO(makeXLS, makePDF, makeHTML, divisionId, endDate),					// this is a standard report with banner notes
//					new MakeInvoiceRegister(makeXLS, makePDF, makeHTML, divisionId, month, year),   	// this is a standard report with totals
//					new MakePACListing(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),			// this is a compound report
//					new makePastDue2(makeXLS, makePDF, makeHTML, divisionId, startDate),	
//					new MakeTicketStatus(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),
//					new MakeWOandFeesSummary(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),
					new MakeWOandFeesDetail(makeXLS, makePDF, makeHTML, divisionId, startDate, endDate),
			};
			super.makeMyReports(reportList);
			
			conn.rollback();
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
		logger.info("Done");		
	}


	@Override
	protected String getTestDirectory() {
		return joshuasTestResultDirectory;
	}

	



}
