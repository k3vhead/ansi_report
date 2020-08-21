package com.ansi.scilla.report.test.serviceTaxMaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.common.utils.AppUtils;

public class SqlMaker {

	/*
		This is the sql we're trying to get to:
		
		select payment_date, 
				isnull([65-OH05],0.00) as [65-OH05], 
				isnull([66-OH06],0.00) as [66-OH06],
				isnull([67-OH07],0.00) as [67-OH07], 
				isnull([71-PA01],0.00) as [71-PA01], 
				isnull([77-CL07],0.00) as [77-CL07], 
				isnull([81-TN01],0.00) as [81-TN01]
		from 
			(select tax_amt, concat(division_nbr,'-',division_code) as div, payment_date 
				from ticket_payment
				join ticket on ticket.ticket_id = ticket_payment.ticket_id
				join payment on payment.payment_id = ticket_payment.payment_id 
				join division on ticket.act_division_id = division.division_id) as sourceTable
		PIVOT
			(sum(tax_amt)
				for div in ([65-OH05], [66-OH06], [67-OH07], [71-PA01], [77-CL07], [81-TN01])
			) as pivotTable
		where payment_date >= ? and payment_date <= ?
		order by payment_date


	 */
	
	
	private void go() throws Exception {
		Connection conn = null;
		Calendar startDate = new GregorianCalendar(2020, Calendar.JANUARY, 1);
		Calendar endDate = new GregorianCalendar(2020, Calendar.MARCH, 31);
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			List<String> allDivList = makeDailyDivList(conn);
			String dailySql = makeSql(allDivList);
			System.out.println(dailySql);
			
			
			List<String> divList = makeMonthlyDivList(conn, startDate, endDate);
			String monthlySql = makeSql(divList);
			System.out.println(monthlySql);
		} catch ( Exception e) {
			conn.rollback();
			throw e;
		} finally {
			conn.close();
		}
	}
	
	
	private List<String> makeDailyDivList(Connection conn) throws SQLException {
		String sql = "select division.division_id, concat(division.division_nbr, '-',division.division_code ) as div\n" + 
				"from division \n" + 
				"where division.division_status=1\n" + 
				"order by concat(division.division_nbr, '-',division.division_code )";
		
		List<String> divList = new ArrayList<String>();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sql);
		while ( rs.next() ) {
			divList.add("[" + rs.getString("div") + "]");
		}
		rs.close();
		return divList;
	}
	/**
	 * Returns a list of divisions that have jobs for which we are collecting tax (ie tax_exempt flag is zero)
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private List<String> makeMonthlyDivList(Connection conn, Calendar startDate, Calendar endDate) throws SQLException {
		String sql = "select distinct division.division_id, concat(division.division_nbr, '-',division.division_code ) as div\n" + 
				"from ticket_payment\n" + 
				"inner join ticket on ticket.ticket_id=ticket_payment.ticket_id\n" + 
				"inner join payment on payment.payment_id=ticket_payment.payment_id and payment_date >= ? and payment_date <= ?\n" + 
				"inner join division on division.division_id=ticket.act_division_id\n" + 
				"where tax_amt > 0\n" + 
				"order by concat(division.division_nbr, '-',division.division_code )";
		
		List<String> divList = new ArrayList<String>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTime().getTime()));
		ps.setDate(2, new java.sql.Date(endDate.getTime().getTime()));
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			divList.add("[" + rs.getString("div") + "]");
		}
		rs.close();
		return divList;
	}

	private String makeSql(List<String> divList) {
		StringBuffer sql = new StringBuffer();
		List<String> selectors = new ArrayList<String>();
		selectors.add("select payment_date");
		for ( String div : divList ) {
			selectors.add("\tisnull(" + div + ",0.00) as " + div);
		}
		sql.append( StringUtils.join(selectors, ",\n"));
		
		sql.append("\nfrom \n" + 
				"			(select tax_amt, concat(division_nbr,'-',division_code) as div, payment_date \n" + 
				"				from ticket_payment\n" + 
				"				join ticket on ticket.ticket_id = ticket_payment.ticket_id\n" + 
				"				join payment on payment.payment_id = ticket_payment.payment_id \n" + 
				"				join division on ticket.act_division_id = division.division_id) as sourceTable\n" + 
				"		PIVOT\n" + 
				"			(sum(tax_amt)\n" + 
				"				for div in (");
		sql.append(StringUtils.join(divList, ","));
		sql.append(")\n" + 
				"			) as pivotTable\n" + 
				"		where payment_date >= ? and payment_date <= ?\n" + 
				"		order by payment_date");
		
		return sql.toString();
	}

	public static void main(String[] args) {
		try {
			new SqlMaker().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
