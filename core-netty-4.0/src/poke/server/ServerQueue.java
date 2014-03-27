package poke.server;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eye.Comm.Request;

public class ServerQueue {
	protected static Logger logger = LoggerFactory.getLogger("server");
	
	protected static LinkedBlockingDeque<ServerQueueEntry> inbound = new LinkedBlockingDeque<ServerQueueEntry>();
	protected static LinkedBlockingDeque<ServerQueueEntry> outbound = new LinkedBlockingDeque<ServerQueueEntry>();

	private static OutboundServerWorker oServerWorker;
	private static InboundServerWorker iServerWorker;
	
	// not the best method to ensure uniqueness
		private static ThreadGroup tgroup = new ThreadGroup("ServerQueue-"
				+ System.nanoTime());

		public static void startup() {
			logger.info("***in server queue startup***");
			if (iServerWorker != null)
				return;

			iServerWorker = new InboundServerWorker(tgroup, 1);
			iServerWorker.start();
			oServerWorker = new OutboundServerWorker(tgroup, 1);
			oServerWorker.start();
		}

	
	public static void enqueueRequest(Request req, Channel ch,
			SocketAddress sa) {
		logger.info("Enqueuing the message from the client");
		try {
			ServerQueueEntry entry = new ServerQueueEntry(req, ch, sa);
			inbound.put(entry);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for processing", e);
		}
	}

	public static void enqueueResponse(Request req, Channel ch) {
		try {
			ServerQueueEntry entry = new ServerQueueEntry(req, ch,null);
			outbound.put(entry);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for reply", e);
		}
	}
	
	
	public static class ServerQueueEntry {
		public ServerQueueEntry(Request req, Channel ch, SocketAddress sa) {
			this.req = req;
			this.channel = ch;
			this.sa = sa;
		}

		public Request req;
		public Channel channel;
		SocketAddress sa;
	}
}
