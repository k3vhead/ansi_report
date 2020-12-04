package com.ansi.scilla.report.invoiceRegisterReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class InvoiceRegisterDivisionSummary extends AbstractInvoiceRegisterSummary implements ReportByStartEnd {

	public static final String FILENAME = "IR_Division_Summary";
	
	
	private static final long serialVersionUID = 1L;

	private final String DIVISION_SUMMARY_SQL = 
			"select \n" + 
			"     concat(division.division_nbr, '-', division.division_code) as name \n" + 
			"    , count(*) as ticket_count\n" + 
			"    , sum(ticket.act_price_per_cleaning) as invoiced_amount \n" + 
			"from ticket \n" + 
			"join division \n" + 
			"  on division.division_id = ticket.act_division_id\n" + 
			"where ticket.invoice_date >= ? and invoice_date < ?\n" + 
			"group by division.division_nbr, division.division_code  \n" + 
			"order by division.division_nbr, division.division_code;\n" ;
	

	
	public static final String REPORT_TITLE = "Division";
	
	
	
	public InvoiceRegisterDivisionSummary() {
		super(REPORT_TITLE);
	}

	protected InvoiceRegisterDivisionSummary(Connection conn) throws Exception {
		super(conn, REPORT_TITLE);


	}

	protected InvoiceRegisterDivisionSummary(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(conn, startDate, endDate, REPORT_TITLE);

	}
	


	
	public static InvoiceRegisterDivisionSummary buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new InvoiceRegisterDivisionSummary(conn, startDate, endDate);
	}
	
	public static InvoiceRegisterDivisionSummary buildReport(Connection conn) throws Exception {
		return new InvoiceRegisterDivisionSummary(conn);
	}

	@Override
	protected String getFile() {
		// TODO Auto-generated method stub
		return FILENAME;
	}

	@Override
	protected String getSql() {
		// TODO Auto-generated method stub
		return DIVISION_SUMMARY_SQL;
	}

}