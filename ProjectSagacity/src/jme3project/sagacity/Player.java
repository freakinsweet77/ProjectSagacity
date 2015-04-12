package jme3project.sagacity;

import com.jme3.math.Ray;
import com.jme3.scene.Node;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;

/**
 * @author Kenton Goodling & Thomas Hippenstiel
 */
public class Player implements PhysicsCollisionListener {
    
  private float x;
  private float y;
  private float z;
  
  private float playerSpeed;
  
  private float playerHealth;
  
  private int playerAttack;
  private int playerDefense;
  
  private String playerFacing;
  
  private int potionCount;
  
  private boolean allowLeft;
  private boolean allowRight;
  private boolean allowUp;
  private boolean allowDown;
  
  private boolean moveLeft;
  private boolean moveRight;
  private boolean moveUp;
  private boolean moveDown;
  
  private boolean ignoreCollision; // for debugging purposes
  
  private Ray ray;
  
  private Node node;

  public Player()
  {
    x = 0;
    y = 2;
    z = 0;  
       
    playerSpeed = .1f;
    
    playerHealth = 100;
    
    potionCount = 1;
    
    playerAttack = 1;
    
    playerDefense = 1;
    
    playerFacing = "up";
    
    allowLeft = true;
    allowRight = true;
    allowUp = true;
    allowDown = true;
    
    moveLeft = false;
    moveRight = false;
    moveUp = false;
    moveDown = false;
    
    ignoreCollision = false; //for debugging purposes
  }
  // Needs work
  public void collision(PhysicsCollisionEvent event) 
  {
      // This is where collision events will go e.g. being damaged when touching an enemy
  }
  
  public Ray getRay()
  {
      return ray;
  }
  
  public void setRay(Ray newRay)
  {
      ray = newRay;
  }
  
  public float getX()
  {
      return x;
  }
  
  public void setX(float newX)
  {
      x += newX;
  }
  
  public float getY()
  {
      return y;
  }
  
  public void setY(float newY)
  {
      y += newY;
  }
  
  public float getZ()
  {
      return z;
  }
  
  public void setSpeed(float speed)
  {
      playerSpeed += speed;
  }
  
  public float getSpeed()
  {
      return playerSpeed;
  }
  
  public void setHealth(float health)
  {
      playerHealth += health;
  }
  
  public float getHealth()
  {
      return playerHealth;
  }
  
  public void setPotions(int potion)
  {
      potionCount += potion;
  }
  
  public int getPotions()
  {
      return potionCount;
  }
  
  public void setAttack(int attack)
  {
      playerAttack += attack;
  }
  
  public int getAttack()
  {
      return playerAttack;
  }
  
  public void setDefense(int defense)
  {
      playerDefense += defense;
  }
  
  public int getDefense()
  {
      return playerDefense;
  }
  
  public void setFacing(String facing)
  {
      playerFacing = facing;
  }
  
  public String getFacing()
  {
      return playerFacing;
  }
  
  public void setZ(float newZ)
  {
      z += newZ;
  }
  
  public Node getNode()
  {
      return node;
  }
  
  public void setNode(Node newNode)
  {
      node = newNode;
  }
  
  public boolean getAllowRight()
  {
      return allowRight;
  }
  
  public void setAllowRight(Boolean allowed)
  {
      allowRight = allowed;
  }
  
    public boolean getAllowDown()
  {
      return allowDown;
  }
  
  public void setAllowDown(Boolean allowed)
  {
      allowDown = allowed;
  } 
  
    public boolean getAllowLeft()
  {
      return allowLeft;
  }
  
  public void setAllowLeft(Boolean allowed)
  {
      allowLeft = allowed;
  }
  
    public boolean getAllowUp()
  {
      return allowUp;
  }
  
  public void setAllowUp(Boolean allowed)
  {
      allowUp = allowed;
  }
  
  
  public boolean getMoveRight()
  {
      return moveRight;
  }
  
  public void setMoveRight(Boolean move)
  {
      moveRight = move;
  }
  
    public boolean getMoveDown()
  {
      return moveDown;
  }
  
  public void setMoveDown(Boolean move)
  {
      moveDown = move;
  } 
  
    public boolean getMoveLeft()
  {
      return moveLeft;
  }
  
  public void setMoveLeft(Boolean move)
  {
      moveLeft = move;
  }
  
    public boolean getMoveUp()
  {
      return moveUp;
  }
  
  public void setMoveUp(Boolean move)
  {
      moveUp = move;
  }
}
