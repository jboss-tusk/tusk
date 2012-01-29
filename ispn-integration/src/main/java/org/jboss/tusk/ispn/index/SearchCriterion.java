package org.jboss.tusk.ispn.index;

public class SearchCriterion {
	
	private Operator operator;
	private String field;
	private String value;
	private String auxValue;
	
	public SearchCriterion(String field, String value) {
		this.field = field;
		this.value = value;
		this.operator = Operator.EQUALS;
	}
	
	public SearchCriterion(String field, String value, Operator operator) {
		this.field = field;
		this.value = value;
		this.operator = operator;
	}
	
	public SearchCriterion(String field, String value, String auxValue, Operator operator) {
		this.field = field;
		this.value = value;
		this.auxValue = auxValue;
		this.operator = operator;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public String getAuxValue() {
		return auxValue;
	}

	public void setAuxValue(String auxValue) {
		this.auxValue = auxValue;
	}
	
	

}
