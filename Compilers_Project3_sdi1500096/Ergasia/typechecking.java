import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class typechecking extends GJDepthFirst<String, String>{

	//private List<symClass> symboltable = new ArrayList<>();

	private Map< String,symClass> symbolmap = new LinkedHashMap< String,symClass>();  //using a type of hashtable with 10 buckets(for this exercise)

	private int var_counter;                        //its like a global variable for this class that becomes 0 at the start of an ex list and it goes +1 up for each Expression we meet
	private int total_expressions=0;
  ArrayList<Integer> all_expression_counters = new ArrayList<Integer>();		//its an array list of variable counters for every expression list
	/**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
   public String visit(MainClass n, String argu) throws Exception {
      String _ret=null;

      n.f0.accept(this, argu);
      String cname=n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);
      n.f10.accept(this, argu);
      n.f11.accept(this, argu);
      n.f12.accept(this, argu);
      n.f13.accept(this, argu);
      n.f14.accept(this, argu);

      n.f15.accept(this, cname+" main");
      n.f16.accept(this, argu);
      n.f17.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public String visit(ClassDeclaration n, String argu) throws Exception {
      String _ret=null;
      n.f0.accept(this, argu);

      String cname=n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, cname);
      n.f5.accept(this, argu);

      return _ret;
   }


   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
      String _ret=null;
      n.f0.accept(this, argu);
      String cname=n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, cname);
      n.f7.accept(this, argu);

      return _ret;
   }



	/**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n, String argu) throws Exception {
      String _ret=null;
      // n.f0.accept(this, argu);
      // n.f1.accept(this, argu);
      String type=n.f0.accept(this, argu);
			//System.out.println(type);
      String varname=n.f1.accept(this, argu);
      int flag=0;
      if(type.equals("int")){				//check if the type exists
      	flag=1;
      }
      else if(type.equals("boolean")){
      	flag=1;
      }
      else if(type.equals("int[]")){
      	flag=1;
      }
			else if(type.equals("boolean[]")){
      	flag=1;
      }
      else{								//if it is a defined class type
				if(symbolmap.containsKey(type)){
						flag=1;
				}
      }
      if(flag==0){
      	throw new MyCustomException("the variable "+varname+" type "+type+" is not declared");		//check if the variavble type name exists
      }


      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */

   public String visit(FormalParameter n, String argu) throws Exception {
      String _ret=null;
      String type=n.f0.accept(this, argu);
      String varname=n.f1.accept(this, argu);

      // n.f0.accept(this, argu);
      // n.f1.accept(this, argu);

      int flag=0;					//check if the type exists
      if(type.equals("int")){
      	flag=1;
      }
      else if(type.equals("boolean")){
      	flag=1;
      }
      else if(type.equals("int[]")){
      	flag=1;
      }
			else if(type.equals("boolean[]")){
      	flag=1;
      }
      else{								//if it is a defined class type
				if(symbolmap.containsKey(type)){
						flag=1;
				}
      }
      if(flag==0){
      	throw new MyCustomException("the parametre "+varname+" type "+type+" is not declared"); 		//check if the parameter type exists
      }

      return _ret;
   }


   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public String visit(MethodDeclaration n, String argu) throws Exception {
      String _ret=null;

      String type=n.f1.accept(this, argu);
      String funname=n.f2.accept(this, argu);
      int flag=0;					//check if the type exists
      if(type.equals("int")){
      	flag=1;
      }
      else if(type.equals("boolean")){
      	flag=1;
      }
      else if(type.equals("int[]")){
      	flag=1;
      }
			else if(type.equals("boolean[]")){
      	flag=1;
      }
      else{
				if(symbolmap.containsKey(type)){		//if it is a defined class type
						flag=1;
				}

      }
      if(flag==0){
      	throw new MyCustomException("the function "+funname+" type "+type+" is not declared");	//check if the function type exists
      }

      //n.f0.accept(this, argu);
      // n.f1.accept(this, argu);
      // n.f2.accept(this, argu);
      //n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      //n.f5.accept(this, argu);
      //n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu+" "+funname);
      //n.f9.accept(this, argu);
      String funtype=n.f10.accept(this, argu+" "+funname);
      //n.f11.accept(this, argu);
      //n.f12.accept(this, argu);

      if(!type.equals(funtype)){				//wrong return type
				int flag2=0;
				if(symbolmap.containsKey(funtype)){		//if it is a defined class type
						symClass c=symbolmap.get(funtype);
						String parent=c.get_exname();
						while(parent!=null){						//check if we return a parent instead
							symClass p=symbolmap.get(parent);
							if(type.equals(parent)){
								flag2=1;
								break;
							}
							parent=p.get_exname();
						}
				}
				if(flag2==0)
					throw new MyCustomException("the function "+funname+" return "+funtype+" instead of "+type);


      }
      return _ret;
   }



   														//for all the following classes the Expression returns the type of itself
   														//and the primary expression

															//also checks if each piece of each expression is the correct type
   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */

   public String visit(AndExpression n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);
      if(! pr1.equals("boolean")){
			throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable1 "+pr1+" is not boolean at &&");
      }

     //n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      //System.out.println("____"+pr2);
      if(! pr2.equals("boolean")){
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable2 "+pr2+" is not boolean at &&");

      }

      return "boolean";
   }


   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);
      if(! pr1.equals("int")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable1 "+pr1+" is not int at <");
      }

     //n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      //System.out.println("____"+pr2);
      if(! pr2.equals("int")){
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable2 "+pr2+" is not int at <");
      }

      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */

   public String visit(PlusExpression n, String argu) throws Exception {
     String _ret=null;
      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);
      if(! pr1.equals("int")){
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable1 "+pr1+" is not int at +");
      }

     //n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      //System.out.println("____"+pr2);
      if(! pr2.equals("int")){
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable2 "+pr2+" is not int at +");
      }
      return "int";
   }


   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */

   public String visit(MinusExpression n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);

      if(! pr1.equals("int")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable1 "+pr1+" is not int at -");
      }

     //n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      //System.out.println("____"+pr2);
      if(! pr2.equals("int")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable2 "+pr2+" is not int at -");

      }
      return "int";
   }


   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */

   public String visit(TimesExpression n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);
      if(! pr1.equals("int")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable1 "+pr1+" is not int at *");
      }

     //n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      //System.out.println("____"+pr2);
      if(! pr2.equals("int")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable2 "+pr2+" is not int at *");

      }
      return "int";
   }




   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n, String argu) throws Exception {
      String _ret=null;

      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);
      if(!pr1.equals("int[]") && !pr1.equals("boolean[]")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayLookup the variable1 "+pr1+" is not int[]/boolean[]");
      }

     //n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      //System.out.println("____"+pr2);
      if(! pr2.equals("int")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayLookup the variable2 "+pr2+" is not int");

      }
			if(pr1.equals("int[]")){
					return "int";
      }
			if(pr1.equals("boolean[]")){
					return "boolean";
      }

      return "hi";
   }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n, String argu) throws Exception {
      String _ret=null;

      String[] c_f=argu.split(" ");
      //System.out.println(argu+"____");
      String pr1=n.f0.accept(this, argu);
      //System.out.println("____"+pr1);
      if(!pr1.equals("int[]") && !pr1.equals("boolean[]")){
      	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable1 "+pr1+" is not int[]/boolean[]");

      }
      return "int";
   }



/**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public String visit(MessageSend n, String argu) throws Exception {
      String _ret=null;

      String[] c_f=argu.split(" ");
      // System.out.println(argu);

      String pr1=n.f0.accept(this, c_f[0]+" "+c_f[1]);
      String fun=n.f2.accept(this, null);
      String type=null;

      int par_num=0;

      String parent=null;		//null to be sure that it will initial
      	int flag=0;					//this flag is to check if the class is exists
      	int flag1=0;				//if this function is inside the parents and 2nd check if its inside the class

				if(symbolmap.containsKey(pr1)){
					flag=1;
					symClass p=symbolmap.get(pr1);
					parent=p.get_exname();
					while(parent!=null){
						symClass p2=symbolmap.get(parent);
						if(p2.get_funcs().containsKey(fun)){
							symFunc f2=p2.get_funcs().get(fun);
							flag1=1;
							par_num=f2.get_num_par();
							type=f2.get_type();

							break;
						}
						else{
							parent=p2.get_exname();
						}

					}
					if(p.get_funcs().containsKey(fun)){
						flag1=1;
						par_num=p.get_funcs().get(fun).get_num_par();
						type=p.get_funcs().get(fun).get_type();
					}
				}
        if(flag==0){
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the class name "+pr1+" does not exists");
        }
        if(flag1==0){
        	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the function "+fun+" does not exists in class "+pr1);
        }
				///////////////////////
				if(!symbolmap.get(pr1).get_funcs().containsKey(fun)){
					pr1=parent;
				}
				///////////////////////////////
				all_expression_counters.add(0);			//add a new expression list counter for this new expression and keep its position in the arraylist all_expression_counters
				int position=all_expression_counters.size() - 1;
				total_expressions++;
        n.f4.accept(this, c_f[0]+" "+c_f[1]+" "+pr1+" "+fun+" "+(total_expressions-1));	//pass to expression list the class and the function that i am refering to and also the position of the expression counter
				//after all the ex are checked we see if the var_counter is the same with the num of parameters we want
        if(all_expression_counters.get(position)+1<par_num){		//means that the parameters ar more than the Expressions
        	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the function "+fun+" in class "+pr1+"is declared with more parameters");
        }

      //n.f1.accept(this, argu);


      //n.f3.accept(this, argu);
      //n.f4.accept(this, argu+" "+fun);
      //n.f5.accept(this, argu);
      return type;
   }


   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, String argu) throws Exception {
      String _ret=null;
			String[] c_f=argu.split(" ");
			int expr_num=Integer.parseInt(c_f[4]);		//get the position in the all_expression_counters in order to get these expression counter
      //all_expression_counters.get(expr_num)=0;			//a counter to show me whose parameter turn is to be checkd
      int par_num=0;
      ArrayList<String> pars = new ArrayList<String>();		//an arraylist of the parameters that the function has
      String ex1=n.f0.accept(this, c_f[0]+" "+c_f[1]);
      // System.out.println("AAAA"+ex1);
			symClass p=symbolmap.get(c_f[2]);

			symFunc f=p.get_funcs().get(c_f[3]);;

			par_num=f.get_num_par();

			for(String variable : f.get_pars().keySet()){
				pars.add(f.get_pars().get(variable).get_type());	//init the array list
			}

      String parent=null;
			if(symbolmap.containsKey(ex1)){
				symClass c=symbolmap.get(ex1);
				parent=c.get_exname();
				while(parent!=null){
					symClass p2=symbolmap.get(parent);
					if(pars.get(all_expression_counters.get(expr_num)).equals(parent)){	//get the type of the parent that has the type we want and break
	  				break;
	  			}
					parent=p2.get_exname();
				}
			}

			// System.out.println("	THE PAR = "+pars.get(all_expression_counters.get(expr_num)));
      if(par_num<all_expression_counters.get(expr_num)+1){		//means that the Expression list is bigger than the parameters of the function
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in function "+c_f[3]+" the "+all_expression_counters.get(expr_num)+" parametre is not declared");
      }
      if(parent==null){
      		if(!pars.get(all_expression_counters.get(expr_num)).equals(ex1)){		//check if the type is equal with the all_expression_counters.get(expr_num) parameter
	      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in function "+c_f[3]+" the "+all_expression_counters.get(expr_num)+" parametre is not that "+ex1+" type");
	      }
      }
      else{
      		if(!pars.get(all_expression_counters.get(expr_num)).equals(ex1) && !pars.get(all_expression_counters.get(expr_num)).equals(parent)){		//check if the type is a parent of the type we use (if a type is a class)
	      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in function "+c_f[3]+" the "+all_expression_counters.get(expr_num)+" parametre is not that "+ex1+" type");
	     	 }
      }

      n.f1.accept(this, argu);
      return _ret;
   }


	/**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, String argu) throws Exception {
      String _ret=null;
			String[] c_f=argu.split(" ");
			int expr_num=Integer.parseInt(c_f[4]);		//again get the correct expression num to see the order o the expressions
			int new_all_expression_counters=all_expression_counters.get(expr_num)+1;
      all_expression_counters.set(expr_num, new_all_expression_counters);
      int par_num=0;
      ArrayList<String> pars = new ArrayList<String>();

      String ex1=n.f1.accept(this, c_f[0]+" "+c_f[1]);

			symClass p=symbolmap.get(c_f[2]);
			symFunc f=p.get_funcs().get(c_f[3]);
			par_num=f.get_num_par();
			for(String variable : f.get_pars().keySet()){
				pars.add(f.get_pars().get(variable).get_type());		//take all the parameters
			}


			String parent=null;
			if(symbolmap.containsKey(ex1)){
				symClass c=symbolmap.get(ex1);
				parent=c.get_exname();
				while(parent!=null){
					symClass p2=symbolmap.get(parent);
					if(pars.get(all_expression_counters.get(expr_num)).equals(parent)){	//get the type of the parent that has the type we want
	  				break;
	  			}
					parent=p2.get_exname();
				}
			}
			//System.out.println("	THE PAR = "+pars.get(all_expression_counters.get(expr_num)));
      if(par_num<all_expression_counters.get(expr_num)+1){ 		//means that the Expression list is bigger than the parameters of the function
      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in function "+c_f[3]+" the "+all_expression_counters.get(expr_num)+" parametre is not declared");
      }
      if(parent==null){
	      if(!pars.get(all_expression_counters.get(expr_num)).equals(ex1)){				//check if the type is equal with the all_expression_counters.get(expr_num) parameter
	      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in function "+c_f[3]+" the "+all_expression_counters.get(expr_num)+" parametre is not that "+ex1+" type");
	      }
	  }
	  else{
	  		if(!pars.get(all_expression_counters.get(expr_num)).equals(ex1) && !pars.get(all_expression_counters.get(expr_num)).equals(parent)){					//check if the type is a parent of the type we use (if a type is a class)
	      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in function "+c_f[3]+" the "+all_expression_counters.get(expr_num)+" parametre is not that "+ex1+" type");
	     	 }
	  }
      //n.f0.accept(this, argu);
      //n.f1.accept(this, argu);
      return _ret;
   }


	/**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public String visit(AssignmentStatement n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");

      String ex1=n.f0.accept(this, null);
      //n.f1.accept(this, argu);
      String ex2=n.f2.accept(this, argu);
      //n.f3.accept(this, argu);
      String type1=null;
      //String pname=null;
			String parent=null;

			//System.out.println("hello1");
			symClass p=symbolmap.get(c_f[0]);
			parent=p.get_exname();
			symClass p2;
			while(parent!=null){				//find the type1
				p2=symbolmap.get(parent);
				if(p2.get_vars().containsKey(ex1)){
					type1=p2.get_vars().get(ex1).get_type();
				}
				parent=p2.get_exname();
			}
			if(p.get_vars().containsKey(ex1)){
				type1=p.get_vars().get(ex1).get_type();
			}
			symFunc f=p.get_funcs().get(c_f[1]);
			if(f.get_pars().containsKey(ex1)){
				type1=f.get_pars().get(ex1).get_type();
			}
			if(f.get_vars().containsKey(ex1)){
				type1=f.get_vars().get(ex1).get_type();
			}
		//System.out.println("hello2");
		parent=null;
		if(symbolmap.containsKey(ex2)){		//if expression is a class check the parents	A a=new B ==> B extends A
			symClass c=symbolmap.get(ex2);
			parent=c.get_exname();
			while(parent!=null){
				symClass c2=symbolmap.get(parent);
				if(type1.equals(parent)){
  				break;
  			}
				parent=c2.get_exname();
			}
		}

	  if(type1==null){ //type1 has the type of identifier
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"1in Assignment the variable "+ex1+" is not declared");
	  }
	  //System.out.println(type1+"___"+ex2);

	  if(parent==null){
	  	if(!type1.equals(ex2)){
		  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"2in Assignment the variable "+ex1+" has not the same type with expression "+ex2);
		  }
	  }
	  else{
	  	if(!type1.equals(ex2) && !type1.equals(parent)){				//check if the type is a parent of the type we use (if a type is a class)
		  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"3in Assignment the variable "+ex1+" has not the same type with expression "+ex2);
		  }
	  }

			//System.out.println("hello3");
      return _ret;
   }




   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n, String argu) throws Exception {	//checks if th ident is declared and if the whole statement has the correct types
      String _ret=null;
      String[] c_f=argu.split(" ");

      String type1=null;
      String ident=n.f0.accept(this, null);

      String ex1=n.f2.accept(this, argu);

      String ex2=n.f5.accept(this, argu);

			symClass p=symbolmap.get(c_f[0]);
			symFunc f=p.get_funcs().get(c_f[1]);
			if(p.get_vars().containsKey(ident)){
				type1=p.get_vars().get(ident).get_type();
			}
			if(f.get_pars().containsKey(ident)){
				type1=f.get_pars().get(ident).get_type();
			}
			if(f.get_vars().containsKey(ident)){
				type1=f.get_vars().get(ident).get_type();
			}

	  if(type1==null){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayAssignment the variable "+ident+" is not declared");
	  }
	  //System.out.println(type1+"___"+ex2);

	  if(!type1.equals("int[]") && !type1.equals("boolean[]") ){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayAssignment the variable "+ident+" is not int[]/boolean[] type");
	  }
	  if(!ex1.equals("int")){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayAssignment the Expression in [] is not int type");
	  }
	  if(!ex2.equals("int") && type1.equals("int[]")){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayAssignment the Expression after = is not int type");
	  }
	  if(!ex2.equals("boolean") && type1.equals("boolean[]")){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in ArrayAssignment the Expression after = is not boolean type");
	  }
      //n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
   public String visit(PrimaryExpression n, String argu) throws Exception {
   		String[] c_f=argu.split(" ");
      return n.f0.accept(this, c_f[0]+" "+c_f[1]+" "+1);	//the  1 is for a mask to send to the identifier in order to return the type of the ident and not hos name
   }



   															//regular scans for errors for while if and print statement
   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public String visit(IfStatement n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //n.f0.accept(this, argu);
      //n.f1.accept(this, argu);
      String ex1=n.f2.accept(this, argu);
      if(!ex1.equals("boolean")){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in IF the expression is not boolean type");
	  }
      //n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      //n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(WhileStatement n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //n.f0.accept(this, argu);
      //n.f1.accept(this, argu);
      String ex1=n.f2.accept(this, argu);
      if(!ex1.equals("boolean")){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in WHILE the expression is not boolean type");
	  }
      //n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      //n.f0.accept(this, argu);
      //n.f1.accept(this, argu);
      String ex1=n.f2.accept(this, argu);
      if(!(ex1.equals("boolean") || ex1.equals("int")) ){
	  	throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"in PRINT the expression is not boolean nor int type");
	  }
      //n.f3.accept(this, argu);
      //n.f4.accept(this, argu);
      return _ret;
   }







	/**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n, String argu) throws Exception{			//the return depends on the argu if it's not null then it means that is called from a
   																			//primary ex and returns the type of the idntifier, otherwise it returns the ident itself
				//System.out.println("IDENT1");
   	    if(argu==null){													//simple if argu is null the return the ident itself if not then return the type of thiw type(means that is called as primary ex)
   	    	return n.f0.toString();
   	    }
   		String ident=n.f0.toString();
   		//System.out.println(argu+"))))"+ident);
   		String[] c_f=argu.split(" ");
   		int flag=0;
   		String type=ident;
   		if(c_f.length==3){
				symClass p=symbolmap.get(c_f[0]);
				String parent=p.get_exname();
				//System.out.println(parent);
				while(parent!=null){
					//System.out.println(parent);
					symClass p2=symbolmap.get(parent);
					if(p2.get_vars().containsKey(ident)){
						flag=1;
						type=p2.get_vars().get(ident).get_type();
					}
					parent=p2.get_exname();
				}
				//System.out.println(type);
				symFunc f=p.get_funcs().get(c_f[1]);
				if(f.get_vars().containsKey(ident)){
					flag=1;
					return f.get_vars().get(ident).get_type();
				}
				if(f.get_pars().containsKey(ident)){
					flag=1;
					return f.get_pars().get(ident).get_type();
				}
				if(p.get_vars().containsKey(ident)){
					flag=1;
					return p.get_vars().get(ident).get_type();
				}
      		if(flag==0){					//if type is not found then the iden is not defined
	      		throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the Identifier "+ident+" not found");
	      	}
	      	return type;
   		}

      return ident;
   }


	 /**
    * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(BooleanArrayType n, String argu) throws Exception {
      return n.f0.toString()+n.f1.toString()+n.f2.toString();
   }

      /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(IntegerArrayType n, String argu) throws Exception{
      return n.f0.toString()+n.f1.toString()+n.f2.toString();
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n, String argu) throws Exception{
      return n.f0.toString();
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n, String argu) throws Exception{
      return n.f0.toString();
   }


   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n, String argu) throws Exception {
      String[] c_f=argu.split(" ");
      return c_f[0];
   }


   /**
    * f0 -> "true"
    */
   public String visit(TrueLiteral n, String argu) throws Exception {
      return "boolean";
   }

   /**
    * f0 -> "false"
    */
   public String visit(FalseLiteral n, String argu) throws Exception {
      return "boolean";
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n, String argu) throws Exception {
      return "int";
   }


	 /**
    * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
		 String _ret=null;
		 //n.f0.accept(this, argu);
		 //n.f1.accept(this, argu);
		 //n.f2.accept(this, argu);
		 // System.out.println("HEREEEE");
		 String ex1=n.f3.accept(this, argu);
		 if(!ex1.equals("int")){
				 throw new MyCustomException("the expression that used inside boolean[] is not int type");
		 }
		 //n.f4.accept(this, argu);
		 return "boolean[]";
   }


	/**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
      String _ret=null;
      //n.f0.accept(this, argu);
      //n.f1.accept(this, argu);
      //n.f2.accept(this, argu);
      // System.out.println("HEREEEE");
      String ex1=n.f3.accept(this, argu);
      if(!ex1.equals("int")){
      		throw new MyCustomException("the expression that used inside int[] is not int type");
      }
      //n.f4.accept(this, argu);
      return "int[]";
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n, String argu) throws Exception {
      String _ret=null;
      //n.f0.accept(this, argu);
      //n.f1.accept(this, argu);
      //n.f2.accept(this, argu);
      //n.f3.accept(this, argu);
      String cname=n.f1.accept(this, null);
      int flag=0;
			if(!symbolmap.containsKey(cname)){
				throw new MyCustomException("the class name "+cname+" does not exists");
			}
      return cname;
   }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public String visit(NotExpression n, String argu) throws Exception {
      String _ret=null;
      //n.f0.accept(this, argu);
      String ex1=n.f1.accept(this, argu);
      if(!ex1.equals("boolean")){
      		throw new MyCustomException("the expression that used with ! is not boolean type");
      }
      return "boolean";
   }


   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n, String argu) throws Exception {
      String _ret=null;
      //n.f0.accept(this, argu);
      String[] c_f=argu.split(" ");
		return n.f1.accept(this, c_f[0]+" "+c_f[1]);
      //n.f2.accept(this, argu);
      //return n.f1.accept(this, argu);
   }



	public void set_symbolmap(Map< String,symClass> a){		//get the symbolmap from main and set it here
		 this.symbolmap=a;
	}



	public void printV(){			//print of the offsets

			System.out.println("Printing the offset...");
			for (String k1 : symbolmap.keySet()) {
				 symClass c=symbolmap.get(k1);
				 if(c.get_hasmain()==1){
					 //System.out.println("MPIKE\n");
					 continue;
				 }
						System.out.println("-----------Class "+c.get_cname()+"-----------");
						System.out.println("---Variables---");
						for(String k2 : c.get_vars().keySet()){
							symVar v=c.get_vars().get(k2);
						 System.out.println(c.get_cname()+"."+v.get_name()+" : "+v.get_offset());
						}
						System.out.println("---Methods---");
				 for(String k3: c.get_funcs().keySet()){
					 symFunc f=c.get_funcs().get(k3);
					 if(f.get_printed()==0){
						 System.out.println(c.get_cname()+"."+f.get_name()+"()"+" : "+f.get_offset());
					 }
				 }
		 }
	 }


}
