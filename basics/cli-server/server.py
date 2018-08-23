import socket               
 
s = socket.socket()         
port = 12345               
s.bind(('', port))        
s.listen(5)     
 

while True:
   c, addr = s.accept()     
   print('connection from', addr)
   data = 'Thank you for connecting'
   c.send(data.encode())
   c.close()