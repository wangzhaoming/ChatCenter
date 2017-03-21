package com.wzm.card.logic.pattern;

import java.util.List;

import com.wzm.card.logic.Card;

public class LianDui extends BasePattern {

	public LianDui(List<Card> cards) {
		super(cards);
		type = 8;
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
			if (cards.get(0).getValue() < o.getCards().get(0).getValue()) {
				return true;
			}
		}

		return false;
	}

}
