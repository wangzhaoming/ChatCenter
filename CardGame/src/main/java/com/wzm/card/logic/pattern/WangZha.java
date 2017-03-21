package com.wzm.card.logic.pattern;

import java.util.List;

import com.wzm.card.logic.Card;

public class WangZha extends BasePattern {

	public WangZha(List<Card> cards) {
		super(cards);
		type = 10;
	}

	@Override
	public boolean validate(BasePattern o) {
		return false;
	}
}
