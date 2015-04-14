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
  private int playerBliss;
  private int playerStorytelling;
  
  private String playerFacing;
  
  private int potionCount;
  private int powerUpCount;
  private int defenseUpCount;
  
  private boolean allowLeft;
  private boolean allowRight;
  private boolean allowUp;
  private boolean allowDown;
  
  private boolean moveLeft;
  private boolean moveRight;
  private boolean moveUp;
  private boolean moveDown;
  
  private boolean foundWisdom;
  
  private boolean hitEnemy;
  
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
    
    playerBliss = 0;
    
    playerStorytelling = 0;
    
    playerFacing = "up";
    
    allowLeft = true;
    allowRight = true;
    allowUp = true;
    allowDown = true;
    
    moveLeft = false;
    moveRight = false;
    moveUp = false;
    moveDown = false;
    
    foundWisdom = false;
    
    hitEnemy = false;
    
    ignoreCollision = false; //for debugging purposes
  }
  
  public Player(int attack, int defense, int bliss, int storytelling)
  {
    x = 0;
    y = 2;
    z = 0;  
       
    playerSpeed = .1f;
    
    playerHealth = 100;
    
    potionCount = 1;
    
    playerAttack = attack;
    
    playerDefense = defense;
    
    playerBliss = bliss;
    
    playerStorytelling = storytelling;
    
    powerUpCount = 0;
    defenseUpCount = 0;
    
    playerFacing = "up";
    
    allowLeft = true;
    allowRight = true;
    allowUp = true;
    allowDown = true;
    
    moveLeft = false;
    moveRight = false;
    moveUp = false;
    moveDown = false;
    
    foundWisdom = false;
    
    hitEnemy = false;
    
    ignoreCollision = false; //for debugging purposes
  }
  
  // Needs work
  public void collision(PhysicsCollisionEvent event) 
  {
      //System.out.println(event.getNodeA().getName());
      if(event.getNodeA() == null || event.getNodeB() == null)
      {
          return;
      }
      try
      {
      // This is where collision events will go e.g. being damaged when touching an enemy
      if(event.getNodeA().getName().equals("PlayerNode") && event.getNodeB().getName().equals("Enemy"))
      {
          //event.getNodeB().getParent().detachChildNamed("Enemy"); -- this can be used to do powerups
          if(playerDefense < 5)
          {
              playerHealth -= 5 - playerDefense;
          }
          else
          {
              playerHealth--;
          }
          
      }
      if(event.getNodeB().getName().equals("PlayerNode") && event.getNodeA().getName().equals("Enemy"))
      {
          if(playerDefense < 5)
          {
              playerHealth -= 5 - playerDefense;
          }
          else
          {
              playerHealth--;
          }
      }
      if(event.getNodeA().getName().equals("PlayerNode") && event.getNodeB().getName().equals("PowerUp"))
      {
          event.getNodeB().getParent().detachChildNamed("PowerUp"); 
          powerUpCount++;
      }
      if(event.getNodeB().getName().equals("PlayerNode") && event.getNodeA().getName().equals("PowerUp"))
      {
          event.getNodeA().getParent().detachChildNamed("PowerUp");
          powerUpCount++;
      }
      if(event.getNodeA().getName().equals("PlayerNode") && event.getNodeB().getName().equals("DefenseUp"))
      {
          event.getNodeB().getParent().detachChildNamed("DefenseUp"); 
          defenseUpCount++;
      }
      if(event.getNodeB().getName().equals("PlayerNode") && event.getNodeA().getName().equals("DefenseUp"))
      {
          event.getNodeA().getParent().detachChildNamed("DefenseUp");
          defenseUpCount++;
      }
      if(event.getNodeA().getName().equals("PlayerNode") && event.getNodeB().getName().equals("Wisdom"))
      {
          event.getNodeB().getParent().detachChildNamed("Wisdom"); 
          playerStorytelling++;
          foundWisdom = true;
      }
      if(event.getNodeB().getName().equals("PlayerNode") && event.getNodeA().getName().equals("Wisdom"))
      {
          event.getNodeA().getParent().detachChildNamed("Wisdom");
          playerStorytelling++;
          foundWisdom = true;
      }
      if(event.getNodeA().getName().equals("Attack") && event.getNodeB().getName().equals("Enemy"))
      {
          hitEnemy = true;
      }
      if(event.getNodeB().getName().equals("Attack") && event.getNodeA().getName().equals("Enemy"))
      {
          hitEnemy = true;
      }
      } 
      catch(Exception e)
      {
          
      }
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
  
  public void setBliss(int bliss)
  {
      playerBliss += bliss;
  }
  
  public int getBliss()
  {
      return playerBliss;
  }
  
  public void setStorytelling(int storytelling)
  {
      playerStorytelling += storytelling;
  }
  
  public int getStorytelling()
  {
      return playerStorytelling;
  }
  
  public void setFoundWisdom(boolean wisdom)
  {
      foundWisdom = wisdom;
  }
  
  public boolean getFoundWisdom()
  {
      return foundWisdom;
  }
     
  public void setNumPowerUp(int powerUp)
  {
      powerUpCount += powerUp;
  }
  
  public int getNumPowerUp()
  {
      return powerUpCount;
  }
  
  public void setNumDefenseUp(int defenseUp)
  {
      defenseUpCount += defenseUp;
  }
  
  public int getNumDefenseUp()
  {
      return defenseUpCount;
  }
  
  public void setHitEnemy(boolean hit)
  {
      hitEnemy = hit;
  }
  
  public boolean getHitEnemy()
  {
      return hitEnemy;
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
