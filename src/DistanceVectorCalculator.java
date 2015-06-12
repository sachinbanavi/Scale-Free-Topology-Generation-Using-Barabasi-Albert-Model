
/**
 * This class sends periodic updates to all the nodes in the network
 *
 */
public class DistanceVectorCalculator extends Thread
{
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			TCPServer.clientReq.sendToAll();
		}
	}
}
