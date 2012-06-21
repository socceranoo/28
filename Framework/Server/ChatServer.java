// This is the ChatServer Program
package Server;

import java.util.*;
//import ChatServerThread.java;

class ChatServer 
{
	private static final int port = 31123;
	private static final int port1 = 31125;
	public static void main(String args[]) 
	{
		ChatServerThread chatthread = new ChatServerThread(port);
		chatthread.start();
		GameServerThread gamethread = new GameServerThread(port+1);
		gamethread.start();
	}
	
	public ChatServer()
	{

	}
}
