package chess.domain.board.coordinate;

import java.util.Arrays;

public enum Row {
	EIGHT(8),
	SEVEN(7),
	SIX(6),
	FIVE(5),
	FOUR(4),
	THREE(3),
	TWO(2),
	ONE(1);

	private final int value;

	Row(final int value) {
		this.value = value;
	}

	public Row move(int distance) {
		return Arrays.stream(values())
			.filter(row -> row.value == this.value + distance)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(""));
	}

	public int getValue() {
		return value;
	}
}