package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.util.List;

import com.ansi.scilla.common.queries.AddressUsage;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.common.utils.PDFMaker;

public class TestContactUsage extends PDFMaker {


	public static void main(String[] args) {
		try {
			new TestContactUsage().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);

			List<AddressUsage> usageList = AddressUsage.makeList(conn);
			for ( int i = 0; i < 5; i++ ) {
				System.out.println(usageList.get(i));
			}

			conn.rollback();
		} catch ( Exception e) {
			conn.rollback();
			throw e;
		} finally {
			if ( conn == null ) {
				throw new RuntimeException("Null connection");
			}
			conn.close();
		}
	}


	
	
	

	

	
	
	
}
