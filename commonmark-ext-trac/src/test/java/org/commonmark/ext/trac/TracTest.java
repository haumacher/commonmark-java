/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package org.commonmark.ext.trac;

import java.util.Collections;
import java.util.Set;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

/**
 * Test for Trac wiki parsing.
 */
public class TracTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(TracExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testHeading() {
        assertRendering("= Heading 1 =", "<h1>Heading 1</h1>\n");
        assertRendering("== Heading 2 ==", "<h2>Heading 2</h2>\n");
        assertRendering("====== Heading 6 ======", "<h6>Heading 6</h6>\n");
        assertRendering("= Heading 1", "<h1>Heading 1</h1>\n");
        assertRendering("== Heading 2 ", "<h2>Heading 2</h2>\n");
        assertRendering("                 == Heading 2 ", "<h2>Heading 2</h2>\n");
        assertRendering("==Not a heading", "<p>==Not a heading</p>\n");
        assertRendering("======= Not a heading", "<p>======= Not a heading</p>\n");
    }

    @Test
    public void testBold() {
        assertRendering("'''bold'''", "<p><strong>bold</strong></p>\n");
    }

    @Test
    public void testItalic() {
        assertRendering("''italic''", "<p><em>italic</em></p>\n");
    }

    @Test
    public void testBoldItalic() {
        assertRendering("'''''Wikipedia style'''''", "<p><em><strong>Wikipedia style</strong></em></p>\n");
    }

    @Test
    public void testWikiCreoleBold() {
        assertRendering("**bold**", "<p><strong>bold</strong></p>\n");
    }

    @Test
    public void testWikiCreoleItalic() {
        assertRendering("//italic//", "<p><em>italic</em></p>");
    }

    @Test
    public void testWikiCreoleBoldItalic() {
        assertRendering("**//!WikiCreole style//**", "<p><em><strong>WikiCreole style</strong></em></p>\n");
    }

    @Test
    public void testMonospaced() {
        assertRendering("`monospaced (''other markup ignored'')`", "");
    }

    @Test
    public void testParagraph() {
        assertRendering("First paragraph\n" +
                "on multiple lines.\n" +
                "\n" +
                "Second paragraph.",

                "<p>First paragraph\n" +
                        "on multiple lines.</p>\n" +
                        "<p>Second paragraph.</p>\n");
    }

    @Test
    public void testBulletList() {
        assertRendering(
                "* bullet list\n" +
                        "  on multiple lines\n" +
                        "  1. nested list\n" +
                        "     * inner\n" +
                        "       item",

                "<ul>\n<li>bullet list\n"
                        + "on multiple lines\n"
                        + "<ol>\n<li>nested list\n"
                        + "<ul>\n<li>inner\n"
                        + "item</li>\n</ul>\n</li>\n</ol>\n</li>\n</ul>\n");
    }

    @Test
    public void testBulletListSpecial() {
        assertRendering(
                "* bullet list\n" +
                        "  on multiple lines\n" +
                        "  1. nested list\n" +
                        "    a. different numbering\n" +
                        "       styles",

                "<ul>\n<li>bullet list\n"
                        + "on multiple lines\n"
                        + "<ol>\n<li>nested list\n"
                        + "<ol class=\"loweralpha\">\n<li>different numbering\n"
                        + "styles\n"
                        + "</li>\n</ol>\n</li>\n</ol>\n</li>\n</ul>\n");
    }

    @Test
    public void testDefinitionList() {
        assertRendering(
                " term:: definition on\n"
                        + "        multiple lines",

                "<dl>\n<dt>term</dt>\n<dd>definition on\n"
                        + "multiple lines\n"
                        + "</dd>\n</dl>");
    }

    @Test
    public void testPreformatted() {
        assertRendering(
                "{{{\n"
                        + "multiple lines, ''no wiki'',\n"
                        + "      white space respected\n"
                        + "}}}",

                "<pre>multiple lines, ''no wiki'',\n"
                        + "      white space respected\n"
                        + "</pre>");
    }

    @Test
    public void testBlockquotes() {
        assertRendering(
                "  if there's some leading\n"
                        + "  space the text is quoted",

                "<blockquote>\n"
                        + "<p>"
                        + "if there's some leading\n"
                        + "space the text is quoted"
                        + "</p>\n"
                        + "</blockquote>\n");
    }

    @Test
    public void testBlockquotesNested() {
        assertRendering(
                "  if there's some leading\n" +
                        "  space the text is quoted" +
                        "    more indentation makes" +
                        "    nested block" +
                        "  original level",

                "<blockquote>\n"
                        + "<p>"
                        + "if there's some leading\n"
                        + "space the text is quoted"
                        + "</p>\n"
                        + "</blockquote>\n");
    }

    @Test
    public void testCitation() {
        assertRendering(
                "> First line\n"
                        + "> Another line\n"
                        + ">\n"
                        + "> > Nested line\n"
                        + ">\n"
                        + "> Last line",

                "<blockquote>\n"
                        + "<p>First line\n"
                        + "Another line</p>\n"
                        + "<blockquote>\n"
                        + "<p>Nested line</p>\n"
                        + "</blockquote>\n"
                        + "<p>Last line</p>\n"
                        + "</blockquote>\n"
                        + "");
    }

    @Test
    public void testCitationTrac() {
        assertRendering(
                "> > ... (I said)\n"
                        + ">   (he replied)",

                "<blockquote>\n"
                        + "<blockquote>\n"
                        + "<p>... (I said)</p>\n"
                        + "</blockquote>\n"
                        + "<p>(he replied)</p>\n"
                        + "</blockquote>\n");
    }

    @Test
    public void testTables() {
        assertRendering(
                "||= Table Header =|| Cell ||\n"
                        + "||||  (details below)  ||",

                "<table>\n"
                        + "<tbody><tr><th> Table Header </th><td> Cell \n"
                        + "</td></tr><tr><td colspan=\"2\" style=\"text-align: center\">  (details below)  \n"
                        + "</td></tr></tbody></table>");
    }

    @Test
    public void testLinks() {
        assertRendering(
                "https://trac.edgewall.org",

                "<p><a href=\"https://trac.edgewall.org\">https://trac.edgewall.org</a></p>\n");
    }

    @Test
    public void testWikiFormatting() {
        assertRendering(
                "WikiFormatting (CamelCase)",

                " <a href=\"/wiki/1.2/WikiFormatting\">wiki:WikiFormatting</a>, <a href=\"/wiki/1.2/WikiFormatting\">wiki:\"WikiFormatting\"</a> \n"
                        + "");
    }

    @Test
    public void testTracLinksWiki() {
        assertRendering(
                "wiki:WikiFormatting, wiki:\"WikiFormatting\"",

                "<p><a href=\"wiki:WikiFormatting\">WikiFormatting</a>, <a href=\"wiki:WikiFormatting\">WikiFormatting</a></p>\n");
    }

    @Test
    public void testTracLinksTicket() {
        assertRendering(
                "#1 (ticket), [1] (changeset), {1} (report)",

                "<p><a href=\"ticket:1\">#1</a> (ticket), <a href=\"changeset:1\">[1]</a> (changeset), <a href=\"report:1\">{1}</a> (report)</p>\n");
    }

    @Test
    public void testTracLinksTicketPrefix() {
        assertRendering(
                "ticket:1, ticket:1#comment:1",

                "<p><a href=\"ticket:1\">#1</a>, <a href=\"ticket:1#comment:1\">ticket:1#comment:1</a></p>\n");
    }

    @Test
    public void testTracLinksTicketInLink() {
        assertRendering(
                "Ticket [ticket:1], [ticket:1 ticket one]",

                " Ticket <a href=\"/ticket/1\" title=\"#1: enhancement: Add a new project summary module. (new)\">1</a>, <a href=\"/ticket/1\" title=\"#1: enhancement: Add a new project summary module. (new)\">ticket&nbsp;one</a> \n");
    }

    @Test
    public void testTracLinksTicketInLinkDouble() {
        assertRendering(
                "Ticket [[ticket:1]], [[ticket:1|ticket one]]",

                "<p>Ticket <a href=\"ticket:1\">1</a>, <a href=\"ticket:1\">ticket one</a></p>\n");
    }

    // Setting Anchors

    @Test
    public void testEscapingMarkup() {
        assertRendering(
                "!'' doubled quotes",

                "<p>'' doubled quotes</p>");
        assertRendering(
                "!wiki:WikiFormatting, !WikiFormatting",

                "<p>wiki:WikiFormatting, WikiFormatting</p>");
        assertRendering(
                "`{{{-}}}` triple curly brackets",

                "<p><code>{{{-}}}</code> triple curly brackets \n"
                        + "</p>");
    }

    @Test
    public void testImages() {
        assertRendering(
                "[[Image(link)]]",

                "<a href=\"/chrome/site/../common/trac_logo_mini.png\" style=\"padding:0; border:none\"><img alt=\"trac_logo_mini.png\" src=\"/chrome/site/../common/trac_logo_mini.png\" title=\"trac_logo_mini.png\"></a>");
    }

    @Test
    public void testProcessors() {
        assertRendering(
                "{{{#!python\n"
                        + "  hello = lambda: \"world\"\n"
                        + "  }}}",

                "");
    }

    @Test
    public void testComments() {
        assertRendering(
                "{{{#!comment\n"
                        + "Note to Editors: ...\n"
                        + "}}}",

                "");
    }

    @Test
    public void testMiscellaneous() {
        assertRendering(
                "Line [[br]] break \n"
                        + "Line \\\\ break\n"
                        + "----\n"
                        + "",

                "<p>\n"
                        + "Line <br> break\n"
                        + "Line <br> break\n"
                        + "</p>\n"
                        + "<hr>\n"
                        + "");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

}
