package poke.client.util;
//Anita
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.ServerInitializer;

public class ClientListener extends Thread{
	
	protected static Logger logger = LoggerFactory.getLogger("server");
	public void run()
	{
		logger.info("$$$$$Client Listener$$$$$");
		String str="8080";
		int port = Integer.parseInt(str);
		
		ServerBootstrap b = new ServerBootstrap();
		logger.info("$$$$$Client Listener ServerBootstrap$$$$$");
		b.channel(NioServerSocketChannel.class);
		b.option(ChannelOption.SO_BACKLOG, 100);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		boolean compressComm = false;
		b.childHandler(new ServerInitializer(compressComm));
		logger.info("$$$$$Client Listener Start server$$$$$");
		// Start the server.
		//logger.info("Starting server " + conf.getServer().getProperty("node.id") + ", listening on port = "+ port);
		ChannelFuture f = b.bind(port).syncUninterruptibly();

		// should use a future channel listener to do this step
		// allChannels.add(f.channel());
		logger.info("Channel Created");
		// block until the server socket is closed.
		try {
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
