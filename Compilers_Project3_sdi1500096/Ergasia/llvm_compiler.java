import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import java.io.*;

public class llvm_compiler extends GJDepthFirst<String, String>{

  private Map< String,symClass> symbolmap = new LinkedHashMap< String,symClass>();  //using a type of hashtable with 10 buckets(for this exercise)

  private Map< String,String> Var_Type = new LinkedHashMap< String,String>(); //a temp help map that holds the variables and the types in each function in order to call the type

  private int var_counter;			//its like a global variable for this class that becomes 0 at the start of an ex list and it goes +1 up for each Expression we meet
  private String all_vars=null; // a temp string for each expression list
  private int total_expressions=0;
  ArrayList<Integer> all_expression_counters = new ArrayList<Integer>();
  ArrayList<String> all_expression_strings = new ArrayList<String>();

	private String file_name;

	private void emit(String buffer) {        //A function to use in order to write the string in my file
    try {
    BufferedWriter out = new BufferedWriter(new FileWriter(file_name,true));out.write(buffer);out.close();
    } catch (Exception ex) {
        System.err.println(ex.getMessage());
    }
  }

	private int var_num=0;      //the registers for the different types
  private int if_then_num=0;  //the registers for if statement
  private int if_else_num=0;
  private int if_end_num=0;
  private int exp_res_num=0;
  private int loop_num=0;
  private int loop_then_num=0;
  private int loop_end_num=0;
  private int nsz_ok_num=0;
  private int nsz_err_num=0;
  private int oob_ok_num=0;
  private int oob_err_num=0;

	String get_var_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=var_num;
    var_num++;
    return "%_"+i;
  }

  String get_if_then_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=if_then_num;
    if_then_num++;
    return "%if_then_"+i;
  }

  String get_if_else_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=if_else_num;
    if_else_num++;
    return "%if_else_"+i;
  }

  String get_if_end_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=if_end_num;
    if_end_num++;
    return "%if_end_"+i;
  }

  String get_exp_res_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=exp_res_num;
    exp_res_num++;
    return "%exp_res_"+i;
  }

  String get_loop_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=loop_num;
    loop_num++;
    return "%loop_"+i;
  }

  String get_loop_then_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=loop_then_num;
    loop_then_num++;
    return "%loop_then_"+i;
  }

  String get_loop_end_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=loop_end_num;
    loop_end_num++;
    return "%loop_end_"+i;
  }

  String get_nsz_ok_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=nsz_ok_num;
    nsz_ok_num++;
    return "%nsz_ok_"+i;
  }

  String get_nsz_err_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=nsz_err_num;
    nsz_err_num++;
    return "%nsz_err_"+i;
  }

  String get_oob_ok_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=oob_ok_num;
    oob_ok_num++;
    return "%oob_ok_"+i;
  }

  String get_oob_err_num(){            //THIS ARE THE FUNCTIONS TO CHECK AND UPDATE THE DIFFERENT TYPES OF REGISTERS THAT I USE
    int i=oob_err_num;
    oob_err_num++;
    return "%oob_err_"+i;
  }


	private void zero_all(){        //a function to restart the registers that i use
      this.var_num=0;
      this.if_then_num=0;
      this.if_else_num=0;
      this.if_end_num=0;
      this.exp_res_num=0;
      this.loop_num=0;
      this.loop_end_num=0;
      this.loop_then_num=0;
      this.nsz_ok_num=0;
      this.nsz_err_num=0;
      this.oob_ok_num=0;
      this.oob_err_num=0;
  }

	public static String removeLastChar(String s) {
    if(s.charAt(s.length() - 2)==','){
      return s.substring(0, s.length() - 2);
    }
    return s;
}

public int total_methods(String cname){   // a function in order to get the total methods of a class
	Map< Integer,symFunc> v_table_helper = new LinkedHashMap< Integer,symFunc>();
	Map< Integer,String> v_table_function_owner = new LinkedHashMap< Integer,String>();
	symClass c=symbolmap.get(cname);
	String parent=c.get_exname();
	while(parent!=null){
		symClass p2=symbolmap.get(parent);

		for(String k3: p2.get_funcs().keySet()){
			symFunc f=p2.get_funcs().get(k3);

			if(f.get_printed()==0){
         // System.out.println("FUNCTION "+f.get_name());
         if(!v_table_helper.containsKey(f.get_offset()/8)){
  				 v_table_helper.put(f.get_offset()/8,f);
  				 v_table_function_owner.put(f.get_offset()/8,parent);
         }
			}

		}
		parent=p2.get_exname();
	}
	for(String k3: c.get_funcs().keySet()){
		symFunc f=c.get_funcs().get(k3);
    if(!f.get_name().equals("main")){
      v_table_helper.put(f.get_offset()/8,f);
			v_table_function_owner.put(f.get_offset()/8,c.get_cname());
    }


	}
	return v_table_helper.size();
}



/**
  * f0 -> MainClass()
  * f1 -> ( TypeDeclaration() )*
  * f2 -> <EOF>
  */
 public String visit(Goal n, String argu) throws Exception {
    String _ret=null;


    for (String k1 : symbolmap.keySet()){
      Map< Integer,symFunc> v_table_helper = new LinkedHashMap< Integer,symFunc>();       //has all the functions of the classes
      Map< Integer,String> v_table_function_owner = new LinkedHashMap< Integer,String>(); //has the true owners of the functions above
      String emit_string;
      // System.out.println(k1);
      symClass c=symbolmap.get(k1);
      if(c.get_hasmain()==1){
        //System.out.println("MPIKE\n");
        continue;
      }
      String parent=c.get_exname();
      while(parent!=null){
        symClass p2=symbolmap.get(parent);
        // System.out.println(parent);
        for(String k3: p2.get_funcs().keySet()){
          symFunc f=p2.get_funcs().get(k3);
          // System.out.println(k3);
          if(f.get_printed()==0){
            // System.out.println("MPIKE");
            if(!v_table_helper.containsKey(f.get_offset()/8)){
              v_table_helper.put(f.get_offset()/8,f);          //posistion the ofset of the function in the parent classes
              v_table_function_owner.put(f.get_offset()/8,parent);     //the owner is the parent
            }

          }
        }
        parent=p2.get_exname();
      }
      for(String k3: c.get_funcs().keySet()){
        symFunc f=c.get_funcs().get(k3);
                                                    //override the above placement and put in position all the function of the classwe are
          v_table_helper.put(f.get_offset()/8,f);
          v_table_function_owner.put(f.get_offset()/8,c.get_cname());     //the owner is the class itself

      }

      emit_string="@."+c.get_cname()+"_vtable = global ["+v_table_helper.size()+" x i8*] [";          // write the size o all the functions
      for(int i=0; i<v_table_helper.size(); i++) {        //for every unction in the v_table
        symFunc f=v_table_helper.get(i);
        // System.out.println("FUNCTION== "+v_table_helper.get(i).get_name());
        String own=v_table_function_owner.get(i);                                     //get the owner
        //System.out.println(own+"----"+f.get_name());
        emit_string=emit_string+"i8* bitcast (";
        if(f.get_type().equals("int")){
          emit_string=emit_string+"i32 ";
        }
        else if(f.get_type().equals("boolean")){
          emit_string=emit_string+"i1 ";
        }
        else if(f.get_type().equals("int[]")){
          emit_string=emit_string+"i32*";
        }
        else if(f.get_type().equals("boolean[]")){
          emit_string=emit_string+"i8*";
        }
        else{
          emit_string=emit_string+"i8*";
        }
        emit_string=emit_string+"(i8*";
        for(String k2 : f.get_pars().keySet()){     //for every parameter in the function we are print their type
          symVar v=f.get_pars().get(k2);
          if(v.get_type().equals("int")){
            emit_string=emit_string+",i32";
          }
          else if(v.get_type().equals("boolean")){
            emit_string=emit_string+",i1";
          }
          else if(v.get_type().equals("int[]")){
            emit_string=emit_string+",i32*";
          }
          else if(v.get_type().equals("boolean[]")){
            emit_string=emit_string+",i8*";
          }
          else{
            emit_string=emit_string+",i8*";
          }
        }
        emit_string=emit_string+")* @"+own+"."+f.get_name()+" to i8*), ";
      }
      emit_string=removeLastChar(emit_string);
      emit_string=emit_string+"]\n\n";
      emit(emit_string);
    }


    n.f0.accept(this, argu);
    n.f1.accept(this, argu);
    n.f2.accept(this, argu);
    return _ret;
 }

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
     String emit_string;


     n.f0.accept(this, argu);
     String cname=n.f1.accept(this, null);          //this part is a standar output in order to define the 3 useful functions
     emit("@."+cname+"_vtable = global [0 x i8*] []\n\n");
     emit("declare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n");
     emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n\n");
     emit("define void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n\n");
     emit("define void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n\n");
     emit("define void @throw_nsz() {\n\t%_str = bitcast [15 x i8]* @_cNSZ to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n\n");

     emit_string="define i32 @main() {\n";        //then define main
     emit(emit_string);
     n.f2.accept(this, argu);
     n.f3.accept(this, argu);
     n.f4.accept(this, argu);
     n.f5.accept(this, argu);
     n.f6.accept(this, argu);
     n.f7.accept(this, argu);
     n.f8.accept(this, argu);
     n.f9.accept(this, argu);
     n.f10.accept(this, argu);
     n.f11.accept(this, null);
     n.f12.accept(this, argu);
     n.f13.accept(this, argu);
     n.f14.accept(this, "main");

     n.f15.accept(this, cname+" main");
     n.f16.accept(this, argu);
     n.f17.accept(this, argu);

     // emit_string=emit_string+"\n}\n\n";
     emit("\n\tret i32 0\n}\n\n");        //and the standar return value
     zero_all();
     this.Var_Type.clear();
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
     String cname=n.f1.accept(this, null);
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
     String cname=n.f1.accept(this, null);
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
      //System.out.println(argu);
      String varname=n.f1.accept(this, null);
      //System.out.println(varname);
      if(argu!=null){
        String emit_string="\t%"+varname+" = alloca ";        //when we meet a value deine we keep memory for it using alloca
        if(type.equals("int")){				//check if the type exists
          emit_string=emit_string+"i32\n";
        }
        else if(type.equals("boolean")){
          emit_string=emit_string+"i1\n";
        }
        else if(type.equals("int[]")){
          emit_string=emit_string+"i32*\n";
        }
        else if(type.equals("boolean[]")){
          emit_string=emit_string+"i8*\n";
        }
        else{								//if it is a defined class type
          emit_string=emit_string+"i8*\n";
        }
        emit(emit_string+"\n");
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
      String v_type;
      String funname=n.f2.accept(this, null);   //the type of the function to be defined
      if(type.equals("int")){
        v_type="i32";
      }
      else if(type.equals("boolean")){
        v_type="i1";
      }
      else if(type.equals("int[]")){
        v_type="i32*";
      }
      else if(type.equals("boolean[]")){
        v_type="i8*";
      }
      else{
        v_type="i8*";
      }
      String emit_string="define "+v_type+" @"+argu+"."+funname+"(i8* %this";   //define the function using the argu which is the owner name
      symClass c=symbolmap.get(argu);
      symFunc funct=c.get_funcs().get(funname);
      for(String p_name:funct.get_pars().keySet()){       //define the first parameter as this and the other based on their type
        symVar p=funct.get_pars().get(p_name);

        if(p.get_type().equals("int")){
          emit_string=emit_string+", i32 ";
        }
        else if(p.get_type().equals("boolean")){
          emit_string=emit_string+", i1 ";
        }
        else if(p.get_type().equals("int[]")){
          emit_string=emit_string+", i32* ";
        }
        else if(p.get_type().equals("boolean[]")){
          emit_string=emit_string+", i8* ";
        }
        else{
          emit_string=emit_string+", i8* ";
        }
        emit_string=emit_string+"%."+p.get_name();
      }
      emit_string=emit_string+") {\n";
      emit(emit_string);
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
      String exp=n.f10.accept(this, argu+" "+funname);
      //n.f11.accept(this, argu);
      //n.f12.accept(this, argu);
      emit("ret "+v_type+" "+exp+"\n}\n\n");    //return the expersion
			zero_all();            //at the end of each method we can zero all the registers this is a beatify detail
      this.Var_Type.clear();
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */

   public String visit(FormalParameter n, String argu) throws Exception {
      String _ret=null;
      String type=n.f0.accept(this, argu);
      String v_type;
      if(type.equals("int")){
        v_type="i32";
      }
      else if(type.equals("boolean")){
        v_type="i1";
      }
      else if(type.equals("int[]")){
        v_type="i32*";
      }
      else if(type.equals("boolean[]")){
        v_type="i8*";
      }
      else{
        v_type="i8*";
      }
      String varname=n.f1.accept(this, null);
      String emit_string="%"+varname+" = alloca "+v_type+"\n";        //alloca the parameters and store them
      emit_string=emit_string+"store "+v_type+" %."+varname+", "+v_type+"*"+" %"+varname+"\n\n";

      // n.f0.accept(this, argu);
      // n.f1.accept(this, argu);
      emit(emit_string);
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
      String name=n.f0.accept(this, null);
      n.f1.accept(this, argu);
      String var=n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      symClass c=symbolmap.get(c_f[0]);
      symFunc f=c.get_funcs().get(c_f[1]);
      boolean a;
      String type1=null,type2=null;
      if(f.get_vars().containsKey(name) || f.get_pars().containsKey(name)){   //if it is a parameter or a variable of a class
        symVar v;
        if(f.get_vars().containsKey(name)){ //checks i its a variable of if its a parameter
          v=f.get_vars().get(name);
        }
        else{
          v=f.get_pars().get(name);
        }
        String vtype=v.get_type();
        if(vtype.equals("int")){
          type1="i32*";
          type2="i32";
        }
        else if(vtype.equals("boolean")){
          type1="i1*";
          type2="i1";
        }
        else if(vtype.equals("int[]")){
          type1="i32**";
          type2="i32*";
        }
        else if(vtype.equals("boolean[]")){
          type1="i8**";
          type2="i8*";
        }
        else{
          type1="i8**";
          type2="i8*";
        }
        name="%"+name;
      }
      else{           //if its a field
        if(!c.get_vars().containsKey(name)){
          String parent=c.get_exname();
          while(parent!=null){
            symClass p2=symbolmap.get(parent);
            if(p2.get_vars().containsKey(name)){
              c=p2;
              break;
            }
            parent=p2.get_exname();
          }
        }

        symVar v=c.get_vars().get(name);
        String temp1=get_var_num();
        String temp2=get_var_num();
        String emit_string=temp1+" = getelementptr i8, i8* %this, i32 "+(v.get_offset()+8)+"\n";      //get the pointer of the field based on the owner
        String vtype=v.get_type();
        emit_string=emit_string+temp2+" = bitcast i8* "+temp1+" to ";       //make the bitcast base the type

        if(vtype.equals("int")){
          emit_string=emit_string+"i32*";
          type1="i32*";
          type2="i32";
        }
        else if(vtype.equals("boolean")){
          emit_string=emit_string+"i1*";
          type1="i1*";
          type2="i1";
        }
        else if(vtype.equals("int[]")){
          emit_string=emit_string+"i32**";
          type1="i32**";
          type2="i32*";
        }
        else if(vtype.equals("boolean[]")){
          emit_string=emit_string+"i8**";
          type1="i8**";
          type2="i8*";
        }
        else {
          emit_string=emit_string+"i8**";
          type1="i8**";
          type2="i8*";
        }
        emit(emit_string+"\n");
        name=temp2;
      }
      String emit_string="\t"+"store "+type2+" "+var+", "+type1+" "+name+"\n\n";  //performe the assignment
      emit(emit_string);

      //String emit_string="\t"+"store i8* "+var+", i8** %"+name+"\n\n";

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
   public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
      String _ret=null;
      String ident=n.f0.accept(this, argu);
      String varTyp =this.Var_Type.get(ident);
      // System.out.println(varTyp);
      if(varTyp.equals("boolean[]")){   //cases if its a boolean array
        String temp0=get_var_num();
        String temp1=get_var_num();

        String emit_string="\t"+temp0+" = bitcast i8* "+ident+" to i32*\n";       //cast because we want to take the first element of the array which is an integer
        emit_string=emit_string+"\t"+temp1+" = load i32, i32* "+temp0+"\n";        //load this integer, this int is the size of the array
        //System.out.println(ident);
        n.f1.accept(this, argu);
        String exp1=n.f2.accept(this, argu);
        String temp2=get_var_num();
        String temp3=get_var_num();
        String temp4=get_var_num();
        String temp5=get_var_num();
        String temp6=get_var_num();

        String temp1_oob_ok=get_oob_ok_num();
        String temp2_oob_error=get_oob_err_num();
        emit_string=emit_string+"\t"+temp2+" = icmp sge i32 "+exp1+", 0\n";       //if the index is greater than zero
        emit_string=emit_string+"\t"+temp3+" = icmp slt i32 "+exp1+", "+temp1+"\n";     //the index is less than the size of the array (we loaded above)
        emit_string=emit_string+"\t"+temp4+" = and i1 "+temp2+", "+temp3+"\n";        //the combo of the 2 condtions above
        emit_string=emit_string+"\tbr i1 "+temp4+", label "+temp1_oob_ok+", label "+temp2_oob_error+"\n\n";     //throw an error if a condition is false
        emit_string=emit_string+"\t"+temp2_oob_error.substring(1)+":\n";
        emit_string=emit_string+"\tcall void @throw_oob()\n";
        emit_string=emit_string+"\tbr label "+temp1_oob_ok+"\n\n";
        emit_string=emit_string+"\t"+temp1_oob_ok.substring(1)+":\n";
        emit_string=emit_string+"\t"+temp5+" = add i32 4, "+exp1+"\n";        //add 4 to the index since one integet is like 4 bytes
        emit_string=emit_string+"\t"+temp6+" = getelementptr i8, i8* "+ident+", i32 "+temp5+"\n";   //get a pointer to the position we found
        emit(emit_string);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        String exp2=n.f5.accept(this, argu);
        String temp7=get_var_num();
        emit_string="\t"+temp7+" = zext i1 "+exp2+" to i8\n";   //in order to store the variable we have to convert it to i8 from i1
        emit_string=emit_string+"\tstore i8 "+temp7+", i8* "+temp6+"\n";      //and then we can store it
        emit(emit_string);
        n.f6.accept(this, argu);
        return _ret;
      }else{          //or else if its a int array
        String temp1=get_var_num();
        String temp2=get_var_num();
        String temp3=get_var_num();
        String temp4=get_var_num();
        String temp5=get_var_num();
        String temp6=get_var_num();
        String temp1_oob_ok=get_oob_ok_num();
        String temp2_oob_error=get_oob_err_num();
        String emit_string="\t"+temp1+" = load i32, i32* "+ident+"\n";
        //System.out.println(ident);
        n.f1.accept(this, argu);
        String exp1=n.f2.accept(this, argu);
        emit_string=emit_string+"\t"+temp2+" = icmp sge i32 "+exp1+", 0\n";//if the index is greater than zero
        emit_string=emit_string+"\t"+temp3+" = icmp slt i32 "+exp1+", "+temp1+"\n";//the index is less than the size of the array (we loaded above)
        emit_string=emit_string+"\t"+temp4+" = and i1 "+temp2+", "+temp3+"\n";//the combo of the 2 condtions above
        emit_string=emit_string+"\tbr i1 "+temp4+", label "+temp1_oob_ok+", label "+temp2_oob_error+"\n\n";//throw an error if a condition is false
        emit_string=emit_string+"\t"+temp2_oob_error.substring(1)+":\n";
        emit_string=emit_string+"\tcall void @throw_oob()\n";
        emit_string=emit_string+"\tbr label "+temp1_oob_ok+"\n\n";
        emit_string=emit_string+"\t"+temp1_oob_ok.substring(1)+":\n";
        emit_string=emit_string+"\t"+temp5+" = add i32 1, "+exp1+"\n";  //add one to the indec because we have one more int in the array (the size)
        emit_string=emit_string+"\t"+temp6+" = getelementptr i32, i32* "+ident+", i32 "+temp5+"\n"; //get a pointer to the position we found
        emit(emit_string);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        String exp2=n.f5.accept(this, argu);
        emit_string="\tstore i32 "+exp2+", i32* "+temp6+"\n";     //store what we wanted
        emit(emit_string);
        n.f6.accept(this, argu);
        return _ret;
      }

   }

   //help for while loop
   //https://stackoverflow.com/questions/27540761/how-to-change-a-do-while-form-loop-into-a-while-form-loop-in-llvm-ir
   /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {
       String _ret=null;
       n.f0.accept(this, argu);
       n.f1.accept(this, argu);
       String temp1_loop=get_loop_num();
       String temp2_loop_then=get_loop_then_num();
       String temp3_loop_end=get_loop_end_num();
       String emit_string="\tbr label "+temp1_loop+"\n\n";
       emit_string=emit_string+"\t"+temp1_loop.substring(1)+":\n";
       emit(emit_string);
       String exp1=n.f2.accept(this, argu);
       emit_string="\tbr i1 "+exp1+", label "+temp2_loop_then+", label "+temp3_loop_end+"\n\n";
       emit_string=emit_string+"\t"+temp2_loop_then.substring(1)+":\n";
       emit(emit_string);
       n.f3.accept(this, argu);
       n.f4.accept(this, argu);
       emit_string="\tbr label "+temp1_loop+"\n\n"; //go at the start o the loop again to see if the condition is true or false
       emit_string=emit_string+"\t"+temp3_loop_end.substring(1)+":\n";    //the end o the loop this label is usd in order to stop the loop and continue
       emit(emit_string);
       return _ret;
    }

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
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp1=n.f2.accept(this, argu);
      String temp1_if_then=get_if_then_num();
      String temp2_if_else=get_if_else_num();
      String temp3_if_end=get_if_end_num();

      String emit_string="\tbr i1 "+exp1+", label "+temp1_if_then+", label "+temp2_if_else+"\n\n";  //see if the condition is true or false and go to the corresponding label

      n.f3.accept(this, argu);
      emit_string=emit_string+"\t"+temp1_if_then.substring(1)+":\n";
      emit(emit_string);
      n.f4.accept(this, argu);
      emit_string="\tbr label "+temp3_if_end+"\n\n\t"+temp2_if_else.substring(1)+":\n";
      emit(emit_string);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      emit_string="\tbr label "+temp3_if_end+"\n\n";
      emit_string=emit_string+"\t"+temp3_if_end.substring(1)+":";   //this label is to leave the if and is called at the end of then and else label
      emit(emit_string);
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
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp=n.f2.accept(this, argu);
      String emit_string="\tcall void (i32) @print_int(i32 ";   //call the prin function
      emit_string=emit_string+exp+")\n\n";
      emit(emit_string);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n, String argu) throws Exception {
      String _ret=null;
      String temp1=get_var_num();
      String pr1=n.f0.accept(this, argu);
      String varTyp =this.Var_Type.get(pr1);

      String emit_string;
      //System.out.println(varTyp);
      if(varTyp.equals("boolean[]")){
          String temp2=get_var_num();
          emit_string="\t"+temp1+" = bitcast i8* "+pr1+" to i32*\n";    //bitcast in order to load thw  size
          emit_string=emit_string+"\t"+temp2+" = load i32, i32* "+temp1+"\n";   //load the size
          emit(emit_string);
          n.f1.accept(this, argu);
          n.f2.accept(this, argu);
          return temp2;
      }
      else {
          emit_string="\t"+temp1+" = load i32, i32* "+pr1+"\n";   //just load the first element which is the size of the array
          emit(emit_string);
          n.f1.accept(this, argu);
          n.f2.accept(this, argu);
          return temp1;
      }

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
      String pr1=n.f0.accept(this, argu);
      n.f1.accept(this, argu);

      String ident=n.f2.accept(this, null);

      n.f3.accept(this, argu);


      String varTyp;
      if (pr1.equals("%this")) {      //if we use this the class we are now is the owner of the method
          varTyp = c_f[0];
      }
      else{
          varTyp =this.Var_Type.get(pr1);
      }

      String temp1=get_var_num();
      String temp2=get_var_num();
      String temp3=get_var_num();
      String temp4=get_var_num();
      String temp5=get_var_num();
      String temp6=get_var_num();

      String emit_string="\t"+temp1+" = bitcast i8* "+pr1+" to i8***\n";          //we need to have it's address(first byte of the object) in a register of type i8***
      emit_string=emit_string+"\t"+temp2+" = load i8**, i8*** "+temp1+"\n";       //Load vtable_ptr

      symClass c=symbolmap.get(varTyp);
      symFunc funct=null;
      if(c.get_funcs().containsKey(ident)){
        funct=c.get_funcs().get(ident);
      }
      else{
        String parent=c.get_exname();
      	while(parent!=null){
      		symClass p2=symbolmap.get(parent);
          if(p2.get_funcs().containsKey(ident)){
            funct=p2.get_funcs().get(ident);
            break;
          }
      		parent=p2.get_exname();
      	}

      }

      emit_string=emit_string+"\t"+temp3+" = getelementptr i8*, i8** "+temp2+", i32 "+(funct.get_offset()/8)+"\n"; //get a pointer to the function_offset/8-th entry in the vtable
      emit_string=emit_string+"\t"+temp4+" = load i8*, i8** "+temp3+"\n";   //Get the actual function pointer
      emit_string=emit_string+"\t"+temp5+" = bitcast i8* "+temp4+" to ";    //bitcast the pointer base its type
      String fun_type;
      if(funct.get_type().equals("int")){
        emit_string=emit_string+"i32 ";
        fun_type="i32";
      }
      else if(funct.get_type().equals("boolean")){
        emit_string=emit_string+"i1 ";
        fun_type="i1";
      }
      else if(funct.get_type().equals("int[]")){
        emit_string=emit_string+"i32* ";
        fun_type="i32*";
      }
      else if(funct.get_type().equals("boolean[]")){
        emit_string=emit_string+"i8* ";
        fun_type="i8*";
      }
      else{
        emit_string=emit_string+"i8* ";
        fun_type="i8*";
      }

      emit_string=emit_string+"(i8*";

      for(String p_name:funct.get_pars().keySet()){       //the signature of the pointer contains and its parameters type
        symVar p=funct.get_pars().get(p_name);

        if(p.get_type().equals("int")){
          emit_string=emit_string+", i32";
        }
        else if(p.get_type().equals("boolean")){
          emit_string=emit_string+", i1";
        }
        else if(p.get_type().equals("int[]")){
          emit_string=emit_string+", i32*";
        }
        else if(p.get_type().equals("boolean[]")){
          emit_string=emit_string+", i8*";
        }
        else{
          emit_string=emit_string+", i8*";
        }
      }

      emit_string=emit_string+")*\n";
      // all_vars=null;
      all_expression_counters.add(0);
      all_expression_strings.add("");
      int position=all_expression_counters.size() - 1;
      total_expressions++;
      n.f4.accept(this, argu+" "+varTyp+" "+ident+" "+(total_expressions-1));   //before we call the function we go to its variables
      n.f5.accept(this, argu);
      // System.out.println("TELOS ");
      if(!all_expression_strings.get(position).equals("")){ //if the final string is not empty use it and print what you want
        emit_string=emit_string+"\t"+temp6+" = call "+fun_type+" "+temp5+"(i8* "+pr1+all_expression_strings.get(position)+")\n\n";
      }
      else{         //if it was empty we had not any parameters then go without it
        emit_string=emit_string+"\t"+temp6+" = call "+fun_type+" "+temp5+"(i8* "+pr1+")\n\n";
      }


      emit(emit_string);
      this.Var_Type.put(temp6,funct.get_type());
      return temp6;
   }


   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, String argu) throws Exception {  //for each expression list has its own string in the all_expression_strings linked has map
      String _ret=null;
      String[] c_f=argu.split(" ");
      String emit_string;
      int expr_num=Integer.parseInt(c_f[4]);


      String exp=n.f0.accept(this, c_f[0]+" "+c_f[1]);
      symClass c=symbolmap.get(c_f[2]);
      // System.out.println("EINAI PRIN "+c_f[3]);
      symFunc funct=null;
      if(c.get_funcs().containsKey(c_f[3])){
        funct=c.get_funcs().get(c_f[3]);
      }
      else{
        // System.out.println("EINAI ELSE ");
        String parent=c.get_exname();
      	while(parent!=null){
      		symClass p2=symbolmap.get(parent);
          if(p2.get_funcs().containsKey(c_f[3])){
            funct=p2.get_funcs().get(c_f[3]);
            break;
          }
      		parent=p2.get_exname();
      	}
      }

      List<symVar> l = new ArrayList<symVar>(funct.get_pars().values());    //arraylist with all the parameters
      // System.out.println("ARXH "+all_expression_counters.get(expr_num)+"==== "+expr_num);
      symVar p=l.get(all_expression_counters.get(expr_num));

      if(p.get_type().equals("int")){                                 //start stacking the all_expression_strings string in its position expr_num
        all_expression_strings.set(expr_num,", i32 ");
      }
      else if(p.get_type().equals("boolean")){
        all_expression_strings.set(expr_num,", i1 ");
      }
      else if(p.get_type().equals("int[]")){
        all_expression_strings.set(expr_num,", i32* ");
      }
      else if(p.get_type().equals("boolean[]")){
        all_expression_strings.set(expr_num,", i8* ");
      }
      else{
        all_expression_strings.set(expr_num,", i8* ");
      }
      all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+exp);
      // System.out.println("perase1 "+funct.get_name());
      n.f1.accept(this, argu);
      // System.out.println("perase2 "+funct.get_name());
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionTerm n, String argu) throws Exception {
      String _ret=null;
      String[] c_f=argu.split(" ");
      int expr_num=Integer.parseInt(c_f[4]);
			int new_all_expression_counters=all_expression_counters.get(expr_num)+1;
      all_expression_counters.set(expr_num, new_all_expression_counters);
      n.f0.accept(this, argu);
      String exp=n.f1.accept(this, c_f[0]+" "+c_f[1]);

      String emit_string;
      symClass c=symbolmap.get(c_f[2]);
      symFunc funct=null;
      if(c.get_funcs().containsKey(c_f[3])){
        funct=c.get_funcs().get(c_f[3]);
      }
      else{
        String parent=c.get_exname();
      	while(parent!=null){
      		symClass p2=symbolmap.get(parent);
          if(p2.get_funcs().containsKey(c_f[3])){
            funct=p2.get_funcs().get(c_f[3]);
            break;
          }
      		parent=p2.get_exname();
      	}
      }

      List<symVar> l = new ArrayList<symVar>(funct.get_pars().values());
      //System.out.println("YPOLOIPES "+all_expression_counters.get(expr_num)+"==== "+expr_num);
      symVar p=l.get(all_expression_counters.get(expr_num));
                                                                                    //start stacking the all_expression_strings string in its position expr_num
      if(p.get_type().equals("int")){
        all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+", i32 ");
      }
      else if(p.get_type().equals("boolean")){
        all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+", i1 ");
      }
      else if(p.get_type().equals("int[]")){
        all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+", i32* ");
      }
      else if(p.get_type().equals("boolean[]")){
        all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+", i8* ");
      }
      else{
        all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+", i8* ");
      }
      all_expression_strings.set(expr_num,all_expression_strings.get(expr_num)+exp);
      return _ret;
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
      String emit_string;
      String temp1=get_var_num();
      String temp2=get_var_num();
      String temp3=get_var_num();
      symClass c=symbolmap.get(cname);
      emit_string="\t"+temp1+" = call i8* @calloc(i32 1, i32 "+(c.get_offset1()+8)+")\n\n";// get the last offset and plus 8
      // a pointer that points at the start of the vtable (equivalently at the first entry
      //     of the vtable) must have type i8**.
      //   Thus, to set the vtable pointer at the start of the object, we need to have it's address
      //     (first byte of the object) in a register of type i8***.
      emit_string=emit_string+"\t"+temp2+" = bitcast i8* "+temp1+" to i8***\n\n";
      emit_string=emit_string+"\t"+temp3+" = getelementptr ["+total_methods(cname)+" x i8*], ["+total_methods(cname)+" x i8*]* @."+cname+"_vtable, i32 0, i32 0\n\n"; //Get the address of the first element of the Base_vtable
      emit_string=emit_string+"\t"+"store i8** "+temp3+", i8*** "+temp2+"\n"; //Set the vtable to the correct address.
      emit(emit_string);
      this.Var_Type.put(temp1,cname);
      return temp1;
   }



   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
   public String visit(AndExpression n, String argu) throws Exception {
      String _ret=null;
      String temp1_exp_res=get_exp_res_num();
      String temp2_exp_res=get_exp_res_num();
      String temp3_exp_res=get_exp_res_num();
      String temp4_exp_res=get_exp_res_num();

      String emit_string;
      String exp1=n.f0.accept(this, argu);

      emit_string="\tbr i1 "+exp1+", label "+temp2_exp_res+", label "+temp1_exp_res+"\n\n"; //Check result, short circuit if false
      emit_string=emit_string+"\t"+temp1_exp_res.substring(1)+":\n";
      emit_string=emit_string+"\tbr label "+temp4_exp_res+"\n\n";
      emit_string=emit_string+"\t"+temp2_exp_res.substring(1)+":\n";
      n.f1.accept(this, argu);
      emit(emit_string);
      String exp2=n.f2.accept(this, argu);
      emit_string="\tbr label "+temp3_exp_res+"\n\n";
      emit_string=emit_string+"\t"+temp3_exp_res.substring(1)+":\n";
      emit_string=emit_string+"\tbr label "+temp4_exp_res+"\n\n";
      emit_string=emit_string+"\t"+temp4_exp_res.substring(1)+":\n";
      String temp1=get_var_num();
      emit_string=emit_string+"\t"+temp1+" = phi i1 [ 0, "+temp1_exp_res+" ], [ "+exp2+", "+temp3_exp_res+" ]\n\n"; // the temp1 has the value based on the predecesor block
      emit(emit_string);
      this.Var_Type.put(temp1,"boolean");
      return temp1;
   }


   /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public String visit(CompareExpression n, String argu) throws Exception {
     String _ret=null;
     String pr1=n.f0.accept(this, argu);
     n.f1.accept(this, argu);
     String pr2=n.f2.accept(this, argu);
     String temp=get_var_num();
     emit("\t" + temp + " = icmp slt i32 " + pr1 + ", " + pr2 + "\n");    //compare less than
     this.Var_Type.put(temp,"boolean");
     return temp;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public String visit(PlusExpression n, String argu) throws Exception {
     String _ret=null;
     String pr1=n.f0.accept(this, argu);
     n.f1.accept(this, argu);
     String pr2=n.f2.accept(this, argu);

     String temp=get_var_num();
     emit("\t" + temp + " = add i32 " + pr1 + ", " + pr2 + "\n");   //add
     this.Var_Type.put(temp,"int");
     return temp;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public String visit(MinusExpression n, String argu) throws Exception {
     String _ret=null;
     String pr1=n.f0.accept(this, argu);
     n.f1.accept(this, argu);
     String pr2=n.f2.accept(this, argu);
     String temp=get_var_num();
     emit("\t" + temp + " = sub i32 " + pr1 + ", " + pr2 + "\n"); //sub
     this.Var_Type.put(temp,"int");
    return temp;
  }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n, String argu) throws Exception {
      String _ret=null;
      String pr1=n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String pr2=n.f2.accept(this, argu);
      String temp=get_var_num();
      emit("\t" + temp + " = mul i32 " + pr1 + ", " + pr2 + "\n");  //mul
      this.Var_Type.put(temp,"int");
      return temp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n, String argu) throws Exception {
      String _ret=null;


      String pr1=n.f0.accept(this, argu);
      String varTyp =this.Var_Type.get(pr1);
      // System.out.println(varTyp);
      if(varTyp.equals("boolean[]")){ //if it is an boolean array
        String temp0=get_var_num();
        String temp1=get_var_num();
        String temp2=get_var_num();
        String temp3=get_var_num();
        String temp4=get_var_num();

        String temp6=get_var_num();
        String temp7=get_var_num();
        String temp8=get_var_num();
        String temp9=get_var_num();
        String temp1_oob_ok=get_oob_ok_num();
        String temp2_oob_error=get_oob_err_num();
        // String emit_string;
        String emit_string="\t"+temp0+" = bitcast i8* "+pr1+" to i32*\n";     //bitcast in order to get the size
        emit_string=emit_string+"\t"+temp1+" = load i32, i32* "+temp0+"\n";     //load the size of the array
        emit(emit_string);
        n.f1.accept(this, argu);
        String pr2=n.f2.accept(this, argu);

        emit_string="\t"+temp2+" = icmp sge i32 "+pr2+", 0\n";              //Check that the index is greater than zero
        emit_string=emit_string+"\t"+temp3+" = icmp slt i32 "+pr2+", "+temp1+"\n";    //Chech that the index is less than the size of the array
        emit_string=emit_string+"\t"+temp4+" = and i1 "+temp2+", "+temp3+"\n";    //Both of these conditions must hold
        emit_string=emit_string+"\tbr i1 "+temp4+", label "+temp1_oob_ok+", label "+temp2_oob_error+"\n\n";   //else throw exception
        emit_string=emit_string+"\t"+temp2_oob_error.substring(1)+":\n";
        emit_string=emit_string+"\tcall void @throw_oob()\n";
        emit_string=emit_string+"\tbr label "+temp1_oob_ok+"\n\n";
        emit_string=emit_string+"\t"+temp1_oob_ok.substring(1)+":\n";   //all ok with the index
        emit_string=emit_string+"\t"+temp6+" = add i32 4, "+pr2+"\n";   //add 4 to the index(int 4 bytes ), the first element is the size
        emit_string=emit_string+"\t"+temp7+" = getelementptr i8, i8* "+pr1+", i32 "+temp6+"\n";     //get pointer to the index we want +1 (for the size)
        emit_string=emit_string+"\t"+temp8+" = bitcast i8* "+temp7+" to i1*\n";       //bitcast to get the pointer from i8* to i1*
        emit_string=emit_string+"\t"+temp9+" = load i1, i1* "+temp8+"\n";           //load the i1
        emit(emit_string);
        n.f3.accept(this, argu);
        this.Var_Type.put(temp9,"boolean[]");
        return temp9;
      }
      else{           //if it is an int array
        String temp1=get_var_num();
        String temp2=get_var_num();
        String temp3=get_var_num();
        String temp4=get_var_num();

        String temp6=get_var_num();
        String temp7=get_var_num();
        String temp8=get_var_num();
        String temp1_oob_ok=get_oob_ok_num();
        String temp2_oob_error=get_oob_err_num();
        // String emit_string;
        String emit_string="\t"+temp1+" = load i32, i32* "+pr1+"\n";        //load the size of the array
        emit(emit_string);
        n.f1.accept(this, argu);
        String pr2=n.f2.accept(this, argu);
        emit_string="\t"+temp2+" = icmp sge i32 "+pr2+", 0\n";          //Check that the index is greater than zero
        emit_string=emit_string+"\t"+temp3+" = icmp slt i32 "+pr2+", "+temp1+"\n";      //Chech that the index is less than the size of the array
        emit_string=emit_string+"\t"+temp4+" = and i1 "+temp2+", "+temp3+"\n";     //Both of these conditions must hold
        emit_string=emit_string+"\tbr i1 "+temp4+", label "+temp1_oob_ok+", label "+temp2_oob_error+"\n\n";  //else throw exception
        emit_string=emit_string+"\t"+temp2_oob_error.substring(1)+":\n";
        emit_string=emit_string+"\tcall void @throw_oob()\n";
        emit_string=emit_string+"\tbr label "+temp1_oob_ok+"\n\n";
        emit_string=emit_string+"\t"+temp1_oob_ok.substring(1)+":\n";   //all ok with the index
        emit_string=emit_string+"\t"+temp6+" = add i32 1, "+pr2+"\n";   //add 1 to the index, the first element is the size
        emit_string=emit_string+"\t"+temp7+" = getelementptr i32, i32* "+pr1+", i32 "+temp6+"\n";       //get pointer to the index we want +1 (for the size)
        emit_string=emit_string+"\t"+temp8+" = load i32, i32* "+temp7+"\n";     //load the i32 that we wanted
        emit(emit_string);
        n.f3.accept(this, argu);
        this.Var_Type.put(temp8,"int[]");
        return temp8;
      }

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
         return n.f0.accept(this, argu);
      }

   /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception{			//the return depends on the argu if it's not null then it means that is called from a
      String ident=n.f0.toString();
      if(argu==null){
        return ident;
      }
    	else{
        String[] c_f=argu.split(" ");
        symClass c=symbolmap.get(c_f[0]);
        symFunc f=c.get_funcs().get(c_f[1]);
        if(f.get_vars().containsKey(ident)){        //the variabl is a variable in the function
          symVar v=f.get_vars().get(ident);
          String vtype=v.get_type();
          String temp=get_var_num();
          String emit_string="\n\t"+temp+" = load ";        //we have to load the variable first if we use it
          if(vtype.equals("int")){
            emit_string=emit_string+"i32, i32* ";
          }
          else if(vtype.equals("boolean")){
            emit_string=emit_string+"i1, i1* ";
          }
          else if(vtype.equals("int[]")){
            emit_string=emit_string+"i32*, i32** ";
          }
          else if(vtype.equals("boolean[]")){
            emit_string=emit_string+"i8*, i8** ";
          }
          else {
            emit_string=emit_string+"i8*, i8** ";
          }
          emit_string=emit_string+"%"+ident;
          emit(emit_string+"\n");
          this.Var_Type.put(temp,vtype);
          //System.out.println( this.Var_Type );
          return temp;
        }
        else if(f.get_pars().containsKey(ident)){     //the variabl is a parameter in the function
          symVar v=f.get_pars().get(ident);
          String vtype=v.get_type();
          String temp=get_var_num();
          String emit_string="\n\t"+temp+" = load ";      //we have to load the variable first if we use it
          if(vtype.equals("int")){
            emit_string=emit_string+"i32, i32* ";
          }
          else if(vtype.equals("boolean")){
            emit_string=emit_string+"i1, i1* ";
          }
          else if(vtype.equals("int[]")){
            emit_string=emit_string+"i32*, i32** ";
          }
          else if(vtype.equals("boolean[]")){
            emit_string=emit_string+"i8*, i8** ";
          }
          else {
            emit_string=emit_string+"i8*, i8** ";
          }
          emit_string=emit_string+"%"+ident;
          emit(emit_string+"\n");
          this.Var_Type.put(temp,vtype);
          return temp;
        }
        else{                             //the variabl is a field in the class
          if(!c.get_vars().containsKey(ident)){     //if the field does not exists in the class we are is in a parent class
            String parent=c.get_exname();
            while(parent!=null){
          		symClass p2=symbolmap.get(parent);
              if(p2.get_vars().containsKey(ident)){
                c=p2;             //change the current class to its father
                break;
              }
          		parent=p2.get_exname();
          	}
          }

          symVar v=c.get_vars().get(ident);
          String temp1=get_var_num();
          String temp2=get_var_num();
          String temp3=get_var_num();
          String emit_string=temp1+" = getelementptr i8, i8* %this, i32 "+(v.get_offset()+8)+"\n";      //get the pointer to the field we have
          String vtype=v.get_type();
          String type1=null,type2=null;
          emit_string=emit_string+temp2+" = bitcast i8* "+temp1+" to ";     //bitcast it

          if(vtype.equals("int")){
            emit_string=emit_string+"i32*";
            type1="i32";
            type2="i32*";
          }
          else if(vtype.equals("boolean")){
            emit_string=emit_string+"i1*";
            type1="i1";
            type2="i1*";
          }
          else if(vtype.equals("int[]")){
            emit_string=emit_string+"i32**";
            type1="i32*";
            type2="i32**";
          }
          else if(vtype.equals("boolean[]")){
            emit_string=emit_string+"i8**";
            type1="i8*";
            type2="i8**";
          }
          else {
            emit_string=emit_string+"i8**";
            type1="i8*";
            type2="i8**";
          }
          emit_string=emit_string+"\n"+temp3+" = load "+type1+", "+type2+" "+temp2+"\n";      //load
          emit(emit_string);
          this.Var_Type.put(temp3,vtype);
          //System.out.println( this.Var_Type );
          return temp3;

        }

      }

      // return ident;
    }

    /**
      * f0 -> ArrayType()
      *       | BooleanType()
      *       | IntegerType()
      *       | Identifier()
      */
     public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, null);
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
    return "%this";
 }


 /**
  * f0 -> "true"
  */
 public String visit(TrueLiteral n, String argu) throws Exception {
    return "1";
 }

 /**
  * f0 -> "false"
  */
 public String visit(FalseLiteral n, String argu) throws Exception {
    return "0";
 }

 /**
  * f0 -> <INTEGER_LITERAL>
  */
 public String visit(IntegerLiteral n, String argu) throws Exception {
    return n.f0.toString();
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

     String temp1=get_var_num();
     String temp2=get_var_num();
     String temp3=get_var_num();
     String temp4=get_var_num();
     String temp5=get_var_num();
     String temp1_nsz_ok=get_nsz_ok_num();
     String temp1_nsz_error=get_nsz_err_num();
     String ex1=n.f3.accept(this, argu);
     String emit_string="\t"+temp1+" = add i32 4, "+ex1+"\n";
     emit_string=emit_string+"\t"+temp2+" = icmp sge i32 "+temp1+", 4\n";       //because it is a i8 array we need 4 i8 to present a i32 (the size) so the array needs 4 memory slots at the start in order to keep the size
     emit_string=emit_string+"\tbr i1 "+temp2+", label "+temp1_nsz_ok+", label "+temp1_nsz_error+"\n\n";
     emit_string=emit_string+"\t"+temp1_nsz_error.substring(1)+":\n";
     emit_string=emit_string+"\t call void @throw_nsz()\n";   //negative size
     emit_string=emit_string+"\tbr label "+temp1_nsz_ok+"\n";
     emit_string=emit_string+"\t"+temp1_nsz_ok.substring(1)+":\n";
     emit_string=emit_string+"\t"+temp3+" = call i8* @calloc(i32 "+temp1+", i32 1)\n";    //calloc in order to save the size of the array 4 slots of 1byte =1 i32
     emit_string=emit_string+"\t"+temp4+" = bitcast i8* "+temp3+" to i32*\n";           //bitcast so we can save an i32
     // emit_string=emit_string+"\t"+temp4+" = getelementptr i32, i8* "+temp3+", i32 0\n";
     // emit_string=emit_string+"\t"+temp4+" = zext i32 "+ex1+" to i8\n";
     emit_string=emit_string+"\tstore i32 "+ex1+", i32* "+temp4+"\n";
     emit(emit_string);
     this.Var_Type.put(temp3,"boolean[]");
 		 //n.f4.accept(this, argu);
 		 return temp3;
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
       String temp1=get_var_num();
       String temp2=get_var_num();
       String temp3=get_var_num();
       String temp4=get_var_num();
       String temp1_nsz_ok=get_nsz_ok_num();
       String temp1_nsz_error=get_nsz_err_num();
       String ex1=n.f3.accept(this, argu);
       String emit_string="\t"+temp1+" = add i32 1, "+ex1+"\n";       //add one to the size because we want ot save an i32 to keep the size value
       emit_string=emit_string+"\t"+temp2+" = icmp sge i32 "+temp1+", 1\n";       //compare to see if it is negative
       emit_string=emit_string+"\tbr i1 "+temp2+", label "+temp1_nsz_ok+", label "+temp1_nsz_error+"\n\n";
       emit_string=emit_string+"\t"+temp1_nsz_error.substring(1)+":\n";
       emit_string=emit_string+"\t call void @throw_nsz()\n";
       emit_string=emit_string+"\tbr label "+temp1_nsz_ok+"\n";
       emit_string=emit_string+"\t"+temp1_nsz_ok.substring(1)+":\n";
       emit_string=emit_string+"\t"+temp3+" = call i8* @calloc(i32 "+temp1+", i32 4)\n";      //calloc for 4 beacause each int needs 4 bytes
       emit_string=emit_string+"\t"+temp4+" = bitcast i8* "+temp3+" to i32*\n";           //bitcast to save the size
       emit_string=emit_string+"\tstore i32 "+ex1+", i32* "+temp4+"\n";           //store the size
       emit(emit_string);
       this.Var_Type.put(temp4,"int[]");
       //n.f4.accept(this, argu);
       return temp4;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
       String _ret=null;
       //n.f0.accept(this, argu);
       String ex1=n.f1.accept(this, argu);
       String temp1=get_var_num();
       emit("\t" + temp1 + " = xor i1 1, " + ex1 + "\n");       // the not clause is done using xor with true 1xor0=>1  1xor1=>0
       this.Var_Type.put(temp1,"boolean");
       return temp1;
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


  public void set_file_name(String name){    //set the name of the file that has been created

     this.file_name=name;
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
