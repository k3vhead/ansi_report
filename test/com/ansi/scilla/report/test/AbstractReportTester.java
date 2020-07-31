package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.accountsReceivable.AccountsReceivableTotalsSummary;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterSummaryReport;
import com.ansi.scilla.report.datadumps.AccountsReceivableTotalsOver60Detail;
import com.ansi.scilla.report.datadumps.ClientContact;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.report.liftAndGenieReport.LiftAndGenieDetailReport;
import com.ansi.scilla.report.liftAndGenieReport.LiftAndGenieDivisionSummary;
import com.ansi.scilla.report.liftAndGenieReport.LiftAndGenieReport;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.pac.PacSummaryReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.report.ReportDistribution;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;
import com.ansi.scilla.report.sixMonthRollingVolume.SmrvReport;
import com.ansi.scilla.report.subscriptions.SubscriptionChangeReport;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;
import com.ansi.scilla.report.ticket.TicketStatusReport;;


public abstract class AbstractReportTester {

	
	
	protected ReportType reportType;
	protected Calendar startDate;
	protected Calendar endDate;
	protected Integer divisionId;
	protected Integer month;
	protected Integer year;
	protected HashMap<String, String> reportDisplay;
	protected Logger logger;

	
	protected abstract String getTestDirectory();	
	
	
	
	protected void makeMyReports(ReportMaker[] reportList) throws Exception {
		this.logger = LogManager.getLogger("com.ansi.scilla.report.reportBuilder");
		logger.info("Start");

		List<Thread> threadList = new ArrayList<Thread>();
		for ( ReportMaker reportMaker : reportList ) {
			threadList.add(new Thread(reportMaker));
		}
		
		for ( Thread thread : threadList ) {
			thread.start();
		}
		
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
		logger.info("Done");		
	}


	
	public abstract class ReportMaker implements Runnable {
		protected boolean makeXLS;
		protected boolean makePDF;
		protected boolean makeHTML;
		
		protected Integer divisionId;
		protected Integer month;
		protected Integer year;
		protected Calendar startDate;
		protected Calendar endDate;

		public ReportMaker(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			this.makeXLS = makeXLS;
			this.makePDF = makePDF;
			this.makeHTML = makeHTML;
		}
		@Override		
		public void run() {
			Connection conn = null;

			try {
				conn = AppUtils.getDevConn();
				conn.setAutoCommit(false);
				makeReport(conn);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			} finally {
				AppUtils.closeQuiet(conn);
			}
		}
		
		public abstract void makeReport(Connection conn) throws Exception;
		
		protected String makeXlsName(String fileName) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
			return getTestDirectory() + fileName + "_" + sdf.format(today) + ".xlsx";
		}
		
		protected String makePdfName(String fileName) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
			return getTestDirectory() + fileName + "_" + sdf.format(today) + ".pdf";
		}
		
		protected String makeHtmlName(String fileName) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
			return getTestDirectory() + fileName + "_" + sdf.format(today) + ".html";
		}

		protected void writeReport(AbstractReport report, String fileName ) throws Exception {
			if ( makeHTML ) { AnsiReportBuilder.writeHTML(report, makeHtmlName(fileName)); }
			if ( makeXLS  ) { AnsiReportBuilder.writeXLS(report, makeXlsName(fileName));   }
			if ( makePDF  ) { AnsiReportBuilder.writePDF(report, makePdfName(fileName));   }
		}
		
		protected void writeReport(CompoundReport report, String fileName) throws Exception {
			if ( makeXLS  ) { AnsiReportBuilder.writeXLS(report, makeXlsName(fileName));   }
			if ( makePDF  ) { AnsiReportBuilder.writePDF(report, makePdfName(fileName));   }
			if ( makeHTML ) { AnsiReportBuilder.writeHTML(report, makeHtmlName(fileName)); }
		}
	}
	
	public class Make6MRV extends ReportMaker {		
		public Make6MRV(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Integer month, Integer year) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.month = month;
			this.year = year;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			String fileName = SmrvReport.FILENAME;
//			SixMonthRollingVolumeReport report = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
			SmrvReport report = SmrvReport.buildReport(conn, divisionId, month, year);
			super.writeReport(report, fileName);
		}
	}

	public class MakeAROver60 extends ReportMaker {
		public MakeAROver60(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			super(makeXLS, makePDF, makeHTML);
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start AROver60");
			String fileName = "AROver60";
			AccountsReceivableTotalsOver60Detail report = AccountsReceivableTotalsOver60Detail.buildReport(conn);
			super.writeReport(report, fileName);
			logger.info("End AROver60");			
		}
		
	}

	public class MakeARTotalsSummary extends ReportMaker {
		public MakeARTotalsSummary(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			super(makeXLS, makePDF, makeHTML);
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start MakeARTotalsSummary");
			String fileName = AccountsReceivableTotalsSummary.FILENAME;
			AccountsReceivableTotalsSummary report = AccountsReceivableTotalsSummary.buildReport(conn);
			super.writeReport(report, fileName);
			logger.info("End MakeARTotalsSummary");			
		}
		
	}

	public class MakeClientContact extends ReportMaker {		
		public MakeClientContact(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			super(makeXLS, makePDF, makeHTML);
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("PastDueReport");
			String fileName = "ClientUsage.xlsx";
			ClientContact report = ClientContact.buildReport(conn);
			super.writeReport(report, fileName);
		}
	}

	
	public class MakeCRRDetail extends ReportMaker {		
		public MakeCRRDetail(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start CRR");
			String fileName = "CRR_DETAIL";
			CashReceiptsRegisterDetailReport report = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End CRR");			
		}
		
	}
	
	
	public class MakeCRRSummary extends ReportMaker {
		
		public MakeCRRSummary(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start CRR Summary");
			String fileName = CashReceiptsRegisterSummaryReport.FILENAME;			
			CashReceiptsRegisterSummaryReport report = CashReceiptsRegisterSummaryReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End CRR Summary");			
		}
		
	}
	

	
	public class MakeDO extends ReportMaker {
		public MakeDO(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start MakeDO");
			String fileName = "DO_Ticket";
			DispatchedOutstandingTicketReport report = DispatchedOutstandingTicketReport.buildReport(conn, divisionId, endDate);
			super.writeReport(report, fileName);
			logger.info("End MakeDO");
		}
		
	}
	
	public class MakeInvoiceRegister extends ReportMaker {
		public MakeInvoiceRegister(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Integer month, Integer year) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.month = month;
			this.year = year;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRR");
			String fileName = "IRR";
			InvoiceRegisterReport report = InvoiceRegisterReport.buildReport(conn, divisionId, month, year);
			super.writeReport(report, fileName);
			logger.info("End IRR");
		}		
	}

	
	public class MakeLiftAndGenieDSum extends ReportMaker {

		public MakeLiftAndGenieDSum(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Lift And Genie Summary");
			String fileName = LiftAndGenieDivisionSummary.FILENAME;			
			LiftAndGenieDivisionSummary report = LiftAndGenieDivisionSummary.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End Lift And Genie Summary");			
		}

	}
	
	public class MakeLiftAndGenieDetailReport extends ReportMaker {

		public MakeLiftAndGenieDetailReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Lift And Genie Summary");
			String fileName = LiftAndGenieDetailReport.FILENAME;			
			LiftAndGenieDetailReport report = LiftAndGenieDetailReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End Lift And Genie Summary");			
		}

	}
	
	public class MakeLiftAndGenie extends ReportMaker {

		public MakeLiftAndGenie(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Lift And Genie Summary");
			String fileName = LiftAndGenieReport.FILENAME;			
			LiftAndGenieReport report = LiftAndGenieReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End Lift And Genie Summary");			
		}

	}
	
	
	public class MakePastDue2 extends ReportMaker {
		public MakePastDue2(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.startDate = startDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("PastDueReport");
			String fileName = "PastDueDate";
			PastDueReport2 report = PastDueReport2.buildReport(conn, startDate, divisionId);
			super.writeReport(report, fileName);
		}
	}

	public class MakePACListing extends ReportMaker {

		public MakePACListing(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start PAC Listing");
			String fileName = "PACListing";
			PacReport report = PacReport.buildReport(conn, divisionId, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End PAC Listing");			
		}
	}
	
	
	
	public class MakePACSummary extends ReportMaker {

		public MakePACSummary(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start PAC Summary");
			String fileName = PacSummaryReport.FILENAME;
			PacSummaryReport report = new PacSummaryReport(conn, divisionId, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End PAC Summary");			
		}
	}
	
	
	
	public class MakeReportDistribution extends ReportMaker {

		public MakeReportDistribution(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			super(makeXLS, makePDF, makeHTML);
		}
		
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Report Distribution");
			String fileName = ReportDistribution.FILENAME;
			ReportDistribution report = ReportDistribution.buildReport(conn);
			super.writeReport(report, fileName);
			logger.info("End Report Distribution");			
		}
	}

	
	public class MakeSubscriptionChangeReport extends ReportMaker {

		public MakeSubscriptionChangeReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Subscription Change Report");
			String fileName = SubscriptionChangeReport.FILENAME;			
			SubscriptionChangeReport report = SubscriptionChangeReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("Subscription Change Report");			
		}

	}
	
	
	public class MakeTicketStatus extends ReportMaker {

		public MakeTicketStatus(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Ticket STatus");
			String fileName = "TicketStatus";
			TicketStatusReport report = TicketStatusReport.buildReport(conn, divisionId, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End Ticket Status");			
		}		
	}


	

	

	



}
