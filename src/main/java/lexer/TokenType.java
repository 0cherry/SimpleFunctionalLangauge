package lexer;

public enum TokenType {
	INT, ID, STRING,
	QUESTION,

	TRUE, FALSE,

	NOT, PLUS, MINUS, TIMES, DIV,
	LT, GT, EQ, DEFINE, LAMBDA, COND,
	CAR, CDR, CONS, IMPORT, EXIT,
	ATOM_Q, NULL_Q, EQ_Q,

	DOT, BANG, PIPE, DASH, UNDERSCORE, CARET,

	QUOTE, APOSTROPHE,
	L_PAREN, R_PAREN;
	
	static TokenType fromSpecialCharactor(char ch) {
		switch ( ch ) {
			case '+':
				return PLUS;
			case '-':
				return MINUS;
			case '*':
				return TIMES;
			case '/':
				return DIV;
			case '<':
				return LT;
			case '>':
				return GT;
			case '=':
				return EQ;
			case '\'':
				return APOSTROPHE;
			case '(':
				return L_PAREN;
			case ')':
				return R_PAREN;
			case '?':
				return QUESTION;
			case '.':
				return DOT;
			case '!':
				return BANG;
			case '|':
				return PIPE;
			case '~':
				return DASH;
			case '_':
				return UNDERSCORE;
			case '^':
				return CARET;
			default:
				return null;
		}
	}
}
