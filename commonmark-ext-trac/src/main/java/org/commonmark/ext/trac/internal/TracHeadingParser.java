package org.commonmark.ext.trac.internal;

import org.commonmark.internal.inline.Position;
import org.commonmark.internal.inline.Scanner;
import org.commonmark.node.Block;
import org.commonmark.node.Heading;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

public class TracHeadingParser extends AbstractBlockParser {

    private final Heading block = new Heading();
    private final SourceLines content;

    public TracHeadingParser(int level, SourceLines content) {
        block.setLevel(level);
        this.content = content;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        return BlockContinue.none();
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        inlineParser.parse(content, block);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            SourceLine line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence content = line.getContent();
            int length = content.length();
            if (nextNonSpace < length && content.charAt(nextNonSpace) == '=') {
                TracHeadingParser tracHeading = getTracHeading(line.substring(nextNonSpace, length));
                if (tracHeading != null) {
                    return BlockStart.of(tracHeading).atIndex(length);
                }
            }

            return BlockStart.none();
        }
    }

    private static TracHeadingParser getTracHeading(SourceLine line) {
        Scanner scanner = Scanner.of(SourceLines.of(line));
        int level = scanner.matchMultiple('=');
        if (level == 0 || level > 6) {
            return null;
        }

        if (!scanner.hasNext()) {
            return null;
        }

        char next = scanner.peek();
        if (!(next == ' ' || next == '\t')) {
            return null;
        }

        scanner.whitespace();
        Position start = scanner.position();
        Position end = start;
        boolean eqCanEnd = true;

        while (scanner.hasNext()) {
            char c = scanner.peek();
            switch (c) {
                case '=':
                    if (eqCanEnd) {
                        scanner.matchMultiple('=');
                        int whitespace = scanner.whitespace();
                        // If there's other characters, the hashes and spaces were part of the heading
                        if (scanner.hasNext()) {
                            end = scanner.position();
                        }
                        eqCanEnd = whitespace > 0;
                    } else {
                        scanner.next();
                        end = scanner.position();
                    }
                    break;
                case ' ':
                case '\t':
                    eqCanEnd = true;
                    scanner.next();
                    break;
                default:
                    eqCanEnd = false;
                    scanner.next();
                    end = scanner.position();
            }
        }

        SourceLines source = scanner.getSource(start, end);
        String content = source.getContent();
        if (content.isEmpty()) {
            return new TracHeadingParser(level, SourceLines.empty());
        }
        return new TracHeadingParser(level, source);
    }

}
