import java.io.InputStream;
import java.io.IOException;
//###############################################FOLLOWS EXACTLY THE TUTORIAL
class CalculatorParser {

    private int lookaheadToken;

    private InputStream in;

    public CalculatorParser(InputStream in) throws IOException {
    	this.in = in;
    	lookaheadToken = in.read();

    }

    private void consume(int symbol) throws IOException, ParseError {
    	if (lookaheadToken != symbol){

    	    throw new ParseError();
    	}
      //System.out.printf("bghke to ");
      //System.out.println(lookaheadToken);
    	lookaheadToken = in.read();
    }

    private int evalDigit(int digit){
		    return digit - '0';
    }

    private int expr() throws IOException, ParseError {
    	int num1,num2;
  		num1=term();

  		//consume(lookaheadToken);
  		return expr2(num1);
    }

    private int expr2(int num) throws IOException, ParseError {

	    if(lookaheadToken==')' || lookaheadToken == '\n' || lookaheadToken == -1)
		    return num;
        //System.out.printf("PRIN THN IF 1\n");
    	if(lookaheadToken !='+' && lookaheadToken !='-'){
    		// System.out.println(lookaheadToken);
    		throw new ParseError();
    	}

    	int num2=0;
      //System.out.printf("PRIN THN IF\n");
      if(lookaheadToken == '+'){
        consume('+');
    		num2=num + term();
      }
      else if(lookaheadToken == '-'){
        //System.out.printf("MPAINEI GIA AFERESH ---\n");
        consume('-');
        //System.out.println(lookaheadToken);
    		num2=num - term();
      }

  		return expr2(num2);
    }

    private int term() throws IOException, ParseError {
    	int num1,num2;
  		num1=factor();
  		num2=term2(num1);
  		return num2;
    }

	private int term2(int num) throws IOException, ParseError {

    	if(lookaheadToken==')' || lookaheadToken=='+' || lookaheadToken=='-' || lookaheadToken == '\n' || lookaheadToken == -1){
        //System.out.printf("kanv print to num= %d\n",num);
		    return num;
    	}

      if(lookaheadToken != '*' && lookaheadToken != '/'){

    		//System.out.println(lookaheadToken);
    		throw new ParseError();
    	}

    	int num2=0;
      if(lookaheadToken == '*'){
        consume('*');
    		num2=num * factor();
      }
      else if(lookaheadToken == '/'){
        consume('/');
    		num2=num / factor();
      }

  		return term2(num2);

    }

    private int factor() throws IOException, ParseError {
	    int num;
	    if(lookaheadToken=='('){
	    	consume('(');
	    	num=expr();
	    	consume(')');
	    	return num;
	    }

	    if(lookaheadToken < '0' || lookaheadToken > '9'){
		    throw new ParseError();
	    }
	    num=number(0);
  		return num;
    }


    private int number(int num) throws IOException, ParseError {
      int num2=0;
      if(lookaheadToken >= '0' && lookaheadToken <= '9'){
        num2=num*10+evalDigit(lookaheadToken);
        consume(lookaheadToken);
      }
      if(lookaheadToken >= '0' && lookaheadToken <= '9'){		//if the new lookahead is still a number then recall number 
        return number(num2);
      }
      else{								//else leave
        return num2;
      }
    }

     public int eval() throws IOException, ParseError {
      	int rv = expr();
      	if (lookaheadToken != '\n' && lookaheadToken != -1)
      	    throw new ParseError();
      	return rv;
    }

    public static void main(String[] args) {
    	try {
    		//System.out.println(3^2&7^(5&8^7));
    		CalculatorParser evaluate = new CalculatorParser(System.in);
            System.out.printf("The final result is ");
    	    System.out.println(evaluate.eval());
    	    // CalculatorParser parser = new CalculatorParser(System.in);
    	    // parser.parse();
    	}
    	catch (IOException e) {
    	    System.err.println(e.getMessage());
    	}
    	catch(ParseError err){
    	    System.err.println(err.getMessage());
    	}
    }
}
