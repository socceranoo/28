package Server;
import java.net.*;
import java.lang.*;

class GameServerThread extends Thread 
{
	ServerSocket servSock = null;
	sGameGroup group;

	public GameServerThread(int port) 
	{
		try 
		{
			servSock = new ServerSocket(port);
		}
		catch (Exception e)
		{
			System.out.println("Could not initialize Game Server. Exiting.");
			System.exit(1);
		}
		System.out.println("Game Server successfully initialized.  Waiting for game connection...");
		group = new sGameGroup();
		group.start();
	}

	public void run()
	{
		while(servSock != null ) 
		{
			Socket tempSock;
			try
			{
				tempSock = servSock.accept();
				System.out.println("Received Game New Connection.");
				group.addClient( tempSock );
			}
			catch (Exception e)
			{
				System.out.println("New Game Connection Failure.  Exiting.");
				System.exit(1);
			}
		}
	}

	public void finalize()
	{
		try
		{
			servSock.close();
		}
		catch(Exception e)
		{
		}
		servSock = null;
		group.stop();
	}
}
