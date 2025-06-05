package iuh.fit.flappybirdclone;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Pipe: đại diện cho 1 cặp ống trên và ống dưới, có khe hở (gap) có thể co lại nếu là trap.
 * Bổ sung khả năng đánh dấu một ống là “HighScore pipe” (vẽ bằng pipe-red.png).
 */
public class Pipe {
    // Chiều rộng cố định của mỗi ống
    public static final double WIDTH = 60;
    // Khe hở mặc định (px)
    private static final double DEFAULT_GAP_HEIGHT = 200;

    // Tốc độ di chuyển của ống (px mỗi frame), có thể tăng dần
    public static double SPEED = 1.5;

    private double x;
    private double gapY;        // tọa độ y bắt đầu của khe hở (phần ống trên)
    private double sceneHeight; // chiều cao màn hình để vẽ ống dưới

    // Chiều cao khe hở hiện tại (bắt đầu bằng DEFAULT_GAP_HEIGHT)
    private double gapHeight = DEFAULT_GAP_HEIGHT;

    // Nếu true tức đây là “trap pipe” sẽ khép khe khi chim đi vào
    private boolean isTrap = false;

    // Nếu true tức đây là “HighScore pipe” → vẽ bằng pipe-red.png
    private boolean isHighScorePipe = false;  // ← THÊM biến này

    private static final Random random = new Random();

    /**
     * Constructor: khởi tạo ống ở vị trí x ban đầu (thường là sceneWidth),
     * và random khe hở theo chiều cao màn hình.
     */
    public Pipe(double startX, double sceneHeight) {
        this.x = startX;
        this.sceneHeight = sceneHeight;

        // Tạo vị trí khe hở ngẫu nhiên: từ 20% đến 80% chiều cao màn hình (trừ gapHeight)
        double minGapY = sceneHeight * 0.2;
        double maxGapY = sceneHeight * 0.8 - gapHeight;
        this.gapY = minGapY + random.nextDouble() * (maxGapY - minGapY);
    }

    /** Đánh dấu ống này là trap pipe. */
    public void setTrap(boolean flag) {
        this.isTrap = flag;
    }

    public boolean isTrap() {
        return isTrap;
    }

    /** Đánh dấu ống này là HighScore pipe (sẽ vẽ bằng ảnh đỏ). */
    public void setHighScorePipe(boolean flag) {
        this.isHighScorePipe = flag;
    }

    public boolean isHighScorePipe() {
        return isHighScorePipe;
    }

    /**
     * Cập nhật vị trí x (di chuyển qua trái).
     */
    public void update() {
        x -= SPEED;
    }

    /**
     * Nếu trap pipe và chim đang giữa khe (GameLoop sẽ gọi shrinkGap()), thì co khe dần.
     */
    public void shrinkGap() {
        // Mỗi lần co 2px; không cho âm
        if (gapHeight > 0) {
            gapHeight = Math.max(0, gapHeight - 2);
        }
    }

    /**
     * Vẽ ống: nếu có hình (ResourceManager), vẽ hình; không thì vẽ hình chữ nhật.
     * Nếu isHighScorePipe == true và ResourceManager có ảnh pipe-red, sẽ sử dụng ảnh đỏ.
     * Dùng gapHeight hiện tại để vẽ khe hở.
     */
    public void render(GraphicsContext gc) {
        // Trước tiên chọn ảnh ống: nếu là HighScore pipe và load được ảnh đỏ, dùng ảnh đỏ
        javafx.scene.image.Image imgToDraw = null;
        if (isHighScorePipe && ResourceManager.getPipeRedImage() != null) {
            imgToDraw = ResourceManager.getPipeRedImage();
        } else {
            imgToDraw = ResourceManager.getPipeImage();
        }

        if (imgToDraw != null) {
            // Vẽ ống trên (lật ngược)
            gc.save();
            gc.translate(x + WIDTH / 2, gapY / 2);
            gc.scale(1, -1);
            gc.drawImage(
                    imgToDraw,
                    -WIDTH / 2, -gapY / 2,
                    WIDTH, gapY
            );
            gc.restore();

            // Vẽ ống dưới
            double bottomY = gapY + gapHeight;
            double bottomHeight = sceneHeight - bottomY;
            gc.drawImage(
                    imgToDraw,
                    x, bottomY,
                    WIDTH, bottomHeight
            );

        } else {
            // Vẽ fallback bằng màu xanh lá + đen
            gc.setFill(Color.GREEN);
            gc.setStroke(Color.DARKGREEN);
            gc.setLineWidth(3);

            // Ống trên
            gc.fillRect(x, 0, WIDTH, gapY);
            gc.strokeRect(x, 0, WIDTH, gapY);
            // Ống dưới
            double bottomY = gapY + gapHeight;
            double bottomHeight = sceneHeight - bottomY;
            gc.fillRect(x, bottomY, WIDTH, bottomHeight);
            gc.strokeRect(x, bottomY, WIDTH, bottomHeight);

            // Vẽ mũ nắp ống
            gc.setFill(Color.DARKGREEN);
            // Nắp ống trên
            gc.fillRect(x - 5, gapY - 20, WIDTH + 10, 20);
            gc.strokeRect(x - 5, gapY - 20, WIDTH + 10, 20);
            // Nắp ống dưới
            gc.fillRect(x - 5, bottomY, WIDTH + 10, 20);
            gc.strokeRect(x - 5, bottomY, WIDTH + 10, 20);
        }
    }

    /**
     * Kiểm tra ống đã đi quá khỏi màn hình (x + WIDTH < 0) chưa.
     */
    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }

    /**
     * Kiểm tra chim đã vượt qua ống (đi chạm x + WIDTH) để cộng điểm.
     */
    public boolean isPassed(double birdX) {
        return birdX > x + WIDTH;
    }

    /**
     * Lấy Rectangle của ống trên để kiểm tra va chạm.
     */
    public javafx.geometry.Rectangle2D getBoundsTop() {
        return new javafx.geometry.Rectangle2D(x, 0, WIDTH, gapY);
    }

    /**
     * Lấy Rectangle của ống dưới để kiểm tra va chạm.
     */
    public javafx.geometry.Rectangle2D getBoundsBottom() {
        double bottomY = gapY + gapHeight;
        double bottomHeight = sceneHeight - bottomY;
        return new javafx.geometry.Rectangle2D(x, bottomY, WIDTH, bottomHeight);
    }

    /* Getters */
    public double getX() {
        return x;
    }
    public double getGapY() {
        return gapY;
    }
    public double getGapHeight() {
        return gapHeight;
    }
    public double getSceneHeight() {
        return sceneHeight;
    }

    /* Setter X nếu cần */
    public void setX(double x) {
        this.x = x;
    }
}
