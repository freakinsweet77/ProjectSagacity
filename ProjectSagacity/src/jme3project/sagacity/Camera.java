/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3project.sagacity;

import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
/**
 * @author Kenton
 */

public class Camera 
{
    private Node node;
    private CameraNode camNode;
    
    private float x;
    private float y;
    private float z;
    

    public Camera(Node newNode)
    {
        x = 0;
        y = 0;
        z = 0;
        node = newNode;
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
      y = newY;
  }
  
  public float getZ()
  {
      return z;
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
  
    public CameraNode getCameraNode()
  {
      return camNode;
  }
  
  public void setCameraNode(CameraNode newNode)
  {
      camNode = newNode;
  }
  
  public void setLocation(float newX, float newY, float newZ)
  {
      x = newX;
      y = newY;
      z = newZ;
      camNode.setLocalTranslation(newX, newY, newZ);
  }
}
