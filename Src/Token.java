

public class Token {	// token class
    public TK kind;	// what kind of token
    public String string;	// value of token
    public int lineNumber;	// where the token was from
    public Token(TK kind, String string, int lineNumber) {
	this.kind = kind;
	this.string = string;
	this.lineNumber = lineNumber;
    }
    public String toString() { // make it printable for debugging
	return "Token("+kind.toString()+" "+string+" "+lineNumber+")";
    }
}
