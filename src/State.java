/**
 * Created by brettchafin on 4/29/17.
 */

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.lang.*;
import java.io.*;


public class State  {

    static String SERVER = "imcs.svcs.cs.pdx.edu";
    static String PORT = "3589";
    static String USERNAME = "SleepyGary";
    static String PASSWORD = "tinyrick";

    public static int BOARD_WIDTH = 5;
    public static int BOARD_HEIGHT = 6;
    public enum Capture {TRUE, FALSE, ONLY}
    public enum Piece {KING, QUEEN, KNIGHT, PAWN, BISHOP, ROOK}

    char[][] board;
    char sideOnMove;
    int moveCount;
    int recursionLevel = 0;
    int maxDepth = 4;
    int NegamaxDepth = 6;
    static int WIN_VALUE = 999;
    static int DEFAULT_ALPHA = -WIN_VALUE;
    static int DEFAULT_BETA = WIN_VALUE;
    int pruneCount = 0;
    int statesEvaled = 0;
    boolean orderingMoves = false;


    boolean verbose = false;


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


    ArrayList<Move> orderMoves(ArrayList<Move> moveList, char[][] board) {

        ArrayList<Move> orderedMoves = new ArrayList<>();
        int[] MoveValues = new int[moveList.size()];
        orderingMoves = true;

        for(int i = 0; i < moveList.size(); i++){


            Move move = moveList.get(i);

            char[][] newBoard = makeMove(move, board);

            int moveValue = - alphaBetaNegamax(newBoard, 2, DEFAULT_ALPHA, DEFAULT_BETA);
            switchSideOnMove();
            if(sideOnMove == 'B') {
                moveCount--; //back to the original move count
            }

            MoveValues[i] = moveValue;
        }

        for(int index = 0; index < moveList.size(); index++) {

            int maxValue = -WIN_VALUE;
            int maxIndex = index;
            for (int i = 0; i < moveList.size(); i++) {
                if (MoveValues[i] > maxValue) {
                    maxValue = MoveValues[i];
                    maxIndex = i;
                }
            }
            orderedMoves.add(moveList.get(maxIndex));
            MoveValues[maxIndex] = -WIN_VALUE;
        }

        assert orderedMoves.size() == moveList.size();

        orderingMoves = false;

        return orderedMoves;
    }


    int alphaBetaNegamax(final char[][] board, int depth, int alpha, int beta) {

        statesEvaled++;


        //if we've won
        if(gameOver(board) || depth <= 0) {
            return scoreState(board);
        }

        ArrayList<Move> moveList = moveGen(board);



        //if theres no more possible moves
        if(moveList.size() == 0) {
            return -WIN_VALUE;
        }

        //if we're in the process of ording moves
        if(orderingMoves == false) {
            //moveList = orderMoves(moveList, board);
        }

        Move moveToMake = moveList.get(0);

        char[][] newBoard = makeMove(moveToMake, board);

        recursionLevel++;
        int bestValue = - alphaBetaNegamax(newBoard, depth - 1, -beta, -alpha);
        recursionLevel--;
        switchSideOnMove(); //switch back to original board and original side on move
        if(sideOnMove == 'B') {
            moveCount--; //back to the original move count
        }


        //if the new value is better then our old best value, return it
        if(bestValue >= beta) {
            pruneCount++;
            return bestValue;
        }

        //update our best worst move
        alpha = Math.max(bestValue, alpha);


        if(verbose) {
            System.err.println("Back in recurrsion level " + recursionLevel);
            System.err.println("Trying the remaining moves");
            printMoveList(moveList);
            System.err.println("On Board");
            printBoard(board);
        }

        for(int i = 1; i < moveList.size(); i++) {
            newBoard = makeMove(moveList.get(i), board);
            recursionLevel++;
            int moveValue = - alphaBetaNegamax(newBoard, depth -1, -beta, -alpha );
            recursionLevel--;
            switchSideOnMove(); //switch back
            if(sideOnMove == 'B') {
                moveCount--; //back to the original move count
            }
            if(verbose) {
                System.err.println("Back in recurrsion level " + recursionLevel);
            }

            //if the new value is better then our old best value, return it
            if(moveValue >= beta) {
                pruneCount++;
                return moveValue;
            }

            bestValue = Math.max(bestValue, moveValue);
            alpha = Math.max(alpha, moveValue);
        }

        return bestValue;
    }


    Move pickAlphaBetaNegaMaxMove(ArrayList<Move> moveList, char[][] board) {

        int bestMoveIndex = 0;
        int bestValue;


        if(moveList.size() == 0) {
            System.err.println("No moves in pickNegaMaxMove");
            System.exit(-1);
        }


        moveList = orderMoves(moveList, board);

        //score the first move
        Move move = moveList.get(0);
        char[][] newBoard = makeMove(move, board);

        recursionLevel++;
        bestValue = - alphaBetaNegamax(newBoard, maxDepth, DEFAULT_ALPHA, DEFAULT_BETA);
        recursionLevel--;
        switchSideOnMove(); //switch back
        if(sideOnMove == 'B') {
            moveCount--; //back to the original move count
        }

        move.printMove();
        System.err.print(" Scored: " + bestValue + "\n");

        //if we've found a win, return that move
        if(bestValue == WIN_VALUE){
            return move;
        }

        //start scoring all other possible moves, stop if you find a winner
        for(int i = 1; i < moveList.size(); i++) {
            move = moveList.get(i);
            newBoard = makeMove(move, board);

            int moveValue = - alphaBetaNegamax(newBoard, maxDepth, -5, 5);
            switchSideOnMove();
            if(sideOnMove == 'B') {
                moveCount--; //back to the original move count
            }
            move.printMove();
            System.err.print(" Scored: " + moveValue + "\n");

            //if we've found a win, return that move
            if(moveValue == WIN_VALUE){
                return move;
            }

            //we've found a better move than we had before
            if(moveValue > bestValue) {
                bestMoveIndex = i;
                bestValue = moveValue;
            }

        }

        System.err.println("Prune count: " + pruneCount);
        System.err.println("StateEvaled count: " + statesEvaled);
        pruneCount = 0;
        statesEvaled = 0;
        return moveList.get(bestMoveIndex);

    }






    /*************** NegaMax Depth first search without Alpha Beta pruning **********/
    int negamax(final char[][] board, int depth) {

        if(verbose) {
            System.err.println("Entering Recurrsion level: " + recursionLevel);
        }

        /*char[][] oldBoard = new char[BOARD_WIDTH][BOARD_HEIGHT];
        for(int i = 0; i < BOARD_WIDTH; i++){
            for(int j = 0; j < BOARD_HEIGHT; j++) {
                oldBoard[i][j] = board[i][j];
            }
        }
        */

        //if we've won
        if(gameOver(board) || depth <= 0) {
            if(verbose) {
                System.err.println("Leaf Reached");
            }
            return scoreState(board);
        }

        ArrayList<Move> moveList = moveGen(board);

        if(verbose) {
            printBoard(board);
            printMoveList(moveList);
        }

        //if theres no more possible moves
        if(moveList.size() == 0) {
            return -WIN_VALUE;
        }

        Move moveToMake = moveList.get(0);

        char[][] newBoard = makeMove(moveToMake, board);

        recursionLevel++;
        int bestValue = - negamax(newBoard, depth - 1 );
        recursionLevel--;
        switchSideOnMove(); //switch back to original board and original side on move
        if(sideOnMove == 'B') {
            moveCount--; //back to the original move count
        }


        if(verbose) {
            System.err.println("Back in recurrsion level " + recursionLevel);
            System.err.println("Trying the remaining moves");
            printMoveList(moveList);
            System.err.println("On Board");
            printBoard(board);
        }

        for(int i = 1; i < moveList.size(); i++) {
            newBoard = makeMove(moveList.get(i), board);
            recursionLevel++;
            int moveValue = - negamax(newBoard, depth -1 );
            recursionLevel--;
            switchSideOnMove(); //switch back
            if(sideOnMove == 'B') {
                moveCount--; //back to the original move count
            }
            if(verbose) {
                System.err.println("Back in recurrsion level " + recursionLevel);
            }
            bestValue = Math.max(bestValue, moveValue);
        }

        return bestValue;
    }


    //takes a state and sums up both sides pieces and returns the score bases on whos on move
    int scoreState(char[][] board) {
        int whitePieceValue = 0;
        int blackPieceValue = 0;

        //for future use, maybe
        boolean whiteKingAlive = false;
        boolean blackKingAlive = false;

        for(int i = 0; i < BOARD_WIDTH; i++){
            for(int j = 0; j < BOARD_HEIGHT; j++) {
                char p = board[i][j];
                switch(p) {
                    case 'P':
                        whitePieceValue +=1;
                        break;
                    case 'B':case 'N':
                        whitePieceValue += 3;
                        break;
                    case 'R':
                        whitePieceValue += 5;
                        break;
                    case 'Q':
                        whitePieceValue += 9;
                        break;
                    case 'K':
                        whiteKingAlive = true;
                        break;
                    case 'p':
                        blackPieceValue +=1;
                        break;
                    case 'b':case 'n':
                        blackPieceValue += 3;
                        break;
                    case 'r':
                        blackPieceValue += 5;
                        break;
                    case 'q':
                        blackPieceValue += 9;
                        break;
                    case 'k':
                        blackKingAlive = true;
                }
            }
        }

        //game over condition
        if(!blackKingAlive || !whiteKingAlive) {
            if(whiteKingAlive && !blackKingAlive && sideOnMove == 'B'){
                return -WIN_VALUE;
            }
            if(whiteKingAlive && !blackKingAlive && sideOnMove == 'W'){
                return WIN_VALUE;
            }
            if(blackKingAlive && !whiteKingAlive && sideOnMove == 'B'){
                return WIN_VALUE;
            }
            if(blackKingAlive && !whiteKingAlive && sideOnMove == 'W'){
                return -WIN_VALUE;
            }
        }

        if(sideOnMove == 'W') {
            return whitePieceValue - blackPieceValue;
        }
        if(sideOnMove == 'B') {
            return blackPieceValue - whitePieceValue;
        }

        System.err.println("SideonMoveError");
        System.exit(-1);
        return 0;
    }


    //TODO: implement a playable interface
    void playUserVsRandomPlayer() {

    }

    /************* Alpha Beta game ***************/
    void playAlphaBetaNegaMaxGame() {

        char[][] newBoard = board;
        boolean isGameOver = false;

        System.err.println("Now playing game with AlphaBetaNegaMax moves at depth " + maxDepth + "\n");

        printBoard(newBoard);

        while(!isGameOver) {
            newBoard = makeAlphaBetaNegaMaxMove(newBoard);
            printBoard(newBoard);
            System.err.println("Score: " + scoreState(newBoard));
            isGameOver = gameOver(newBoard);
            if(isGameOver) {
                continue;
            }
            newBoard = makeNegaMaxMove(newBoard);
            printBoard(newBoard);
            System.err.println("Score: " + scoreState(newBoard));
            isGameOver = gameOver(newBoard);
        }

        int finalScore = scoreState(newBoard);
        if((finalScore == WIN_VALUE && sideOnMove == 'W') || finalScore == -WIN_VALUE && sideOnMove == 'B'){
            System.err.println("Win White");
        }
        else if((finalScore == WIN_VALUE && sideOnMove == 'B') || finalScore == -WIN_VALUE && sideOnMove == 'W' ){
            System.err.println("Win Black");
        }
        else {
            System.err.println("Draw");
        }
    }


    /*************** make ABNaga move ***************/
    char[][] makeAlphaBetaNegaMaxMove(char[][] board) {

        ArrayList<Move> moveList = moveGen(board);

        //if we dont have any moves to make on the board, we lost
        if(moveList.size() <= 0){
            System.err.println(sideOnMove + " ran out of moves!");
            printBoard(board);
            System.exit(-1);
        }

        //printMoveList(moveList);
        Move moveToMake = pickAlphaBetaNegaMaxMove(moveList, board);

        System.err.print("Moving: ");
        moveToMake.printMove();


        return makeMove(moveToMake, board);
    }





    /************* Play NegaMax Game ************/
    void playNegaMaxGame() {
        char[][] newBoard = board;
        boolean isGameOver = false;

        System.err.println("Now playing game with NegaMax moves at depth " + maxDepth + "\n");

        printBoard(newBoard);

        while(!isGameOver) {
            newBoard = makeNegaMaxMove(newBoard);
            printBoard(newBoard);
            System.err.println("Score: " + scoreState(newBoard));
            isGameOver = gameOver(newBoard);
            /*
            if(isGameOver) {
                continue;
            }
            newBoard = makeRandomMove(newBoard);
            printBoard(newBoard);
            System.err.println("Score: " + scoreState(newBoard));
            isGameOver = gameOver(newBoard);
            */
        }

        int finalScore = scoreState(newBoard);
        if((finalScore == WIN_VALUE && sideOnMove == 'W') || finalScore == -WIN_VALUE && sideOnMove == 'B'){
            System.err.println("Win White");
        }
        else if((finalScore == WIN_VALUE && sideOnMove == 'B') || finalScore == -WIN_VALUE && sideOnMove == 'W' ){
            System.err.println("Win Black");
        }
        else {
            System.err.println("Draw");
        }

    }

    char[][] makeNegaMaxMove(char[][] board) {

        ArrayList<Move> moveList = moveGen(board);

        //if we dont have any moves to make on the board, we lost
        if(moveList.size() <= 0){
            System.err.println(sideOnMove + " ran out of moves!");
            printBoard(board);
            System.exit(-1);
        }

        //printMoveList(moveList);
        Move moveToMake = pickNegaMaxMove(moveList, board);

        System.err.print("Moving: ");
        moveToMake.printMove();


        return makeMove(moveToMake, board);
    }

    Move pickNegaMaxMove(ArrayList<Move> moveList, char[][] board) {

        int bestMoveIndex = 0;
        int bestValue;


        if(moveList.size() == 0) {
            System.err.println("No moves in pickNegaMaxMove");
            System.exit(-1);
        }

        //score the first move
        Move move = moveList.get(0);
        char[][] newBoard = makeMove(move, board);

        recursionLevel++;
        bestValue = - negamax(newBoard, NegamaxDepth);
        recursionLevel--;
        switchSideOnMove(); //switch back
        if(sideOnMove == 'B') {
            moveCount--; //back to the original move count
        }

        move.printMove();
        System.err.print(" Scored: " + bestValue + "\n");

        //if we've found a win, return that move
        if(bestValue == WIN_VALUE){
            return move;
        }

        //start scoring all other possible moves, stop if you find a winner
        for(int i = 1; i < moveList.size(); i++) {
            move = moveList.get(i);
            newBoard = makeMove(move, board);

            int moveValue = - negamax(newBoard, NegamaxDepth);
            switchSideOnMove();
            if(sideOnMove == 'B') {
                moveCount--; //back to the original move count
            }

            move.printMove();
            System.err.print(" Scored: " + moveValue + "\n");

            //if we've found a win, return that move
            if(moveValue == WIN_VALUE){
                return move;
            }

            //we've found a better move than we had before
            if(moveValue > bestValue) {
                bestMoveIndex = i;
                bestValue = moveValue;
            }

        }

        return moveList.get(bestMoveIndex);

    }

    /************* Starts a random game with both players making random moves **********/
    void playRandomGame() {
        char[][] newBoard = board;
        boolean isGameOver = false;

        System.err.println("Now playing game with random moves\n");

        while(!isGameOver) {
            newBoard = makeRandomMove(newBoard);
            System.err.println("Score: " + scoreState(newBoard));
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
        Move moveToMake = pickRandomMove(moveList);

        System.err.print("Moving: ");
        moveToMake.printMove();


        return makeMove(moveToMake, board);

    }

    //Grabs a random move from the moveList
    Move pickRandomMove(ArrayList<Move> moveList) {

        int index;
        if(moveList.size() == 1) {
            index = 0;
        }
        else {
            index = (int) Math.random() % (moveList.size() - 1);
        }

        return moveList.get(index);
    }

    char[][] makeMove(Move move, char[][] board) {

        //TODO: Pawn Promotion

        if(verbose) {
            System.err.print("Trying move: ");
            move.printMove();
            //System.err.println("on board: ");
            //printBoard();
        }

        char[][] newBoard = new char[BOARD_WIDTH][BOARD_HEIGHT];
        for(int i = 0; i < BOARD_WIDTH; i++){
            for(int j = 0; j < BOARD_HEIGHT; j++) {
                newBoard[i][j] = board[i][j];
            }
        }


        //make sure we're moving the right square
        assert newBoard[move.src.xCord][move.src.yCord] != '.';

        ///make sure we're not over the move limit
        assert moveCount < 41;


        //grab the piece to move
        char pieceToMove = newBoard[move.src.xCord][move.src.yCord];

        //make sure were moving the right color
        assert sideOnMove == getColor(pieceToMove);



        //pawn promotion white
        if(pieceToMove == 'P' && move.dest.yCord == 0) {

            //pick piece up
            newBoard[move.src.xCord][move.src.yCord] = '.';

            //promote to white queen
            newBoard[move.dest.xCord][move.dest.yCord] = 'Q';
        }

        //pawn promotion black
        else if(pieceToMove == 'p' && move.dest.yCord == 5) {

            //pick piece up
            newBoard[move.src.xCord][move.src.yCord] = '.';

            //promote to white queen
            newBoard[move.dest.xCord][move.dest.yCord] = 'q';
        }

        //normal move
        else {

            //pick piece up
            newBoard[move.src.xCord][move.src.yCord] = '.';

            //place piece
            newBoard[move.dest.xCord][move.dest.yCord] = pieceToMove;
        }



        //increment move counts, switch sides and print board
        if(sideOnMove == 'B') {
            moveCount += 1;
        }
        switchSideOnMove();

        return newBoard;
    }

    boolean gameOver(char[][] board) {

        if(moveCount > 40) {
            //System.err.println("Max move count reached!\n\n Draw!");
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
            if(verbose) {
                System.err.println("Black's king has been captured!\n\nWhite Wins!");
            }
            return true;
        }
        if(isWhiteKing == false) {
            if(verbose) {
                System.err.println("White's king has been captured!\n\nBlack Wins!");
            }
            return true;
        }

        return false;

    }

    /***

    /** Generates a list of all legal moves on the current board
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
    } */


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
                        moveList = union(moveList, moveList(i, j, board));
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
                        moveList = union(moveList, moveList(i, j, board));
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
        //assert isKing == true;

        return moveList;
    }


    /** Given the current piece, and a direction to move, returns all possible legal moves */
    ArrayList<Move> scan(int x0, int y0, int dx, int dy, Capture capture, boolean stopShort, char[][] board ){
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
    ArrayList<Move> symmScan(int x, int y, int dx, int dy, Capture capture, boolean stopShort, char[][] board){

        ArrayList<Move> moveList = new ArrayList<Move>();

        //run through 4 rotations
        for(int i = 0; i < 4; i++) {
            moveList = union(moveList, scan(x, y, dx, dy, capture, stopShort, board));

            //swap dx with dy and negate dy
            int temp = dx;
            dx = dy;
            dy = - temp;
        }

        return moveList;
    }

    /** Trys moves in all directions for a given piece */
    ArrayList<Move> moveList(int x, int y, char[][] board) {

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
                moveList = union(moveList, symmScan(x, y, 0, 1, capture, stopShort, board));
                moveList = union(moveList, symmScan(x, y, 1, 1, capture, stopShort, board));
                return moveList;
            }

            case ROOK:case BISHOP: {
                stopShort = (pType == Piece.BISHOP); //set true if Bishop

                //case for the side move
                if(pType == Piece.BISHOP) {
                    capture = Capture.FALSE;
                }

                //try moving to the right/over
                moveList = union(moveList, symmScan(x, y, 0, 1, capture, stopShort, board));

                //for the bishops diagonal
                if(pType == Piece.BISHOP) {
                    stopShort = false;
                    capture = Capture.TRUE;
                    moveList = union(moveList, symmScan(x, y, 1, 1, capture, stopShort, board));
                }

                return moveList;
            }

            case KNIGHT: {
                stopShort = true;
                moveList = union(moveList, symmScan(x, y, 1, 2, capture, stopShort, board));
                moveList = union(moveList, symmScan(x, y, -1, 2, capture, stopShort, board));
                return moveList;
            }

            case PAWN: {

                //which way is the pawn moving
                int direction = -1;

                if(getColor(p) == 'B') {
                    direction = 1;
                }

                moveList = union(moveList, scan(x, y, -1, direction, capture = Capture.ONLY, stopShort = true, board));
                moveList = union(moveList, scan(x, y, 1, direction, capture = Capture.ONLY, stopShort = true, board));
                moveList = union(moveList, scan(x, y, 0, direction, capture = Capture.FALSE, stopShort = true, board));

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

            //extract input from my local test file
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


                    /*
                    System.err.println();
                    System.err.println("Input Board: ");
                    System.err.println(sideOnMove + " " + numOfMoves);
                    print(inputBoard);
                    State board = new State(inputBoard, sideOnMove, (int)numOfMoves);
                    ArrayList<Move> moveList = board.moveGen(board.board);

                    Move toMake = board.pickAlphaBetaNegaMaxMove(moveList, board.board);
                    //Move toMake = board.pickNegaMaxMove(moveList, board.board);
                    toMake.printMove();



                    /*
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    int i= 0;
                    while(i < 5){
                        String cmd = br.readLine();
                        Move test = new Move(cmd);
                        test.printMove();
                        //client.send(cmd, true);
                        i++;

                    }
                    */



                    /************* Server play ********************/

                    /*

                    Client client = new Client(SERVER, PORT, USERNAME, PASSWORD);

                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String inputGame = br.readLine();
                    System.out.print("Enter color: ");
                    String color = br.readLine();


                    String gameID = inputGame;
                    //char sideToPlay = client.offer('W');
                    char sideToPlay = client.accept(gameID, 'W');


                    //if we have first move
                    if(sideToPlay == 'W'){

                        //create a fresh board
                        State board = new State();

                        while(true) {

                            //gen moves for AlphaBeta to pick from
                            ArrayList<Move> mostList = board.moveGen(board.board);
                            Move toMake = board.pickAlphaBetaNegaMaxMove(mostList, board.board);
                            //Move toMake = board.pickNegaMaxMove(mostList, board.board);

                            //make the selected move and send it to the oppenent
                            board.board = board.makeMove(toMake, board.board);
                            client.sendMove(toMake.toString());

                            //wait for opponents move
                            String oppenentMove = client.getMove();
                            if(oppenentMove == null) {
                                client.close();
                                System.exit(1);
                            }
                            System.out.println("Opponent move: " + oppenentMove);

                            //create a Move objects for move string and make the move on local board
                            Move oppMove = new Move(oppenentMove);
                            oppMove.printMove();
                            board.board = board.makeMove(new Move(oppenentMove), board.board);
                        }

                    }

                    //if we're second move
                    if(sideToPlay == 'B'){
                        //create a fresh board
                        State board = new State();

                        while(true) {
                            String oppenentMove = client.getMove();
                            if (oppenentMove == null) {
                                client.close();
                                System.exit(1);
                            }

                            //make oppenents move
                            System.out.println("Opponent move: " + oppenentMove);
                            Move oppMove = new Move(oppenentMove);
                            oppMove.printMove();
                            board.board = board.makeMove(new Move(oppenentMove), board.board);

                            //make my move
                            ArrayList<Move> mostList = board.moveGen(board.board);
                            Move toMake = board.pickAlphaBetaNegaMaxMove(mostList, board.board);
                            //Move toMake = board.pickNegaMaxMove(mostList, board.board);
                            board.board = board.makeMove(toMake, board.board);
                            client.sendMove(toMake.toString());
                        }

                    }
                    client.close();



                    //board.playRandomGame();

                    //board.playNegaMaxGame();

                    */

                    /************* Alphabeta vs depth 2 negamax ********/

                    /*
                    State board = new State();
                    board.playAlphaBetaNegaMaxGame();
                    */


                    /************* Text input play ***********/

                    State board = new State(inputBoard, sideOnMove, (int)numOfMoves);
                    board.playNegaMaxGame();




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
