import socket
 
try:
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
except socket.error as err:
	print("socket creation failed with error ", err)
 
port = 80
 
try:
	host_ip = socket.gethostbyname('www.google.com')
except socket.gaierror:
	print("there was an error resolving the host")
	exit()
 
s.connect((host_ip, port))
print("connected to ", host_ip)