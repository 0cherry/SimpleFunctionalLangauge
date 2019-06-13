package lexer;

import java.io.Serializable;

class ScanContext {
    private CharStream input;
    private StringBuilder builder;

    ScanContext() {
        this.input = new CharStream();
        this.builder = new StringBuilder();
    }

    void setInput(Serializable input) {
        this.input.setReader(input);
    }

    CharStream getCharStream() {
        return input;
    }

    String getLexime() {
        String str = builder.toString();
        builder.setLength(0);
        return str;
    }

    void append(char ch) {
        builder.append(ch);
    }
}
