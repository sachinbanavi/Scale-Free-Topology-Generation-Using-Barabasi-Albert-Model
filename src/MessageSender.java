import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * It is used to send the message to neighboring nodes through the output stream by using object output stream to send the updated messages
 *
 */
public class MessageSender
{
	int nodeNo = 0;
	ArrayList<SendersList> sender_list = new ArrayList<SendersList>();
	// Initializes the node number
	public MessageSender(int nodeNo)
	{
		this.nodeNo = nodeNo;
	}
	
	//populate sender list to send messages to desired nodes
	public void addMessageSender(int recNodeNo, Socket sock)
	{
		SendersList sl = new SendersList();
		sl.receiverNodeNo = recNodeNo;
		try
		{
			ObjectOutputStream oos= new ObjectOutputStream(sock.getOutputStream());
			sl.ooss = oos;
			sender_list.add(sl);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// send the information to all nodes with respective node numbers
	public void sendToAll()
	{
		
		Message msg = new Message();
		msg.senderId = nodeNo;
		msg.note = "";
		synchronized(TCPServer.node_list)
		{
			msg.nodesDetails = TCPServer.node_list;
		}
		
		synchronized(sender_list)
		{

			for(int i=0  ; i<sender_list.size(); i++)
			{
				try
				{
					sender_list.get(i).ooss.reset();
					sender_list.get(i).ooss.writeObject(msg);
					System.out.println("Sending my vector to node number "+ sender_list.get(i).receiverNodeNo);
				}
				catch (IOException e) 
				{
					System.out.println("Send message failed");
					e.printStackTrace();
				}
			}
		}
	}
		
		//Sends the messages to the receiver node untill the sender list is empty
	public void send(Message msg, int receiverNodeNo)
	{
		ObjectOutputStream oos = null;
		for(int i=0  ; i<sender_list.size(); i++) //iterate untill the sender list is empty
		{
			if(receiverNodeNo == sender_list.get(i).receiverNodeNo)
			{
				oos = sender_list.get(i).ooss;
				break;
			}
		}
		try
		{
			oos.writeObject(msg);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}

class SendersList
{
	ObjectOutputStream ooss;
	int receiverNodeNo;
}