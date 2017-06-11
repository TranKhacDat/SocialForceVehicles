package tran.socialforcevehicles;

import javax.swing.*;
import java.awt.*;
import javax.swing.JPanel;
import javax.vecmath.Vector2d;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.concurrent.*;


/**
 * 
 * Created by Dat Tran on 12.06.2017
 *
 */

public final class CMainTestVehicle extends TestCAgent {

    public CMainTestVehicle()
    {
    }

    public static void main( String []args ) throws InterruptedException
    {
        JFrame frame = new JFrame("Social Force Model!");
        CEnvironment l_env = new CEnvironment();

        frame.add( l_env );

        frame.setSize(335, 700 );
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        int i =0;
        long startTime = System.currentTimeMillis();
        while ( i < 6550 ) {

            l_env.update();
            l_env.repaint();
            i++;
            Thread.sleep( 10 );
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);

    }
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
 * environment class
 */

class CEnvironment extends JPanel {

    private Graphics2D graphics2d;
    private ArrayList<CVehicle> m_vehicle = new ArrayList<CVehicle>();
    private ArrayList<CStatic> m_wall = new ArrayList<>( 2 );
    private ArrayList<CWall> m_walledge = new ArrayList<>( );
    public ArrayList<COutput> test = new ArrayList<>();

    public CEnvironment() {

        setFocusable( true );
        setBackground( Color.WHITE );
        setDoubleBuffered( true );

        m_wall.add( new CStatic( 0, 0, 20, 820 ) ); // position -> , position | , width_wall, height_wall 
        
        m_wall.add( new CStatic( 100, 0, 20, 300 ) );
        
        m_wall.add( new CStatic( 200, 350, 20, 400 ) );
        
        m_wall.add( new CStatic( 300, 0, 20, 820 ) );

        m_wall.forEach( i->
        {
            m_walledge.add(i.getwall1());
            m_walledge.add(i.getwall2());
            m_walledge.add(i.getwall3());
            m_walledge.add(i.getwall4());
        }
        );

     
        IntStream.range( 0, 1 )
                .forEach( i -> m_vehicle.add( new CVehicle( new Vector2d(36,40),
                		1, new CGoal(415, 250, 0, 100).get_goals(), this ) ) );

        IntStream.range( 0, 1 )
                .forEach( i -> m_vehicle.add( new CVehicle( new Vector2d( 36,0 ),
                		1, new CGoal(310, 350, 550, 200).get_goals(), this ) ) );

      

    }

    /**
     * paint all elements
     * @return
     **/
    public void paint( Graphics g ) {
        super.paint( g );
        graphics2d = ( Graphics2D ) g;
        drawWall(Color.GRAY);
        drawVehicle();
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }

    /**
     * draw each static element wall
     * @return
     **/
    private void drawWall(Color color) {
        graphics2d.setColor( color ) ;
        m_wall.forEach( i -> graphics2d.fillRect( i.getX1(), i.getY1(), i.getWidth(), i.getHeight() ) );
    }

    /**
     * draw each Vehicle
     * @return
     **/
    private void drawVehicle() {
        graphics2d.setColor( Color.BLUE ) ;
        for( int j = 0; j < 1; j++ )
        {
            Ellipse2D.Double shape = new Ellipse2D.Double( m_vehicle.get(j).getPosition().getX(), m_vehicle.get(j).getPosition().getY(), 30, 30 ); //60
            graphics2d.fill( shape );
        }
        graphics2d.setColor( Color.ORANGE ) ;
        for( int j = 1; j < 2; j++ )
        {
            Ellipse2D.Double shape = new Ellipse2D.Double( m_vehicle.get(j).getPosition().getX(), m_vehicle.get(j).getPosition().getY(), 30, 30 );
            graphics2d.fill( shape );
        }
     

    }
    /**
     * get the list of Vehicle with their information
     * @return a list of Vehicle information
     **/
    public ArrayList<CVehicle> getVehicleinfo()
    {
        return m_vehicle;
    }

    /**
     * get the list of walls with their information
     * @return a list of wall information
     **/
    public ArrayList<CWall> getWallinfo()
    {
        return m_walledge;
    }

    /**
     * undate environment state
     * @return
     **/
    public void update() {

            m_vehicle.stream()
                    .parallel()
                    .forEach( j ->
                    {
                        try
                        {
                            j.call();
                        }
                        catch ( final Exception l_exception ) {}
                    });
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
 * force class
 */

class CForce {

    private static final double m_detta = 2;
    private static final double m_lamda = 0.2;
    private static final double m_repulsefactortoped = 2.1;
    private static final double m_sigma = 0.3;
    private static final double m_repulsefactortowall = 5;
    private static final double m_R = 0.2;

    /**
     * calculate seek force towards goal position
     * @return force vector
     **/
    static Vector2d drivingForce( final Vector2d p_desiredvelocity, final Vector2d p_current )
    {
        return CVector.sub( p_desiredvelocity, p_current );
    }

    /**
     * helper function to calculate repulsive force to Vehicle
     * @return double value
     **/
    static double calculateb( final CVehicle p_self, final CVehicle p_other )
    {
        final Vector2d l_tempvector = CVector.sub( p_self.getPosition(), p_other.getPosition() );

        final double l_tempvalue = CVector.sub( l_tempvector, CVector.scale( m_detta, p_other.getVelocity() ) ).length();

        return Math.sqrt( ( l_tempvector.length() + l_tempvalue ) * ( l_tempvector.length() + l_tempvalue )
                                         - ( p_other.getSpeed() * p_other.getSpeed() ) );
    }

    /**
     * calculate repulsive force towards other Vehicle
     * @return force vector
     **/
    static Vector2d repulseothers( final CVehicle p_self, final CVehicle p_other )
    {
        final Vector2d l_normvector = CVector.direction( p_other.getPosition(), p_self.getPosition() );

        final double l_temp = - calculateb( p_self, p_other ) / m_sigma;

        return CVector.scale( m_repulsefactortoped * Math.exp( l_temp ), l_normvector );
    }

    /**
     * calculate repulsive force towards other Vehicle (another way)
     * @return force vector
     **/
    static Vector2d repulseotherPed( final CVehicle p_self, final CVehicle p_other, ArrayList<COutput> test )
    {
        final double l_radious = p_self.getM_radius() * 0.5 + p_other.getM_radius() * 0.5 ;
        final double l_temp = l_radious - CVector.distance( p_self.getPosition(), p_other.getPosition() );
        //System.out.println( p_self + " dis " + l_temp );
        //if ( l_temp >= -4 ) {
        //test.add( new COutput( p_self.getPosition().x,p_self.getPosition().y, m_repulsefactortoped * Math.exp( l_temp / m_sigma ) * anisotropic_character( p_self.getPosition(),
               // p_other.getPosition() ) ) );
           // System.out.println( p_self.getPosition()+" self "+m_repulsefactortoped * Math.exp( l_temp / m_sigma )*anisotropic_character( p_self.getPosition(),
                   // p_other.getPosition() ) +" other "+ p_other.getPosition());
        return CVector.scale( m_repulsefactortoped * Math.exp( l_temp / m_sigma ) * anisotropic_character( p_self.getPosition(),
                    p_other.getPosition() ), CVector.normalize( CVector.sub( p_self.getPosition(), p_other.getPosition() ) ) );
        //}

        //return new Vector2d( 0, 0 );
    }

    /**
     * calculate repulsive force towards wall
     * @return force vector
     **/
    static Vector2d repulsewall( final CVehicle p_self, final CWall p_wall, ArrayList<COutput> test )
    {
        final Vector2d l_normposition = CVector.perpendicular_derection( p_self.getPosition(), p_wall );

        if ( CVector.check( l_normposition, p_wall.getPoint1(), p_wall.getPoint2() ) )
        {
            final double l_temp = p_self.getM_radius() - CVector.distance( p_self.getPosition(), l_normposition );
            //double p = m_repulsefactortowall * Math.exp( l_temp / m_R );
            if ( l_temp >= - 4 ) {
                //test.add( new COutput( p_self.getPosition().x,p_self.getPosition().y, p ) );

                //System.out.println( p_self.getPosition()+" self "+m_repulsefactortowall * Math.exp( l_temp / m_R )+" wall "+ l_normposition);
                return CVector.scale( m_repulsefactortowall * Math.exp( l_temp / m_R ), CVector.normalize(
                        CVector.sub( p_self.getPosition(), l_normposition ) ) );
            }
        }
        return new Vector2d(0,0);
    }

    /**
     * check if wall/other Vehicles is under Vehicle's point of view or not?
     * @return double value
     **/
    static double anisotropic_character( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return m_lamda + ( 1 -m_lamda )*( ( 1 + CVector.angle( p_v1, p_v2 ) ) * 0.5 );
    }

}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
 * goal class
 */

class CGoal {

    private ArrayList<Vector2d> m_goals = new ArrayList<>();

    public CGoal( final double l_x, final double l_x1, final double l_x2, final double l_x3 )
    {
        //m_goals.add( new Vector2d( l_x , 20 + ( Math.random() * 100 ) ) );
    	
    	
    	m_goals.add( new Vector2d( l_x , 160000 + ( Math.random() * 60 ) ) );//20
    	m_goals.add( new Vector2d( l_x, 265 ) );
        m_goals.add( new Vector2d( l_x2 + ( 20 + Math.random() * l_x3 ) , 400 + ( Math.random() * 140 ) ) );
    	
    	
    	//m_goals.add( new Vector2d (100,100));
    	
    }

    /**
     * returns get list of goals
     * @return list of goals
     **/
    public ArrayList<Vector2d> get_goals()
    {
        return m_goals;
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
 * output class
 */

class COutput {

    public double m_selfX;
    public double m_selfY;
    public double m_force;
    public COutput( double p_selfX, double p_selfY, double p_force )
    {
        m_selfX = p_selfX;
        m_selfY = p_selfY;
        m_force = p_force;
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
* static class
*/

class CStatic {

    private final int m_X1;
    private final int m_Y1;
    private final int m_width;
    private final int m_height;

    public CStatic( final int p_x1, final int p_y1, final int p_width, final int p_height )
    {
        m_X1 = p_x1;
        m_Y1 = p_y1;
        m_width = p_width;
        m_height = p_height;
    }

    /**
     * returns static object's position's X
     * @return X
     **/
    public final int getX1()
    {
        return m_X1;
    }

    /**
     * returns static object's position's Y
     * @return Y
     **/
    public final int getY1()
    {
        return m_Y1;
    }

    /**
     * returns static object's wall1 among 4 sorrounded walls
     * @return wall1
     **/
    public final CWall getwall1()
    {
        return new CWall( new Vector2d( m_X1, m_Y1 ), new Vector2d( m_X1 + m_width, m_Y1 ) ) ;
    }

    /**
     * returns static object's wall2 among 4 sorrounded walls
     * @return wall2
     **/
    public final CWall getwall2()
    {
        return new CWall( new Vector2d( m_X1, m_Y1 ), new Vector2d( ( m_X1 ), ( m_Y1 + m_height ) ) ) ;
    }

    /**
     * returns static object's wall3 among 4 sorrounded walls
     * @return wall3
     **/
    public final CWall getwall3()
    {
        return new CWall( new Vector2d( ( m_X1 + m_width ), ( m_Y1 + m_height ) ), new Vector2d( m_X1 + m_width, m_Y1 ) ) ;
    }

    /**
     * returns static object's wall4 among 4 sorrounded walls
     * @return wall4
     **/
    public final CWall getwall4()
    {
        return new CWall( new Vector2d( ( m_X1 + m_width ), ( m_Y1 + m_height ) ), new Vector2d( ( m_X1 ), ( m_Y1 + m_height ) ) ) ;
    }

    /**
     * returns static object's width
     * @return width
     **/
    public final int getWidth() {
        return m_width;
    }

    /**
     * returns static object's height
     * @return height
     **/
    public final int getHeight() {
        return m_height;
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
* vector class
*/

final class CVector {

    /**
     * calculate direction between two points
     * @return direction
     **/
    static Vector2d direction( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return normalize( sub( p_v1, p_v2 ) );
    }

    /**
     * to scale vector
     * @return scaled vector
     **/
    static Vector2d scale( final double p_speed, final Vector2d p_v1 )
    {
        return new Vector2d( p_v1.getX() * p_speed, p_v1.getY() * p_speed );
    }

    /**
     * vector add operation of 2 given vectors
     * @return result of addition
     **/
    static Vector2d add( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return new Vector2d( p_v1.getX() + p_v2.getX(), p_v1.getY() + p_v2.getY() );
    }

    /**
     * vector add operation of 3 given vectors
     * @return result of addition
     **/
    static Vector2d add( final Vector2d p_v1, final Vector2d p_v2, final Vector2d p_v3 )
    {
        return new Vector2d( p_v1.getX() + p_v2.getX() + p_v3.getX(), p_v1.getY() + p_v2.getY() + p_v3.getY() );
    }

    /**
     * vector subtract operation of 2 given vectors
     * @return result of subtraction
     **/
    static Vector2d sub( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return new Vector2d( p_v1.getX() - p_v2.getX(), p_v1.getY() - p_v2.getY() );
    }

    /**
     * calculate distance between 2 points
     * @return distance
     **/
    static double distance( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return sub( p_v1, p_v2 ).length();
    }

    /**
     * calculate the center of any line
     * @return center point
     **/
    static Vector2d staticObjectCentre( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return new Vector2d( ( p_v1.getX() + p_v2.getX() )/2.0 , ( p_v1.getY() + p_v2.getY() )/2.0 );
    }

    /**
     * calculate cos of any angle theta
     * @return cos value of any angle
     **/
    static double cosdheta( final Vector2d p_v1, final Vector2d p_v2, final Vector2d p_v3 )
    {
        return ( direction( p_v1, p_v2 ).dot( sub( p_v3, p_v2 ) ) )/
                ( direction( p_v1, p_v2 ).length() * sub( p_v3, p_v2 ).length() );

    }

    /**
     * calculate angle between 2 vectors
     * @return angle
     **/
    static double angle( final Vector2d p_v1, final Vector2d p_v2 )
    {
        return p_v1.dot( p_v2 ) / p_v1.length() * p_v2.length();

    }

    /**
     * perpendicular direction from a point to a line
     * @return direction vector
     **/
    static Vector2d perpendicular_derection( final Vector2d p_position, final CWall l_wall )
    {
        final Vector2d l_wallPoint = l_wall.getPoint1();
        final Vector2d l_walldirection = CVector.normalize( CVector.sub( l_wall.getPoint2(), l_wall.getPoint1() ) );
        final double l_check = CVector.sub( p_position, l_wallPoint ).dot( l_walldirection );

        return CVector.add( l_wallPoint, CVector.scale( l_check, l_walldirection ) );
    }

    /**
     * truncate a vector's magnitude comparing with a given double value
     * @return truncated vector
     **/
    static Vector2d truncate( final Vector2d p_vector, final double p_scalefactor ) {
        double l_check;

        l_check = p_scalefactor / p_vector.length();
        l_check = l_check < 1.0 ? 1.0 : 1/l_check;

        return CVector.scale( l_check, p_vector );
    }

    /**
     * check if wall is under Vehicle point of view or not?
     * @return true or false
     **/
    static boolean check( final Vector2d p_point, final Vector2d p_wallpoint1, final Vector2d p_wallpoint2 )
    {
        final double l_wall2Towall1 = CVector.sub( p_wallpoint2, p_wallpoint1 ).length();
        final double l_pointTowall1 = CVector.sub( p_point, p_wallpoint1 ).length();
        final double l_pointTowall2 = CVector.sub( p_wallpoint2, p_point ).length();

        return ( l_wall2Towall1 == l_pointTowall1 + l_pointTowall2 ) ? true : false;
    }

    /**
     * calculate normalized vector
     * @return normalized vector
     **/
    static Vector2d normalize( final Vector2d p_vector ) {

        Vector2d l_temp = new Vector2d( 0, 0 );

        if ( p_vector.length() != 0 )
        {
            l_temp.x = p_vector.x / p_vector.length();
            l_temp.y = p_vector.y / p_vector.length();
        }

        return l_temp;
    }

    /**
     * calculatee distance from a point to a line
     * @return distance vector
     **/
    public static Vector2d distanceToWall( final Vector2d p_position, final Vector2d p_wall1, final Vector2d p_wall2 )
    {
        if( CVector.sub( p_position, p_wall1 ).dot( CVector.direction( p_wall2, p_wall1 ) ) <= 0 )
        {
            return CVector.sub( p_wall1, p_position );
        }
        else if( CVector.sub( p_position, p_wall1 ).dot( CVector.direction( p_wall2, p_wall1 ) ) > 0 && CVector.sub( p_position,
                            p_wall1 ).dot( CVector.direction( p_wall2, p_wall1 ) ) <= CVector.sub( p_wall2, p_wall1 ).length() )
        {
            return CVector.sub( CVector.sub( p_wall1, p_position ), CVector.scale( CVector.sub( p_wall1, p_position )
                            .dot( CVector.direction( p_wall2, p_wall1 ) ), CVector.direction( p_wall2, p_wall1 ) ) );
        }
        else return CVector.sub( p_wall2, p_position );

    }

}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
 * vehicle class
 */

class CVehicle implements IVehicle{

    private static final double m_maxspeedfactor = 2.5;
    private static final double m_maxforce = 3.0;
    private static final double m_radius = 10;
    private Vector2d m_position;
    private Vector2d m_goal;
    private ArrayList<Vector2d> m_goals;
    private Vector2d m_velocity ;
    private double m_speed;
    private CEnvironment l_env;
    private double m_maxspeed;
    private int m_controlossilation;

    public CVehicle( final Vector2d p_position, final double p_speed, final ArrayList<Vector2d> p_goal, final CEnvironment p_env ) {
        m_goals = p_goal;
        m_goal = p_goal.remove( 0 );
        m_position = p_position;
        m_speed = p_speed;
        m_velocity = CVector.scale( p_speed, CVector.direction( m_goal, m_position ) );
        l_env = p_env;
        m_maxspeed = p_speed * m_maxspeedfactor;
        m_controlossilation = 0;
    }

    @Override
    public Vector2d getGoalposition() {
        return m_goal;
    }

    @Override
    public IVehicle setGoalposition( final Vector2d p_position )
    {
        this.m_goal = p_position;
        return this;
    }

    @Override
    public Vector2d getPosition() {
        return m_position;
    }

    @Override
    public IVehicle setPosition( final double p_x, final double p_y ) {
        this.m_position = new Vector2d( p_x, p_y );
        return this;
    }

    @Override
    public Vector2d getVelocity() {
        return m_velocity;
    }

    @Override
    public double getSpeed() {
        return m_speed;
    }


    @Override
    public IVehicle setposX( final double p_posX ) {
        this.m_position.x = p_posX;
        return this;
    }

    @Override
    public IVehicle setposY( final double p_posY ) {
        this.m_position.y = p_posY;
        return this;
    }

    @Override
    public Vector2d accelaration()
    {
        Vector2d l_repulsetoWall = new Vector2d( 0, 0 );
        Vector2d l_repulsetoOthers = new Vector2d( 0, 0 );
        Vector2d l_desiredVelocity = CVector.scale( this.m_maxspeed, CVector.direction( this.getGoalposition(), this.getPosition() ) );

        for ( int i = 0; i < l_env.getWallinfo().size(); i++ )
        {
            l_repulsetoWall = CVector.add( l_repulsetoWall, CForce.repulsewall( this, l_env.getWallinfo().get( i ), l_env.test ) );
        }


        for ( int i = 0; i < l_env.getVehicleinfo().size(); i++ )
        {
            if( !l_env.getVehicleinfo().get(i).equals( this ) )
            {
                l_repulsetoOthers = CVector.add( l_repulsetoOthers, CForce.repulseotherPed( this, l_env.getVehicleinfo().get( i ), l_env.test ) );
            }
        }

        final Vector2d l_temp = CVector.add( CForce.drivingForce( l_desiredVelocity, this.getVelocity() ), l_repulsetoOthers );

        return CVector.truncate( CVector.add( l_temp, l_repulsetoWall ), m_maxforce );

    }

    /**
     * returns vehicle's radius
     * @return radius
     **/
    public double getM_radius()
    {
        return m_radius;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public IVehicle call() throws Exception {

        final double l_check = CVector.sub( this.getGoalposition(), this.getPosition() ).length();

        if ( this.m_goals.isEmpty() ) { m_controlossilation ++; }

        if ( l_check <= this.getM_radius() * 0.5 )
        {
            this.m_velocity = new Vector2d(0, 0);
            if ( this.m_goals.size() > 0 )
            {
                this.m_goal = this.m_goals.remove( 0 );
                this.m_velocity = CVector.scale( m_maxspeed, CVector.normalize( CVector.add( this.m_velocity, this.accelaration() ) ) );
                this.m_position = CVector.add( m_position, m_velocity );
            }
        }
        else
        {
            if( m_controlossilation >= 1000 )
            {
                this.m_velocity = new Vector2d( 0, 0 );
            }
            else
            {
                this.m_velocity = CVector.scale( m_maxspeed, CVector.normalize( CVector.add( this.m_velocity, this.accelaration() ) ) );
                this.m_position = CVector.add( m_position, m_velocity );
            }
        }

        if( m_position.getX() > 800.0 ) {
            setposX( 0.0 );
        }
        if( m_position.getX() < 0.0 ) {
            setposX( 800.0 );
        }
        if( m_position.getY() > 600.0 ) {
            setposY( 0.0 );
        }
        if( m_position.getY() < 0.0 ) {
            setposY( 600.0);
        }

        return this;
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
* wall class
*/

class CWall {

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

//---------------------------------------------------------------------------------------------------------------------------------------------------------


/**
* vehicle interface
*/

interface IVehicle extends Callable<IVehicle>{


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


