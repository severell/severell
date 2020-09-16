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

import org.apache.commons.validator.EmailValidator;
import org.apache.commons.validator.UrlValidator;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.util.HtmlTools;

import org.apache.maven.shared.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * An abstract class to manage report generation, with many helper methods to ease the job: you just need to
 * implement getTitle() and renderBody().
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id: AbstractMavenReportRenderer.java 1379502 2012-08-31 16:43:35Z struberg $
 * @since 2.0
 * @TODO Later it may be appropriate to create something like a VelocityMavenReportRenderer
 * that could take a velocity template and pipe that through Doxia rather than coding them
 * up like this.
 * @see #getTitle()
 * @see #renderBody()
 */
public abstract class AbstractMavenReportRenderer
    implements MavenReportRenderer
{
    /** The current sink to use */
    protected Sink sink;

    /** The current section number */
    private int section;

    /**
     * Default constructor.
     *
     * @param sink the sink to use.
     */
    public AbstractMavenReportRenderer( Sink sink )
    {
        this.sink = sink;
    }

    /** {@inheritDoc} */
    public void render()
    {
        sink.head();

        sink.title();
        text( getTitle() );
        sink.title_();

        sink.head_();

        sink.body();
        renderBody();
        sink.body_();

        sink.flush();

        sink.close();
    }

    // ----------------------------------------------------------------------
    // Section handler
    // ----------------------------------------------------------------------

    /**
     * Convenience method to wrap section creation in the current sink. An anchor will be add for the name.
     *
     * @param name the name of this section, could be null.
     * @see #text(String)
     * @see Sink#section1()
     * @see Sink#sectionTitle1()
     * @see Sink#sectionTitle1_()
     * @see Sink#section2()
     * @see Sink#sectionTitle2()
     * @see Sink#sectionTitle2_()
     * @see Sink#section3()
     * @see Sink#sectionTitle3()
     * @see Sink#sectionTitle3_()
     * @see Sink#section4()
     * @see Sink#sectionTitle4()
     * @see Sink#sectionTitle4_()
     * @see Sink#section5()
     * @see Sink#sectionTitle5()
     * @see Sink#sectionTitle5_()
     */
    protected void startSection( String name )
    {
        section = section + 1;

        switch ( section )
        {
            case 1:
                sink.section1();
                sink.sectionTitle1();
                break;
            case 2:
                sink.section2();
                sink.sectionTitle2();
                break;
            case 3:
                sink.section3();
                sink.sectionTitle3();
                break;
            case 4:
                sink.section4();
                sink.sectionTitle4();
                break;
            case 5:
                sink.section5();
                sink.sectionTitle5();
                break;

            default:
                // TODO: warning - just don't start a section
                break;
        }

        text( name );

        switch ( section )
        {
            case 1:
                sink.sectionTitle1_();
                break;
            case 2:
                sink.sectionTitle2_();
                break;
            case 3:
                sink.sectionTitle3_();
                break;
            case 4:
                sink.sectionTitle4_();
                break;
            case 5:
                sink.sectionTitle5_();
                break;

            default:
                // TODO: warning - just don't start a section
                break;
        }

        sink.anchor( HtmlTools.encodeId( name ) );
        sink.anchor_();
    }

    /**
     * Convenience method to wrap section ending in the current sink.
     *
     * @see Sink#section1_()
     * @see Sink#section2_()
     * @see Sink#section3_()
     * @see Sink#section4_()
     * @see Sink#section5_()
     * @IllegalStateException if too many closing sections.
     */
    protected void endSection()
    {
        switch ( section )
        {
            case 1:
                sink.section1_();
                break;
            case 2:
                sink.section2_();
                break;
            case 3:
                sink.section3_();
                break;
            case 4:
                sink.section4_();
                break;
            case 5:
                sink.section5_();
                break;

            default:
                // TODO: warning - just don't start a section
                break;
        }

        section = section - 1;

        if ( section < 0 )
        {
            throw new IllegalStateException( "Too many closing sections" );
        }
    }

    // ----------------------------------------------------------------------
    // Table handler
    // ----------------------------------------------------------------------

    /**
     * Convenience method to wrap the table start in the current sink.
     *
     * @see Sink#table()
     */
    protected void startTable()
    {
        startTable( new int[] {Sink.JUSTIFY_LEFT}, false );
    }

    /**
     * Convenience method to wrap the table start in the current sink.
     *
     * @param justification the justification of table cells.
     * @param grid whether to draw a grid around cells.
     *
     * @see Sink#table()
     * @see Sink#tableRows(int[],boolean)
     * @since 2.1
     */
    protected void startTable( int[] justification, boolean grid )
    {
        sink.table();
        sink.tableRows( justification, grid );
    }

    /**
     * Convenience method to wrap the table ending in the current sink.
     *
     * @see Sink#table_()
     */
    protected void endTable()
    {
        sink.tableRows_();
        sink.table_();
    }

    /**
     * Convenience method to wrap the table header cell start in the current sink.
     *
     * @param text the text to put in this cell, could be null.
     * @see #text(String)
     * @see Sink#tableHeaderCell()
     * @see Sink#tableHeaderCell_()
     */
    protected void tableHeaderCell( String text )
    {
        sink.tableHeaderCell();

        text( text );

        sink.tableHeaderCell_();
    }

    /**
     * Convenience method to wrap a table cell start in the current sink.
     * <p>The text could be a link patterned text defined by <code>{text, url}</code></p>
     *
     * @param text the text to put in this cell, could be null.
     * @see #linkPatternedText(String)
     * @see #tableCell(String)
     */
    protected void tableCell( String text )
    {
        tableCell( text, false );
    }

    /**
     * Convenience method to wrap a table cell start in the current sink.
     * <p>The text could be a link patterned text defined by <code>{text, url}</code></p>
     * <p>If <code>asHtml</code> is true, add the text as Html</p>
     *
     * @param text the text to put in this cell, could be null.
     * @param asHtml <tt>true</tt> to add the text as Html, <tt>false</tt> otherwise.
     * @see #linkPatternedText(String)
     * @see Sink#tableCell()
     * @see Sink#tableCell_()
     * @see Sink#rawText(String)
     */
    protected void tableCell( String text, boolean asHtml )
    {
        sink.tableCell();

        if ( asHtml )
        {
            sink.rawText( text );
        }
        else
        {
            linkPatternedText( text );
        }

        sink.tableCell_();
    }

    /**
     * Convenience method to wrap a table row start in the current sink.
     * <p>The texts in the <code>content</code> could be link patterned texts defined by <code>{text, url}</code></p>
     *
     * @param content an array of text to put in the cells in this row, could be null.
     * @see #tableCell(String)
     * @see Sink#tableRow()
     * @see Sink#tableRow_()
     */
    protected void tableRow( String[] content )
    {
        sink.tableRow();

        if ( content != null )
        {
            for ( int i = 0; i < content.length; i++ )
            {
                tableCell( content[i] );
            }
        }

        sink.tableRow_();
    }

    /**
     * Convenience method to wrap a table header row start in the current sink.
     * <p>The texts in the <code>content</code> could be link patterned texts defined by <code>{text, url}</code></p>
     *
     * @param content an array of text to put in the cells in this row header, could be null.
     * @see #tableHeaderCell(String)
     * @see Sink#tableRow()
     * @see Sink#tableRow_()
     */
    protected void tableHeader( String[] content )
    {
        sink.tableRow();

        if ( content != null )
        {
            for ( int i = 0; i < content.length; i++ )
            {
                tableHeaderCell( content[i] );
            }
        }

        sink.tableRow_();
    }

    /**
     * Convenience method to wrap a table caption in the current sink.
     *
     * @param caption the caption of the table, could be null.
     * @see #text(String)
     * @see Sink#tableCaption()
     * @see Sink#tableCaption_()
     */
    protected void tableCaption( String caption )
    {
        sink.tableCaption();

        text( caption );

        sink.tableCaption_();
    }

    // ----------------------------------------------------------------------
    // Paragraph handler
    // ----------------------------------------------------------------------

    /**
     * Convenience method to wrap a paragraph in the current sink.
     *
     * @param paragraph the paragraph to add, could be null.
     * @see #text(String)
     * @see Sink#paragraph()
     * @see Sink#paragraph_()
     */
    protected void paragraph( String paragraph )
    {
        sink.paragraph();

        text( paragraph );

        sink.paragraph_();
    }

    /**
     * Convenience method to wrap a link in the current sink.
     *
     * @param href the link to add, cannot be null.
     * @param name the link name.
     * @see #text(String)
     * @see Sink#link(String)
     * @see Sink#link_()
     */
    protected void link( String href, String name )
    {
        sink.link( href );

        text( name );

        sink.link_();
    }

    /**
     * Convenience method to wrap a text in the current sink.
     * <p>If text is empty or has a <code>null</code> value, add the <code>"-"</code> charater</p>
     *
     * @param text a text, could be null.
     * @see Sink#text(String)
     */
    protected void text( String text )
    {
        if ( StringUtils.isEmpty( text ) ) // Take care of spaces
        {
            sink.text( "-" );
        }
        else
        {
            sink.text( text );
        }
    }

    /**
     * Convenience method to wrap a text as verbatim style in the current sink .
     *
     * @param text a text, could be null.
     * @see #text(String)
     * @see Sink#verbatim(boolean)
     * @see Sink#verbatim_()
     */
    protected void verbatimText( String text )
    {
        sink.verbatim( true );

        text( text );

        sink.verbatim_();
    }

    /**
     * Convenience method to wrap a text with a given link href as verbatim style in the current sink.
     *
     * @param text a string
     * @param href an href could be null
     * @see #link(String, String)
     * @see #verbatimText(String)
     * @see Sink#verbatim(boolean)
     * @see Sink#verbatim_()
     */
    protected void verbatimLink( String text, String href )
    {
        if ( StringUtils.isEmpty( href ) )
        {
            verbatimText( text );
        }
        else
        {
            sink.verbatim( true );

            link( href, text );

            sink.verbatim_();
        }
    }

    /**
     * Convenience method to add a Javascript code in the current sink.
     *
     * @param jsCode a string of Javascript
     * @see Sink#rawText(String)
     */
    protected void javaScript( String jsCode )
    {
        sink.rawText( "<script type=\"text/javascript\">\n" + jsCode + "</script>" );
    }

    /**
     * Convenience method to wrap a patterned text in the current link.
     * <p>The text variable should contained this given pattern <code>{text, url}</code>
     * to handle the link creation.</p>
     *
     * @param text a text with link pattern defined.
     * @see #text(String)
     * @see #link(String, String)
     * @see #applyPattern(String)
     */
    public void linkPatternedText( String text )
    {
        if ( StringUtils.isEmpty( text ) )
        {
            text( text );
        }
        else
        {
            List<String> segments = applyPattern( text );

            if ( segments == null )
            {
                text( text );
            }
            else
            {
                for ( Iterator<String> it = segments.iterator(); it.hasNext(); )
                {
                    String name = it.next();
                    String href = it.next();

                    if ( href == null )
                    {
                        text( name );
                    }
                    else
                    {
                        if ( getValidHref( href ) != null )
                        {
                            link( getValidHref( href ), name );
                        }
                        else
                        {
                            text( href );
                        }
                    }
                }
            }
        }
    }

    /**
     * Create a link pattern text defined by <code>{text, url}</code>.
     * <p>This created pattern could be used by the method <code>linkPatternedText(String)</code> to
     * handle a text with link.</p>
     *
     * @param text
     * @param href
     * @return a link pattern
     * @see #linkPatternedText(String)
     */
    protected static String createLinkPatternedText( String text, String href )
    {
        if ( text == null )
        {
            return text;
        }

        if ( href == null )
        {
            return text;
        }

        return '{' + text + ", " + href + '}';
    }

    /**
     * Convenience method to display a <code>Properties</code> object as comma separated String.
     *
     * @param props the properties to display.
     * @return the properties object as comma separated String
     */
    protected static String propertiesToString( Properties props )
    {
        if ( props == null || props.isEmpty() )
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for ( Map.Entry<?, ?> entry : props.entrySet() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ", " );
            }

            sb.append( entry.getKey() ).append( "=" ).append( entry.getValue() );
        }

        return sb.toString();
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Return a valid href.
     * <p>A valid href could start by <code>mailto:</code>.</p>
     * <p>For a relative path, the href should start by <code>./</code> to be valid.</p>
     *
     * @param href an href, could be null.
     * @return a valid href or <code>null</code> if the href is null or not valid.
     */
    private static String getValidHref( String href )
    {
        if ( StringUtils.isEmpty( href ) )
        {
            return null;
        }

        href = href.trim();

        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator( schemes );
        EmailValidator emailValidator = EmailValidator.getInstance();

        if ( emailValidator.isValid( href )
            || ( href.contains( "?" ) && emailValidator.isValid( href.substring( 0, href.indexOf( "?" ) ) ) ) )
        {
            return "mailto:" + href;
        }
        else if ( href.toLowerCase().startsWith( "mailto:" ) )
        {
            return href;
        }
        else if ( urlValidator.isValid( href ) )
        {
            return href;
        }
        else
        {
            String hrefTmp;
            if ( !href.endsWith( "/" ) )
            {
                hrefTmp = href + "/index.html";
            }
            else
            {
                hrefTmp = href + "index.html";
            }

            if ( urlValidator.isValid( hrefTmp ) )
            {
                return href;
            }

            if ( href.startsWith( "./" ) )
            {
                if ( href.length() > 2 )
                {
                    return href.substring( 2, href.length() );
                }

                return ".";
            }

            return null;
        }
    }

    /**
     * The method parses a text and applies the given pattern <code>{text, url}</code> to create
     * a list of text/href.
     *
     * @param text a text with or without the pattern <code>{text, url}</code>
     * @return a map of text/href
     */
    private static List<String> applyPattern( String text )
    {
        if ( StringUtils.isEmpty( text ) )
        {
            return null;
        }

        // Map defined by key/value name/href
        // If href == null, it means
        List<String> segments = new ArrayList<String>();

        // TODO Special case http://jira.codehaus.org/browse/MEV-40
        if ( text.indexOf( "${" ) != -1 )
        {
            int lastComma = text.lastIndexOf( "," );
            int lastSemi = text.lastIndexOf( "}" );
            if ( lastComma != -1 && lastSemi != -1 && lastComma < lastSemi )
            {
                segments.add( text.substring( lastComma + 1, lastSemi ).trim() );
                segments.add( null );
            }
            else
            {
                segments.add( text );
                segments.add( null );
            }

            return segments;
        }

        boolean inQuote = false;
        int braceStack = 0;
        int lastOffset = 0;

        for ( int i = 0; i < text.length(); i++ )
        {
            char ch = text.charAt( i );

            if ( ch == '\'' && !inQuote && braceStack == 0 )
            {
                // handle: ''
                if ( i + 1 < text.length() && text.charAt( i + 1 ) == '\'' )
                {
                    i++;
                    segments.add( text.substring( lastOffset, i ) );
                    segments.add( null );
                    lastOffset = i + 1;
                }
                else
                {
                    inQuote = true;
                }
            }
            else
            {
                switch ( ch )
                {
                    case '{':
                        if ( !inQuote )
                        {
                            if ( braceStack == 0 )
                            {
                                if ( i != 0 ) // handle { at first character
                                {
                                    segments.add( text.substring( lastOffset, i ) );
                                    segments.add( null );
                                }
                                lastOffset = i + 1;
                            }
                            braceStack++;
                        }
                        break;
                    case '}':
                        if ( !inQuote )
                        {
                            braceStack--;
                            if ( braceStack == 0 )
                            {
                                String subString = text.substring( lastOffset, i );
                                lastOffset = i + 1;

                                int lastComma = subString.lastIndexOf( "," );
                                if ( lastComma != -1 )
                                {
                                    segments.add( subString.substring( 0, lastComma ).trim() );
                                    segments.add( subString.substring( lastComma + 1 ).trim() );
                                }
                                else
                                {
                                    segments.add( subString );
                                    segments.add( null );
                                }
                            }
                        }
                        break;
                    case '\'':
                        inQuote = false;
                        break;
                    default:
                        break;
                }
            }
        }

        if ( !StringUtils.isEmpty( text.substring( lastOffset ) ) )
        {
            segments.add( text.substring( lastOffset ) );
            segments.add( null );
        }

        if ( braceStack != 0 )
        {
            throw new IllegalArgumentException( "Unmatched braces in the pattern." );
        }

        if ( inQuote )
        {
            //throw new IllegalArgumentException( "Unmatched quote in the pattern." );
            //TODO: warning...
        }

        return Collections.unmodifiableList( segments );
    }

    // ----------------------------------------------------------------------
    // Abstract methods
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public abstract String getTitle();

    /**
     * Renderer the body content of the report.
     */
    protected abstract void renderBody();
}
