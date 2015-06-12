import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.net.DatagramSocket;

/**
 * The main class which contains all the logic
 * This class reads the node information such as NodeNo, Ip address, TCP port, UDP port
 * It also reads the config file to fetch the network information
 *
 */
public class TCPServer extends Thread
{
//	Define required members
	static int portTCP;
	static int portUDP;
	static int dedicatedPortUDP = 9090;
	static int nodeNo;
	static String nodeIP = "";
	static DatagramSocket serverSocket;
	static int m0;
	static Hashtable<Integer, Node> node_list = new Hashtable<Integer, Node>();
	static ArrayList<Node> node_neighbours = new ArrayList<Node>();
	static TCPClient c;
	static MessageSender clientReq;
	static int m;
	static int n;
	
	public TCPServer()
	{}

   public static void main(String [] args)
   {
//	   read input from console for the node information
		readNodeInfo();
	   
//	    nodeNo = Integer.parseInt(args[0]);
//	    nodeIP = args[1];
//		portTCP = Integer.parseInt(args[2]);
//		portUDP = Integer.parseInt(args[3]);
		clientReq= new MessageSender(nodeNo);
		//extracting info from config file
		try
		{
//			create a stream to read the config file
			BufferedReader br;
			br = readConfigFile();
//			create udp port for nodes to listen for queries on a dedicated port
			createDedicatedUDPReceiver();
			if(nodeNo<m0)
				addNodeToNetwork(br);
			else
				addNewNodeToNetwork();
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
   }


/**
 * reads the node information  from the console, given by the user
 */
private static void readNodeInfo() {
	
	try{
		//create a scanner to read information from the console
	Scanner sn = new Scanner(System.in);
	   BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
	   System.out.println("Enter NodeNo, IP, TcpPort, UdpPort");
	 	nodeNo = Integer.parseInt(sn.next());
//		parse the input to the required format
		   nodeIP = sn.next();
		   portTCP = Integer.parseInt(sn.next());
		   portUDP = Integer.parseInt(sn.next());
	} catch(NumberFormatException e){
		System.out.println("Please enter valid input");
		System.out.println();
		readNodeInfo();
	}
}

//read the config file for the information about the m0 nodes for the initial network configuratin details
   public static BufferedReader readConfigFile()
   {
	   BufferedReader br = null;
	   try
		{
			br = new BufferedReader(new FileReader("config.txt"));
			String strLine = br.readLine();
			m0 = Integer.parseInt(strLine);
			strLine = br.readLine();
			String data[];
			data = strLine.split(" ");
			n = Integer.parseInt(data[2]);
			strLine = br.readLine();
			data = strLine.split(" ");
			m = Integer.parseInt(data[2]);
			Node nd;
			//extracting m0 nodes information
			for(int i = 0;i <m0; i++)
			{
				nd = new Node();
				strLine = br.readLine();
				if(i == nodeNo)
					nd.hops = 0;
				data = strLine.split(" ");
				nd.nodeNo = Integer.parseInt(data[0]);
				nd.IP = data[1];
				nd.portTCP= Integer.parseInt(data[2]);
				nd.portUDP= Integer.parseInt(data[3]);
				node_list.put(nd.nodeNo, nd);
			}
		}
		catch (FileNotFoundException e2)
		{
			e2.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	   return br;
   }
// create a dedicated port for UDP to listen for query messages
   static public void createDedicatedUDPReceiver()
   {
	   try
		{
			DatagramSocket serverSocket = new DatagramSocket(dedicatedPortUDP);
			Thread udp_msg_rcv = new UDPMessageReceiver(nodeNo,serverSocket, dedicatedPortUDP);
			udp_msg_rcv.start();
			System.out.println("Dedicated UPDMessageReceiver for queries only created at " + dedicatedPortUDP);
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
   }
   
// if the node is part of the initial network, do the required work
   static public void addNodeToNetwork(BufferedReader br)
   {
	   System.out.println("This node is part of initial network.");
		//extracting neighbours information
	   String[] data;
	   String strLine = null;
		for(int j = 0;j <=nodeNo; j++)
			try
			{
				strLine = br.readLine();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		data = strLine.split(" ");
		Node nd;
		if(Integer.parseInt(data[0]) == nodeNo)
		{
			for(int k = 2;k <data.length; k++)
			{
				node_list.get(Integer.parseInt(data[k])).hops=1;
				nd = node_list.get(Integer.parseInt(data[k]));
				node_neighbours.add(nd);
			}
		}
		try {
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//creating TCP socket connector.
		//sending TCP request to all its neighbours
		try
		{
			Thread sc = new SocketConnector(portTCP, nodeNo);
			sc.start();
			c =new TCPClient(nodeNo);
			clientReq = c.initialConnection(clientReq);
			System.out.println("Initial TCP connection done");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
//   initiate the distance vector protocol
		initiateDistanceVectorProtocol();
		createUDPReceiver();
   }
   
   public static void initiateDistanceVectorProtocol()
   {
	   if(nodeNo == 0)
		{	
			Thread dvCalculator = new DistanceVectorCalculator();
			dvCalculator.start();
		}
   }
   
// create receiver for reading messages on a UDP port
   static public void createUDPReceiver()
   {
	   try
		{
			serverSocket = new DatagramSocket(portUDP);
			Thread udp_msg_rcv = new UDPMessageReceiver(nodeNo,serverSocket);
			udp_msg_rcv.start();
			System.out.println("UPDMessageReceiver created at " + portUDP);
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
   }
   
// adds the node to the network as it is a new node
   static public void addNewNodeToNetwork()
   {
	   System.out.println("This is a new node");
		//Select randomly a node from m0
		int recNodeNo = (int)(Math.random()*m0);
		//Send UDP msg to get node's details
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
			msg.senderId = nodeNo;
			msg.receiverId = recNodeNo;
			msg.note= "query";
			msg.additionalData = nodeIP+" "+portUDP;
			oo.writeObject(msg);
			oo.close();
			sendData = bStream.toByteArray();
			String IP =node_list.get(recNodeNo).IP;
			int UDPport = node_list.get(recNodeNo).portUDP;
			serverSocket = new DatagramSocket(portUDP);
			sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(IP), UDPport);
			serverSocket.send(sendPacket);
			System.out.println("UDP query message sent to node "+ recNodeNo);
			newNodeWaitingForUDPReply(serverSocket);
			Thread udp_msg_rcv = new UDPMessageReceiver(nodeNo,serverSocket);
			udp_msg_rcv.start();
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
   }
   
   static public void newNodeWaitingForUDPReply(DatagramSocket serverSocket)
   {
	   try
	   {
		    byte[] receiveData = new byte[10000];
			//wait for response
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream( receivePacket.getData()));
			Message received_msg = null;
			received_msg = (Message) iStream.readObject();
			System.out.println("UDP query's reply message Received");
			iStream.close();
			newNodeUpdatingVector(received_msg);
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
	    catch (IOException e)
		{
			e.printStackTrace();
		}
   }
   
   static void newNodeUpdatingVector(Message received_msg)
   {
	 //Network limit reached.
		//Exiting
		if(received_msg.note.equals("Reject"))
		{
			System.out.println("Exiting network reached its size limit.");
			System.exit(0);
		}
		//Within network capacity
		else
		{
			//update NodeDetails
			node_list = received_msg.nodesDetails;
			//setting hop = Integer.MAX_VALUE
			Enumeration<Integer> enumKey = node_list.keys();
			while(enumKey.hasMoreElements())
				node_list.get(enumKey.nextElement()).hops = Integer.MAX_VALUE-10;
			//randomly select m nodes
			Node[] selected_nodes = randomlySelectNodes(m);
			
			//updating node_neighbours
			for(int j =0;j<selected_nodes.length;j++)
			{
				node_list.get(selected_nodes[j].nodeNo).hops = 1;
				node_neighbours.add(selected_nodes[j]);
				node_neighbours.get(j).hops = 1;
			}
			
			//add own node's detail
			Node node = new Node();
			node.IP = nodeIP;
			node.nodeNo = nodeNo;
			node.portTCP = portTCP;
			node.portUDP = portUDP;
			node.hops = 0;
			node_list.put(nodeNo, node);
							
			//send TCP req to those m nodes.
			try
			{
				Thread sc = new SocketConnector(portTCP, nodeNo);
				sc.start();
				c =new TCPClient(nodeNo);
				for(int i = 0;i<m;i++)
					clientReq = c.addConnection(selected_nodes[i].nodeNo, selected_nodes[i].portTCP, selected_nodes[i].IP,clientReq);
			}			
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Server's catch");
				System.exit(0);
			}
		}
   }
   
   //Select nodes randomly on the basis of probability of degrees.
   public static Node[] randomlySelectNodes(int m)
   {
	   ArrayList<ArrayList<Node>> aan = new ArrayList<ArrayList<Node>>();
	   ArrayList<Integer> dgr_idx = new ArrayList<Integer>();
	   ArrayList<Integer> dgr_pool = new ArrayList<Integer>();
	   Node[] selected_node = new Node[m];
	   //setting up nodes to do random selection
	   Enumeration<Integer> enumKey = node_list.keys();
	   while(enumKey.hasMoreElements())
	   {
		   Integer key = enumKey.nextElement();
		   Integer dgr = node_list.get(key).degree;
		   if(dgr_idx.contains(dgr))
		   {
			   int idx = dgr_idx.indexOf(dgr);
			   aan.get(idx).add(node_list.get(key));   
		   }
		   else
		   {
			   dgr_idx.add(dgr);
			   int idx = dgr_idx.indexOf(dgr);
			   ArrayList<Node> an = new ArrayList<Node>();
			   aan.add(an);
			   aan.get(idx).add(node_list.get(key));
		   }
		   for(int i = 0; i<dgr;i++)
			   dgr_pool.add(dgr);
	   }
	   //selecting nodes randomly 
	   for(int j = 0; j<m;j++)
	   {
		   int rnd = (int)(Math.random()*dgr_pool.size());
		   int selected_dgr = dgr_pool.get(rnd);
		   int idx = dgr_idx.indexOf(selected_dgr);
		   int list_size = aan.get(idx).size();
		   int rnd1 = (int)(Math.random()*list_size);
		   Node node = aan.get(idx).get(rnd1);
		   System.out.println("node number "+node.nodeNo +" got selected by random process");
		   selected_node[j] = node;
		   //not last one to be removed. This condition is used to avoid extra removal.		   
		   if(j != (m-1))
		   {
			   aan.get(idx).remove(rnd1);
			   for(int i = 0; i<selected_dgr;i++)
				   dgr_pool.remove(selected_dgr);
			   if(aan.get(idx).size() == 0)
			   {
				   aan.remove(idx);
				   dgr_idx.remove(idx);
			   }
		   }
	   }
	   return selected_node;
   }
}