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
            port = receivePacket.getPort();

            String[] splited = sentence.split("\\s+");
            
           
           

            int packet_seq_No = Integer.parseInt(splited[1]);
            splited[2] = splited[2].replace("\n", "").replace("\r", "").replace(" ", "");
            int packet_ack_No = Integer.parseInt(splited[2].trim());

            if(state==""){
                if(splited[0]=="SYN"){
                    this.ack_No=packet_seq_No+1;
                    String ackNoString = Integer.toString(this.ack_No);
                    String seqNoString = Integer.toString(this.seq_No);
                    String sentence_for_send = "SYN-ACK" + " "+seqNoString+" "+ackNoString;
                    
                    sendData = sentence_for_send.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    this.socket.send(sendPacket);
                    state="SYN-RECEVED";
                }
            }
            else if(state=="SYN-RECEVED"){
                if(splited[0]=="ACK" && packet_ack_No == (this.seq_No)+1){
                    state="ESTABLISHED";
                    someOneIsConnected = 1;
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
