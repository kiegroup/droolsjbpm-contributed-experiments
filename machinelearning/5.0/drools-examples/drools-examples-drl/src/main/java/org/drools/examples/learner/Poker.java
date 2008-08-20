package org.drools.examples.learner;

import org.drools.learner.tools.ClassAnnotation;
import org.drools.learner.tools.FieldAnnotation;

@ClassAnnotation(label_element = "getLabel")
public class Poker {
	@FieldAnnotation(readingSeq = 0)
	private int s1; // 'Suit of card #1': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
	@FieldAnnotation(readingSeq = 1, discrete=false)
	private int c1; // 'Rank of card #1': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

	@FieldAnnotation(readingSeq = 2)
	private int s2; // 'Suit of card #2': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
	@FieldAnnotation(readingSeq = 3, discrete=false)
	private int c2; // 'Rank of card #2': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

	@FieldAnnotation(readingSeq = 4)
	private int s3; // 'Suit of card #3': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
	@FieldAnnotation(readingSeq = 5, discrete=false)
	private int c3; // 'Rank of card #3': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

	@FieldAnnotation(readingSeq = 6)
	private int s4; // 'Suit of card #4': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
	@FieldAnnotation(readingSeq = 7, discrete=false)
	private int c4; // 'Rank of card #4': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

	@FieldAnnotation(readingSeq = 8)
	private int s5; // 'Suit of card #5': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
	@FieldAnnotation(readingSeq = 9, discrete=false)
	private int c5; // 'Rank of card #5': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

	@FieldAnnotation(readingSeq = 10, ignore = true)//target=true)
	private int poker_hand;  
	/*
	 *0: Nothing in hand; not a recognized poker hand 
      1: One pair; one pair of equal ranks within five cards
      2: Two pairs; two pairs of equal ranks within five cards
      3: Three of a kind; three equal ranks within five cards
      4: Straight; five cards, sequentially ranked with no gaps
      5: Flush; five cards with the same suit
      6: Full house; pair + different rank three of a kind
      7: Four of a kind; four equal ranks within five cards
      8: Straight flush; straight + flush
      9: Royal flush; {Ace, King, Queen, Jack, Ten} + flush
	 */
	
	public Poker() {
		
	}
	
	public boolean getLabel() {
		return poker_hand>=5; 
	}
	
	public int getS1() {
		return s1;
	}
	
	public void setS1(int s1) {
		this.s1 = s1;
	}

	public int getC1() {
		return c1;
	}
	
	public void setC1(int c1) {
		this.c1 = c1;
	}

	public int getS2() {
		return s2;
	}
	
	public void setS2(int s2) {
		this.s2 = s2;
	}

	public int getC2() {
		return c2;
	}

	public void setC2(int c2) {
		this.c2 = c2;
	}

	public int getS3() {
		return s3;
	}

	public void setS3(int s3) {
		this.s3 = s3;
	}
	
	public int getC3() {
		return c3;
	}

	public void setC3(int c3) {
		this.c3 = c3;
	}

	public int getS4() {
		return s4;
	}

	public void setS4(int s4) {
		this.s4 = s4;
	}

	public int getC4() {
		return c4;
	}

	public void setC4(int c4) {
		this.c4 = c4;
	}

	public int getS5() {
		return s5;
	}

	public void setS5(int s5) {
		this.s5 = s5;
	}

	public int getC5() {
		return c5;
	}

	public void setC5(int c5) {
		this.c5 = c5;
	}

	public int getPoker_hand() {
		return poker_hand;
	}

	public void setPoker_hand(int poker_hand) {
		this.poker_hand = poker_hand;
	}
	
	

}
