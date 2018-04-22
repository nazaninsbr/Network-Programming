public abstract class TCPServerSocket {
	public TCPServerSocket(int port) throws Exception {
		 EnhancedDatagramSocket socket= new EnhancedDatagramSocket(port);
	}

	public abstract TCPSocket accept() throws Exception{
		//needs to have handshaking
		int notConnected = 1;
		int port;
		while(notConnected==1){
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			this.socket.receive(receivePacket);
			String sentence = new String( receivePacket.getData());
			System.out.println("RECEIVED: " + sentence);
			InetAddress IPAddress = receivePacket.getAddress();
			port = receivePacket.getPort();
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			this.socket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(sentence, sentence.length);
			this.socket.receive(receivePacket);
			notConnected = 0;
		}
		ServerSocket tcp_socket = new ServerSocket(port);
	}

	public abstract void close() throws Exception;
}