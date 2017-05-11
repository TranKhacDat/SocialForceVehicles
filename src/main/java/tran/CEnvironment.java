package tran;

import java.awt.*;
import javax.swing.JPanel;
import javax.vecmath.Vector2d;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * 
 * Created by Fatema on 20.10.2016
 * Edited by Dat Tran on 09.05.2017
 *
 */


public class CEnvironment extends JPanel {

    private Graphics2D graphics2d;
    private ArrayList<CVehicle> m_vehicle = new ArrayList<CVehicle>();
    private ArrayList<CStatic> m_wall = new ArrayList<>( 2 );
    private ArrayList<CWall> m_walledge = new ArrayList<>( );
    public ArrayList<COutput> test = new ArrayList<>();

    public CEnvironment() {

        setFocusable( true );
        setBackground( Color.WHITE );
        setDoubleBuffered( true );


        m_wall.add( new CStatic( 100, 0, 20, 820 ) );
        

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
