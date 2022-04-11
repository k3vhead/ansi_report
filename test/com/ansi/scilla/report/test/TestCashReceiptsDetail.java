package com.ansi.scilla.report.test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;
import com.ansi.scilla.report.ticket.TicketStatusReport;

public class TestCashReceiptsDetail {

	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");

	public static void main(String[] args) {
		try {
			new TestCashReceiptsDetail().go();
//			new TestCashReceiptsDetail().testTwoQuery();
		} catch (Exception e) {
			Calendar now = Calendar.getInstance();
			System.err.println("Exception: " + sdf.format(now.getTime()));
			e.printStackTrace();
		}
	}
	

	private void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);

			Calendar startDate = new GregorianCalendar(2017, Calendar.JULY, 5);
			Calendar endDate = new GregorianCalendar(2017, Calendar.JULY, 5);
			CashReceiptsRegisterDetailReport report = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
			XSSFWorkbook reportXLS = XLSBuilder.build(report);
			//x.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/sixmonth.xlsx"));
			reportXLS.write(new FileOutputStream("cashReceiptsDetail.xlsx"));
		} finally {
			conn.close();
		}
	}

	private void testTwoQuery() throws Exception {
		Date startDate = new Date();
		
		String TWO_QUERY = "this is a stub";
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			PreparedStatement s = conn.prepareStatement(TWO_QUERY);
			
			Thread firstSix = new Thread(new SixMonthsOfVolume(s));
			Thread nextSix = new Thread(new SixMonthsOfVolume(s));
			firstSix.start();
			nextSix.start();
			
			while ( true ) {
				try {
					firstSix.join();
					nextSix.join();
					break;
				} catch ( InterruptedException e) {
					System.err.println("Interrupted");
				}
			}
			
		} finally {
			conn.close();
		}

		Date endDate = new Date();
		Long elapsed = endDate.getTime() - startDate.getTime();
		System.out.println("Double Query: " + "\t"+ elapsed);
	}




	
	
	public class SixMonthsOfVolume implements Runnable {
		private PreparedStatement s;
		
		public SixMonthsOfVolume(PreparedStatement s) {
			this.s = s;
		}

		@Override
		public void run() {
//			Connection conn = null;
			int counter = 0;
			try {
//				conn = AppUtils.getDevConn();
//				Statement s = conn.createStatement();
				ResultSet rs = s.executeQuery();
				while ( rs.next()) {
					counter++;
				}
				rs.close();
				System.out.println(counter);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
//			} finally {
//				AppUtils.closeQuiet(conn);
			}
		}
	}
	
	
	
	


}
