/*
 * copyright 2014, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.server.management.managers;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

import eye.Comm.LeaderElection;
import eye.Comm.Management;
import eye.Comm.LeaderElection.VoteAction;

/**
 * The election manager is used to determine leadership within the network.
 * 
 * @author gash
 * 
 */
public class ElectionManager extends Thread{
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<ElectionManager> instance = new AtomicReference<ElectionManager>();
	//ConcurrentHashMap<Channel, HeartbeatData> outgoingHB = new ConcurrentHashMap<Channel, HeartbeatData>();
	//ConcurrentHashMap<String, HeartbeatData> incomingHB = new ConcurrentHashMap<String, HeartbeatData>();

	private String nodeId;

	/** @brief the number of votes this server can cast */
	private int votes = 1;

	public static ElectionManager getInstance(String id, int votes) {
		instance.compareAndSet(null, new ElectionManager(id, votes));
		return instance.get();
	}

	public static ElectionManager getInstance() {
		return instance.get();
	}

	/**
	 * initialize the manager for this server
	 * 
	 * @param nodeId
	 *            The server's (this) ID
	 */
	protected ElectionManager(String nodeId, int votes) {
		this.nodeId = nodeId;

		if (votes >= 0)
			this.votes = votes;
	}

	/**
	 * @param args
	 */
	public void processRequest(LeaderElection req) {
		
		logger.info("n election manager process request");
		if (req == null)
			return;
		
		
		if (req.hasExpires()) {
			long ct = System.currentTimeMillis();
			if (ct > req.getExpires()) {
				// election is over
				return;
			}
		}
		if (req.getVote().getNumber() == VoteAction.ELECTION_VALUE) {
			logger.info("in ELECTION_VALUE");
		} else if (req.getVote().getNumber() == VoteAction.DECLAREVOID_VALUE) {
			// no one was elected, I am dropping into standby mode`
			logger.info("in DECLAREVOID_VALUE");
		} else if (req.getVote().getNumber() == VoteAction.DECLAREWINNER_VALUE) {
			// some node declared themself the leader
			logger.info("in DECLAREWINNER_VALUE");
		} else if (req.getVote().getNumber() == VoteAction.ABSTAIN_VALUE) {
			// for some reason, I decline to vote
			logger.info("in ABSTAIN_VALUE");
		} else if (req.getVote().getNumber() == VoteAction.NOMINATE_VALUE) {
			int comparedToMe = req.getNodeId().compareTo(nodeId);
			if (comparedToMe == -1) {
				// Someone else has a higher priority, forward nomination
				// TODO forward
			} else if (comparedToMe == 1) {
				// I have a higher priority, nominate myself
				// TODO nominate myself
			}
		}
	}
	private Management generateElection()
	{
		LeaderElection.Builder le = LeaderElection.newBuilder();
		le.setNodeId(nodeId);
		le.setBallotId(nodeId);
		le.setDesc(nodeId+"'s Election");
		le.setVote(VoteAction.ELECTION);
		le.setExpires(System.currentTimeMillis() + 2000);
		Management.Builder b = Management.newBuilder();
		b.setElection(le.build());
		//b.setBeat(le.build());
		Management msg = b.build();
		return msg;
		
	}
	@Override
	public void run()
	{
		logger.info("starting election manager Anita");

		while (true) {
			try {
				Thread.sleep(5000);
logger.info("outgoingHB.size() in election===="+HeartbeatManager.getInstance().outgoingHB.size());
				// ignore until we have edges with other nodes
				if (HeartbeatManager.getInstance().outgoingHB.size() > 0) 
					{
					// TODO verify known node's status
					logger.info("comin in iffff");
					
					// send my status (heartbeatMgr)
					GeneratedMessage msg = null;
					for (HeartbeatData hd : HeartbeatManager.getInstance().outgoingHB.values()) {
						// if failed sends exceed threshold, stop sending
						if (hd.getFailuresOnSend() > HeartbeatData.sFailureToSendThresholdDefault)
							continue;

						// only generate the message if needed
						if (msg == null)
							msg = generateElection();

						try {
							logger.info("sending votesss=="+msg);
							hd.channel.writeAndFlush(msg);
							logger.info("sending votes success");
								} catch (Exception e) {
							hd.incrementFailuresOnSend();
							logger.error("Failed " + hd.getFailures() + " times to send HB for " + hd.getNodeId()
									+ " at " + hd.getHost(), e);
						}
					}
				} else
					; // logger.info("No nodes to send HB");
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected management communcation failure", e);
				break;
			}
		}

	}
}
