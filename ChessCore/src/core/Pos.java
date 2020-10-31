package core;

import java.io.Serializable;

public class Pos implements Serializable, Cloneable {
	int _x, _y;

	public Pos() {
		super();
	}

	public Pos(int x, int y) {
		super();
		_x = x;
		_y = y;
	}

	@Override
	protected Object clone() {
		return new Pos(_x, _y);
	}

	@Override
	public boolean equals(Object o) {
		return ((Pos) o)._x == _x && ((Pos) o)._y == _y;
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}

	@Override
	public String toString() {
		return new String(
				new char[] { (char) (_x + 'a'), (char) (8 - _y + '0') });
	}
}
