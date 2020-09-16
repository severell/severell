package org.apache.maven.reporting;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.doxia.sink.render.RenderingContext;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The basis for a Maven report which can be generated both as part of a site generation or
 * as a direct standalone goal invocation.
 * Both invocations are delegated to <code>abstract executeReport( Locale )</code> from:
 * <ul> 
 * <li>Mojo's <code>execute()</code> method, see maven-plugin-api</li>
 * <li>MavenMultiPageReport's <code>generate( Sink, SinkFactory, Locale )</code>, see maven-reporting-api</li>
 * </ul>
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: AbstractMavenReport.java 1624794 2014-09-14 00:30:58Z hboutemy $
 * @since 2.0
 * @see #execute() <code>Mojo.execute()</code>, from maven-plugin-api 
 * @see #generate(Sink, SinkFactory, Locale) <code>MavenMultiPageReport.generate( Sink, SinkFactory, Locale )</code>,
 *  from maven-reporting-api
 * @see #executeReport(Locale) <code>abstract executeReport( Locale )</code>
 */
public abstract class AbstractMavenReport
    extends AbstractMojo
    implements MavenMultiPageReport
{
    /**
     * The output directory for the report. Note that this parameter is only evaluated if the goal is run directly from
     * the command line. If the goal is run indirectly as part of a site generation, the output directory configured in
     * the Maven Site Plugin is used instead.
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}", readonly = true, required = true )
    protected File outputDirectory;

    /**
     * The Maven Project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

    /**
     * Specifies the input encoding.
     */
    @Parameter( property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true )
    private String inputEncoding;

    /**
     * Specifies the output encoding.
     */
    @Parameter( property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}", readonly = true )
    private String outputEncoding;

    /**
     * Doxia Site Renderer component.
     */
    @Component
    protected Renderer siteRenderer;

    /** The current sink to use */
    private Sink sink;

    /** The sink factory to use */
    private SinkFactory sinkFactory;

    /** The current report output directory to use */
    private File reportOutputDirectory;

    /**
     * This method is called when the report generation is invoked directly as a standalone Mojo.
     *
     * @throws MojoExecutionException if an error occurs when generating the report
     * @see org.apache.maven.plugins.site.ReportDocumentRender
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( !canGenerateReport() )
        {
            return;
        }

        Writer writer = null;
        try
        {
            File outputDirectory = new File( getOutputDirectory() );

            String filename = getOutputName() + ".html";

            Locale locale = Locale.getDefault();

            SiteRenderingContext siteContext = new SiteRenderingContext();
            siteContext.setDecoration( new DecorationModel() );
            siteContext.setTemplateName( "org/apache/maven/doxia/siterenderer/resources/default-site.vm" );
            siteContext.setLocale( locale );
            siteContext.setTemplateProperties( getTemplateProperties() );

            RenderingContext context = new RenderingContext( outputDirectory, filename );

            SiteRendererSink sink = new SiteRendererSink( context );

            generate( sink, null, locale );

            if ( !isExternalReport() ) // MSHARED-204: only render Doxia sink if not an external report
            {
                outputDirectory.mkdirs();

                writer =
                    new OutputStreamWriter( new FileOutputStream( new File( outputDirectory, filename ) ),
                                            getOutputEncoding() );

                getSiteRenderer().generateDocument( writer, sink, siteContext );

                //getSiteRenderer().copyResources( siteContext, new File( project.getBasedir(), "src/site/resources" ),
                //                            outputDirectory );
            }
        }
        catch ( RendererException e )
        {
            throw new MojoExecutionException(
                "An error has occurred in " + getName( Locale.ENGLISH ) + " report generation.", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "An error has occurred in " + getName( Locale.ENGLISH ) + " report generation.", e );
        }
        catch ( MavenReportException e )
        {
            throw new MojoExecutionException(
                "An error has occurred in " + getName( Locale.ENGLISH ) + " report generation.", e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    /**
     * create template properties like done in maven-site-plugin's
     * <code>AbstractSiteRenderingMojo.createSiteRenderingContext( Locale )</code>
     * @return properties
     */
    private Map<String, Object> getTemplateProperties()
    {
        Map<String, Object> templateProperties = new HashMap<String, Object>();
        templateProperties.put( "project", getProject() );
        templateProperties.put( "inputEncoding", getInputEncoding() );
        templateProperties.put( "outputEncoding", getOutputEncoding() );
        // Put any of the properties in directly into the Velocity context
        for ( Map.Entry<Object, Object> entry : getProject().getProperties().entrySet() )
        {
            templateProperties.put( (String) entry.getKey(), entry.getValue() );
        }
        return templateProperties;
    }

    /**
     * Generate a report.
     *
     * @param aSink the sink to use for the generation.
     * @param aLocale the wanted locale to generate the report, could be null.
     * @throws MavenReportException if any
     * @deprecated use {@link #generate(Sink, SinkFactory, Locale)} instead.
     */
    public void generate( org.codehaus.doxia.sink.Sink aSink, Locale aLocale )
        throws MavenReportException
    {
        generate( aSink, null, aLocale );
    }

    /**
     * Generate a report.
     *
     * @param aSink
     * @param aLocale
     * @throws MavenReportException
     * @see org.apache.maven.reporting.MavenReport#generate(org.apache.maven.doxia.sink.Sink, java.util.Locale)
     * @deprecated use {@link #generate(Sink, SinkFactory, Locale)} instead.
     */
    public void generate( Sink aSink, Locale aLocale )
        throws MavenReportException
    {
        generate( aSink, null, aLocale );
    }

    /**
     * This method is called when the report generation is invoked by maven-site-plugin.
     *
     * @param aSink
     * @param aSinkFactory
     * @param aLocale
     * @throws MavenReportException
     */
    public void generate( Sink aSink, SinkFactory aSinkFactory, Locale aLocale )
        throws MavenReportException
    {
        if ( aSink == null )
        {
            throw new MavenReportException( "You must specify a sink." );
        }

        if ( !canGenerateReport() )
        {
            getLog().info( "This report cannot be generated as part of the current build. "
                           + "The report name should be referenced in this line of output." );
            return;
        }

        this.sink = aSink;

        this.sinkFactory = aSinkFactory;

        executeReport( aLocale );

        closeReport();
    }

    /**
     * {@inheritDoc}
     * @return CATEGORY_PROJECT_REPORTS
     */
    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    /** {@inheritDoc} */
    public File getReportOutputDirectory()
    {
        if ( reportOutputDirectory == null )
        {
            reportOutputDirectory = new File( getOutputDirectory() );
        }

        return reportOutputDirectory;
    }

    /** {@inheritDoc} */
    public void setReportOutputDirectory( File reportOutputDirectory )
    {
        this.reportOutputDirectory = reportOutputDirectory;
        this.outputDirectory = reportOutputDirectory;
    }

    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * Gets the input files encoding.
     *
     * @return The input files encoding, never <code>null</code>.
     */
    protected String getInputEncoding()
    {
        return ( inputEncoding == null ) ? ReaderFactory.ISO_8859_1 : inputEncoding;
    }

    /**
     * Gets the effective reporting output files encoding.
     *
     * @return The effective reporting output file encoding, never <code>null</code>.
     */
    protected String getOutputEncoding()
    {
        return ( outputEncoding == null ) ? ReaderFactory.UTF_8 : outputEncoding;
    }

    /**
     * Actions when closing the report.
     */
    protected void closeReport()
    {
        getSink().close();
    }

    /**
     * @return the sink used
     */
    public Sink getSink()
    {
        return sink;
    }

    /**
     * @return the sink factory used
     */
    public SinkFactory getSinkFactory()
    {
        return sinkFactory;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#isExternalReport()
     * @return <tt>false</tt> by default.
     */
    public boolean isExternalReport()
    {
        return false;
    }

    /** {@inheritDoc} */
    public boolean canGenerateReport()
    {
        return true;
    }

    /**
     * Execute the generation of the report.
     *
     * @param locale the wanted locale to return the report's description, could be <code>null</code>.
     * @throws MavenReportException if any
     */
    protected abstract void executeReport( Locale locale )
        throws MavenReportException;
}
