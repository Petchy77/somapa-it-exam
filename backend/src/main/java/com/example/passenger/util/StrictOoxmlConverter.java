package com.example.passenger.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class StrictOoxmlConverter {

    private static final String[][] REPLACEMENTS = {
            {
                    "http://purl.oclc.org/ooxml/spreadsheetml/main",
                    "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
            },
            {
                    "http://purl.oclc.org/ooxml/drawingml/main",
                    "http://schemas.openxmlformats.org/drawingml/2006/main"
            },
            {
                    "http://purl.oclc.org/ooxml/drawingml/spreadsheetDrawing",
                    "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing"
            },
            {
                    "http://purl.oclc.org/ooxml/officeDocument/relationships",
                    "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
            },
            {
                    "http://purl.oclc.org/ooxml/package/2006/relationships",
                    "http://schemas.openxmlformats.org/package/2006/relationships"
            }
    };

    private static final Pattern DATE_CELL_PATTERN = Pattern.compile(
            "<c([^>]*?) t=\"d\"([^>]*)>\\s*<v>([^<]*)</v>\\s*</c>");

    private StrictOoxmlConverter() {
    }

    public static InputStream toTransitional(InputStream input) throws IOException {
        byte[] original = input.readAllBytes();
        if (!isStrict(original)) {
            return new ByteArrayInputStream(original);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(original.length);
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(original));
             ZipOutputStream zout = new ZipOutputStream(out)) {

            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] data = zin.readAllBytes();
                String lower = entry.getName().toLowerCase();
                boolean textual = lower.endsWith(".xml") || lower.endsWith(".rels");

                if (textual) {
                    String text = new String(data, StandardCharsets.UTF_8);

                    // 1) Rewrite namespace URIs to their transitional equivalents.
                    for (String[] pair : REPLACEMENTS) {
                        text = text.replace(pair[0], pair[1]);
                    }

                    // 2) Rewrite t="d" date cells to inline strings so POI can
                    //    see the date value. Only worksheets carry cell data, so
                    //    this is a no-op on other xml parts.
                    if (lower.startsWith("xl/worksheets/") && lower.endsWith(".xml")) {
                        text = rewriteDateCells(text);
                    }
                    data = text.getBytes(StandardCharsets.UTF_8);
                }

                ZipEntry copy = new ZipEntry(entry.getName());
                zout.putNextEntry(copy);
                zout.write(data);
                zout.closeEntry();
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    static String rewriteDateCells(String xml) {
        Matcher m = DATE_CELL_PATTERN.matcher(xml);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String attrsBefore = m.group(1);
            String attrsAfter = m.group(2);
            String value = m.group(3);
            String replacement =
                    "<c" + attrsBefore + " t=\"inlineStr\"" + attrsAfter + ">"
                    + "<is><t>" + value + "</t></is>"
                    + "</c>";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static boolean isStrict(byte[] bytes) {
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase("xl/workbook.xml")) {
                    byte[] data = zin.readAllBytes();
                    String text = new String(data, StandardCharsets.UTF_8);
                    return text.contains("http://purl.oclc.org/ooxml/spreadsheetml/main");
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
}
