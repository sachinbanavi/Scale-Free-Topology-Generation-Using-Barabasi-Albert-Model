
public class Setter extends Thread
{
	public void run()
	{
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		MessageReceiver.count = 0;
	}
}
