from __future__ import absolute_import
import comm_pb2
import sys
import google

import socket

from comm_pb2 import Request
vendor_dir = os.path.join(os.path.dirname(__file__), 'vendor')
google.__path__.append(os.path.join(vendor_dir, 'google'))
sys.path.insert(0, vendor_dir)

class TestClient():
  def __init__(self,Request, host = 'localhost', port = 5570):
    self.addr = (host, port)
    self.__call(Request)

  def __call(self, Request):
    header  = Request.header()
    payload = Request.body();

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    sock.connect(self.addr)
    sock.send(header)
    sock.send(payload)

    size_t = sock.recv(4)
    size   = struct.unpack('!I', size_t)
    data   = sock.recv(size[0])

    result = Result()
    result.ParseFromString(data)

    if result.exception != Result.NONE:
      raise Exception(result.message)

    print str(result.sucka)

if __name__ == '__main__':
  import sys, os
  sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

  from comm import Request



