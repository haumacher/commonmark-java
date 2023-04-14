package org.commonmark.ext.trac.internal;

import static org.commonmark.internal.util.Escaping.*;

import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

public class TracCodeBlockParser extends AbstractBlockParser {

    private final FencedCodeBlock block = new FencedCodeBlock();

    private String _syntax;
    private StringBuilder code = new StringBuilder();
    
    /** 
     * Creates a {@link TracCodeBlockParser}.
     */
    public TracCodeBlockParser(int fenceIndent) {
        block.setFenceChar('{');
        block.setFenceLength(3);
        block.setFenceIndent(fenceIndent);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        CharSequence line = state.getLine().getContent();
        if (isClosing(line, nextNonSpace)) {
            return BlockContinue.finished();
        } else {
            return BlockContinue.atIndex(state.getIndex());
        }
    }

    private boolean isClosing(CharSequence line, int index) {
        return startsWith(line, index, "}}}");
    }

    @Override
    public void addLine(SourceLine line) {
        CharSequence content = line.getContent();
        if (_syntax == null) {
            String first = content.toString();
            int start = skipWhiteSpace(content, 0);
            
            String syntax = parseSyntax(first, start);
            if (syntax == null) {
                _syntax = "";
                if (!first.isEmpty()) {
                    appendCodeLine(first);
                }
            } else {
                _syntax = syntax;
            }
        } else {
            appendCodeLine(content);
        }
    }

    private void appendCodeLine(CharSequence content) {
        code.append(content);
        code.append('\n');
    }

    @Override
    public void closeBlock() {
        // first line becomes info string
        block.setInfo(unescapeString(_syntax.trim()));
        block.setLiteral(code.toString());
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int indent = state.getIndent();
            
            int nextNonSpace = state.getNextNonSpaceIndex();
            TracCodeBlockParser blockParser = checkOpener(state.getLine().getContent(), nextNonSpace, indent);
            if (blockParser != null) {
                return BlockStart.of(blockParser).atIndex(nextNonSpace + blockParser.block.getFenceLength());
            } else {
                return BlockStart.none();
            }
        }
    }

    // Spec: A code fence is the sequence "{{{" followed by an optional syntax hint #!<grammar-name> that may either directly follow the fence or occur on the next line.
    static TracCodeBlockParser checkOpener(CharSequence line, int index, int indent) {
        if (!startsWith(line, index, "{{{")) {
            return null;
        }
        
        return new TracCodeBlockParser(indent);
    }

    private static String parseSyntax(CharSequence line, int start) {
        if (startsWith(line, start, "#!")) {
            int length = line.length();
            int end = start = start + "#!".length();
            while (end < length && !Character.isWhitespace(line.charAt(end))) {
                end++;
            }
            if (end == start) {
                // Invalid syntax.
                return null;
            }
            
            return line.subSequence(start, end).toString();
        } else {
            // Invalid syntax.
            return null;
        }
    }

    private static int skipWhiteSpace(CharSequence line, int start) {
        int length = line.length();
        while (start < length && Character.isWhitespace(line.charAt(start))) {
            start++;
        }
        return start;
    }

    /** 
     * Whether the given line start with the given substring at the given index.
     */
    private static boolean startsWith(CharSequence line, int start, String substring) {
        assert start <= line.length();
        
        int charsLeft = line.length() - start;
        int cnt = substring.length();
        
        if (charsLeft < cnt) {
            return false;
        }
        
        for (int i = 0; i < cnt; i++) {
            if (line.charAt(start + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
