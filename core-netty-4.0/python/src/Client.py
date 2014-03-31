import testMessage2_pb2
import sys

import socket


testMessage2_pb2.clientName= "1"
print(testMessage2_pb2.clientName)
sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server_address=('localhost',5570)
print('connecting to %s port %s' %server_address)
sock.connect(server_address)

try:
   	print('sending message')
    sock.send(testMessage2_pb2.clientName.SerializeToString())
    sock.send(testMessage2_pb2.clientName)
	print('message is sent')
    amount_received =0;
    amount_expected =len(message)
    print('amount expected %s',amount_expected)

 

finally:
    print('closing socket')
    sock.close()