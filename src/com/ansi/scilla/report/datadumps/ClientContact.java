package com.ansi.scilla.report.datadumps;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.DataDumpReport;

public class ClientContact extends DataDumpReport implements ReportByNoInput {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Client Contact";
	
	public ClientContact(Connection conn) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		makeReport(conn);
	}
	
	public static final  String REPORT_TITLE = "Client Contact";

	protected static final String sql = "select contact.contact_id, contact.last_name, contact.first_name, contact.mobile_phone, contact.fax, contact.business_phone, contact.email, "
			+ "\n\tsum(counter.quote_count) as quote_count, sum(counter.job_count) as job_count "
			+ "\nfrom contact "
			+ "\nleft outer join (select contact.contact_id, count(quote.quote_id) as quote_count, 0 as job_count "
			+ "\nfrom contact "
			+ "\ninner join quote on quote.signed_by_contact_id=contact.contact_id "
			+ "\ngroup by contact.contact_id "
			+ "\nunion "
			+ "\nselect contact.contact_id, 0 as quote_count, count(job.job_id) as job_count "
			+ "\nfrom contact "
			+ "\ninner join job on job.billing_contact_id=contact.contact_id " 
			+ "\n\tor job.site_contact = contact.contact_id  "
			+ "\n\tor job.job_contact_id = contact.contact_id "
			+ "\n\tor job.contract_contact_id = contact.contact_id "
			+ "\ngroup by contact.contact_id) counter "
			+ "\non counter.contact_id=contact.contact_id "
			+ "\ngroup by contact.contact_id, contact.last_name, contact.first_name, contact.mobile_phone, contact.fax, contact.business_phone, contact.email "
			+ "\norder by last_name, first_name";

	

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	public static ClientContact buildReport(Connection conn) throws Exception {
		return new ClientContact(conn);
	}
	
}
