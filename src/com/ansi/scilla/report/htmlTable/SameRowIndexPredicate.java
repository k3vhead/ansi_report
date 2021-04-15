package com.ansi.scilla.report.htmlTable;

import org.apache.commons.collections.Predicate;

public class SameRowIndexPredicate implements Predicate {

	private Integer index;
	public SameRowIndexPredicate(Integer index) {
		this.index = index;
	}
	
	@Override
	public boolean evaluate(Object arg0) {
		HTMLRow row = (HTMLRow)arg0;
		return row.getRowNum().equals(index);
	}
	
}
