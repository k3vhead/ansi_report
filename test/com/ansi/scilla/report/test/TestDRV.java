package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.util.List;

import com.ansi.scilla.common.queries.TicketDRVQuery;
import com.ansi.scilla.common.utils.AppUtils;

public class TestDRV {

	public static void main(String[] args) {
		try {
			new TestDRV().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			List<TicketDRVQuery> recordList = TicketDRVQuery.makeMonthlyReport(conn, 3, 2, 2017);
			for ( TicketDRVQuery record : recordList ) {
				System.out.println(record);
			}
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
	}
}
