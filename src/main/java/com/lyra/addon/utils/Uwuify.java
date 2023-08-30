package com.lyra.addon.utils;

import java.util.Random;

public class Uwuify {

	// Credits to https://github.com/Woodmaninator/AttmayMBBot/blob/main/src/main/java/attmayMBBot/functionalities/UwUifyer.java
    // Its improved a bit to be more kawaii uwu >_<

	private static final String[] emojiArray = new String[] { " rawr x3", " rawr xD", " OwO", " UwU", " uwu", " o.O",
			" >w<", " (⑅˘꒳˘)", " (˘ω˘)",
			" (U ᵕ U❁)", " σωσ", " òωó", " (U ﹏ U)", " ( o ω o )",
			" ʘwʘ", " :3", " xD", " >_<", " (✿oωo)", " (^•ω•^)", " ^•ﻌ•^ ",
			" (ˆ ﻌ ˆ)♡ ", " rawrrr", };

	public static String uwuify(String message, boolean addEmoji) {
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

		if (addEmoji)
			stringBuilder.append(emojiArray[random.nextInt(emojiArray.length)]);

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
