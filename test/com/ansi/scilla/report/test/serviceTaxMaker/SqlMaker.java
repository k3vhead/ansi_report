package com.ansi.scilla.report.test.serviceTaxMaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			List<String> divList = makeDivList(conn);
			String sql = makeSql(divList);
			System.out.println(sql);
		} catch ( Exception e) {
			conn.rollback();
			throw e;
		} finally {
			conn.close();
		}
	}
	
	/**
	 * Returns a list of divisions that have jobs for which we are collecting tax (ie tax_exempt flag is zero)
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private List<String> makeDivList(Connection conn) throws SQLException {
		String sql = "select distinct job.division_id, concat(division.division_nbr, '-',division.division_code ) as div\n" + 
				"from job\n" + 
				"inner join division on division.division_id=job.division_id\n" + 
				"where job.tax_exempt=0\n" + 
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
