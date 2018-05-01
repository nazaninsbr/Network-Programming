import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TCPSocketImpl extends TCPSocket {
	
	private EnhancedDatagramSocket socket;
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
		socket= new EnhancedDatagramSocket(port);
		this.seq_No = 0;
		this.ack_No = 0;
		this.ip=ip;
		this.port=port;
		this.next_seq_No = 0;
		slowStart = 1;
		cwnd = 1;
	}

	@Override
	public void send(String pathToFile) throws Exception {
		//check kardane inke next sequence number kamtar as window size basheh age kamtar nabashe sabr kone
		//age timeout gozasht bere oni ke timeout esh gozashte ro dobare befreste va timer esh ro start bezane
		//age seq.no moshkeli nadasht  send kone , next seq.no ro handel kone 
		ArrayList<String> fileContent = readFile(pathToFile);
		InetAddress ip_adress = InetAddress.getByName(this.ip);
		byte[] sendData = new byte[1024];
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
		//Timer timer = new Timer();
		ExecutorService service = Executors.newSingleThreadExecutor();
		while(next_seq_No< fileContent.size()){
			try {
				Runnable r = new Runnable() {
					@Override
					public void run() {
					}
				};
				if(next_seq_No <= cwnd){
					sendData =fileContent.get(next_seq_No).getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
					socket.send(sendPacket);
					next_seq_No +=1;
				}
				Future<?> f = service.submit(r);
				f.get(2, TimeUnit.MINUTES);

			}
			catch (TimeoutException e) {
				sendData = fileContent.get(seq_No).getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
				this.socket.send(sendPacket);
			}
			catch(Exception ex){
				System.out.println("An Error!!\n");
				System.out.println(ex);
			}
		}
	}
	@Override
	public void receive(String pathToFile) throws Exception {

		InetAddress ip_adress = InetAddress.getByName(this.ip);
		String Data ="";
		byte[] sendData = new byte[1024];
		String seqNoString = Integer.toString(this.seq_No);
		String ackNoString = Integer.toString(this.ack_No);
		String message_for_send="SYN"+" "+seqNoString+" "+ackNoString;
		sendData =message_for_send.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
		this.socket.send(sendPacket);
		String  state = "SYN-SENT";
		ArrayList<String> fileContent = new ArrayList<String>();

	   

		while(true)
		{
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			this.socket.receive(receivePacket);
			String sentence = new String(receivePacket.getData());
			String[] splited = sentence.split("\\s+");
			int packet_ack_num=Integer.parseInt(splited[2]);
			int packet_seq_num=Integer.parseInt(splited[1]);
			//InetAddress IPAddress = receivePacket.getAddress();
			port = receivePacket.getPort();

			if(state=="SYN-SENT"){
				if(packet_ack_num==this.seq_No+1){

					this.seq_No=packet_ack_num;
					this.ack_No=packet_seq_num+1;
					message_for_send="ACK"+" "+seqNoString+" "+ackNoString;
					sendData =message_for_send.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
					this.socket.send(sendPacket);
					state="ESTABLISHED";
					//send ACK packet to server

				}
				else if(state=="ESTABLISHED"){ //receive  data

					if(packet_seq_num >  this.seq_No)
					{
						int packet_ack;
						packet_ack=packet_seq_num + 1;
						ackNoString = Integer.toString(packet_ack);
						String ack_message="ACK"+" "+seqNoString+" "+ackNoString;
						sendData =ack_message.getBytes();
						sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
						this.socket.send(sendPacket);
						break;
					}
				}
			}

			while(true){
				
				//byte[] receiveData = new byte[1024];
				//DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				int packet_ack=-1;
				this.socket.receive(receivePacket);
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
				if(flag!=-1)
					packet_ack=flag;
					ackNoString = Integer.toString(packet_ack);
					String ack_message="ACK"+" "+seqNoString+" "+ackNoString;
					sendData =ack_message.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
					this.socket.send(sendPacket);
			
				if (flag==-1 && packet_seq_num > this.seq_No){

					packet_ack=packet_seq_num + 1;
					ackNoString = Integer.toString(packet_ack);
					ack_message="ACK"+" "+seqNoString+" "+ackNoString;
					sendData =ack_message.getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length,ip_adress, port);
					this.socket.send(sendPacket);
				

				for (int i=0;i < fileContent.size();i++)
				{
					Data +=fileContent.get(i).getBytes();
				}



				}
				//check ye seri shode ya na
				
			}
			
			
		   // System.out.println("RECEIVED: " + sentence);
		   //InetAddress IPAddress = receivePacket.getAddress();
			//port = receivePacket.getPort();
	
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
		}
		return cwnd;
		//throw new RuntimeException("Not implemented!");
	}

	@Override
	public long getWindowSize() {
		//ehtemalan on chizi ke to recv goftim ke ino handel kone bayad inja anjam beshe va meghdaresh ro return kone
		return 1;
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

