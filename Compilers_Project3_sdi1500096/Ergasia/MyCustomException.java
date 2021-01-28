public class MyCustomException extends Exception{		//basically its a class to use in order to throw an error and to get the message of the error we typed 
	private String mess;

	public MyCustomException(String mess){
		this.mess=mess;
	}

	public String toString(){
		return mess;
	}
}