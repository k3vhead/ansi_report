package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.ansi.scilla.common.db.Ticket;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.utils.AppUtils;

public class TestThreads {

	public static void main(String[] args) {
		new TestThreads().go();
	}
	
	public void go() {
		Date startSingle = new Date();
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);

			List<Ticket> singleTicketList = run(conn);

			Date endSingle = new Date();

			conn.rollback();
			
			Long elapsed = endSingle.getTime() - startSingle.getTime();
			System.out.println("Single elapsed: " + elapsed + "\t" + singleTicketList.size());

		} catch ( Exception e ) {
			AppUtils.rollbackQuiet(conn);
			throw new RuntimeException(e);
		} finally {
			AppUtils.closeQuiet(conn);
		}

		Date startThread = new Date();
		List<Ticket> multiTicketList = new ArrayList<Ticket>();
		Thread dispatched = new Thread(new SelectOptionOne(TicketStatus.DISPATCHED,multiTicketList));
		Thread completed = new Thread(new SelectOptionOne(TicketStatus.COMPLETED,multiTicketList)); 
		dispatched.start();
		completed.start();
		while ( true ) {
			try {
				dispatched.join();
				completed.join();
				break;
			} catch ( InterruptedException e ) {
				System.err.println("Interrupted");
			}
		}
		Date endThread = new Date();
		Long elapsed = endThread.getTime() - startThread.getTime();
		System.out.println("Multi thread: " + elapsed + "\t" + multiTicketList.size());
		
	}
	
	
	public List<Ticket> run(Connection conn) throws Exception {
		System.out.println("Start Single " );
		List<Ticket> ticketList = new ArrayList<Ticket>();
		String sql = "select * from ticket where ticket_status=? or ticket_status=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, TicketStatus.DISPATCHED.code());
		ps.setString(2, TicketStatus.COMPLETED.code());
		ResultSet rs = ps.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		while ( rs.next() ) {
			ticketList.add((Ticket)new Ticket().rs2Object(rsmd, rs));
		}
		rs.close();
		System.out.println("End Single ");
		return ticketList;
	}
	
	
	
	
	public class SelectOptionOne implements Runnable {

		public TicketStatus ticketStatus;
		public List<Ticket> ticketList;
		
		public SelectOptionOne(TicketStatus ticketStatus, List<Ticket> ticketList) {
			super();
			this.ticketStatus = ticketStatus;
			this.ticketList = ticketList;
		}

		@Override
		public void run() {
			System.out.println("Start Thread " + ticketStatus);
			Connection conn = null;
			try {
				conn = AppUtils.getDevConn();
				conn.setAutoCommit(false);
				
				Ticket ticket = new Ticket();
				ticket.setStatus(ticketStatus.code());
				List<Ticket> ticketList = Ticket.cast(ticket.selectSome(conn));
				CollectionUtils.addAll(this.ticketList, ticketList.iterator());
				conn.rollback();
			} catch ( Exception e ) {
				AppUtils.rollbackQuiet(conn);
				throw new RuntimeException(e);
			} finally {
				AppUtils.closeQuiet(conn);
			}
			System.out.println("End Thread " + ticketStatus);
		}
		
	}

}
