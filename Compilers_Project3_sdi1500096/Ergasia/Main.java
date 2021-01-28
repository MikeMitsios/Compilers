import syntaxtree.*;
import visitor.*;
import java.io.*;

class Main {
    public static void main (String [] args)throws Exception{
	if(args.length ==0){
	    System.err.println("Usage: java Driver <inputFile>");
	    System.exit(1);
	}

  String name;
	for(String i : args){
		System.err.println("Program : "+i+"______________________");
		FileInputStream fis = null;
		try{
        //create a ile .ll based on the name of the file we got
        name="output/"+i.replace(".java","")+".ll";
  			File file = new File(name);
  			FileWriter writer = new FileWriter(file);
  			writer.close();


		    fis = new FileInputStream(i);
		    MiniJavaParser parser = new MiniJavaParser(fis);
		    System.err.println("Program parsed successfully.");
		    EvalVisitor eval = new EvalVisitor();		//first scan of the tree
		    Goal root = parser.Goal();			//parsing
		    root.accept(eval, null);
		    // eval.printV();
        // eval.printV2();



		    typechecking check = new typechecking();
        //check.set_file_name(name);//pass the name at the type checking
		    check.set_symbolmap(eval.get_symbolmap());		//second scan of the tree
		    root.accept(check, null);
		    check.printV();
        // System.out.println("hello");
        llvm_compiler llvm_com=new llvm_compiler();
        llvm_com.set_file_name(name);//pass the name at the type checking
		    llvm_com.set_symbolmap(eval.get_symbolmap());		//second scan of the tree
		    root.accept(llvm_com, null);
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
