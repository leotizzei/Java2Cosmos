package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import adapter.spec.prov.IExportToCosmos;

class Util {

	
	public static void main(String args[]){
		Util u = new Util();
		//String str = u.changePackage("package adapter.impl.rules;import blablabla", "package newComponent.impl;");
		//System.out.println("res = "+str);
		File source = new File("/home/lsd/ra001973/workspace2/adapter/src/java/test2/IHelloWorld.java");
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(source.getCanonicalPath()));
			char[] cbuf = new char[4048];
			in.read(cbuf, 0, 4048);
			String file = String.valueOf(cbuf);
			String teste = new String("bla bla bla ");
			StringPosition position = new StringPosition();
			position.beginString = 0;
			position.endString = 15;
			boolean isCom= u.isNotCommented(position, teste); 
			System.out.println(isCom);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	protected StringPosition readPackage(String file){
		
		int packInt = file.indexOf("package ");
		if(packInt>=0){
			int semicolon = file.indexOf(";",packInt);
			if(semicolon < 0 ){
				return null;
			}
			else{
				semicolon++;
				StringPosition sp = new StringPosition();
				sp.beginString = packInt;
				sp.endString = semicolon;
				return sp;
			}
		}
		return null;
		
		
	}
	
	protected StringPosition readClassVisibility(String file){
		String pattern = new String("public ");
		
		int visibilityPos = file.indexOf(pattern);
		int classPos = file.indexOf("class ");
		if( ( visibilityPos >= 0 ) && ( classPos > visibilityPos ) ){
			StringPosition sp = new StringPosition();
			sp.beginString = visibilityPos;
			sp.endString = visibilityPos + pattern.length();
			return sp;
		}
		return null;
	}
	
	protected StringPosition readImport(String file){
		
		//read from the template Facade
		String pattern = new String("import ");
		int importPos = file.indexOf(pattern);
		int semicolon = file.indexOf(';', importPos);
		semicolon++;
		if(importPos>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = importPos;
			sp.endString = semicolon;
			return sp;		}
		else
			return null;
	}
	
	/**
	 *  get the interface name from the old/current interface
	 * @param file
	 * @return
	 */
	protected String getInterfaceName(String file){
		String pattern = new String("interface ");
		int interfacePos = file.indexOf( pattern );
		if(interfacePos >= 0 ){
			interfacePos += pattern.length();
		}
		else
			return null;
		String result;
		
		int index = interfacePos;
		//read white spaces
		while(file.charAt(index) == ' ')
			index++;
		int beginIndex = index;
		int endIndex = index;
		//read interface name
		while((file.charAt(endIndex) != ' ') && (file.charAt(endIndex) != '{'))
			endIndex++;
			
		result = file.substring(beginIndex, endIndex);
		return result;
			
	}
	
	
	/*get the interface name from the import*/
	protected StringPosition readInterfaceNameFromImport(String file){
		String pattern = new String("import adapter.spec.prov.IExportToCosmos;");
		int importinterface = file.indexOf(pattern);
		if(importinterface>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = importinterface;
			sp.endString = importinterface + pattern.length();
			return sp;		
		}
		else
			return null;
			
	}
	/**
	 * this method find the position to place "implement" 
	 * @param file an string which has the whole java source file in it
	 * @return
	 */
	protected StringPosition getImplementsPosition(String file){
		int interfacePosition = file.indexOf("interface ");
		int braces = file.indexOf("{",interfacePosition);
		
		StringPosition sp = new StringPosition();
		
		if( (braces >= 0) && ( interfacePosition >= 0) ){
			sp.beginString = braces;
			sp.endString = braces + 1;
		}
		return sp;
	}
	
	protected StringPosition findPattern(String str,String file){
		String pattern = new String(str);
		int position = file.indexOf(pattern);
		if(position>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = position;
			sp.endString = position + pattern.length();
			return sp;		
		}
		else
			return null;
	}
	
	protected StringPosition readInterfaceNameImplements(String file){
		String pattern = new String("implements IExportToCosmos");
		int implementPos = file.indexOf(pattern);
		if(implementPos>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = implementPos;
			sp.endString = implementPos + pattern.length();
			return sp;		
		}
		else
			return null;
	}
	
	protected StringPosition readMethod(String file){
		String pattern = new String("{");
		int classPos = file.indexOf("class Facade");
		int position = file.indexOf(pattern,classPos);
		if(position>=0){
			position++;
			StringPosition sp = new StringPosition();
			sp.beginString = position;
			int lastBrace = file.lastIndexOf("}");
			lastBrace--;
			sp.endString = lastBrace;
			return sp;	
		}
		else
			return null;
	}
	
	
	protected String changePackage(String packageLine,String newPackage){
		int i = packageLine.indexOf("package ");
		if(i>=0){
			
			int packend = packageLine.indexOf(";", i);
			packend++;
			String pack = packageLine.substring(i, packend);
			String res = packageLine.replaceFirst(pack, newPackage);
			return res;
		}
		else{
			System.out.println("there is no package");
			return packageLine;
		}
	
			
	}
	
	protected String addImport(String oldLine, String newImport, int posCursor){
		int imp = oldLine.indexOf("import ",posCursor);
		if(imp>=0){
			//insert the newImport between the 'imp' position and the import
			String s = oldLine.substring(0, imp).concat(newImport);
			s = s.concat(oldLine.substring((imp+1), oldLine.length()));
			return s;
		}
		else{
			System.out.println("there is no import");
			return null;
		}
	}
	
	protected String changeVisibility(String line){
		int publik = line.indexOf("public ");
		if(publik>=0){
			String result = line.replaceFirst("public ", "       ");
			return result;
		}
		else{
			return null;
		}
	}
	
	protected static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
        System.out.println("[replace] pattern =("+pattern+") str=("+str+")");
        while ((e = str.indexOf(pattern, s)) >= 0) {
        	System.out.println("e = "+e);
            result.append(str.substring(s, e));
            result.append(replace);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        System.out.println("replace = "+result.toString());
        return result.toString();
    }
	
	protected String replace(String replacement,String oldString,int begin,int end){
		String newStr = oldString.substring(0, begin);
		newStr = newStr.concat(replacement);
		/*if(begin<end)
			end--;
			*/
		newStr = newStr.concat(oldString.substring(end, oldString.length()));
		return newStr;
	}
	/**
	 * return true if the string placed between position.beginString and position.endString
	 * is commented. Otherwise, returns false.
	 * @param position where the string is placed
	 * @param file the whole file in string format
	 * @return
	 */
	protected boolean isNotCommented(StringPosition position,String file){
		int beginCom = file.lastIndexOf("/*", position.beginString);
		int endCom = file.indexOf("*/", position.beginString);
		if( (beginCom >= 0) && (beginCom < endCom) && (endCom > position.beginString) )
			return true;
		
		beginCom = file.lastIndexOf("/*", position.endString);
		endCom = file.indexOf("*/", position.endString);
		if( (beginCom < endCom) && (beginCom > position.beginString) )
			return true;
		
		return false;
	}
}
