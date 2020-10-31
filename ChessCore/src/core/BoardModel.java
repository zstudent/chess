package core;

import java.io.Serializable;

public class BoardModel implements ChessProtocol, Serializable {
	int[][] _board = new int[8][8];
	Pos[] _kingpos = new Pos[2];
	static int[][] INITIAL_FIELD = new int[][] {
		{ BLACK + ROOK, BLACK + KNIGHT, BLACK + BISHOP, BLACK + QUEEN,
			BLACK + KING, BLACK + BISHOP, BLACK + KNIGHT, BLACK + ROOK },
			{ BLACK + PAWN, BLACK + PAWN, BLACK + PAWN, BLACK + PAWN,
				BLACK + PAWN, BLACK + PAWN, BLACK + PAWN, BLACK + PAWN },
				{ NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE },
				{ NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE },
				{ NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE },
				{ NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE },
				{ WHITE + PAWN, WHITE + PAWN, WHITE + PAWN, WHITE + PAWN,
					WHITE + PAWN, WHITE + PAWN, WHITE + PAWN, WHITE + PAWN },
					{ WHITE + ROOK, WHITE + KNIGHT, WHITE + BISHOP, WHITE + QUEEN,
						WHITE + KING, WHITE + BISHOP, WHITE + KNIGHT, WHITE + ROOK } };

	public BoardModel() {
		super();
	}

	void doMove(ChessMove move) {
		setFigure(NONE, move.getFrom());
		setFigure(move.getFigure(), move.getTo());
		if ((move.getFlags() & INFO_EP) != 0) {
			setFigure(NONE, move.getTo().getX(), move.getFrom().getY());
		}
	}

	public BoardModel getCopy() {
		BoardModel model = new BoardModel();
		model._board = new int[8][8];
		for (int i = 0; i < 8; i++) {
			System.arraycopy(_board[i], 0, model._board[i], 0, 8);
		}
		model._kingpos[0] = _kingpos[0];
		model._kingpos[1] = _kingpos[1];
		return model;
	}

	public int getFigure(int x, int y) {
		return _board[y][x];
	}

	Pos getKingPos(int kind) {
		return _kingpos[kind];
	}

	public boolean isEmpty(int x, int y) {
		return _board[y][x] == 0;
	}

	public boolean isEmpty(Pos pos) {
		return isEmpty(pos.getX(), pos.getY());
	}

	public void reset() {
		for (int j = 0; j < 8; j++) {
			for (int i = 0; i < 8; i++) {
				setFigure(INITIAL_FIELD[j][i], i, j);
			}
		}
	}

	public void setFigure(int figure, int x, int y) {
		_board[y][x] = figure;
		if ((figure & 0xE) == KING) {
			_kingpos[figure & KIND] = new Pos(x, y);
		}
	}

	public void setFigure(int figure, Pos pos) {
		setFigure(figure, pos.getX(), pos.getY());
	}

}
