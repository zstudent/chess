package core;

/**
 * Insert the type's description here.
 * Creation date: (07.12.2001 %T)
 * @author: 
 */
public interface ChessProtocol {
	int NONE = 0;
	int KING = 2;
	int QUEEN = 4;
	int ROOK = 6;
	int BISHOP = 8;
	int KNIGHT = 10;
	int PAWN = 12;
	int BLACK = 0;
	int WHITE = 1;
	int KIND = 1;
	int MAX = 8;
	int CAN_FIGHT = 1;
	int MUST_FIGHT = 2;
	int KNIGHT_MOVE = 4;
	int INFO_FIGHTS = 1;
	int INFO_THREATEN = 2;
	int INFO_MATE = 4;
	int INFO_EP = 8;
	int C_OFFERDRAW = 1;
	int C_SURRENDER = 2;
	int C_YES = 3;
	int C_NO = 4;
}
