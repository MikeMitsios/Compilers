import syntaxtree.*;
import visitor.*;
import java.io.*;

class Main {
    public static void main (String [] args)throws Exception{
	if(args.length ==0){
	    System.err.println("Usage: java Driver <inputFile>");
	    System.exit(1);
	}

	for(String i : args){
		System.err.println("Program : "+i+"______________________");
		FileInputStream fis = null;
		try{
		    fis = new FileInputStream(i);
		    MiniJavaParser parser = new MiniJavaParser(fis);
		    System.err.println("Program parsed successfully.");
		    EvalVisitor eval = new EvalVisitor();		//first scan of the tree
		    Goal root = parser.Goal();			//parsing
		    root.accept(eval, null);
		    // eval.printV();
        // eval.printV2();



		    typechecking check = new typechecking();
		    check.set_symbolmap(eval.get_symbolmap());		//second scan of the tree
		    root.accept(check, null);
		    check.printV();
		}
		catch(ParseException ex){
		    System.out.println(ex.getMessage());
		}
		catch(FileNotFoundException ex){
		    System.err.println(ex.getMessage());
		}
		catch(MyCustomException e){					//catch me errrors
			System.err.println(e.toString());
		}
		finally{
		    try{
			if(fis != null) fis.close();
		    }
		    catch(IOException ex){
			System.err.println(ex.getMessage());
		    }
		}

	}

    }
}
