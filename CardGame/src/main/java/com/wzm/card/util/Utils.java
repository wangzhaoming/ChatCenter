package com.wzm.card.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import com.wzm.card.logic.Card;

public class Utils {
	public static List<JLabel> retriveLabelListFromMap(Map<JLabel, Card> map) {
		List<Card> cards = new ArrayList<Card>();
		List<JLabel> lbls = new ArrayList<JLabel>();
		for (JLabel lbl : map.keySet()) {
			cards.add(map.get(lbl));
		}
		Collections.sort(cards);

		for (int i = 0; i < cards.size(); i++) {
			for (JLabel lbl : map.keySet()) {
				if (map.get(lbl).equals(cards.get(i))) {
					lbls.add(lbl);
				}
			}
		}

		return lbls;
	}

	public static List<Card> retriveCardListFromMap(Map<JLabel, Card> map) {
		List<Card> cards = new ArrayList<Card>();
		for (JLabel lbl : map.keySet()) {
			cards.add(map.get(lbl));
		}
		Collections.sort(cards);

		return cards;
	}

	public static List<Card> retriveCardListFromString(String string) {
		List<Card> cards = new ArrayList<Card>();
		string = string.substring(1, string.length() - 1);

		if (string.length() > 0) {
			String[] cardString = string.split(",");
			for (String s : cardString) {
				String[] str = s.trim().split("-");
				cards.add(new Card(Integer.parseInt(str[1]), Integer
						.parseInt(str[0])));
			}
		}
		return cards;
	}

	public static String getLocalIP() {
		Enumeration<NetworkInterface> allNetInterfaces = null;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		InetAddress ip = null;

		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = allNetInterfaces.nextElement();

			Enumeration<InetAddress> addresses = netInterface
					.getInetAddresses();

			while (addresses.hasMoreElements()) {
				ip = addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					if (!ip.isLoopbackAddress()) {
						return ip.toString().substring(1);
					}
				}
			}
		}

		return "0.0.0.0";
	}

	public static URL getResourceURL(String name) {
		URL path = Utils.class.getResource(name);

		return path;
	}
}
