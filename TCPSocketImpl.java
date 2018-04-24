import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
    
    public EnhancedDatagramSocket socket;

    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        socket= new EnhancedDatagramSocket(port);
        
    }

    @Override
    public void send(String pathToFile) throws Exception {
        //check kardane inke next sequence number kamtar as window size basheh age kamtar nabashe sabr kone
        //age timeout gozasht bere oni ke timeout esh gozashte ro dobare befreste va timer esh ro start bezane
        //age seq.no moshkeli nadasht  send kone , next seq.no ro handel kone 
        readFile()


    }
    @Override
    public void receive(String pathToFile) throws Exception {
        //check konim ack az samt moghabel amade ya mohtava 
        //age did ack hast check kone duplicate ack hast ya na
        //age duplicate ack hast sevomi hast?fast retransmit tanzim window size va threshold
        //age duplicate ack nist va ack hast window size ra yek vahed ezafeh konim
        //age ack nabood file ra benevisim

        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        this.socket.close();
        //throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getSSThreshold() {
        //ehtemalan on chizi ke to recv goftim ke ino handel kone bayad inja anjam beshe va meghdaresh ro return kone
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        //ehtemalan on chizi ke to recv goftim ke ino handel kone bayad inja anjam beshe va meghdaresh ro return kone
        throw new RuntimeException("Not implemented!");
    }
    public static void readFile(String filename){
        try{
            File file = new File(filename);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st; 
            while ((st=br.readLine()) != null){
                
            }
        }
        catch(Exception e){
            System.out.println("An Error in reading the file!\n");
            System.out.println(e);
        } 
    }
        

}

