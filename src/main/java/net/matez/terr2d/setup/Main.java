package net.matez.terr2d.setup;

import net.matez.terr2d.log.Logger;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.render.*;
import net.matez.terr2d.world.World;
import net.matez.terr2d.world.WorldGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CompletableFuture;

public class Main extends JFrame {
    //setup
    private static JFrame frame;
    public static Main instance;
    public static Logger LOGGER;
    public static int width, height;
    public static int zoomedWidth, zoomedHeight;

    //data
    private static int dragMoveX = 0, dragMoveY = 0;
    private int mouseX, mouseZ;
    private int clickX, clickZ;
    private boolean isControlDown = false, isAltDown = false;
    private boolean dragging = false;

    //settings
    private int dragSmoothness = 1;
    public static float zoom = 1;

    private static boolean refreshVisuals = false;
    private static boolean showWater = false;
    private static boolean showKeybindings = false;
    private static boolean renderChunkData = false;
    private static int heightDiv = 0;

    //metering
    private int ups;
    private long time;

    //instances
    public World world;
    public HUDImage hudImage;
    public DataImage dataImage;
    public Camera camera;
    public AdvancedImagePanel panel;
    public ChunkRenderer chunkRenderer;
    public WorldRenderer worldRenderer;
    public final WorldGenerator generator;

    private String keybindings = "Keybindings:" +
            "\nZoom: SCROLL" +
            "\nDetails: CTRL+SCROLL" +
            "\nRefresh visuals: CTRL+A" +
            "\nToggle water: CTRL+W" +
            "\nToggle chunk details: CTRL+D" +
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
        frame.setTitle("Terra2D - by matez");
        instance = new Main();

        LOGGER.success("Finished");
    }

    public Main() {
        world = new World();
        generator = new WorldGenerator(world);
        hudImage = new HUDImage();
        dataImage = new DataImage();
        camera = new Camera();
        chunkRenderer = new ChunkRenderer();
        worldRenderer = new WorldRenderer();

        width = frame.getWidth();
        height = frame.getHeight();
        zoomedWidth = Math.round(width * zoom);
        zoomedHeight = Math.round(height * zoom);

        camera.setLocation(new BlockPos(width / 2, 0, height / 2));

        worldRenderer.create(zoomedWidth, zoomedHeight);
        hudImage.createBufferedImage(zoomedWidth, zoomedHeight);
        dataImage.createBufferedImage(width, height);

        panel = new AdvancedImagePanel(worldRenderer.getBufferedImage(), hudImage.getBufferedImage(), dataImage.getBufferedImage(),width,height);
        panel.setBounds(0, 0, width, height);
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

            updateApp();
            renderApp();

            if (time != 0) {
                ups = (int) (System.currentTimeMillis() - time);
            }
            time = System.currentTimeMillis();
            frame.setTitle("Terra2D - - - made by matez - - - " + ups + "ms");
        }
    }

    private void updateApp(){
        camera.setLocation(camera.getLocation().add(Math.round(dragMoveX*zoom), 0, Math.round(dragMoveY*zoom)));
        dragMoveX=0;
        dragMoveY=0;

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (generator) {
                    if (!dragging) {
                        generator.generate(camera, worldRenderer.getWidth(), worldRenderer.getHeight());
                    }
                }
            }
        });
    }

    private void renderApp(){
        boolean visualsRefresh = shouldRefreshVisuals();

        chunkRenderer.setRenderWater(shouldShowWater());

        //
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (chunkRenderer) {
                    chunkRenderer.renderChunks(world,camera, worldRenderer.getWidth(), worldRenderer.getHeight(), refreshVisuals);
                }
            }
        });

        worldRenderer.render(world,camera, renderChunkData,refreshVisuals,zoom);
        hudImage.render(camera,zoom, mouseX, mouseZ,refreshVisuals);
        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();
        int worldStartX = (int) Math.ceil((float) (cameraX - width / 2));
        int worldStartZ = (int) Math.ceil((float) (cameraZ - height / 2));

        int pointerX = (hudImage.getOldPointerX()+worldStartX);
        int pointerZ = (hudImage.getOldPointerY()+worldStartZ);

        int pointerY = world.getChunk(pointerX,pointerZ).getColumn(pointerX,pointerZ).getColumnMaxHeight();

        //int[] generatedWorldSize = world.getGeneratedWorldSize();
        String dataText = "Terra2D Terrain Visualizer" +
                "\n---------" +
                "\nCoordinates: " + "x"+ pointerX + " y" + pointerY+ " z" + pointerZ +
                "\nWorld Generated Size: " + /*generatedWorldSize[0] + "x" +generatedWorldSize[1] +*/
                "\nScreen Size: " + width + "x" + height +
                "\nZoom: " + (double)zoom +
                "\nHeightmap Details: " + getHeightDiv();

        if(showKeybindings){
            dataText = dataText + "\n\n\n"+keybindings;
        }

        dataImage.render(dataText,refreshVisuals, showKeybindings,18);
        //

        panel.invalidate();
        panel.validate();
        panel.repaint();

        if(visualsRefresh){
            refreshVisuals = false;
        }
    }
    
    public void setupListeners() {
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                clickX = e.getX();
                clickZ = e.getY();
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
                mouseZ = -1;
                dragging = false;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                int dx = e.getX() - clickX;
                int dy = e.getY() - clickZ;
                dragMoveX = dragMoveX - dx;
                dragMoveY = dragMoveY - dy;
                clickX = e.getX();
                clickZ = e.getY();
                dragging = true;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseZ = e.getY();
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
                zoom += (float) val / 30;
                if (zoom < 0.1f) {
                    zoom = 0.1f;
                }
                zoom(zoom);
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if (worldRenderer.getWidth() == evt.getComponent().getWidth() && worldRenderer.getHeight() == evt.getComponent().getHeight()) {
                    return;
                }
                if (evt.getComponent() != frame) {
                    return;
                }

                zoom(zoom);
                panel.setBounds(0, 0, width, height);
                frame.invalidate();
                frame.validate();
                frame.repaint();
            }
        });
        panel.setFocusable(true);
        panel.requestFocus();
    }

    public void zoom(float newZoom){
        zoom = newZoom;
        width = frame.getWidth();
        height = frame.getHeight();
        zoomedWidth = Math.round(width * zoom);
        zoomedHeight = Math.round(height * zoom);
        worldRenderer.create(zoomedWidth, zoomedHeight);
        hudImage.createBufferedImage(zoomedWidth, zoomedHeight);
        dataImage.createBufferedImage(width, height);
        panel.setImage(worldRenderer.getBufferedImage(), hudImage.getBufferedImage(), dataImage.getBufferedImage(),width,height);
        frame.invalidate();
        frame.validate();
        frame.repaint();
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
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    if (renderChunkData) {
                        renderChunkData = false;
                    } else {
                        renderChunkData = true;
                    }
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
                    ScreenshotCreator creator = new ScreenshotCreator(generator,chunkRenderer,world,camera,width,height,zoom);
                }
            }
        }else{
            showKeybindings = false;
        }
        isControlDown = e.isControlDown();
        isAltDown = e.isAltDown();
    }

    public static void setRefreshVisuals(boolean r) {
        refreshVisuals = r;
    }
}
