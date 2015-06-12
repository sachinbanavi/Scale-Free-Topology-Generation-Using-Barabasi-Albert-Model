import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;


public class QueryClass
{
	 public static void main(String [] args)
	 {
		 String nodeIP = args[0];
		 int portUDP = Integer.parseInt(args[1]);
		 String networkNodeIP = args[2];
		 //int networkNortUDP = Integer.parseInt(args[3]);
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
				msg.additionalData = nodeIP+" "+portUDP;
				oo.writeObject(msg);
				oo.close();
				sendData = bStream.toByteArray();
				serverSocket = new DatagramSocket(portUDP);
				sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(networkNodeIP), 9090);
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
			try 
			{
				serverSocket.receive(receivePacket);
				iStream = new ObjectInputStream(new ByteArrayInputStream( receivePacket.getData()));
				Message received_msg = null;
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
			Enumeration<Integer> enumKey = node_list.keys();
			System.out.println("Node number, IP, TCP port number, UDP port number, Degree, Hops");
			while(enumKey.hasMoreElements())
			{
			    Integer key = enumKey.nextElement();
			    System.out.println(node_list.get(key).nodeNo+","+node_list.get(key).IP+","+node_list.get(key).portTCP+","+
			    node_list.get(key).portUDP+","+node_list.get(key).degree+","+node_list.get(key).hops);
			}
	 }
}
