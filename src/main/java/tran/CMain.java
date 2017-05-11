package tran;

import javax.swing.*;


/**
 * 
 * Created by Fatema on 20.10.2016
 * Edited by Dat Tran on 09.05.2017
 *
 */

public class CMain { 

    public CMain()
    {
    }

    public static void main( String []args ) throws InterruptedException
    {
        JFrame frame = new JFrame("Social Force Model!");
        CEnvironment l_env = new CEnvironment();

        frame.add( l_env );

        frame.setSize( 100, 600 );
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
