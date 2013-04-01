package adapter.impl.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ChangeManager {

	private String INSTANTIATIONPATTERN = "/*public interface is instanciated with the Facade*/";
	private String REGISTRATIONPATTERN = "/*interfaces are set as provided*/";
	private String REGISTRATIONLISTPATTERN = "/*create a list with interface names and set as provided interfaces */";
	private static String localMgr = "/home/leonardo/workspace6/Java2Cosmos/src/java/adapter/impl/rules/";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChangeManager cm = new ChangeManager();
		String managerPath = localMgr + "Manager.java";
		

	}

	public boolean changeManager(String managerPath, List<InterfaceMetaInfo> interfaceList ){
		File manager = new File(managerPath);
		if( manager.exists()){
			String managerStr = readManager( manager );
			String result = changesControler( managerStr, interfaceList );
			boolean created = writeManager( manager, result);
			return created;
		}
		else{
			System.err.println("The file "+managerPath+" does not exist.");
			return false;
		}
	}
	
	private String changesControler(String managerStr, List<InterfaceMetaInfo> interfaceList ){
		String instantiation = createInstantiation( interfaceList );
		int instantiationPos = searchInstantiation( managerStr, this.INSTANTIATIONPATTERN );
		managerStr = this.replace(instantiation, managerStr, instantiationPos, (instantiationPos + 1) );
		
		String registration = createRegistration( interfaceList );
		int registrationPos = searchRegistration( managerStr, this.REGISTRATIONPATTERN );
		managerStr = this.replace(registration, managerStr, registrationPos, (registrationPos + 1) );
		
		String auxPattern = "String[] listProv = new String[";
		int registrationListPos = searchListRegistration( managerStr, this.REGISTRATIONLISTPATTERN );
		int j = managerStr.indexOf( auxPattern , registrationListPos );
		int start;
		String listInitialization = new String("");
		if( j < 0 ){
			start = 0;
			listInitialization += "\n" + auxPattern + " " + interfaceList.size() + "];\n";
			managerStr = this.replace(listInitialization, managerStr, registrationListPos, (registrationListPos + 1) );
			j = registrationListPos;
		}
		else{
			
			j += auxPattern.length();
			char index = managerStr.charAt( j );
			int size = charToInt( index );
			start = size;
			size += interfaceList.size();
			managerStr = this.replace(String.valueOf( size ), managerStr, j, ( j + 1 ));
			j = j + "];".length() + 1;
			
		}
		String registrationList = listRegistration( interfaceList ,start);
		registrationList = listInitialization + registrationList ;
		managerStr = this.replace( registrationList, managerStr, j , (j + 1) );
		
		return managerStr;
	}
	
	private int charToInt(char c){
		String s = String.valueOf(c);
		int i = Integer.parseInt(s);
		return i;
	}
	
	private int longToInt(long i){
		String tam = String.valueOf(i);
		int length = Integer.parseInt( tam );
		return length;
	}
	
	public String readManager(File manager){
		try {
			if( ( manager != null ) && ( manager.exists() )){
				long size = manager.length();
				int length = longToInt(size);
				byte[] b = new byte[length];
				FileInputStream fis = new FileInputStream(manager);
				fis.read(b);
				String str = new String(b);
				return str;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean writeManager(File manager, String newContent){
		if( (manager == null) || ( !manager.exists() ) ){
			System.err.println("Manager file is either empty or null");
			return false;
		}
		else{
			try {
				FileOutputStream fos = new FileOutputStream(manager);
				byte[] b = newContent.getBytes();
				fos.write(b);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} 
		}
		
	}
	
	private String createInstantiation(List<InterfaceMetaInfo> interfaceList){
		if(interfaceList == null)
			return null;
		else{
			String result = new String("");
			for(int i=0;i<interfaceList.size();i++){
				InterfaceMetaInfo imi = interfaceList.get(i);
				result += imi.getInterfaceName() + " providedInterface"+i+" = new Facade"+i+"();\n";
			}
			return result;
		}
	}
	
	private int searchInstantiation(String managerStr, String pattern){
		int result;
		int i = managerStr.indexOf(pattern);
		if( i < 0 ){
			String anotherPattern = new String("HashMap<String,Object>();");
			int j = managerStr.indexOf(anotherPattern);
			j += anotherPattern.length();
			result = j;
		}
		else{
			i += pattern.length();
			result = i;
		}
		return result;
	}
	
	
	private String createRegistration(List<InterfaceMetaInfo> interfaceList){
		
		if(interfaceList == null)
			return null;
		else{
			String result = new String("");
			for(int i=0;i<interfaceList.size();i++){
				InterfaceMetaInfo imi = interfaceList.get(i);
				result += "this.setProvidedInterface(\""+imi.getInterfaceName() + "\", providedInterface"+i+");\n";
			}
			return result;
		}
		
	}
	
	private int searchRegistration(String managerStr, String pattern){
		int i = managerStr.indexOf(pattern);
		i += pattern.length();
		return i;
	}
	
	
	private String listRegistration(List<InterfaceMetaInfo> interfaceList,int start){
		/*listProv[0] = "IExportToCosmos";*/
		
		if(interfaceList == null)
			return null;
		else{
			int j = start;
			String result = new String("");
			for(int i=0;i<interfaceList.size();i++){
				//InterfaceMetaInfo imi = interfaceList.get(i);
				j = start + i;
				result += "listProv["+j+"] = \"providedInterface"+i+"\";\n";
			}
			return result;
		}
		
	}
	
	private int searchListRegistration(String managerStr, String pattern){
		int i = managerStr.indexOf(pattern);
		i += pattern.length();
		return i;
	}
	
	private String replace(String replacement,String oldString,int begin,int end){
		String newStr = oldString.substring(0, begin);
		newStr = newStr.concat(replacement);
		newStr = newStr.concat(oldString.substring(end, oldString.length()));
		return newStr;
	}
}
