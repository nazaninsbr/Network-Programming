import java.io.*;
import java.net.*;

public class TCPServerSocketImpl extends TCPServerSocket {
    private int someOneIsConnected; 
    public EnhancedDatagramSocket socket;
    private int seq_No; 
    private int ack_No;
    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.someOneIsConnected = 0;
        this.socket = new EnhancedDatagramSocket(port);
        this.seq_No = 0;
        this.ack_No = 0;
    }

    @Override
    public TCPSocket accept() throws Exception {
        // throw new RuntimeException("Not implemented!");
        //needs to have handshaking
        int port;
        while(this.someOneIsConnected==0){
            System.out.println("Listening");
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            DatagramPacket receivePacket1 = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket1);
            //this.seq_No +=1;
            String sentence = new String(receivePacket1.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket1.getAddress();
            port = receivePacket1.getPort();
            if(sentence=="SYNC"){
                this.seq_No +=1;
                String ackNoString = Integer.toString(seq_No);
                sentence= ackNoString + sentence;
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                this.socket.send(sendPacket);
            }
            DatagramPacket receivePacket2 = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket2);
            someOneIsConnected = 0;
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
