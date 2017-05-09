/**
 * Created by brettchafin on 4/29/17.
 */

import java.io.FileInputStream;
import java.util.*;
import java.lang.*;

public class State {

    public static int BOARD_WIDTH = 5;
    public static int BOARD_HEIGHT = 6;
    public enum Capture {TRUE, FALSE, ONLY}
    public enum Piece {KING, QUEEN, KNIGHT, PAWN, BISHOP, ROOK}

    char[][] board;
    char sideOnMove;
    int moveCount;


    State(char[][] toBoard, char toSideOnMove, int toMoveCount) {

        board = new char[BOARD_WIDTH][BOARD_HEIGHT];
        for(int i = 0; i < BOARD_WIDTH; i++){
            for(int j = 0; j < BOARD_HEIGHT; j++) {
                board[i][j] = toBoard[i][j];
            }
        }

        sideOnMove = toSideOnMove;
        moveCount = toMoveCount;
    }

    //creates new board in the starting posistions
    State(){
        sideOnMove = 'W';
        moveCount = 0;

        board = new char[BOARD_WIDTH][BOARD_HEIGHT];
        board[0][0] = 'k';
        board[1][0] = 'q';
        board[2][0] = 'b';
        board[3][0] = 'n';
        board[4][0] = 'r';
        board[0][1] = 'p';
        board[1][1] = 'p';
        board[2][1] = 'p';
        board[3][1] = 'p';
        board[4][1] = 'p';
        board[0][2] = '.';
        board[1][2] = '.';
        board[2][2] = '.';
        board[3][2] = '.';
        board[4][2] = '.';
        board[0][3] = '.';
        board[1][3] = '.';
        board[2][3] = '.';
        board[3][3] = '.';
        board[4][3] = '.';
        board[0][4] = 'P';
        board[1][4] = 'P';
        board[2][4] = 'P';
        board[3][4] = 'P';
        board[4][4] = 'P';
        board[0][5] = 'R';
        board[1][5] = 'N';
        board[2][5] = 'B';
        board[3][5] = 'Q';
        board[4][5] = 'K';
    }


    /************* Starts a random game with both players making random moves **********/
    void playRandomGame() {
        char[][] newBoard = board;
        boolean isGameOver = false;

        System.err.println("Now playing game with random moves\n");

        while(!isGameOver) {
            newBoard = makeRandomMove(newBoard);
            isGameOver = gameOver(newBoard);
        }

    }

    char[][] makeRandomMove(char[][] board){
        ArrayList<Move> moveList = moveGen(board);

        //if we dont have any moves to make on the board, we lost
        if(moveList.size() <= 0){
            System.err.println(sideOnMove + " ran out of moves!");
            printBoard(board);
            System.exit(-1);
        }

        //printMoveList(moveList);

        //pick random move
        int index;
        if(moveList.size() == 1) {
            index = 1;
        }
        else {
            index = (int) Math.random() % (moveList.size() - 1);
        }

        //TODO: Not working with single move to make
        Move moveToMake = moveList.get(index);

        System.err.print("Moving: ");
        moveToMake.printMove();


        return makeMove(moveToMake, board);


    }

    char[][] makeMove(Move move, char[][] board) {

        //make sure we're moving the right square
        assert board[move.src.xCord][move.src.yCord] != '.';

        //grab the piece to move
        char pieceToMove = board[move.src.xCord][move.src.yCord];

        //make sure were moving the right color
        assert sideOnMove == getColor(pieceToMove);

        //pick piece up
        board[move.src.xCord][move.src.yCord] = '.';

        //place piece
        board[move.dest.xCord][move.dest.yCord] = pieceToMove;

        //increment move counts, switch sides and print board
        if(sideOnMove == 'B') {
            moveCount += 1;
        }
        switchSideOnMove();
        printBoard(board);

        return board;
    }

    boolean gameOver(char[][] board) {

        if(moveCount > 40) {
            System.err.println("Max move count reached!\n\n Draw!");
            return true;
        }

        //search for kings
        boolean isBlackKing = false;
        boolean isWhiteKing = false;
        for(int i = 0; i < BOARD_WIDTH; i++){
            for(int j = 0; j < BOARD_HEIGHT; j++) {
                char p = board[i][j];
                if(p == 'k') {
                    isBlackKing = true;
                }
                if(p == 'K') {
                    isWhiteKing = true;
                }
            }
        }

        //if a king is missing, its game over
        if(isBlackKing == false) {
            System.err.println("Black's king has been captured!\n\nWhite Wins!");
            return true;
        }
        if(isWhiteKing == false) {
            System.err.println("White's king has been captured!\n\nBlack Wins!");
            return true;
        }

        return false;

    }


    /** Generates a list of all legal moves on the current board */
    ArrayList<Move> moveGen(){

        ArrayList<Move> moveList = new ArrayList<>();
        boolean isKing = false; //ensure theres a king on the board

        if(sideOnMove == 'W') {
            for(int i = 0; i < BOARD_WIDTH; i++){
                for(int j = 0; j < BOARD_HEIGHT; j++) {
                    char p = board[i][j];
                    if(p != '.' && getColor(p) == 'W') {
                        if(p == 'K') {
                            isKing = true;
                        }
                        moveList = union(moveList, moveList(i, j));
                    }
                }
            }
        }

        else if (sideOnMove == 'B'){
            for(int i = 0; i < BOARD_WIDTH; i++){
                for(int j = 0; j < BOARD_HEIGHT; j++) {
                    char p = board[i][j];
                    if(p != '.' && getColor(p) == 'B') {
                        if(p == 'k') {
                            isKing = true;
                        }
                        moveList = union(moveList, moveList(i, j));
                    }
                }
            }
        }


        //side on move error
        else {
            System.err.println("Side on Move error in moveGen");
            System.exit(-1);
        }

        //make sure theres a king on the board
        assert isKing == true;

        return moveList;
    }


    /******* Move gen with board argument *********/
    ArrayList<Move> moveGen(char[][] board){

        ArrayList<Move> moveList = new ArrayList<>();
        boolean isKing = false; //ensure theres a king on the board

        if(sideOnMove == 'W') {
            for(int i = 0; i < BOARD_WIDTH; i++){
                for(int j = 0; j < BOARD_HEIGHT; j++) {
                    char p = board[i][j];
                    if(p != '.' && getColor(p) == 'W') {
                        if(p == 'K') {
                            isKing = true;
                        }
                        moveList = union(moveList, moveList(i, j));
                    }
                }
            }
        }

        else if (sideOnMove == 'B'){
            for(int i = 0; i < BOARD_WIDTH; i++){
                for(int j = 0; j < BOARD_HEIGHT; j++) {
                    char p = board[i][j];
                    if(p != '.' && getColor(p) == 'B') {
                        if(p == 'k') {
                            isKing = true;
                        }
                        moveList = union(moveList, moveList(i, j));
                    }
                }
            }
        }


        //side on move error
        else {
            System.err.println("Side on Move error in moveGen");
            System.exit(-1);
        }

        //make sure theres a king on the board
        assert isKing == true;

        return moveList;
    }


    /** Given the current piece, and a direction to move, returns all possible legal moves */
    ArrayList<Move> scan(int x0, int y0, int dx, int dy, Capture capture, boolean stopShort ){
        int x = x0;
        int y = y0;
        char color;
        ArrayList<Move> moveList = new ArrayList<Move>();

        //make sure we're not scanning an empty square
        assert board[x][y] != '.';

        color = getColor(board[x][y]);

        /*find the color of the board
        if(Character.isLowerCase(board[x][y])) {
            color = 'B';
        }
        else{
            color = 'W';
        }
        */

        do{
            //increment Cords
            x = x + dx;
            y = y + dy;

            //if we're out of bounds
            if(x >= BOARD_WIDTH || x < 0 || y >= BOARD_HEIGHT || y < 0){
                //System.err.println("Scan out of Bounds");
                break;
            }

            //if theres a piece here
            if(board[x][y] != '.') {

                //if their the same color
                char tempColor = getColor(board[x][y]);
                if(color == tempColor) {
                    break;
                }

                //if we cant capture
                if(capture == Capture.FALSE){
                    break;
                }

                //if we can capture with piece, we cant move any father
                stopShort = true;
            }
            //if our only move is to capture and we dont have one, break
            else if(capture == Capture.ONLY) {
                break;
            }

            //We've found a valid move, insert it in the list
            Move toAdd = new Move(new Square(x0, y0), new Square(x,y));
            moveList.add(toAdd);

        }while(stopShort == false); //while we still have farther to go

        //return all the moves we've generated
        return moveList;
    }


    /** Scans all rotations of a piece for moves */
    ArrayList<Move> symmScan(int x, int y, int dx, int dy, Capture capture, boolean stopShort){

        ArrayList<Move> moveList = new ArrayList<Move>();

        //run through 4 rotations
        for(int i = 0; i < 4; i++) {
            moveList = union(moveList, scan(x, y, dx, dy, capture, stopShort));

            //swap dx with dy and negate dy
            int temp = dx;
            dx = dy;
            dy = - temp;
        }

        return moveList;
    }

    /** Trys moves in all directions for a given piece */
    ArrayList<Move> moveList(int x, int y) {

        assert board[x][y] != '.';

        //set defaults
        Capture capture = Capture.TRUE;
        boolean stopShort = false;

        //piece at x y cords
        char p = board[x][y];

        //get piece type
        Piece pType = getPieceType(p);

        ArrayList<Move> moveList = new ArrayList<Move>();


        switch (pType) {

            case KING:case QUEEN: {
                stopShort = (pType == Piece.KING); //if were a king
                moveList = union(moveList, symmScan(x, y, 0, 1, capture, stopShort));
                moveList = union(moveList, symmScan(x, y, 1, 1, capture, stopShort));
                return moveList;
            }

            case ROOK:case BISHOP: {
                stopShort = (pType == Piece.BISHOP); //set true if Bishop

                //case for the side move
                if(pType == Piece.BISHOP) {
                    capture = Capture.FALSE;
                }

                //try moving to the right/over
                moveList = union(moveList, symmScan(x, y, 0, 1, capture, stopShort));

                //for the bishops diagonal
                if(pType == Piece.BISHOP) {
                    stopShort = false;
                    capture = Capture.TRUE;
                    moveList = union(moveList, symmScan(x, y, 1, 1, capture, stopShort));
                }

                return moveList;
            }

            case KNIGHT: {
                stopShort = true;
                moveList = union(moveList, symmScan(x, y, 1, 2, capture, stopShort));
                moveList = union(moveList, symmScan(x, y, -1, 2, capture, stopShort));
                return moveList;
            }

            case PAWN: {

                //which way is the pawn moving
                int direction = -1;

                if(getColor(p) == 'B') {
                    direction = 1;
                }

                moveList = union(moveList, scan(x, y, -1, direction, capture = Capture.ONLY, stopShort = true));
                moveList = union(moveList, scan(x, y, 1, direction, capture = Capture.ONLY, stopShort = true));
                moveList = union(moveList, scan(x, y, 0, direction, capture = Capture.FALSE, stopShort = true));

                return moveList;
            }

            default: {
                System.err.println("MoveGen Type Error");
                System.exit(-1);
            }

        }
        System.err.println("MoveGen Type Error");
        System.exit(-1);
        return null;

    }

    //util function that returns the peice type
    private Piece getPieceType(char p) {
        switch(p){
            case 'K':case 'k':
                return Piece.KING;

            case 'Q':case 'q':
                return Piece.QUEEN;

            case 'p':case 'P':
                return Piece.PAWN;

            case 'r':case 'R':
                return Piece.ROOK;

            case 'N':case 'n':
                return Piece.KNIGHT;

            case 'B':case 'b':
                return Piece.BISHOP;
        }

        System.err.println("Incorrect char sent to getPieceType: " + p);
        System.exit(-1);
        return null;
    }

    //utility Union function
    public <T> ArrayList<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }


    //util function to get the peice color
    char getColor(char piece) {

        //make sure we're not scanning an empty square
        assert piece != '.';

        if(Character.isLowerCase(piece)) {
            return 'B';
        }
        else{
            return 'W';
        }
    }

    void printBoard(){
        System.err.println("\n\n" + moveCount + " " + sideOnMove);
        for(int i = 0; i < BOARD_HEIGHT; i++){
            for(int j = 0; j < BOARD_WIDTH; j++) {
                System.err.print(board[j][i]);
            }
            System.err.println();
        }
    }

    //print with board arg
    void printBoard(char[][] board){
        System.err.println("\n\n" + moveCount + " " + sideOnMove);
        for(int i = 0; i < BOARD_HEIGHT; i++){
            for(int j = 0; j < BOARD_WIDTH; j++) {
                System.err.print(board[j][i]);
            }
            System.err.println();
        }
    }

    static void print(char[][] board) {
        for(int i = 0; i < BOARD_HEIGHT; i++){
            for(int j = 0; j < BOARD_WIDTH; j++) {
                System.err.print(board[j][i]);
            }
            System.err.println();
        }
    }


    void switchSideOnMove () {
        if (sideOnMove == 'W') {
            sideOnMove = 'B';
        }
        else if(sideOnMove == 'B') {
            sideOnMove = 'W';
        }
        else {
            System.err.println("Error switching move sides");
            System.exit(-1);
        }
    }

    void printMoveList(ArrayList<Move> moveList) {

        int size = moveList.size();

        if(size <= 0) {
            System.err.println("No Moves");
            return;
        }

        //System.err.println();
        for(int i = 0; i < size; i++) {
            moveList.get(i).printMove();
        }
    }


    /******  Main *********/
    public static void main(String args[]){

        /*State board = new State();
        board.printBoard();
        ArrayList<Move> moveList = board.moveGen();
        for(int i = 0; i < moveList.size(); i++) {
            moveList.get(i).printMove();
        }*/


        try {
            FileInputStream stream = new FileInputStream("tests.txt");
            int input = 0;

            char sideOnMove;
            int numOfMoves;

            int boardHeight = 6;
            int boardWidth = 5;

            char[][] inputBoard = new char[boardWidth][boardHeight];


            if((input = stream.read()) != -1){
                //System.out.print((char)input);
                char temp = (char)input;
                if((input = stream.read()) != -1) {
                    if((char)input == ' ') {
                        numOfMoves = Character.getNumericValue(temp);
                        input = stream.read();
                    }
                    else {
                        char temp2 = (char)input;
                        String str = "";
                        str += temp;
                        str += temp2;
                        numOfMoves = Integer.parseInt(str);
                        input = stream.read();
                        if ((char)input == ' ') {input = stream.read();}
                    }
                    sideOnMove = (char)input;

                    for(int i = 0; i < boardHeight; i++ ){
                        for(int j = 0; j < boardWidth; j++) {
                            if((input = stream.read()) != -1) {
                                if((char)input == '\n'){
                                    j--;
                                    continue;
                                }
                                inputBoard[j][i] = (char)input;
                            }
                        }
                    }

                    System.err.println();
                    System.err.println("Input Board: ");
                    System.err.println(sideOnMove + " " + numOfMoves);
                    print(inputBoard);
                    State board = new State(inputBoard, sideOnMove, (int)numOfMoves);
                    System.err.println();
                    System.err.println("State Created: ");
                    board.printBoard();
                    System.err.println();

                    board.playRandomGame();

                    /*
                    ArrayList<Move> moveList = board.moveGen();
                    for(int i = 0; i < moveList.size(); i++) {
                        moveList.get(i).printMove();
                    }
                    */



                }

            }
            else{
                System.err.println("Input Error");
            }

        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
