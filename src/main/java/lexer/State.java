package lexer;

import static lexer.TokenType.*;
import static lexer.TransitionOutput.*;

enum State {
	START {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			TokenType type = TokenType.fromSpecialCharactor(v);
			switch ( ch.type() ) {
				case LETTER:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					switch ( v ) {
						case '-':
						case '+':
							context.append(v);
							return GOTO_SIGN;
						case '#':
							context.append(v);
							return GOTO_SHARP;
						default:
							return GOTO_MATCHED(type, ""+v);
					}
				case QUOTE:
					return GOTO_MATCHED(type, ""+v);
				case DOUBLE_QUOTE:
//					context.append(v);
					return GOTO_STRING;
				case PARENTHESIS:
					return GOTO_MATCHED(type, ""+v);
				case WS:
					return GOTO_START;
				case END_OF_STREAM:
					return GOTO_EOS;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case SPECIAL_CHAR:
				case LETTER:
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case QUOTE:
				case DOUBLE_QUOTE:
				case PARENTHESIS:
					context.getCharStream().pushBack(ch.value());
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(Token.ofName(context.getLexime()));
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case QUOTE:
				case DOUBLE_QUOTE:
				case PARENTHESIS:
					context.getCharStream().pushBack(ch.value());
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	STRING {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:
				case DIGIT:
				case SPECIAL_CHAR:
				case PARENTHESIS:
				case WS:
				case QUOTE:
					context.append(ch.value());
					return GOTO_STRING;
				case DOUBLE_QUOTE:
//					context.append(ch.value());
					return GOTO_ACCEPT_STRING;
				case END_OF_STREAM:
					return GOTO_FAILED;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_STRING {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:
				case DIGIT:
				case SPECIAL_CHAR:
				case PARENTHESIS:
				case QUOTE:
				case DOUBLE_QUOTE:
					context.getCharStream().pushBack(ch.value());
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(TokenType.STRING, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	SHARP {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					switch ( v ) {
						case 't':
						case 'T':
						case 'f':
						case 'F':
							context.append(v);
							return GOTO_ACCEPT_BOOLEAN;
						default:
							return GOTO_FAILED;
					}
				default:
					return GOTO_FAILED;
			}
		}
	},
	ACCEPT_BOOLEAN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case QUOTE:
				case DOUBLE_QUOTE:
				case PARENTHESIS:
					context.getCharStream().pushBack(ch.value());
				case WS:
				case END_OF_STREAM:
					String lexme = context.getLexime();
					switch ( lexme ) {
						case "#t":
						case "#T":
							return GOTO_MATCHED(TRUE, context.getLexime());
						case "#f":
						case "#F":
							return GOTO_MATCHED(FALSE, context.getLexime());
					}
				default:
					return GOTO_FAILED;
			}
		}
	},
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case QUOTE:
				case DOUBLE_QUOTE:
				case PARENTHESIS:
					context.getCharStream().pushBack(ch.value());
				case WS:
				case END_OF_STREAM:
					String lexme = context.getLexime();
					switch ( lexme ) {
						case "+":
							return GOTO_MATCHED(PLUS, lexme);
						case "-":
							return GOTO_MATCHED(MINUS, lexme);
						default:
							throw new AssertionError();
					}
				default:
					throw new AssertionError();
			}
		}
	},
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
