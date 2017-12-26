package provisioningserver;


import java.net.DatagramPacket;


public class ProcessService extends Thread{
	boolean running = false;
	
	public ProcessService(){
		try {
			running = true;	
					
		}
		catch (Exception e){
			
		}
	}//
	
	public void run() {
		DatagramPacket receivePacket = null;		
		while (running){
			try{
				receivePacket = ServiceQueue.getInstance().pop();
				Processor ob = new Processor(receivePacket);
				ob.start();
				Thread.sleep(200);
								
			}
			catch (Exception e){
			
			}			
		}		
	}
	
	public void shutdown(){
		running = false;
	}

	
}//end of class
