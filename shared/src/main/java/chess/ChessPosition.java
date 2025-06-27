package chess;

import java.util.Objects;
/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    public boolean inBounds() {
        return !(row < 1 || row > 8 || col < 1 || col > 8);
    }

    @Override
    public boolean equals(Object o) {
        // if o is self
        if (this == o) {
            return true;
        }

        // if o is null or not same class
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // cast to position
        ChessPosition that = (ChessPosition) o;

        // return true if row and col are equivalent
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row + ", " +
                "col=" + col +
                "}";
    }
}
