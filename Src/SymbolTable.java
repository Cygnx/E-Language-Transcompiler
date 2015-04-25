
import java.util.*;

public class SymbolTable {		// symbolTable class
	private ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();	// lists of scopes
	
	public void newScope(){
		ArrayList<String> tempTable = new ArrayList<String>();	//creates a new scope
		table.add(0,tempTable);									//add scope to list
	}
	
	public void addToCurrentScope(String var){
		if( !isVarInCurScope(var) ){	//	Checks to see if the var is in the current scope
			println("int x_" + Integer.toString(table.size() - 1) + "_" +var + ";", 0);
			table.get(0).add(var);		// add it if it's not
		}
		else
			System.err.println("redeclaration of variable " + var ); // variable already exists in this scope
	}	

	public void destroyCurrentScope(){	// deletes the current scope
		table.remove(0);		
	}
	
	public boolean isValidScope(int scope){	// Checks to see if the scope is out of bound
		return scope < table.size();		
	}
	
	public boolean isVarInCurScope(String var)
	{
		for( String current_var : table.get(0)) //	iterate through the list
			if(current_var.equals(var))				//	iterate through the current scope
				return true;						//	if we found the var in the current scope, return true
		return false;	
	}
	
	public boolean isValidVar(String var, int scope){	// Checks to see if var is valid. 
														// If it is, generate C equivalent with respect to scope
		// scope : -2 = search through all valid scopes
		// scope : -1 = Search at the global scope
		// scope : < 0 = search within the specified scope
		if( !isValidScope(scope))			// return false for out of bound scopes
			return false;
		
		int depth = 0;							//Keeps track of which scope we're looking at
		if(scope == -2){ 						// search all scopes 
			for(ArrayList<String>list : table){
				for(String current_var : list){
					if(current_var.equals(var))		// if found 
					{
						println("x_" + Integer.toString( (table.size() - 1) - depth)+ "_" +var, 2); // Generate C code 
						return true;				// return true
					}
				}
				depth++;							// increase scope counter
			}
			return false;							// else false
		}
		
		if (scope == -1)
			scope = table.size() - 1; 				 // set the scope to be global by choosing the last element of the table list
	
		for( String current_var : table.get(scope)){ //	iterate through the list
			if(current_var.equals(var))				//	iterate through the current scope
			{
				if(scope == table.size() - 1) 		// Global scope
					println("x_0_" +var, 2);
				else								// non-global scope
					println("x_" + Integer.toString( (table.size() - 1 - scope))+ "_" +var, 2); // Generate C code
				return true;						//	if we found the var in the current scope, return true
			}
		}
		return false;								// otherwise return false
	}
	
	public void println(String x, int type){	// wrapper for system.out.println, helps with the spacing
		// static parameter for the print function below
		// 0 : complete statement
		// 1 : start of a statement
		// 2 : Middle of a statement
		// 3 : End of a statement
		String tabs = "";
		for(int i = 0; i < table.size(); i++)
			tabs += "	";
		if(type == 0)
			System.out.println(tabs + x);
		else if (type == 1)
			System.out.print(tabs + x); // use print to prevent a \n
		else if (type == 2)
			System.out.print(x); // use print to prevent a \n
		else if (type == 3)
			System.out.println(x); // use print to prevent a \n
	}
}
