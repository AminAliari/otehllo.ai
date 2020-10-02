import java.util.ArrayList;
import java.util.List;

import game.AbstractPlayer;
import game.BoardSquare;
import game.Move;
import game.OthelloGame;

public class MVP extends AbstractPlayer {

    private OthelloGame jogo;
    private int max;

    private BoardSquare bestMove;

    private Point[] corners = {new Point(0, 0), new Point(0, 7), new Point(7, 0), new Point(7, 7)};
    private Point[] nearCorners = {new Point(2, 2), new Point(2, 7), new Point(7, 2), new Point(7, 7)};

    public BoardSquare play(int[][] tab) {
        jogo = new OthelloGame();
        max = getMyBoardMark();
        List<Move> jogadas = jogo.getValidMoves(tab, max);

        if (jogadas.size() > 0) {
            search(tab, 0, max, depth, false, 9000, -9000);
            return bestMove;

        } else {
            return new BoardSquare(-1, -1);
        }
    }


    private int search(int[][] board, int currentDepth, int isMax, int depth, boolean flag, int alpha, int beta) {

        if (currentDepth > depth) {
            int score = 0;
            for (int i = 0; i < 4; i++) {
                if (board[corners[i].i][corners[i].j] == isMax) {
                    score += 300;
                } else if (board[corners[i].i][corners[i].j] == isMax * -1) {
                    score -= 300;
                } else {
                    if (board[nearCorners[i].i][nearCorners[i].j] == isMax) {
                        score -= 50;
                    }
                    if (board[nearCorners[i].i][nearCorners[i].j] == isMax * -1) {
                        score += 50;
                    }
                }
            }
            return score;
        }
        BoardSquare best = null;
        int bestScore = -9000;
        if (currentDepth < depth - 1) {
            bestScore = beta;
        }
        int n = 0;
        List<Move> nextMoves = jogo.getValidMoves(board, isMax);
        for (Move move : nextMoves) {
            n++;
            int minScore = -search(move.getBoard(), currentDepth + 1, isMax * -1, depth, false, -bestScore, -alpha);
            if (minScore > bestScore) {
                bestMove = move.getBardPlace();
                best = bestMove;
                bestScore = minScore;
                if (minScore >= alpha || minScore >= 8003) {
                    return minScore;
                }
            }
        }
        if (n == 0) {
            best = null;

            if (flag) {
                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[0].length; j++) {
                        if (board[i][j] == isMax) {
                            n++;
                        } else if (board[i][j] == isMax * -1) {
                            n--;
                        }
                    }
                }
                if (n > 0) {
                    return n + 8000;
                } else {
                    return n - 8000;
                }
            }
            bestScore = -search(board, currentDepth + 1, isMax * -1, depth, true, -bestScore, -alpha);
        }
        bestMove = best;

        if (currentDepth >= depth - 1) {
            return bestScore + (n << 3);
        } else {
            return bestScore;
        }
    }


    private int[][] copyArray(int[][] array) {
        int[][] copyArray = new int[array.length][array[0].length];

        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, copyArray[i], 0, array[i].length);
        }
        return copyArray;
    }

    private void printState(Move m) {
        print(m.getBardPlace().toString());
        printArray(m.getBoard());
        print("");
    }

    private void printArray(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                print(board[i][j] + "\t", true);
            }
            print("");
        }
    }

    private void print(Object s) {
        System.out.println(s);
    }

    private void print(Object s, boolean noLine) {
        System.out.print(s);
    }

    private class Point {
        public int i, j;

        public Point(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public boolean equals(Object o) {
            Point p = (Point) o;
            return i == p.i && j == p.j;
        }
    }

    private float cCorner = 401.724f, cCornerLead = 182.026f, cxSpot = 7, cEdge = 3.2f, cStability = 78.922f, cMove = 74.396f, cParity = 10;

    private float getScore(int[][] board) {

        // parity, possible moves, stability

        int maxCoins, minCoins, maxMoves = jogo.getValidMoves(board, max).size(), minMoves = jogo.getValidMoves(board, max * -1).size(),
                maxStability = 0, minStability = 0, minLeadCorners = 0, maxLeadCorners = 0, edgePrivilageMax = 0, edgePrivilageMin = 0,
                maxXspotScore = 0, minXspotScore = 0;

        ArrayList<Point> maxChecked = new ArrayList<>();
        ArrayList<Point> minChecked = new ArrayList<>();
        List<Move> maxNexts = jogo.getValidMoves(board, max);
        List<Move> minNexts = jogo.getValidMoves(board, max);

        for (Move next : maxNexts) {
            maxStability += getChangedCoins(board, next, max, maxChecked);
        }

        for (Move next : minNexts) {
            minStability += getChangedCoins(board, next, max * -1, minChecked);
        }

        int[] count = countCoins(board);
        maxCoins = count[0];
        minCoins = count[1];

        maxStability = maxCoins - maxStability;
        minStability = minCoins - minStability;

        int partiyScore = maxCoins - minCoins;
        int possibleMoves = maxMoves - minMoves;
        int stabilityScore = maxStability - minStability;

        // corners
        int maxCorners = 0, minCorners = 0;

        if (board[0][0] == max) {
            maxCorners++;
        } else if (board[0][0] == max * -1) {
            minCorners++;
        }

        if (board[0][7] == max) {
            maxCorners++;
        } else if (board[0][7] == max * -1) {
            minCorners++;
        }

        if (board[7][0] == max) {
            maxCorners++;
        } else if (board[7][0] == max * -1) {
            minCorners++;
        }

        if (board[7][7] == max) {
            maxCorners++;
        } else if (board[7][7] == max * -1) {
            minCorners++;
        }
        int cornerScore = maxCorners - minCorners;

        // corner leads

        if (board[1][2] == max || board[2][1] == max || board[2][2] == max) {
            maxLeadCorners++;
        } else if (board[1][2] == max * -1 || board[2][1] == max * -1 || board[2][2] == max * -1) {
            minLeadCorners++;
        }
        if (board[2][5] == max || board[2][6] == max || board[1][5] == max) {
            maxLeadCorners++;
        } else if (board[2][5] == max * -1 || board[2][6] == max * -1 || board[1][5] == max * -1) {
            minLeadCorners++;
        }
        if (board[5][1] == max || board[5][2] == max || board[6][2] == max) {
            maxLeadCorners++;
        } else if (board[5][1] == max * -1 || board[5][2] == max * -1 || board[6][2] == max * -1) {
            minLeadCorners++;
        }
        if (board[5][5] == max || board[5][6] == max || board[6][5] == max) {
            maxLeadCorners++;
        } else if (board[5][5] == max * -1 || board[5][6] == max * -1 || board[6][5] == max * -1) {
            minLeadCorners++;
        }
        int cornerLeadScore = maxLeadCorners - minLeadCorners;

        // xSpots
        if ((board[1][1] == max || board[0][1] == max || board[1][0] == max) && board[0][0] == 0) {
            maxXspotScore--;
        } else if ((board[1][1] == max * -1 || board[0][1] == max * -1 || board[1][0] == max * -1) && board[0][0] == 0) {
            minXspotScore--;
        }
        if ((board[6][6] == max || board[6][7] == max || board[7][6] == max) && board[0][0] == 0) {
            maxXspotScore--;
        } else if ((board[6][6] == max * -1 || board[6][7] == max * -1 || board[7][6] == max * -1) && board[7][7] == 0) {
            minXspotScore--;
        }
        if ((board[6][0] == max || board[6][1] == max || board[7][1] == max) && board[7][0] == 0) {
            maxXspotScore--;
        } else if ((board[6][0] == max * -1 || board[6][1] == max * -1 || board[7][1] == max * -1) && board[7][0] == 0) {
            minXspotScore--;
        }
        if ((board[0][6] == max || board[1][6] == max || board[1][7] == max) && board[0][0] == 0) {
            maxXspotScore--;
        } else if ((board[0][6] == max * -1 || board[1][6] == max * -1 || board[1][7] == max * -1) && board[0][0] == 0) {
            minXspotScore--;
        }
        int xSpotScore = maxXspotScore - minXspotScore;

        for (int s1 = 0; s1 < 8; s1++) {
            int s2 = 8 - s1;
            for (int i = s1; i < s2; i++) {
                for (int j = s1; j < s2; j++) {
                    if ((i == s1 || i == s2 - 1) && board[i][j] == 1) {
                        if (i == 2 || i == 5 || i == 7)
                            edgePrivilageMax++;
                        else
                            edgePrivilageMax--;
                    } else if ((i == s1 || i == s2 - 1) && board[i][j] == -1) {
                        if (i == 2 || i == 5 || i == 7)
                            edgePrivilageMin++;
                        else
                            edgePrivilageMin--;
                    }
                    if ((j == s1 || j == s2 - 1) && board[i][j] == 1) {
                        if (j == 2 || j == 5 || j == 7)
                            edgePrivilageMax++;
                        else
                            edgePrivilageMax--;
                    } else if ((j == s1 || j == s2 - 1) && board[i][j] == -1) {
                        if (j == 2 || j == 5 || j == 7)
                            edgePrivilageMin++;
                        else
                            edgePrivilageMin--;
                    }
                }
            }
        }
        int edgePrivilageScore = edgePrivilageMax - edgePrivilageMin;

        float score = (cornerScore * cCorner) + (cornerLeadScore * cCornerLead) + (xSpotScore * cxSpot) + (edgePrivilageScore * cEdge) + (stabilityScore * cStability) + (possibleMoves * cMove) + (partiyScore * cParity);

        return score;
    }

    private int[] countCoins(int[][] board) {
        int[] count = {0, 0};

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == max) {
                    count[0]++;
                } else if (board[i][j] == max * -1) {
                    count[1]++;
                }
            }
        }
        return count;
    }

    private int getChangedCoins(int[][] board, Move m, int isMax, ArrayList<Point> checked) {
        int total = 0;

        int r = m.getBardPlace().getRow();
        int c = m.getBardPlace().getCol();

        for (int i = r + 1; i < board.length; i++) {
            if (board[i][c] == 0) break;

            if (board[i][c] == isMax) {
                Point p = new Point(i, c);
                if (!checked.contains(p)) {
                    total += Math.abs(r - i) - 1;
                    checked.add(p);
                }
                break;
            }
        }

        for (int i = r - 1; i > -1; i--) {
            if (board[i][c] == 0) break;

            if (board[i][c] == isMax) {
                Point p = new Point(i, c);
                if (!checked.contains(p)) {
                    total += Math.abs(r - i) - 1;
                    checked.add(p);
                }
                break;
            }
        }

        for (int j = c + 1; j < board[0].length; j++) {
            if (board[r][j] == 0) break;

            if (board[r][j] == isMax) {
                Point p = new Point(r, j);
                if (!checked.contains(p)) {
                    total += Math.abs(c - j) - 1;
                    checked.add(p);
                }
                break;
            }
        }

        for (int j = c - 1; j > -1; j--) {
            if (board[r][j] == 0) break;

            if (board[r][j] == isMax) {
                Point p = new Point(r, j);
                if (!checked.contains(p)) {
                    total += Math.abs(c - j) - 1;
                    checked.add(p);
                }
                break;
            }
        }

        for (int i = r + 1, j = c + 1; i < board.length && j < board[0].length; i++, j++) {
            if (board[i][j] == 0) break;

            if (board[i][j] == isMax) {
                Point p = new Point(i, j);
                if (!checked.contains(p)) {
                    total += Math.abs(c - j) - 1;
                    checked.add(p);
                }
                break;
            }
        }
        for (int i = r - 1, j = c - 1; i > -1 && j > -1; i--, j--) {
            if (board[i][j] == 0) break;

            if (board[i][j] != 0 && board[i][j] != isMax) {
                Point p = new Point(i, j);
                if (!checked.contains(p)) {
                    total += Math.abs(c - j) - 1;
                    checked.add(p);
                }
            }
        }
        return total;
    }

    public MVP(int depth) {
        super(depth);
    }
}
