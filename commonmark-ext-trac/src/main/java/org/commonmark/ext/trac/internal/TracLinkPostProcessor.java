package org.commonmark.ext.trac.internal;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

/**
 * {@link PostProcessor} matching ticket:<num> and wikid:<page-name> syntax.
 */
public class TracLinkPostProcessor implements PostProcessor {

    private static final String QUOT = "\"";

    static Pattern LINK_PATTERN = Pattern.compile(
            "((?:wiki|ticket):)" + "(?:(?:" + QUOT + "([^\"]+)" + QUOT + ")" + "|" + "([^\\s\\.\\,\\;\\\"]+))");

    String x = "\\[[0-9]+\\]";
    String y = "\\{[0-9]+\\}";
    String z = "#([0-9]+)\\b";

    @Override
    public Node process(Node node) {
        TracLinkVisitor autolinkVisitor = new TracLinkVisitor();
        node.accept(autolinkVisitor);
        return node;
    }

    final class TracLinkVisitor extends AbstractVisitor {
        int inLink = 0;

        @Override
        public void visit(Link link) {
            inLink++;
            super.visit(link);
            inLink--;
        }

        @Override
        public void visit(Text text) {
            if (inLink == 0) {
                linkify(text);
            }
        }

        private void linkify(Text originalTextNode) {
            String literal = originalTextNode.getLiteral();

            Matcher matcher = LINK_PATTERN.matcher(literal);

            Node lastNode = originalTextNode;
            List<SourceSpan> sourceSpans = originalTextNode.getSourceSpans();
            SourceSpan sourceSpan = sourceSpans.size() == 1 ? sourceSpans.get(0) : null;

            int startPos = 0;
            while (matcher.find(startPos)) {
                int matchPos = matcher.start();
                if (matchPos > startPos) {
                    lastNode = appendLiteralText(lastNode, literal, sourceSpan, startPos, matchPos);
                }

                boolean ticket = matcher.group(1).equals("ticket:");
                Text linkText = (matcher.group(2) != null)
                        ? createTextNode(literal, sourceSpan, matcher.start(2), matcher.end(2), ticket)
                        : createTextNode(literal, sourceSpan, matcher.start(3), matcher.end(3), ticket);

                String destination = matcher.group(1)
                        + (matcher.group(2) != null ? matcher.group(2) : matcher.group(3));
                Link linkNode = new Link(destination, null);
                linkNode.appendChild(linkText);
                linkNode.setSourceSpans(linkText.getSourceSpans());

                lastNode = insertNode(linkNode, lastNode);

                startPos = matcher.end();
            }

            if (startPos < literal.length()) {
                lastNode = appendLiteralText(lastNode, literal, sourceSpan, startPos, literal.length());
            }

            // Original node no longer needed
            originalTextNode.unlink();
        }

        private Node appendLiteralText(Node lastNode, String literal,
                SourceSpan sourceSpan, int startPos, int endPos) {
            Text textBefore = createTextNode(literal, sourceSpan, startPos, endPos, false);
            lastNode = insertNode(textBefore, lastNode);
            return lastNode;
        }

        private Text createTextNode(String literal, SourceSpan sourceSpan,
                int beginIndex, int endIndex, boolean ticket) {
            String text = literal.substring(beginIndex, endIndex);
            if (ticket) {
                if (text.matches("[0-9]+")) {
                    text = "#" + text;
                } else {
                    text = "ticket:" + text;
                }
            }
            Text textNode = new Text(text);
            if (sourceSpan != null) {
                int length = endIndex - beginIndex;
                textNode.addSourceSpan(SourceSpan.of(sourceSpan.getLineIndex(), beginIndex, length));
            }
            return textNode;
        }

        private Node insertNode(Node node, Node insertAfterNode) {
            insertAfterNode.insertAfter(node);
            return node;
        }
    }
}
