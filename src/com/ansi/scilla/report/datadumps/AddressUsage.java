package com.ansi.scilla.report.datadumps;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.DataDumpReport;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;

public class AddressUsage extends DataDumpReport implements ReportByNoInput {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Address Usage";
	
	public AddressUsage(Connection conn) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		makeReport(conn);
	}
	
	public static final  String REPORT_TITLE = "Address Usage";

	protected static final String sql = "select address.address_id, address.name, address.address1, address.address2, address.city, address.state, address.zip, address.country_code, " 
			+ "\ncount(quote_id) as quote_count " 
			+ "\nfrom address " 
			+ "\nleft outer join quote on quote.job_site_address_id=address.address_id or quote.bill_to_address_id=address.address_id " 
			+ "\ngroup by address.address_id, address.name, address.address1, address.address2, address.city, address.state, address.zip, address.country_code "
			+ "\norder by address.name";

	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	

	public static AddressUsage buildReport(Connection conn) throws Exception {
		return new AddressUsage(conn);
	}

	
	
}
