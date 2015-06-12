import java.io.Serializable;
import java.util.Hashtable;

/**
 * This is a data structure of the Message that is exchanged betweent the nodes
 *
 */
class Message implements Serializable
{
	private static final long serialVersionUID = 1L;
	int senderId;
	int receiverId;
	int question;
	String additionalData = "";
	String note = "";
	Hashtable<Integer, Node> nodesDetails;
}
