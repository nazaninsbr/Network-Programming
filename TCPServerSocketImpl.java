import java.io.*;
import java.net.*;

public class TCPServerSocketImpl extends TCPServerSocket {
    private int someOneIsConnected; 
    public EnhancedDatagramSocket socket;
    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.someOneIsConnected = 0;
        this.socket = new EnhancedDatagramSocket(port);
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
            this.seq_No +=1;
            String sentence = new String(receivePacket1.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket1.getAddress();
            port = receivePacket1.getPort();
            //**************//
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            this.socket.send(sendPacket);
            DatagramPacket receivePacket2 = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket2);
            this.seq_No +=1;
            someOneIsConnected = 0;
        }
        EnhancedDatagramSocket tcp_server_socket = new EnhancedDatagramSocket(port);
    }

    @Override
    public void close() throws Exception {
        // throw new RuntimeException("Not implemented!");
        this.socket.close();
    }
}
