import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class TypingGame extends JPanel implements ActionListener, KeyListener {
    private JFrame frame;
    private Timer timer;
    private ArrayList<FallingObject> fallingObjects;
    private StringBuilder currentWord;
    private int score = 0;
    private int highScore = 0;
    private boolean isPlaying = false;

    private JLabel scoreLabel;
    private JLabel wordToTypeLabel;
    private JLabel highScoreLabel;
    private static Random random;
    
        private static final String HIGH_SCORE_FILE = "highscore.dat";
        private String highlightWord = "";
    
        private int fallSpeed = 2; // Initial speed of falling objects
        private int speedIncreaseInterval = 10000; // Increase speed every 10 seconds
        private long lastSpeedIncreaseTime;
        
        private int backgroundSpeed = 1; // Speed of background effects
        private long lastBackgroundIncreaseTime;
    
        public TypingGame() {
            // Load high score
            highScore = loadHighScore();
    
            // Setup JFrame
            frame = new JFrame("Typing Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());
    
            // Add menu panel
            setupMenu();
    
            // Add key listener for typing
            frame.addKeyListener(this);
            frame.setFocusable(true);
            frame.setVisible(true);
    
            random = new Random();
            lastSpeedIncreaseTime = System.currentTimeMillis();
            lastBackgroundIncreaseTime = System.currentTimeMillis();
        }
    
        private void setupMenu() {
            JPanel menuPanel = new JPanel();
            menuPanel.setLayout(new GridLayout(2, 1));
    
            JButton startButton = new JButton("Start Game");
            startButton.setFont(new Font("Arial", Font.BOLD, 20));
            startButton.addActionListener(e -> startGame());
            menuPanel.add(startButton);
    
            JButton quitButton = new JButton("Quit Game");
            quitButton.setFont(new Font("Arial", Font.BOLD, 20));
            quitButton.addActionListener(e -> System.exit(0));
            menuPanel.add(quitButton);
    
            frame.add(menuPanel, BorderLayout.CENTER);
        }
    
        private void startGame() {
            isPlaying = true;
    
            // Reset UI and initialize game panel
            frame.getContentPane().removeAll();
            frame.getContentPane().repaint();
            frame.getContentPane().setLayout(new BorderLayout());
    
            // Game panel setup
            setupGamePanel();
    
            // Start the game timer
            timer = new Timer(30, this);
            fallingObjects = new ArrayList<>();
            currentWord = new StringBuilder();
            highlightWord = "";
            timer.start();
        }
    
        private void setupGamePanel() {
            JPanel topPanel = new JPanel(new GridLayout(1, 2));
    
            scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
            scoreLabel.setHorizontalAlignment(SwingConstants.LEFT);
            topPanel.add(scoreLabel);
    
            highScoreLabel = new JLabel("High Score: " + highScore);
            highScoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
            highScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            topPanel.add(highScoreLabel);
    
            frame.add(topPanel, BorderLayout.NORTH);
    
            wordToTypeLabel = new JLabel("Type this word: ");
            wordToTypeLabel.setFont(new Font("Arial", Font.BOLD, 36));
            wordToTypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            wordToTypeLabel.setForeground(new Color(50, 50, 200));
            frame.add(wordToTypeLabel, BorderLayout.SOUTH);
    
            frame.add(this, BorderLayout.CENTER);
            frame.revalidate();
        }
    
        private void updateGame() {
            // Reduce the number of falling objects by making them appear less frequently
            if (fallingObjects.size() < 3 && random.nextInt(100) < 3) {  // 3 objects max, appear less often
                fallingObjects.add(new FallingObject(getWidth(), random));
            }
    
            for (FallingObject obj : fallingObjects) {
                obj.move(fallSpeed);
    
                if (obj.y > getHeight()) {
                    gameOver();
                    return;
                }
            }
    
            // Increase speed over time
            if (System.currentTimeMillis() - lastSpeedIncreaseTime > speedIncreaseInterval) {
                fallSpeed++;
                lastSpeedIncreaseTime = System.currentTimeMillis();
            }
    
            // Increase background speed over time
            if (System.currentTimeMillis() - lastBackgroundIncreaseTime > speedIncreaseInterval) {
                backgroundSpeed++;
                lastBackgroundIncreaseTime = System.currentTimeMillis();
            }
    
            repaint();
        }
    
        private void updateLabels() {
            SwingUtilities.invokeLater(() -> {
                scoreLabel.setText("Score: " + score);
                highScoreLabel.setText("High Score: " + highScore);
    
                if (!fallingObjects.isEmpty()) {
                    FallingObject obj = fallingObjects.get(0);
                    String targetWord = obj.word;
                    String typedSoFar = currentWord.toString();
    
                    // Highlight the correctly typed portion
                    highlightWord = "<html><span style='color:green;'>"
                            + targetWord.substring(0, Math.min(typedSoFar.length(), targetWord.length()))
                            + "</span><span style='color:black;'>"
                            + targetWord.substring(Math.min(typedSoFar.length(), targetWord.length()))
                            + "</span></html>";
    
                    wordToTypeLabel.setText(highlightWord);
                } else {
                    wordToTypeLabel.setText("Type this word: ");
                }
            });
        }
    
        private void gameOver() {
            timer.stop();
            JOptionPane.showMessageDialog(frame, "Game Over! Your score: " + score);
    
            // Update high score if necessary
            if (score > highScore) {
                highScore = score;
                saveHighScore(highScore);
                JOptionPane.showMessageDialog(frame, "New High Score: " + highScore);
            }
    
            frame.dispose();
            new TypingGame();
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPlaying) {
                updateGame();
                updateLabels();
            }
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
    
            // Smooth, gentle background using soft gradients and circles, with increasing speed
            drawBackground(g);
    
            for (FallingObject obj : fallingObjects) {
                obj.draw(g);
            }
        }
    
        private void drawBackground(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
        
            // Calculate time progression (0 to 1 for a full cycle of day to night)
            long currentTime = System.currentTimeMillis();
            double timeElapsed = (currentTime % 60000L) / 60000.0; // 60 seconds for a full cycle
        
            // Interpolate colors between day and night
            Color dayColor = new Color(135, 206, 235); // Light sky blue
            Color nightColor = new Color(25, 25, 112); // Midnight blue
            int red = (int) ((1 - timeElapsed) * dayColor.getRed() + timeElapsed * nightColor.getRed());
            int green = (int) ((1 - timeElapsed) * dayColor.getGreen() + timeElapsed * nightColor.getGreen());
            int blue = (int) ((1 - timeElapsed) * dayColor.getBlue() + timeElapsed * nightColor.getBlue());
            Color backgroundColor = new Color(red, green, blue);
        
            // Fill background with gradient
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        
            // Sun/Moon Animation
            int diameter = 50; // Size of sun/moon
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            double angle = Math.PI * 2 * timeElapsed; // Full circle in radians
        
            // Calculate sun/moon position based on angle
            int sunX = (int) (centerX + Math.cos(angle) * (getWidth() / 2 - 100));
            int sunY = (int) (centerY - Math.sin(angle) * (getHeight() / 2 - 100));
        
            // Draw sun during the day, moon at night
            if (timeElapsed < 0.5) { // Daytime
                g2d.setColor(new Color(255, 223, 0)); // Sun color (yellow)
            } else { // Nighttime
                g2d.setColor(new Color(173, 216, 230)); // Moon color (light blue)
            }
            g2d.fillOval(sunX - diameter / 2, sunY - diameter / 2, diameter, diameter);
        
            // Add stars during nighttime
            if (timeElapsed >= 0.5) {
                g2d.setColor(new Color(255, 255, 255, 150)); // Faint white stars
                for (int i = 0; i < 50; i++) {
                    int starX = random.nextInt(getWidth());
                    int starY = random.nextInt(getHeight() / 2); // Top half of screen
                    g2d.fillRect(starX, starY, 2, 2); // Tiny star points
                }
            }
        }
        
    
        @Override
        public void keyTyped(KeyEvent e) {
            char typedChar = e.getKeyChar();
    
            // Handle backspace for deleting last typed letter
            if (typedChar == KeyEvent.VK_BACK_SPACE && currentWord.length() > 0) {
                currentWord.setLength(currentWord.length() - 1);
            } else if (Character.isLetterOrDigit(typedChar)) {
                currentWord.append(typedChar);
            }
    
            if (!fallingObjects.isEmpty()) {
                FallingObject obj = fallingObjects.get(0);
                String targetWord = obj.word;
                String typedSoFar = currentWord.toString();
    
                // Check if typed word matches the target word
                if (targetWord.equalsIgnoreCase(typedSoFar)) {
                    // Slow blast effect with visible particles
                    obj.blast();
                    fallingObjects.remove(0);
                    score += 10;
                    currentWord.setLength(0);
                    highlightWord = ""; // Reset highlight
                }
            }
    
            repaint();
        }
    
        @Override
        public void keyPressed(KeyEvent e) {}
    
        @Override
        public void keyReleased(KeyEvent e) {}
    
        public static void main(String[] args) {
            SwingUtilities.invokeLater(TypingGame::new);
        }
    
        // High score utility methods
        private int loadHighScore() {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
                return dis.readInt();
            } catch (IOException e) {
                return 0; // Default high score if file doesn't exist
            }
        }
    
        private void saveHighScore(int score) {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
                dos.writeInt(score);
            } catch (IOException e) {
                System.err.println("Error saving high score: " + e.getMessage());
            }
        }
    
        // Inner class for falling objects
        private static class FallingObject {
            int x, y;
            String word;
            Color color;
            private boolean isBlasting = false;
            private ArrayList<SmallerObject> smallerObjects;
    
            private static final String[] WORDS = {"JAVA", "CODE", "GAME", "HELLO", "WORLD"};
            private static final Color[] COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
    
            public FallingObject(int screenWidth, Random random) {
                this.x = random.nextInt(screenWidth - 50);
                this.y = 0;
                this.word = WORDS[random.nextInt(WORDS.length)];
                this.color = COLORS[random.nextInt(COLORS.length)];
                this.smallerObjects = new ArrayList<>();
            }
    
            public void move(int speed) {
                y += speed;
            }
    
            public void draw(Graphics g) {
                if (isBlasting) {
                    // Draw smaller particles when blasting
                    for (SmallerObject obj : smallerObjects) {
                        obj.draw(g);
                    }
                } else {
                    g.setColor(color);
                    g.fillOval(x, y, 50, 50); // Draw the falling object
                    g.setColor(Color.BLACK);
                    g.drawString(word, x + 10, y + 30);
                }
            }
    
            public void blast() {
                isBlasting = true;
                for (int i = 0; i < 8; i++) { // Increased particles
                    smallerObjects.add(new SmallerObject(x, y, new Random()));
                }
                Timer blastTimer = new Timer(800, e -> isBlasting = false); // Slower blast effect duration
                blastTimer.setRepeats(false);
                blastTimer.start();
            }
        }
    
        // Inner class for smaller particles after the blast
        private static class SmallerObject {
            int x, y, size;
            Color color;
    
            public SmallerObject(int x, int y, Random random) {
                this.x = x;
                this.y = y;
                this.size = random.nextInt(15) + 5; // Random small size
                this.color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)); // Random color
            }
    
            public void draw(Graphics g) {
                g.setColor(color);
                g.fillOval(x + random.nextInt(30) - 15, y + random.nextInt(30) - 15, size, size); // Random position offset
        }
    }
}
