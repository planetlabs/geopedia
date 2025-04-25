package com.sinergise.generics.core.filter;

import java.util.ArrayList;
import java.util.Collection;

public class ComplexFilterValue {
	public String value = null;
	public Collection<ComplexFilterValue> cfvCollection = null;
	
	
	JoinOperator joinOperator = JoinOperator.NONE;
	MatchOperator matchOperator = MatchOperator.NONE;
	
	public enum JoinOperator{OR,AND,NONE}
	public enum MatchOperator{LESS,GREATER,EQUAL,NONE}
	
	
	public ComplexFilterValue(String value, MatchOperator matchOperator, JoinOperator joinOperator) {
		this.value = value;
		this.joinOperator = joinOperator;
		this.matchOperator = matchOperator;
		cfvCollection = null;
	}
	
	public ComplexFilterValue(Collection<ComplexFilterValue> cfvCollection, JoinOperator joinOperator) {
		value = null;
		matchOperator = MatchOperator.NONE;
		this.cfvCollection = cfvCollection;
		this.joinOperator = joinOperator;
	}
	
	
	public static MatchOperator getMatchOperator (String str) {
		String trimmed = str.trim();
		if (trimmed.startsWith("<"))
			return MatchOperator.LESS;
		else if (trimmed.startsWith(">"))
			return MatchOperator.GREATER;
		else if (trimmed.startsWith("="))
			return MatchOperator.EQUAL;
		else
			return MatchOperator.NONE;
	}
	
	/**
	 * Dissects complex filter value
	 * 
	 * TODO: throw exception on errors, add support for parentheses
	 * @param value
	 * @return
	 */
	public static Collection<ComplexFilterValue> dissect(String value){
		ArrayList<ComplexFilterValue> dissected = new ArrayList<ComplexFilterValue>();
		
		String [] ranges;
		JoinOperator joinOperator=JoinOperator.NONE;
		if (value.contains("&")) { 
			ranges = value.split("&");
			joinOperator=JoinOperator.AND;
		} else if (value.contains("|")) {
			ranges = value.split("\\|");
			joinOperator=JoinOperator.OR;
		} else  {
			ranges = new String[1];
			ranges[0]=value;
		}
		
		for (int i=0;i<ranges.length;i++) {
			String valToParse = ranges[i].trim();
			MatchOperator mo = getMatchOperator(valToParse);
			if (mo !=MatchOperator.NONE) {
				valToParse=valToParse.substring(1);
				valToParse=valToParse.trim();
			}
			JoinOperator jo = joinOperator;
			if (i==0) {
				jo=JoinOperator.NONE;
			}
			
			dissected.add(new ComplexFilterValue(valToParse,mo,jo));
		}
		return dissected;
	}
	
	public static String build(Collection<ComplexFilterValue> values) {
		String value="";
		if (values==null || values.size()==0)
			return value;
		for (ComplexFilterValue v:values) {
			if (v.joinOperator==JoinOperator.AND)
				value+="&";
			else if (v.joinOperator==JoinOperator.OR)
				value+="|";
			
			if (v.matchOperator==MatchOperator.EQUAL)
				value+="=";
			else if (v.matchOperator==MatchOperator.LESS)
				value+="<";
			else if (v.matchOperator==MatchOperator.GREATER)
				value+=">";
			
			if (v.value!=null)
				value+=v.value;
		}
		return value;
	}
	
	public static void main (String [] args) {
		String str = "133.5";
		Collection<ComplexFilterValue> vls = dissect(str);
	
		System.out.println(build(vls));
		for (ComplexFilterValue c:vls) {
			System.out.println(c.value+" "+c.matchOperator+" "+c.joinOperator);
		}
	}
}
