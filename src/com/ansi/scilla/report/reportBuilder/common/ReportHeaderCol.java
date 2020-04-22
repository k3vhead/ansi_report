package com.ansi.scilla.report.reportBuilder.common;

import java.util.List;

import com.ansi.scilla.common.ApplicationObject;

public class ReportHeaderCol extends ApplicationObject implements Comparable<ReportHeaderCol> {

	private static final long serialVersionUID = 1L;

	private List<ReportHeaderRow> rowList;
	private Integer seq;
	public ReportHeaderCol() {
		super();
	}
	public ReportHeaderCol(List<ReportHeaderRow> rowList, Integer seq) {
		super();
		this.rowList = rowList;
		this.seq = seq;
	}
	public List<ReportHeaderRow> getRowList() {
		return rowList;
	}
	public void setRowList(List<ReportHeaderRow> rowList) {
		this.rowList = rowList;
	}
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	
	public Integer size() {
		return rowList.size();
	}
	
	@Override
	public int compareTo(ReportHeaderCol o) {
		return this.seq.compareTo(o.getSeq());
	}
	
	
}
