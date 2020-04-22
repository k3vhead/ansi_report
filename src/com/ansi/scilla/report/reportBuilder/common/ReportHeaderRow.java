package com.ansi.scilla.report.reportBuilder.common;

import java.lang.reflect.Method;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;

public class ReportHeaderRow extends ApplicationObject implements Comparable<ReportHeaderRow> {

	private static final long serialVersionUID = 1L;

	private String label;
	private Method value;
	private Integer seq;
	private DataFormats formatter;
	
	public ReportHeaderRow() {
		super();
	}
	public ReportHeaderRow(String label, Method value, Integer seq, DataFormats formatter) {
		this();
		this.label = label;
		this.value = value;
		this.seq = seq;
		this.formatter = formatter;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Method getValue() {
		return value;
	}
	public void setValue(Method value) {
		this.value = value;
	}
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	
	public DataFormats getFormatter() {
		return formatter;
	}
	public void setFormatter(DataFormats formatter) {
		this.formatter = formatter;
	}
	@Override
	public int compareTo(ReportHeaderRow o) {
		return this.seq.compareTo(o.getSeq());
	}
	
	
	
	
}
