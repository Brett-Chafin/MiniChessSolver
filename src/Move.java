/**
 * Created by brettchafin on 5/1/17.
 */
public class Move {
    Square src;
    Square dest;

    Move(Square toSrc, Square toDest){
        src = new Square(toSrc.xCord, toSrc.yCord);
        dest = new Square(toDest.xCord, toDest.yCord);
    }

    public void printMove() {
        System.err.println(src.moveForm() + "-" + dest.moveForm());
    }
}
