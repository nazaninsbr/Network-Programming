import socket 

mysocket = socket.socket()
mysocket.bind(("127.0.0.1", 4444))
mysocket.listen(5)

while True:
	(client, (ip, port)) = mysocket.accept()
	print('connected ', ip)