package provisioningserver;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

import org.apache.log4j.Logger;

import databasemanager.DatabaseManager;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Processor extends Thread{
	static Logger logger = Logger.getLogger(Processor.class);
	DatagramPacket receivePacket = null;
	final int PACKET_TYPE_IPCHANGER_IP_REQUEST=0x0000;
	final int PACKET_TYPE_IPCHANGER_IP_RESPONSE=0x0001;
	final int VOICE_LISTEN_IP_LIST=0x0002;
	final int MEDIA_PROXY_PUBLIC_IP_LIST=0x0003;
	final int OPERATOR_CODE=0x0004;  
	
	final int PACKET_TYPE_MAILSERVER_REQUEST=0x0064;
	final int PACKET_TYPE_MAILSERVER_RESPONSE=0x0065;
	final int MAILSERVER_IP=0x0066;
	final int MAILSERVER_PORT=0x0067;
	final int AUTH_MAIL_ADDRESS=0x0068;
	final int AUTH_MAIL_PASS=0x0069;	

	String remoteIP = "192.168.20.130";
	int remotePort = 220;
	String ip_changer_voice_listen_ip = null;
	String ip_changer_public_ip = null;
	String operator_code = null;
	
	String mailServerIP = null;
	String mailServerPort=null;
	String mailID = null;
	String mailPass = null;
	
	
	

	public Processor(DatagramPacket receivePacket){
		this.receivePacket = receivePacket;
		InetAddress IPAddress = receivePacket.getAddress(); 
		remoteIP = IPAddress.toString().replace("/", "");;
		remotePort = receivePacket.getPort();
		
		
	}
	public void run(){			
		processConnection();		
	}
	

	
	private synchronized void processConnection(){
		try{
			logger.debug("Processing IP: "+remoteIP+":"+remotePort);
			byte[] data = new byte[2048];
            System.arraycopy(receivePacket.getData(), 0, data, 0, receivePacket.getLength());                 
            int dcb = decodeBytes(data,receivePacket.getLength());
            
            checkReceivedData(data,dcb);
            
		}
	    catch (Exception e) {		
		  logger.debug("Error: "+e.toString());
		 
	    }
		
	}//

public void checkReceivedData(byte[] data,int len){
	 	
        
        int index=0;
        
        int pktType=data[index++];
        pktType=(pktType<<8)|data[index++];
            
        int pktLen=data[index++];
        pktLen=(pktLen<<8)|data[index++];
            
        logger.debug("Packet Type: "+pktType+", Packet Len: "+pktLen); 
        
        try{
        	switch(pktType){
            	case PACKET_TYPE_IPCHANGER_IP_REQUEST:              	             	    
            	    logger.debug("Got configuration request");
            	    //check source IP with database and if exists then provide configuration...
            	    if(loadDataFromDB()) {
            	    	DatagramPacket sendPacket = createConfigurationPacket();                  
            	    	UDPServer.getInstance().serverSocket.send(sendPacket);
            	    }
            	    else {
            	    	logger.debug("No Data Found");
            	    }
            	    
            	    break;
            	case PACKET_TYPE_MAILSERVER_REQUEST:
            		logger.debug("Got IP Changer info request");
            		if(loadDataFromDB_MailInfo()) {
            	    	DatagramPacket sendPacket = createResponsePacket();                  
            	    	UDPServer.getInstance().serverSocket.send(sendPacket);
            	    }
            	    else {
            	    	logger.debug("No Data Found");
            	    }
            		break;
            	default:
            	    logger.debug("Invalid Request.");
            	    break;
            	
            	    
            	    
           }
        }
        catch(Exception e){
        	logger.debug(e.toString());
        }
        
	 }//
     
     public int encodeBytes(byte [] data,int len){
		  Random rand=new Random();
		  int randLen=rand.nextInt(20)+3;
		  
		  for(int i=len-1;i>=0;i--){
		   data[i+randLen+2]=data[i];
		  }
		  data[0]=(byte)(randLen>>8 & 0xff);
		  data[1]=(byte)(randLen & 0xff);
		  for(int i=0;i<randLen;i++){
		   data[2+i]=(byte) rand.nextInt(256);
		  }
		  for(int i=0;i<len;i++){
		   data[i+randLen+2]=(byte) (data[(i%randLen)+2]^data[i+2+randLen]);
		  }
		  byte temp=data[1];
		  data[1]=data[4];
		  data[4]=temp;
		  return randLen+len+2;
     }//
     
     public int decodeBytes(byte [] data,int len){
		  int index=0;
		  if(len>=5){
		   byte temp = data[1];
		         data[1] = data[4];
		         data[4] = temp;
		         int randLen = data[0];
		         randLen=(randLen<<8)|data[1];
		         int dataLen=len-randLen-2;
		         if (dataLen>0) {
		             for (int i = 0; i < dataLen; i++) {
		              data[i + randLen + 2] = (byte) (data[i + randLen + 2] ^ data[i % randLen + 2]);
		             }
		             for(int i=0;i<dataLen;i++){
		              data[i]=data[i + randLen + 2];
		             }
		             index=dataLen;
		         }
		  }
		  return index;
	 }
     
     private  DatagramPacket createConfigurationPacket() throws UnknownHostException{
			        
	        byte [] sendData=new byte[2048];
	        int index=0;   	          
	         
	        //packet type
	        sendData[index++]=(byte)((PACKET_TYPE_IPCHANGER_IP_RESPONSE>>8) & 0xff);
	        sendData[index++]=(byte)((PACKET_TYPE_IPCHANGER_IP_RESPONSE) & 0xff);
	        //packet length	        
	        //sendData[index++]=(byte)(((0x0012)>>8) & 0xff);
	        //sendData[index++]=(byte)(((0x0012)) & 0xff);
	        index++;
	        index++;
	        //attribute type    
	        sendData[index++]=(byte)(((VOICE_LISTEN_IP_LIST)>>8) & 0xff);
	        sendData[index++]=(byte)(((VOICE_LISTEN_IP_LIST)) & 0xff);
	        
	        String IP = ip_changer_voice_listen_ip;
	        int l = IP.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)IP.charAt(i);
	        }
	        
	        //attribute type    
	        sendData[index++]=(byte)(((MEDIA_PROXY_PUBLIC_IP_LIST)>>8) & 0xff);
	        sendData[index++]=(byte)(((MEDIA_PROXY_PUBLIC_IP_LIST)) & 0xff);
	        
	        IP = ip_changer_public_ip;
	        l = IP.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)IP.charAt(i);
	        }
	        
	        sendData[2]=(byte)(((index)>>8) & 0xff);
	        sendData[3]=(byte)(((index)) & 0xff);
         
	        //logger.debug("Index: "+index);
	           
	       	      
	        index=encodeBytes(sendData, index);
	        
	        DatagramPacket sendPacket=new DatagramPacket(sendData,index,InetAddress.getByName(remoteIP),remotePort);
	        return sendPacket;
	}// 
     
     private  DatagramPacket createResponsePacket() throws UnknownHostException{
	        
	        byte [] sendData=new byte[2048];
	        int index=0;   	          
	         
	        //packet type
	        sendData[index++]=(byte)((PACKET_TYPE_MAILSERVER_RESPONSE>>8) & 0xff);
	        sendData[index++]=(byte)((PACKET_TYPE_MAILSERVER_RESPONSE) & 0xff);	        
	        index++;
	        index++;
	        //attribute type    
	        sendData[index++]=(byte)(((MAILSERVER_IP)>>8) & 0xff);
	        sendData[index++]=(byte)(((MAILSERVER_IP)) & 0xff);
	        
	        String str = mailServerIP;
	        int l = str.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)str.charAt(i);
	        }	        
	        	        
	        //attribute type    
	        sendData[index++]=(byte)(((MAILSERVER_PORT)>>8) & 0xff);
	        sendData[index++]=(byte)(((MAILSERVER_PORT)) & 0xff);
	        
	        str = mailServerPort;
	        l = str.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)str.charAt(i);
	        }
	        
	      //attribute type    
	        sendData[index++]=(byte)(((AUTH_MAIL_ADDRESS)>>8) & 0xff);
	        sendData[index++]=(byte)(((AUTH_MAIL_ADDRESS)) & 0xff);
	        
	        str = mailID;
	        l = str.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)str.charAt(i);
	        }
	        //attribute type    
	        sendData[index++]=(byte)(((AUTH_MAIL_PASS)>>8) & 0xff);
	        sendData[index++]=(byte)(((AUTH_MAIL_PASS)) & 0xff);
	        
	        str = mailPass;
	        l = str.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)str.charAt(i);
	        }
	        
	        sendData[index++]=(byte)(((VOICE_LISTEN_IP_LIST)>>8) & 0xff);
	        sendData[index++]=(byte)(((VOICE_LISTEN_IP_LIST)) & 0xff);
	        
	        str = ip_changer_voice_listen_ip;
	        l = str.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)str.charAt(i);
	        }
	        
	        //attribute type    
	        sendData[index++]=(byte)(((MEDIA_PROXY_PUBLIC_IP_LIST)>>8) & 0xff);
	        sendData[index++]=(byte)(((MEDIA_PROXY_PUBLIC_IP_LIST)) & 0xff);
	        
	        str = ip_changer_public_ip;
	        l = str.length();	        
	        
	       	//attribute length        
	        sendData[index++]=(byte)(((l)>>8) & 0xff);
	        sendData[index++]=(byte)(((l)) & 0xff);
	        
	        //attribute value
	        for(int i=0;i<l;i++){	        		        	
	        	sendData[index++] = (byte)str.charAt(i);
	        }
	        
	        
	        //packet length
	        sendData[2]=(byte)(((index)>>8) & 0xff);
	        sendData[3]=(byte)(((index)) & 0xff);
      
	        //logger.debug("Index: "+index);
	           
	       	      
	        index=encodeBytes(sendData, index);
	        
	        DatagramPacket sendPacket=new DatagramPacket(sendData,index,InetAddress.getByName(remoteIP),remotePort);
	        return sendPacket;
	}// 
     
     
     
     
     public boolean loadDataFromDB() {
    	 
    	 Connection connection = null;
         PreparedStatement ps = null;
         Statement stmt = null;
         String sql = "";
         
         try {
        	 connection = DatabaseManager.getInstance().getConnection();          
             stmt = connection.createStatement();          
             
             sql = "select ip_changer_voice_listen_ip,ip_changer_public_ip from byteSaverConfigTable where ip_changer_ip=? and ip_changer_port=?";
             ps = connection.prepareStatement(sql);
             ps.setString(1, remoteIP);
             ps.setLong(2, remotePort);
             ResultSet idSet = ps.executeQuery();
             
             boolean dataExists = false;
             
             if (idSet.next()) {            	 
            	 ip_changer_voice_listen_ip = idSet.getString("ip_changer_voice_listen_ip");
            	 ip_changer_public_ip = idSet.getString("ip_changer_public_ip");            	 
            	 
            	 dataExists = true;
             }
             if(!dataExists) {
            	 return false;
             }
         }
         catch(Exception e) {
        	 logger.fatal(e.toString());
         }
         finally
 		{				
 			if(stmt!=null)
 				try
 				{
 					stmt.close();
 					stmt=null;
 				}catch(Exception ex){}					
 			if(connection!=null)
 				try
 				{
 					connection.close();						
 				}catch(Exception ex){}
 			connection = null;
 		}
    	 
    	 return true;
     }
     
public boolean loadDataFromDB_MailInfo(){
    	 
    	 Connection connection = null;
         PreparedStatement ps = null;
         Statement stmt = null;
         String sql = "";
         
         try {
        	 connection = DatabaseManager.getInstance().getConnection();          
             stmt = connection.createStatement();          
             
             sql = "select operator_code,ip_changer_voice_listen_ip,ip_changer_public_ip from byteSaverConfigTable where ip_changer_ip=? and ip_changer_port=?";
             ps = connection.prepareStatement(sql);
             ps.setString(1, remoteIP);
             ps.setLong(2, remotePort);
             ResultSet idSet = ps.executeQuery();
             
             boolean dataExists = false;
             
             if (idSet.next()) {
            	 operator_code = idSet.getString("operator_code");
            	 ip_changer_voice_listen_ip = idSet.getString("ip_changer_voice_listen_ip");
            	 ip_changer_public_ip = idSet.getString("ip_changer_public_ip");
            	 dataExists = true;
             }
             if(dataExists) {
            	 sql = "select msMailServer,msMailSeverPort,msAuthEmailAddress,msAuthEmailPassword from vbMailServerInformation";
                 ps = connection.prepareStatement(sql);                 
                 idSet = ps.executeQuery();
                 if (idSet.next()) {
                		mailServerIP = idSet.getString("msMailServer");
                		mailServerPort = idSet.getString("msMailSeverPort");
                		mailID = idSet.getString("msAuthEmailAddress");
                		mailPass = idSet.getString("msAuthEmailPassword");
                 }
                 else{
                	 return false;
                 }
                 
             }
             else{
            	 return false;
             }
         }
         catch(Exception e) {
        	 logger.fatal(e.toString());
         }
         finally
 		{				
 			if(stmt!=null)
 				try
 				{
 					stmt.close();
 					stmt=null;
 				}catch(Exception ex){}					
 			if(connection!=null)
 				try
 				{
 					connection.close();						
 				}catch(Exception ex){}
 			connection = null;
 		}
    	 
    	 return true;
     }
	

	

}// End of class
