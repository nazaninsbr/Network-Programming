import socket
 
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
 
host = 'localhost';
port = 5000;
 
while(1) :
	msg = input('Enter message to send : ')
	try :
		s.sendto(msg.encode(), (host, port))
	except socket.error:
		print('Error Code : ' + str(msg[0]) + ' Message ' + msg[1])
		break