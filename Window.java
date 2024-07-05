import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Window extends JFrame {
    private final int WIDTH = 800;
    private final int HEIGHT = 800;
    private double zoom = 200;
    private double offsetX = -2.0;
    private double offsetY = -2.0;

    private BufferedImage image;

    public Window() {
        setTitle("Mandelbrot Set");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        addKeyListener(new KeyAdapter() 
        {
            public void keyPressed(KeyEvent e) 
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                {
                	zoomToPoint(WIDTH / 2, HEIGHT / 2, 1.01);
                    renderFractal();
                    repaint();
                } 
                else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) 
                {
                	zoomToPoint(WIDTH / 2, HEIGHT / 2, 1.0 / 1.01);
                    renderFractal();
                    repaint();
                } 
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) 
                {
                    offsetX += 4 / zoom;
                    renderFractal();
                    repaint();
                } 
                else if (e.getKeyCode() == KeyEvent.VK_LEFT) 
                {
                    offsetX -= 4 / zoom;
                    renderFractal();
                    repaint();
                } 
                else if (e.getKeyCode() == KeyEvent.VK_UP) 
                {
                    offsetY -= 4 / zoom;
                    renderFractal();
                    repaint();
                } 
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) 
                {
                    offsetY += 4 / zoom;
                    renderFractal();
                    repaint();
                }
            }
        });

        setFocusable(true);
        renderFractal();
    }
    private void zoomToPoint(int x, int y, double factor) 
    {
        double oldZoom = zoom;
        zoom *= factor;

        offsetX = (x / oldZoom + offsetX) - (x / zoom);
        offsetY = (y / oldZoom + offsetY) - (y / zoom);
    }

    private void renderFractal() 
    {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int x = 0; x < WIDTH; x++) {
            final int finalX = x;
            executor.execute(() -> drawFractalColumn(finalX));
        }
        executor.shutdown();
        try 
        {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void drawFractalColumn(int x) {
        for (int y = 0; y < HEIGHT; y++) {
            double zx = x / zoom + offsetX;
            double zy = y / zoom + offsetY;
            int iter = calculateMandelbrot(zx, zy);
            int color = iter % 256;
            image.setRGB(x, y, new Color(color, color, color).getRGB());
        }
    }

    private int calculateMandelbrot(double zx, double zy) {
        double x = zx;
        double y = zy;

        int iter = 0;
        while (iter < 256 && x * x + y * y < 4.0) {
            double xtemp = x * x - y * y + zx;
            y = 2 * x * y + zy;
            x = xtemp;
            iter++;
        }

        return iter;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window mandlebrot = new Window();
            mandlebrot.setVisible(true);
        });
    }
}
