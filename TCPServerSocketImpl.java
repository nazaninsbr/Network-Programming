import java.io.*;
import java.net.*;

public class TCPServerSocketImpl extends TCPServerSocket {
    private int someOneIsConnected; 
    public EnhancedDatagramSocket socket;
    private int seq_No; 
    private int ack_No;
    private int port;
    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.someOneIsConnected = 0;
         this.port=port;
        this.socket= new EnhancedDatagramSocket(this.port);



        this.seq_No = 0;
        this.ack_No = 0;
    }

    @Override
    public TCPSocket accept() throws Exception {
        // throw new RuntimeException("Not implemented!");
        //needs to have handshaking
        int port;
        String state="";
        //String state = new String("");
        System.out.println("Listening");
        while(this.someOneIsConnected==0){
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket);
            //this.seq_No +=1;
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            System.out.println("ip getAddress" + IPAddress);
            port = receivePacket.getPort();

            String[] splited = sentence.split("\\s+");
            
           
           

            int packet_seq_No = Integer.parseInt(splited[1]);
            splited[2] = splited[2].replace("\n", "").replace("\r", "").replace(" ", "");
            int packet_ack_No = Integer.parseInt(splited[2].trim());

            if(state.equals("")){
                
                if(splited[0].equals("SYN")){
                    // System.out.println("here here here");
                    this.ack_No=packet_seq_No+1;
                    // System.out.println("ACK No:" + this.ack_No);
                    String ackNoString = Integer.toString(this.ack_No);
                    System.out.println("ACK No String:" + ackNoString);
                    String seqNoString = Integer.toString(this.seq_No);
                    System.out.println("seq No String" + seqNoString);
                    String sentence_for_send = "SYN-ACK" + " "+seqNoString+" "+ackNoString;
                    System.out.println("sentence: " + sentence_for_send);
                    
                    sendData = sentence_for_send.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    this.socket.send(sendPacket);
                    state="SYN-RECEVED";
                }
            }
            if(state.equals("SYN-RECEVED")){
                
                if(splited[0].equals("ACK") && packet_ack_No == (this.seq_No)+1){
                    state="ESTABLISHED";
                    someOneIsConnected = 1;
                    System.out.println("ESTABLISHED");
                }
            }
        
            
        }
        
        // find a way to change the IP
        TCPSocketImpl tcp_server_socket = new TCPSocketImpl("127.0.0.1", this.port);
        return tcp_server_socket;
    }

    @Override
    public void close() throws Exception {
        // throw new RuntimeException("Not implemented!");
        this.socket.close();
    }
}
