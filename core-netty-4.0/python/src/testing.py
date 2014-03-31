import testMessage2_pb2
import sys
import socket,pickle
import struct

message=testMessage2_pb2.UserLogin()
message.clientName= "1"
message.request_type="2"
print(message)
sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server_address=('localhost',5570)
print('connecting to %s port %s' %server_address)
sock.connect(server_address)
print 'sending message'
s=message.SerializeToString()
"""totallen=4+len(s)
pack1=struct.pack('>I',totallen)
print(pack1+s)
sock.sendall(pack1+s)
#sock.sendall(str(message).encode('utf-8'))
#sock.sendall(message.SerializeToString())"""
print(s)
sock.send(s)
sock.flush()
print('closing socket')
sock.close()

