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
	private int send_port;
	private int next_seq_No;
	private String ip;
	private int start_of_window;
	private int excepted_seq_No;
	public TCPSocketImpl(String ip, int port) throws Exception {
		super(ip, port);
		// System.out.println("AAAAAAAAAAAAAAAA");
		//this.port=port;
		// this.socket= new EnhancedDatagramSocket(port);
		this.port=port;
		this.ip=ip;
		this.socket= new EnhancedDatagramSocket(this.port);
		this.start_of_window = 0;
		this.send_port = 12345;

		
		 // System.out.println("OOOOOOOOOOOOOOOO");
		this.seq_No = 0;
		this.ack_No = 0;
		this.excepted_seq_No = 0;
		
		this.next_seq_No = 0;
		slowStart = 0;
		cwnd = 1;
		//handShaking();
		
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
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, send_port);
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
						sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, send_port);
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
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		int dup_ack_packet_num = -1; 
		int times_sent = 0;
		int dup_ack_times = 0;
		String seqNoString;
		String sendDataString; 
		while(next_seq_No< fileContent.size()){
			if(next_seq_No-start_of_window <= cwnd && (next_seq_No!=start_of_window || times_sent==0)){
				sendDataString = Integer.toString(next_seq_No) +" "+ fileContent.get(next_seq_No);
				sendData = sendDataString.getBytes();
				System.out.println("File Part To Send: "+sendDataString);
				sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, 3456);
				// System.out.println("waiting");
				// TimeUnit.SECONDS.sleep(20);
				socket.send(sendPacket);
				System.out.println("Sent One byte of Data to port: "+3456);
				next_seq_No +=1;
				
				// Thread t = timeoutPacket(next_seq_No-1, ip, fileContent, 10, socket,3456);
				// t.start();
				// try{
				// 	t.join();
				// 	getWindowSize();
				// 	// t.stop();
				// }catch(InterruptedException ie){}
				try{
					socket.setSoTimeout(2000);
					System.out.println("Waiting for ACK");
					socket.receive(receivePacket);
					String sentence = new String(receivePacket.getData());
					String message =sentence.split("\\s+")[0];
					String ack_number =sentence.split("\\s+")[1];
					String seq_number =sentence.split("\\s+")[2];
					int packet_ack_num =Integer.parseInt(ack_number);
					int packet_seq_num =Integer.parseInt(seq_number);
					if(packet_ack_num-1==start_of_window){
						throw new Exception("my.own.Exception");
					}
				}
				catch(IOException e){
					System.out.println("Timeout");
					int index = start_of_window;
					while(index<=next_seq_No){
						sendDataString = Integer.toString(index) +" "+ fileContent.get(index);
						sendData = sendDataString.getBytes();
						System.out.println("File Part To Send: "+fileContent.get(next_seq_No));
						sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, 3456);
					}
				}
				catch(Exception st){
					start_of_window +=1;
					times_sent = 0;
					getWindowSize();
				}
			}
			if(dup_ack_times == 3 && dup_ack_packet_num!=-1){
				sendDataString = Integer.toString(dup_ack_packet_num) +" "+ fileContent.get(next_seq_No);
				sendData = sendDataString.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, 3456);
				socket.send(sendPacket);
				dup_ack_packet_num = -1;
				dup_ack_times = 0;
				slowStart = 1;
				ssthreshold = getSSThreshold();
			}
		}
		sendDataString = Integer.toString(-100) +" "+ fileContent.get(-100);
		sendData = sendDataString.getBytes();
		System.out.println("File Part To Send: "+sendDataString);
		sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, 3456);
		socket.send(sendPacket);

	}
	@Override
	public void receive(String pathToFile) throws Exception {
		int this_send_port = 12346;
		System.out.println("Started Receive");
		//InetAddress ip_adress = InetAddress.getByName(this.ip);
		InetAddress ip_adress = InetAddress.getLocalHost();
		String Data ="";
		byte[] sendData = new byte[1024];
		String seqNoString = Integer.toString(this.seq_No);
		String ackNoString = Integer.toString(this.ack_No);
		// String message_for_send="SYN"+" "+seqNoString+" "+ackNoString;
		// sendData =message_for_send.getBytes();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, this_send_port);

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

				System.out.println("In loop, port: "+this.port);
				

				System.out.println("In loop, port: "+port);
				System.out.println("Sending packets to, port: "+this_send_port);
				this.socket.receive(receivePacket);

				System.out.println("Got One byte of Data");
				sentence = new String(receivePacket.getData());
				System.out.println("Got message:" + sentence);

				splited = sentence.split("\\s+");
				packet_seq_num = Integer.parseInt(splited[0]);
				//splited[2] = splited[2].replace("\n", "").replace("\r", "").replace(" ", "");
				//packet_ack_num = Integer.parseInt(splited[2].trim());
				System.out.println("packet_seq_num" + packet_seq_num);
				//seqNoString = Integer.toString(packet_seq_num);
				if(packet_seq_num==-100){
					break;
				}
				if( packet_seq_num == this.excepted_seq_No)
				{
					
					packet_ack = this.excepted_seq_No;
					ackNoString = Integer.toString(packet_ack);
					String ack_message="ACK"+" "+ackNoString;
					sendData =ack_message.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, this_send_port);
					this.socket.send(sendPacket);
					System.out.println("Sent Ack");
					fileContent.add(splited[1]);
					this.excepted_seq_No += 1;

				}
				else
				{
					packet_ack = this.excepted_seq_No;
					ackNoString = Integer.toString(packet_ack);
					String ack_message="ACK"+" "+ackNoString;
					sendData =ack_message.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, this_send_port);
					this.socket.send(sendPacket);
					System.out.println("Sent Ack");
				}




				//for(int i=0;i < packet_seq_num-1;i++){
					
						//if(fileContent.get(i).getBytes().equals("")){
							//flag=i;
						//}
							
				//}
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
		 for (int i=0;i < fileContent.size();i++){
			writeToFile( pathToFile,fileContent.get(i));
		// 	Data +=fileContent.get(i).getBytes();
		 }
		
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
				// System.out.println(st);
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
	public static void writeToFile(String filename, String str){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			writer.append(str);
			writer.close();
		}
		catch (Exception e){
			System.out.println("An Error writing to file!\n");
			System.out.println(e);
		}
	}
		

}