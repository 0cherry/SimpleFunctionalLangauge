package interpreter;

import parser.ast.ListNode;
import parser.ast.Node;
import parser.ast.QuoteNode;

import java.io.PrintStream;
public class OriginalPrettyPrinter {
    private PrintStream ps;
    private StringBuffer output;
    
    public static OriginalPrettyPrinter getPrinter(PrintStream ps) {
        return new OriginalPrettyPrinter(ps);
    }

    private OriginalPrettyPrinter(PrintStream ps) {
        this.ps = ps;
        this.output = new StringBuffer();
    }
    
    private void printNode(ListNode listNode) {     
        if (listNode == ListNode.EMPTYLIST) {
            return;
        }
        if (listNode == ListNode.ENDLIST) {
			return;
		}
//        ps.print(" ");
        output.append(' ');
        printNode(listNode.car());
//        ps.print(" ");
        output.append(' ');
        printNode(listNode.cdr());
    }
    
    private void printNode(QuoteNode quoteNode) {
//        ps.print("'");
        output.append('\'');
        printNode(quoteNode.nodeInside());
    }
    
    private void printNode(Node node) {
        if (node instanceof ListNode) {
            if(((ListNode) node).car() instanceof QuoteNode)
                printNode((QuoteNode) ((ListNode) node).car());
            else {
//                ps.print("(");
                output.append('(');
                printNode((ListNode) node);
//                ps.print(")");
                output.append(')');
            }
        } else if (node instanceof QuoteNode) {
            printNode((QuoteNode)node);
        } else {
//            ps.print(node);
            output.append(node);
        }
    }
    
    public void prettyPrint(Node node) {
        printNode(node);
        ps.println(output);
        output.delete(0, output.length());
    }

    public void errorPrint(String msg) {
        ps.println(msg);
    }
}
