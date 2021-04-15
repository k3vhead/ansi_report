package com.ansi.scilla.report.report;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public abstract class ReportDistributionDetail extends StandardReport implements ReportByNoInput {

	private static final long serialVersionUID = 1L;


	private List<RowData> data;
	
	
	protected ReportDistributionDetail(Connection conn) throws Exception {
		super();
		this.data = makeData(conn);				
		makeReport(data);
	}
	
	
	private List<RowData> makeData(Connection conn) throws SQLException {
		String sql = "select report_subscription.user_id, report_subscription.report_id, report_subscription.division_id,\n" + 
				"		ansi_user.first_name, ansi_user.last_name, ansi_user.email,\n" + 
				"		CASE \n" + 
				"			when division.division_nbr is null then null\n" + 
				"			else concat(division.division_nbr, '-', division.division_code)\n" + 
				"		end as div\n" + 
				"from report_subscription\n" + 
				"inner join ansi_user on ansi_user.user_id=report_subscription.user_id\n" + 
				"left outer join division on division.division_id=report_subscription.division_id\n" + 
				makeSortBy();
				
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sql);
		List<RowData> data = new ArrayList<RowData>();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		return data;
	}
	

	private void makeReport(List<RowData> data) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Calendar today = Calendar.getInstance();
		String subtitle = "As of " + sdf.format(today.getTime());
//		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		makeColumnHeaders();
		
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		super.makeHeaderLeft(new ArrayList<ReportHeaderRow>());
		super.makeHeaderRight(new ArrayList<ReportHeaderRow>());
	}

	
	protected abstract void makeColumnHeaders();
	protected abstract String makeSortBy();

	
	


	
}
