package poke.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.ServerQueue.ServerQueueEntry;
import eye.Comm.Management;
import eye.Comm.Request;

public class InboundServerWorker extends Thread {
	
	protected static Logger logger = LoggerFactory.getLogger("server");
	int workerId;
	boolean forever = true;
	public InboundServerWorker(ThreadGroup tgrp, int workerId) {
		super(tgrp, "inbound-Server-" + workerId);
		this.workerId = workerId;

		if (ServerQueue.outbound == null)
			throw new RuntimeException("connection worker detected null queue");
	}
	@Override
	public void run() {
		logger.info("inbound server worker queue");
		while (true) {
			logger.info("inbound server worker queue in while=="+ServerQueue.inbound.size());
			if (!forever && ServerQueue.inbound.size() == 0)
			break;
			
			
			try {
				logger.info("inbound server worker queue in tryyy");
				
				ServerQueueEntry msg = ServerQueue.inbound.take();
				logger.info("inbound server worker queue msggg=="+msg);
				if (logger.isDebugEnabled())
					logger.debug("Inbound server message received");

				Request req = (Request) msg.req;
				logger.info("inbound server worker queue req=="+req);
				

			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected processing failure", e);
				break;
			}
		}
		

		if (!forever) {
			logger.info("connection queue closing");
		}
	}
}