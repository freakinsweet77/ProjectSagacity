/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3project.sagacity;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * @author Kenton
 */
public class Room
{

    private String type;
    private Node node;
    private int row = 0;
    private int col = 0;
    private char roomgrid[][] = new char[10][14];
    private int floorrow;
    private int floorcol;
    private boolean LExit;
    private boolean RExit;
    private boolean UExit;
    private boolean DExit;
    private boolean isRendered;
    private boolean LRoom;
    private char floor[][];
    
    private Vector3f worldlocation = new Vector3f();

    public Room(int newrow, int newcol, int newexits, char newtype, int roomnum, float x, float y)
    {
        floorrow = newrow;
        floorcol = newcol;
        switch (newtype)
        {
            case 'S':
                type = "Start";
                break;
            case 'R':
                type = "Room";
                break;
            case 'B':
                type = "Boss";
                break;
        }
        worldlocation.set(0,0,y);
        setExits(newexits);
        node = new Node("room" +roomnum);
        isRendered = false;
        worldlocation.set(x,0,y);
        for (int i = 0; i < roomgrid.length; i++)
        {
            for (int j = 0; j < roomgrid[i].length; j++)
            {
                roomgrid[i][j] = 'V';
            }
        }
        node.setLocalTranslation(worldlocation);
    }

    public void setLRoom(boolean flag)
    {
        LRoom = flag; 
    }
    
    public boolean getLRoom()
    {
        return LRoom;
    }
    
    void placeItem(int row, int col)
    {
        roomgrid[row][col] = 'O';
    }

    public int getrow()
    {
        return floorrow;
    }

    public int getcol()
    {
        return floorcol;
    }

    public Node getNode()
    {
        return node;
    }
    

    public boolean isFilled(int newrow, int newcol)
    {
        if (roomgrid[row][col] != 'V')
        {
            return true;
        } else
        {
            return false;
        }
    }

    public boolean isRendered()
    {
        return isRendered;
    }

    public void setRendered(boolean flag)
    {
        isRendered = flag;
    }

    public boolean getLExit()
    {
        return LExit;
    }
    
    public boolean getRExit()
    {
        return RExit;
    }
    
    public boolean getUExit()
    {
        return UExit;
    }
    
    public boolean getDExit()
    {
        return DExit;
    }
    
    protected void setExits(int exits)
    {
        /*
         * exits come in form of 4 digit int
         * where from left to right the values are of exits
         * LRUD
         */
        switch (exits)
        {
            case 0:
                LExit = false;
                RExit = false;
                UExit = false;
                DExit = false;
                break;
            case 1:
                LExit = false;
                RExit = false;
                UExit = false;
                DExit = true;
                break;
            case 10:
                LExit = false;
                RExit = false;
                UExit = true;
                DExit = false;
                break;
            case 11:
                LExit = false;
                RExit = false;
                UExit = true;
                DExit = true;
                break;
            case 100:
                LExit = false;
                RExit = false;
                UExit = true;
                DExit = false;
                break;
            case 101:
                LExit = false;
                RExit = true;
                UExit = false;
                DExit = true;
                break;
            case 110:
                LExit = false;
                RExit = true;
                UExit = true;
                DExit = false;
                break;
            case 111:
                LExit = false;
                RExit = true;
                UExit = true;
                DExit = true;
                break;
            case 1000:
                LExit = true;
                RExit = false;
                UExit = false;
                DExit = false;
                break;
            case 1001:
                LExit = true;
                RExit = false;
                UExit = false;
                DExit = true;
                break;
            case 1010:
                LExit = true;
                RExit = false;
                UExit = true;
                DExit = false;
                break;
            case 1011:
                LExit = true;
                RExit = false;
                UExit = true;
                DExit = true;
                break;
            case 1100:
                LExit = true;
                RExit = true;
                UExit = false;
                DExit = false;
                break;
            case 1101:
                LExit = true;
                RExit = true;
                UExit = false;
                DExit = true;
                break;
            case 1110:
                LExit = true;
                RExit = true;
                UExit = true;
                DExit = false;
                break;
            case 1111:
                LExit = true;
                RExit = true;
                UExit = true;
                DExit = true;
                break;
        }
    }
}
