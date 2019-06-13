package parser.ast;

public class StringNode implements ValueNode {
    private String value;

    public StringNode(String text) {
        value = text;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return '"' + getValue() + '"';
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof StringNode && value.equals(((StringNode) o).getValue());
    }
}
