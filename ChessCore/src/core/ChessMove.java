package core;

import java.io.Serializable;

public class ChessMove implements ChessProtocol, Serializable {
	Pos[] _steps = new Pos[MAX];
	int _figure;
	int _numsteps;
	int _no;
	int _flags;
	ChessMove _extended;

	public ChessMove() {
	}

	public ChessMove(int figure, Pos from) {
		this(figure, from, null);
	}

	public ChessMove(int figure, Pos from, Pos to) {
		super();
		_figure = figure;
		addStep(from);
		if (to != null) {
			addStep(to);
		}
	}

	public void addFlags(int mask) {
		_flags |= mask;
	}

	public void addStep(Pos step) {
		_steps[_numsteps] = step;
		_numsteps++;
	}

	public ChessMove getCopy() {
		ChessMove move = new ChessMove();
		move._figure = _figure;
		System.arraycopy(_steps, 0, move._steps, 0, _numsteps);
		move._numsteps = _numsteps;
		return move;
	}

	public ChessMove getExtended() {
		return _extended;
	}

	public int getFigure() {
		return _figure;
	}

	public int getFlags() {
		return _flags;
	}

	public Pos getFrom() {
		return _steps[0];
	}

	public int getNo() {
		return _no;
	}

	public int getNumSteps() {
		return _numsteps;
	}

	public Pos getStep(int num) {
		return _steps[num];
	}

	public Pos getTo() {
		return _steps[_numsteps - 1];
	}

	void setExtended(ChessMove extended) {
		_extended = extended;
	}

	public void setNo(int no) {
		_no = no;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Integer.toString((_no + 1) / 2) + ". ");
		// sb.append((_figure & KIND) == WHITE ? "White " : "Black ");
		switch (_figure & 0xE) {
		case KING:
			sb.append("K");
			break;
		case QUEEN:
			sb.append("Q");
			break;
		case BISHOP:
			sb.append("B");
			break;
		case KNIGHT:
			sb.append("N");
			break;
		case ROOK:
			sb.append("R");
			break;
		}
		if (_extended != null) {
			sb.append(_extended.getFrom().getX() == 0 ? "0-0-0" : "0-0");
		} else {
			sb.append(getFrom());
			sb.append((_flags & INFO_FIGHTS) != 0 ? " x " : " - ");
			sb.append(getTo());
			if ((_flags & INFO_EP) != 0) {
				sb.append("e.p ");
			}
			if ((_flags & INFO_THREATEN) != 0) {
				sb.append("+");
			}
			if ((_flags & INFO_MATE) != 0) {
				sb.append("+");
			}
		}
		return sb.toString();
	}

}
