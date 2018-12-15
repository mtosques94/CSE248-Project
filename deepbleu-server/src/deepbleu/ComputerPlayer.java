package deepbleu;

import deepbleu.pieces.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

/**
 * A chess engine implementing Minimax with alpha-beta pruning. 
 * 
 * TO DO: 
 *      - Add option for iterative deepening w/ time constraints.
 *      - Improve board evaluation.
 *      
 * @author Matthew Tosques
 */
public class ComputerPlayer extends Player {
    
    //number of ply to look ahead
    final AtomicInteger DEPTH = new AtomicInteger(5); 
    //incentivize high mobility boards / aim to restrict opponent's available moves
    //nasty performance hit but helpful
    final AtomicBoolean USE_MOBILITY_SCORING = new AtomicBoolean(true); 

    //Use as many threads as possible, up to the number of logical CPUs present
    private ExecutorService ES = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 4));
    //This has the completed work returned to a blocking queue in order of completion.
    private ExecutorCompletionService<CompletedWork> ECS = new ExecutorCompletionService<>(ES);
    //The progress bar is bound to this value.
    private final ReadOnlyDoubleWrapper AI_PROGRESS = new ReadOnlyDoubleWrapper(0);
    
    public ComputerPlayer(String name, boolean isWhite) {
        super(name, isWhite);
    }
    
    public ReadOnlyDoubleProperty progressProperty() {
        return AI_PROGRESS;
    }

    public void selfDestruct() {
        ES.shutdownNow();
    }
    
    public void reset() {
        ES.shutdownNow();
		ES = Executors.newFixedThreadPool(
				Math.min(Runtime.getRuntime().availableProcessors(), 4));
        ECS = new ExecutorCompletionService<>(ES);    
    }
    
    @Override
    public ChessMove getMove(Board b) {
        int setDepth = DEPTH.get();
        boolean useMobility = USE_MOBILITY_SCORING.get();
        AI_PROGRESS.set(0);
        //For each legal move, submit an AITask to the pool for immediate processing.
        Collection<ChessMove> legalMoves = b.getAllLegalMoves();
        for (ChessMove potentialMove : legalMoves) {
            b.move(potentialMove);
            ECS.submit(new AITask(b, potentialMove, this, setDepth, useMobility));
            b.undoLastMove();
        }
        //Assume the worst and update the intended move any time a better option is found.
        ArrayList<CompletedWork> scores = new ArrayList<>();
        CompletedWork bestScore = new CompletedWork(new ChessMove(-1, -1, -1, -1), AITask.LOSE);
        //If we're really in some sort of bind, this will stop us from returning null.
        //Also seems to add some variety in the case that all moves score equally, at least on Windows.
        ChessMove move = legalMoves.stream().findFirst().get();
        //Find the move with the highest score.
        int numMovesScored = 0;
        System.out.println("Generating game tree (depth=" + (DEPTH)
                + "ply).  Finished evaluating moves: ");
        long startTime = System.currentTimeMillis();
        double total = legalMoves.size();
        while (scores.size() < legalMoves.size()) {
            try {
                CompletedWork mostRecentlyCompleted = ECS.take().get();
                ChessMove rootMove = mostRecentlyCompleted.getRootMove();
                
                //bellow are some dubious first attempts at making scores reflect both the game tree
                //as well as immediate consequences in a more axiomatic manner
                //otherwise this is strictly depth first search with no opening / closing incentives
                
                //1 never pass up a win
                if(b.tiles[rootMove.toRow][rootMove.toCol] instanceof King) {
                    mostRecentlyCompleted.setDelta(AITask.WIN);
                }
                
                //2 strongly favor moving King and Queen's pawn as opener
                if ((rootMove.fromCol == 3 | rootMove.fromCol == 4)
                        && (rootMove.toCol == 3) || rootMove.toCol == 4) {
                    if (b.tiles[rootMove.fromRow][rootMove.fromCol] != null
                            && b.tiles[rootMove.fromRow][rootMove.fromCol] instanceof Pawn
                            && b.tiles[rootMove.fromRow][rootMove.fromCol].moveCount == 0) {
                        mostRecentlyCompleted.setDelta(AITask.PAWN_D_E_BONUS);
                    }
                }
                //End of immediate consequence logic
                
                System.out.print(++numMovesScored + " ");
                if ((numMovesScored) % 5 == 0) 
                    System.out.println();
                double num = numMovesScored; 
                AI_PROGRESS.set(num / total);
                scores.add(mostRecentlyCompleted);
                //Change the intended move if needed.
                if (mostRecentlyCompleted.getScore() > bestScore.getScore()) {
                    bestScore = mostRecentlyCompleted;
                    move = bestScore.getRootMove();
                    //b.selected = b.tiles[move.fromRow][move.fromCol];
                    //b.updateGraphics();
                    System.out.print(" {Best score: " + bestScore.getScore() + "} ");
                }
            } catch (InterruptedException | ExecutionException ex) {
                return null;
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.print("\nTotal time to decide: " + totalTime / 1000 + " seconds.  ");
        b.selected = null;
        AI_PROGRESS.set(0);
        return move;
    }
}

/**
 * A callable task that computes the game tree of a given board to a given depth. 
 */
class AITask implements Callable<CompletedWork> {
    
    private int MAX_DEPTH;
    private final boolean MOBILITY_SCORING;
    
    //Material score values
    static final double WIN = Double.POSITIVE_INFINITY;
    static final double LOSE = Double.NEGATIVE_INFINITY;
    //King can't be worth infinity because we are scoring by material.
    static final double KING_VALUE = 10000000; 
    //don't deviate too far from this
    static final double QUEEN_VALUE = 950;
    static final double ROOK_VALUE = 500;
    static final double KNIGHT_VALUE = 300;
    static final double BISHOP_VALUE = 300;
    static final double PAWN_VALUE = 100;
    
    //Axiomatic incentives (set all these to zero to score purely by material)
    //Assuming the opponent has near-identical incentives.
    //Arbitrary values usually ruin output, keep changes small.
    static final double ADVANCE_PIECE_BONUS = 15; //move pieces forward
    static final double ADVANCE_PAWN_BONUS = 11; //move pawns forward sometimes more
    static final double PAWN_ON_PAWN_BONUS = 27; //use pawns to block enemy pawns head on, 
                                                 //don't be too scared of the opponent doing the same
    static final double PAWN_D_E_BONUS = 12; //strongly favor moving king & queen's pawns early on
    static final double SIDE_PAWN_PENALTY = 18; //pawns on the edge are worth a bit less
    static final double BISHOP_PAIR_BONUS = 90; //two bishops are greater than the sum of their parts
    static final double MOBILITY_BONUS = 3; //add this many points for every legal move you end up with
                                                    //don't consider king or pawn
    //weight sides uniquely? I'm not sure if this actually does anything useful.
    static final double CONVICTION_DIVISOR = 1.0; 

    private final Board b;
    private final ChessMove moveBeingTried;
    private final Player me;

    public AITask(Board b, ChessMove moveBeingTried, Player me, int depth, boolean scoreByMobility) {
        this.b = new Board(b);
        this.moveBeingTried = moveBeingTried;
        this.me = me;
        this.MAX_DEPTH = depth;
        this.MOBILITY_SCORING = scoreByMobility;
        if(b.numPiecesInPlay() < 16 && MAX_DEPTH < 7) {
            this.MAX_DEPTH++;
        }
    }

    @Override
    public CompletedWork call() {
        //Starting tree as minimizing player because we have already performed our move in ComputerPlayer.getMove()
        return new CompletedWork(moveBeingTried, evalMove(b, MAX_DEPTH - 1, LOSE, WIN, false));
    }
    
    double evalMove(Board b, int depth, double alpha, double beta, boolean max) {
        //end game condition(s)
        boolean isTerminalNode = b.kingCaptured(); 
        double score = Double.NaN;
        //Either the game ended or we've sufficiently fried the CPU.
        if (depth == 0 || isTerminalNode) 
            return evalBoard(b);
        if (max) { //Maximizing player, aka the AI.  Always wants the best possible outcome.
            score = LOSE;
            Collection<ChessMove> legalMoves = b.getAllLegalMoves();
            for (ChessMove potentialMove : legalMoves) {
                b.move(potentialMove);
                //check for win
                if(potentialMove.enemyCaptured != null && potentialMove.enemyCaptured instanceof King) {
                    b.undoLastMove();
                    return WIN;
                }
                double value = evalMove(b, depth - 1, alpha, beta, false);
                b.undoLastMove();
                //update maximizers best found value
                if (value > score) score = value;
                if (value > alpha) alpha = value;
                //prune the tree if possible
                if (beta <= alpha) break;
            }
            return score;
        } else { //The minimizing player aka opponent.  Always wants the WORST outcome for the AI.
            score = WIN;
            Collection<ChessMove> legalMoves = b.getAllLegalMoves();
            for (ChessMove potentialMove : legalMoves) {
                b.move(potentialMove);
                //check for loss
                if(potentialMove.enemyCaptured != null && potentialMove.enemyCaptured instanceof King) {
                    b.undoLastMove();
                    return LOSE;
                }
                double value = evalMove(b, depth - 1, alpha, beta, true);
                b.undoLastMove();
                //update minimizers worst found value
                if (value < score) score = value;
                if (value < beta) beta = value;
                //prune the tree if possible
                if (beta <= alpha) break;
            }
            return score;
        }
    }

    double evalBoard(Board b) {
        double score = 0;
        boolean isKing = false; //don't call instanceof twice
        boolean isPawn = false; // ^^
        boolean hasBishop = false; //using this in loop to count bishops
        boolean hasEnemyBishop = false; //same idea for enemy
        
        for (Piece[] row : this.b.tiles) 
            for (Piece p : row) 
                if (p != null) { //for every piece p
                    if (p.player == me) { //if p is my piece
                        //Material scoring
                        if (p instanceof King) {
                            score += KING_VALUE;
                            isKing = true;
                        }
                        else if (p instanceof Queen)  score += QUEEN_VALUE;
                        else if (p instanceof Rook)   score += ROOK_VALUE;
                        else if (p instanceof Knight) score += KNIGHT_VALUE;
                        else if (p instanceof Bishop) {
                            if (!hasBishop) {
                                hasBishop = true;
                            } else {
                                score += BISHOP_PAIR_BONUS;
                            }
                            score += BISHOP_VALUE;
                        }
                        else {
                            score += PAWN_VALUE;
                            isPawn = true;
                        }
                        //Axiomatic scoring
                        if (!isKing) {
                            if (this.me.isWhite) { //Playing as white and this is my piece.
                                if (isPawn) {
                                    if(p.moveCount == 0 && (p.y == 3 || p.y == 4)) {
                                        score -= PAWN_D_E_BONUS;
                                    }
                                    if (p.x -1 >= 0 && b.tiles[p.x - 1][p.y] != null
                                            && b.tiles[p.x - 1][p.y] instanceof Pawn
                                            && b.hasEnemyAt(p.x - 1, p.y, p)) {
                                        score += PAWN_ON_PAWN_BONUS;
                                    }
                                    score += ((ADVANCE_PIECE_BONUS + ADVANCE_PAWN_BONUS) * (6 - p.x));
                                    if(p.y == 0 || p.y == 7) {
                                        score -= SIDE_PAWN_PENALTY;
                                    }
                                } else score += (ADVANCE_PIECE_BONUS * (6 - p.x));
                            } else { //Playing as black and this is my piece.
                                if (isPawn) {
                                    if (p.x +1 <= 7 && b.tiles[p.x + 1][p.y] != null
                                            && b.tiles[p.x + 1][p.y] instanceof Pawn
                                            && b.hasEnemyAt(p.x + 1, p.y, p)) {
                                        score += PAWN_ON_PAWN_BONUS;
                                    }
                                    score += ((ADVANCE_PIECE_BONUS + ADVANCE_PAWN_BONUS) * (p.x-1));
                                    if(p.y == 0 || p.y == 7) {
                                        score -= SIDE_PAWN_PENALTY;
                                    }
                                } else {
                                    score += (ADVANCE_PIECE_BONUS * (1 - p.x));
                                    if (this.MOBILITY_SCORING) {
                                        score += p.getLegalMoves(b).size() * MOBILITY_BONUS;
                                    }
                                }
                            }
                        }
                    } else { //if p is NOT my piece
                        //Material scoring
                        if (p instanceof King) {
                            score -= KING_VALUE;
                            isKing = true;
                        }
                        else if (p instanceof Queen)  score -= QUEEN_VALUE;
                        else if (p instanceof Rook)   score -= ROOK_VALUE;
                        else if (p instanceof Knight) score -= KNIGHT_VALUE;
                        else if (p instanceof Bishop) {
                            if (!hasEnemyBishop) {
                                hasEnemyBishop = true;
                            } else {
                                score -= BISHOP_PAIR_BONUS;
                            }
                            score -= BISHOP_VALUE;
                        }
                        else {
                            score -= PAWN_VALUE;
                            isPawn = true;
                        }
                        //Axiomatic scoring, near symmetry with other block
                        if (!isKing) {
                            if (this.me.isWhite) { //Playing as white and this is not my piece.
                                if (isPawn) {
                                    if(p.moveCount == 0 && (p.y == 3 || p.y == 4)) {
                                        score += PAWN_D_E_BONUS / CONVICTION_DIVISOR;
                                    }   
                                    if (p.x +1 <= 7 && b.tiles[p.x + 1][p.y] != null
                                            && b.tiles[p.x + 1][p.y] instanceof Pawn
                                            && b.hasEnemyAt(p.x + 1, p.y, p)) {
                                        score += PAWN_ON_PAWN_BONUS / CONVICTION_DIVISOR;
                                    }
                                    score -= ((ADVANCE_PIECE_BONUS + ADVANCE_PAWN_BONUS) * (1 - p.x));
                                    if(p.y == 0 || p.y == 7) {
                                        score += SIDE_PAWN_PENALTY / CONVICTION_DIVISOR;
                                    }
                                } else score -= (ADVANCE_PIECE_BONUS * (1 - p.x));
                            } else { //Playing as black and this is not my piece.
                                if (isPawn) {
                                    if (p.x - 1 >= 0 && b.tiles[p.x - 1][p.y] != null
                                            && b.tiles[p.x - 1][p.y] instanceof Pawn
                                            && b.hasEnemyAt(p.x - 1, p.y, p)) {
                                        score += PAWN_ON_PAWN_BONUS / CONVICTION_DIVISOR;
                                    }
                                    score -= ((ADVANCE_PIECE_BONUS + ADVANCE_PAWN_BONUS) * (6 - p.x));
                                    if(p.y == 0 || p.y == 7) {
                                        score += SIDE_PAWN_PENALTY / CONVICTION_DIVISOR;
                                    }
                                } else {
                                    score -= (ADVANCE_PIECE_BONUS * (6 - p.x));
                                    if (this.MOBILITY_SCORING) {
                                        score -= p.getLegalMoves(b).size() * MOBILITY_BONUS / CONVICTION_DIVISOR;
                                    }
                                }
                            }
                        }
                        
                    }
                    isKing = false; isPawn = false;
                }
        return score;
    }
}

/**
 * The result of an AITask.
 * Knows the root move and the resulting score.
 */
class CompletedWork {

    private final ChessMove rootMove;
    private double score;

    public CompletedWork(ChessMove realMove, double score) {
        this.rootMove = realMove;
        this.score = score;
    }

    public ChessMove getRootMove() {
        return rootMove;
    }

    public double getScore() {
        return score;
    }

    public void setDelta(double delta) {
        this.score += delta;
    }
}
