/**
 * Created by brettchafin on 5/1/17.
 */

import java.util.*;

public class Move {
    Square src;
    Square dest;

    Move(Square toSrc, Square toDest){
        src = new Square(toSrc.xCord, toSrc.yCord);
        dest = new Square(toDest.xCord, toDest.yCord);
    }

    Move(String move) {
        char[] charArray = move.toCharArray();
        Map<Character,Integer> numMap = new HashMap<Character, Integer>();
        Map<Character, Integer> charMap = new HashMap<Character, Integer>();

        numMap.put('6', 0);
        numMap.put('5', 1);
        numMap.put('4', 2);
        numMap.put('3', 3);
        numMap.put('2', 4);
        numMap.put('1', 5);

        charMap.put('a', 0);
        charMap.put('b', 1);
        charMap.put('c', 2);
        charMap.put('d', 3);
        charMap.put('e', 4);


        int SrcxCord = charMap.get(charArray[0]);
        int SrcyCord = numMap.get(charArray[1]);
        int DestxCord = charMap.get(charArray[3]);
        int DestyCord = numMap.get(charArray[4]);

        src = new Square(SrcxCord, SrcyCord);
        dest = new Square(DestxCord, DestyCord);
    }

    public void printMove() {
        System.err.println(src.moveForm() + "-" + dest.moveForm());
    }

    public String toString() {
        return src.moveForm() + "-" + dest.moveForm();
    }
}
