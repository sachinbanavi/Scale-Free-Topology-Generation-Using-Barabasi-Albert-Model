import java.io.Serializable;

/**
 * Defines the data structure for Node information
 * 
 */
public class Node implements Serializable
{
	private static final long serialVersionUID = 1L;
	int nodeNo;
	int portTCP;  
	int portUDP;
	String IP;
	int degree = 0; 
	int hops; 
}
