package com.gamewerks.blocky.engine;

import static com.gamewerks.blocky.engine.Direction.LEFT;
import static com.gamewerks.blocky.engine.Direction.NONE;
import static com.gamewerks.blocky.engine.Direction.RIGHT;
import com.gamewerks.blocky.util.Constants;
import com.gamewerks.blocky.util.Position;
import java.util.Random;

public class BlockyGame {
    private static final int LOCK_DELAY_LIMIT = 30;
    
    private Board board;
    private Piece activePiece;
    private Direction movement;
    
    private PieceKind[] pieceType; //added
    private int pieceIndex; // added
    
    private int lockCounter;
    
    /**
     * Initializes all piece types and shuffles them
     */
    private void pieceInitialize() {
        pieceType = PieceKind.values();
        shufflePieceTypes();
        pieceIndex = 0;
    }
    
    /**
     * Shuffles the pieceType array using the Fisher-Yates shuffle
     */
    private void shufflePieceTypes() {
        Random r = new Random();
        for (int i = pieceType.length -1; i>0; i--) {
            int j = r.nextInt(i+1);
            PieceKind temp = pieceType[i];
            pieceType[i] = pieceType[j];
            pieceType[j] = temp;
        }
        
    }
    
    public BlockyGame() {
        board = new Board();
        movement = Direction.NONE;
        lockCounter = 0;
        pieceInitialize();
        trySpawnBlock();
    }
    
    private void trySpawnBlock() {
        if (activePiece == null) {
            if(pieceIndex >= pieceType.length) {
                shufflePieceTypes(); // we shuffle the piece types when all pieces used
                pieceIndex = 0;
            }
         activePiece = new Piece(pieceType[pieceIndex], new Position(3, Constants.BOARD_WIDTH / 2 - 2));
         pieceIndex++;
            if (board.collides(activePiece)) {
                System.exit(0);
            }
        }
    }
    
    private void processMovement() {
        Position nextPos;
        switch(movement) {
        case NONE:
            nextPos = activePiece.getPosition();
            break;
        case LEFT:
            nextPos = activePiece.getPosition().add(0, -1);
            break;
        case RIGHT:
            nextPos = activePiece.getPosition().add(0, 1);
            break; // added missing break
        default:
            throw new IllegalStateException("Unrecognized direction: " + movement.name());
        }
        if (!board.collides(activePiece.getLayout(), nextPos)) {
            activePiece.moveTo(nextPos);
        }
    }
    
    private void processGravity() {
        Position nextPos = activePiece.getPosition().add(1, 0);
        if (!board.collides(activePiece.getLayout(), nextPos)) {
            lockCounter = 0;
            activePiece.moveTo(nextPos);
        } else {
            if (lockCounter < LOCK_DELAY_LIMIT) {
                lockCounter += 1;
            } else {
                board.addToWell(activePiece);
                lockCounter = 0;
                activePiece = null;
            }
        }
    }
    
    private void processClearedLines() {
        board.deleteRows(board.getCompletedRows());
    }
    
    public void step() {
        trySpawnBlock();
        processMovement(); // step processes movement
        processGravity();
        processClearedLines();
    }
    
    public boolean[][] getWell() {
        return board.getWell();
    }
    
    public Piece getActivePiece() { return activePiece; }
    public void setDirection(Direction movement) { this.movement = movement; }
    public void rotatePiece(boolean dir) { activePiece.rotate(dir); }
}
