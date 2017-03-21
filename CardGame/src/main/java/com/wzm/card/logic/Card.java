package com.wzm.card.logic;

public class Card implements Comparable<Card>{
	private int digit;//小王16,4，大王17,5
	private int suit;
	private int value;
	public static final int SPADE=0;
	public static final int HEART=1;
	public static final int DIAMOND=2;
	public static final int CLUB=3;

	public Card(int digit,int suit) {
		this.digit=digit;
		this.suit=suit;
		if (digit==1) {
			value=14;
		}else if(digit==2){
			value=15;
		}else {
			value=digit;
		}
		
	}

	public int getDigit() {
		return digit;
	}

	public int getSuit() {
		return suit;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==null) {
			return false;
		}
		
		if (obj instanceof Card) {
			Card c=(Card) obj;
			return digit==c.getDigit()&&suit==c.getSuit();
		}
		
		return false;
	}

	@Override
	public int compareTo(Card o) {
		if (value<o.getValue()) {
			return -1;
		}
		if (value>o.getValue()) {
			return 1;
		}
		if (suit<o.getSuit()) {
			return -1;
		}
		if (suit>o.getSuit()) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public String toString() {
//		String digitString=null;
//		String suitString=null;
//		
//		switch (digit) {
//		case 1:
//			digitString="A";
//			break;
//		case 11:
//			digitString="J";
//			break;
//		case 12:
//			digitString="Q";
//			break;
//		case 13:
//			digitString="K";
//			break;
//		case 16:
//			digitString="Joker";
//			break;
//		case 17:
//			digitString="Joker";
//			break;
//		default:
//			digitString=""+digit;
//			break;
//		}
//		
//		switch (suit) {
//		case 0:
//			suitString="SPADE";
//			break;
//		case 1:
//			suitString="HEART";
//			break;
//		case 2:
//			suitString="DIAMOND";
//			break;
//		case 3:
//			suitString="CLUB";
//			break;
//		case -1:
//			suitString="BLACK";
//			break;
//		case -2:
//			suitString="RED";
//			break;
//		}
		return suit+"-"+digit;
	}
}
