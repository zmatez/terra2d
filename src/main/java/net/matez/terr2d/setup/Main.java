package net.matez.terr2d.setup;

import net.matez.terr2d.log.Logger;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.render.Camera;
import net.matez.terr2d.render.ImagePanel;
import net.matez.terr2d.render.WorldImage;
import net.matez.terr2d.render.HUDImage;
import net.matez.terr2d.world.World;
import net.matez.terr2d.world.WorldGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CompletableFuture;

public class Main extends JFrame {
    private static JFrame frame;
    public static Main instance;
    public static Logger LOGGER;
    public World world;
    public WorldImage worldImage;
    public HUDImage hudImage;
    public Camera camera;
    public ImagePanel panel, hud;
    public final WorldGenerator generator;
    private static int moveX=0, moveY=0, clickPosX, clickPosY;
    public int details = 3;
    public float zoom = 1;
    private int ups;
    private long time;
    private boolean dragging = false;
    private int mouseX, mouseY;

    public static void main(String[] args){
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
        LOGGER.success("Ready");
    }

    public Main(){
        this.world = new World();
        this.generator = new WorldGenerator(world);
        this.worldImage = new WorldImage();
        this.hudImage = new HUDImage();
        this.camera = new Camera();
        camera.setLocation(new BlockPos(frame.getWidth()/2,0,frame.getHeight()/2));
        worldImage.createBufferedImage(frame.getWidth(),frame.getHeight());
        hudImage.createBufferedImage(frame.getWidth(),frame.getHeight());
        panel = new ImagePanel(worldImage.getBufferedImage());
        panel.setBounds(0,0,frame.getWidth(),frame.getHeight());
        panel.setBackground(Color.BLACK);
        frame.add(panel);
        hud = new ImagePanel(hudImage.getBufferedImage());
        hud.setBounds(0,0,frame.getWidth(),frame.getHeight());
        hud.setBackground(new Color(0,0,0,0));
        frame.add(hud);

        frame.invalidate();
        frame.validate();
        frame.repaint();

        setupListeners();


        while(true){
            camera.setLocation(camera.getLocation().add(moveX,0,moveY));
            moveX = 0;
            moveY = 0;
            CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    synchronized (generator) {
                        if(!dragging) {
                            generator.generate(camera, worldImage.getWorldWidth(), worldImage.getWorldHeight(), zoom);
                        }
                    }
                }
            });

            worldImage.render(world,camera,false, details, zoom,panel);
            hudImage.render(camera,details,zoom,mouseX,mouseY,hud);
            if(time!=0){
                ups = (int)(System.currentTimeMillis() - time);
            }
            time = System.currentTimeMillis();
            frame.setTitle("Terra2D - - - made by matez - - - "+ ups +"ms");
        }
    }

    private boolean isControlDown = false, isAltDown = false;
    public void setupListeners(){
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                /*if(e.getButton()==MouseEvent.BUTTON1) {
                    double noise = noise(e.getX(), e.getY(), (double) redist.getValue() / 10000);
                    System.out.println("-----------> " + noise + " at " + e.getX() + " : " + e.getY());
                    clickedNoise = noise;
                }else{
                    clickedNoise=-1;
                }*/
            }

            @Override
            public void mousePressed(MouseEvent e) {
                clickPosX=e.getX();
                clickPosY=e.getY();
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
                moveX = moveX-dx;
                moveY = moveY-dy;
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
                isControlDown = e.isControlDown();
                isAltDown = e.isAltDown();
            }

            @Override
            public void keyPressed(KeyEvent e) {

                if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                    //clickedNoise=-1;
                }
                if(e.getKeyCode()==KeyEvent.VK_RIGHT){
                    moveX++;
                }
                if(e.getKeyCode()==KeyEvent.VK_DOWN){
                    moveY++;
                }
                if(e.getKeyCode()==KeyEvent.VK_LEFT){
                    moveX--;
                }
                if(e.getKeyCode()==KeyEvent.VK_UP){
                    moveY--;
                }

                if(e.isControlDown()){
                    if(e.getKeyCode()==KeyEvent.VK_RIGHT){
                        moveX+=5;
                    }
                    if(e.getKeyCode()==KeyEvent.VK_DOWN){
                        moveY+=5;
                    }
                    if(e.getKeyCode()==KeyEvent.VK_LEFT){
                        moveX-=5;
                    }
                    if(e.getKeyCode()==KeyEvent.VK_UP){
                        moveY-=5;
                    }
                }
                isControlDown = e.isControlDown();
                isAltDown = e.isAltDown();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                isControlDown = e.isControlDown();
                isAltDown = e.isAltDown();
            }
        });

        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int val = e.getUnitsToScroll()/3;
                if(isControlDown) {
                    details += val;
                    if (details <= 0) {
                        details = 1;
                    }
                }else{
                    zoom += (float)val/30;
                    if (zoom < 0) {
                        zoom = 0;
                    }
                }
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if(worldImage.getWorldWidth() == evt.getComponent().getWidth() && worldImage.getWorldHeight() == evt.getComponent().getHeight()){
                    return;
                }
                if(evt.getComponent() != frame){
                    return;
                }
                worldImage.createBufferedImage(frame.getWidth(),frame.getHeight());
                hudImage.createBufferedImage(frame.getWidth(),frame.getHeight());
                panel.setImage(worldImage.getBufferedImage());
                panel.setBounds(0,0,frame.getWidth(),frame.getHeight());
                hud.setImage(hudImage.getBufferedImage());
                hud.setBounds(0,0,frame.getWidth(),frame.getHeight());
                frame.invalidate();
                frame.validate();
                frame.repaint();
            }
        });
        panel.setFocusable(true);
        panel.requestFocus();
    }
}
