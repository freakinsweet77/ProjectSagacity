/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3project.sagacity;

/**
 * @author Kenton
 */
public class Floor
{

    private char pathmap[][];
    private int gridsize;
    private int numrooms;
    private int currooms;
    private int row;
    private int col;

    public Floor(int newnumrooms)
    {
        gridsize = ((int) (Math.sqrt(newnumrooms)) + 3 /*3 is a magic number just to make our floor have space for empty rooms*/);
        pathmap = new char[gridsize][gridsize];
        numrooms = newnumrooms;
        currooms = 0;
        for (row = 0; row < pathmap.length; row++)
        {
            for (col = 0; col < pathmap[row].length; col++)
            {
                pathmap[row][col] = 'E';
            }
        }
    }

    /*
     *  S = Start
     *  R = Room
     *  E = Empty
     *  B = Boss
     */
    public void generatefloor()
    {
        for (row = 0; row < pathmap.length; row++)
        {
            for (col = 0; col < pathmap[row].length; col++)
            {
                if (currooms < numrooms && pathmap[row][col] == 'E')
                {
                    if (row == 0 && col == 0)
                    {
                        pathmap[row][col] = 'S';
                        currooms++;
                    } else if ((col - 1 >= 0 && pathmap[row][col - 1] != 'E')
                            || (row - 1 >= 0 && pathmap[row - 1][col] != 'E')
                            || (col + 1 < pathmap[row].length && pathmap[row][col + 1] != 'E')
                            || (row + 1 < pathmap.length && pathmap[row + 1][col] != 'E'))
                    {
                        if ((int) (Math.random() * 7 + 1) % 2 == 0)
                        {
                            if (currooms == numrooms - 1)
                            {
                                pathmap[row][col] = 'B';
                                currooms++;
                            } else
                            {
                                pathmap[row][col] = 'R';
                                currooms++;
                            }
                        } else
                        {
                            pathmap[row][col] = 'E';
                        }
                    }
                }
            }
        }
        if (currooms < numrooms)
        {
            generatefloor();
        }
    }
    
    public char[][] getFloor()
    {
        return pathmap;
    }
    
    public int getAttached(int row, int col)
    {
        int exits = 0;
        
        if((col - 1) >= 0 && pathmap[row][col - 1] != 'E')
        {
            exits += 1000;
        }
        if((row - 1) >= 0 && pathmap[row - 1][col] != 'E')
        {
            exits += 10;
        }
        if((col + 1) < pathmap[row].length && pathmap[row][col + 1] != 'E')
        {
            exits += 100;
        }
        if((row + 1) < pathmap.length && pathmap[row + 1][col] != 'E')
        {
            exits += 1;
        }
        
        return exits;
    }

    public void printfloor()
    {
        for (row = 0; row < pathmap.length; row++)
        {
            for (col = 0; col < pathmap[row].length; col++)
            {
                System.out.print(pathmap[row][col]);
            }
            System.out.println();
        }
    }

}
