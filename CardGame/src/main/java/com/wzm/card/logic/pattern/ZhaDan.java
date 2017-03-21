package com.wzm.card.logic.pattern;

import java.util.List;

import com.wzm.card.logic.Card;

public class ZhaDan extends BasePattern {

	public ZhaDan(List<Card> cards) {
		super(cards);
		type = 4;
	}

	@Override
	public boolean validate(BasePattern o) {
		if (o.getType() == 10) {
			return true;
		}

		if (o.getType() == type) {
			if (cards.get(0).getValue() < o.getCards().get(0).getValue()) {
				return true;
			}
		}

		return false;
	}
}
