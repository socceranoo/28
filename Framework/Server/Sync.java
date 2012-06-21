package Server;

import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

class Sync
{
	sGameGroup parent;
	public Sync(sGameGroup s)
	{
		parent = s;
	}

	synchronized void syncPlay(sGameThread tempThread)
	{
		try
		{
			while (tempThread.player_id != tempThread.trumpObject.cur_player)
			{
				wait();
				tempThread.print("thread "+tempThread.player_id+" waking up play");
			}
			tempThread.play();
			notifyAll();
		}
		catch (Exception e)
		{

		}

	}

	synchronized void syncBid(sGameThread tempThread, int round)
	{
		try
		{
			while (tempThread.player_id != tempThread.trumpObject.cur_player)
			{
				wait();
				tempThread.print("thread "+tempThread.player_id+" waking up bid");
			}
			tempThread.bid(round);
			notifyAll();
		}
		catch (Exception e)
		{

		}
	}

	synchronized void converge(int option, sGameThread tempThread)
	{
		try
		{
			if(parent.num_threads != parent.theGroup.size())
			{
				wait();
				tempThread.print("thread "+tempThread.player_id+" waking up converge and num_threads = "+parent.num_threads);
			}
			else
			{
				if (option == 1)
					tempThread.trumpObject.game_over = false;
				parent.num_threads = 0;
			}
			notifyAll();

		}
		catch (Exception e)
		{

		}

	}
	
	synchronized void syncTrump(sGameThread tempThread)
	{
		try
		{
			if (tempThread.player_id != tempThread.trumpObject.cur_player)
			{
				wait();
				tempThread.print("thread "+tempThread.player_id+" waking up trump");
			}
			else
			{
				tempThread.setTrump();
				notifyAll();
			}
		}
		catch (Exception e)
		{

		}

	}
}
