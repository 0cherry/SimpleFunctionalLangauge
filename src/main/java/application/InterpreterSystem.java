package application;

import interpreter.CuteInterpreter;
import lexer.CuteLexer;
import lexer.Token;
import parser.CuteParser;
import parser.ast.Node;

import java.util.concurrent.LinkedBlockingDeque;

public class InterpreterSystem {
    public static LinkedBlockingDeque<String> signal_queue;
    public static LinkedBlockingDeque<Token> tokens_queue;
    public static LinkedBlockingDeque<Node> nodes_queue;
    private static Thread[] pool;

    public static void main(String args[]) throws InterruptedException {
        InterpreterSystem.ready();
        signal_queue.put("> ");
        InterpreterSystem.run();
    }

    private static void run() throws InterruptedException {
        for (Thread t:pool)
            t.start();

        for (Thread t:pool)
            t.join();
    }

    private static void ready() {
        signal_queue = new LinkedBlockingDeque<>();
        tokens_queue = new LinkedBlockingDeque<>();
        nodes_queue = new LinkedBlockingDeque<>();

        pool = new Thread[3];
        pool[0] = new CuteLexer();
        pool[1] = new CuteParser();
        pool[2] = new CuteInterpreter();
    }
}
