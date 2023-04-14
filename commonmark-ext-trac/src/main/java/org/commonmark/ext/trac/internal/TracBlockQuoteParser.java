package org.commonmark.ext.trac.internal;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

/**
 * {@link AbstractBlockParser} that interprets indented text as block quote.
 */
public class TracBlockQuoteParser extends AbstractBlockParser {

    private final BlockQuote block = new BlockQuote();
    private int _blockColumn;

    /**
     * Creates a {@link TracBlockQuoteParser}.
     *
     * @param blockColumn The indentation column of the currently parsed block.
     */
    public TracBlockQuoteParser(int blockColumn) {
        _blockColumn = blockColumn;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public BlockQuote getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (isMarker(state) && state.getColumn() + state.getIndent() >= _blockColumn) {
            return BlockContinue.atColumn(_blockColumn);
        } else {
            return BlockContinue.none();
        }
    }

    static boolean isMarker(ParserState state) {
        int index = state.getNextNonSpaceIndex();
        CharSequence line = state.getLine().getContent();
        return state.getIndent() > 1 && index < line.length();
    }

    public static class Factory extends AbstractBlockParserFactory {
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (isMarker(state)) {
                int newColumn = state.getColumn() + state.getIndent();
                return BlockStart.of(new TracBlockQuoteParser(newColumn)).atColumn(newColumn);
            } else {
                return BlockStart.none();
            }
        }
    }
}
