/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package org.commonmark.ext.trac.internal;

import org.commonmark.internal.inline.EmphasisDelimiterProcessor;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterRun;

/**
 * Trac/Wikipedia-style emphasis.
 */
public class TracEmphasisDelimiterProcessor extends EmphasisDelimiterProcessor {

    /**
     * Creates a {@link TracEmphasisDelimiterProcessor}.
     */
    public TracEmphasisDelimiterProcessor() {
        super('\'');
    }

    @Override
    public int getMinLength() {
        return 2;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        char delimiterChar = getOpeningCharacter();

        int usedDelimiters;
        Node emphasis;
        // calculate actual number of delimiters used from this closer
        if (openingRun.length() >= 3 && closingRun.length() >= 3) {
            usedDelimiters = 3;
            emphasis = new StrongEmphasis(String.valueOf(delimiterChar).repeat(usedDelimiters));
        } else {
            usedDelimiters = 2;
            emphasis = new Emphasis(String.valueOf(delimiterChar).repeat(usedDelimiters));
        }

        SourceSpans sourceSpans = SourceSpans.empty();
        sourceSpans.addAllFrom(openingRun.getOpeners(usedDelimiters));

        Text opener = openingRun.getOpener();
        for (Node node : Nodes.between(opener, closingRun.getCloser())) {
            emphasis.appendChild(node);
            sourceSpans.addAll(node.getSourceSpans());
        }

        sourceSpans.addAllFrom(closingRun.getClosers(usedDelimiters));

        emphasis.setSourceSpans(sourceSpans.getSourceSpans());
        opener.insertAfter(emphasis);

        return usedDelimiters;
    }

}
