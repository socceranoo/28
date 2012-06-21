package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

class sGameGroup extends Thread 
{
	Vector theGroup;
	int current_player;
	Trump tr;
	static boolean []id_pool;
	int num_threads;
	Sync sync, sync2;

	public sGameGroup() 
	{
		tr = new Trump();
		theGroup = new Vector();
		current_player = -1;
		id_pool = new boolean [tr.MAX_PLAYERS];
		set_id_pool_to_false();
		num_threads=0;
		sync = new Sync(this);
		sync2 = new Sync(this);
	}

	public void addClient(Socket s)
	{
		sGameThread tempThread;
		int id = get_new_player_id();
		if (id < 0)
			return;
		tempThread = new sGameThread( s, this , id, tr, sync, sync2);
		theGroup.addElement(tempThread);
		tempThread.start();
	}

	public void run() 
	{
		while( true )
		{
			try
			{
				sleep(30000);
			}
			catch (Exception e)
			{
			}
			cleanHouse();
		}
	}

	/* send a message "msg", of type "type", to all Clients */
	public void sendMessage(String msg, String type)
	{ 
		int x;

		for(x=0; x<theGroup.size(); x++) 
			((sGameThread)theGroup.elementAt(x)).message(type+"||"+msg);
	/* remember that the format for messages is "type||message" */
	}


	/* send a message "msg", of type "type", to the Client with alias "target" */
	public void sendMessage(String msg, String target, String type)
	{ 
		int x;
		sGameThread tempThread;

		for(x=0; x<theGroup.size(); x++)
		{
			tempThread=(sGameThread)theGroup.elementAt(x);
			if( tempThread.getAlias().equals(target) )
				tempThread.message(type+"||"+msg);
		}
	}

	/* here is where we handle any input received from a Client */
	/* This method is called by sGameThread directly */
	public void handleInput(int position, sGameThread T, int card_index)
	{
		try 
		{
			sGameThread tempThread;
			for(int x=0; x<theGroup.size(); x++)
			{
				tempThread=(sGameThread)theGroup.elementAt(x);
				if (tempThread != T)
				{
					String output = "pos";
					tempThread.os.writeObject(output);
					tempThread.os.flush();
					tempThread.os.writeObject(T.player_id);
					tempThread.os.flush();
					tempThread.os.writeObject(position);
					tempThread.os.flush();
					tempThread.os.writeObject(card_index);
					tempThread.os.flush();
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	/* return a list of all currently connected users in the form "name1&name2&name3" */
	public String [] calcList2()
	{
		String []temp = new String[tr.NO_OF_PLAYERS];
		for(int x=0; x<theGroup.size(); x++)
		{
			temp[x] = ((sGameThread) (theGroup.elementAt(x))).getAlias();
			if(temp[x] == null)
				temp[x] = "null";
		}
		return temp;
	}

	/* go through the Vector, and search for "dead" Threads (which are disconnected) and then remove them from the list */
	public void cleanHouse()
	{
		int x;
		sGameThread tempThread;

		for (x=0; x<theGroup.size(); x++)
		{
			tempThread = (sGameThread)theGroup.elementAt(x);
			if( tempThread==null ||  ! tempThread.isAlive() )
				theGroup.removeElement( tempThread );
		}
	}

	public void finalize()
	{
		Thread t;

		for( int x=0;x<theGroup.size();x++)
		{
			t = (Thread)theGroup.elementAt(x);
			if( t!=null && t.isAlive() )
				t.stop();
		}
	}
	
	public int get_new_player_id()
	{
		for (int k = 0; k < tr.MAX_PLAYERS; k++)
		{
			if (id_pool[k] == false)
			{
				id_pool[k] = true;	
				return k;	
			}
		}	
		return -1;
	}

	public void set_id_pool_to_false()	
	{
		for (int k = 0; k < tr.MAX_PLAYERS; k++)
		{
			id_pool[k] = false;	
		}	
	}

	void send_new_deal()
	{
		try
		{
			sGameThread tempThread;
			for (int x=0; x<theGroup.size(); x++)
			{
				tempThread = (sGameThread)theGroup.elementAt(x);
				if( tempThread != null && tempThread.isAlive() )
				{
					int [] card_array = tempThread.set_card_array();
					String msg = "cards";
					tempThread.os.writeObject(msg);
					tempThread.os.flush();
					tempThread.os.writeObject(card_array);
					tempThread.os.flush();
				}
			}
		}
		catch (Exception e)
		{

		}
	}
	
	void send_round_value()
	{
		try
		{
			sGameThread tempThread;
			for (int x=0; x<theGroup.size(); x++)
			{
				tempThread = (sGameThread)theGroup.elementAt(x);
				if( tempThread != null && tempThread.isAlive() )
				{
					String msg = "round";
					tempThread.os.writeObject(msg);
					tempThread.os.flush();
					tempThread.os.writeObject(tr.round_score);
					tempThread.os.flush();
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	void send_game_value()
	{
		try
		{
			sGameThread tempThread;
			for (int x=0; x<theGroup.size(); x++)
			{
				tempThread = (sGameThread)theGroup.elementAt(x);
				if( tempThread != null && tempThread.isAlive() )
				{
					String msg = "game";
					tempThread.os.writeObject(msg);
					tempThread.os.flush();
					tempThread.os.writeObject(tr.game_score);
					tempThread.os.flush();
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	void sendTrumpSet(sGameThread T, int player_id, int card_position)
	{
		try
		{
			sGameThread tempThread;
			for (int x=0; x<theGroup.size(); x++)
			{
				tempThread = (sGameThread)theGroup.elementAt(x);
				if (tempThread != T)
				{
					if( tempThread != null && tempThread.isAlive() )
					{
						String msg = "trumpset";
						tempThread.os.writeObject(msg);
						tempThread.os.flush();
						tempThread.os.writeObject(player_id);
						tempThread.os.flush();
						tempThread.os.writeObject(card_position);
						tempThread.os.flush();
					}
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	void sendRevealedTrump()
	{
		try
		{
			sGameThread tempThread;
			for (int x=0; x<theGroup.size(); x++)
			{
				tempThread = (sGameThread)theGroup.elementAt(x);
				if( tempThread != null && tempThread.isAlive() )
				{
					String msg = "round";
					tempThread.os.writeObject(msg);
					tempThread.os.flush();
					tempThread.os.writeObject(tr.round_score);
					tempThread.os.flush();
				}
			}
		}
		catch (Exception e)
		{

		}
	}
	/* END OF CLASS */
}
