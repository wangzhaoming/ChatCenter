package com.wzm.chat.frame;

import javax.swing.JEditorPane;

public class ChatEditorPane extends JEditorPane {
	public ChatEditorPane() {
		super();
		setContentType("text/html; charset=utf-8");
	}

	public void append(String str) {
		StringBuffer sb = new StringBuffer(getText());
		int index = sb.indexOf("</body>");

		// sb.insert(index, "<p>"+str+"</p>");start://CardGame:10.0.5.177

		if (str.indexOf("http://") > -1) {
			sb.insert(index, generateHyperLink(str));
		} else if (str.indexOf("start://") > -1) {
			sb.insert(
					index,
					generateHyperLink(str,
							str.substring(str.lastIndexOf(":") + 1)));
		} else {
			sb.insert(index, str);
		}

		setText(sb.toString());
	}

	public void appendln() {
		append("<br />");
	}

	public void appendln(String str) {
		append(str);
		append("<br />");
	}

	private String generateHyperLink(String url, String txt) {
		return new StringBuffer().append("<a href='").append(url.trim()).append("'>")
				.append(txt).append("</a>").toString();
	}

	private String generateHyperLink(String url) {
		return new StringBuffer().append("<a href='").append(url.trim()).append("'>")
				.append(url).append("</a>").toString();
	}

	public void clear() {
		setText("");
	}
}
