// Trump.java - John K. Estell - 10 May 2003
// Last modified: 23 Febraury 2004
// Demonstration program for the basic extension of the Hand class

package Server;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.net.*;

public class Trump 
{
	// Constants
	public final int TOTAL_SUITS = 4, TOTAL_RANKS = 13;
	public final int TOTAL_CARDS = 16, NO_OF_PLAYERS = 4;
	public final int SIZE_OF_HAND = TOTAL_CARDS/NO_OF_PLAYERS;
	public final int MAX_PLAYERS = 8;
	/*
	private final int card_spacing = 25, dist = 30, margin = 12;
	private final int frame_width = 1024, frame_height = 800, frame_height_actual = 768;
	private final int card_width = 72, card_height = 96;
	private final int button_width = 150, button_height = 20;
	private final String directory = "cardgame/images/";
	private final String [] names = {"North", "East", "South", "West"};
	private final String [] button_text = {"New Game", "Button 2", "Button 3"};
	private final String [] button_action_command = {"New Game", "sort", "sort"};
	*/

	// card related Objects
	public Deck cardDeck;
	public Player [] player;
	public Round round;
	public Card trump;

	//Static variables
	public int dealer;
	public int cur_player;
	public int num_cards_table;
	public int num_of_bids;
	public int round_score, game_score;
	public int target_score;
	public boolean round_over;
	public boolean game_over;
	public boolean bidding_over;
	public ClassLoader cldr;

	public Trump()
	{
		cldr = this.getClass().getClassLoader();
		//INITIATE CARD DECK
		initiate_cardDeck();
		//Initiate the number of players.	
		player = new Player[NO_OF_PLAYERS];
		for ( int k = 0; k < NO_OF_PLAYERS; k++ ) 
		{
			player[k] = new Player();
		}
		// Initiate the round 	
		new_deal(true);
		round = new Round(NO_OF_PLAYERS);
		dealer = 0;
		num_cards_table = 0;
		num_of_bids = 0;
		cur_player = dealer;
		round_over = false;
		game_over = false;
		bidding_over = false;
		target_score=24;
	}

	void new_deal(boolean brand_new)
	{
		trump = null;
		if (brand_new == false)
		{
			cardDeck.restoreDeck();
			cardDeck.shuffle();
		}
		for ( int k = 0; k< NO_OF_PLAYERS; k++)
		{
			player[k].discardHand();
			for ( int i = 0; i < SIZE_OF_HAND; i++ ) 
			{
				Card c = cardDeck.dealCard();
				player[k].addCard( c );
			}
		}
	}


	void after_one_card(int card_pos, int player_id)
	{
		num_cards_table++;
		Card card =player[player_id].getCard(card_pos); 
		round.addCard(card);
		int firstCard = round.getNumberOfCards();
		if (firstCard == 1)
		{
			round.setFirstCard(card);
		}
		//System.out.println("Player :"+player_id+" has put the card :"+card.index+"");
		if (num_cards_table % NO_OF_PLAYERS == 0)
		{
			after_one_round();
			round_over = true;	
		}

		if (num_cards_table == (NO_OF_PLAYERS * SIZE_OF_HAND))
		{
			after_one_game();
			game_over = true;
		}
		//int temp = cur_player;
		//temp++;
		//temp = temp % NO_OF_PLAYERS;
		//cur_player = temp;
	}

	void after_one_round()
	{
		//cur_player = round.whoPlaysNext(cur_player);
		round_score = round.evaluateHand();	
		round.discardHand();
		round.resetRound();
	}

	void after_one_game()
	{
		//cur_player = dealer;
		num_cards_table = 0;
		game_score = 13;
		new_deal(false);
	}

	void after_one_bid(int bid, int player_id)
	{
		num_of_bids++;
		player[player_id].bid = bid;
		if (num_of_bids == NO_OF_PLAYERS * 2)
		{
			bidding_over = true;
		}
	}

	void trumpSet(Card trumpCard)
	{
		trump = trumpCard;
	}

	int who_plays()
	{
		int next_player = 0;
		int cur_bid = player[0].bid;
		for ( int k = 0; k< NO_OF_PLAYERS; k++)
		{
			if (player[k].bid > cur_bid)
			{
				cur_bid = player[k].bid;
				next_player = k;
			}
		}
		num_of_bids = 0;
		target_score = cur_bid;
		return next_player;
	}

	int checkValidCard(int player_id, Card card, int card_position)
	{
		return round.isValidCard(card);	
	}
		
	// add the Card instantiations here
	void initiate_cardDeck()
	{
		cardDeck = new Deck();
		Iterator suitIterator = Suit.VALUES.iterator();
		while ( suitIterator.hasNext() ) 
		{
			Suit suit = (Suit) suitIterator.next();
			Iterator rankIterator = Rank.VALUES.iterator();
			while ( rankIterator.hasNext() ) 
			{
				Rank rank = (Rank) rankIterator.next();
				//String imageFile = directory + Card.getFilename( suit, rank );
				//ImageIcon cardImage = new ImageIcon( getImage( getCodeBase(), imageFile ) );
				ImageIcon cardImage = null; 
				//URL imageURL = cldr.getResource(imageFile);
				//ImageIcon cardImage = new ImageIcon(imageURL);
				Card card = new Card( suit, rank, cardImage );
				cardDeck.addCard( card );
				//System.out.println(""+card.index);
			}
		}
	}
	/*
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


	*/
} //class ends here
