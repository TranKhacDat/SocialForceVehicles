package tran;

import javax.vecmath.Vector2d;
import java.util.concurrent.*;

/**
 * 
 * Created by Fatema on 20.10.2016
 * Edited by Dat Tran on 09.05.2017
 *
 */


public interface IVehicle extends Callable<IVehicle>{


    /**
     * returns the current goal position
     * @return goal position
     **/
    Vector2d getGoalposition();

    /**
     * set the goal position
     * @return the object itself
     **/
    IVehicle setGoalposition( Vector2d p_position );

    /**
     * returns the current position
     * @return current position
     **/
    Vector2d getPosition();

    /**
     * set the current position
     * @return the object itself
     **/
    IVehicle setPosition( final double p_x, final double p_y );

    /**
     * returns the current velocity
     * @return current velocity
     **/
    Vector2d getVelocity();

    /**
     * returns the current speed
     * @return current speed
     **/
    double getSpeed();

    /**
     * set the current position's X
     * @return the object itself
     **/
    IVehicle setposX( final double p_posX );

    /**
     * set the current position's Y
     * @return the object itself
     **/
    IVehicle setposY( final double p_posY );

    /**
     * calculate accelaration
     * @return accelaration
     **/
    Vector2d accelaration();

}

