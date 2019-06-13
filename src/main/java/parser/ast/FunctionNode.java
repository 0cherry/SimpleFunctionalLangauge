package parser.ast;

import lexer.TokenType;

import java.util.HashMap;
import java.util.Map;


public class FunctionNode implements ValueNode{	
	public enum FunctionType { 
		DEFINE 	{ TokenType tokenType() {return TokenType.DEFINE;} }, 
		LAMBDA 	{ TokenType tokenType() {return TokenType.LAMBDA;} },
		COND 	{ TokenType tokenType() {return TokenType.COND;} },
		NOT 	{ TokenType tokenType() {return TokenType.NOT;} },
		CDR 	{ TokenType tokenType() {return TokenType.CDR;} },
		CAR 	{ TokenType tokenType() {return TokenType.CAR;} },
		CONS 	{ TokenType tokenType() {return TokenType.CONS;} },
		EQ_Q 	{ TokenType tokenType() {return TokenType.EQ_Q;} },
		NULL_Q 	{ TokenType tokenType() {return TokenType.NULL_Q;} },
		ATOM_Q 	{ TokenType tokenType() {return TokenType.ATOM_Q;} },
		IMPORT 	{ TokenType tokenType() {return TokenType.IMPORT;} },
		EXIT 	{ TokenType tokenType() {return TokenType.EXIT;} }
		;
		
		private static Map<TokenType, FunctionType> fromTokenType = new HashMap<TokenType, FunctionType>();
		
		static {
			for (FunctionType fType : FunctionType.values()){
				fromTokenType.put(fType.tokenType(), fType);
			}
		}
		
		static FunctionType getFunctionType(TokenType tType){
			return fromTokenType.get(tType);
		}
		
		abstract TokenType tokenType();
	
	}
	public FunctionType funcType;
	
	public FunctionNode(TokenType tType){
		funcType = FunctionType.getFunctionType(tType);
	}
	
	@Override
	public String toString(){
		return "#<procedure:"+funcType.name()+">";
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof FunctionNode && funcType.equals(((FunctionNode) o).funcType);
	}
}
