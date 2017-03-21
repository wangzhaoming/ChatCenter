package com.wzm.card.logic.pattern;

import java.util.List;

import com.wzm.card.logic.Card;

public class SanDaiEr extends BasePattern {

	public SanDaiEr(List<Card> cards) {
		super(cards);
		type=6;
	}

	@Override
	public boolean validate(BasePattern o) {
		if (o.getType() == 10) {
			return true;
		}
		if (o.getType() == 4) {
			return true;
		}

		if (o.getType() == type) {
			int[][] map = PatternFactory.generateMap(cards);
			int[][] map1 = PatternFactory.generateMap(o.getCards());

			int a = 0, b = 0;
			for (int i = 0; i < 2; i++) {
				if (map[i][1] == 3) {
					a = map[i][0];
				}
				if (map1[i][1] == 3) {
					b = map1[i][0];
				}
			}

			if (b > a) {
				return true;
			}
		}

		return false;
	}

}
