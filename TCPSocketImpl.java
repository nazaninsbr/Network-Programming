import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
    
    public EnhancedDatagramSocket socket;

    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        socket= new EnhancedDatagramSocket(port);
        
    }

    @Override
    public void send(String pathToFile) throws Exception {


        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        this.socket.close()
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getSSThreshold() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        throw new RuntimeException("Not implemented!");
    }
}
