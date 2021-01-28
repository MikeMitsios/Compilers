import java_cup.runtime.*;
import java.io.*;
/////############################## THE WHOLE CODE IS BASED ON THE TUTORIAL
class Main {
    public static void main(String[] argv) throws Exception{
        System.out.println("Please type your Code:");
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();
        System.out.println("Main2.java output:");
     ///////////   this part is to print the result of the given code
        try{
	Process process = Runtime.getRuntime().exec("javac Main2.java");
	 process.waitFor() ; 
	 process = Runtime.getRuntime().exec("java Main2");
	 BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
	 String s = null;
	while ((s = stdInput.readLine()) != null) {
	    System.out.println(s);
	}
	 
		}catch (Exception ex) {
         ex.printStackTrace();
      }

      /////////	
    }
}
