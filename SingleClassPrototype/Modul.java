
import Control.RobotControl;
import Interfaces.*;
import Model.ModelDeprecated;
import SpecialSettingsEtc.*;
import view.Camera;
import Model.*;
import Model.DijkstraPathFinder;
import view.View;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;

import Model.RoboPos;


/** This class defines the basic structure of our program
 * It is defined by the model view controller architecture.
 * view is responsible for processing the image stream and showing it on screen
 * model is responsible for converting the visual to the internal data model
 * and it creates pathways
 * RobotControl then converts the path order in orders fors the robot.
 */
public class Modul {
    private static Random rnd = new RandomCount();



    private static View factoryView = new View();
    private static ModelDeprecated factoryModelDeprecated = new ModelDeprecated(rnd);
    private static RobotControl factoryControl = new RobotControl();
    private static Camera factoryCamera = new Camera();


    private Thread processingThread;
    public IView view;
    public IModel model;
    public IControl control;
    public ICamera camera;
    private boolean[] workmodes = new boolean[Workmode.values().length]; //which workmodes are turned on.
    private boolean running;
    private int graphSkip = Settings.getGraphCreationPixelSkip();
    private DijkstraPathFinder g;


    public Modul(View view , ModelDeprecated modelDeprecated, IControl control, ICamera camera) {
        this.view = view;
        this.model = modelDeprecated;
        this.control = control;
        this.camera = camera;
    }


    public static void main(String[] args){

        try {
            Modul modul = new Modul(factoryView.getInstance(), factoryModelDeprecated.getInstance(),factoryControl.getInstance(), factoryCamera.getInstance());

            modul.setWorkmode(Workmode.SIMPLECLASSIFICATORANDNOTJODISPECIALSAUCE, true);
            modul.setWorkmode(Workmode.JODISPECIALSAUCEANDNOTSIMPLECLASSIFICATOR, false);
            modul.setWorkmode(Workmode.SYSTEMOUT, true);
            modul.setWorkmode(Workmode.SYSTEMOUTARCHIVE, false);
            modul.setWorkmode(Workmode.SHOWKLASSIFIED,true);
            modul.setWorkmode(Workmode.SHOWASTAR, true);
            modul.setWorkmode(Workmode.SHOWTESSELATED, true);
            modul.start(true);



        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }



    }

    private void mainLoop() {
        running = true;
        int loop = -1;
        RoboPos lastPos = null;
        Classifier cl = new Classifier();
        cl.editFields();

        while (running){
            loop++;
            long loopStart = System.currentTimeMillis();
            //something is happening
            windowManagment();
            RoboPos robotPos = new RoboPos(0,0,0);

            //https://www.youtube.com/watch?v=bjSpO2B6G4s
            if (isWorkmode(Workmode.SIMPLECLASSIFICATORANDNOTJODISPECIALSAUCE)) {
                //previous classifier approach below
                BufferedImage bi = view.getCurrentShot();

                ObjectType[][] m2 = view.classify(bi, isWorkmode(Workmode.SHOWKLASSIFIED), cl);
                robotPos = view.getRobotCenter(m2, 4).get(0);
                System.out.println("Robot position is " + robotPos.getX() + ":" + robotPos.getY());
                if (g != null) g.setVisible(false);
                g = view.getGraph(m2, bi.getType(), robotPos, graphSkip, isWorkmode(Workmode.SHOWASTAR));
                LinkedList<Node> path = g.calculatePathway(robotPos,0,0,isWorkmode(Workmode.SHOWASTAR));

                //previous classifier approach above
            }


            if (isWorkmode(Workmode.JODISPECIALSAUCEANDNOTSIMPLECLASSIFICATOR)) {
                //openCV approach below
                //markus had to comment out, because preprocess doesnt seem to be part of computer vision anymore
               // Mat test = ComputerVision.preprocess(Settings.getInputPath());
              //  ArrayList<Integer> positions = ComputerVision.retrieveRobot(test, 0, 0);
                //robotPos = new RoboPos(positions.get(0), positions.get(1), positions.get(2));
               // MatOfPoint contour = ComputerVision.retrieveContour(test, positions);
               // LinkedList<Node> path = ComputerVision.retrieveDijcstraGrid(test, new MatOfPoint2f(contour.toArray()), positions, 10);
                //openCV approach above
            }

            double direction = 0;
            if (lastPos == null){
                control.move(Tangential.Direction.forward);
            }else {
                direction = Math.atan2(robotPos.getX()-lastPos.getX(), -1 * (robotPos.getY()-lastPos.getY()));
            }
            //control.sendCommand(1100,1800,robotPos,direction,path);
            lastPos = robotPos;



            long loopEnd = System.currentTimeMillis();
            double timeHappend = (loopEnd - loopStart)/1000.;
            Archivar.shout("Loop: "+ loop + " took " + timeHappend + " seconds to complete");
        }
        System.out.println("Finished programm at " + System.currentTimeMillis());
    }






    private void windowManagment() {
        show(Workmode.SHOWSENSOR,isWorkmode(Workmode.SHOWSENSOR));
    }


    private void start(boolean b) {

        stopProcessingThread();
        if (!b) return;

        // TODO: find suitable values for the final camera-maze setup
        camera.startCamera(60, 1, 1300, 2000, 75,  Settings.getInputPath(), 0.075, 0.1, 0.8, 0.8);
        control.startConnection();

        processingThread = new Thread() {

            public void run(){
                mainLoop();
            }
        };
        processingThread.start();
    }



    private void stopProcessingThread() {
        running = false;

        camera.stopCamera();
        control.closeConnection();
        processingThread = null;
    }

    private void show(Workmode tesselated, boolean b) {
        switch (tesselated) {

            case SYSTEMOUT:
                break;
            case SHOWKLASSIFIED:
                break;
            case SHOWTESSELATED:
                break;
            case SHOWSENSOR:
                break;
        }
    }

    public boolean isWorkmode(Workmode w){
        return workmodes[w.ordinal()];
    }

    private void setWorkmode(Workmode w, boolean b) {
        workmodes[w.ordinal()] = b;
        if (w.equals(Workmode.SYSTEMOUT)) Archivar.liveOutput(b);
        if (w.equals(Workmode.SYSTEMOUTARCHIVE)) Archivar.setStoreShouts(b);
    }









    /**
     * extendes random so the time it is executed is measured
     */
    private static class RandomCount extends Random {
        private static long rndCount = 0l;

        @Override
        public double nextDouble() {
            rndCount++;
            return super.nextDouble();
        }

        @Override
        public long nextLong() {
            rndCount++;
            return super.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            rndCount++;
            return super.nextBoolean();
        }

        @Override
        public int nextInt() {
            rndCount++;
            return super.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            rndCount++;
            return super.nextInt(bound);
        }
    }


}
