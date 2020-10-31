package core;

import java.io.Serializable;
import java.util.Vector;


public class GameModel implements ChessProtocol, Serializable {
	BoardModel _board;
	public final static int NONE = 0;
	public final static int FORWARD = 1;
	public final static int BACKWARD = 2;
	public final static int LEFT = 3;
	public final static int RIGHT = 4;
	Vector _validmoves = new Vector();
	int _mover;
	int _moveNo;
	boolean[] _kingmoved = new boolean[2];
	boolean[][] _rookmoved = new boolean[2][2];
	Vector[] _moves = new Vector[2];

	public GameModel() {
		super();
	}

	void addMoves(Vector moves) {
		if (moves != null) {
			for (int i = 0; i < moves.size(); i++) {
				_validmoves.addElement(moves.elementAt(i));
			}
		}
	}

	public boolean checkDLine(BoardModel board, int srcx, int srcy, int dstx,
			int dsty, int kind) {
		int deltax, deltay, delta, x, y;
		deltax = dstx - srcx;
		deltay = dsty - srcy;
		if (deltax != 0 && deltay != 0 && Math.abs(deltax) == Math.abs(deltay)) {
			deltax = dstx > srcx ? 1 : -1;
			deltay = dsty > srcy ? 1 : -1;
			x = srcx + deltax;
			y = srcy + deltay;
			while (isValidPos(x, y)) {
				if (x == dstx && y == dsty) {
					return board.isEmpty(x, y) || isFight(board, kind, x, y);
				}
				if (!board.isEmpty(x, y)) {
					break;
				}
				x += deltax;
				y += deltay;
			}
		}
		return false;
	}

	void checkEPMove(int pawn, int x, int y) {
		if ((pawn & 0xE) != PAWN) {
			return;
		}
		int kind = pawn & KIND;
		if (getRow(y, kind) == 5) {
			ChessMove prevMove = getLastMove((kind + 1) % 2);
			if ((prevMove.getFigure() & 0xE) == PAWN
					&& Math.abs(prevMove.getTo().getY()
							- prevMove.getFrom().getY()) == 2
							&& Math.abs(prevMove.getTo().getX() - x) == 1) {
				ChessMove move = new ChessMove(pawn, new Pos(x, y));
				move.addStep(new Pos(prevMove.getTo().getX(), prevMove.getTo()
						.getY() - prevMove.getFrom().getY() > 0 ? y - 1 : y + 1));
				move.addFlags(INFO_EP);
				if (!isThreatenFor(move, kind)) {
					_validmoves.addElement(move);
				}
			}
		}
	}

	public boolean checkHLine(BoardModel board, int srcx, int srcy, int dstx,
			int dsty, int kind) {
		int deltax, deltay, delta, x, y;
		deltax = dstx - srcx;
		deltay = dsty - srcy;
		if (deltay == 0 && deltax != 0) {
			deltax = dstx > srcx ? 1 : -1;
			x = srcx + deltax;
			y = srcy;
			while (isValidPos(x, y)) {
				if (x == dstx && y == dsty) {
					return board.isEmpty(x, y) || isFight(board, kind, x, y);
				}
				if (!board.isEmpty(x, y)) {
					break;
				}
				x += deltax;
			}
		}
		return false;
	}

	void checkKnightMove(int figure, int srcx, int srcy, int dir, int dirmod) {
		if ((figure & 0xE) != KNIGHT) {
			return;
		}
		ChessMove move = new ChessMove(figure, new Pos(srcx, srcy));
		int x, y;
		int kind = getKind(figure);
		int delta = kind == WHITE ? -1 : 1;
		x = srcx;
		y = srcy;
		switch (dir) {
		case FORWARD:
			y += delta * 2;
			break;
		case BACKWARD:
			y -= delta * 2;
			break;
		case LEFT:
			x -= delta * 2;
			break;
		case RIGHT:
			x += delta * 2;
			break;
		}
		switch (dirmod) {
		case FORWARD:
			y += delta * 1;
			break;
		case BACKWARD:
			y -= delta * 1;
			break;
		case LEFT:
			x -= delta * 1;
			break;
		case RIGHT:
			x += delta * 1;
			break;
		}
		if (isValidPos(x, y)) {
			move.addStep(new Pos(x, y));
			if (!isThreatenFor(move, kind)) {
				if (!_board.isEmpty(x, y)) {
					if (isFight(_board, kind, x, y)) {
						_validmoves.addElement(move);
					}
				} else {
					_validmoves.addElement(move);
				}
			}
		}
	}

	void checkLongCastleMove(int kind) {
		Pos kingpos = _board.getKingPos(kind);
		int x = kingpos.getX();
		int y = kingpos.getY();
		if (_board.getFigure(4, getY(1, kind)) == KING + kind
				&& !_kingmoved[kind]) {
			if (_board.getFigure(0, getY(1, kind)) == ROOK + kind
					&& !_rookmoved[kind][0]) {
				ChessMove move = new ChessMove(KING + kind, kingpos);
				move.setExtended(new ChessMove(ROOK + kind, new Pos(0, y),
						new Pos(3, y)));
				for (int i = 3; i > 0; i--) {
					if (!_board.isEmpty(i, y)) {
						return;
					}
				}
				if (!isThreatenFor(move, kind)) {
					for (int i = 3; i > 1; i--) {
						move.addStep(new Pos(i, y));
						if (isThreatenFor(move, kind)) {
							return;
						}
					}
				}
				if (move != null) {
					_validmoves.addElement(move);
				}
			}
		}
	}

	void checkMoves(int x, int y) {
		int figure = _board.getFigure(x, y);
		int kind = getKind(figure);
		switch (figure & 0x0E) {
		case PAWN: {
			checkValidMoves(figure, x, y, FORWARD, NONE, 1, 0);
			if (getRow(y, kind) == 2) {
				checkValidMoves(figure, x, y, FORWARD, NONE, 2, 0);
			}
			checkValidMoves(figure, x, y, FORWARD, LEFT, 1, CAN_FIGHT
					| MUST_FIGHT);
			checkValidMoves(figure, x, y, FORWARD, RIGHT, 1, CAN_FIGHT
					| MUST_FIGHT);
			checkEPMove(figure, x, y);
			break;
		}
		case ROOK: {
			checkValidMoves(figure, x, y, FORWARD, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, LEFT, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, RIGHT, NONE, MAX, CAN_FIGHT);
			break;
		}
		case KNIGHT: {
			checkKnightMove(figure, x, y, FORWARD, LEFT);
			checkKnightMove(figure, x, y, FORWARD, RIGHT);
			checkKnightMove(figure, x, y, BACKWARD, LEFT);
			checkKnightMove(figure, x, y, BACKWARD, RIGHT);
			checkKnightMove(figure, x, y, LEFT, FORWARD);
			checkKnightMove(figure, x, y, LEFT, BACKWARD);
			checkKnightMove(figure, x, y, RIGHT, FORWARD);
			checkKnightMove(figure, x, y, RIGHT, BACKWARD);
			break;
		}
		case BISHOP: {
			checkValidMoves(figure, x, y, FORWARD, LEFT, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, FORWARD, RIGHT, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, LEFT, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, RIGHT, MAX, CAN_FIGHT);
			break;
		}
		case QUEEN: {
			checkValidMoves(figure, x, y, FORWARD, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, LEFT, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, RIGHT, NONE, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, FORWARD, LEFT, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, FORWARD, RIGHT, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, LEFT, MAX, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, RIGHT, MAX, CAN_FIGHT);
			break;
		}
		case KING: {
			checkValidMoves(figure, x, y, FORWARD, NONE, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, NONE, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, LEFT, NONE, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, RIGHT, NONE, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, FORWARD, LEFT, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, FORWARD, RIGHT, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, LEFT, 1, CAN_FIGHT);
			checkValidMoves(figure, x, y, BACKWARD, RIGHT, 1, CAN_FIGHT);
			checkLongCastleMove(kind);
			checkShortCastleMove(kind);
			break;
		}
		}
	}

	void checkShortCastleMove(int kind) {
		Pos kingpos = _board.getKingPos(kind);
		int x = kingpos.getX();
		int y = kingpos.getY();
		if (_board.getFigure(4, getY(1, kind)) == KING + kind
				&& !_kingmoved[kind]) {
			if (_board.getFigure(7, getY(1, kind)) == ROOK + kind
					&& !_rookmoved[kind][1]) {
				ChessMove move = new ChessMove(KING + kind, kingpos);
				move.setExtended(new ChessMove(ROOK + kind, new Pos(7, y),
						new Pos(5, y)));
				for (int i = 5; i < 7; i++) {
					if (!_board.isEmpty(i, y)) {
						return;
					}
				}
				if (!isThreatenFor(move, kind)) {
					for (int i = 5; i < 7; i++) {
						move.addStep(new Pos(i, y));
						if (isThreatenFor(move, kind)) {
							return;
						}
					}
				}
				if (move != null) {
					_validmoves.addElement(move);
				}
			}
		}
	}

	void checkValidMoves(int figure, int srcx, int srcy, int dir, int dirmod,
			int amount, int options) {
		ChessMove move = new ChessMove(figure, new Pos(srcx, srcy));
		int x, y;
		int kind = getKind(figure);
		int delta = kind == WHITE ? -1 : 1;
		x = srcx;
		y = srcy;
		for (int i = 0; i < amount; i++) {
			switch (dir) {
			case FORWARD:
				y += delta;
				break;
			case BACKWARD:
				y -= delta;
				break;
			case LEFT:
				x -= 1;
				break;
			case RIGHT:
				x += 1;
				break;
			}
			switch (dirmod) {
			case LEFT:
				x -= 1;
				break;
			case RIGHT:
				x += 1;
				break;
			}
			if (!isValidPos(x, y)) {
				break;
			}
			move.addStep(new Pos(x, y));
			if (!_board.isEmpty(x, y)) {
				if ((options & CAN_FIGHT) != 0 && isFight(_board, kind, x, y)
						&& !isThreatenFor(move, kind)) {
					_validmoves.addElement(move);
				}
				break;
			}
			if ((options & MUST_FIGHT) != 0) {
				if (isFight(_board, kind, x, y) && !isThreatenFor(move, kind)) {
					_validmoves.addElement(move);
				}
				break;
			}
			if (!isThreatenFor(move, kind)) {
				_validmoves.addElement(move);
				move = move.getCopy();
			}
		}
	}

	public boolean checkVLine(BoardModel board, int srcx, int srcy, int dstx,
			int dsty, int kind) {
		int deltax, deltay, delta, x, y;
		deltax = dstx - srcx;
		deltay = dsty - srcy;
		if (deltax == 0 && deltay != 0) {
			deltay = dsty > srcy ? 1 : -1;
			x = srcx;
			y = srcy + deltay;
			while (isValidPos(x, y)) {
				if (x == dstx && y == dsty) {
					return board.isEmpty(x, y) || isFight(board, kind, x, y);
				}
				if (!board.isEmpty(x, y)) {
					break;
				}
				y += deltay;
			}
		}
		return false;
	}

	public void doMove(ChessMove move) {
		if (!_board.isEmpty(move.getTo())) {
			move.addFlags(INFO_FIGHTS);
		}
		_board.doMove(move);
		if (move.getExtended() != null) {
			_board.doMove(move.getExtended());
		}
		int kind = getKind(move.getFigure());
		switch (move.getFigure() & 0xE) {
		case KING: {
			_kingmoved[kind] = true;
			break;
		}
		case ROOK: {
			switch (move.getFrom().getX()) {
			case 0:
				_rookmoved[kind][0] = true;
				break;
			case 7:
				_rookmoved[kind][1] = true;
				break;
			}
			break;
		}
		case PAWN: {
			if (getRow(move.getTo().getY(), kind) == 8) {
				_board.setFigure(QUEEN + kind, move.getTo());
			}
			break;
		}
		}
		if (isThreatenFor(move, (kind + 1) % 2)) {
			move.addFlags(INFO_THREATEN);
		}
		_moveNo++;
		move.setNo(_moveNo);
		_moves[kind].addElement(move);
	}

	public BoardModel getBoard() {
		return _board;
	}

	public int getCol(int x, int kind) {
		return kind == BLACK ? 8 - x : x + 1;
	}

	public int[][] getHintFor(int x, int y) {
		int[][] hint = new int[8][8];
		for (int i = 0; i < _validmoves.size(); i++) {
			ChessMove move = (ChessMove) _validmoves.elementAt(i);
			if (move.getFrom().getX() == x && move.getFrom().getY() == y) {
				for (int j = 0; j < move.getNumSteps(); j++) {
					Pos step = move.getStep(j);
					hint[step.getY()][step.getX()] = 1;
				}
			}
		}
		return hint;
	}

	public int getKind(int figure) {
		return figure & KIND;
	}

	public ChessMove getLastMove(int kind) {
		if (_moves[kind].size() > 0) {
			return (ChessMove) _moves[kind].lastElement();
		} else {
			return null;
		}
	}

	public ChessMove getMoveFromTo(Pos from, Pos to) {
		ChessMove move;
		for (int i = 0; i < _validmoves.size(); i++) {
			move = (ChessMove) _validmoves.elementAt(i);
			if (move.getFrom().equals(from) && move.getTo().equals(to)) {
				return move;
			}
		}
		return null;
	}

	public int getMoveNo() {
		return _moveNo;
	}

	public int getMover() {
		return _mover;
	}

	public int getRow(int y, int kind) {
		return kind == BLACK ? y + 1 : 8 - y;
	}

	public int getX(int col, int kind) {
		return kind == BLACK ? 8 - col : col - 1;
	}

	public int getY(int row, int kind) {
		return kind == BLACK ? row - 1 : 8 - row;
	}

	public boolean isFight(BoardModel board, int kind, int x, int y) {
		return !board.isEmpty(x, y) && kind != getKind(board.getFigure(x, y));
	}

	public boolean isGameOver() {
		return _moveNo > 0 && _validmoves.size() == 0;
	}

	public boolean isThreatenFor(ChessMove move, int kind) {
		int figure;
		BoardModel board = _board.getCopy();
		board.doMove(move);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (!board.isEmpty(j, i)) {
					figure = board.getFigure(j, i);
					if (getKind(figure) != kind
							&& isValidFromTo(board, figure, new Pos(j, i),
									board.getKingPos(kind))) {
						return true;
					}
				}
			}
		}
		Pos white = board.getKingPos(WHITE);
		Pos black = board.getKingPos(BLACK);
		if (Math.abs(white.getX() - black.getX()) < 2
				&& Math.abs(white.getY() - black.getY()) < 2) {
			return true;
		}
		return false;
	}

	public boolean isValidFromTo(BoardModel board, int figure, Pos from, Pos to) {
		int deltax, deltay;
		int kind = getKind(figure);
		int dstx = to.getX();
		int dsty = to.getY();
		int srcx = from.getX();
		int srcy = from.getY();
		switch (figure & 0xE) {
		case PAWN: {
			deltax = Math.abs(dstx - srcx);
			deltay = getRow(dsty, kind) - getRow(srcy, kind);
			// if (getRow(srcx, kind) == 5) {
			// Move prevMove = getLastMove((kind + 1) % 2);
			// if (prevMove != null && prevMove.getFigure() == PAWN &&
			// Math.abs(prevMove.getTo().getY() - prevMove.getFrom().getY()) ==
			// 2 && Math.abs(prevMove.getTo().getX() - srcx) == 1)

			// }
			if (isFight(board, kind, dstx, dsty)) {
				return deltax == 1 && deltay == 1;
			}
			if (!board.isEmpty(dstx, dsty) || deltax != 0) {
				return false;
			}
			if (getRow(srcx, kind) == 2 && deltay == 2) {
				return true;
			}
			return deltay == 1;
		}
		case KNIGHT: {
			deltax = Math.abs(dstx - srcx);
			deltay = Math.abs(dsty - srcy);
			if ((deltax == 2 && deltay == 1) || (deltax == 1 && deltay == 2)) {
				return board.isEmpty(dstx, dsty)
						|| isFight(board, kind, dstx, dsty);
			}
			break;
		}
		case BISHOP: {
			return checkDLine(board, srcx, srcy, dstx, dsty, figure);
		}
		case ROOK: {
			return checkHLine(board, srcx, srcy, dstx, dsty, figure)
					|| checkVLine(board, srcx, srcy, dstx, dsty, figure);
		}
		case QUEEN: {
			return checkDLine(board, srcx, srcy, dstx, dsty, figure)
					|| checkHLine(board, srcx, srcy, dstx, dsty, figure)
					|| checkVLine(board, srcx, srcy, dstx, dsty, figure);
		}
		case KING: {
			return false;
		}
		}
		return false;
	}

	public boolean isValidPos(int x, int y) {
		return x >= 0 && x <= 7 && y >= 0 && y <= 7;
	}

	public String moveToString(ChessMove move) {
		StringBuffer sb = new StringBuffer();
		// if (getKind(move.getFigure()) == WHITE)
		sb.append(move.getFrom().toString());
		sb.append(" - ");
		sb.append(move.getTo().toString());
		return sb.toString();
	}

	public void prepare() {
		_board = new BoardModel();
		_board.reset();
		_moves[WHITE] = new Vector();
		_moves[BLACK] = new Vector();
		setMover(WHITE);
	}

	public void setMover(int kind) {
		_validmoves.removeAllElements();
		_mover = kind;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (!_board.isEmpty(j, i)
						&& getKind(_board.getFigure(j, i)) == kind) {
					checkMoves(j, i);
				}
			}
		}
		if (_validmoves.size() == 0) {
			System.out.println("GAME OVER!");
		}
	}

}
