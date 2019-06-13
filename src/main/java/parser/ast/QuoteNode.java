package parser.ast;

public class QuoteNode implements Node {

	private Node quoted;
	
	public QuoteNode(Node quoted) {
		this.quoted = quoted;
	}
	
	
	@Override
	public String toString(){
		return "'" + quoted.toString();
	}


	public Node nodeInside() {
		// TODO Auto-generated method stub
		return quoted;
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof QuoteNode && quoted.equals(((QuoteNode) o).nodeInside());
	}
}
