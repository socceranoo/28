package Server;
import java.net.*;
import java.lang.*;
//import sClientGroup.java;

class ChatServerThread extends Thread 
{
	ServerSocket servSock = null;
	sClientGroup group;

	public ChatServerThread(int port) 
	{
		try 
		{
			servSock = new ServerSocket(port);
		}
		catch (Exception e)
		{
			System.out.println("Could not initialize. Exiting.");
			System.exit(1);
		}
		System.out.println("Server successfully initialized.  Waiting for connection...");
		group = new sClientGroup();
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
				System.out.println("Received New Connection.");
				group.addClient( tempSock );
			}
			catch (Exception e)
			{
				System.out.println("New Connection Failure.  Exiting.");
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
