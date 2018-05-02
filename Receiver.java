import java.io.IOException;
import java.net.DatagramPacket;

public class Receiver {
    public static void main(String[] args) throws Exception {
    	// System.out.println("Here");
        TCPServerSocket tcpServerSocket = new TCPServerSocketImpl(12345);
        // System.out.println("here here");
        TCPSocket tcpSocket = tcpServerSocket.accept();
        tcpSocket.receive("receiving.mp3");
        tcpSocket.close();
        tcpServerSocket.close();
    }
}
