package net.matez.terr2d.setup;

import net.matez.terr2d.log.Logger;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.render.*;
import net.matez.terr2d.world.World;
import net.matez.terr2d.world.WorldGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class Main extends JFrame {
    private static JFrame frame;
    public static Main instance;
    public static Logger LOGGER;
    public World world;
    public WorldImage worldImage;
    public HUDImage hudImage;
    public DataImage dataImage;
    public Camera camera;
    public AdvancedImagePanel panel;
    public final WorldGenerator generator;
    private static int dragMoveX = 0, dragMoveY = 0, clickPosX, clickPosY;
    public int details = 3;
    public float zoom = 1;
    private int ups;
    private long time;
    private boolean dragging = false;
    private int mouseX, mouseY, oldMouseX, oldMouseY;
    private DataImage dataLabel;
    private String dataText = "";
    private boolean refreshVisuals = false;
    private static boolean showWater = true;
    private static boolean showKeybindings = false;
    private static int heightDiv = 0;

    private String keybindings = "Keybindings:" +
            "\nZoom: SCROLL" +
            "\nDetails: CTRL+SCROLL" +
            "\nRefresh visuals: CTRL+A" +
            "\nToggle water: CTRL+W" +
            "\nHeightmap Details +: CTRL+[" +
            "\nHeightmap Details -: CTRL+]" +
            "\nTake a screenshot: CTRL+S";


    public static void main(String[] args) {
        LOGGER = new Logger();
        LOGGER.progress("Starting app");
        frame = new JFrame();
        frame.setSize(1600, 900);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setTitle("Terra2D - - - made by matez");
        instance = new Main();

        LOGGER.success("Finished");
    }

    public Main() {
        this.world = new World();
        this.generator = new WorldGenerator(world);
        this.worldImage = new WorldImage();
        this.hudImage = new HUDImage();
        this.dataImage = new DataImage();
        this.camera = new Camera();
        camera.setLocation(new BlockPos(frame.getWidth() / 2, 0, frame.getHeight() / 2));
        worldImage.createBufferedImage(frame.getWidth(), frame.getHeight());
        hudImage.createBufferedImage(frame.getWidth(), frame.getHeight());
        dataImage.createBufferedImage(frame.getWidth(), frame.getHeight());

        panel = new AdvancedImagePanel(worldImage.getBufferedImage(), hudImage.getBufferedImage(), dataImage.getBufferedImage());
        panel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        panel.setBackground(Color.BLACK);
        frame.add(panel);

        frame.invalidate();
        frame.validate();
        frame.repaint();

        setupListeners();

        LOGGER.success("Ready");
        gameLoop();
    }

    private void gameLoop(){
        while (true) {
            boolean visualsRefresh = shouldRefreshVisuals();
            camera.setLocation(camera.getLocation().add(dragMoveX, 0, dragMoveY));
            dragMoveX = 0;
            dragMoveY = 0;
            CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    synchronized (generator) {
                        if (!dragging) {
                            generator.generate(camera, worldImage.getWorldWidth(), worldImage.getWorldHeight(), zoom);
                        }
                    }
                }
            });

            worldImage.render(world, camera, false, details, zoom, panel,refreshVisuals);
            hudImage.render(worldImage, camera, details, zoom, mouseX, mouseY, panel,refreshVisuals);
            int screenWidth = frame.getWidth();
            int screenHeight = frame.getHeight();
            float visibleScreenWidth = screenWidth / zoom;
            float visibleScreenHeight = screenHeight / zoom;
            int worldWidth = (int)Math.ceil((float)visibleScreenWidth);
            int worldHeight = (int)Math.ceil((float)visibleScreenHeight);
            BlockPos cameraPos = camera.getLocation();
            int cameraX = cameraPos.getX();
            int cameraZ = cameraPos.getZ();
            int worldStartX = (int)Math.ceil((float)(cameraX - worldWidth/2) / zoom);
            int worldStartZ = (int)Math.ceil((float)(cameraZ - worldHeight/2) / zoom);

            int[] generatedWorldSize = world.getGeneratedWorldSize();
            dataText = "Terra2D Terrain Visualizer" +
                    "\n---------" +
                    "\nCoordinates: " + "x"+(hudImage.getOldPointerX()+worldStartX) + " y"+ worldImage.getYAt(mouseX,mouseY) + " z" +(hudImage.getOldPointerY() + worldStartZ) +
                    "\nWorld Visible Size: " + worldWidth + "x" +worldHeight +
                    "\nWorld Generated Size: " + generatedWorldSize[0] + "x" +generatedWorldSize[1] +
                    "\nScreen Size: " + screenWidth + "x" + screenHeight +
                    "\nZoom: " + (double)zoom + " Details: " + details +
                    "\nBlock size: " + worldImage.getOldBlockSize() + "px2" +
                    "\nHeightmap Details: " + getHeightDiv();

            if(showKeybindings){
                dataText = dataText + "\n\n\n"+keybindings;
            }

            dataImage.render(dataText, panel,refreshVisuals, showKeybindings,18);

            if (time != 0) {
                ups = (int) (System.currentTimeMillis() - time);
            }
            time = System.currentTimeMillis();
            frame.setTitle("Terra2D - - - made by matez - - - " + ups + "ms");

            if(visualsRefresh){
                refreshVisuals = false;
            }
        }
    }

    private boolean isControlDown = false, isAltDown = false;

    public void setupListeners() {
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                clickPosX = e.getX();
                clickPosY = e.getY();
                dragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                dragging = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                mouseX = -1;
                mouseY = -1;
                dragging = false;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                int dx = e.getX() - clickPosX;
                int dy = e.getY() - clickPosY;
                dragMoveX = dragMoveX - dx;
                dragMoveY = dragMoveY - dy;
                clickPosX = e.getX();
                clickPosY = e.getY();
                dragging = true;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

        });
        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                keybinding(e,false);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                keybinding(e,true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keybinding(e,false);
            }
        });

        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int val = e.getUnitsToScroll() / 3;
                if (isControlDown) {
                    details += val;
                    if (details <= 0) {
                        details = 1;
                    }
                } else {
                    zoom += (float) val / 30;
                    if (zoom < 0) {
                        zoom = 0;
                    }
                }
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if (worldImage.getWorldWidth() == evt.getComponent().getWidth() && worldImage.getWorldHeight() == evt.getComponent().getHeight()) {
                    return;
                }
                if (evt.getComponent() != frame) {
                    return;
                }
                worldImage.createBufferedImage(frame.getWidth(), frame.getHeight());
                hudImage.createBufferedImage(frame.getWidth(), frame.getHeight());
                dataImage.createBufferedImage(frame.getWidth(), frame.getHeight());
                panel.setImage(worldImage.getBufferedImage(), hudImage.getBufferedImage(), dataImage.getBufferedImage());
                panel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
                frame.invalidate();
                frame.validate();
                frame.repaint();
            }
        });
        panel.setFocusable(true);
        panel.requestFocus();
    }

    public boolean shouldRefreshVisuals() {
        return refreshVisuals;
    }

    public static boolean shouldShowWater() {
        return showWater;
    }

    public static int getHeightDiv() {
        return heightDiv;
    }

    private void keybinding(KeyEvent e, boolean press){
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            //clickedNoise=-1;
        }
        if(press) {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                dragMoveX++;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                dragMoveY++;
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                dragMoveX--;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                dragMoveY--;
            }
        }

        if (e.isControlDown()) {
            if(press) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    dragMoveX += 5;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    dragMoveY += 5;
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    dragMoveX -= 5;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    dragMoveY -= 5;
                }
            }

            //keybindings
            if (e.isShiftDown()) {
                showKeybindings=true;
            }else{
                showKeybindings=false;
            }
            if(press) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    refreshVisuals = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    if (showWater) {
                        showWater = false;
                    } else {
                        showWater = true;
                    }
                    refreshVisuals = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
                    heightDiv++;
                    refreshVisuals = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
                    if(heightDiv>0){
                        heightDiv--;
                        refreshVisuals = true;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    ScreenshotCreator creator = new ScreenshotCreator(generator,world,camera,frame.getWidth(),frame.getHeight(),details,zoom);
                }
            }
        }else{
            showKeybindings = false;
        }
        isControlDown = e.isControlDown();
        isAltDown = e.isAltDown();
    }
}
