package com.lyra.addon.utils;

import java.util.List;
import java.util.Random;

public class Uwuify {

	// Credits to github.com/Woodmaninator/AttmayMBBot/blob/8eb3a362bfe128e4f076a1aea4359597ac9ebec5/src/main/java/attmayMBBot/functionalities/UwUifyer.java
    // Its improved a bit to be more kawaii uwu >_<

	private static final String[] emojiArray = new String[] { " rawr x3", " rawr xD", " OwO", " UwU", " uwu", " o.O",
			" >w<", " (\u2445\u02D8\uA4B3\u02D8)", " (\uA20D\u1D17\\uA20D)", " (\u02D8\u03C9\u02D8)",
			" (U \u1D55 U\u2741)", " \u03C3\u03C9\u03C3", " ò\u03C9ó", " (U \uFE4F U)", " ( o \u03C9 o )",
			" \u0298w\u0298", " :3", " xD", " >_<", " \uD83D\uDE33", " \uD83E\uDD7A",
			" \uD83D\uDE33\uD83D\uDE33\uD83D\uDE33", " (\u273Fo\u03C9o)", " /(^•\u03C9•^)", "^•\uFECC•^ ",
			"(ˆ \uFECC ˆ)\u2661 ", " rawrrr", };

	public static String uwuify(String message) {
		int currentIndex = 0;
		StringBuilder stringBuilder = new StringBuilder();
		Random random = new Random();
		while (currentIndex < message.length()) {
			char currentChar = message.charAt(currentIndex);
			switch (currentChar) {
			case ' ':
				stringBuilder.append(' ');
				if (random.nextFloat() <= .05f)
					if (currentIndex + 1 < message.length())
						if (thisCharIsPartOfTheAlphabet(message.charAt(currentIndex + 1))) {
							stringBuilder.append(changeCharIfNecessary(message.charAt(currentIndex + 1)));
							stringBuilder.append('-');
							stringBuilder.append(changeCharIfNecessary(message.charAt(currentIndex + 1)));
							if (random.nextFloat() >= .5f) {
								stringBuilder.append('-');
								stringBuilder.append(changeCharIfNecessary(message.charAt(currentIndex + 1)));
							}
							currentIndex++;
						}
				currentIndex++;
				break;
			default:
				stringBuilder.append(changeCharIfNecessary(currentChar));
				currentIndex++;
				break;
			}
		}

		if (random.nextFloat() <= .9f)
			stringBuilder.append(" " + emojiArray[random.nextInt(emojiArray.length)]);

		return stringBuilder.toString().toLowerCase();
	}

	private static char changeCharIfNecessary(char c) {
		if (c == 'l' || c == 'r')
			return 'w';
		if (c == 'L' || c == 'R')
			return 'W';
		return c;
	}

	private static boolean thisCharIsPartOfTheAlphabet(char c) {
		return ((int) c >= 65 && (int) c <= 90) || ((int) c >= 97 && (int) c <= 122);
	}
}