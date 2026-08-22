package Sapoko.docgen.generation;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация генерации .docx поверх Apache POI.
 *
 * Две особенности формата, из-за которых наивная замена не работает.
 *
 * 1. Word хранит текст параграфа не одной строкой, а набором фрагментов
 *    с одинаковым форматированием (run). Плейсхолдер почти всегда разрезан
 *    между ними: ${signer.post} лежит как '${' + 'signer' + '.' + 'post' + '}'.
 *    Поэтому текст склеивается, замена ищется в целой строке, а результат
 *    раскладывается обратно по фрагментам — так сохраняется форматирование.
 *
 * 2. Блоки подписи в части шаблонов лежат в надписях (текстовых полях).
 *    Обычный обход getParagraphs() / getTables() до них не доходит, а типы
 *    внутри mc:AlternateContent при урезанном наборе схем (poi-ooxml-lite)
 *    не распознаются — поэтому там работаем через XmlCursor, которому
 *    типы не нужны.
 *
 * Госпитальная таблица: строка-образец определяется по плейсхолдеру
 * ${position} в одной из ячеек. Она клонируется по числу переданных строк
 * и заполняется позиционно — порядок значений в List<String> должен
 * совпадать с порядком колонок в шаблоне.
 */
@Component
public class DocxGenerator implements DocumentGenerator {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}\\s]{1,80})}");

    /** По этому плейсхолдеру ищется строка-образец госпитальной таблицы. */
    private static final String TABLE_ROW_MARKER = "${position}";

    private static final String W_NS =
            "declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' ";

    /** Параграфы внутри надписей. */
    private static final String TEXTBOX_PARAGRAPHS = W_NS + ".//w:txbxContent//w:p";

    /** Текстовые узлы внутри одного параграфа. */
    private static final String TEXT_NODES = W_NS + ".//w:t";

    @Override
    public byte[] generate(Map<String, String> placeholders,
                           List<List<String>> tableRows,
                           Path templatePath) throws GenerationException {

        if (templatePath == null || !Files.isRegularFile(templatePath)) {
            throw new GenerationException("Файл шаблона не найден: " + templatePath);
        }

        try (InputStream in = Files.newInputStream(templatePath);
             XWPFDocument doc = new XWPFDocument(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Порядок важен: сначала разворачиваем таблицу (её ячейки содержат
            // плейсхолдеры-образцы), только потом общая замена.
            expandTableRows(doc, tableRows == null ? List.of() : tableRows);
            replaceInDocument(doc, placeholders);
            failOnUnresolved(doc, templatePath);

            doc.write(out);
            return out.toByteArray();

        } catch (GenerationException e) {
            throw e;
        } catch (IOException e) {
            throw new GenerationException("Не удалось прочитать шаблон: " + templatePath, e);
        } catch (RuntimeException e) {
            throw new GenerationException("Ошибка при обработке шаблона: " + templatePath, e);
        }
    }

    // ------------------------------------------------------------------
    // Госпитальная таблица
    // ------------------------------------------------------------------

    private void expandTableRows(XWPFDocument doc, List<List<String>> rows) {
        for (XWPFTable table : doc.getTables()) {
            int templateIndex = findTemplateRowIndex(table);
            if (templateIndex < 0) {
                continue;
            }

            XWPFTableRow template = table.getRow(templateIndex);

            if (rows.isEmpty()) {
                table.removeRow(templateIndex);
                return;
            }

            // Клоны создаём до заполнения — образец ещё содержит плейсхолдеры.
            for (int i = 1; i < rows.size(); i++) {
                CTRow copy = (CTRow) template.getCtRow().copy();
                table.addRow(new XWPFTableRow(copy, table), templateIndex + i);
            }

            for (int i = 0; i < rows.size(); i++) {
                fillRow(table.getRow(templateIndex + i), rows.get(i), i + 1);
            }
            return;
        }

        if (!rows.isEmpty()) {
            throw new GenerationException(
                    "В шаблоне нет строки-образца таблицы (ячейка с " + TABLE_ROW_MARKER
                            + "), но передано строк: " + rows.size());
        }
    }

    private int findTemplateRowIndex(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++) {
            for (XWPFTableCell cell : rows.get(i).getTableCells()) {
                if (cell.getText() != null && cell.getText().contains(TABLE_ROW_MARKER)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void fillRow(XWPFTableRow row, List<String> values, int rowNumber) {
        List<XWPFTableCell> cells = row.getTableCells();
        if (values.size() != cells.size()) {
            throw new GenerationException("Строка таблицы №" + rowNumber + ": передано значений "
                    + values.size() + ", а колонок в шаблоне " + cells.size());
        }
        for (int i = 0; i < cells.size(); i++) {
            setCellText(cells.get(i), values.get(i));
        }
    }

    /** Записывает текст в ячейку, сохраняя форматирование первого фрагмента. */
    private void setCellText(XWPFTableCell cell, String text) {
        List<XWPFParagraph> paragraphs = cell.getParagraphs();

        for (int i = paragraphs.size() - 1; i > 0; i--) {
            cell.removeParagraph(i);
        }

        XWPFParagraph paragraph = cell.getParagraphs().isEmpty()
                ? cell.addParagraph()
                : cell.getParagraphs().get(0);

        for (int i = paragraph.getRuns().size() - 1; i > 0; i--) {
            paragraph.removeRun(i);
        }

        XWPFRun run = paragraph.getRuns().isEmpty()
                ? paragraph.createRun()
                : paragraph.getRuns().get(0);

        run.setText(text == null ? "" : text, 0);
    }

    // ------------------------------------------------------------------
    // Замена плейсхолдеров
    // ------------------------------------------------------------------

    private void replaceInDocument(XWPFDocument doc, Map<String, String> values) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, values);
        }
        for (XWPFTable t : doc.getTables()) {
            replaceInTable(t, values);
        }
        replaceInTextBoxes(doc.getDocument().getBody(), values);

        for (XWPFHeader h : doc.getHeaderList()) {
            h.getParagraphs().forEach(p -> replaceInParagraph(p, values));
            h.getTables().forEach(t -> replaceInTable(t, values));
            replaceInTextBoxes(h._getHdrFtr(), values);
        }
        for (XWPFFooter f : doc.getFooterList()) {
            f.getParagraphs().forEach(p -> replaceInParagraph(p, values));
            f.getTables().forEach(t -> replaceInTable(t, values));
            replaceInTextBoxes(f._getHdrFtr(), values);
        }
    }

    private void replaceInTable(XWPFTable table, Map<String, String> values) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                cell.getParagraphs().forEach(p -> replaceInParagraph(p, values));
                cell.getTables().forEach(t -> replaceInTable(t, values));
            }
        }
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) {
            return;
        }

        String[] texts = new String[runs.size()];
        for (int i = 0; i < runs.size(); i++) {
            String t = runs.get(i).getText(0);
            texts[i] = t == null ? "" : t;
        }

        String[] replaced = applyReplacements(texts, values);
        if (replaced == null) {
            return;
        }
        for (int i = 0; i < runs.size(); i++) {
            runs.get(i).setText(replaced[i], 0);
        }
    }

    /**
     * Надписи. Типы узлов внутри mc:AlternateContent при урезанном наборе схем
     * не распознаются, поэтому работаем курсором: он читает и пишет текст
     * элемента, не зная его схемы.
     */
    private void replaceInTextBoxes(XmlObject root, Map<String, String> values) {
        for (XmlObject paragraph : root.selectPath(TEXTBOX_PARAGRAPHS)) {
            XmlObject[] nodes = paragraph.selectPath(TEXT_NODES);
            if (nodes.length == 0) {
                continue;
            }

            XmlCursor[] cursors = new XmlCursor[nodes.length];
            try {
                String[] texts = new String[nodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    cursors[i] = nodes[i].newCursor();
                    String t = cursors[i].getTextValue();
                    texts[i] = t == null ? "" : t;
                }

                String[] replaced = applyReplacements(texts, values);
                if (replaced != null) {
                    for (int i = 0; i < nodes.length; i++) {
                        cursors[i].setTextValue(replaced[i]);
                    }
                }
            } finally {
                for (XmlCursor cursor : cursors) {
                    if (cursor != null) {
                        cursor.dispose();
                    }
                }
            }
        }
    }

    /**
     * Ядро замены. Склеивает фрагменты в одну строку, ищет плейсхолдеры в ней,
     * затем раскладывает результат обратно: каждый символ возвращается в тот
     * фрагмент, которому принадлежал, а замена целиком уходит в фрагмент,
     * где плейсхолдер начался.
     *
     * @return новые тексты фрагментов или null, если менять нечего
     */
    private String[] applyReplacements(String[] texts, Map<String, String> values) {
        int[] starts = new int[texts.length];
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            starts[i] = joined.length();
            joined.append(texts[i]);
        }

        String full = joined.toString();
        Matcher matcher = PLACEHOLDER.matcher(full);

        StringBuilder[] result = new StringBuilder[texts.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new StringBuilder();
        }

        int pos = 0;
        boolean changed = false;

        while (matcher.find()) {
            String replacement = values.get(matcher.group(1));
            if (replacement == null) {
                continue; // не наш плейсхолдер — поймает failOnUnresolved
            }
            copyRange(full, pos, matcher.start(), starts, texts, result);
            result[fragmentAt(starts, texts, matcher.start())].append(replacement);
            pos = matcher.end();
            changed = true;
        }

        if (!changed) {
            return null;
        }

        copyRange(full, pos, full.length(), starts, texts, result);

        String[] out = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            out[i] = result[i].toString();
        }
        return out;
    }

    /** Переносит участок [from, to) исходной строки в те фрагменты, которым он принадлежал. */
    private void copyRange(String full, int from, int to,
                           int[] starts, String[] texts, StringBuilder[] result) {
        if (from >= to) {
            return;
        }
        for (int i = 0; i < texts.length; i++) {
            int fragmentStart = starts[i];
            int fragmentEnd = fragmentStart + texts[i].length();
            int overlapStart = Math.max(from, fragmentStart);
            int overlapEnd = Math.min(to, fragmentEnd);
            if (overlapStart < overlapEnd) {
                result[i].append(full, overlapStart, overlapEnd);
            }
        }
    }

    private int fragmentAt(int[] starts, String[] texts, int position) {
        for (int i = 0; i < texts.length; i++) {
            if (position >= starts[i] && position < starts[i] + texts[i].length()) {
                return i;
            }
        }
        return 0;
    }

    // ------------------------------------------------------------------
    // Проверка результата
    // ------------------------------------------------------------------

    /**
     * Плейсхолдер, оставшийся в документе, означает рассогласование шаблона
     * и метаданных в sample_fields — либо что до какой-то части документа
     * замена не добралась. Лучше узнать сразу, чем выдать человеку документ
     * с ${signer2.post} посреди подписи.
     *
     * Проверяем по сырому XML со снятыми тегами: так видно всё, включая
     * надписи, и заодно склеиваются плейсхолдеры, разрезанные между фрагментами.
     */
    private void failOnUnresolved(XWPFDocument doc, Path templatePath) {
        StringBuilder xml = new StringBuilder(doc.getDocument().xmlText());
        doc.getHeaderList().forEach(h -> xml.append(h._getHdrFtr().xmlText()));
        doc.getFooterList().forEach(f -> xml.append(f._getHdrFtr().xmlText()));

        Set<String> left = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(xml.toString().replaceAll("<[^>]+>", ""));
        while (matcher.find()) {
            left.add(matcher.group());
        }

        if (!left.isEmpty()) {
            throw new GenerationException("В шаблоне " + templatePath.getFileName()
                    + " остались незаполненные плейсхолдеры: " + String.join(", ", left));
        }
    }
}
