package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.ansi.scilla.common.db.Ticket;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.utils.AppUtils;

public class TestThreadMonthlyVolume {

	final String ONE_QUERY = "select job_site.name as job_site_name " 
			+ "\n\t, job_site.zip " 
			+ "\n\t, job_site.address1 " 
			+ "\n\t, job.job_nbr " 
			+ "\n\t, max(t.start_date) as last_run " 
			+ "\n\t, ppcm01.price_per_cleaning as ppcm01 " 	
			+ "\n\t, ppcm02.price_per_cleaning as ppcm02 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm03 " 
			+ "\n\t, (isnull(ppcm01.price_per_cleaning,0) + isnull(ppcm02.price_per_cleaning,0) + isnull(ppcm03.price_per_cleaning,0)) as ppcq01 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm04 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm05 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm06 " 
			+ "\n\t, (isnull(ppcm04.price_per_cleaning,0) + isnull(ppcm05.price_per_cleaning,0) + isnull(ppcm06.price_per_cleaning,0)) as ppcq02 " 
			+ "\n\t, (isnull(ppcm01.price_per_cleaning,0) + isnull(ppcm02.price_per_cleaning,0) + isnull(ppcm03.price_per_cleaning,0) + isnull(ppcm04.price_per_cleaning,0) + isnull(ppcm05.price_per_cleaning,0) + isnull(ppcm06.price_per_cleaning,0)) as ppch01 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm07 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm08 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm09 " 
			+ "\n\t, (isnull(ppcm07.price_per_cleaning,0) + isnull(ppcm08.price_per_cleaning,0) + isnull(ppcm09.price_per_cleaning,0)) as ppcq03 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm10 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm11 " 
			+ "\n\t, ppcm03.price_per_cleaning as ppcm12 " 
			+ "\n\t, (isnull(ppcm10.price_per_cleaning,0) + isnull(ppcm11.price_per_cleaning,0) + isnull(ppcm12.price_per_cleaning,0)) as ppcq03 " 
			+ "\n\t, (isnull(ppcm07.price_per_cleaning,0) + isnull(ppcm08.price_per_cleaning,0) + isnull(ppcm09.price_per_cleaning,0) + isnull(ppcm10.price_per_cleaning,0) + isnull(ppcm11.price_per_cleaning,0) + isnull(ppcm12.price_per_cleaning,0)) as ppch01 " 
			+ "\nfrom job " 
			+ "\ninner join quote on quote.quote_id = job.quote_id " 
			+ "\ninner join address as job_site on job_site.address_id = quote.job_site_address_id " 
			+ "\ninner join division on division.division_id = job.division_id " 
			+ "\nleft outer join ticket t on ticket_status in ('C','I','P') and ticket_type in ('job','run') and t.job_id = job.job_id " 
			+ "\nleft outer join view_monthly_volume ppcm01 on ppcm01.job_id=job.job_id and ppcm01.job_site_address_id=quote.job_site_address_id and ppcm01.ppc_year=2017 and ppcm01.ppc_month=7 "  
			+ "\nleft outer join view_monthly_volume ppcm02 on ppcm02.job_id=job.job_id and ppcm02.job_site_address_id=quote.job_site_address_id and ppcm02.ppc_year=2017 and ppcm02.ppc_month=8  " 
			+ "\nleft outer join view_monthly_volume ppcm03 on ppcm03.job_id=job.job_id and ppcm03.job_site_address_id=quote.job_site_address_id and ppcm03.ppc_year=2017 and ppcm03.ppc_month=9  " 
			+ "\nleft outer join view_monthly_volume ppcm04 on ppcm04.job_id=job.job_id and ppcm04.job_site_address_id=quote.job_site_address_id and ppcm04.ppc_year=2017 and ppcm04.ppc_month=10  " 
			+ "\nleft outer join view_monthly_volume ppcm05 on ppcm05.job_id=job.job_id and ppcm05.job_site_address_id=quote.job_site_address_id and ppcm05.ppc_year=2017 and ppcm05.ppc_month=11  " 
			+ "\nleft outer join view_monthly_volume ppcm06 on ppcm06.job_id=job.job_id and ppcm06.job_site_address_id=quote.job_site_address_id and ppcm06.ppc_year=2017 and ppcm06.ppc_month=12 " 
			+ "\nleft outer join view_monthly_volume ppcm07 on ppcm07.job_id=job.job_id and ppcm07.job_site_address_id=quote.job_site_address_id and ppcm07.ppc_year=2018 and ppcm07.ppc_month=1 " 
			+ "\nleft outer join view_monthly_volume ppcm08 on ppcm08.job_id=job.job_id and ppcm08.job_site_address_id=quote.job_site_address_id and ppcm08.ppc_year=2018 and ppcm08.ppc_month=2 " 
			+ "\nleft outer join view_monthly_volume ppcm09 on ppcm09.job_id=job.job_id and ppcm09.job_site_address_id=quote.job_site_address_id and ppcm09.ppc_year=2018 and ppcm09.ppc_month=3 " 
			+ "\nleft outer join view_monthly_volume ppcm10 on ppcm10.job_id=job.job_id and ppcm10.job_site_address_id=quote.job_site_address_id and ppcm10.ppc_year=2018 and ppcm10.ppc_month=4 " 
			+ "\nleft outer join view_monthly_volume ppcm11 on ppcm11.job_id=job.job_id and ppcm11.job_site_address_id=quote.job_site_address_id and ppcm11.ppc_year=2018 and ppcm11.ppc_month=5 " 
			+ "\nleft outer join view_monthly_volume ppcm12 on ppcm12.job_id=job.job_id and ppcm12.job_site_address_id=quote.job_site_address_id and ppcm12.ppc_year=2018 and ppcm12.ppc_month=6 " 
			+ "\nwhere division.division_id = 112 " 
			+ "\n\tand job.job_status = 'A'	 " 
			+ "\n\tand (  ppcm01.price_per_cleaning is not null or ppcm02.price_per_cleaning is not null or ppcm03.price_per_cleaning is not null " 
			+ "\n\t\tor ppcm04.price_per_cleaning is not null or ppcm05.price_per_cleaning is not null or ppcm06.price_per_cleaning is not null " 
			+ "\n\t\tor ppcm07.price_per_cleaning is not null or ppcm08.price_per_cleaning is not null or ppcm09.price_per_cleaning is not null " 
			+ "\n\t\tor ppcm10.price_per_cleaning is not null or ppcm11.price_per_cleaning is not null or ppcm12.price_per_cleaning is not null) " 
			+ "\ngroup by job_site.name " 
			+ "\n\t, job_site.zip " 
			+ "\n\t, job_site.address1 " 
			+ "\n\t, job.job_nbr " 
			+ "\n\t, ppcm01.price_per_cleaning " 
			+ "\n\t, ppcm02.price_per_cleaning " 
			+ "\n\t, ppcm03.price_per_cleaning " 
			+ "\n\t, ppcm04.price_per_cleaning " 
			+ "\n\t, ppcm05.price_per_cleaning " 
			+ "\n\t, ppcm06.price_per_cleaning " 
			+ "\n\t, ppcm07.price_per_cleaning " 
			+ "\n\t, ppcm08.price_per_cleaning " 
			+ "\n\t, ppcm09.price_per_cleaning " 
			+ "\n\t, ppcm10.price_per_cleaning " 
			+ "\n\t, ppcm11.price_per_cleaning " 
			+ "\n\t, ppcm12.price_per_cleaning " 
			+ "\norder by job_site_name, job_nbr";
	
	
	final String TWO_QUERY = "select job_site.name as job_site_name "
			+ "\n\t, job_site.zip "
			+ "\n\t, job_site.address1 "
			+ "\n\t, job.job_nbr "
			+ "\n\t, max(t.start_date) as last_run "
			+ "\n\t, ppcm01.price_per_cleaning as ppcm01	 "
			+ "\n\t, ppcm02.price_per_cleaning as ppcm02 "
			+ "\n\t, ppcm03.price_per_cleaning as ppcm03 "
			+ "\n\t, (isnull(ppcm01.price_per_cleaning,0) + isnull(ppcm02.price_per_cleaning,0) + isnull(ppcm03.price_per_cleaning,0)) as ppcq01 "
			+ "\n\t, ppcm03.price_per_cleaning as ppcm04 "
			+ "\n\t, ppcm03.price_per_cleaning as ppcm05 "
			+ "\n\t, ppcm03.price_per_cleaning as ppcm06 "
			+ "\n\t, (isnull(ppcm04.price_per_cleaning,0) + isnull(ppcm05.price_per_cleaning,0) + isnull(ppcm06.price_per_cleaning,0)) as ppcq02 "
			+ "\n\t, (isnull(ppcm01.price_per_cleaning,0) + isnull(ppcm02.price_per_cleaning,0) + isnull(ppcm03.price_per_cleaning,0) + isnull(ppcm04.price_per_cleaning,0) + isnull(ppcm05.price_per_cleaning,0) + isnull(ppcm06.price_per_cleaning,0)) as ppch01 "
			+ "\nfrom job "
			+ "\ninner join quote on quote.quote_id = job.quote_id "
			+ "\ninner join address as job_site on job_site.address_id = quote.job_site_address_id "
			+ "\ninner join division on division.division_id = job.division_id "
			+ "\nleft outer join ticket t on ticket_status in ('C','I','P') and ticket_type in ('job','run') and t.job_id = job.job_id "
			+ "\nleft outer join view_monthly_volume ppcm01 on ppcm01.job_id=job.job_id and ppcm01.job_site_address_id=quote.job_site_address_id and ppcm01.ppc_year=2017 and ppcm01.ppc_month=7  "
			+ "\nleft outer join view_monthly_volume ppcm02 on ppcm02.job_id=job.job_id and ppcm02.job_site_address_id=quote.job_site_address_id and ppcm02.ppc_year=2017 and ppcm02.ppc_month=8  "
			+ "\nleft outer join view_monthly_volume ppcm03 on ppcm03.job_id=job.job_id and ppcm03.job_site_address_id=quote.job_site_address_id and ppcm03.ppc_year=2017 and ppcm03.ppc_month=9  "
			+ "\nleft outer join view_monthly_volume ppcm04 on ppcm04.job_id=job.job_id and ppcm04.job_site_address_id=quote.job_site_address_id and ppcm04.ppc_year=2017 and ppcm04.ppc_month=10  "
			+ "\nleft outer join view_monthly_volume ppcm05 on ppcm05.job_id=job.job_id and ppcm05.job_site_address_id=quote.job_site_address_id and ppcm05.ppc_year=2017 and ppcm05.ppc_month=11  "
			+ "\nleft outer join view_monthly_volume ppcm06 on ppcm06.job_id=job.job_id and ppcm06.job_site_address_id=quote.job_site_address_id and ppcm06.ppc_year=2017 and ppcm06.ppc_month=12 "
			+ "\nwhere division.division_id = 112 "
			+ "\n\tand job.job_status = 'A'	 "
			+ "\n\tand (  ppcm01.price_per_cleaning is not null or ppcm02.price_per_cleaning is not null or ppcm03.price_per_cleaning is not null "
			+ "\n\t\tor ppcm04.price_per_cleaning is not null or ppcm05.price_per_cleaning is not null or ppcm06.price_per_cleaning is not null) "
			+ "\ngroup by job_site.name "
			+ "\n\t, job_site.zip "
			+ "\n\t, job_site.address1 "
			+ "\n\t, job.job_nbr "
			+ "\n\t, ppcm01.price_per_cleaning "
			+ "\n\t, ppcm02.price_per_cleaning "
			+ "\n\t, ppcm03.price_per_cleaning "
			+ "\n\t, ppcm04.price_per_cleaning "
			+ "\n\t, ppcm05.price_per_cleaning "
			+ "\n\t, ppcm06.price_per_cleaning "
			+ "\norder by job_site_name, job_nbr";
	public static void main(String[] args) {
		try {
			for (int i = 0; i < 10; i++ ) {
//				new TestThreadMonthlyVolume().testSingleQuery();
				new TestThreadMonthlyVolume().testTwoQuery();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void testSingleQuery() throws Exception {
		Date startDate = new Date();
		Connection conn = null;
		int counter = 0;
	
		try {
			conn = AppUtils.getDevConn();
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(ONE_QUERY);
			while ( rs.next()) {
				counter++;
			}
			rs.close();
		} finally {
			conn.close();
		}
		Date endDate = new Date();
		Long elapsed = endDate.getTime() - startDate.getTime();
		System.out.println("Single Query: " + counter + "\t"+ elapsed);
	}

	private void testTwoQuery() throws Exception {
		Date startDate = new Date();
		
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
