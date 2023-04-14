package org.commonmark.ext.trac;

import java.util.Collections;
import java.util.Set;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.internal.AutolinkPostProcessor;
import org.commonmark.ext.trac.internal.TracBlockQuoteParser;
import org.commonmark.ext.trac.internal.TracCodeBlockParser;
import org.commonmark.ext.trac.internal.TracEmphasisDelimiterProcessor;
import org.commonmark.ext.trac.internal.TracHeadingParser;
import org.commonmark.ext.trac.internal.TracLinkPostProcessor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer.Builder;
import org.commonmark.renderer.html.HtmlRenderer.HtmlRendererExtension;

/**
 * Extension for Trac wiki.
 * 
 * @see <a href="https://trac.edgewall.org/wiki/1.2/WikiFormatting">Trac WikiFormatting</a>
 */
public class TracExtension implements Parser.ParserExtension, HtmlRendererExtension {

    private TracExtension() {
    }

    public static Extension create() {
        return new TracExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new TracHeadingParser.Factory());
        parserBuilder.customBlockParserFactory(new TracCodeBlockParser.Factory());
        parserBuilder.customBlockParserFactory(new TracBlockQuoteParser.Factory());
        parserBuilder.customDelimiterProcessor(new TracEmphasisDelimiterProcessor());
        parserBuilder.postProcessor(new AutolinkPostProcessor());
        parserBuilder.postProcessor(new TracLinkPostProcessor());
    }
    
    @Override
    public void extend(Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new CommentRendererFactory());
    }
    
    static class CommentRendererFactory implements HtmlNodeRendererFactory {
        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            final Set<Class<? extends Node>> types = Collections.<Class<? extends Node>>singleton(FencedCodeBlock.class);
            final CoreHtmlNodeRenderer parent = new CoreHtmlNodeRenderer(context);
            
            return new NodeRenderer() {
                @Override
                public Set<Class<? extends Node>> getNodeTypes() {
                    return types;
                }

                @Override
                public void render(Node node) {
                    FencedCodeBlock codeBlock = (FencedCodeBlock) node;
                    if ("comment".equals(codeBlock.getInfo())) {
                        // Skip.
                        return;
                    }
                    
                    parent.visit(codeBlock);
                }
            };
        }
    }
    
}
