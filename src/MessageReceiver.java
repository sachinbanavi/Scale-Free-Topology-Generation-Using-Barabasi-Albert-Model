import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.*;

/**
 * This class receives the message from the sender and checks weather the information is 
 * updated or not,if updated then it forwards the message to its neighbouring nodes and
 *  updates the receiver nodes information.Else discard the message.  
 *
 */
public class MessageReceiver extends Thread 
{
	int nodeNo;
	Socket socket;
	ObjectInputStream ois;
	static int count = 0;
	//Creates the input stream for receiving the message
	public MessageReceiver(int nodeNo,Socket socket)
	{
		this.nodeNo = nodeNo;
		this.socket = socket;
		try 
		{
			ois = new ObjectInputStream(socket.getInputStream());//new input stream object
		}
		catch (IOException e) 
		{
			System.out.println("Error while reading message");
			e.printStackTrace();
		}
	}
	
	//Convert the data in the input stream in the required form and adds nodes information to Hash table
	public void run()
	{
		while(true)
		{
			try
			{
				Message msg = (Message)ois.readObject();
				System.out.println("Message received from node "+msg.senderId);
				if(msg.note.equals("conn"))
				{
					String[] data = msg.additionalData.split(" ");
					Node n = new Node();
					n.nodeNo = Integer.parseInt(data[0]);
					n.IP = data[1];
					n.portTCP = Integer.parseInt(data[2]);
					n.portUDP = Integer.parseInt(data[3]);
					n.degree = Integer.parseInt(data[4]);
					TCPClient c = new TCPClient(nodeNo);
					TCPServer.clientReq = c.addSecondConnection(n, TCPServer.clientReq);
					
				}				
				else
				{
					if(count == 0)
					{
						Thread set = new Setter();
						set.start();
					}
					//checking whether updated information is received or not
					Hashtable<Integer, Node> aai = msg.nodesDetails;
					int flag = 0;
					
					synchronized(TCPServer.node_list)
					{
						System.out.println("Own vector is (Node number, its degree, its distance from my node)");
						Enumeration<Integer> enumKey = TCPServer.node_list.keys();
						//Checking hash table for more or unprossed data
						while(enumKey.hasMoreElements())
						{
						    Integer key = enumKey.nextElement();
						    System.out.print(TCPServer.node_list.get(key).nodeNo+","+TCPServer.node_list.get(key).degree+
				    		","+TCPServer.node_list.get(key).hops+"\t");
						}
						System.out.println();
						System.out.println("Received vector from node number "+msg.senderId+" contains (Node number, its degree, its distance from my node)");
						enumKey = aai.keys();
						
						//Checking hash table for more or unprossed data
						while(enumKey.hasMoreElements())
						{
						    Integer key = enumKey.nextElement();
							System.out.print(aai.get(key).nodeNo+","+aai.get(key).degree+","+aai.get(key).hops+"\t");
						}
						System.out.println();
						enumKey = aai.keys();
						Integer key;
						
						//Checking hash table for more or unprossed data
						while(enumKey.hasMoreElements())
						{
						    key = enumKey.nextElement();
						    //checking for the match in hash table
						    if(TCPServer.node_list.containsKey(key))
						    {
								if((aai.get(key).hops+1) < TCPServer.node_list.get(key).hops)
								{
									TCPServer.node_list.get(key).hops = aai.get(key).hops+1;
									if(flag == 0)
										flag = 1;
									System.out.println("Got updated information");
								}
								if(aai.get(key).degree > TCPServer.node_list.get(key).degree)
								{
									TCPServer.node_list.get(key).degree = aai.get(key).degree;
									if(flag == 0)
										flag = 1;
									System.out.println("Got updated information");
								}
						    }
						    
						    //update the new information 
						    else
							{
								aai.get(key).hops = aai.get(key).hops+1;
								TCPServer.node_list.put(aai.get(key).nodeNo, aai.get(key));
								flag = 1;
								System.out.println("Got updated information");
							}
						}
					}
					//updation occured
					if(flag == 1 || count == 0) //flag = 0 && count ==1 means no flooding.
					{
						TCPServer.clientReq.sendToAll();
						if(count == 0)
							count = 1;
					}
				}
			}
			//If the class it self is not present
			catch (ClassNotFoundException e) 
			{
				System.out.println("Error while reading data from " + socket.getRemoteSocketAddress()+ " at "+socket.getPort());
  				e.printStackTrace();
				System.exit(0);
			}
			catch(IOException e) 
			{
				System.out.println("Great Error while reading data from " + socket.getRemoteSocketAddress()+" at "+socket.getPort());
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}
