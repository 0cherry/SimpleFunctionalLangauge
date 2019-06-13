package parser.ast;

public class IdNode implements ValueNode {
    private String idString;

    public IdNode(String text) {
        idString = text;
    }

    public String getIdString() {
        return idString;
    }

    @Override
    public String toString() {
        return idString;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof IdNode && idString.equals(((IdNode) o).getIdString());
    }
}
