import javax.swing.*;
import java.awt.*;
import java.util.*;

public class SweetCrush extends JFrame {

    // ---------------- CANDY ENUM ----------------
    enum Candy {
        RED, GREEN, BLUE, YELLOW, PURPLE;
        static Candy random() {
            return values()[new Random().nextInt(values().length)];
        }
    }

    // ---------------- TREE NODE (LEVELS) ----------------
    static class LevelNode {
        int target, moves;
        LevelNode next;
        LevelNode(int t, int m) {
            target = t;
            moves = m;
        }
    }

    // ---------------- CONSTANTS ----------------
    final int SIZE = 8;

    // ---------------- DATA STRUCTURES ----------------
    Candy[][] board = new Candy[SIZE][SIZE];               // 2D ARRAY
    JButton[][] buttons = new JButton[SIZE][SIZE];

    Stack<Candy> stack = new Stack<>();                     // STACK (gravity)
    Queue<Point> queue = new LinkedList<>();                // QUEUE (match removal)
    LinkedList<String> history = new LinkedList<>();        // LINKED LIST (moves)
    ArrayList<LevelNode> levels = new ArrayList<>();        // ARRAYLIST (levels)

    // ---------------- GAME STATE ----------------
    LevelNode currentLevel;
    int levelIndex = 0;
    int score = 0, moves;
    int sr = -1, sc = -1;

    JLabel scoreLabel = new JLabel();
    JLabel movesLabel = new JLabel();
    JLabel levelLabel = new JLabel();

    // ---------------- CONSTRUCTOR ----------------
    public SweetCrush() {
        buildLevels();

        setTitle("Sweet Crush - DSA Project");
        setSize(600, 700);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel top = new JPanel();
        top.add(levelLabel);
        top.add(scoreLabel);
        top.add(movesLabel);
        add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(SIZE, SIZE));
        add(grid, BorderLayout.CENTER);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton b = new JButton();
                b.setFont(new Font("Arial", Font.BOLD, 12));
                buttons[i][j] = b;
                int r = i, c = j;
                b.addActionListener(e -> click(r, c));
                grid.add(b);
            }
        }

        startLevel();
        setVisible(true);
    }

    // ---------------- LEVEL TREE ----------------
    void buildLevels() {
        levels.add(new LevelNode(100, 20));
        levels.add(new LevelNode(200, 18));
        levels.add(new LevelNode(300, 16));
        levels.add(new LevelNode(400, 14));
        levels.add(new LevelNode(500, 12));

        for (int i = 0; i < levels.size() - 1; i++)
            levels.get(i).next = levels.get(i + 1);
    }

    void startLevel() {
        currentLevel = levels.get(levelIndex);
        score = 0;
        moves = currentLevel.moves;

        levelLabel.setText("Level: " + (levelIndex + 1));
        scoreLabel.setText("Score: 0");
        movesLabel.setText("Moves: " + moves);

        initBoard();
        refresh();

        JOptionPane.showMessageDialog(this,
                "Level " + (levelIndex + 1) +
                        "\nTarget Score: " + currentLevel.target +
                        "\nMoves: " + currentLevel.moves);
    }

    // ---------------- BOARD INIT ----------------
    void initBoard() {
        do {
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    board[i][j] = Candy.random();
        } while (hasMatch());
    }

    // ---------------- CLICK LOGIC ----------------
    void click(int r, int c) {
        if (moves <= 0) return;

        if (sr == -1) {
            sr = r;
            sc = c;
            buttons[r][c].setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            return;
        }

        if (Math.abs(sr - r) + Math.abs(sc - c) == 1) {
            swap(sr, sc, r, c);

            if (hasMatch()) {
                moves--;
                history.add("Swap (" + sr + "," + sc + ")");
                resolve();
                movesLabel.setText("Moves: " + moves);
                scoreLabel.setText("Score: " + score);

                if (score >= currentLevel.target) {
                    levelIndex++;
                    if (levelIndex < levels.size())
                        startLevel();
                    else {
                        JOptionPane.showMessageDialog(this, "🎉 ALL LEVELS COMPLETED!");
                        System.exit(0);
                    }
                }
            } else {
                swap(sr, sc, r, c); // revert
            }
        }

        buttons[sr][sc].setBorder(null);
        sr = sc = -1;
        refresh();
    }

    // ---------------- SWAP ----------------
    void swap(int r1, int c1, int r2, int c2) {
        Candy t = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = t;
    }

    // ---------------- MATCH CHECK ----------------
    boolean hasMatch() {
        // Horizontal
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE - 2; j++)
                if (board[i][j] != null &&
                        board[i][j] == board[i][j+1] &&
                        board[i][j] == board[i][j+2])
                    return true;

        // Vertical
        for (int j = 0; j < SIZE; j++)
            for (int i = 0; i < SIZE - 2; i++)
                if (board[i][j] != null &&
                        board[i][j] == board[i+1][j] &&
                        board[i][j] == board[i+2][j])
                    return true;

        return false;
    }

    // ---------------- RESOLVE MATCHES ----------------
    void resolve() {
        do {
            queue.clear();

            // Horizontal
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE - 2; j++)
                    if (board[i][j] != null &&
                            board[i][j] == board[i][j+1] &&
                            board[i][j] == board[i][j+2]) {

                        queue.add(new Point(i, j));
                        queue.add(new Point(i, j+1));
                        queue.add(new Point(i, j+2));
                        score += 10;
                    }

            // Vertical
            for (int j = 0; j < SIZE; j++)
                for (int i = 0; i < SIZE - 2; i++)
                    if (board[i][j] != null &&
                            board[i][j] == board[i+1][j] &&
                            board[i][j] == board[i+2][j]) {

                        queue.add(new Point(i, j));
                        queue.add(new Point(i+1, j));
                        queue.add(new Point(i+2, j));
                        score += 10;
                    }

            while (!queue.isEmpty()) {
                Point p = queue.poll();
                board[p.x][p.y] = null;
            }

            applyGravity();

        } while (hasMatch());
    }

    // ---------------- GRAVITY (STACK) ----------------
    void applyGravity() {
        for (int c = 0; c < SIZE; c++) {
            stack.clear();
            for (int r = 0; r < SIZE; r++)
                if (board[r][c] != null)
                    stack.push(board[r][c]);

            for (int r = SIZE - 1; r >= 0; r--)
                board[r][c] = stack.isEmpty() ? Candy.random() : stack.pop();
        }
    }

    // ---------------- GUI REFRESH ----------------
    void refresh() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setText(board[i][j].name());
                buttons[i][j].setBackground(color(board[i][j]));
            }
    }

    Color color(Candy c) {
        switch (c) {
            case RED: return Color.RED;
            case GREEN: return Color.GREEN;
            case BLUE: return Color.CYAN;
            case YELLOW: return Color.YELLOW;
            default: return Color.MAGENTA;
        }
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        new SweetCrush();
    }
}
