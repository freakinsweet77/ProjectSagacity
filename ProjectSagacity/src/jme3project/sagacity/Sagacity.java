package jme3project.sagacity;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
import com.jme3.math.Vector3f;
import com.jme3.util.TangentBinormalGenerator;
import java.util.Random;

public class Sagacity extends SimpleApplication
{
  // Class variables
  // -------------------------- //
  private Random randomGenerator = new Random();
  
  private Node camNode;
  private Node[] rooms;
  private Node currentNode = rootNode; // for debugging camera
  private Node player;
  
  CollisionResults results;
  
  private DirectionalLight sunlight;
  
  private int minRooms = 10;
  private int maxRooms = 20;
  private int camX = 0;
  private int camY = 750;
  private int camZ = 35;
  private int playerX = 0;
  private int playerY = 7;
  private int playerZ = 0;
  
  private float blockWidth = 5;
  private float blockHeight = 5;
  
  private Ray playerRay;
  
  private boolean allowLeftMovement = true;
  private boolean allowUpMovement = true;
  private boolean allowRightMovement = true;
  private boolean allowDownMovement = true;
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
      // y value should be reset to 50
      setCamera(rootNode, camX, camY, camZ);
      initKeys();
      makeFloor();
      makePlayer();
      
  }
  
  protected void makePlayer()
  {
      results = new CollisionResults();
      
      player = new Node();
      player.setLocalTranslation(rootNode.getLocalTranslation());
      
      Box box = new Box(2.0f, 2.0f, 2.0f);
      Geometry playerBox = new Geometry("Player", box);
      playerBox.setLocalTranslation(0f, 7f, 0f);
      TangentBinormalGenerator.generate(box);
      Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setColor("Color", ColorRGBA.White);
      playerBox.setMaterial(mat1);
      player.attachChild(playerBox);
      
      playerRay = new Ray(player.getChild("Player").getLocalTranslation(), rootNode.getChild("Top Wall 2").getLocalTranslation());
      playerRay.setLimit(0.001f);
      rootNode.attachChild(player);
  }
  
  // Create a floor with multiple rooms
  protected void makeFloor()
  {
      // Change background color of the display
      viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
      // Determining the number of rooms that will be on the current floor
      rooms = new Node[getNumRooms(minRooms, maxRooms)];
      initRooms();
      initNeighborData();
      
      // Initially build the room around the root node
      makeGround(rootNode);
      makeWalls(rootNode);
      makeLight(rootNode);
      
      // Arbitrary values - user data cannot be negative, so I start with a high number
      int row = 100;
      int col = 100;
       
      // Setting the row and col of the rootNode
      rootNode.setUserData("row", row);
      rootNode.setUserData("col", col);
      
      // Looping efficiency variables
      int lastRoll = -1;
      int breakCounter = 0;
     
      for(int i = 0; i < rooms.length; i++)
      {
          breakCounter++; 
          
          // Selecting the type of the next room randomly
          int nextRoom = getRandom(4);
          
          // Restart if the same failed number was rolled again
          if(lastRoll == nextRoom)
          {
              i--;
              continue;
          }
          
          // Break if it may be caught in an endless loop
          if(breakCounter > 20)
          {
              break;
          }
          
          // Number values represent a direction
          // 0 -> left
          // 1 -> up
          // 2 -> right
          // 3 -> down
         
          if(nextRoom == 0)
          {
              col -= 1;
              
              // If the coordinates are not already taken - continue
              if(!checkRoomCoordinates(row, col))
              {
                  rooms[i].setLocalTranslation(-70.001f, 0f, 0f);
                  rooms[i].setUserData("row", row);
                  rooms[i].setUserData("col", col);
                  rooms[i].setUserData("rightNeighbor", true);
                  if(i > 0)
                  {
                      rooms[i-1].setUserData("leftNeighbor", true);
                  }
                  else
                  {
                      rootNode.setUserData("leftNeighbor", true);
                  }
              }
              else
              {
                  lastRoll = 0;
                  col += 1;
                  i--;
                  continue;
              }
          }
          if(nextRoom == 1)
          {
              row -= 1;
              
              if(!checkRoomCoordinates(row, col))
              {
                  rooms[i].setLocalTranslation(0f, 0f, -50.001f);
                  rooms[i].setUserData("row", row);
                  rooms[i].setUserData("col", col);
                  rooms[i].setUserData("bottomNeighbor", true);
                  if(i > 0)
                  {
                      rooms[i-1].setUserData("topNeighbor", true);
                  }
                  else
                  {
                      rootNode.setUserData("topNeighbor", true);
                  }
                  
              }
              else
              {
                  lastRoll = 1;
                  row += 1;
                  i--;
                  continue;
              }
          }
          if(nextRoom == 2)
          {
              col += 1; 
              
              if(!checkRoomCoordinates(row, col))
              {
                  rooms[i].setLocalTranslation(70.001f, 0f, 0f);
                  rooms[i].setUserData("row", row);
                  rooms[i].setUserData("col", col);
                  rooms[i].setUserData("leftNeighbor", true);
                  if(i > 0)
                  {
                      rooms[i-1].setUserData("rightNeighbor", true);
                  }
                  else
                  {
                      rootNode.setUserData("rightNeighbor", true);
                  }
              }
              else
              {
                  lastRoll = 2;
                  col -= 1;
                  i--;
                  continue;
              }
          }
          if(nextRoom == 3)
          {
              row += 1;
              
              if(!checkRoomCoordinates(row, col))
              {
                  rooms[i].setLocalTranslation(0f, 0f, 50.001f);
                  rooms[i].setUserData("row", row);
                  rooms[i].setUserData("col", col);
                  rooms[i].setUserData("topNeighbor", true);
                  if(i > 0)
                  {
                      rooms[i-1].setUserData("bottomNeighbor", true);
                  }
                  else
                  {
                      rootNode.setUserData("bottomNeighbor", true);
                  }
              }
              else
              {
                  lastRoll = 3;
                  row -= 1;
                  i--;
                  continue;
              }
          }
          
          // Must attach the child room to the rootNode during the first iteration
          if(i == 0)
          {
              rootNode.attachChild(rooms[i]);
          }
          else
          {
              rooms[i-1].attachChild(rooms[i]);
          }
          
          makeGround(rooms[i]);
          makeWalls(rooms[i]);
          makeDoors();
          makeLight(rooms[i]);
          
          // Reset loop efficiency variables
          lastRoll = -1;
          breakCounter = 0;
      }
  }
  
  protected void initRooms()
  {
      for(int i = 0; i < rooms.length; i++)
      {
          rooms[i] = new Node();
          rooms[i].setUserData("row", 0);
          rooms[i].setUserData("col", 0);
      }
  }
  
  // Creates the floor of a room
  protected void makeGround(Node room)
  {
      Box box = new Box(35f, .2f, 25f);
      Geometry floor = new Geometry("Floor", box);
      floor.setLocalTranslation(0f, 0f, 0f);
      TangentBinormalGenerator.generate(box);
      Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setColor("Color", ColorRGBA.Gray);
      floor.setMaterial(mat1);
      room.attachChild(floor);
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
      for(int i = 0; i < leftWall.length - 1; i++)
      {
          room.attachChild(leftWall[i]);
          room.attachChild(rightWall[i]);
      }
      
      // Attach the top and bottom walls to the node - could be a method?
      for(int i = 0; i < topWall.length - 1; i++)
      {
          room.attachChild(topWall[i]);
          room.attachChild(bottomWall[i]);
      }
  }   
  
  protected Geometry[] initWall(String side, float wallX, float wallY, float wallZ)
  {
      Box box = new Box(wallX, wallY, wallZ);
      Geometry[] wall;
      int wallLocation;
      
      // Determine how many segments are necessary in the wall
      if(side.equals("top") || side.equals("bottom"))
      {
           wall = new Geometry[8];
           wallLocation = -30;
      }
      else
      {
          wall = new Geometry[6];
          wallLocation = -20;
      }
      
      TangentBinormalGenerator.generate(box);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.randomColor());
      
      for(int i = 0; i < wall.length - 1; i++)
      {
          if(side.equals("left"))
          {
              wall[i] = new Geometry("Left Wall " + i, box);
              wall[i].setLocalTranslation(-35f, 5f, wallLocation);
          }
          if(side.equals("top"))
          {
              wall[i] = new Geometry("Top Wall " + i, box);
              wall[i].setLocalTranslation(wallLocation, 5f, -25f);
          }
          if(side.equals("right"))
          {
              wall[i] = new Geometry("Right Wall " + i, box);
              wall[i].setLocalTranslation(35f, 5f, wallLocation);
          }
          if(side.equals("bottom"))
          {
              wall[i] = new Geometry("Bottom Wall " + i, box);
              wall[i].setLocalTranslation(wallLocation, 5f, 25f);
          }
          
          wall[i].setMaterial(mat);
          //wall.setMaterial((Material) assetManager.loadMaterial("Materials/ShinyRock.j3m"));
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
      
      for(Node room : rooms)
      {
          room.setUserData("leftNeighbor", false);
          room.setUserData("topNeighbor", false);
          room.setUserData("rightNeighbor", false);
          room.setUserData("bottomNeighbor", false);
      }
  }
  
  // NEEDS TO BE FINISHED - IGNORE THIS - will be used for random door placement
  protected void addNeighborData()
  {
      int row;
      int col;
      int nextRow;
      int nextCol;
      
      for(int i = 0; i < rooms.length; i++)
      {
          row = rooms[i].getUserData("row");
          col = rooms[i].getUserData("col");
      }
  }
  
  // Detaches wall segments as needed
  protected void makeDoors()
  {
      if(rootNode.getUserData("topNeighbor"))
      {
          rootNode.detachChildNamed("Top Wall 3");
      }
      if(rootNode.getUserData("bottomNeighbor"))
      {
          rootNode.detachChildNamed("Bottom Wall 3");
      }
      if(rootNode.getUserData("leftNeighbor"))
      {
          rootNode.detachChildNamed("Left Wall 2");
      }
      if(rootNode.getUserData("rightNeighbor"))
      {
          rootNode.detachChildNamed("Right Wall 2");
      }
      
      for(int i = 0; i < rooms.length; i++)
      {
          if(rooms[i].getUserData("topNeighbor"))
          {
              rooms[i].detachChildNamed("Top Wall 3");
          }
          if(rooms[i].getUserData("bottomNeighbor"))
          {
              rooms[i].detachChildNamed("Bottom Wall 3");
          }
          if(rooms[i].getUserData("leftNeighbor"))
          {
              rooms[i].detachChildNamed("Left Wall 2");
          }
          if(rooms[i].getUserData("rightNeighbor"))
          {
              rooms[i].detachChildNamed("Right Wall 2");
          }
      }
  }
  
  // Sets the camera to the desired location
  protected void setCamera(Node room, int x, int y, int z)
  {
      camNode = new CameraNode("Camera Node", cam); 
      flyCam.setEnabled(false);
      room.attachChild(camNode);
      camNode.setLocalTranslation(new Vector3f(x, y, z));
      camNode.lookAt(room.getLocalTranslation(), Vector3f.UNIT_Y); // Change room to rootNode as needed for testing
  }
  
  // TODO: makeLight method should use ambientLighting
  protected void makeLight(Node room)
  {
      sunlight = new DirectionalLight();
      sunlight.setColor(ColorRGBA.White);
      sunlight.setDirection(new Vector3f(1.0f,1.0f,1.0f).normalizeLocal());
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
      
      for(Node room : rooms)
      {
          row = room.getUserData("row");
          col = room.getUserData("col");
          if(row == nextRow && col == nextCol)
          {
              return true;
          }
          else if(row == rootRow && col == rootCol)
          {
              return true;
          }
      }
      return false;
  }
  
  protected void checkPlayerCollision()
  {
      playerRay.setOrigin(player.getChild("Player").getLocalTranslation());
      checkRoomCollision(playerRay);
  }
  
  protected void checkRoomCollision(Ray ray)
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
          allowUpMovement = false;
      }
      else
      {
          allowUpMovement = true;
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
          allowLeftMovement = false;
      }
      else
      {
          allowLeftMovement = true;
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
          allowRightMovement = false;
      }
      else
      {
          allowRightMovement = true;
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
          allowDownMovement = false;
      }
      else
      {
          allowDownMovement = true;
      }
      
      results.clear();
  }
  
  // Initializes key bindings - we will use booleans to create game states
  private void initKeys() 
  {
    inputManager.addMapping("Zoom",  new KeyTrigger(KeyInput.KEY_Z));
    inputManager.addMapping("CameraLeft",  new KeyTrigger(KeyInput.KEY_LEFT));
    inputManager.addMapping("CameraUp",  new KeyTrigger(KeyInput.KEY_UP));
    inputManager.addMapping("CameraRight",  new KeyTrigger(KeyInput.KEY_RIGHT));
    inputManager.addMapping("CameraDown",  new KeyTrigger(KeyInput.KEY_DOWN));
    inputManager.addMapping("CameraReset",  new KeyTrigger(KeyInput.KEY_X));
    
    inputManager.addMapping("PlayerLeft",  new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("PlayerUp",  new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("PlayerRight",  new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("PlayerDown",  new KeyTrigger(KeyInput.KEY_S));
    
    inputManager.addListener(actionListener,"Zoom", "CameraReset");
    inputManager.addListener(analogListener,"CameraLeft", "CameraUp", "CameraRight", "CameraDown", "PlayerLeft", "PlayerUp", "PlayerRight", "PlayerDown");
  }

  // Action listener is for actions that should only happen once in a given moment
  private ActionListener actionListener = new ActionListener() 
  {
      public void onAction(String name, boolean keyPressed, float tpf) 
      {
        if (name.equals("Zoom") && !keyPressed) 
        {
            if(camY == 50)
            {
                camX = 0;
                camY = 750;
                camZ = 35;
                setCamera(currentNode, camX, camY, camZ);
            }
            else
            {
                camX = 0;
                camY = 50;
                camZ = 35;
                setCamera(currentNode, camX, camY, camZ);
            }
        }
        if (name.equals("CameraReset") && !keyPressed) 
        {
            camX = 0;
            camZ = 35;
            setCamera(currentNode, camX, camY, camZ);
        }
    }
  };
  
  // Analog listener is for consistent actions that should be able to repeat constantly
  private AnalogListener analogListener = new AnalogListener() 
  {
    public void onAnalog(String name, float value, float tpf) 
    {
        if (name.equals("CameraLeft")) 
        {
             camX -= 5;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("CameraUp")) 
        {
             camZ -= 5;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("CameraRight")) 
        {
             camX += 5;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("CameraDown")) 
        {
             camZ += 5;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("PlayerLeft") && allowLeftMovement) 
        {
             playerX -= 1;
             player.getChild("Player").setLocalTranslation(playerX, playerY, playerZ);
             camX -= 1;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("PlayerUp") && allowUpMovement) 
        {
             playerZ -= 1;
             player.getChild("Player").setLocalTranslation(playerX, playerY, playerZ);
             camZ -= 1;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("PlayerRight") && allowRightMovement) 
        {
             playerX += 1;
             player.getChild("Player").setLocalTranslation(playerX, playerY, playerZ);
             camX += 1;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
        if (name.equals("PlayerDown") && allowDownMovement) 
        {
             playerZ += 1;
             player.getChild("Player").setLocalTranslation(playerX, playerY, playerZ);
             camZ += 1;
             camNode.setLocalTranslation(camX, camY, camZ);
        }
    }
  };
  
  @Override
  public void simpleUpdate(float tpf) 
  {
      checkPlayerCollision();
  }
}