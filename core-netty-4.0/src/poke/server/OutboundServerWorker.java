package poke.server;

import io.netty.channel.ChannelFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.ServerQueue.ServerQueueEntry;
import poke.server.management.ManagementQueue;
import poke.server.management.ManagementQueue.ManagementQueueEntry;

public class OutboundServerWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("server");
	
	int workerId;
	boolean forever = true;
	public OutboundServerWorker(ThreadGroup tgrp, int workerId) {
		super(tgrp, "outbound-mgmt-" + workerId);
		this.workerId = workerId;

		if (ServerQueue.outbound == null)
			throw new RuntimeException("Server worker detected null queue");
	}

	@Override
	public void run() {
		while (true) {
			if (!forever && ServerQueue.outbound.size() == 0)
				break;

			try {
				// block until a message is enqueued
			 ServerQueueEntry msg = ServerQueue.outbound.take();

				if (logger.isDebugEnabled())
					logger.debug("Outbound server message received");

				if (msg.channel.isWritable()) {
					boolean rtn = false;
					if (msg.channel != null && msg.channel.isOpen() && msg.channel.isWritable()) {
						ChannelFuture cf = msg.channel.write(msg);

						// blocks on write - use listener to be async
						cf.awaitUninterruptibly();
						rtn = cf.isSuccess();
						if (!rtn)
							ServerQueue.outbound.putFirst(msg);
					}

				} else
					ServerQueue.outbound.putFirst(msg);
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected server communcation failure", e);
				break;
			}
		}

		if (!forever) {
			logger.info("server outbound queue closing");
		}
	}

}
