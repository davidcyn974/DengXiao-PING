package com.ping.ide;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import java.util.regex.Pattern;
import java.util.List;

//import static com.sun.javafx.scene.control.skin.Utils.getResource;
public class SyntaxHighlighter {




    private static final String KEYWORDHTML_PATTERN = "\\b(" + String.join("|", Constants.KEYWORDSHTML) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "<!--[\\s\\S]*?-->";

    private static final String KEYWORDCSS_PATTERN = "\\b(" + String.join("|", Constants.KEYWORDCSS) + ")\\b";
    private static final String COMMENTCSS_PATTERN = "/\\*[\\s\\S]*?\\*/";
    private static final String STRINGCSS_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String FUNCTIONCSS_PATTERN = "[-a-zA-Z0-9]+(?=\\()";
    private static final String CLASSCSS_PATTERN = "\\.[a-zA-Z\\-_]+";

    private static final String KEYWORDPHP_PATTERN = "\\b(" + String.join("|", Constants.KEYWORDPHP) + ")\\b";
    private static final String COMMENTPHP_PATTERN = "/\\*[\\s\\S]*?\\*/";

    private static final String COMMENTPHPONE_PATTERN = "//.*";
    private static final String STRINGPHP_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String FUNCTIONPHP_PATTERN = "[-a-zA-Z0-9]+(?=\\()";
    private static final String CLASSPHP_PATTERN = "\\.[a-zA-Z\\-_]+";
    private static final String VARIABLEPHP_PATTERN = "\\$[a-zA-Z\\-_]+";

    private static final String KEYWORDJS_PATTERN = "\\b(" + String.join("|", Constants.KEYWORDJS) + ")\\b";
    private static final String COMMENTJS_PATTERN = "/\\*[\\s\\S]*?\\*/";

    private static final String COMMENTJSONE_PATTERN = "//.*";
    private static final String STRINGJS_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    private static final String STRINGJSONE_PATTERN = "'([^'\\\\]|\\\\.)*'";

    private static final String STRINGVAR_PATTERN = "`([^`\\\\]|\\\\.)*`";
    private static final String FUNCTIONJS_PATTERN = "[-a-zA-Z0-9]+(?=\\()";
    private static final String TYPEJS_PATTERN = "\\b(" + String.join("|", Constants.TYPEJS) + ")\\b";
    private static Pattern PATTERN = Pattern.compile(
            "(?<KEYWORDHTML>" + KEYWORDHTML_PATTERN + ")"
                    + "|(?<KEYWORDCSS>" + KEYWORDCSS_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<COMMENTCSS>" + COMMENTCSS_PATTERN + ")"
                    + "|(?<STRINGCSS>" + STRINGCSS_PATTERN + ")"
                    + "|(?<FUNCTIONCSS>" + FUNCTIONCSS_PATTERN + ")"
                    + "|(?<CLASSCSS>" + CLASSCSS_PATTERN + ")"
                    + "|(?<COMMENTPHP>" + COMMENTPHP_PATTERN + ")"
                    + "|(?<STRINGPHP>" + STRINGPHP_PATTERN + ")"
                    + "|(?<FUNCTIONPHP>" + FUNCTIONPHP_PATTERN + ")"
                    + "|(?<CLASSPHP>" + CLASSPHP_PATTERN + ")"
                    + "|(?<VARIABLEPHP>" + VARIABLEPHP_PATTERN + ")"
                    + "|(?<KEYWORDPHP>" + KEYWORDPHP_PATTERN + ")"
                    + "|(?<COMMENTPHPONE>" + COMMENTPHPONE_PATTERN + ")"
                    + "|(?<COMMENTJS>" + COMMENTJS_PATTERN + ")"
                    + "|(?<STRINGJS>" + STRINGJS_PATTERN + ")"
                    + "|(?<FUNCTIONJS>" + FUNCTIONJS_PATTERN + ")"
                    + "|(?<KEYWORDJS>" + KEYWORDJS_PATTERN + ")"
                    + "|(?<TYPEJS>" + TYPEJS_PATTERN + ")"
                    + "|(?<COMMENTJSONE>" + COMMENTJSONE_PATTERN + ")"
                    + "|(?<STRINGJSONE>" + STRINGJSONE_PATTERN + ")"
                    + "|(?<STRINGVAR>" + STRINGVAR_PATTERN + ")"
    );
    public static StyleSpans<Collection<String>> computeHighlighting(String text, String fileName) {
        if (fileName.endsWith(".html")) {
            PATTERN = Pattern.compile(
                    "(?<KEYWORDHTML>" + KEYWORDHTML_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                            + "|(?<STRING>" + STRING_PATTERN + ")"
                            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
            );
            return computeHighlightingHTML(text);
        } else if (fileName.endsWith(".css")) {
            PATTERN = Pattern.compile(
                    "(?<KEYWORDCSS>" + KEYWORDCSS_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                            + "|(?<COMMENTCSS>" + COMMENTCSS_PATTERN + ")"
                            + "|(?<STRINGCSS>" + STRINGCSS_PATTERN + ")"
                            + "|(?<FUNCTIONCSS>" + FUNCTIONCSS_PATTERN + ")"
                            + "|(?<CLASSCSS>" + CLASSCSS_PATTERN + ")"
            );
            return computeHighlightingCSS(text);
        } else if (fileName.endsWith(".php")) {
            PATTERN = Pattern.compile(
                    "(?<KEYWORDPHP>" + KEYWORDPHP_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                            + "|(?<COMMENTPHP>" + COMMENTPHP_PATTERN + ")"
                            + "|(?<STRINGPHP>" + STRINGPHP_PATTERN + ")"
                            + "|(?<FUNCTIONPHP>" + FUNCTIONPHP_PATTERN + ")"
                            + "|(?<CLASSPHP>" + CLASSPHP_PATTERN + ")"
                            + "|(?<VARIABLEPHP>" + VARIABLEPHP_PATTERN + ")"
                            + "|(?<COMMENTPHPONE>" + COMMENTPHPONE_PATTERN + ")"
            );
            return computeHighlightingPHP(text);
        }
        else if (fileName.endsWith(".js")) {
            PATTERN = Pattern.compile(
                    "(?<KEYWORDJS>" + KEYWORDJS_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                            + "|(?<COMMENTJS>" + COMMENTJS_PATTERN + ")"
                            + "|(?<STRINGJS>" + STRINGJS_PATTERN + ")"
                            + "|(?<FUNCTIONJS>" + FUNCTIONJS_PATTERN + ")"
                            + "|(?<TYPEJS>" + TYPEJS_PATTERN + ")"
                            + "|(?<COMMENTJSONE>" + COMMENTJSONE_PATTERN + ")"
                            + "|(?<STRINGJSONE>" + STRINGJSONE_PATTERN + ")"
                            + "|(?<STRINGVAR>" + STRINGVAR_PATTERN + ")"
            );
            return computeHighlightingJS(text);
        }
        else {

            Matcher matcher = PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while (matcher.find()) {
                String styleClass =
                                matcher.group("PAREN") != null ? "paren" :
                                        matcher.group("BRACE") != null ? "brace" :
                                                matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                        null; /* never happens */
                assert styleClass != null;
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();

            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            return spansBuilder.create();
        }

    }
    public static StyleSpans<Collection<String>> computeHighlightingHTML(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORDHTML") != null ? "keywordhtml" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                    matcher.group("STRING") != null ? "string" :
                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();

        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    public static StyleSpans<Collection<String>> computeHighlightingCSS(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORDCSS") != null ? "keywordcss" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                    matcher.group("STRINGCSS") != null ? "stringcss" :
                                                            matcher.group("COMMENTCSS") != null ? "commentcss" :
                                                                    matcher.group("FUNCTIONCSS") != null ? "functioncss" :
                                                                            matcher.group("CLASSCSS") != null ? "classcss" :
                                                                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();

        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    public static StyleSpans<Collection<String>> computeHighlightingPHP(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORDPHP") != null ? "keywordphp" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                    matcher.group("STRINGPHP") != null ? "stringphp" :
                                                            matcher.group("COMMENTPHP") != null ? "commentphp" :
                                                                    matcher.group("COMMENTPHPONE") != null ? "commentphpone" :
                                                                        matcher.group("FUNCTIONPHP") != null ? "functionphp" :
                                                                                matcher.group("CLASSPHP") != null ? "classphp" :
                                                                                        matcher.group("VARIABLEPHP") != null ? "variablephp" :
                                                                                                null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();

        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    public static StyleSpans<Collection<String>> computeHighlightingJS(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORDJS") != null ? "keywordjs" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                    matcher.group("STRINGJS") != null ? "stringjs" :
                                                            matcher.group("STRINGJSONE") != null ? "stringjsone" :
                                                                    matcher.group("STRINGVAR") != null ? "stringvar" :
                                                                    matcher.group("COMMENTJS") != null ? "commentjs" :
                                                                    matcher.group("FUNCTIONJS") != null ? "functionjs" :
                                                                                    matcher.group("TYPEJS") != null ? "typejs" :
                                                                                            matcher.group("COMMENTJSONE") != null ? "commentjsone" :
                                                                                                        null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();

        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}
