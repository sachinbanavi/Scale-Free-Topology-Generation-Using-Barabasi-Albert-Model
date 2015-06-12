import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This class creates the server sockets and listens on a given port no for incoming connection requests
 *
 */
public class SocketConnector extends Thread
{
	ServerSocket ssocket = null;
	Socket socket = null;
	int node;
	int port;
	
//	initialize the instance variables
	public SocketConnector(int port, int nodeNo) throws IOException
	   {
	      this.port = port;
	      this.node = nodeNo;
	      try
			{
//	    	  create new server socket and listen for incoming connections
				ssocket = new ServerSocket(port);
				//System.out.println("Socket created");
			}
			catch(IOException e)
			{				
				e.printStackTrace();
			} 
	   }
	public void run()
	   {
	      while(true)
	      {
	         try
	         {
//	        	 accept the new connection request
	            socket = ssocket.accept();
	            //setting own degree
				TCPServer.node_list.get(node).degree += 1;
				//System.out.println("degree "+TCPServer.node_list.get(node).degree);
	            System.out.println(node + " number node is listening");
	            Thread ms = new MessageReceiver(node, socket);
	            ms.start();
	         }
	         catch(IOException e)
	         {
	            e.printStackTrace();
	            break;
	         } 
	      }
	   }
	
}
