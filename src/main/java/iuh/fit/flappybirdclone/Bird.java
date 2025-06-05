package iuh.fit.flappybirdclone;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

/**
 * Bird: đối tượng đại diện cho chú chim trong game.
 */
public class Bird {
    // Vị trí và vận tốc theo phương Y
    private double x;
    private double y;
    private double velocityY;

    // Bán kính và hằng số vật lý
    public static final double RADIUS = 15;
    private static final double GRAVITY = 0.15;
    private static final double JUMP_STRENGTH = -4.5;
    private static final double MAX_VELOCITY = 5;

    // Chiều cao màn hình, dùng để kiểm tra chạm đất/trần
    private double sceneHeight;

    public Bird(double startX, double startY, double sceneHeight) {
        this.x = startX;
        this.y = startY;
        this.velocityY = 0;
        this.sceneHeight = sceneHeight;
    }

    /**
     * Cập nhật vị trí và vận tốc của chim theo trọng lực.
     */
    public void update() {
        // Áp dụng trọng lực
        velocityY += GRAVITY;
        // Giới hạn vận tốc tối đa
        if (velocityY > MAX_VELOCITY) {
            velocityY = MAX_VELOCITY;
        }
        // Cập nhật vị trí
        y += velocityY;
    }

    /**
     * Vẽ chim lên canvas (nếu có hình, vẽ hình; nếu không có, vẽ hình tròn).
     * Đã tăng kích thước lên khoảng 80x80 để chim to rõ ràng hơn.
     */
    public void render(GraphicsContext gc) {
        if (ResourceManager.getBirdImage() != null) {
            // --- Tăng kích thước thành 80x80 thay vì RADIUS*2.5 ---
            double size = 120;
            gc.drawImage(
                    ResourceManager.getBirdImage(),
                    x - size / 2, y - size / 2,
                    size, size
            );
        } else {
            // Vẽ tròn (fallback)
            double fallbackSize = RADIUS * 2; // vẫn giữ kích thước ban đầu
            gc.setFill(Color.YELLOW);
            gc.fillOval(x - RADIUS, y - RADIUS, fallbackSize, fallbackSize);
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            gc.strokeOval(x - RADIUS, y - RADIUS, fallbackSize, fallbackSize);

            // Vẽ mắt
            gc.setFill(Color.BLACK);
            gc.fillOval(x - 5, y - 8, 6, 6);
            // Vẽ mỏ
            gc.setFill(Color.ORANGE);
            gc.fillPolygon(
                    new double[]{x + RADIUS - 2, x + RADIUS + 8, x + RADIUS - 2},
                    new double[]{y - 3, y, y + 3},
                    3
            );
        }
    }

    /**
     * Chim nhảy (flap).
     */
    public void flap() {
        velocityY = JUMP_STRENGTH;
        ResourceManager.playFlapSound();
    }

    /**
     * Kiểm tra chim có chạm ống hay không.
     */
    public boolean collidesWith(Pipe pipe) {
        // Nếu chim chạm vùng X của ống
        if (x + RADIUS > pipe.getX() && x - RADIUS < pipe.getX() + Pipe.WIDTH) {
            // Lấy các thông số của khe hở (gap)
            double gapTopY = pipe.getGapY();
            double gapHeight = pipe.getGapHeight(); // ← dùng getGapHeight() thay vì GAP_HEIGHT
            double gapBottomY = gapTopY + gapHeight;

            // Nếu chim chạm phần trên hoặc phần dưới của ống
            if (y - RADIUS < gapTopY || y + RADIUS > gapBottomY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra chim có chạm sàn hoặc trần (y <= 0 hoặc y >= sceneHeight) không.
     */
    public boolean hitGroundOrRoof() {
        return (y - RADIUS <= 0) || (y + RADIUS >= sceneHeight);
    }

    /* Getters */
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getVelocityY() {
        return velocityY;
    }

    /* Setters (nếu cần) */
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
}
