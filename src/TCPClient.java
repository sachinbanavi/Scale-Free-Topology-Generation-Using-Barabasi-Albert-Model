import java.net.NoRouteToHostException;
import java.net.Socket;
import java.io.*;

/**
 * This class sends request to all the desired nodes so that the requesting node can be added to the network
 * It creates TCP ports and sends the request
 */
public class TCPClient
{	
	int nodeNo;
	Socket socket;
	
	public TCPClient(int nodeNo)
	{
		this.nodeNo = nodeNo;
	}
	
//	send message to the required node for connection request
	public MessageSender addConnection(int recNodeNo, int portTCP, String IP, MessageSender clientReq)
	{
		//System.out.println("inside addConnection");
		try
		{
			socket = new Socket(IP, portTCP);
			System.out.println("TCP Connection Request sent to node " + recNodeNo);
			clientReq.addMessageSender(recNodeNo, socket);  //to communicate to the node to request
		}
		catch (NoRouteToHostException e)
		{
			System.out.println("Host Missing");
			e.printStackTrace();			
		}
		catch (IOException e) 
		{
			System.out.println("Send message failed");
			e.printStackTrace();
		}
		
		try
		{
			Thread.sleep(200);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.senderId = nodeNo;
		msg.receiverId = recNodeNo;
		msg.note = "conn";
		msg.additionalData = nodeNo+" "+TCPServer.nodeIP + " "+TCPServer.portTCP+" "+TCPServer.portUDP+" "+TCPServer.m;
		clientReq.send(msg, recNodeNo);
		
		return clientReq;
	}
	
//	Creating the connection to the second node in the m0 network
	public MessageSender addSecondConnection(Node node, MessageSender clientReq)
	{
		try
		{
			socket = new Socket(node.IP, node.portTCP);
			System.out.println("TCP Connection Request sent to node " + node.nodeNo);
			clientReq.addMessageSender(node.nodeNo, socket);  //send request to the node for connection request
		}
		catch (NoRouteToHostException e)
		{
			System.out.println("Host Missing");
			e.printStackTrace();			
		}
		catch (IOException e) 
		{
			System.out.println("Send message failed");
			e.printStackTrace();
		}
		node.hops = 1;
		TCPServer.node_list.put(node.nodeNo, node);	// add the node to the list
		TCPServer.node_neighbours.add(node); // also add the node to the neighbour list
		
		try
		{
			Thread.sleep(200);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		return clientReq;
	}
	
	
//	sleeps for some time till all the nodes become active are are ready to communicate
	public MessageSender initialConnection(MessageSender clientReq)
	{
		//System.out.println("inside initialConnection");
		try
		{
			Thread.sleep(20000);  // sleep while others get ready
		}
		catch(InterruptedException e)
		{
			System.out.println("Client's sleep catch");
		}
		
		try
		{
			//System.out.println("TCPServer.node_neighbours.size() "+TCPServer.node_neighbours.size());
			for(int i=0;i<TCPServer.node_neighbours.size();i++)
			{
				socket = new Socket();
				int recNodeNo = TCPServer.node_neighbours.get(i).nodeNo;
				socket = new Socket(TCPServer.node_neighbours.get(i).IP, TCPServer.node_neighbours.get(i).portTCP);
				System.out.println("Connection Request sent to node " + recNodeNo);
				clientReq.addMessageSender(recNodeNo, socket);
			}
		}
		catch (NoRouteToHostException e)
		{
			System.out.println("Host Missing");
			e.printStackTrace();			
		}
		catch (IOException e) 
		{
			System.out.println("Send message failed");
			e.printStackTrace();
		}
		return clientReq;
	}
}
