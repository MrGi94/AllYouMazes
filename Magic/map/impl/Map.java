package Magic.map.impl;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.builders.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A map defines polygon objects on which to "play" the pursuit-evasion game
 *
 * A map consists of floors.
 *
 * Each floor can contain obstacles
 *
 * Created by nik on 2/8/17.
 */
public class Map implements Serializable
{
    private static Logger logger = LoggerFactory.getLogger(Map.class);

    /**
     * Every map needs a name
     */
    private String name = "";

    /**
     * Floors in the map
     */
    private Collection<Floor> floors;

    /**
     * Construct the map created by one or more floors
     *
     * @param floors the floors which make up the map
     */
    public Map(Collection<Floor> floors)
    {
        this.floors = Collections.unmodifiableCollection(floors);
    }

    /**
     * Get a read-only Collection of the floors which make up this map
     *
     * @return the collection of floors on this map
     */
    public Collection<Floor> getFloors()
    {
        return floors;
    }

    public static Map loadFile() {

            JFileChooser fc = new JFileChooser("");
            fc.setCurrentDirectory(new File( System.getProperty("user.dir")));
            fc.setAcceptAllFileFilterUsed(true);
            fc.setMultiSelectionEnabled(false);
            fc.setFileFilter(new FileNameExtensionFilter("Serialized","ser"));
           // fc.setLocation(parent.getLocation());
            fc.setVisible(true);


            int returnVal = fc.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +
                        fc.getSelectedFile().getName());



                File f = fc.getSelectedFile();


            try {
                FileInputStream s = new FileInputStream(f);
                ObjectInputStream o = new ObjectInputStream(s);
                Map m = (Map)o.readObject();
                return m;
            }
            catch (IOException | ClassNotFoundException e){
               e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * @param fc the file chooser
     * @param extension like ".txt"
     * @return if
     */
    public static File addExtension(JFileChooser fc, String extension) {
        File file = fc.getSelectedFile();
        String path = file.getAbsolutePath();



        if(!path.endsWith("." + extension))
        {
            file = new File(path +"."+ extension);
        }
        return file;
    }

    public static void saveToFile(Map map) {
        JFileChooser fc = new JFileChooser("");
        fc.setCurrentDirectory(new File( System.getProperty("user.dir")));
        fc.setAcceptAllFileFilterUsed(true);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileNameExtensionFilter("Serialized","ser"));
        // fc.setLocation(parent.getLocation());
        fc.setVisible(true);

        int returnVal = fc.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to save this file: " +
                    fc.getSelectedFile().getName());
            File f = addExtension(fc,"ser");


        FileOutputStream s;
        try{
            s = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(s);
            os.writeObject(map);
            os.close();
            s.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
         }
    }

    /**
     * Get the collection of polygons which make up all objects of this map
     *
     * @return An ArrayList of Polygons making up this map
     */
    public Collection<MapPolygon> getPolygons()
    {
        ArrayList<MapPolygon> polygons = new ArrayList<>();
        fillPolygonList(polygons);
        polygons.trimToSize();
        return polygons;
    }

    /**
     * Fill a Collection of polygons with all Polygons of this map
     *
     * @param polygons the list of polygons to fill
     */
    private void fillPolygonList(Collection<MapPolygon> polygons)
    {
        fillObjectList(floors, polygons);
        for(Floor floor : floors)
        {
            fillObjectList(floor.getObstacles(), polygons);
            fillObjectList(floor.getGates(), polygons);
        }
    }

    /**
     * Fill a collection of polygons with the polygons of a collection of AbstractObjects
     *
     * @param collection the collection of AbstractObjects
     * @param polygons the collection of polygons to fill
     */
    private void fillObjectList(Collection<? extends AbstractObject> collection, Collection<MapPolygon> polygons)
    {
        for(AbstractObject object : collection)
        {
            polygons.add(object.getPolygon());
        }
    }

    /**
     * Get a simple Map which is a rectangle floor with a box in the middle

     * @return A map object containing a simple map
     */
    public static Map getSimpleMap()
    {
        return getMap("level.ser");

    }

    public static Map getTestMap(){
        MapBuilder bob = MapBuilder.create();
        MapPolygon p = new MapPolygon(
                new int[]{2, 466, 462, 9},
                new int[]{5, 6, 386, 380},
                4, false);
        return bob.makeFloor(p)
                .addObstacle(
                        new MapPolygon(
                                new int[]{132, 228, 141},
                                new int[]{70, 85, 160},
                                3, true)
                )
                .addObstacle(
                        new MapPolygon(
                                new int[]{372,413,421,428,411},
                                new int[]{60,59,105,165,173},
                                5, true)
                )
                .addObstacle(
                        new MapPolygon(
                                new int[]{319, 349, 221, 238},
                                new int[]{133, 303, 326, 248},
                                4, true)
                )
                .addObstacle(
                        new MapPolygon(
                                new int[]{78, 134, 115, 91, 76},
                                new int[]{248, 295, 330, 333, 314},
                                5, true)
                ).finish().build();
    }

    public static Map getMap(String path) {

        try {
            FileInputStream s = new FileInputStream(path);
            ObjectInputStream o = new ObjectInputStream(s);
            Map m = (Map)o.readObject();
            return m;
        }
        catch (IOException | ClassNotFoundException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error loading map: {}", e);
            }
            return getSimpleTestMap();
        }
    }

    public static Map getSimpleTestMap() {
        MapPolygon mainFloor = new MapPolygon(
                new int[] {   0, 600, 600,   0},
                new int[] {   0,   0, 600, 600},
                4,
                false
        );

        MapPolygon obstacle = new MapPolygon(
                new int[] {240, 360, 360, 240},
                new int[] {240, 240, 360, 360},
                4,
                true
        );

        return MapBuilder.create()
                .makeFloor(mainFloor)
                .addObstacle(obstacle)
                .finish()
                .build();
    }


    public static Map getSimpleMapNoHoles(){
        MapBuilder b = MapBuilder.create();
        return b.makeFloor(new MapPolygon(
                new int[] { 200, 500, 700, 700, 500, 200, 0, 0},
                new int[] { 0, 0, 200, 500, 700, 700, 500, 200},
                8,
                false
        )).finish().build();
    }

    public boolean isInsideAndLegal(Vector2D v) {
        Collection<Floor> mp = this.getFloors();

        for (Floor f : mp){

            if (f.getPolygon().contains(v.getX(),v.getY())){
                for (Obstacle ob : f.getObstacles()){
                    if (ob.getPolygon().getBounds2D().contains(v.getX(),v.getY())){
                        if (ob.getPolygon().contains(v.getX(),v.getY()))return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    public Vector2D getEvaderSpawnLocation() {
        Floor f = floors.iterator().next();
        return f.getEvaderSpawnLocation();
    }



    public Vector2D getPursuerSpawnLocation(){
        Floor f = floors.iterator().next();
        return f.getPursuerSpawnLocation();
    }

    /**
     * makes a description of the map
     * @return String containing the amount of floor vertexes, amount of obstace vertexes
     * and amount of obstacles separated by commas
     */
    public String getMapDescription() {
        Floor f = this.floors.iterator().next();
        int v = f.getPolygon().npoints;
        int ov = f.getObstaclePoints().size();
        int oi = f.getObstacles().size();
        return String.format("%d,%d,%d", v, ov, oi);

    }
}
