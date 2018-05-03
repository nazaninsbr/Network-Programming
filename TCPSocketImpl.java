import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Timer;
import java.util.TimerTask;

public class TCPSocketImpl extends TCPSocket {
	
	public EnhancedDatagramSocket socket;
	private int slowStart; 
	private long ssthreshold;
	private long cwnd; 
	private int seq_No; 
	private int ack_No;
	private int port;
	private int next_seq_No;
	private String ip;
	public TCPSocketImpl(String ip, int port) throws Exception {
		super(ip, port);
		// System.out.println("AAAAAAAAAAAAAAAA");
		//this.port=port;
		// this.socket= new EnhancedDatagramSocket(port);

		this.socket= new EnhancedDatagramSocket(this.port);
		this.port=port;
		this.ip=ip;
		
		
		 // System.out.println("OOOOOOOOOOOOOOOO");
		this.seq_No = 0;
		this.ack_No = 0;
		
		this.next_seq_No = 0;
		slowStart = 0;
		cwnd = 1;
	}

	public static Thread timeoutPacket(final int seqNo, final String ip, final ArrayList<String> fileContent, final int seconds, final EnhancedDatagramSocket socket,final int port) throws Exception{
		byte[] sendData = new byte[1024];
		sendData =fileContent.get(seqNo).getBytes();
		InetAddress ip_adress = InetAddress.getByName(ip);
		ExecutorService service = Executors.newSingleThreadExecutor();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress,port);
		return new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					Timer timer = new Timer();
					timer.schedule(new TimerTask(){ @Override
            				public void run(){
            					try{
            						System.out.println("Time's up!");
									socket.send(sendPacket);
									timer.cancel();
									// Thread t = timeoutPacket(seqNo, ip, fileContent, seconds, socket,port);
									// t.join();
            					}
            					catch (IOException ioe){
            						System.out.println("Exception!");
									ioe.printStackTrace();
									System.out.println(ioe);
								}
								catch(Exception ie){
									System.out.println("Exception 2!");
								}
			
							}
					}, seconds*1000);
					while(true){
						System.out.println("Waiting for ACK");
						socket.receive(receivePacket);
						String sentence = new String(receivePacket.getData());
					    String message =sentence.split("\\s+")[0];
					    String ack_number =sentence.split("\\s+")[1];
					    String seq_number =sentence.split("\\s+")[2];
					    int packet_ack_num =Integer.parseInt(ack_number);
					    int packet_seq_num =Integer.parseInt(seq_number);
					    System.out.println("here after time's up");
					    if(packet_ack_num-1==seqNo){
					    	timer.cancel();
					    	return;
					    }
					    
					}

				}catch(IOException ioe){

					System.out.println("Exception!");
					ioe.printStackTrace();
					System.out.println(ioe);

				}
			}
		});
	}

	public void handShaking(){
		try{
			System.out.println("in Handshaking!");
			InetAddress ip_adress = InetAddress.getByName(this.ip);
			byte[] sendData = new byte[1024];
			String seqNoString = Integer.toString(this.seq_No);
			String ackNoString = Integer.toString(this.ack_No);
			
			String message_for_send="SYN"+" "+seqNoString+" "+ackNoString;
			
			sendData =message_for_send.getBytes();
			System.out.println("message " + message_for_send);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
			this.socket.send(sendPacket);
			String  state = "SYN-SENT";

			while(true)
			{
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				//System.out.println(message_for_send);
				this.socket.receive(receivePacket);
				String sentence = new String(receivePacket.getData());
				System.out.println("sentence: "+ sentence);
				
				String[] splited = sentence.split("\\s+");
				int packet_seq_num = Integer.parseInt(splited[1]);
            	splited[2] = splited[2].replace("\n", "").replace("\r", "").replace(" ", "");
            	int packet_ack_num = Integer.parseInt(splited[2].trim());
				
				//InetAddress IPAddress = receivePacket.getAddress();
				port = receivePacket.getPort();

				if(state.equals("SYN-SENT")){
					if(packet_ack_num==this.seq_No+1){

						this.seq_No=packet_ack_num;
						this.ack_No=packet_seq_num+1;
						seqNoString = Integer.toString(this.seq_No);
						ackNoString = Integer.toString(this.ack_No);
						message_for_send="ACK"+" "+seqNoString+" "+ackNoString;
						sendData =message_for_send.getBytes();
						System.out.println("second message: " + message_for_send);
						sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
						this.socket.send(sendPacket);
						
						state="ESTABLISHED";
						// System.out.println("here here");
						// System.out.println("state" + state);
						//send ACK packet to server

					}
					if(state.equals("ESTABLISHED")){
						System.out.println("ESTABLISHED Connection");
							/*if(packet_seq_num >  this.seq_No)
							{
								int packet_ack;
								packet_ack=packet_seq_num + 1;
								ackNoString = Integer.toString(packet_ack);
								String ack_message="ACK"+" "+seqNoString+" "+ackNoString;
								System.out.println("ack message" + ack_message);
								sendData =ack_message.getBytes();
								sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
								this.socket.send(sendPacket);
								
							}*/
						break;
					}
				}	
			}
		}
		catch (Exception ex){
			System.out.println("Exception!");
			ex.printStackTrace();
			System.out.println(ex);
		}	
	}

	@Override
	public void send(String pathToFile) throws Exception {
		handShaking();
		System.out.println("Finished Handshaking!");
		//check kardane inke next sequence number kamtar as window size basheh age kamtar nabashe sabr kone
		//age timeout gozasht bere oni ke timeout esh gozashte ro dobare befreste va timer esh ro start bezane
		//age seq.no moshkeli nadasht  send kone , next seq.no ro handel kone 
		ArrayList<String> fileContent = readFile(pathToFile);
		InetAddress ip_adress = InetAddress.getByName(this.ip);
		byte[] sendData = new byte[1024];
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
		//Timer timer = new Timer();
		
		int dup_ack_packet_num = -1; 
		int dup_ack_times = 0;

		while(next_seq_No< fileContent.size()){
			if(next_seq_No <= cwnd){
				sendData =fileContent.get(next_seq_No).getBytes();
				System.out.println("File Part To Send: "+fileContent.get(next_seq_No));
				sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
				// System.out.println("waiting");
				// TimeUnit.SECONDS.sleep(20);
				socket.send(sendPacket);
				System.out.println("Sent One byte of Data to port: "+port);
				next_seq_No +=1;
				
				Thread t = timeoutPacket(next_seq_No-1, ip, fileContent, 10, socket,port);
				t.start();
				try{
					t.join();
					getWindowSize();
					// t.stop();
				}catch(InterruptedException ie){}
			}
			if(dup_ack_times == 3 && dup_ack_packet_num!=-1){
				sendData = fileContent.get(dup_ack_packet_num).getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
				socket.send(sendPacket);
				dup_ack_packet_num = -1;
				dup_ack_times = 0;
				slowStart = 1;
				ssthreshold = getSSThreshold();
			}
		}
	}
	@Override
	public void receive(String pathToFile) throws Exception {
		System.out.println("Started Receive");
		InetAddress ip_adress = InetAddress.getByName(this.ip);
		String Data ="";
		byte[] sendData = new byte[1024];
		String seqNoString = Integer.toString(this.seq_No);
		String ackNoString = Integer.toString(this.ack_No);
		// String message_for_send="SYN"+" "+seqNoString+" "+ackNoString;
		// sendData =message_for_send.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
		// this.socket.send(sendPacket);
		// String  state = "SYN-SENT";
		ArrayList<String> fileContent = new ArrayList<String>();
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		// this.socket.receive(receivePacket);
		// String sentence = new String(receivePacket.getData());
		// String[] splited = sentence.split("\\s+");
		// int packet_ack_num=Integer.parseInt(splited[2]);
		// int packet_seq_num=Integer.parseInt(splited[1]);
		String sentence = new String();
		String[] splited;
		int packet_ack_num;
		int packet_seq_num;

		while(true){
				//byte[] receiveData = new byte[1024];
				//DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				int packet_ack=-1;
				System.out.println("In loop, port: "+port);
				this.socket.receive(receivePacket);
				System.out.println("Got One byte of Data");
			    sentence = new String(receivePacket.getData());
				String message=sentence.split("\\s+")[0];
				String ack_number=sentence.split("\\s+")[1];
				String seq_number=sentence.split("\\s+")[2];
				packet_ack_num=Integer.parseInt(ack_number);
				packet_seq_num=Integer.parseInt(seq_number);
				int flag=-1;
				seqNoString = Integer.toString(packet_seq_num);
				fileContent.add(packet_seq_num-1, sentence);

				for(int i=0;i < packet_seq_num-1;i++){
					
						if(fileContent.get(i).getBytes().equals("")){
							flag=i;
						}
							
				}
				if(flag!=-1){
					packet_ack=flag;
					ackNoString = Integer.toString(packet_ack);
					String ack_message="ACK"+" "+seqNoString+" "+ackNoString;
					sendData =ack_message.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
					this.socket.send(sendPacket);
				}
				if (flag==-1 && packet_seq_num > this.seq_No){
					packet_ack=packet_seq_num + 1;
					ackNoString = Integer.toString(packet_ack);
					String ack_message="ACK"+" "+seqNoString+" "+ackNoString;
					sendData =ack_message.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
					this.socket.send(sendPacket);
					System.out.println("Sent Ack");
				}
			// while(true){
			// 	//
			// 	byte[] receiveData = new byte[1024];
			// 	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// 	this.socket.receive(receivePacket);
			// 	String sentence = new String(receivePacket.getData());
			// 	//check ye seri shode ya na

			// }

			
			
		   // System.out.println("RECEIVED: " + sentence);
		   //InetAddress IPAddress = receivePacket.getAddress();
			//port = receivePacket.getPort();
	
		}
		// for (int i=0;i < fileContent.size();i++){
		// 	Data +=fileContent.get(i).getBytes();
		// }
		
		//check konim ack az samt moghabel amade ya mohtava 
		//age did ack hast check kone duplicate ack hast ya na
		//age duplicate ack hast sevomi hast?fast retransmit tanzim window size va threshold
		//age duplicate ack nist va ack hast window size ra yek vahed ezafeh konim
		//age ack nabood file ra benevisim

		//throw new RuntimeException("Not implemented!");
	}

	@Override
	public void close() throws Exception {
		this.socket.close();
		//throw new RuntimeException("Not implemented!");
	}

	@Override
	public long getSSThreshold() {
		//ehtemalan on chizi ke to recv goftim ke ino handel kone bayad inja anjam beshe va meghdaresh ro return kone
		if (slowStart==1){
			ssthreshold = ssthreshold/2;
			cwnd = 1;
			slowStart = 0;
		}
		return ssthreshold;
		//throw new RuntimeException("Not implemented!");
	}

	@Override
	public long getWindowSize() {
		//ehtemalan on chizi ke to recv goftim ke ino handel kone bayad inja anjam beshe va meghdaresh ro return kone
		if(cwnd < ssthreshold){
			cwnd *= 2; 
		}
		else{
			cwnd += 1;
		}
		return cwnd;
	}


	public static ArrayList<String> readFile(String filename){
		ArrayList<String> fileContent = new ArrayList<String>();
		try{
			File file = new File(filename);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st; 
			while ((st=br.readLine()) != null){
				fileContent.add(st);
			}
			return fileContent;
		}
		catch(Exception e){
			System.out.println("An Error in reading the file!\n");
			System.out.println(e);
		} 
		return fileContent;
	}
		

}