package org.gnet.util;

import java.util.ArrayList;

public class UIDGenerator {
	private final int min;
	private final int length;
	private final String string;
	private final ArrayList<Integer> list;

	public UIDGenerator(final int min, final String length) {
		this.min = min;
		this.length = length.length();
		string = length;
		list = new ArrayList<Integer>();
		// System.out.println("New UIDGenerator created. (length: " +
		// this.length + ")");
	}

	public int generateUID() {
		final String id = new RandomInt(0, Integer.parseInt(string))
				.generateRandom() + "";
		if (!(id.length() == length)) {
			// System.out.println("Regenerating, length!=length (" +
			// Integer.parseInt(id) + ")");
			// System.out.println("Error: " + id.length() + " <-> " + length);
			return generateUID();

		} else {
			final int idNum = Integer.parseInt(id);
			if (!list.contains(idNum)) {
				list.add(idNum);
				// System.out.println("ADDED NEW UID: " + idNum);
				return idNum;
			} else if (list.contains(idNum)) {
				// System.out.println("Regenerating, list.contains(id) (" +
				// idNum + ")");
				return generateUID();
			} else {
				return -1;
			}

		}
	}
}
