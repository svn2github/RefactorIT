package EqualsOnDiffTypes;

public class MyObject{

	/**
	 *
	 */
	public void aaa (){
	
		String str1 = "mazafaka";
		
		
		function(str1);
	
	}

	/**
	 * @audit EqualsOnDiffTypesSameBranch
	 */
	public void function (Object aaa){
		
		String str2 = "mazafaka2";
		
		if (str2.equals(aaa)){
		
		}
	}

}
