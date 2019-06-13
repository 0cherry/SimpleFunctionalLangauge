package interpreter;

import lexer.Token;
import lexer.TokenType;
import parser.ast.*;
import application.InterpreterSystem;

import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

public class CuteInterpreter extends Thread {
    private static LinkedBlockingDeque<String> signal_queue = InterpreterSystem.signal_queue;
    private static LinkedBlockingDeque<Token> tokens_queue = InterpreterSystem.tokens_queue;
    private static LinkedBlockingDeque<Node> nodes_queue = InterpreterSystem.nodes_queue;
    private Stack<HashMap<String, Node>> variables_stack;
    private OriginalPrettyPrinter printer = OriginalPrettyPrinter.getPrinter(System.out);

    public CuteInterpreter() {
        variables_stack = new Stack<>();
        variables_stack.push(new HashMap<>());
    }

    @Override
    public void run() {
        while(true) {
            try {
                Node expr = nodes_queue.take();
                Node result = runExpr(expr);
                printer.prettyPrint(result);
                if(tokens_queue.isEmpty() && nodes_queue.isEmpty())
                    signal_queue.put("> ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Node runExpr(Node rootExpr) {
        if(rootExpr == null)
            return null;

        Node result = null;

        if(rootExpr instanceof IdNode)
            result = lookupVariables((IdNode) rootExpr);
        else if(rootExpr instanceof IntNode)
            result = rootExpr;
        else if(rootExpr instanceof StringNode)
            result = rootExpr;
        else if(rootExpr instanceof BooleanNode)
            result = rootExpr;
        else if(rootExpr instanceof BinaryOpNode)
            result = rootExpr;
        else if(rootExpr instanceof FunctionNode)
            result = rootExpr;
        else if(rootExpr instanceof ListNode) {
            if(((ListNode) rootExpr).car() instanceof ListNode) {
                if (((FunctionNode) (((ListNode) ((ListNode) rootExpr).car()).car())).funcType == FunctionNode.FunctionType.LAMBDA) {
                    result = lambda((ListNode) rootExpr);
                    variables_stack.pop();
                }
                else
                    errorLog(rootExpr, "run Expr Error");
            }
            else
                result = runList((ListNode) rootExpr);
        }
        else
            errorLog(rootExpr, "run Expr Error");

        return result;
    }

    private Node lookupVariables(IdNode idNode) {
        Node defined_expr = null;

        for(int i = variables_stack.size()-1; i >= 0; i--) {
            defined_expr = variables_stack.elementAt(i).get(idNode.getIdString());
            if(defined_expr != null)
                break;
        }

        if(defined_expr == null) {
            errorLog(idNode, "define Error : [" + idNode.getIdString() + "] is not defined");
            return null;
        }
        else
            return defined_expr;
    }

    private Node runList(ListNode list) {
        Node opNode = list.car();
        if(opNode instanceof QuoteNode) {
            if(((QuoteNode) opNode).nodeInside() instanceof IntNode)
                return stripQuote(list);
            else
                return list;
        }
        else if(opNode instanceof FunctionNode)
            return runFunction(list);
        else if(opNode instanceof BinaryOpNode)
            return runBinary(list);
        // run defined expression
        else if(opNode instanceof IdNode) {
            return runExpr(ListNode.cons(runExpr(opNode), list.cdr()));
        }
        else {
            errorLog(list, "runList Error : [CAR of list] is not a operator");
            return null;
        }
    }

    private Node runFunction(ListNode list) {
        ListNode rhs = list.cdr();
        FunctionNode function = (FunctionNode) list.car();
        Node result = null;
        switch (function.funcType)
        {
            case CAR:
                result = car(rhs);
                break;
            case CDR:
                result = cdr(rhs);
                break;
            case CONS:
                result = cons(rhs);
                break;
            case ATOM_Q:
                result = atom_q(rhs);
                break;
            case EQ_Q:
                result = eq_q(rhs);
                break;
            case NULL_Q:
                result = null_q(rhs);
                break;
            case NOT:
                result = not(rhs);
                break;
            case COND:
                result = cond(rhs);
                break;
            case DEFINE:
                result = define(rhs);
                break;
            case LAMBDA:
                result = list;
                break;
            case EXIT:
                result = exit(rhs);
                break;
            case IMPORT:
                result = import_code(rhs);
                break;
            default:
                errorLog(list, "runFunction Error : wrong function");
        }
        return result;
    }

    // must modify
    private Node import_code(ListNode rhs) {
        if(rhs.size() < 1) {
            errorLog("[import] number of arguments Error : expected 1 more");
            errorLog("\tgiven : " + rhs.size());
            return null;
        }

        Node rhs1 = runExpr(rhs.car());
        if(rhs1 instanceof StringNode) {
            String import_file_name = ((StringNode) rhs1).getValue();
            try {
                signal_queue.put(import_file_name);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new FunctionNode(TokenType.IMPORT);
        }
        else {
            errorLog("[import] file path Error : expected StringNode");
            errorLog("\tgiven : " + rhs1.getClass().getName());
            return null;
        }
    }

    private Node exit(ListNode rhs) {
        if(rhs.size() > 1) {
            errorLog("[exit] number of arguments Error : expected 0 or 1");
            errorLog("\tgiven : " + rhs.size());
            return null;
        }

        // (exit) or (exit expr)
        if(rhs == ListNode.EMPTYLIST)
            System.exit(0);
        // Node exit_status = runExpr(rhs.cdr().car());
        System.exit(0);
        return null;
    }

    private Node cond(ListNode rhs) {
        // (condition ... result)
        Node condition_result_expression = rhs.car();
        if(condition_result_expression == null)
            return null;
        if(!(condition_result_expression instanceof ListNode)) {
            errorLog("[cond] required (condition ... expr)");
            return null;
        }
        Node condition = runExpr(((ListNode) condition_result_expression).car());
        if(!(condition instanceof BooleanNode)) {
            errorLog("[cond] condition must be BooleanNode");
            return null;
        }
        if(((BooleanNode) condition).getValue())
            return runExpr(((ListNode) condition_result_expression).last());
        else
            return cond(rhs.cdr());
    }

    private Node lambda(ListNode rhs) {
        ListNode lambda_expr = (ListNode) rhs.car();
        Node lambda_var = lambda_expr.cdr().car();
        Node lambda_body = lambda_expr.last();
        ListNode argv = rhs.cdr();
        variables_stack.push(new HashMap<>());

        /* set variables in lambda scope */
        // ((lambda (arg-id ...) ... body) args)
        if(lambda_var instanceof ListNode) {
            if(((ListNode) lambda_var).size() != argv.size()) {
                errorLog("[lambda] number of arguments Error : given " + argv.size());
                return null;
            }
            defineLambdaMultipleVariables((ListNode) lambda_var, argv);
        }
        // ((lambda rest-id ... body) args)
        else {
            defineLambdaSingleVariable(lambda_var, argv);
        }

        // check var ... body
        ListNode rest_id = lambda_expr.cdr().cdr();
        for(; rest_id.car() != lambda_body; rest_id = rest_id.cdr()) {
            if(runExpr(rest_id.car()) == null)
                errorLog("[lambda] rest-id Error : Undefined");
        }

        // runExpr lambda body
        return runExpr(rest_id.car());
    }

    private void defineLambdaSingleVariable(Node lambda_var, ListNode argv) {
        Stack<Node> value_stack = new Stack<>();
        for(ListNode i = argv; i.size() > 0; i = i.cdr())
            value_stack.push(stripQuote(runExpr(i.car())));
        ListNode argv_list = (value_stack.size() > 0 ? ListNode.cons(value_stack.pop(), ListNode.EMPTYLIST) : ListNode.EMPTYLIST);
        for(; value_stack.size() > 0; ) {
            argv_list = ListNode.cons(value_stack.pop(), argv_list);
        }
        argv_list = ListNode.cons(wrapQuote(argv_list), ListNode.EMPTYLIST);
        Node define_expr = ListNode.cons(new FunctionNode(TokenType.DEFINE), ListNode.cons(lambda_var, argv_list));
        runExpr(define_expr);
    }

    private void defineLambdaMultipleVariables(ListNode lambda_var, ListNode argv) {
        for(ListNode i = lambda_var, j = argv; i.car() != null && j.car() != null; i = i.cdr(), j = j.cdr()) {
            Node id = i.car();
            Node value = runExpr(j.car());
            Node define_expr = ListNode.cons(new FunctionNode(TokenType.DEFINE), ListNode.cons(id, ListNode.cons(value, ListNode.EMPTYLIST)));
            runExpr(define_expr);
        }
    }

    private Node define(ListNode rhs) {
        if(rhs.size() != 2) {
            errorLog("[define] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node define_identifier = rhs.car();
        if(define_identifier instanceof IdNode) {
            String id = ((IdNode) define_identifier).getIdString();
            Node value = runExpr(rhs.cdr().car());
            variables_stack.peek().put(id, value);
            //System.out.printf("%s is defined %s.\n", ((IdNode) define_identifier).getValue(), value.toString());
            return new FunctionNode(TokenType.DEFINE);
            //return new IdNode("");
        }
        else {
            errorLog("[define] operand Error : arg1 is not Identifier");
            return null;
        }
    }

    private Node eq_q(ListNode rhs) {
        if(rhs.size() != 2) {
            errorLog("[eq?] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node eq_q_operand1 = stripQuote(runExpr(rhs.car()));
        Node eq_q_operand2 = stripQuote(runExpr(rhs.cdr().car()));

        // ListNode's equals method is required to modify
        if(eq_q_operand1.equals(eq_q_operand2))
            return BooleanNode.TRUE_NODE;
        else
            return BooleanNode.FALSE_NODE;
    }

    private Node not(ListNode rhs) {
        if(rhs.size() != 1) {
            errorLog("[not] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node not_operand = runExpr(rhs.car());
        if(not_operand instanceof BooleanNode) {
            if (((BooleanNode) not_operand).getValue())
                return BooleanNode.FALSE_NODE;
            else
                return BooleanNode.TRUE_NODE;
        }
        else {
            errorLog("[not] operand Error : not Boolean");
            return null;
        }
    }

    private Node atom_q(ListNode rhs) {
        if(rhs.size() != 1) {
            errorLog("[atom?] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node atom_q_operand = stripQuote(runExpr(rhs.car()));
        if(atom_q_operand instanceof ListNode) {
            if(((ListNode) atom_q_operand).car() == null)
                return BooleanNode.TRUE_NODE;
            else
                return BooleanNode.FALSE_NODE;
        }
        else if(atom_q_operand == null)
            return null;
        else
            return BooleanNode.TRUE_NODE;
    }

    private Node null_q(ListNode rhs) {
        if(rhs.size() != 1) {
            errorLog("[null?] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node null_q_operand = stripQuote(runExpr(rhs.car()));
        if(null_q_operand instanceof IntNode)
            return BooleanNode.FALSE_NODE;
        else if(null_q_operand instanceof BooleanNode)
            return BooleanNode.FALSE_NODE;
        else if(null_q_operand instanceof StringNode)
            return BooleanNode.FALSE_NODE;
        else {
            if(null_q_operand == ListNode.EMPTYLIST)
                return BooleanNode.TRUE_NODE;
            else
                return BooleanNode.FALSE_NODE;
        }
    }

    private Node cons(ListNode rhs) {
        if(rhs.size() != 2) {
            errorLog("[cons] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node cons_operand1 = stripQuote(runExpr(rhs.car()));
        Node cons_operand2 = stripQuote(runExpr(rhs.cdr().car()));
        if(cons_operand2 instanceof ListNode) {
            Node cons_list = ListNode.cons(cons_operand1, (ListNode) cons_operand2);
            return wrapQuote(cons_list);
        }
        else {
            errorLog("[cons] operand Error");
            return null;
        }
    }

    private Node cdr(ListNode rhs) {
        if(rhs.size() != 1) {
            errorLog("[cdr] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node cdr_operand = stripQuote(runExpr(rhs.car()));
        if(cdr_operand == ListNode.EMPTYLIST) {
            errorLog("[cdr] arguments Error : given null");
            return null;
        }
        if(cdr_operand instanceof ListNode) {
            return wrapQuote(((ListNode) cdr_operand).cdr());
        } else {
            errorLog("[cdr] operand Error : not List");
            return null;
        }
    }

    private Node car(ListNode rhs) {
        if(rhs.size() != 1) {
            errorLog("[car] number of arguments Error : given " + rhs.size());
            return null;
        }
        Node car_operand = stripQuote(runExpr(rhs.car()));
        if(car_operand == ListNode.EMPTYLIST) {
            errorLog("[car] arguments Error : given null");
            return null;
        }
        if(car_operand instanceof ListNode) {
            car_operand = ((ListNode) car_operand).car();
            if(car_operand instanceof IntNode)
                return car_operand;
            else if(car_operand instanceof BooleanNode)
                return car_operand;
            else if(car_operand instanceof StringNode)
                return car_operand;
            else
                return wrapQuote(car_operand);
        } else {
            errorLog("[car] operand Error : not List");
            return null;
        }
    }

    private Node runBinary(ListNode list) {
        Node result;
        ListNode rhs = list.cdr();
        if(rhs.size() < 2) {
            errorLog("runBinary number of arguments Error : given " + rhs.size());
            return null;
        }

        BinaryOpNode binaryOpNode = (BinaryOpNode) list.car();
        switch(binaryOpNode.binType)
        {
            case MINUS:
                result = minus(rhs.car() ,rhs.cdr());
                break;
            case PLUS:
                result = plus(rhs.car(), rhs.cdr());
                break;
            case TIMES:
                result = times(rhs.car(), rhs.cdr());
                break;
            case DIV:
                result = div(rhs.car(), rhs.cdr());
                break;
            case LT:
                result = lt(rhs.car(), rhs.cdr());
                break;
            case GT:
                result = gt(rhs.car(), rhs.cdr());
                break;
            case EQ:
                result = eq(rhs.car(), rhs.cdr());
                break;
            default:
                errorLog("runBinary op Error");
                result = null;
        }
        return result;
    }

    private IntNode minus(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        IntNode result;
        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Integer result_value = ((IntNode) car).getValue() - ((IntNode) cdr_car).getValue();
            result =  new IntNode(result_value.toString());
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            result = minus(result, cdr.cdr());
        return result;
    }

    private IntNode plus(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        IntNode result;
        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Integer result_value = ((IntNode) car).getValue() + ((IntNode) cdr_car).getValue();
            result =  new IntNode(result_value.toString());
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            result = plus(result, cdr.cdr());
        return result;
    }

    private IntNode times(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        IntNode result;
        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Integer result_value = ((IntNode) car).getValue() * ((IntNode) cdr_car).getValue();
            result =  new IntNode(result_value.toString());
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            result = times(result, cdr.cdr());
        return result;
    }

    private IntNode div(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        IntNode result;
        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Integer result_value = ((IntNode) car).getValue() / ((IntNode) cdr_car).getValue();
            result =  new IntNode(result_value.toString());
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            result = div(result, cdr.cdr());
        return result;
    }

    private BooleanNode lt(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Boolean less = ((IntNode) car).getValue() < ((IntNode) cdr_car).getValue();
            if(!less)
                return BooleanNode.FALSE_NODE;
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            return lt(cdr_car, cdr.cdr());
        return BooleanNode.TRUE_NODE;
    }

    private BooleanNode gt(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Boolean greater = ((IntNode) car).getValue() > ((IntNode) cdr_car).getValue();
            if(!greater)
                return BooleanNode.FALSE_NODE;
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            return gt(cdr_car, cdr.cdr());
        return BooleanNode.TRUE_NODE;
    }

    private BooleanNode eq(Node car, ListNode cdr) {
        car = runExpr(car);
        Node cdr_car = runExpr(cdr.car());

        if(car instanceof IntNode && cdr_car instanceof IntNode) {
            Boolean equal = ((IntNode) car).getValue().equals(((IntNode) cdr_car).getValue());
            if(!equal)
                return BooleanNode.FALSE_NODE;
        }
        else {
            errorLog("runBinary operand Error : not IntNode");
            return null;
        }

        if(cdr.cdr() != ListNode.EMPTYLIST)
            return eq(cdr_car, cdr.cdr());
        return BooleanNode.TRUE_NODE;
    }

    private Node wrapQuote(Node node) {
        return ListNode.cons(new QuoteNode(node), ListNode.EMPTYLIST);
    }

    private Node stripQuote(Node node) {
        if(node instanceof ListNode) {
            if(((ListNode) node).car() instanceof QuoteNode)
                return ((QuoteNode) ((ListNode) node).car()).nodeInside();
        }
        return node;
    }

    private void errorLog(Node error_point, String msg) {
        printer.prettyPrint(error_point);
        printer.errorPrint(msg);
    }

    private void errorLog(String msg) {
        printer.errorPrint(msg);
    }
}
