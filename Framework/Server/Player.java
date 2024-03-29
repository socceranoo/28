// Player.java - John K. Estell - 10 May 2003
// Last modified: 23 Febraury 2004
// Extension of the Hand class to implement the rules of the dumb card game.
package Server;

/**
 * Implementation of the rules for the dumb card game.
 */
public class Player extends Hand {
    /**
     * Creates a hand for the a Player.
     */
   
    public int bid;
    public Player() {
        super();
	  bid = 0;
    }
    
    /**
     * Evaluates a hand according to the rules of the dumb card game.
     * Each card is worth its displayed pip value (ace = 1, two = 2, etc.)
     * in points with face cards worth ten points.  The value of a hand
     * is equal to the summation of the points of all the cards held in
     * the hand.
*/
    public int evaluateHand() { 
	/*
        int value = 0;
        Rank.setKingHigh();
        for ( int i = 0; i < getNumberOfCards(); i++ ) {
            Card c = getCard( i );
            int cardValue = c.getRank().compareTo( Rank.ACE ) + 1;
            if ( cardValue > 10 )
               cardValue = 10;
            value += cardValue;
        }
        
        return value;
	*/
	return 0;
    }
}
            
