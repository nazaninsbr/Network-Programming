import java.util.Random;
import java.io.*;
import java.net.*;

public class TCPSocketImpl extends TCPSocket {
    
    private EnhancedDatagramSocket socket;
    private int slowStart; 
    private long ssthreshold;
    private int seq_No; 
    private int ack_No;
   // private int port;
   // private String ip;
    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        socket= new EnhancedDatagramSocket(port);
        this.seq_No = 0;
        this.ack_No = 0;
        //this.ip=ip;
       // this.port=port;
        slowStart = 1;
        
    }

    @Override
    public void send(String pathToFile) throws Exception {
        //check kardane inke next sequence number kamtar as window size basheh age kamtar nabashe sabr kone
        //age timeout gozasht bere oni ke timeout esh gozashte ro dobare befreste va timer esh ro start bezane
        //age seq.no moshkeli nadasht  send kone , next seq.no ro handel kone 


    }
    @Override
    public void receive(String pathToFile) throws Exception {

        byte[] sendData = new byte[1024];
        String seqNoString = Integer.toString(this.seq_No);
        String ackNoString = Integer.toString(this.ack_No);
        String message_for_send="SYN"+" "+seqNoString+" "+ackNoString;
        sendData =message_for_send.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip, port);
        this.socket.send(sendPacket);
        String  state = "SYN-SENT";
       

        while(true)
        {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            String[] splited = sentence.split("\\s+");
            int packet_ack_num=Integer.parseInt(splited[2]);
            int packet_seq_num=Integer.parseInt(splited[1]);
            InetAddress IPAddress = receivePacket.getAddress();
            port = receivePacket.getPort();

            if(state=="SYN-SENT"){
                if(packet_ack_num==this.seq_No+1){

                    this.seq_No=packet_ack_num;
                    this.ack_No=packet_seq_num+1;
                    massage_for_send="ACK"+" "+seqNoString+" "+ackNoString;
                    sendData =message_for_send.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
                    this.socket.send(sendPacket);
                    state="ESTABLISHED";
                    //send ACK packet to server

                }
                else if(state="ESTABLISHED"){ //receive  data

                    if(packet_seq_num >  this.seq_No)
                    {
                        packet_ack=packet_seq_num + 1;
                        ack_message="ACK"+" "+seqNoString;
                        sendData =ack_messages.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
                        this.socket.send(sendPacket);
                    }





                }
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
        if (slowStart==1){
            ssthreshold = ssthreshold/2;
        }
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        //ehtemalan on chizi ke to recv goftim ke ino handel kone bayad inja anjam beshe va meghdaresh ro return kone
        throw new RuntimeException("Not implemented!");
    }
    // public static void readFile(String filename){
    //     try{
    //         File file = new File(filename);
    //         BufferedReader br = new BufferedReader(new FileReader(file));
    //         String st; 
    //         while ((st=br.readLine()) != null){
                
    //         }
    //     }
    //     catch(Exception e){
    //         System.out.println("An Error in reading the file!\n");
    //         System.out.println(e);
    //     } 
    // }
        

}

