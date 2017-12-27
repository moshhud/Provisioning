package provisioningserver;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;
import org.apache.log4j.Logger;

public class UDPServer extends Thread{
	public static UDPServer obUDPServerTLV;
	static Logger logger = Logger.getLogger(UDPServer.class);
	ProcessService obProcessService;
	   
	
	String remoteIP = "192.168.20.130";
	int remotePort = 220;
	static String serverIP = "43.240.101.55";
 	static int serverPort = 4444;
	DatagramSocket serverSocket = null;
	boolean running = false;
	
	
	public static UDPServer getInstance(){
		if(obUDPServerTLV==null){			
			createInstance();
		}
		
		return obUDPServerTLV;
	}//
	
	public static synchronized UDPServer createInstance(){
		if(obUDPServerTLV==null){
			obUDPServerTLV = new UDPServer();
			LoadConfiguration();
		}
		return obUDPServerTLV;
	}//
	
	@Override
	public void run(){
		 try{
		   	  	
			    logger.debug("run...");
	   	  	    serverSocket = new DatagramSocket(serverPort, InetAddress.getByName(serverIP));	   	  	    
	            logger.debug("Server started with: "+serverIP+":"+serverPort); 
	            byte[] receiveData = new byte[2048];
	            byte[] sendData = new byte[2048];
	            
	            obProcessService = new ProcessService();
	            obProcessService.start();
	            running = true;
	            
	            while(running)
	               {
	               	  
	                  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	                  logger.debug("Waiting for client Request...");
	                  serverSocket.receive(receivePacket);
	                  
	                  InetAddress IPAddress = receivePacket.getAddress();
	                  remoteIP = IPAddress.toString().replace("/", "");
	                  remotePort = receivePacket.getPort();      
	                  logger.debug("Received from Client [" +remoteIP+":"+remotePort+"] "+receivePacket.getLength());    
	                   
	                  ServiceQueue.getInstance().push(receivePacket);
	                  int queueSize = ServiceQueue.getInstance().getpacketArraySize();			
	      			  logger.debug("Queue size: "+queueSize);
	                  
	               }
	   	    }
	   	    catch(Exception e){
	   	  	  logger.fatal("Error 1: "+e);
	   	  	  
	   	    }
	}//end of run
	
	
public static void LoadConfiguration(){
		
		FileInputStream fileInputStream = null;
		String strConfigFileName = "properties.cfg";
		try
	    {
			Properties properties = new Properties();
		    File configFile = new File(strConfigFileName);
		    if (configFile.exists())
		      {
		    	fileInputStream = new FileInputStream(strConfigFileName);
		        properties.load(fileInputStream);		        
		        
		        
		        String Port = "220";
		        
		        if(properties.get("serverIP")!=null){
		        	serverIP = (String) properties.get("serverIP");
		        }
		        
		        if(properties.get("serverPort")!=null){
		        	Port =  (String) properties.get("serverPort");
		        }
		        try {
		        	serverPort = Integer.parseInt(Port);
		        }
		        catch(Exception e) {
		        	logger.fatal(e.toString());
		        }
		        		        
		        
		        fileInputStream.close();

		      }
	    }
		catch (Exception ex)
	    {
	      logger.fatal("Error while loading configuration file :" + ex.toString(), ex);
	      //System.out.println("Error: "+ex);
	      System.exit(0);
	    }
	    finally
	    {
	      if (fileInputStream != null)
	      {
	        try
	        {
	        	fileInputStream.close();
	        }
	        catch (Exception ex)
	        {
	        	logger.fatal(ex.toString());
	        }
	      }
	    }
		
	}
	
	public void shutdown()
	{
		logger.debug("Server shutdown");
		obProcessService.shutdown();		
	    running = false;	    
	 }//
    



}
