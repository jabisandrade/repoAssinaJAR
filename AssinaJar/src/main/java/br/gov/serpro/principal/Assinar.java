package br.gov.serpro.principal;


public class Assinar {


	/**
	 * @author Jabis Andrade 
	 * @category Utilitario para assinatura de arquivos .jar (ex.: applets)
	 */
	
	public static void main(String[] args) {
		
		if (isLinux()){
			System.out.print("Sistema Operacional identificado: LINUX \n");
			UtilUnix.iniciar();
		}else{
			System.out.print("Sistema Operacional identificado: WINDOWS \n");
			UtilWin.iniciar();
		}
	}
	
	public static boolean isLinux(){		
		String sistema = System.getProperty("os.name");		
		if (sistema.equals("Linux")){
			return true;			
		}else{
			return false;
		}
	}	
}
