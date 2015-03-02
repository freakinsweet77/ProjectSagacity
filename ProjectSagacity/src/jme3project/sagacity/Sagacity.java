package jme3project.sagacity;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import java.util.Random;

public class Sagacity extends SimpleApplication
{
    // Class variables //
    // -------------------------- //

    private Random randomGenerator = new Random();
    private Node camNode;
    private Node[] rooms;
    private Node currentNode = rootNode; // for debugging camera
    private Node player;
    CollisionResults results;
    private DirectionalLight sunlight;
    private AmbientLight ambient;
    private PointLight roomLight;
    private int minRooms = 10;
    private int maxRooms = 20;
    private int roomGridWidth = 14;
    private int roomGridHeight = 10;
    private int wallCollisionIndex = 0;
    private int environmentCollisionIndex = 0;
    private float blockWidth = 5;
    private float blockHeight = 5;
    private BulletAppState bulletAppState = new BulletAppState();
    private RigidBodyControl wallCollision[];
    private RigidBodyControl floorCollision[];
    private RigidBodyControl environmentCollision[];
    private RigidBodyControl playerCollision;
    private Player sage = new Player();
    private Camera camera = new Camera(rootNode);

    // -------------------------- //
    public static void main(String[] args)
    {
        Sagacity app = new Sagacity();

        app.start();
    }

    // Runs at startup of the application
    @Override
    public void simpleInitApp()
    {
        stateManager.attach(bulletAppState);
        initKeys();
        makeFloor();
        makePlayer();
        makeEnvironment();
        setupCamera(rootNode, 0, 750, 35);
        initLight();
    }

    protected void makePlayer()
    {
        results = new CollisionResults();
        sage.setNode(rootNode);
        sage.getNode().setLocalTranslation(rootNode.getLocalTranslation());

        Spatial playerBox = assetManager.loadModel("Models/Sacboy.j3o");
        playerBox.setLocalTranslation(0f, 7f, 0f);
        playerBox.setName("Player");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Magenta);
        playerBox.setMaterial(mat);
        sage.getNode().attachChild(playerBox);


        // Adding the player to the physical space (allowing for collision)
        playerCollision = new RigidBodyControl(0f);
        playerBox.addControl(playerCollision);
        bulletAppState.getPhysicsSpace().add(playerCollision);

        //playerRay = new Ray(player.getChild("Player").getLocalTranslation(), rootNode.getChild("Top Wall 2").getLocalTranslation());
        //playerRay.setLimit(.00001f);
        rootNode.attachChild(sage.getNode());
    }

    // Create a floor with multiple rooms
    protected void makeFloor()
    {
        // Change background color of the display
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

        int numRooms = getNumRooms(minRooms, maxRooms);

        // Determining the number of rooms that will be on the current floor
        rooms = new Node[numRooms];

        // 2400 is an arbitrary large value to avoid indexing issues
        wallCollision = new RigidBodyControl[numRooms * 2400];
        floorCollision = new RigidBodyControl[numRooms];
        environmentCollision = new RigidBodyControl[numRooms * 2400];

        initRooms();
        initNeighborData();
        initEnvironmentData();

        // Initially build the room around the root node
        makeGround(rootNode, 0);
        makeWalls(rootNode);

        // Arbitrary values - user data cannot be negative, so I start with a high number
        int row = 100;
        int col = 100;

        // Setting the row and col of the rootNode
        rootNode.setUserData("row", row);
        rootNode.setUserData("col", col);

        // Looping efficiency variables
        int lastRoll = -1;
        int breakCounter = 0;

        for (int i = 0; i < rooms.length; i++)
        {
            breakCounter++;

            // Selecting the type of the next room randomly
            int nextRoom = getRandom(4);

            // Restart if the same failed number was rolled again
            if (lastRoll == nextRoom)
            {
                i--;
                continue;
            }

            // Break if it may be caught in an endless loop
            if (breakCounter > 20)
            {
                break;
            }

            // Number values represent a direction
            // 0 -> left
            // 1 -> up
            // 2 -> right
            // 3 -> down

            if (nextRoom == 0)
            {
                col -= 1;

                // If the coordinates are not already taken - continue
                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(-70.001f, 0f, 0f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("rightNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("leftNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("leftNeighbor", true);
                    }
                } else
                {
                    lastRoll = 0;
                    col += 1;
                    i--;
                    continue;
                }
            }
            if (nextRoom == 1)
            {
                row -= 1;

                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(0f, 0f, -50.001f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("bottomNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("topNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("topNeighbor", true);
                    }

                } else
                {
                    lastRoll = 1;
                    row += 1;
                    i--;
                    continue;
                }
            }
            if (nextRoom == 2)
            {
                col += 1;

                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(70.001f, 0f, 0f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("leftNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("rightNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("rightNeighbor", true);
                    }
                } else
                {
                    lastRoll = 2;
                    col -= 1;
                    i--;
                    continue;
                }
            }
            if (nextRoom == 3)
            {
                row += 1;

                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(0f, 0f, 50.001f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("topNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("bottomNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("bottomNeighbor", true);
                    }
                } else
                {
                    lastRoll = 3;
                    row -= 1;
                    i--;
                    continue;
                }
            }

            // Must attach the child room to the rootNode during the first iteration
            if (i == 0)
            {

                rootNode.attachChild(rooms[i]);
            } else
            {
                rooms[i - 1].attachChild(rooms[i]);
            }

            makeGround(rooms[i], 0);
            makeWalls(rooms[i]);
            makeDoors();

            // Reset loop efficiency variables
            lastRoll = -1;
            breakCounter = 0;
        }
    }

    protected void initRooms()
    {
        rootNode.setUserData("isRendered", false);
        for (int i = 0; i < rooms.length; i++)
        {
            rooms[i] = new Node();
            rooms[i].setUserData("row", 0);
            rooms[i].setUserData("col", 0);
            rooms[i].setUserData("isRendered", false);
        }
    }

    // Creates the floor of a room
    protected void makeGround(Node room, int roomNum)
    {
        Box box = new Box(35, .2f, 25);
        Geometry floor = new Geometry("Floor", box);
        floor.setLocalTranslation(0f, 0f, 0f);
        TangentBinormalGenerator.generate(box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture floorTexture = assetManager.loadTexture("Textures/FloorBlue.jpg");
        mat.setTexture("ColorMap", floorTexture);
        floor.setMaterial(mat);
        room.attachChild(floor);
        floorCollision[roomNum] = new RigidBodyControl(0.0f);
        floor.addControl(floorCollision[roomNum]);
        bulletAppState.getPhysicsSpace().add(floorCollision[roomNum]);
    }

    // Creates the walls of a room
    protected void makeWalls(Node room)
    {
        // Left and right walls 
        //Box box = new Box(.2f, 10f, blockHeight);
        // Top and bottom walls
        //Box box2 = new Box(35f, 10f, .2f);

        Geometry[] leftWall = initWall("left", .2f, 10f, blockHeight);
        Geometry[] rightWall = initWall("right", .2f, 10f, blockHeight);
        Geometry[] topWall = initWall("top", blockWidth, 10f, .2f);
        Geometry[] bottomWall = initWall("bottom", blockWidth, 10f, .2f);

        // Attach the left and right walls to the node - could be a method?
        for (int i = 0; i < leftWall.length - 1; i++)
        {
            room.attachChild(leftWall[i]);
            room.attachChild(rightWall[i]);
            // Adding the wall to the physical space
            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            leftWall[i].addControl(wallCollision[wallCollisionIndex]);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);

            wallCollisionIndex++;

            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            rightWall[i].addControl(wallCollision[wallCollisionIndex]);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);

            wallCollisionIndex++;
        }

        // Attach the top and bottom walls to the node - could be a method?
        for (int i = 0; i < topWall.length - 1; i++)
        {
            room.attachChild(topWall[i]);
            room.attachChild(bottomWall[i]);
            // Adding the wall to the physical space
            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            topWall[i].addControl(wallCollision[wallCollisionIndex]);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);

            wallCollisionIndex++;

            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            bottomWall[i].addControl(wallCollision[wallCollisionIndex]);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);

            wallCollisionIndex++;
        }
    }

    protected Geometry[] initWall(String side, float wallX, float wallY, float wallZ)
    {
        Box box = new Box(wallX, wallY, wallZ);
        Geometry[] wall;
        int wallLocation;

        // Determine how many segments are necessary in the wall
        if (side.equals("top") || side.equals("bottom"))
        {
            wall = new Geometry[8];
            wallLocation = -30;
        } else
        {
            wall = new Geometry[6];
            wallLocation = -20;
        }

        //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //Texture wallTexture = assetManager.loadTexture("Textures/BrickGrey.jpg");
        //mat.setTexture("ColorMap", wallTexture);

        for (int i = 0; i < wall.length - 1; i++)
        {
            if (side.equals("left"))
            {
                wall[i] = new Geometry("Left Wall " + i, box);
                wall[i].setLocalTranslation(-35f, 5f, wallLocation);
            }
            if (side.equals("top"))
            {
                wall[i] = new Geometry("Top Wall " + i, box);
                wall[i].setLocalTranslation(wallLocation, 5f, -25f);
            }
            if (side.equals("right"))
            {
                wall[i] = new Geometry("Right Wall " + i, box);
                wall[i].setLocalTranslation(35f, 5f, wallLocation);
            }
            if (side.equals("bottom"))
            {
                wall[i] = new Geometry("Bottom Wall " + i, box);
                wall[i].setLocalTranslation(wallLocation, 5f, 25f);
            }

            //wall[i].setMaterial(mat);
            wall[i].setMaterial((Material) assetManager.loadMaterial("Materials/GreyBrick.j3m"));

            wallLocation += blockHeight * 2;
        }

        return wall;
    }

    protected void initNeighborData()
    {
        rootNode.setUserData("leftNeighbor", false);
        rootNode.setUserData("topNeighbor", false);
        rootNode.setUserData("rightNeighbor", false);
        rootNode.setUserData("bottomNeighbor", false);

        for (Node room : rooms)
        {
            room.setUserData("leftNeighbor", false);
            room.setUserData("topNeighbor", false);
            room.setUserData("rightNeighbor", false);
            room.setUserData("bottomNeighbor", false);
        }
    }

    // Initializing the environment user data for each room node
    protected void initEnvironmentData()
    {
        int random = -1;

        for (int row = 1; row <= roomGridWidth; row++)
        {
            for (int col = 1; col <= roomGridHeight; col++)
            {
                // Gives a 20% chance for an object to be placed in each location
                random = getRandom(5);

                if (random == 1) // 1 is an arbitrary 'magic number'
                {
                    rootNode.setUserData("EnvironmentObject " + row + "-" + col, true);
                } else
                {
                    rootNode.setUserData("EnvironmentObject " + row + "-" + col, false);
                }
            }
        }

        for (Node room : rooms)
        {
            for (int row = 2; row <= roomGridWidth; row++)
            {
                for (int col = 2; col <= roomGridHeight; col++)
                {
                    random = getRandom(5);

                    if (random == 1) // 1 is an arbitrary 'magic number'
                    {
                        room.setUserData("EnvironmentObject " + row + "-" + col, true);
                    } else
                    {
                        room.setUserData("EnvironmentObject " + row + "-" + col, false);
                    }
                }
            }
        }
    }

    // Called to set user data to determine where environment pieces should go
    protected void makeEnvironment()
    {
        // Displacement variables
        double xLocation = 0;
        double zLocation = 0;

        for (int row = 2; row <= roomGridWidth; row++)
        {
            for (int col = 2; col <= roomGridHeight; col++)
            {
                if (rootNode.getUserData("EnvironmentObject " + row + "-" + col))
                {
                    xLocation = getRoomItemXLocation(row);
                    zLocation = getRoomItemZLocation(col);
                    makeEnvironmentItem(rootNode, xLocation, zLocation);
                }
            }
        }

        for (Node room : rooms)
        {
            for (int row = 2; row <= roomGridWidth; row++)
            {
                for (int col = 2; col <= roomGridHeight; col++)
                {
                    if (room.getUserData("EnvironmentObject " + row + "-" + col))
                    {
                        xLocation = getRoomItemXLocation(row);
                        zLocation = getRoomItemZLocation(col);
                        makeEnvironmentItem(room, xLocation, zLocation);
                    }
                }
            }
        }

    }

    // NEEDS TO BE FINISHED - IGNORE THIS - will be used for random door placement
    protected void addNeighborData()
    {
        int row;
        int col;
        int nextRow;
        int nextCol;

        for (int i = 0; i < rooms.length; i++)
        {
            row = rooms[i].getUserData("row");
            col = rooms[i].getUserData("col");
        }
    }

    // Returns the xLocation of an object to be placed in a room
    protected double getRoomItemXLocation(int row)
    {
        // Simple displacement algorithm
        double xLocation = (row - 8) * 5;

        return xLocation;
    }

    // Returns the zLocation of an object to be placed in a room
    protected double getRoomItemZLocation(int col)
    {
        // Simple displacement algorithm
        double zLocation = (col - 6) * 5;

        return zLocation;
    }

    protected void makeEnvironmentItem(Node room, double xLocation, double zLocation)
    {
        Sphere sphere = new Sphere(32, 32, 2.0f);
        Geometry environmentItem = new Geometry("EnvironmentObject", sphere);
        environmentItem.setLocalTranslation((float) xLocation, 2f, (float) zLocation);
        TangentBinormalGenerator.generate(sphere);
        //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Brown);
        environmentItem.setMaterial((Material) assetManager.loadMaterial("Materials/Rock.j3m"));

        /* COLLISION FOR ENV OBJECTS - MESSES UP RANDOMIZATION FOR SOME REASON
         environmentCollision[environmentCollisionIndex] = new RigidBodyControl(0.0f);
         environmentItem.addControl(environmentCollision[environmentCollisionIndex]);
         bulletAppState.getPhysicsSpace().add(environmentCollision[environmentCollisionIndex]);
      
         environmentCollisionIndex++;
         */

        room.attachChild(environmentItem);
    }

    // Detaches wall segments as needed
    protected void makeDoors()
    {
        if (rootNode.getUserData("topNeighbor"))
        {
            rootNode.detachChildNamed("Top Wall 3");
        }
        if (rootNode.getUserData("bottomNeighbor"))
        {
            rootNode.detachChildNamed("Bottom Wall 3");
        }
        if (rootNode.getUserData("leftNeighbor"))
        {
            rootNode.detachChildNamed("Left Wall 2");
        }
        if (rootNode.getUserData("rightNeighbor"))
        {
            rootNode.detachChildNamed("Right Wall 2");
        }

        for (int i = 0; i < rooms.length; i++)
        {
            if (rooms[i].getUserData("topNeighbor"))
            {
                rooms[i].detachChildNamed("Top Wall 3");
            }
            if (rooms[i].getUserData("bottomNeighbor"))
            {
                rooms[i].detachChildNamed("Bottom Wall 3");
            }
            if (rooms[i].getUserData("leftNeighbor"))
            {
                rooms[i].detachChildNamed("Left Wall 2");
            }
            if (rooms[i].getUserData("rightNeighbor"))
            {
                rooms[i].detachChildNamed("Right Wall 2");
            }
        }
    }

    // Sets the camera to the desired location
    protected void setupCamera(Node room, float x, float y, float z)
    {
        flyCam.setEnabled(false);
        //cam.setFrustumPerspective(90, settings.getWidth()/settings.getHeight(), 1, 1000);
        camNode = new CameraNode("Camera Node", cam);
        camera.setCameraNode((CameraNode) camNode);
        room.attachChild(camera.getCameraNode());
        camera.getCameraNode().setLocalTranslation(new Vector3f(x, y, z));
        camNode.lookAt(sage.getNode().getLocalTranslation(), Vector3f.UNIT_Y); // Change room to rootNode as needed for testing
    }

    // Initializes ambient light and creates a light to make the rooms brighter
    protected void initLight()
    {
        ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(ambient);

        roomLight = new PointLight();
        roomLight.setPosition(camera.getNode().getLocalTranslation());
        rootNode.addLight(roomLight);
        LightControl lightControl = new LightControl();
        camera.getCameraNode().addControl(lightControl);
    }

    // Makes a directional light in any room as needed
    protected void makeLight(Node room)
    {
        sunlight = new DirectionalLight();
        sunlight.setColor(ColorRGBA.White);
        sunlight.setDirection(room.getLocalTranslation().normalizeLocal());
        room.addLight(sunlight);
    }

    // Returns a random number below the max value
    protected int getRandom(int max)
    {
        return randomGenerator.nextInt(max);
    }

    // Returns a random number between two values
    protected int getNumRooms(int min, int max)
    {
        return randomGenerator.nextInt(max - min) + min;
    }

    // Checks if the coordinates for the next room are already taken
    protected boolean checkRoomCoordinates(int nextRow, int nextCol)
    {
        int row;
        int col;
        int rootRow = rootNode.getUserData("row");
        int rootCol = rootNode.getUserData("col");

        for (Node room : rooms)
        {
            row = room.getUserData("row");
            col = room.getUserData("col");
            if (row == nextRow && col == nextCol)
            {
                return true;
            } else if (row == rootRow && col == rootCol)
            {
                return true;
            }
        }
        return false;
    }

    /* protected void checkPlayerCollision()
     {
     playerRay.setOrigin(player.getChild("Player").getLocalTranslation());
     checkRoomCollision(playerRay);
     }*/
    // Renders only the rooms closest to the player - needs work
    protected void renderNearestRooms()
    {
        double renderDistance = 200;
        boolean isRendered = rootNode.getUserData("isRendered");


        if ((player.getChild("Player").getLocalTranslation().distance(rootNode.getLocalTranslation()) < renderDistance) && !isRendered)
        {
            makeGround(rootNode, 0);
            makeWalls(rootNode);
            makeDoors();
            //makeEnvironment(rootNode);
            rootNode.setUserData("isRendered", true);
        }

        for (int i = 0; i < rooms.length - 1; i++)
        {
            double roomDistance = player.getChild("Player").getLocalTranslation().distance(rooms[i].getLocalTranslation());
            isRendered = rooms[i].getUserData("isRendered");
            if (roomDistance < renderDistance && !isRendered)
            {
                makeGround(rooms[i], 0);
                makeWalls(rooms[i]);
                makeDoors();
                //makeEnvironment(rooms[i]);

                rooms[i].setUserData("isRendered", true);
            } else if (roomDistance > renderDistance)
            {
                for (int j = 0; j < 6; j++)
                {
                    rooms[i].detachChildNamed("Left Wall " + j);
                    rooms[i].detachChildNamed("Right Wall " + j);
                }
                for (int j = 0; j < 8; j++)
                {
                    rooms[i].detachChildNamed("Top Wall " + j);
                    rooms[i].detachChildNamed("Bottom Wall " + j);
                }
                rooms[i].detachChildNamed("Floor");
                rooms[i].detachChildNamed("EnvironmentObject");
                rooms[i].setUserData("isRendered", false);
            }
        }
    }

    /* protected void checkRoomCollision(Ray ray)
     {
     // Add in collision for rootNode and final room node rooms[length - 1]
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 7; j++)
     {
     try
     {
     rootNode.getChild("Top Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Top Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Top Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
     if (results.size() > 0) 
     {
     sage.setAllowUp(false);
     }
     else
     {
     sage.setAllowUp(false);
     }
      
     results.clear();
      
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 5; j++)
     {
     try
     {
     rootNode.getChild("Left Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Left Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Left Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
     if (results.size() > 0) 
     {
     sage.setAllowLeft(false);
     }
     else
     {
     sage.setAllowLeft(true);
     }
      
     results.clear();
      
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 5; j++)
     {
     try
     {
     rootNode.getChild("Right Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Right Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Right Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
      
     if (results.size() > 0) 
     {
     sage.setAllowRight(false);
     }
     else
     {
     sage.setAllowRight(false);
     }
      
     results.clear();
      
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 7; j++)
     {
     try
     {
     rootNode.getChild("Bottom Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Bottom Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Bottom Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
      
      
     if (results.size() > 0) 
     {
     sage.setAllowDown(false);
     }
     else
     {
     sage.setAllowDown(true);
     }
      
     results.clear();
     }*/
    // Initializes key bindings - we will use booleans to create game states
    private void initKeys()
    {
        inputManager.addMapping("Zoom", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("CameraLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("CameraUp", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("CameraRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("CameraDown", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("CameraReset", new KeyTrigger(KeyInput.KEY_X));

        inputManager.addMapping("PlayerLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("PlayerUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("PlayerRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("PlayerDown", new KeyTrigger(KeyInput.KEY_S));

        inputManager.addMapping("IgnoreCollision", new KeyTrigger(KeyInput.KEY_RCONTROL));

        inputManager.addListener(actionListener, "Zoom", "CameraReset", "IgnoreCollision");
        inputManager.addListener(analogListener, "CameraLeft", "CameraUp", "CameraRight", "CameraDown", "PlayerLeft", "PlayerUp", "PlayerRight", "PlayerDown");
    }
    // Action listener is for actions that should only happen once in a given moment
    private ActionListener actionListener = new ActionListener()
    {
        public void onAction(String name, boolean keyPressed, float tpf)
        {
            if (name.equals("Zoom") && !keyPressed)
            {
                if (camera.getY() == 65)
                {
                    camera.setY(750);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                } else
                {
                    camera.setY(65);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                }
            }
            if (name.equals("CameraReset") && !keyPressed)
            {
                camera.setX(0);
                camera.setZ(35);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            /* if (name.equals("IgnoreCollision")) 
             {
             if(!ignoreCollision)
             {
             allowLeftMovement = true;
             allowUpMovement = true;
             allowDownMovement = true;
             allowRightMovement = true;
             ignoreCollision = true;
             }
             else
             {
             ignoreCollision = false;
             }
             } */
        }
    };
    // Analog listener is for consistent actions that should be able to repeat constantly
    private AnalogListener analogListener = new AnalogListener()
    {
        public void onAnalog(String name, float value, float tpf)
        {
            if (name.equals("CameraLeft"))
            {
                camera.setX(-5f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            if (name.equals("CameraUp"))
            {
                camera.setZ(-5f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            if (name.equals("CameraRight"))
            {
                camera.setX(5f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            if (name.equals("CameraDown"))
            {
                camera.setZ(5f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            if (name.equals("PlayerLeft") && sage.getAllowLeft())
            {
                sage.setX(-.1f);
                sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                // Adding the player to the physical space (allowing for collision)
                playerCollision = new RigidBodyControl(0f);
                sage.getNode().getChild("Player").addControl(playerCollision);
                bulletAppState.getPhysicsSpace().add(playerCollision);
                camera.setX(-.1f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            if (name.equals("PlayerUp") && sage.getAllowUp())
            {
                sage.setZ(-.1f);
                sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                playerCollision = new RigidBodyControl(0f);
                sage.getNode().getChild("Player").addControl(playerCollision);
                bulletAppState.getPhysicsSpace().add(playerCollision);
                camera.setZ(-.1f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
            if (name.equals("PlayerRight") && sage.getAllowRight())
            {
                sage.setX(.1f);
                sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                playerCollision = new RigidBodyControl(0f);
                sage.getNode().getChild("Player").addControl(playerCollision);
                bulletAppState.getPhysicsSpace().add(playerCollision);
                camera.setX(.1f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());

            }
            if (name.equals("PlayerDown") && sage.getAllowDown())
            {
                sage.setZ(.1f);
                sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                playerCollision = new RigidBodyControl(0f);
                sage.getNode().getChild("Player").addControl(playerCollision);
                bulletAppState.getPhysicsSpace().add(playerCollision);
                camera.setZ(.1f);
                camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
            }
        }
    };
    /*@Override
     public void simpleUpdate(float tpf) 
     {
     if(!ignoreCollision)
     {
     checkPlayerCollision();
     }
     //renderNearestRooms();
     }*/
}
