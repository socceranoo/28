// Round.java - John K. Estell - 10 May 2003
// Last modified: 23 Febraury 2004
package Server;

/**
 * Implementation of the rules for the dumb card game.
 */
public class Round extends Hand 
{
	private int NO_OF_PLAYERS;
	private Suit base_suit;
	/**
	* Creates a Round.
	*/
	public Round(int a) 
	{
		super();
		NO_OF_PLAYERS = a;
	}

	/**
	* Evaluates a hand according to the rules of the dumb card game.
	* Each card is worth its displayed pip value (ace = 1, two = 2, etc.)
	* in points with face cards worth ten points.  The value of a hand
	* is equal to the summation of the points of all the cards held in
	* the hand.
	*/
	public int evaluateHand() 
	{
		int value = 0;
		Rank.setKingHigh();
		for ( int i = 0; i < getNumberOfCards(); i++ ) 
		{
			Card c = getCard( i );
			value += c.getRank().trumpValue;
		}

		return value;
	}

	public int whoPlaysNext(int a)
	{
		a++;		
		a%=NO_OF_PLAYERS;
		return a;
	}

	public void setFirstCard(Card card)
	{
		
		base_suit = card.suitValue;	
	}

	public int isValidCard(Card current_card)
	{
		return 1;
	}

	public void resetRound()
	{
		base_suit = null;
	}
}
            
