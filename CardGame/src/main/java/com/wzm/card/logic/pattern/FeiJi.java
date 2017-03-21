package com.wzm.card.logic.pattern;

import java.util.List;

import com.wzm.card.logic.Card;

public class FeiJi extends BasePattern {

	public FeiJi(List<Card> cards) {
		super(cards);
		type = 9;
	}

	public FeiJi(List<Card> cards, int type) {
		super(cards);
		this.type = type;
	}

	@Override
	public boolean validate(BasePattern o) {
		if (o.getType() == 10) {
			return true;
		}
		if (o.getType() == 4) {
			return true;
		}

		if (o.getType() == type && o.getCardNumber() == cards.size()) {
			int[][] map = PatternFactory.generateMap(cards);
			int[][] map1 = PatternFactory.generateMap(o.getCards());

			int a = 0, b = 0;
			while (map[a][1] != 3) {
				a++;
			}
			while (map1[b][1] != 3) {
				b++;
			}
			if (map[a][0] < map1[b][0]) {
				return true;
			}
		}

		return false;
	}

}
