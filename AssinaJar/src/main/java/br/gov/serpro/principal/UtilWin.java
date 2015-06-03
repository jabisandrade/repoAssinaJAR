package br.gov.serpro.principal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class UtilWin {
	
	//private static Properties config = new Properties();
	//private static String arquivo = "config.ini";
	private static String CFG;
	private static String CFG1;
	private static String CFG2;
	private static String JARSIGNER;
	private static String PATHDRIVE;
	private static String PATH;
	private static String PATHASS;
	private static String APPPATH;
	private static String SCRIPT;
	private static char PASSWORD[];
	private static String ALIAS;

	public static void iniciar() {		
		
		carregarArquivoConfiguracao();	
		
		File arquivoTmp = new File(SCRIPT);
		if (arquivoTmp.exists()){
			arquivoTmp.delete();
		}
		
		try {		
			
			String senhaDigitada=null;
			JPasswordField jpassword = new JPasswordField();
			if (JOptionPane.showConfirmDialog (null, jpassword, "Entre com a senha do token.", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				senhaDigitada = new String(jpassword.getPassword());
				PASSWORD = senhaDigitada.toCharArray();
			} else{
				System.exit(0);
			}
			
			JOptionPane.showMessageDialog(null,"Certifique-se que o programa tenha finalizado antes de remover o token.","Atenção!",JOptionPane.WARNING_MESSAGE);

			//Tenta com o token verde
			CFG = CFG1;
			
			if (!obterAlias()){
				//Tenta com o token azul
				CFG = CFG2;	
			}

			if (!obterAlias()){
				throw new Exception("O token não pode ser lido. Verifique se o drive está corretamente instalado ou se a senha digitada é válida!"); 	
			}		
	     	          
	        FileOutputStream fos = new FileOutputStream(arquivoTmp);   
	        File diretorio = new File(PATH);
			File fList[] = diretorio.listFiles();
			
			for ( int i = 0; i < fList.length; i++ ){
				
				String arquivo = PATH+fList[i].getName();
				String arquivoAssinado = PATHASS+fList[i].getName();
				String comando = "\n"+montarComando(arquivo, arquivoAssinado);
				fos.write(comando.getBytes());
			}
			fos.close();  
			  
			executarBatSO(arquivoTmp);
			JOptionPane.showMessageDialog(null,"Processo de assinatura de arquivos concluido. Verifique no diretorio de saida os arquivos assinados.","Processamento concluido.",JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Erro: "+e.getMessage(),"Erro inesperado",JOptionPane.ERROR_MESSAGE);
			System.out.println("Erro inesperado: "+e.getMessage());
			if (arquivoTmp.exists()){
				arquivoTmp.delete();
			}
		}

	}
	
	public static boolean obterAlias(){
		try {
			Provider pr = new sun.security.pkcs11.SunPKCS11(CFG);
			Security.addProvider(pr);

			KeyStore ks = KeyStore.getInstance("pkcs11",pr);
			ks.load(null, PASSWORD);

			Enumeration<String> aliasEnum = ks.aliases();
			while (aliasEnum.hasMoreElements()) {
				ALIAS = (String) aliasEnum.nextElement();
				if (ks.isKeyEntry(ALIAS)) {
					System.out.println("Alias identificado no token.");
				}
			}			
			return true;
		}catch (Exception e) {
			//System.out.println("Erro na obtenção do alias no token: "+e.getMessage());
			return false;			
		}
	}
		
	public static void carregarArquivoConfiguracao(){
		try {

			//config.load(new FileInputStream(arquivo));
			APPPATH = System.getProperty("user.dir") + File.separator;
			PATHDRIVE = APPPATH + "etc" + File.separator + "drives" + File.separator + "win" + File.separator; 
			CFG1 = PATHDRIVE + "tokenverde.cfg";
			CFG2 = PATHDRIVE + "tokenazul.cfg";			
			PATH = APPPATH + "home" + File.separator + "entrada" + File.separator;
			PATHASS = APPPATH + "home" + File.separator + "saida" + File.separator;
			SCRIPT = "tmpAssina.bat";
			JARSIGNER = APPPATH + "java7"+ File.separator + "bin" + File.separator +"jarsigner.exe";


			System.out.println("Arquivo de configuração carregado.");

		} catch (Exception ex) {
			System.out.println("Erro na leitura do arquivo de configuração :"+ex.getMessage());

		}
	}
	
	private static void executarBatSO(File arquivo) throws Exception {
				
		try{			
			if (arquivo.exists()){
				
				System.out.println("Executando processo de assinatura de arquivos. Aguarde ...");				
				Process p = Runtime.getRuntime().exec("cmd /C "+arquivo.getName());  				
				InputStream in = p.getInputStream();  
				@SuppressWarnings("unused")
				int ch;
				System.out.print("Assinando ... ");
				while ((ch = in.read()) != -1) {  
					System.out.println("... ... ...");
				}
				System.out.println("... concluido!");
				
				arquivo.delete();
				System.out.println("Rotina de assinatura executada.");	
				
			}
					
		} catch (Exception e) {
			System.out.println("Erro ao executar rotina no SO.");
		}
		
	}
		
	public static String montarComando(String arquivo, String arquivoAssinado){
		
		StringBuilder comando = new StringBuilder();
		
		comando.append("\"");
		comando.append(JARSIGNER);
		comando.append("\"");
		comando.append(" ");
		comando.append("-keystore NONE -storetype PKCS11 -providerClass sun.security.pkcs11.SunPKCS11 -providerArg");
		comando.append(" ");
		comando.append("\"");	
		comando.append(CFG);
		comando.append("\"");	
		comando.append(" ");
		comando.append("-storepass");
		comando.append(" ");
		comando.append("\"");	
		comando.append(PASSWORD);
		comando.append("\"");	
		comando.append(" ");
		comando.append("-sigfile SERPRO");
		comando.append(" ");
		comando.append("-signedjar");
		comando.append(" ");
		comando.append("\"");			
		comando.append(arquivoAssinado);
		comando.append("\"");
		comando.append(" ");
		comando.append("\"");
		comando.append(arquivo);
		comando.append("\"");
		comando.append(" ");
		comando.append("\"");
		comando.append(ALIAS);
		comando.append("\"");
		
		return comando.toString();
	}
	
}
