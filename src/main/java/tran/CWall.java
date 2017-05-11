package tran;

import javax.vecmath.Vector2d;

/**
 * 
 * Created by Fatema on 20.10.2016
 * Edited by Dat Tran on 09.05.2017
 *
 */


public class CWall {

    private Vector2d m_point1;
    private Vector2d m_point2;

    public CWall( final Vector2d p_point1, final Vector2d p_point2 )
    {
        m_point1 = p_point1;
        m_point2 = p_point2;
    }

    /**
     * returns the first point of the wall
     * @return point1
     **/
    public Vector2d getPoint1() {
        return m_point1;
    }

    /**
     * returns the second point of the wall
     * @return point2
     **/
    public Vector2d getPoint2() {
        return m_point2;
    }
}
