package com.wzm.card.logic.pattern;

import java.util.ArrayList;
import java.util.List;

import com.wzm.card.logic.Card;

public class PatternFactory {
	public static BasePattern parsePattern(List<Card> cards) {
		int[][] map = generateMap(cards);

		if (cards.size() == 1) {
			return new One(cards);
		}
		if (cards.size() == 2) {
			if (map[0][1] == 2) {
				return new Two(cards);
			}
			if (map[0][0] == 16 && map[1][0] == 17) {
				return new WangZha(cards);
			}
			return null;
		}
		if (cards.size() == 3) {
			if (map[0][1] == 3) {
				return new Three(cards);
			}
			return null;
		}
		if (cards.size() == 4) {
			if (map[0][1] == 4) {
				return new ZhaDan(cards);
			}
			if (map[0][1] == 3 || map[1][1] == 3) {
				return new SanDaiYi(cards);
			}
			return null;
		}
		if (cards.size() == 5) {
			if (map[0][1] == 3 || map[1][1] == 3) {
				return new SanDaiEr(cards);
			}
		}
		if (isShunZi(map, cards.size())) {
			return new ShunZi(cards);
		}
		if (isLianDui(map, cards.size())) {
			return new LianDui(cards);
		}

		int flag = isFeiJi(map, cards.size());
		if (flag != -1) {
			return new FeiJi(cards, flag);
		}

		return null;
	}

	private static boolean isShunZi(int[][] map, int n) {
		if (map[n - 1][0] < 15) {
			boolean flag = true;
			for (int i = 1; i < n; i++) {
				if (map[i][0] != (map[i - 1][0] + 1)) {
					flag = false;
					break;
				}
			}
			return flag;
		}
		return false;
	}

	private static int isFeiJi(int[][] map, int n) {
		int i = 0;
		List<Integer> three = new ArrayList<Integer>();
		List<Integer> two = new ArrayList<Integer>();
		List<Integer> one = new ArrayList<Integer>();
		while (map[i][1] > 0) {
			if (map[i][1] == 3) {
				three.add(map[i][0]);
			} else if (map[i][1] == 2) {
				two.add(map[i][0]);
			} else if (map[i][1] == 1) {
				one.add(map[i][0]);
			}
			i++;
		}
		if (three.get(three.size() - 1) < 15) {
			boolean flag = true;
			for (int j = 1; j < three.size(); j++) {
				if (three.get(j) != three.get(j - 1) + 1) {
					flag = false;
					break;
				}
			}
			if (flag) {
				if (two.size() == three.size()
						&& two.size() * 2 + three.size() * 3 == n) {
					return 12;
				}
				if (one.size() == three.size()
						&& one.size() + three.size() * 3 == n) {
					return 11;
				}
				if (three.size() * 3 == n) {
					return 9;
				}
			}
		}
		return -1;
	}

	private static boolean isLianDui(int[][] map, int n) {
		if (n % 2 == 0) {
			if (map[n / 2 - 1][0] < 15) {
				boolean flag = true;
				for (int i = 0; i < n / 2; i++) {
					if (map[i][1] != 2
							|| (i > 0 && map[i][0] != (map[i - 1][0] + 1))) {
						flag = false;
						break;
					}
				}
				return flag;
			}
		}
		return false;
	}

	public static int[][] generateMap(List<Card> cards) {
		int[][] map = new int[15][2];

		int i = 0;
		for (Card card : cards) {
			if (map[i][0] != 0 && map[i][0] != card.getValue()) {
				i++;
			}
			map[i][0] = card.getValue();
			map[i][1]++;
		}

		return map;
	}
}
