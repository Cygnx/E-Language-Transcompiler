/* *** This file is given as part of the programming assignment. *** */


public class Parser {

	// tok is global to all these parsing methods;
	// scan just calls the scanner's scan method and saves the result in tok.
	private Token tok; // the current token
	private SymbolTable symTable;
	private Scan scanner;
	
	private void scan() {	// scan input	
		tok = scanner.scan();
	}

	Parser(Scan scanner) {	// parser constructor
		symTable = new SymbolTable();	// builds a new symbol table
		this.scanner = scanner;	// initlizes objects
		scan();
		program();
		if (tok.kind != TK.EOF)
			parse_error("junk after logical end of program");
	}
	
	private void program() {	// recursive descent function
		symTable.println("#include <stdio.h>", 0);// generate c code
		symTable.println("void main ()", 0);// generate c code
		block();
	}

	private void block() { 	// recursive descent function
		symTable.println("{", 0);// generate c code
		symTable.newScope();
		declaration_list();
		statement_list();
		symTable.destroyCurrentScope();
		symTable.println("}",0);// generate c code
	}

	private void declaration_list() {	// recursive descent function
		// below checks whether tok is in first set of declaration.
		// here, that's easy since there's only one token kind in the set.
		// in other places, though, there might be more.
		// so, you might want to write a general function to handle that.
		while (is(TK.DECLARE)) {
			declaration();
		}
	}

	private void declaration() {	// recursive descent function
		mustbe(TK.DECLARE);
		if(is(TK.ID))
			symTable.addToCurrentScope(tok.string);
		mustbe(TK.ID);
		while (is(TK.COMMA)) {
			scan();
			if(is(TK.ID))
				symTable.addToCurrentScope(tok.string);
			mustbe(TK.ID);
		}
	}

	private void statement_list() {	// recursive descent function
		while(is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF) || is(TK.FOR))
			statement();
	}
	
	private void statement(){	// recursive descent function
		if(is(TK.TILDE) || is(TK.ID))
		{
			assignment();symTable.println(";", 3);
		}
		else if(is(TK.PRINT))
			print();
		else if(is(TK.DO))
			_do();
		else if(is(TK.IF))
			_if();
		else if(is(TK.FOR))
			_for();
		else
			parse_error("missing token (mustbe)");
	}
	
	private void assignment(){	// recursive descent function
		symTable.println("",1);		//formating for assignment
		ref_id();
		if(is(TK.ASSIGN))
			symTable.println("=", 2);// generate c code
		mustbe(TK.ASSIGN);
		expr();
	}

	private void print(){	// recursive descent function
		mustbe(TK.PRINT);
		symTable.println("printf(\"%d\\n\",", 1);	// generate c code
		expr();
		symTable.println(" );", 3);// generate c code
	}

	private void ref_id(){	// recursive descent function
		int level = -2;
		boolean isScoped = false;
		String errorString = "";	// string buffer for the error message below
		
		if(is(TK.TILDE)){
			isScoped = true;	// flag to see if scope operator is used
			errorString += "~";
			scan();
			if(is(TK.NUM)){
				level = Integer.parseInt(tok.string);
				errorString += tok.string;
				scan();
			}
			else 
				level = -1;
			errorString += tok.string;
		}
		if(is(TK.ID)){
			if(! symTable.isValidVar(tok.string, level)){	// if valid variable	
				if(!isScoped)	// if scope operator is used
					System.err.println(tok.string + " is an undeclared variable on line " + tok.lineNumber);
				else
					System.err.println("no such variable "+ errorString + " on line " + tok.lineNumber);
				System.exit(1);
			}
		}
		mustbe(TK.ID);
	}

	private void _do(){	// recursive descent function
		mustbe(TK.DO);
		symTable.println("while", 1);// generate c code
		guarded_command();
		mustbe(TK.ENDDO);
	}

	private void _for(){	// recursive descent function
		mustbe(TK.FOR);
		declaration_list();	// optional delarations
		
		mustbe(TK.ELSEIF);	
		symTable.println("for(",1);// generate c code
		assignment();
		symTable.println(";",2);// generate c code
		
		mustbe(TK.ELSEIF);
		expr();
		symTable.println("<= 0;",2);// generate c code
		
		mustbe(TK.ELSEIF);
		assignment();
		symTable.println(")",3);// generate c code
		
		mustbe(TK.THEN);
		block();
		mustbe(TK.ENDFOR);
	}
	private void _if(){	// recursive descent function
		mustbe(TK.IF);
		symTable.println("if", 1);// generate c code
		guarded_command();
		while(is(TK.ELSEIF)){
			scan();
			symTable.println("else if",1);// generate c code
			guarded_command();
		}
		if(is(TK.ELSE)){
			scan();
			symTable.println("else", 0);// generate c code
			block();
		}
		mustbe(TK.ENDIF);
	}

	private void guarded_command(){	// recursive descent function
		symTable.println("( ", 2);// generate c code
		expr();
		symTable.println(" <= 0 )", 3);// generate c code
		mustbe(TK.THEN);
		block();
	}

	private void expr(){	// recursive descent function
		term();
		while(is(TK.PLUS) || is(TK.MINUS)){
			addop();
			term();
		}
	}

	private void term(){	// recursive descent function
		factor();
		while(is(TK.TIMES) || is(TK.DIVIDE)){
			multop();
			factor();
		}
	}

	private void factor(){	// recursive descent function
		if(is(TK.LPAREN)){
			mustbe(TK.LPAREN);
			symTable.println("(", 2);// generate c code
			expr();
			mustbe(TK.RPAREN);
			symTable.println(")", 2);// generate c code
		}
		else if(is(TK.TILDE) || is(TK.ID)){
			ref_id();
		}
		else if(is(TK.NUM)){
			symTable.println(tok.string, 2);// generate c code
			scan();
		}	
		else
			parse_error("missing token (mustbe)");
	}

	private void addop(){	// recursive descent function
		if(is(TK.PLUS)){
			symTable.println(" + ", 2);// generate c code
			scan();
		}
		else if(is(TK.MINUS)){
			symTable.println(" - ", 2);// generate c code
			scan();
		}
		else
			parse_error("missing token (mustbe)");
	}

	private void multop(){	// recursive descent function
		if(is(TK.TIMES)){
			symTable.println(" * ", 2);// generate c code
			scan();
		}
		else if(is(TK.DIVIDE)){
			symTable.println(" / ", 2);// generate c code
			scan();
		}
		else
			parse_error("missing token (mustbe)");
	}
	
	// is current token what we want?
	private boolean is(TK tk) {
		return tk == tok.kind;
	}

	// ensure current token is tk and skip over it.
	private void mustbe(TK tk) {
		if (tok.kind != tk) {
			System.err.println("mustbe: want " + tk + ", got " + tok);
			parse_error("missing token (mustbe)");
		}
		scan();
	}

	private void parse_error(String msg) {	// parse the kind of error
		System.err.println("can't parse: line " + tok.lineNumber + " " + msg);
		System.exit(1);
	}
}
