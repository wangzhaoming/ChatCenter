package com.wzm.card.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Cards {
	private List<Card> cards = new ArrayList<Card>();
	private List<Card> cards1 = new ArrayList<Card>();
	private List<Card> cards2 = new ArrayList<Card>();
	private List<Card> cards3 = new ArrayList<Card>();

	public static final int CARDS_NUM_PER_PERSON = 17;

	public Cards() {
		cards.add(new Card(16, 4));
		cards.add(new Card(17, 5));
		for (int i = 1; i <= 13; i++) {
			for (int j = 0; j < 4; j++)
				cards.add(new Card(i, j));
		}
		generateCards(cards1);
		generateCards(cards2);
		generateCards(cards3);
	}

	private void generateCards(List<Card> c) {
		Random r = new Random();
		for (int i = 0; i < CARDS_NUM_PER_PERSON; i++) {
			r.setSeed(System.currentTimeMillis());
			r.nextInt();
			int n = r.nextInt(cards.size());
			c.add(cards.get(n));
			cards.remove(n);
		}
		Collections.sort(c);
	}

	public List<Card> getCards(int i) {
		switch (i) {
		case 0:
			return cards1;
		case 1:
			return cards2;
		case 2:
			return cards3;
		}
		
		return null;
	}

	public List<Card> getHiddenCards() {
		return cards;
	}

}
