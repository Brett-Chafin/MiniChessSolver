/**
 * Created by brettchafin on 5/1/17.
 */
public class Square {
    int xCord;
    int yCord;

    Square(int x, int y) {
        xCord = x;
        yCord = y;
    }

    public String moveForm(){
        String[] nums = {"6", "5", "4", "3", "2", "1"};
        String[] letters = {"a", "b", "c", "d", "e"};

        String str = letters[xCord] + nums[yCord];
        return str;
    }
}
