package org.commonmark.ext.trac;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.internal.AutolinkPostProcessor;
import org.commonmark.ext.trac.internal.TracBlockQuoteParser;
import org.commonmark.ext.trac.internal.TracEmphasisDelimiterProcessor;
import org.commonmark.ext.trac.internal.TracHeadingParser;
import org.commonmark.ext.trac.internal.TracLinkPostProcessor;
import org.commonmark.parser.Parser;

/**
 * Extension for Trac wiki.
 * 
 * @see <a href="https://trac.edgewall.org/wiki/1.2/WikiFormatting">Trac WikiFormatting</a>
 */
public class TracExtension implements Parser.ParserExtension {

    private TracExtension() {
    }

    public static Extension create() {
        return new TracExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new TracHeadingParser.Factory());
        parserBuilder.customBlockParserFactory(new TracBlockQuoteParser.Factory());
        parserBuilder.customDelimiterProcessor(new TracEmphasisDelimiterProcessor());
        parserBuilder.postProcessor(new AutolinkPostProcessor());
        parserBuilder.postProcessor(new TracLinkPostProcessor());
    }

}
