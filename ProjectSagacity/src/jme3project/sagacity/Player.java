package jme3project.sagacity;

import com.jme3.math.Ray;
import com.jme3.scene.Node;

/**
 * @author Kenton Goodling
 */
public class Player {
    
  private float x;
  private float y;
  private float z;
  
  private float playerSpeed;
  
  private boolean allowLeft;
  private boolean allowRight;
  private boolean allowUp;
  private boolean allowDown;
  
  private boolean ignoreCollision; // for debugging purposes
  
  private Ray ray;
  
  private Node node;

  public Player()
  {
    x = 0;
    y = 5;
    z = 0;  
       
    playerSpeed = .1f;
    
    allowLeft = true;
    allowRight = true;
    allowUp = true;
    allowDown = true;
    
    ignoreCollision = false; //for debugging purposes
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
  
}
