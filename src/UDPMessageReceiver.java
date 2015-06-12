import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Enumeration;
import java.io.*;

public class UDPMessageReceiver extends Thread 
{
	DatagramSocket serverSocket;
	byte[] receiveData = new byte[10000];
    byte[] sendData = new byte[10000];
	int nodeNo;
	boolean dedicatedReceiver = false;
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

	public UDPMessageReceiver(int nodeNo,DatagramSocket serverSocket)
	{
		this.nodeNo = nodeNo;
		this.serverSocket = serverSocket;
	}
	
	public UDPMessageReceiver(int nodeNo,DatagramSocket serverSocket, int dedPortUDP)
	{
		this.nodeNo = nodeNo;
		this.serverSocket = serverSocket;
		dedicatedReceiver = true;
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				serverSocket.receive(receivePacket);
				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream( receivePacket.getData()));
				Message received_msg = (Message) iStream.readObject();
				if(dedicatedReceiver)
					System.out.println("UDP Message received at dedicated port");
				else
					System.out.println("UDP Message received from node number "+received_msg.senderId);
				if(received_msg.note.equals("query"))
				{
					Message msg = new Message();
					if(!dedicatedReceiver)
					{
						msg.senderId = nodeNo;
						msg.receiverId = received_msg.senderId;
						if(TCPServer.node_list.size() >= TCPServer.n)
							msg.note = "Reject";
						else
						{
							msg.note = "Reply";
							msg.nodesDetails = TCPServer.node_list;
						}
					}
					else
					{
						if(received_msg.question == 1 || received_msg.question == 2)
							msg.nodesDetails = TCPServer.node_list;
						else
						{
							Integer farNode = 0;
							int farHops = 0;
							Enumeration<Integer> enumKey = TCPServer.node_list.keys();
							while(enumKey.hasMoreElements())
							{
							    Integer key = enumKey.nextElement();
							    if(TCPServer.node_list.get(key).hops > farHops)
							    {
							    	farHops = TCPServer.node_list.get(key).hops;
							    	farNode = TCPServer.node_list.get(key).nodeNo;
							    }
							}
							msg.note = farNode.toString(); 
						}
					}
					String[] data = received_msg.additionalData.split(" ");
					DatagramPacket sendPacket;
					byte[] sendData = new byte[10000];
					ByteArrayOutputStream bStream = new ByteArrayOutputStream();
					ObjectOutput oo = null;
					try 
					{
						oo = new ObjectOutputStream(bStream);
					}
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
					oo.writeObject(msg);
					oo.close();
					sendData = bStream.toByteArray();
					sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(data[0]), Integer.parseInt(data[1]));
					serverSocket.send(sendPacket);
					if(dedicatedReceiver)
						System.out.println("UDP reply message sent.");
					else
						System.out.println("UDP reply message sent to node number "+msg.receiverId);
				}
				iStream.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}	
		}
	}
}
