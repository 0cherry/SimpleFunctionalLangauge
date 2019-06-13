package lexer;

import java.io.*;

public class CharStream {
    private Reader reader;
    private Character cache;

    CharStream() {
        this.cache = null;
    }

    public void setReader(Serializable line) {
        if (line instanceof String)
            this.reader = new StringReader((String) line);
        else if (line instanceof File) {
            try {
                this.reader = new FileReader((File) line);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    Char nextChar() {
        if (cache != null) {
            char ch = cache;
            cache = null;

            return Char.of(ch);
        } else {
            try {
                int ch = reader.read();
                switch (ch) {
                    /*
                    case -1:
                        if (paren_pair != 0 || double_quote_pair % 2 != 0) {
                            if(InterpreterSystem.nodes_queue.isEmpty()) {
                                System.out.println("# need more input");
                                try {
                                    CuteLexer.context.setInput(InterpreterSystem.lexical_queue.take());
                                } catch (InterruptedException e) {}
                            }
                            return Char.of((char) 10);
                        } else
                            return Char.end();
                    */
                    case -1:
                        return Char.end();
                    case 45:
//                        return nextChar();
                    case '(':
                    case ')':
                    case '"':
                    case 10:
                    default:
                        return Char.of((char) ch);
                }
            } catch (IOException e) {
                throw new ScannerException("" + e);
            }
        }
    }

    void pushBack(char ch) {
        cache = ch;
    }
}
