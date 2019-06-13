package lexer;

class Char {
	private final char value;
	private final CharacterType type;

	enum CharacterType {
		LETTER, DIGIT, SPECIAL_CHAR, QUOTE, DOUBLE_QUOTE, PARENTHESIS, WS, END_OF_STREAM,
	}
	
	static Char of(char ch) {
		return new Char(ch, getType(ch));
	}
	
	static Char end() {
		return new Char(Character.MIN_VALUE, CharacterType.END_OF_STREAM);
	}
	
	private Char(char ch, CharacterType type) {
		this.value = ch;
		this.type = type;
	}
	
	char value() {
		return this.value;
	}
	
	CharacterType type() {
		return this.type;
	}
	
	private static CharacterType getType(char ch) {
		int code = (int)ch;
		if ( (code >= (int)'A' && code <= (int)'Z')
			|| (code >= (int)'a' && code <= (int)'z')
			|| ch == '?' ) {
			return CharacterType.LETTER;
		}

		if ( Character.isDigit(ch) ) {
			return CharacterType.DIGIT;
		}
		
		switch ( ch ) {
			case '(': case ')': case '{': case '}':
				return CharacterType.PARENTHESIS;
			case '\'': case '`':
				return CharacterType.QUOTE;
			case '"':
				return CharacterType.DOUBLE_QUOTE;
			// : ; \ [ ] @ % & ...
			case '-': case '+': case '*': case '/':
			case '<': case '=': case '>': case '#':
			case '_': case '.': case '|': case '~':
			case '!': case '^':
				return CharacterType.SPECIAL_CHAR;
		}
		
		if ( Character.isWhitespace(ch) ) {
			return CharacterType.WS;
		}
		
		throw new IllegalArgumentException("inputContext=" + ch);
	}
}
