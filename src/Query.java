
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * After joining the network, each node answers queries from a dedicated UDP port 9090.
 *
 */
public class Query
{
	
	static String nodeIP;
	static int portUDP;
	static String nodeIPToQuery;
	static int queryNumber;
	 
	 public static void main(String [] args)
	 {
		 readQueryInfo();
//		 nodeIP = args[0];
//		 portUDP = Integer.parseInt(args[1]);
//		 nodeIPToQuery = args[2];
//		 queryNumber = Integer.parseInt(args[3]);
		 DatagramSocket serverSocket = null;
		 Hashtable<Integer, Node> node_list = new Hashtable<Integer, Node>();
		 try
			{
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
				Message msg = new Message();
				msg.note= "query";
				msg.question = queryNumber; 
				msg.additionalData = nodeIP+" "+portUDP;
				oo.writeObject(msg);
				oo.close();
				sendData = bStream.toByteArray();
				serverSocket = new DatagramSocket(portUDP);
				sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(nodeIPToQuery), 9090);
				serverSocket.send(sendPacket);
				System.out.println("UDP query message sent to a network node ");
				
			}
			catch(SocketException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			byte[] receiveData = new byte[10000];
			//wait for response
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			ObjectInputStream iStream;
			Message received_msg = null;
			try 
			{
				serverSocket.receive(receivePacket);
				iStream = new ObjectInputStream(new ByteArrayInputStream( receivePacket.getData()));
				received_msg = (Message) iStream.readObject();
				node_list = received_msg.nodesDetails;
				System.out.println("UDP query's reply message Received");
				iStream.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			//Printing data recieved.
			if(queryNumber == 1 || queryNumber == 2)
			{
				Enumeration<Integer> enumKey = node_list.keys();
				System.out.println("Node number, IP, TCP port number, UDP port number, Degree, Hops");
				while(enumKey.hasMoreElements())
				{
				    Integer key = enumKey.nextElement();
				    System.out.println(node_list.get(key).nodeNo+","+node_list.get(key).IP+","+node_list.get(key).portTCP+","+
				    node_list.get(key).portUDP+","+node_list.get(key).degree+","+node_list.get(key).hops);
				}
			}
			else
				System.out.println("Farthest node is "+ received_msg.note);
	 }
	 
	 
	 
	 
	 private static void readQueryInfo() {
			
			try{
				//create a scanner to read information from the console
			Scanner sn = new Scanner(System.in);
			   BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
			   System.out.println("Enter your NodeIP, UDP Port, NodeIP (for which you want to query), QueryNumber \n");
			   System.out.println("Queries");
			   System.out.println("1. Transfer of the current routing table \n" +
			   		"2. Send the node degree information \n" +
			   		"3. Send the ID of the farthest away node in the system \n");
			 	nodeIP = sn.next();
				portUDP = Integer.parseInt(sn.next());
				nodeIPToQuery = sn.next();
				queryNumber  = Integer.parseInt(sn.next());
			} catch(NumberFormatException e){
				System.out.println("Please enter valid input");
				System.out.println();
				readQueryInfo();
			}
		}
}
