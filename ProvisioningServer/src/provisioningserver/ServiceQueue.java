package provisioningserver;


import java.net.DatagramPacket;

public class ServiceQueue {
	private DatagramPacket[] packetArray;
	private static ServiceQueue queue = null;

	int push =0;
	int pop = 0;
	final int QUEUE_SIZE = 400;
	private boolean isFull;
	
	
	
	public ServiceQueue(){
		
		packetArray = new DatagramPacket[QUEUE_SIZE];
		push =0;
		pop = 0;
		isFull = false;
		
	}//
	
	
	public static ServiceQueue getInstance()
	 {
	   if (queue == null)		   
		   startQueue();
	   return queue;
	}
	
	private static synchronized void startQueue(){
		if(queue == null){
			queue = new ServiceQueue();
		}			
		   
	}
	
	public boolean isEmpty(){
		return push == pop;
	}
	
	public boolean isFull(){
		return (push+1)%QUEUE_SIZE == pop;
	}
	
	public synchronized void push(DatagramPacket packet){
		if (isFull()){
			isFull = true;
			try{
				wait();
			}
			catch(Exception e){
				//logger.fatal("Push: ", e);
			}
		}
		if (isEmpty()){
			notifyAll();			
		}
		push = (push+1)%QUEUE_SIZE;
		packetArray[push] = packet;
		//System.out.println("Push: "+push);
	}//end of method push
	
	public synchronized DatagramPacket pop(){
		if (isEmpty()) {
			try{
				wait();
			}
			catch(Exception e){
				//logger.fatal("Pop: ", e);
			}
		}
		
		if (isFull){
			isFull = false;
			notifyAll();
		}
		pop = (pop+1)%QUEUE_SIZE;
		DatagramPacket packet = packetArray[pop];
		packetArray[pop] = null;
		//System.out.println("Pop: "+pop);
		return packet;
		
	}//end of method pop
	
	public int getpacketArraySize(){
		int size=0;
		if (push >= pop) {
			size = (push - pop);
		}
		
		return size;
	}
	

}//end of class
