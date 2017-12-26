package provisioningserver;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import shutdown.ShutDownListener;
import shutdown.ShutDownService;

public class ProvisioningServerMain implements ShutDownListener{
	
	static Logger logger = Logger.getLogger(ProvisioningServerMain.class);
	public static ProvisioningServerMain obprovisioningserver = null;
	public static  UDPServerTLV obUDPServerTLV = null;
	
	public static void main(String[] args)	
	{
		PropertyConfigurator.configure("log4j.properties");
		
		obprovisioningserver = new ProvisioningServerMain();
		obUDPServerTLV = UDPServerTLV.getInstance();		
		obUDPServerTLV.start();
		ShutDownService.getInstance().addShutDownListener(obprovisioningserver);
		logger.debug("Server started successfully.");
		
	}
	
	@Override
	public void shutDown()
	{
		UDPServerTLV.getInstance().shutdown();
		logger.debug("Shut down server successfully");
		System.exit(0);
	}

}
