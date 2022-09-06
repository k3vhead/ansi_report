package com.ansi.scilla.report.htmlTable;

import org.apache.commons.collections4.Predicate;

public class SameRowIndexPredicate implements Predicate<HTMLRow> {

	private Integer index;
	public SameRowIndexPredicate(Integer index) {
		this.index = index;
	}
	
	@Override
	public boolean evaluate(HTMLRow row) {
		return row.getRowNum().equals(index);
	}
	
}
