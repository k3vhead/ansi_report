package com.ansi.scilla.report.test.serviceTaxMaker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeaderExtended;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;

/**
 * Demo code for making sql based on query results, making report Column Headers based on query results
 * and formatting report data from dynamic results. Essentially, if you don't know the number/type of columns
 * at compile time, steal this code.
 * 
 * In this particular case:
 *      We get routinely get monthly data (makeMonthlyDivList). If there are conflicts to resolve, we
 *      get daily data (makeDailyDivList). In either case, we get a list of divisions for which we need
 *      to get service tax data.
 *      
 * Since we don't know the list of divisions until runtime, the RowData object needs to be dynamic. So,
 * we put the results of the query in a HashMap. We then need to create the ColumnHeader array based
 * on the division list (because we have one column per division, and we don't know the list of divisions
 * until runtime).
 * 
 * We have extended the ColumnHeader object to provide a way to get values from the RowData hashmap when
 * that is necessary.
 * 
 *       +++++++++      LOOK AT THE DOC FOR EACH METHOD     ++++++++++++
 *       
 *       
 * @author dclewis
 *
 */
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
			
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
			
			List<String> divList = makeMonthlyDivList(conn, startDate, endDate);
			String monthlySql = makeSql(divList);
			System.out.println(monthlySql);
			
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
			ColumnHeader[] columnHeaders = makeColumnHeaders(divList);
			List<RowData> dataRows = makeDataRows(conn, monthlySql, startDate, endDate);
			for ( RowData dataRow : dataRows) {
				for ( ColumnHeader columnHeader : columnHeaders ) {
					Object displayData = makeDisplayData(columnHeader, dataRow);
					if ( displayData == null ) {
						System.out.println(columnHeader.getFieldName() + "\tNULL NULL NULL");
					} else {
						System.out.println(columnHeader.getFieldName() + "\t" + displayData.getClass().getName() + "\t" + displayData);
					}
					
				}
			}
		} catch ( Exception e) {
			conn.rollback();
			throw e;
		} finally {
			conn.close();
		}
	}
	
	
	/**
	 * When we're trying to resolve conflicts/confusion from the monthly report, we create a daily report. For
	 * the daily report, we want data from all divisions. Use this code to get a list of all divisions.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
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
	 * Returns a list of divisions for which we have collected tax in the date range specified. This is the "routine" process.
	 * 
	 * @param conn
	 * @param startDate
	 * @param endDate
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

	
	/**
	 * For the list of divisions provided, get the tax collected.
	 * 
	 * @param divList
	 * @return
	 */
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

	
	/**
	 * This is just like what we routinely put in a standard report
	 * 
	 * @param conn
	 * @param monthlySql
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private List<RowData> makeDataRows(Connection conn, String monthlySql, Calendar startDate, Calendar endDate) throws SQLException {
		List<RowData> dataRows = new ArrayList<RowData>();
		PreparedStatement ps = conn.prepareStatement(monthlySql);
		ps.setDate(1, new java.sql.Date(startDate.getTime().getTime()));
		ps.setDate(2, new java.sql.Date(endDate.getTime().getTime()));
		ResultSet rs = ps.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		while ( rs.next() ) {
			dataRows.add(new RowData(rs, rsmd));
		}
		rs.close();
		return dataRows;
	}


	/**
	 * This will make a list of column headers based on the list of divisions. "Payment Date" is always there,
	 * so we have a standard column header object. We don't know how many divisions we'll display until runtime,
	 * so we use the extended column header as a way to get to the RowData's hashmap of values.
	 * 
	 * @param divList
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private ColumnHeader[] makeColumnHeaders(List<String> divList) throws NoSuchMethodException, SecurityException {
		Method getterMethod = RowData.class.getMethod("getValues", (Class<?>[])null);
		ColumnHeader[] columnHeaders = new ColumnHeader[divList.size()+1];  // a column for payment date and 1 for each div
		columnHeaders[0] = new ColumnHeader("paymentDate", "Payment Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE);
		for ( int i = 0; i < divList.size(); i++ ) {
			String div = divList.get(i);
			columnHeaders[i+1] = new ColumnHeaderExtended(getterMethod, div, div, 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE);
		}
		return columnHeaders;
	}



	/**
	 * DON'T COPY THIS ONE -- it's here as test code, and has been copied into the ReportBuilder 
	 * 
	 * @param columnHeader
	 * @param row
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	protected Object makeDisplayData(ColumnHeader columnHeader, Object row) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		Object value = null;
		if ( columnHeader instanceof ColumnHeaderExtended ) {
			ColumnHeaderExtended extended = (ColumnHeaderExtended)columnHeader;
			Method getterMethod = extended.getGetterMethod();
			
			HashMap<String, Object> values = (HashMap<String, Object>)getterMethod.invoke(row, (Object[])null);
			if ( values == null ) {
				value = "null values";
			} else {
				String fieldName = extended.getFieldName();
				if ( fieldName == null ) {
					value = "null fieldname";
				} else {
					value = values.get(extended.getFieldName());
				}
				
			}
			
		} else {
			String methodName = "get" + StringUtils.capitalize(columnHeader.getFieldName());
			Method dataMethod = row.getClass().getMethod(methodName, (Class<?>[])null);
			value = dataMethod.invoke(row, (Object[])null);
			if ( value instanceof String && columnHeader.getMaxCharacters() != null ) {
				value = StringUtils.abbreviate((String)value, columnHeader.getMaxCharacters());
			}
			
		}
		return value;
	}
	
	
	
	public static void main(String[] args) {
		try {
			new SqlMaker().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		private java.sql.Date paymentDate;
		private HashMap<String, Double> values = new HashMap<String, Double>();
		
		public RowData(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
			super();
			this.paymentDate = rs.getDate("payment_date");
			for ( int i = 0; i < rsmd.getColumnCount(); i++ ) {
				String columnName = rsmd.getColumnName(i+1);   // because sql starts with 1 and nobody knows why
				if ( ! columnName.equalsIgnoreCase("payment_date") ) {
					Double value = rs.getDouble(columnName);
//					System.out.println("Putting\t" + columnName + "\t" + value);
					values.put("[" + columnName + "]", value);  // wrap with brackets to match "makeDivList()"
				}
			}
		}

		public java.sql.Date getPaymentDate() {
			return paymentDate;
		}

		public void setPaymentDate(java.sql.Date paymentDate) {
			this.paymentDate = paymentDate;
		}

		public HashMap<String, Double> getValues() {
			return values;
		}

		public void setValues(HashMap<String, Double> values) {
			this.values = values;
		}
		
	}
}
