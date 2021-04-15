package com.ansi.scilla.report.report;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;

public class ReportDistributionBySubscriber extends ReportDistributionDetail {

	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Report Distribution By Subscriber";
	public static final String FILENAME = "reportDistributionBySubscriber";
	
	public ReportDistributionBySubscriber(Connection conn) throws Exception {
		super(conn);
		this.setTitle(REPORT_TITLE);
	}
	
	@Override
	protected void makeColumnHeaders() {
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("userName", "Subscriber", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("email","Email", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("reportId", "Report", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("division","Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
		});
		
//		super.setColumnWidths(new ColumnWidth[] {
//		ColumnWidth.DATE,			//Completed
//		ColumnWidth.DATETIME,		// Job ID
//		ColumnWidth.TICKET_NBR,			// Ticket #
//		ColumnWidth.TICKET_STATUS,	// Status
//		ColumnWidth.JOB_PPC,			// PPC
//		ColumnWidth.TICKET_INVOICED,			// Invoiced
//		ColumnWidth.JOB_JOB_NBR,	// Job #
//		ColumnWidth.ADDRESS_NAME,	// Site Name
//		ColumnWidth.ADDRESS_ADDRESS1,	// Site Address (colspan 2)
//		(ColumnWidth)null,			// Site Address
//});	

	}

	@Override
	protected String makeSortBy() {
		return "order by ansi_user.last_name, ansi_user.first_name, report_subscription.report_id";
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return super.makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static ReportDistributionBySubscriber buildReport(Connection conn) throws Exception {		
		return new ReportDistributionBySubscriber(conn);
	}
}
