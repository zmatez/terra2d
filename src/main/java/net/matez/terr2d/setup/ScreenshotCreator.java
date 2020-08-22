package net.matez.terr2d.setup;

import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.render.Camera;
import net.matez.terr2d.render.ChunkRenderer;
import net.matez.terr2d.render.DataImage;
import net.matez.terr2d.render.WorldRenderer;
import net.matez.terr2d.world.World;
import net.matez.terr2d.world.WorldGenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class ScreenshotCreator extends JDialog {
    private static JDialog frame;
    private JProgressBar bar;
    private Font font = new Font("Segoe UI", Font.PLAIN, 12);

    public ScreenshotCreator(WorldGenerator generator, ChunkRenderer renderer,World world, Camera camera, int screenWidth, int screenHeight, float zoom) {
        Main.LOGGER.progress("Starting Screenshot Creator");
        frame = new JDialog();
        frame.setSize(250, 340);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setLayout(null);
        frame.setTitle("Screenshot Creator");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame.setVisible(true);

        JLabel cameraLabel = new JLabel("Customize camera position:");
        cameraLabel.setFont(font);
        cameraLabel.setBounds(5, 5, frame.getWidth() - 10, 15);
        frame.add(cameraLabel);
        JSpinner camXSpinner = new JSpinner();
        camXSpinner.setValue(camera.getLocation().getX());
        camXSpinner.setBounds(5, 20, 115, 20);
        frame.add(camXSpinner);
        JSpinner camZSpinner = new JSpinner();
        camZSpinner.setValue(camera.getLocation().getZ());
        camZSpinner.setBounds(120, 20, 115, 20);
        frame.add(camZSpinner);

        SpinnerNumberModel widthModel = new SpinnerNumberModel(1, 1, null, 1);
        SpinnerNumberModel heightModel = new SpinnerNumberModel(1, 1, null, 1);
        JLabel sizeLabel = new JLabel("Customize output resolution");
        sizeLabel.setFont(font);
        sizeLabel.setBounds(5, 55, frame.getWidth() - 10, 15);
        frame.add(sizeLabel);
        JSpinner sizeWidthSpinner = new JSpinner();
        sizeWidthSpinner.setModel(widthModel);
        sizeWidthSpinner.setValue(screenWidth);
        sizeWidthSpinner.setBounds(5, 70, 115, 20);
        frame.add(sizeWidthSpinner);
        JSpinner sizeHeightSpinner = new JSpinner();
        sizeHeightSpinner.setModel(heightModel);
        sizeHeightSpinner.setValue(screenHeight);
        sizeHeightSpinner.setBounds(120, 70, 115, 20);
        frame.add(sizeHeightSpinner);

        Float floatValue = zoom;
        Float floatStep = 0.25f;
        SpinnerNumberModel zoomModel = new SpinnerNumberModel(floatValue, 0.000001F, null, floatStep);
        JLabel zoomLabel = new JLabel("Zoom");
        zoomLabel.setFont(font);
        zoomLabel.setBounds(5, 105, frame.getWidth() - 10, 15);
        frame.add(zoomLabel);
        JSpinner zoomSpinner = new JSpinner();
        zoomSpinner.setModel(zoomModel);
        zoomSpinner.setValue(zoom);
        zoomSpinner.setBounds(5, 120, 115, 20);
        frame.add(zoomSpinner);

        JLabel pathLabel = new JLabel("Output file path");
        pathLabel.setFont(font);
        pathLabel.setBounds(5, 205, frame.getWidth() - 10, 15);
        frame.add(pathLabel);
        JTextField field = new JTextField();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                .withZone(ZoneId.systemDefault());
        Instant instant = Instant.now();
        String output = formatter.format( instant );
        field.setText(System.getProperty("user.home") + "/Desktop/terra2d-"+output+".png");
        field.setBounds(5, 220, 200, 20);
        frame.add(field);
        JButton selectPath = new JButton("#");
        selectPath.setBounds(210, 220, 30, 20);
        frame.add(selectPath);

        selectPath.addActionListener(a -> {
            DateTimeFormatter formatterx = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                    .withZone(ZoneId.systemDefault());
            Instant instantx = Instant.now();
            String outputx = formatterx.format( instantx );
            JFileChooser chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
            chooser.setSelectedFile(new File("terra2d-"+outputx+".png"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "PNG Files", "png");
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                field.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton button = new JButton("Screenshot");
        button.setBounds(20, 270, 205, 20);
        frame.add(button);

        button.addActionListener(a -> {
            if(camXSpinner.getValue()!=null && camZSpinner.getValue()!=null && sizeWidthSpinner.getValue()!=null && sizeHeightSpinner.getValue()!=null && zoomSpinner.getValue()!=null && !field.getText().isEmpty()){
                File f = new File(field.getText());
                if(!f.isDirectory()){
                    takeScreenshot(generator,renderer,world,(int)camXSpinner.getValue(),(int)camZSpinner.getValue(),(int)sizeWidthSpinner.getValue(),(int)sizeHeightSpinner.getValue(),(float)zoomSpinner.getValue(),f);
                }
            }
        });

        bar = new JProgressBar();
        bar.setBounds(5, 295, 235, 10);
        frame.add(bar);

        frame.invalidate();
        frame.validate();
        frame.repaint();
    }

    public void takeScreenshot(WorldGenerator generator, ChunkRenderer chunkRenderer, World world, int cameraX, int cameraZ, int outputResWidth, int outputResHeight, float zoom, File output) {
        Main.LOGGER.progress("Taking screenshot...");
        bar.setIndeterminate(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                WorldRenderer renderer = new WorldRenderer();
                DataImage data = new DataImage();
                renderer.create(outputResWidth, outputResHeight);
                data.createBufferedImage(outputResWidth, outputResHeight);
                Camera camera = new Camera();
                camera.setLocation(new BlockPos(cameraX, 0, cameraZ));
                generator.generate(camera,outputResWidth,outputResHeight);
                chunkRenderer.renderChunks(world,camera,outputResWidth,outputResHeight,false);
                int worldStartX = (int)Math.ceil((float)(cameraX - outputResWidth/2) / zoom);
                int worldStartZ = (int)Math.ceil((float)(cameraZ - outputResHeight/2) / zoom);
                int worldEndX = (int)Math.ceil((float)(cameraX + outputResWidth/2) / zoom);
                int worldEndZ = (int)Math.ceil((float)(cameraZ + outputResHeight/2) / zoom);
                int worldSizeX = (Math.abs(worldStartX)+worldEndX);
                int worldSizeZ = (Math.abs(worldStartZ)+worldEndZ);
                renderer.render(world, camera, false, false,zoom);

                int fontSize = outputResWidth/100;

                data.render(("Terra2D Terrain Visualizer\nmade by matez for WildNature mod\n-----------\nCamPos: " + cameraX + "x" + cameraZ + "Zoom: " + zoom + "\nWorld Size: " + worldSizeX + "x" + worldSizeZ),false,false,fontSize);
                BufferedImage bufferedImage = new BufferedImage(outputResWidth,outputResHeight,BufferedImage.TYPE_INT_ARGB);
                BufferedImage worldImage = renderer.getBufferedImage();
                BufferedImage dataImage = data.getBufferedImage();
                Graphics graphics = bufferedImage.getGraphics();
                graphics.drawImage(worldImage,0,0,null);
                graphics.drawImage(dataImage,0,0,null);

                try {
                    ImageIO.write(bufferedImage, "png", output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Main.LOGGER.success("Saved screenshot as " + output.getAbsolutePath());
                bar.setIndeterminate(false);
                bar.setValue(100);
                frame.dispose();
                try {
                    Desktop.getDesktop().open(output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
