package org.gnet.util;

public class RandomInt {

	private final int min;
	private final int max;

	public RandomInt(final int min, final int max) {
		this.min = min;
		this.max = max;
	}

	public int generateRandom() {
		final int number = createRandom();
		/*
		 * if (number < min) { number = generateRandom();
		 * System.out.println("REGENERATING"); } else if (number >= min) {
		 * return number; } return number;
		 */
		return number < min ? generateRandom() : number;
	}

	private final int createRandom() {
		return (int) (Math.random() * (max + 1));
	}

}
