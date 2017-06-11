/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason SocialForce                                    #
 * # Copyright (c) 2017, LightJason (info@lightjason.org)                               #
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


import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.action.binding.IAgentAction;
import org.lightjason.agentspeak.action.binding.IAgentActionFilter;
import org.lightjason.agentspeak.action.binding.IAgentActionName;
import org.lightjason.agentspeak.agent.IBaseAgent;
import org.lightjason.agentspeak.configuration.IAgentConfiguration;
import org.lightjason.agentspeak.consistency.metric.CNCD;
import org.lightjason.agentspeak.consistency.metric.IMetric;
import org.lightjason.agentspeak.generator.IAgentGenerator;
import org.lightjason.agentspeak.generator.IBaseAgentGenerator;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.socialforce.collectors.CSum;
import org.lightjason.socialforce.model.CDefaultModel;
import org.lightjason.socialforce.model.IModel;
import org.lightjason.socialforce.potential.CExponential;
import org.lightjason.socialforce.potential.rating.CPositiveNegative;

import java.awt.*;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.LogManager;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * agent test-case
 */
public final class TestCAgent extends IBaseTest
{
    static
    {
        LogManager.getLogManager().reset();
    }

    /**
     * test force of two agents
     *
     * @throws Exception any exception
     */
    @Test
    @Ignore
    public final void twoagent() throws Exception
    {
        final CEnvironement l_environment = new CEnvironement( 150, 150 );


        final IAgentGenerator<CAgent> l_generator1 = new CAgent.CGenerator( IOUtils.toInputStream(
            "bar. +!main <- generic/print('bar agent').",
            "UTF-8"
        ), l_environment );

        final IAgentGenerator<CAgent> l_generator2 = new CAgent.CGenerator( IOUtils.toInputStream(
            "foo. +!main <- generic/print('foo agent').",
            "UTF-8"
        ), l_environment );


        final CAgent l_agent1 = l_generator1.generatesingle(
                                                    new DenseDoubleMatrix1D( new double[]{70, 70} ),
                                                             new DenseDoubleMatrix1D( new double[]{150, 150} ),
                                                             25
        );

        final CAgent l_agent2 = l_generator2.generatesingle(
            new DenseDoubleMatrix1D( new double[]{90, 90} ),
            new DenseDoubleMatrix1D( new double[]{0, 0} ),
            50
        );


        l_agent1.heatmap( 300, 300 ).view( "Agent 1" );
        //l_agent2.heatmap( 300, 300 ).view( "Agent 2" );
    }



    /**
     * main test call
     *
     * @param p_args arguments
     */
    public static void main( final String[] p_args )
    {
        new TestCAgent().invoketest();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------


    /**
     * environment class
     */
    private static final class CEnvironement
    {
        /**
         * grid representation
         */
        private final ObjectMatrix2D m_grid;

        /**
         * ctor
         *
         * @param p_ysize y-size
         * @param p_xsize x-size
         */
        CEnvironement( final int p_ysize, final int p_xsize )
        {
            m_grid = new SparseObjectMatrix2D( p_ysize, p_xsize );
        }


        /**
         * move object
         *
         * @param p_agent current object
         * @param p_position new position
         * @return object on the new cell position
         */
        public final synchronized  CAgent move( final CAgent p_agent, final DoubleMatrix1D p_position )
        {
            final DoubleMatrix1D l_position = this.inside( p_position );

            // check of the target position is free, if not return object, which blocks the cell
            final CAgent l_object = (CAgent) m_grid.getQuick( (int) l_position.getQuick( 0 ), (int) l_position.getQuick( 1 ) );
            if ( l_object != null )
                return l_object;

            // cell is free, move the position
            m_grid.set( (int) p_agent.position().get( 0 ), (int) p_agent.position().get( 1 ), null );
            m_grid.set( (int) l_position.getQuick( 0 ), (int) l_position.getQuick( 1 ), p_agent );

            return p_agent;
        }

        /**
         * stream of all agents
         *
         * @param p_xpos x-position
         * @param p_ypos y-position
         * @return stream data
         */
        @SuppressWarnings( "unchecked" )
        public final Stream<CAgent> stream( final int p_ypos, final int p_xpos )
        {
            final CAgent l_agent = (CAgent) m_grid.getQuick(
                                                Math.min( m_grid.rows() - 1, Math.max( 0, p_ypos ) ),
                                                Math.min( m_grid.columns() - 1, Math.max( 0, p_xpos ) )
            );
            return l_agent == null ? Stream.of() : Stream.of( l_agent );
        }

        /**
         * cuts the vector to valid coordinates
         *
         * @param p_position position
         * @return new vector
         */
        private DoubleMatrix1D inside( final DoubleMatrix1D p_position )
        {
            return new DenseDoubleMatrix1D( new double[]{
                Math.min( m_grid.rows() - 1, Math.max( 0, p_position.getQuick( 0 ) ) ),
                Math.min( m_grid.columns() - 1, Math.max( 0, p_position.getQuick( 1 ) ) ),
            } );
        }

    }


    // ---------------------------------------------------------------------------------------------------------------------------------------------------------


    /**
     * agent with force extension on beliefs
     */
    @IAgentAction
    private static final class CAgent extends IBaseAgent<CAgent> implements IForce<CAgent>
    {
        /**
         * maximum value of the metric
         */
        private static final double METRICMAXIMUM = 1;
        /**
         * metric - comparing beliefbases
         */
        private static final IMetric METRIC = new CNCD();
        /**
         * force model
         */
        private static final IModel<CAgent> FORCEMODEL = new CDefaultModel<>();
        /**
         * test potential function
         */
        private static final UnaryOperator<Double> POTENTIAL = new CExponential( () -> 1D, () -> 0.5D );
        /**
         * test rating function
         */
        private static final BiFunction<Double, Double, Double> RATING = new CPositiveNegative( () -> 15D, () -> 0.5 );
        /**
         * maxim force efficiency
         */
        private final double m_forceefficency;
        /**
         * current position
         */
        private final DoubleMatrix1D m_position;
        /**
         * goal position
         */
        private final DoubleMatrix1D m_goal;
        /**
         * environment reference
         */
        private final CEnvironement m_environment;

        /**
         * ctor
         *
         * @param p_configuration agent configuration
         * @param p_environment environment
         * @param p_start start position
         * @param p_goal goal position
         * @param p_forceefficency force efficency
         */
        private CAgent( final IAgentConfiguration<CAgent> p_configuration, final CEnvironement p_environment,
                        final DoubleMatrix1D p_start, final DoubleMatrix1D p_goal, final double p_forceefficency )
        {
            super( p_configuration );
            m_goal = p_goal;
            m_position = p_start;
            m_environment = p_environment;
            m_forceefficency = p_forceefficency;

            if ( !this.equals( m_environment.move( this, m_position ) ) )
                throw new RuntimeException(
                    MessageFormat.format(
                        "agent cannot be set into environment on [{0}x{1}], position is not empty",
                        m_position.getQuick( 0 ), m_position.getQuick( 1 )
                    )
                );
        }

        /**
         * calculates the heatmap of the social-force
         *
         * @param p_imagewidth image width
         * @param p_imageheight image height
         * @return heatmap
         */
        public final CHeatMap heatmap( final int p_imagewidth, final int p_imageheight )
        {
            final int l_size = (int) m_forceefficency;
            final DoubleMatrix2D l_force = new SparseDoubleMatrix2D( m_environment.m_grid.rows(), m_environment.m_grid.columns() );

            IntStream.range( -l_size, l_size )
                     .parallel()
                     .forEach(
                         i -> IntStream.range( -l_size, l_size )
                                       .filter( j -> ( j != 0 ) && ( i != 0 ) )
                                       .forEach(
                                           j -> this.force( l_force, m_position.getQuick( 0 ) + i, m_position.getQuick( 1 ) + j )
                                       ) );

            //System.out.println( l_force );

            return CHeatMap.from( p_imagewidth, p_imageheight, l_force, Color.CYAN, Color.BLUE, Color.BLACK, Color.RED, Color.YELLOW );
        }

        /**
         * set force
         *
         * @param p_force force matrix
         * @param p_ypos y-position
         * @param p_xpos x-position
         */
        private void force( final DoubleMatrix2D p_force, final Number p_ypos, final Number p_xpos )
        {
            p_force.set(
                Math.min( p_force.rows() - 1, Math.max( 0, p_ypos.intValue() ) ),
                Math.min( p_force.columns() - 1, Math.max( 0, p_xpos.intValue() ) ),
                FORCEMODEL.apply( this, m_environment.stream( p_ypos.intValue(), p_xpos.intValue() ) )
            );
        }

        /**
         * returns the external view of the agent
         *
         * @return literal stream
         */
        public final Stream<ILiteral> literal()
        {
            return m_beliefbase.stream();
        }

        /**
         * returns position
         *
         * @return position vector
         */
        public final DoubleMatrix1D position()
        {
            return m_position;
        }

        /**
         * returns goalposition
         *
         * @return position vector
         */
        public final DoubleMatrix1D goal()
        {
            return m_goal;
        }

        @Override
        public final Function<CAgent, Double> metric()
        {
            return ( i ) -> 0.5D;
            //METRIC.apply( m_beliefbase.stream(), i.beliefbase().stream() );
        }

        /**
         * move left action
         */
        @IAgentActionFilter
        @IAgentActionName( name = "move/left" )
        private void moveleft()
        {
            this.move( EDirection.LEFT );
        }

        /**
         * move right action
         */
        @IAgentActionFilter
        @IAgentActionName( name = "move/right" )
        private void moveright()
        {
            this.move( EDirection.RIGHT );
        }

        /**
         * move forward action
         */
        @IAgentActionFilter
        @IAgentActionName( name = "move/forward" )
        private void moveforward()
        {
            this.move( EDirection.FORWARD );
        }

        /**
         * move execution
         *
         * @param p_direction direction
         */
        private void move( final EDirection p_direction )
        {
            if ( m_goal.equals( m_position ) )
                return;

            // calculate new position, try to move if possible set new position
            final DoubleMatrix1D l_position = p_direction.position( m_position, m_goal, 1 + Math.random() );

            if ( !this.equals( m_environment.move( this, l_position ) ) )
                throw new RuntimeException();

            m_position.assign( l_position );
        }


        @Override
        public final UnaryOperator<Double> potential()
        {
            return POTENTIAL;
        }

        @Override
        public final BiFunction<Double, Double, Double> potentialrating()
        {
            return RATING;
        }

        @Override
        public final Collector<Double, ?, Double> potentialreduce()
        {
            return CSum.factory();
        }

        @Override
        public final BiFunction<CAgent, CAgent, Double> distancescale()
        {
            return (i, j) -> METRICMAXIMUM - Algebra.DEFAULT.norm2(
                new DenseDoubleMatrix1D( i.position().toArray() )
                    .assign(
                        new DenseDoubleMatrix1D( new double[]{j.position().getQuick( 0 ), j.position().getQuick( 1 ) } ),
                        Functions.minus
                    )
            ) / m_forceefficency;
        }

        @Override
        public final Collector<Double, ?, Double> forceresult()
        {
            return CSum.factory();
        }


        /**
         * agent generator
         */
        public static final class CGenerator extends IBaseAgentGenerator<CAgent>
        {
            /**
             * actions
             */
            private static final Set<IAction> ACTIONS = Collections.unmodifiableSet(
                                                            Stream.concat(
                                                                org.lightjason.agentspeak.common.CCommon.actionsFromPackage(),
                                                                org.lightjason.agentspeak.common.CCommon.actionsFromAgentClass( CAgent.class )
                                                            ).collect( Collectors.toSet() )
            );
            /**
             * environment
             */
            private final CEnvironement m_environment;

            /**
             * ctor
             *
             * @param p_stream ASL stream
             * @param p_environement environment reference
             * @throws Exception thrown on any error
             */
            CGenerator( final InputStream p_stream, final CEnvironement p_environement ) throws Exception
            {
                super( p_stream, ACTIONS );
                m_environment = p_environement;
            }

            @Override
            @SuppressWarnings( "unchecked" )
            public final CAgent generatesingle( final Object... p_data )
            {
                return new CAgent( m_configuration, m_environment, (DoubleMatrix1D) p_data[0], (DoubleMatrix1D) p_data[1], ( (Number) p_data[2] ).doubleValue() );
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------


    /**
     * direction enum (counter clockwise)
     */
    private enum EDirection
    {
        FORWARD( 0 ),
        FORWARDLEFT( 45 ),
        LEFT( 90 ),
        BACKWARDLEFT( 135 ),
        BACKWARD( 180 ),
        BACKWARDRIGHT( 225 ),
        RIGHT( 270 ),
        FORWARDRIGHT( 315 );


        /**
         * rotation-matrix for the direction vector
         */
        private final DoubleMatrix2D m_rotation;

        /**
         * ctor
         *
         * @param p_alpha rotation of the normal-viewpoint-vector
         */
        EDirection( final double p_alpha )
        {
            m_rotation = rotationmatrix( Math.toRadians( p_alpha ) );
        }

        /**
         * creates a rotation matrix
         *
         * @param p_alpha angel in radians
         * @return matrix
         *
         * @see https://en.wikipedia.org/wiki/Rotation_matrix
         */
        private static DoubleMatrix2D rotationmatrix( final double p_alpha )
        {
            final double l_sin = Math.sin( p_alpha );
            final double l_cos = Math.cos( p_alpha );
            return new DenseDoubleMatrix2D( new double[][]{{l_cos, l_sin}, {-l_sin, l_cos}} );
        }

        /**
         * calculates the new position
         *
         * @param p_position current position
         * @param p_goalposition goal position
         * @param p_speed number of cells / step size
         * @return new position
         */
        public DoubleMatrix1D position( final DoubleMatrix1D p_position, final DoubleMatrix1D p_goalposition, final double p_speed )
        {
            // calculate the stright line by: current position + l * (goal position - current position)
            // normalize direction and rotate the normalized vector based on the direction
            // calculate the target position based by: current position + speed * rotate( normalize( goal position - current position ) )
            final DoubleMatrix1D l_view = new DenseDoubleMatrix1D( p_goalposition.toArray() );
            return Algebra.DEFAULT.mult(
                m_rotation,
                l_view
                    .assign( p_position, Functions.minus )
                    .assign( Functions.div( Math.sqrt( Algebra.DEFAULT.norm2( l_view ) ) ) )
            )
                                .assign( Functions.mult( p_speed ) )
                                .assign( p_position, Functions.plus )
                                .assign( Math::round );
        }


        /**
         * returns the direction by an angle (in degree)
         *
         * @param p_angle angle in degree
         * @return direction
         */
        public static EDirection byAngle( final double p_angle )
        {
            return EDirection.values()[
                (int) (
                    p_angle < 0
                    ? 360 + p_angle
                    : p_angle
                ) / 45
                ];
        }

    }


}
