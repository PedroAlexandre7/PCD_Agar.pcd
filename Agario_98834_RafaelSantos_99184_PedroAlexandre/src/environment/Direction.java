package environment;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public enum Direction implements Serializable {
	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

	private Coordinate vector;

	Direction(int x, int y) {
		vector = new Coordinate(x, y);
	}

	public Coordinate getVector() {
		return vector;
	}
	
	public static Direction randomDirection() {
		int random = ThreadLocalRandom.current().nextInt(values().length);
		return values()[random];
	}
}
