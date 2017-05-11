/**
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of SocialForceVehicles by Khac Dat Tran.                         #
 * # Copyright (c) 2017, Khac Dat Tran (khac.dat.tran@tu-clausthal.de)                  #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU Lesser General Public License as                     #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU Lesser General Public License for more details.                                #
 * #                                                                                    #
 * # You should have received a copy of the GNU Lesser General Public License           #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package tran.socialforcevehicles;

import javax.swing.*;


/**
 *
 * Created by Fatema on 20.10.2016
 * Edited by Dat Tran on 09.05.2017
 *
 */

public final class CMain
{
    /**
     * Hidden constructor
     */
    private CMain()
    {
    }

    /**
     * Main
     * @param p_args command line args
     * @throws InterruptedException Throws exception if interrupted
     */
    public static void main( final String[] p_args ) throws InterruptedException
    {
        final JFrame l_frame = new JFrame( "Social Force Model!" );
//        CEnvironment l_env = new CEnvironment();
//
//        frame.add( l_env );

        l_frame.setSize( 100, 600 );
        l_frame.setVisible( true );
        l_frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        int i = 0;
        final long l_startTime = System.currentTimeMillis();
        while ( i < 6550 )
        {

//            l_env.update();
//            l_env.repaint();
            i++;
            Thread.sleep( 10 );
        }
        final long l_stopTime = System.currentTimeMillis();
        final long l_elapsedTime = l_stopTime - l_startTime;
        System.out.println( l_elapsedTime );
    }
}
