package parser.ast;

public class IntNode implements ValueNode { 
	private Integer value;

	public IntNode(String text) {
		value = new Integer(text);
	}

	public Integer getValue() { return value; }

	@Override
	public String toString(){
		return value.toString();
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof IntNode && value.equals(((IntNode) o).getValue());
	}
}
