package parser;

import lexer.Token;
import lexer.TokenType;
import parser.ast.*;
import application.InterpreterSystem;

import java.util.concurrent.LinkedBlockingDeque;

public class CuteParser extends Thread {
    private static LinkedBlockingDeque<String> signal_queue = InterpreterSystem.signal_queue;
	private static LinkedBlockingDeque<Token> tokens_queue = InterpreterSystem.tokens_queue;
	private static LinkedBlockingDeque<Node> nodes_queue = InterpreterSystem.nodes_queue;

	private static Node END_OF_LIST = new Node(){};

	public CuteParser() {
	}

	@Override
	public void run() {
		while(true) {
			try {
				Node query = parseExpr();
				if(query == null) {
					System.out.println("Parse Error");
					break;
				}
				nodes_queue.put(query);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Node parseExpr() {
		Token t = null;
		try {
            t = tokens_queue.take();
//            if(tokens_queue.isEmpty() && nodes_queue.isEmpty())
//                signal_queue.put("");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		TokenType tType = t.type();
		String tLexeme = t.lexme();

		switch (tType) {
		case ID:
			return new IdNode(tLexeme);
		case INT:
			return new IntNode(tLexeme);
		case STRING:
			return new StringNode(tLexeme);
		case DIV:
		case EQ:
		case MINUS:
		case GT:
		case PLUS:
		case TIMES:
		case LT:
			return new BinaryOpNode(tType);
		case ATOM_Q:
		case CAR:
		case CDR:
		case COND:
		case CONS:
		case DEFINE:
		case EQ_Q:
		case LAMBDA:
		case NOT:
		case NULL_Q:
		case IMPORT:
        case EXIT:
			return new FunctionNode(tType);
		case FALSE:
			return BooleanNode.FALSE_NODE;
		case TRUE:
			return BooleanNode.TRUE_NODE;
		case L_PAREN:
			return parseList();
		case R_PAREN:
			return END_OF_LIST;
		case APOSTROPHE:
			return ListNode.cons(new QuoteNode(parseExpr()), ListNode.EMPTYLIST);
//			return ListNode.cons(new QuoteNode(parseQuotedExpr()), ListNode.EMPTYLIST);
		case QUOTE:
			return new QuoteNode(parseExpr());
//			return new QuoteNode(parseQuotedExpr());
		default:
			System.out.println("parseExpr Error!");
			return null;
		}
	}

	private ListNode parseList() {
		Node head = parseExpr();
		if(head == null) {
			System.out.println("parseList Error!");
			return null;
		}
		if(head == END_OF_LIST) // if next token is RPAREN
			return ListNode.EMPTYLIST;

		ListNode tail = parseList();
		if(tail == null) {
			System.out.println("parseList Error!");
			return null;
		}

		return ListNode.cons(head, tail);
	}

	private Node parseQuotedExpr() {
		Token t = null;
		try {
			t = tokens_queue.take();
//            if(tokens_queue.isEmpty() && nodes_queue.isEmpty())
//                signal_queue.put("");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (t == null) {
			System.out.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();

		switch (tType) {
			case ID:
			case DIV:
			case EQ:
			case MINUS:
			case GT:
			case PLUS:
			case TIMES:
			case LT:
			case ATOM_Q:
			case CAR:
			case CDR:
			case COND:
			case CONS:
			case DEFINE:
			case EQ_Q:
			case LAMBDA:
			case NOT:
			case NULL_Q:
            case IMPORT:
            case EXIT:
				return new IdNode(tLexeme);
			case INT:
				return new IntNode(tLexeme);
			case FALSE:
				return BooleanNode.FALSE_NODE;
			case TRUE:
				return BooleanNode.TRUE_NODE;
			case L_PAREN:
				return parseQuotedList();
			case R_PAREN:
				return END_OF_LIST;
			case APOSTROPHE:
				return ListNode.cons(new QuoteNode(parseQuotedExpr()), ListNode.EMPTYLIST);
			case QUOTE:
				return new QuoteNode(parseQuotedExpr());
			default:
				System.out.println("parseQuotedExpr Error!");
				return null;
		}
	}

	private ListNode parseQuotedList() {
		Node head = parseQuotedExpr();
		if(head == null) {
			System.out.println("parseQuotedList Error!");
			return null;
		}
		if(head == END_OF_LIST) // if next token is RPAREN
			return ListNode.EMPTYLIST;

		ListNode tail = parseQuotedList();
		if(tail == null) {
			System.out.println("parseList Error!");
			return null;
		}

		return ListNode.cons(head, tail);
	}
}
