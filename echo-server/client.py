import socket               
 
s = socket.socket()         
port = 12345               
s.connect(('127.0.0.1', port))
data = 'Message ....'
s.send(data.encode())
s.close()       