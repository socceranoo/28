package Server;

import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

class sGameThread extends Thread 
{
	sGameGroup parent;
	Socket theSock;
	String alias;
	int player_id;
	int bid ;
	int [] init;
	ObjectOutputStream os;
	ObjectInputStream is;
	Trump trumpObject;
	Sync S, S2;

	public sGameThread(Socket s, sGameGroup p, int id, Trump tr, Sync sync, Sync sync2)
	{
		trumpObject = tr;
		theSock = s;
		parent = p;
		player_id = id;
		bid = 24;
		S = sync;
		S2 = sync2;
	}

	public void run()
	{
		try
		{
			int temp = 0;
			os = new ObjectOutputStream(theSock.getOutputStream());
			os.flush();
			is=new ObjectInputStream(theSock.getInputStream());
			alias = (String) is.readObject();

			//synchronisation code goes here
			while (parent.theGroup.size() != trumpObject.NO_OF_PLAYERS)
			{
				sleep_time(500);
			}
			sleep_time(200);
			//	Initial Transfer of objects	
			os.writeObject("init");					
			os.flush();
			//Prepare init array 
			init = new int[3];
			init[0] = trumpObject.NO_OF_PLAYERS;
			init[1] = trumpObject.TOTAL_CARDS;
			init[2] = player_id;
			os.writeObject(init);
			os.flush();
			System.out.println("sent init array" + init);
			// Send list of Players
			os.writeObject(parent.calcList2());
			os.flush();
			System.out.println("sent string names" + parent.calcList2());
			//Send Cards to the respective players
			
			int [] card_index = set_card_array();
			os.writeObject("cards");					
			os.flush();
			os.writeObject(card_index);
			os.flush();
			while (theSock !=null) 
			{
				
				os.writeObject("first");					
				os.flush();
				S.syncBid(this, 1);
				S2.converge(0, this);
				os.writeObject("second");					
				os.flush();
				S.syncBid(this, 2);
				S2.converge(0, this);
				
				S.syncTrump(this);
				while (trumpObject.game_over == false)
				{
					S.syncPlay(this);
					S2.converge(0, this);
				}
				parent.num_threads++;
				S2.converge(1, this);
				/*
				synchronize();
				os.writeObject("first");					
				os.flush();
				os.writeObject("bid");		
				os.flush();
				bid = (Integer)is.readObject();
				trumpObject.after_one_bid(bid, player_id);
				temp= trumpObject.cur_player;
				temp = (temp+1) % trumpObject.NO_OF_PLAYERS;
				trumpObject.cur_player = temp;
				
				synchronize();
				os.writeObject("second");					
				os.flush();
				os.writeObject("bid");		
				os.flush();
				bid = (Integer)is.readObject();
				trumpObject.after_one_bid(bid, player_id);
				if ( trumpObject.bidding_over == true)
				{
					trumpObject.cur_player = trumpObject.who_plays();	
				}
				else
				{
					temp= trumpObject.cur_player;
					temp = (temp+1) % trumpObject.NO_OF_PLAYERS;
					trumpObject.cur_player = temp;
				}
				synchronize();	
				int card_position = -1;
				Card card = null;
				int card_id;
				String play = "play";
				os.writeObject(play);
				os.flush();
				int validcard = 0;
				while( validcard != 1)
				{
					card_position= (Integer)is.readObject();
					card = trumpObject.player[player_id].getCard(card_position);
					validcard = trumpObject.checkValidCard(player_id, card, card_position);
					os.writeObject("valid");					
					os.flush();
					os.writeObject(validcard);
					os.flush();
				}
				parent.handleInput(card_position, this, card.index);         
				trumpObject.after_one_card(card_position, player_id);
				if (trumpObject.round_over == true)
				{
					sleep_time(500);
					parent.send_round_value();
					trumpObject.round_over = false;
				}

				if (trumpObject.game_over == true)
				{
					sleep_time(500);
					parent.send_game_value();
					trumpObject.game_over = false;
					parent.send_new_deal();
				}
					
				temp= trumpObject.cur_player;
				temp = (temp+1) % trumpObject.NO_OF_PLAYERS;
				trumpObject.cur_player = temp;
				*/
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception caught " +e+"\n");
			parent.theGroup.removeElement(this);
			parent.id_pool[player_id] = false;
			this.stop();
		}
	}

	public boolean message(String str)
	{
		/*
		try
		{
			ps.println(str);
		}
		catch (Exception e)
		{
			return false;
		}
		*/
		return true;
	}

	public void finalize()
	{
		try
		{
			is.close();
			os.close();
			theSock.close();
		}
		catch(Exception e)
		{

		}
		theSock = null;
	}

	public void setAlias(String str)
	{
		alias = str;
	}

	public String getAlias()
	{
		return alias;
	}

	public void synchronize()
	{
		while (trumpObject.cur_player != player_id)
		{
			sleep_time(500);
		}
		sleep_time(500);
		return;	
	}

	void sleep_time(int time)
	{
		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{

		}
	}

	public int [] set_card_array()
	{
		int [] card_array = new int[trumpObject.SIZE_OF_HAND];
			
		for ( int i = 0; i < trumpObject.SIZE_OF_HAND; i++ )
		{
			card_array[i] = trumpObject.player[player_id].getCard(i).index;
			//System.out.println("Card "+(i+1)+":"+card_array[i]);
		}
		
		return card_array;
	}

	void bid(int round)
	{
		try 
		{
			os.writeObject("bid");		
			os.flush();
			print("Thread"+player_id+"executing bid "+round);
			int bid = (Integer)is.readObject();
			print("Thread"+player_id+" received bid "+bid);
			trumpObject.after_one_bid(bid, player_id);
			int temp= trumpObject.cur_player;
			temp = (temp+1) % trumpObject.NO_OF_PLAYERS;
			trumpObject.cur_player = temp;
			parent.num_threads++;
		}
		catch (Exception e)
		{

		}
	}

	void setTrump()
	{
		try 
		{
			os.writeObject("settrump");		
			os.flush();
			int card_position = (Integer)is.readObject();
			Card card = trumpObject.player[player_id].getCard(card_position);
			trumpObject.trumpSet(card);
			parent.sendTrumpSet(this, card_position, player_id);
		}
		catch (Exception e)
		{

		}
	}
	
	void play()
	{
		try 
		{
			int card_position = -1;
			Card card = null;
			int card_id;
			String play = "play";
			os.writeObject(play);
			os.flush();
			print("Thread "+player_id+" playing");
			int validcard = 0;
			while( validcard != 1)
			{
				card_position= (Integer)is.readObject();
				card = trumpObject.player[player_id].getCard(card_position);
				validcard = trumpObject.checkValidCard(player_id, card, card_position);
				os.writeObject("valid");					
				os.flush();
				os.writeObject(validcard);
				os.flush();
			}
			parent.handleInput(card_position, this, card.index);         
			trumpObject.after_one_card(card_position, player_id);
			if (trumpObject.round_over == true)
			{
				parent.send_round_value();
				trumpObject.round_over = false;
				if (trumpObject.game_over == true)
				{
					parent.send_game_value();
					parent.send_new_deal();
				}
			}
			else
			{
				int temp= trumpObject.cur_player;
				temp = (temp+1) % trumpObject.NO_OF_PLAYERS;
				trumpObject.cur_player = temp;
			}
			print("Next player is "+trumpObject.cur_player);
			parent.num_threads++;
		}
		catch (Exception e)
		{

		}
	}

	
	void print(String str)
	{
		System.out.println(str);
	}

}
