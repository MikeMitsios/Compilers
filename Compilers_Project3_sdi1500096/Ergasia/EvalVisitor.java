import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;


// int key_producer(string str){
//
//   int l = str.length();
//   int sum = 0;
//   long long int bigSum = 0L;
//   for (int i = 0; i < l; i++) {
//     sum +=  str[i];
//   }
//   return sum%10;
// }

public class EvalVisitor extends GJDepthFirst<String, String>{

  //private List<symClass> symboltable = new ArrayList<>();

  private Map< String,symClass> symbolmap = new LinkedHashMap< String,symClass>();  //using a type of hashtable with 10 buckets(for this exercise)

  private int offset;


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
   public String visit(MainClass n, String argu) throws Exception{
      String _ret=null;
      //n.f0.accept(this, argu);
      String cname=n.f1.accept(this, argu);
      symClass cl= new symClass(cname,1,null);
      symFunc fun= new symFunc("main","void");			//save at symbol map the main function and class
      String mainstr=n.f11.accept(this, argu);

      symVar variable= new symVar(mainstr,"String[]");  //special var for main
      fun.add_var(variable);
      cl.add_funct(fun);
      symbolmap.put(cname,cl);
      // n.f2.accept(this, argu);
      // n.f3.accept(this, argu);
      // n.f4.accept(this, argu);
      // n.f5.accept(this, argu);
      // n.f6.accept(this, argu);
      // n.f7.accept(this, argu);
      // n.f8.accept(this, argu);
      // n.f9.accept(this, argu);
      // n.f10.accept(this, argu);

      // n.f12.accept(this, argu);
      // n.f13.accept(this, argu);
      n.f14.accept(this, cname+" "+"main");
      n.f15.accept(this, argu);
      // n.f16.accept(this, argu);
      // n.f17.accept(this, argu);
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

  public String visit(ClassDeclaration n, String argu) throws Exception{
      //R _ret=null;
      //n.f0.accept(this, argu);
  	  //System.out.println(n.f1.accept(this, argu));

  	  String cname=n.f1.accept(this, argu);
  	  symClass cl= new symClass(cname,0,null);
  	  if(symbolmap.containsKey(cname)){
        throw new MyCustomException("the class "+cname+" already exists");		//error if the name exists
      }


      symbolmap.put(cname,cl);

      //n.f2.accept(this, argu);
      offset=0;
      n.f3.accept(this, "/"+" "+cname);			//a mask to show the Variable visitor that this specific variables are from a class
      cl.set_offset1(offset);
      offset=0;

      n.f4.accept(this, cname);

      cl.set_offset2(offset);
      //n.f5.accept(this, argu);


      return "hi";				//return trash we dont care about the return value
      //return _ret;
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
   public String visit(ClassExtendsDeclaration n, String argu) throws Exception{	//its the same type as ClassDeclaration Except the second error
      String _ret=null;
      //n.f0.accept(this, argu);
      String cname=n.f1.accept(this, argu);
      int offset_var=0;						//set the 2 types of offset to 0
      int offset_fun=0;
      //n.f1.accept(this, argu);
      //n.f2.accept(this, argu);
      String parent=n.f3.accept(this, argu);
      symClass cl= new symClass(cname,0,parent);
      int flag=0;

  	  	if(symbolmap.containsKey(cname)){
  	  		throw new MyCustomException("the class "+cname+" already exists");
  	  	}

        symClass p=symbolmap.get(parent);
  	  	if(symbolmap.containsKey(parent)){
  	  		offset_var=p.get_offset1();
  	  		offset_fun+=p.get_offset2();
  	  	}
        else{
          throw new MyCustomException("the parent class "+parent+" not found");
        }

      symbolmap.put(cname,cl);

      //n.f3.accept(this, argu);
      //n.f4.accept(this, argu);
      offset=offset_var;
      n.f5.accept(this, "/"+" "+cname);		//again the same mask here
      cl.set_offset1(offset);
      offset=offset_fun;

      n.f6.accept(this, cname);



      cl.set_offset2(offset);

      //n.f7.accept(this, argu);
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
   public String visit(MethodDeclaration n, String argu) throws Exception{
      String _ret=null;
      //n.f0.accept(this, argu);
      String type=n.f1.accept(this, argu);
      String funname=n.f2.accept(this, argu);
      symFunc fun= new symFunc(funname,type);
      fun.set_offset(offset);
      offset=offset+8;						//this is standar
      symClass p=symbolmap.get(argu);

      if(p.get_funcs().containsKey(funname)){
        throw new MyCustomException("In Class "+p.get_cname()+" the function "+funname+" already exists");
      }

      p.add_funct(fun);
      n.f4.accept(this, argu+" "+funname);
      String parent=p.get_exname();
			while(parent!=null){
        symClass p2=symbolmap.get(parent);
        symFunc f2;
				if(p2.get_funcs().containsKey(funname)){
          f2=p2.get_funcs().get(funname);
					fun.set_offset(f2.get_offset());	//here it change the offset because it found the same function from the parent
          fun.set_printed(1);
					offset=offset-8;
					if(!fun.get_type().equals(f2.get_type()) ){
						throw new MyCustomException("In Class "+p.get_cname()+" the function "+funname+" has not the same type ("+fun.get_type()+") as the one in the parent class "+p2.get_cname()+"/"+f2.get_type());
					}
					if(fun.get_num_par()!=f2.get_num_par()){
						throw new MyCustomException("In Class "+p.get_cname()+" the function "+funname+" has not the same num of parametres with "+f2.get_name()+" as the one in the parent class "+p2.get_cname());
					}
					int i=0;

          List<symVar> parameters_l1 = new ArrayList<symVar>(f2.get_pars().values());
          List<symVar> parameters_l2 = new ArrayList<symVar>(fun.get_pars().values());
					while(i<f2.get_pars().size() && i<fun.get_pars().size() ){		//when the name and type s the same this while scans to see if the declared variables is the same typ inorder
            if(! parameters_l1.get(i).get_type().equals(parameters_l2.get(i).get_type()) ){
              throw new MyCustomException("In Class "+p.get_cname()+" the function "+funname+" has not the same type in variable"+(i+1)+" "+parameters_l1.get(i).get_type()+"/"+parameters_l2.get(i).get_type()+" as the one in the parent class "+p2.get_cname());
            }
            i++;

					}
          break;
				}
        parent=p2.get_exname();



			}

      n.f7.accept(this, argu+" "+funname);	//a mask that pass 2 informtions the class and the function
      n.f8.accept(this, argu);

      n.f10.accept(this, argu);

      return _ret;
   }



   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n, String argu) throws Exception{

   	  String[] c_f = argu.split(" ");
      String type=n.f0.accept(this, argu);
      //System.out.println(type);
      String varname=n.f1.accept(this, argu);
      symVar variable= new symVar(varname,type);


      if(c_f[0].equals("/")){			//check to see if the variable is a class variable or a function inside a class
      	//System.out.println("MPAINEI");
      	variable.set_offset(offset);	//set and change the offset
	      if(type.equals("int")){
	      	offset=offset+4;
	      }
	      else if(type.equals("boolean")){
	      	offset=offset+1;
	      }
	      else{
	      	offset=offset+8;
	      }
        symClass p=symbolmap.get(c_f[1]);
        if(p.get_vars().containsKey(varname) ){
          throw new MyCustomException("the variable name "+varname+" in class "+p.get_cname()+" already exists");
        }
        p.add_var(variable);
      }
      else{						//in case of function variables we dont calculate the offset just save them at the right place in the symboltable
        symClass p=symbolmap.get(c_f[0]);
      	symFunc f=p.get_funcs().get(c_f[1]);
        if(f.get_vars().containsKey(varname) || f.get_pars().containsKey(varname)){
          throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the variable name "+varname+" in function "+f.get_name()+" already exists");
        }
        f.add_var(variable);

      }

      return n.f0.accept(this, argu)+" "+n.f1.accept(this, argu);
   }



	 /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n, String argu) throws Exception{	//The parameters has their own LinkedHashMap in each symFunc class
      String _ret=null;

      String[] c_f = argu.split(" ");
      String type=n.f0.accept(this, argu);
      String varname=n.f1.accept(this, argu);
      symVar variable= new symVar(varname,type);
      if(type!=null && variable!=null){
        symClass p=symbolmap.get(c_f[0]);
      	symFunc f=p.get_funcs().get(c_f[1]);
      	if(f.get_pars().containsKey(varname)){
          throw new MyCustomException(c_f[0]+"===="+c_f[1]+"/"+"the Parameter name "+varname+" already exists");
        }
        f.add_parameters(variable);
        f.add_par();

        //System.out.println(p.get_cname()+" "+f.get_name()+"() "+f.get_vars().get(f.get_vars().size() - 1).get_name() );

      }
      // n.f0.accept(this, argu);
      // n.f1.accept(this, argu);
      return _ret;
   }

    /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n, String argu) throws Exception{	//no need to accapt just return the value
   	 // System.out.println(n.f0.toString());
      return n.f0.toString();
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
   public String visit(IntegerArrayType n, String argu) throws Exception{	//no need to accapt just return the value
      return n.f0.toString()+n.f1.toString()+n.f2.toString();
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n, String argu) throws Exception{	//no need to accapt just return the value
      return n.f0.toString();
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n, String argu) throws Exception{	//no need to accapt just return the value
      return n.f0.toString();
   }




   public Map< String,symClass> get_symbolmap(){
   		return symbolmap;
   }


   public void printV(){				//a help function to print the symboltable (debuggng)

   	//System.out.println("KANEI PRINT");
   	 for (String c : symbolmap.keySet()) {
            System.out.println(c);
            System.out.println(symbolmap.get(c).get_exname());
            for(String v : symbolmap.get(c).get_vars().keySet()){
            	System.out.println(symbolmap.get(c).get_vars().get(v).get_type()+" "+v);
            }

        	for(String f : symbolmap.get(c).get_funcs().keySet()){
        		System.out.println("\t"+symbolmap.get(c).get_funcs().get(f).get_type()+" "+f+"()");
            for(String fp : symbolmap.get(c).get_funcs().get(f).get_pars().keySet()){
        			System.out.println("\t\t"+symbolmap.get(c).get_funcs().get(f).get_pars().get(fp).get_type()+" "+fp);
        		}
        		for(String fv : symbolmap.get(c).get_funcs().get(f).get_vars().keySet()){
        			System.out.println("\t\t"+symbolmap.get(c).get_funcs().get(f).get_vars().get(fv).get_type()+" "+fv);
        		}

        	}


     }
   }

   public void printV2(){			//print of the offsets

    	 System.out.println("Printing the offset...");
    	 for (String k1 : symbolmap.keySet()) {
          symClass c=symbolmap.get(k1);
    	 		if(c.get_hasmain()==1){
            //System.out.println("MPIKE\n");
    	 			continue;
    	 		}
             System.out.println("-----------"+c.get_cname()+"-----------");
             System.out.println("---Variables---");
             for(String k2 : c.get_vars().keySet()){
               symVar v=c.get_vars().get(k2);
             	System.out.println(c.get_cname()+"."+v.get_name()+" : "+v.get_offset());
             }
             System.out.println("---Methods---");
         	for(String k3: c.get_funcs().keySet()){
            symFunc f=c.get_funcs().get(k3);
         		System.out.println(c.get_cname()+"."+f.get_name()+"()"+" : "+f.get_offset());


         	}


      }
    }

}

//These are the classes that my symboltable made of

class symClass{

	private String cname;
	private int hasmain;	//if the class is main class
	private String exname;
	private int offset1;	//on offset for variables and the other for functions for inheritance
	private int offset2;
	private Map< String,symFunc> functs = new LinkedHashMap< String,symFunc>();	//two other LinkedHashMap that are saved the NAMES of the functions and variables the main has
	private Map< String,symVar> vars = new LinkedHashMap< String,symVar>();

	public symClass(String name,int num , String parent){
        this.cname = name;
        this.hasmain=num;
        this.exname = parent;
    }

    public String get_cname(){
    	return cname;
    }

    public String get_exname(){
    	return exname;
    }

    public int get_hasmain(){
    	return hasmain;
    }

    public void add_funct(symFunc func){
    	functs.put(func.get_name(),func);
    }

    public Map< String,symFunc> get_funcs(){
    	return functs;
    }


    public void add_var(symVar var){
    	vars.put(var.get_name(),var);
    }

    public Map< String,symVar> get_vars(){
    	return vars;
    }

    public void set_offset1(int a){
		this.offset1=a;
	}

	public void set_offset2(int a){
		this.offset2=a;
	}

	public int get_offset1(){
		return this.offset1;
	}

	public int get_offset2(){
		return this.offset2;
	}

}

class symFunc{
	private String name;
	private String type;
	private int num_par;	//number f parameters
	private int offset;
  private int printed;  //printed or not the offset
	private Map< String,symVar> vars = new LinkedHashMap< String,symVar>();	//the LinkedHashMap of the names of variables each function has
  private Map< String,symVar> parameters = new LinkedHashMap< String,symVar>(); //the LinkedHashMap of the parameters in each function

	public symFunc(String name,String type){
		this.name=name;
		this.type=type;
		this.num_par=0;
    this.printed=0;
	}

	public String get_name(){
		return name;
	}

	public String get_type(){
		return type;
	}

	public int get_num_par(){
		return num_par;
	}

	public void add_var(symVar var){
    	vars.put(var.get_name(),var);
    }

  public void add_parameters(symVar par){
    	parameters.put(par.get_name(),par);
    }

  public void add_par(){
  	num_par++;
  }

  public Map<String,symVar> get_vars(){
  	return vars;
  }

  public Map<String,symVar> get_pars(){
  	return parameters;
  }

  public void set_offset(int a){
		this.offset=a;
	}

  public void set_printed(int a){
		this.printed=a;
	}
  public int get_printed(){
		return this.printed;
	}


	public int get_offset(){
		return this.offset;
	}
}

class symVar{

	private String name;
	private String type;
	private int var;
	private String value;
	private int offset;

	public symVar(String name,String type){
		this.name=name;
		this.type=type;
		//this.var=v_p;
	}


	public String get_name(){
		return name;
	}

	public String get_type(){
		return type;
	}

	public int get_var(){
		return this.var;
	}

	public void set_offset(int a){
		this.offset=a;
	}

	public int get_offset(){
		return this.offset;
	}

}
