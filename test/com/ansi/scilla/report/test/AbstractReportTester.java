package com.ansi.scilla.report.test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.accountsReceivable.AccountsReceivableTotals;
import com.ansi.scilla.report.accountsReceivable.AccountsReceivableTotalsByDivision;
import com.ansi.scilla.report.accountsReceivable.AccountsReceivableTotalsSummary;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterSummaryReport;
import com.ansi.scilla.report.creditCardFees.CreditCardFeesByDayReport;
import com.ansi.scilla.report.creditCardFees.CreditCardFeesSummaryReport;
import com.ansi.scilla.report.datadumps.AccountsReceivableTotalsOver60Detail;
import com.ansi.scilla.report.datadumps.ClientContact;
import com.ansi.scilla.report.expiringDocumentReport.ExpiringDocumentReport;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterCompanySummary;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterDivisionSummary;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterRegionSummary;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterSummaryReport;
import com.ansi.scilla.report.liftAndGenieReport.LiftAndGenieDetailReport;
import com.ansi.scilla.report.liftAndGenieReport.LiftAndGenieDivisionSummary;
import com.ansi.scilla.report.liftAndGenieReport.LiftAndGenieReport;
import com.ansi.scilla.report.monthlyServiceTaxReport.MonthlyServiceTax;
import com.ansi.scilla.report.monthlyServiceTaxReport.MonthlyServiceTaxByDayReport;
import com.ansi.scilla.report.monthlyServiceTaxReport.MonthlyServiceTaxReport;
import com.ansi.scilla.report.pac.PacActivationListReport;
import com.ansi.scilla.report.pac.PacCancelledListReport;
import com.ansi.scilla.report.pac.PacDetailReport;
import com.ansi.scilla.report.pac.PacProposedListReport;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.pac.PacSummaryReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;
import com.ansi.scilla.report.reportDistribution.ReportDistribution;
import com.ansi.scilla.report.sixMonthRollingVolume.SmrvReport;
import com.ansi.scilla.report.skippedAndDispatched.SkippedAndDispatchedReport;
import com.ansi.scilla.report.subscriptions.SubscriptionChangeReport;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;
import com.ansi.scilla.report.ticket.TicketStatusReport;
import com.ansi.scilla.report.woAndFees.WOAndFeesDetailReport;
import com.ansi.scilla.report.woAndFees.WOAndFeesReport;
import com.ansi.scilla.report.woAndFees.WOAndFeesSummaryReport;;


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
		private ReportConn reportConn = ReportConn.PROD;

		protected boolean makeXLS;
		protected boolean makePDF;
		protected boolean makeHTML;
		
		protected Integer divisionId;
		protected Integer month;
		protected Integer year;
		protected Calendar startDate;
		protected Calendar endDate;

		public ReportMaker(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			super();
			this.makeXLS = makeXLS;
			this.makePDF = makePDF;
			this.makeHTML = makeHTML;
		}
		
		public ReportConn getReportConn() {
			return reportConn;
		}

		public void setReportConn(ReportConn reportConn) {
			this.reportConn = reportConn;
		}

		@Override		
		public void run() {
			Connection conn = null;

			try {
//				conn = AppUtils.getDevConn();
//				conn = this.reportConn.getConn();
				conn =  DriverManager.getConnection(
						"jdbc:sqlserver://192.168.10.100:1433;databaseName=asap;integratedSecurity=false;", 
						"ansi_sched.webapp", 
						"@Ansi2021");
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

	public class MakeARTotals extends ReportMaker {
		public MakeARTotals(boolean makeXLS, boolean makePDF, boolean makeHTML) {
			super(makeXLS, makePDF, makeHTML);
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start End AR TotalBy Div");
			String fileName = AccountsReceivableTotals.FILENAME;
			AccountsReceivableTotals report = AccountsReceivableTotals.buildReport(conn);
			super.writeReport(report, fileName);
			logger.info("End AR TotalBy Div");			
		}
		
	}


	public class MakeARTotalByDiv extends ReportMaker {
		public MakeARTotalByDiv(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start End AR TotalBy Div");
			String fileName = AccountsReceivableTotalsByDivision.FILENAME;
			AccountsReceivableTotalsByDivision report = AccountsReceivableTotalsByDivision.buildReport(conn, divisionId);
			super.writeReport(report, fileName);
			logger.info("End AR TotalBy Div");			
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
	
	
	public class MakeCreditCardFeesSummaryReport extends ReportMaker {		
		public MakeCreditCardFeesSummaryReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("CreditCardSummary");
			String fileName = CreditCardFeesSummaryReport.FILENAME;
			CreditCardFeesSummaryReport report = CreditCardFeesSummaryReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
		}
	}
	
	public class MakeCreditCardFeesByDayReport extends ReportMaker {		
		public MakeCreditCardFeesByDayReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("CreditCardByDay");
			String fileName = CreditCardFeesSummaryReport.FILENAME;
			CreditCardFeesByDayReport report = CreditCardFeesByDayReport.buildReport(conn, startDate, endDate);
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
			String fileName = "DO_Ticket_"+divisionId;
			DispatchedOutstandingTicketReport report = DispatchedOutstandingTicketReport.buildReport(conn, divisionId, endDate);
			super.writeReport(report, fileName);
			logger.info("End MakeDO");
		}
		
	}
	public class MakeExpiringDocumentReport extends ReportMaker {
		
		public MakeExpiringDocumentReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Expiring Document Report");
			String fileName = ExpiringDocumentReport.FILENAME;			
			ExpiringDocumentReport report = ExpiringDocumentReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End Expiring Document Report");			
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
	
	public class MakeInvoiceRegisterDivisionSummary extends ReportMaker {
		public MakeInvoiceRegisterDivisionSummary(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRR");
			String fileName = "IRR";
			InvoiceRegisterDivisionSummary report = InvoiceRegisterDivisionSummary.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End IRR");
		}		
	}
	public class MakeInvoiceRegisterCompanySummary extends ReportMaker {
		public MakeInvoiceRegisterCompanySummary(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRR");
			String fileName = "IRR";
			InvoiceRegisterCompanySummary report = InvoiceRegisterCompanySummary.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End IRR");
		}		
	}
	public class MakeInvoiceRegisterRegionSummary extends ReportMaker {
		public MakeInvoiceRegisterRegionSummary(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRR");
			String fileName = "IRR";
			InvoiceRegisterRegionSummary report = InvoiceRegisterRegionSummary.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End IRR");
		}		
	}
	
	public class MakeInvoiceRegisterSummaryReport extends ReportMaker {
		public MakeInvoiceRegisterSummaryReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRRS");
			String fileName = InvoiceRegisterSummaryReport.FILENAME;
			InvoiceRegisterSummaryReport report = InvoiceRegisterSummaryReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("End IRRS");
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
	
	public class MakeMonthlyServiceTax extends ReportMaker {
		
		public MakeMonthlyServiceTax(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Monthly Service Tax");
			String fileName = MonthlyServiceTax.FILENAME;			
			MonthlyServiceTax report = MonthlyServiceTax.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("Monthly Service Tax");			
		}
	
	}
	
	public class MakeMonthlyServiceTaxReport extends ReportMaker {
	
		public MakeMonthlyServiceTaxReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Monthly Service Tax Report");
			String fileName = MonthlyServiceTaxReport.FILENAME;			
			MonthlyServiceTaxReport report = MonthlyServiceTaxReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("Monthly Service Tax Report");			
		}
	
	}
	
	
	public class MakeServiceTaxByDayReport extends ReportMaker {
		
		public MakeServiceTaxByDayReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}
	
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Monthly Service Tax Report");
			String fileName = MonthlyServiceTaxByDayReport.FILENAME;			
			MonthlyServiceTaxByDayReport report = MonthlyServiceTaxByDayReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
			logger.info("Monthly Service Tax Report");			
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
	
	
	public class MakePACDetail extends ReportMaker {
		public String whichReport;		
		public MakePACDetail(String whichReport, boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.whichReport = whichReport;
			this.divisionId = divisionId;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start PAC Listing");
			String fileName = "PACListing";
			PacDetailReport report = null;
			switch ( this.whichReport ) {
			case "P":
				report = new PacProposedListReport(conn, divisionId, startDate, endDate);
				break;
			case "A":
				report = new PacActivationListReport(conn, divisionId, startDate, endDate);
				break;
			case "C":
				report = new PacCancelledListReport(conn, divisionId, startDate, endDate);
				break;
			default:
				throw new Exception("which report must be P, A or C");					
			}
			super.writeReport(report, fileName);
			logger.info("End PAC Listing");			
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
	
	public class MakeSkippedAndDispatchedReport extends ReportMaker {

		public MakeSkippedAndDispatchedReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
			this.divisionId = divisionId;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Skipped and Dispatched Report");
			String fileName = SkippedAndDispatchedReport.FILENAME;			
			SkippedAndDispatchedReport report = SkippedAndDispatchedReport.buildReport(conn, divisionId, startDate.get(Calendar.MONTH), startDate.get(Calendar.YEAR));
			super.writeReport(report, fileName);
			logger.info("Skipped And Dispatched Report");			
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


	public class MakeWOandFeesDetail extends ReportMaker {		
		public MakeWOandFeesDetail(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			String fileName = WOAndFeesDetailReport.FILENAME;
//			SixMonthRollingVolumeReport report = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
			WOAndFeesDetailReport report = WOAndFeesDetailReport.buildReport(conn, divisionId, startDate, endDate);
			super.writeReport(report, fileName);
		}

	}
	
	public class MakeWOandFeesSummary extends ReportMaker {		
		public MakeWOandFeesSummary(boolean makeXLS, boolean makePDF, boolean makeHTML, Integer divisionId, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.divisionId = divisionId;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			String fileName = WOAndFeesSummaryReport.FILENAME;
//			SixMonthRollingVolumeReport report = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
			WOAndFeesSummaryReport report = WOAndFeesSummaryReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
		}
	}
	
	public class MakeWOandFeesReport extends ReportMaker {		
		public MakeWOandFeesReport(boolean makeXLS, boolean makePDF, boolean makeHTML, Calendar startDate, Calendar endDate) {
			super(makeXLS, makePDF, makeHTML);
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public void makeReport(Connection conn) throws Exception {
			String fileName = WOAndFeesReport.FILENAME;
			WOAndFeesReport report = WOAndFeesReport.buildReport(conn, startDate, endDate);
			super.writeReport(report, fileName);
		}

	}

	

	public enum ReportConn {
		DEV("getDevConn"),
		PROD("getProdConn"),
		;
		private String methodName;
		private ReportConn(String methodName) { this.methodName = methodName; }
		public Connection getConn() throws Exception {
			Method method = AppUtils.class.getMethod(this.methodName, (Class<?>[])null);
			Object o = method.invoke(null, (Object[])null);
			return (Connection)o;
		}
		
	}



}
