/**
 * ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    engine.eval("print('Hello, World')");
 */
package net.jtreemer.labolink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Properties;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import fi.iki.elonen.util.ServerRunner;
import net.jtreemer.labolink.db.HibernateUtil;



public class Main {
	
	private static File pidFile = null;
	private static HapiContext hapiContext = null;
	private static HL7Service connection = null; 
	private static Thread mainProc = null;
	public static Properties AppConfig = null;

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	
	public static void main(String[] args) throws IOException {
		
		String reciver_home = System.getenv("LABOLINK_HOME");
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		
		File dir = new File(reciver_home);
		if(!dir.isDirectory()){
			dir.mkdirs();
		}
		pidFile = new File(dir, "serv.pid");
		File f = pidFile;
		FileWriter fw = new FileWriter(f,false);
		fw.write(pid);
		fw.close();
		f.setLastModified(new Date().getTime());
		f.deleteOnExit();
		
		hapiContext = new DefaultHapiContext();
		
		File configFile = new File(dir,"recieving.config");
		Properties prop = new Properties();
		if(configFile.exists() && configFile.isFile()){
			try{
				FileInputStream fileInputStream = new FileInputStream(configFile);
				prop.load(fileInputStream);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		AppConfig = prop;
		String outPath = "";
		String outFile = "out.hl7";
		String portNumber ="8888";
		if(System.getProperties().containsKey("hl7.output.pathname")) {
			outPath=System.getProperty("hl7.output.pathname","");
		}else {
			outPath = prop.getProperty("hl7.output.pathname", "");
		}
		
		if(System.getProperties().containsKey("hl7.output.filename")) {
			outFile=System.getProperty("hl7.output.filename","out.hl7");
		}else {
			outFile = prop.getProperty("hl7.output.filename", "out.hl7");
		}
		
		
		if(System.getProperties().containsKey("hl7.input.portnumber")) {
			portNumber=System.getProperty("hl7.input.portnumber","8888");
		}else {
			portNumber = prop.getProperty("hl7.input.portnumber", "8888");
		}
				
		int portno = 8888;
		try{
			portno = Integer.parseInt(portNumber, 10);
			if( portno< 1 && portno > 65535){
				portno = 8888;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		String mode = Main.AppConfig.getProperty("export.mode", "file");
		if(!mode.equals("file"))
			HibernateUtil.getSessionFactory();
		HL7Reciving handler = new HL7Reciving(outPath,outFile);
		BBConnectionListener connectionListener = new BBConnectionListener();
		connection = null;
		try {
			System.out.println("Port number: "+portno);
			System.out.println("Path output: "+outPath);
			System.out.println("File output: "+outFile);
			connection = hapiContext.newServer(portno, false);
			connection.registerApplication("ORU", "R01", handler);
			connection.registerConnectionListener(connectionListener);
			connection.start();
			System.out.println("Server Started");
			//connection.startAndWait();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		intitialOrderingWS();
		
		//hapiContext.close();
	}
	
	public static void intitialOrderingWS() {
		String isAllowOrder = AppConfig.getProperty("allowOrdering", "FALSE");
		if("TRUE".equals(isAllowOrder)) {
			try {
				new LaboLinkOrder(80).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void start(String[] args) throws IOException {
		//String reciver_home = System.getenv("LABOLINK_HOME");
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		
		File dir = new File(".");
		if(!dir.isDirectory()){
			dir.mkdirs();
		}
		pidFile = new File(dir, "serv.pid");
		File f = pidFile;
		FileWriter fw = new FileWriter(f,false);
		fw.write(pid);
		fw.close();
		f.setLastModified(new Date().getTime());
		f.deleteOnExit();
		
		hapiContext = new DefaultHapiContext();
		File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		
		File configFile = new File(jarDir,"recieving.config");
		Properties prop = new Properties();
		if(configFile.exists() && configFile.isFile()){
			try{
				FileInputStream fileInputStream = new FileInputStream(configFile);
				prop.load(fileInputStream);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		AppConfig = prop;
		
		String outPath = prop.getProperty("hl7.output.pathname", "");
		String outFile = prop.getProperty("hl7.output.filename","out.hl7");
		String portNumber = prop.getProperty("hl7.input.portnumber", "8888");
		int portno = 8888;
		try{
			portno = Integer.parseInt(portNumber, 10);
			if( portno< 1 && portno > 65535){
				portno = 8888;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		HibernateUtil.getSessionFactory();
		HL7Reciving handler = new HL7Reciving(outPath,outFile);
		BBConnectionListener connectionListener = new BBConnectionListener();
		connection = null;
		try {
			System.out.println("Port number: "+portno);
			System.out.println("Path output: "+outPath);
			System.out.println("File output: "+outFile);
			connection = hapiContext.newServer(portno, false);
			connection.registerApplication("ORU", "R01", handler);
			connection.registerConnectionListener(connectionListener);
			connection.start();
			//connection.startAndWait();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		//hapiContext.close();
	}
	
	public static void stop(String args[]) throws Exception {
		HibernateUtil.shutdown();
		if(connection!=null){
			
			connection.getExecutorService().shutdownNow();
			connection.stop();
		}
		if(hapiContext!=null){
			hapiContext.close();
			
		}
		
	}

}
