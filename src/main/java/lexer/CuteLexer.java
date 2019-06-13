package lexer;

import parser.ast.Node;
import application.InterpreterSystem;

import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingDeque;

public class CuteLexer extends Thread {
    private static LinkedBlockingDeque<String> signal_queue = InterpreterSystem.signal_queue;
    private static LinkedBlockingDeque<Token> tokens_queue = InterpreterSystem.tokens_queue;
    private static LinkedBlockingDeque<Node> nodes_queue = InterpreterSystem.nodes_queue;
    private static Scanner keyboard_input = new Scanner(System.in);
    private static ScanContext context = new ScanContext();

    public CuteLexer() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                System .out.print(signal_queue.take());
                input();
                scan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void input() {
        String input = keyboard_input.nextLine();
        context.setInput(input);
//        scan();
//        if(signal.equals("> ")) {
//            String line = "";
//            while(line.length() == 0) {
//                System.out.print(signal);
//                line = keyboard_input.nextLine();
//            }
//            context.setInput(line);
//        } else {
//            File import_file = new File("./src/main/" + signal);
//            context.setInput(import_file);
//        }
    }

    private void scan() throws InterruptedException {
        Optional<Token> nextToken = generateToken(context);
        while (nextToken.isPresent()) {
            tokens_queue.put(nextToken.get());
            nextToken = generateToken(context);
        }
    }

    private Optional<Token> generateToken(ScanContext context) {
        lexer.State current = lexer.State.START;
        while (true) {
            TransitionOutput output = current.transit(context);
            if (output.nextState() == lexer.State.MATCHED) {
                return output.token();
            } else if (output.nextState() == lexer.State.FAILED) {
                throw new ScannerException();
            } else if (output.nextState() == lexer.State.EOS) {
                return Optional.empty();
            }

            current = output.nextState();
        }
    }
}
