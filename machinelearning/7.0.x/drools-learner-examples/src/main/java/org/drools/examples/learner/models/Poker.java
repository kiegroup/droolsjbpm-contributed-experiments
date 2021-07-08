package org.drools.examples.learner.models;

import org.drools.learner.tools.ClassAnnotation;
import org.drools.learner.tools.FieldAnnotation;

@ClassAnnotation( labelElement = "getLabel" )
public class Poker {

    @FieldAnnotation( readingSeq = 0 )
    private int firstCardSuit; // 'Suit of card #1': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
    @FieldAnnotation( readingSeq = 1, discrete = false )
    private int firstCardRank; // 'Rank of card #1': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

    @FieldAnnotation( readingSeq = 2 )
    private int secondCardSuit; // 'Suit of card #2': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
    @FieldAnnotation( readingSeq = 3, discrete = false )
    private int secondCardRank; // 'Rank of card #2': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

    @FieldAnnotation( readingSeq = 4 )
    private int thirdCardSuit; // 'Suit of card #3': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
    @FieldAnnotation( readingSeq = 5, discrete = false )
    private int thirdCardRank; // 'Rank of card #3': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

    @FieldAnnotation( readingSeq = 6 )
    private int fourthCarSuit; // 'Suit of card #4': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
    @FieldAnnotation( readingSeq = 7, discrete = false )
    private int fourthCardRank; // 'Rank of card #4': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

    @FieldAnnotation( readingSeq = 8 )
    private int fifthCardSuit; // 'Suit of card #5': Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}
    @FieldAnnotation( readingSeq = 9, discrete = false )
    private int fifthCardRank; // 'Rank of card #5': Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)

    @FieldAnnotation( readingSeq = 10, target = true )
    private int classification;
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

    public int getFirstCardSuit() {
        return firstCardSuit;
    }

    public void setFirstCardSuit( int firstCardSuit ) {
        this.firstCardSuit = firstCardSuit;
    }

    public int getFirstCardRank() {
        return firstCardRank;
    }

    public void setFirstCardRank( int firstCardRank ) {
        this.firstCardRank = firstCardRank;
    }

    public int getSecondCardSuit() {
        return secondCardSuit;
    }

    public void setSecondCardSuit( int secondCardSuit ) {
        this.secondCardSuit = secondCardSuit;
    }

    public int getSecondCardRank() {
        return secondCardRank;
    }

    public void setSecondCardRank( int secondCardRank ) {
        this.secondCardRank = secondCardRank;
    }

    public int getThirdCardSuit() {
        return thirdCardSuit;
    }

    public void setThirdCardSuit( int thirdCardSuit ) {
        this.thirdCardSuit = thirdCardSuit;
    }

    public int getThirdCardRank() {
        return thirdCardRank;
    }

    public void setThirdCardRank( int thirdCardRank ) {
        this.thirdCardRank = thirdCardRank;
    }

    public int getFourthCarSuit() {
        return fourthCarSuit;
    }

    public void setFourthCarSuit( int fourthCarSuit ) {
        this.fourthCarSuit = fourthCarSuit;
    }

    public int getFourthCardRank() {
        return fourthCardRank;
    }

    public void setFourthCardRank( int fourthCardRank ) {
        this.fourthCardRank = fourthCardRank;
    }

    public int getFifthCardSuit() {
        return fifthCardSuit;
    }

    public void setFifthCardSuit( int fifthCardSuit ) {
        this.fifthCardSuit = fifthCardSuit;
    }

    public int getFifthCardRank() {
        return fifthCardRank;
    }

    public void setFifthCardRank( int fifthCardRank ) {
        this.fifthCardRank = fifthCardRank;
    }

    public int getClassification() {
        return classification;
    }

    public void setClassification( int classification ) {
        this.classification = classification;
    }

}