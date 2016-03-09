package com.sos.jitl.extract.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.jitl.extract.helper.ExtractUtil;
import com.sos.jitl.extract.job.CSV2CSVJobOptions;
import com.sos.jitl.reporting.helper.ReportUtil;

/** @author Robert Ehrlich */
public class CSV2CSVModel {

    private Logger logger = LoggerFactory.getLogger(CSV2CSVModel.class);
    private CSV2CSVJobOptions options;

    private String[] headers = null;
    private int[] headerIndexes = null;
    private String[] fields = null;
    boolean printAllFields = false;
    boolean hasNumericalFields = false;

    /** @param opt */
    public CSV2CSVModel(CSV2CSVJobOptions opt) {
        options = opt;
    }

    private void setDefaults() {
        headers = null;
        headerIndexes = null;
        fields = null;
        printAllFields = false;
        hasNumericalFields = false;
    }

    /**
	 * 
	 */
    public void process() throws Exception {
        String method = "process";

        DateTime start = new DateTime();
        FileReader reader = null;
        CSVParser parser = null;
        CSVPrinter printer = null;
        FileWriter writer = null;
        boolean removeOutputFile = false;
        String outputFile = options.output_file.Value();

        try {
            setDefaults();

            if (ExtractUtil.hasDateReplacement(outputFile)) {
                outputFile = ExtractUtil.getDateReplacement(outputFile);
                logger.info(String.format("%s: output file after replacement = %s", method, outputFile));
            }

            File f = new File(options.input_file.Value());
            if (!f.exists()) {
                throw new Exception(String.format("input file does not exist %s", f.getCanonicalPath()));
            }
            if (!f.canRead()) {
                throw new Exception(String.format("can't read the input file %s", f.getCanonicalPath()));
            }

            reader = new FileReader(options.input_file.Value());
            writer = new FileWriter(outputFile);

            parseFields();
            parser = getCSVParser(reader);
            parseHeader(parser);
            printer = getCSVPrinter(writer);

            int i = 0;
            int headerRows = options.skip_header.value() ? 0 : 1;
            int dataRows = 0;
            for (CSVRecord record : parser) {
                Iterable<String> rec = null;
                if (printAllFields) {
                    // schleife nur wegen null values, sonst rec record;
                    ArrayList<String> al = new ArrayList<String>();
                    for (int j = 0; j < record.size(); j++) {
                        String val = record.get(j);
                        if (val.equals(options.input_file_null_string.Value())) {
                            val = options.null_string.Value();
                        }
                        al.add(val);
                    }
                    rec = al;
                } else {
                    ArrayList<String> al = new ArrayList<String>();
                    for (int index : headerIndexes) {
                        String val = null;
                        try {
                            val = record.get(index);
                        } catch (Exception ex) {
                            throw new Exception(String.format("[param \"fields\" = %s] not found field index = %s", options.fields.Value(), index + 1));// orig
                                                                                                                                                        // index
                                                                                                                                                        // +
                                                                                                                                                        // 1
                        }
                        if (val.equals(options.input_file_null_string.Value())) {
                            val = options.null_string.Value();
                        }
                        al.add(val);
                    }
                    rec = al;
                }

                if (i == 0 && hasNumericalFields && options.skip_header.value()) {

                } else {
                    printer.printRecord(rec);
                    dataRows++;

                    if ((dataRows + headerRows) % options.log_info_step.value() == 0) {
                        logger.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                    }
                }
                i++;
            }

            logger.info(String.format("%s: total rows written = %s (header = %s, data = %s), duration = %s", method, (headerRows + dataRows), headerRows, dataRows, ReportUtil.getDuration(start, new DateTime())));
        } catch (Exception ex) {
            removeOutputFile = true;
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (Exception e) {
                }
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
            if (printer != null) {
                try {
                    printer.flush();
                } catch (Exception e) {
                }
                try {
                    printer.close();
                } catch (Exception e) {
                }
            }

            if (removeOutputFile) {
                try {
                    File f = new File(outputFile);
                    if (f.exists()) {
                        f.deleteOnExit();
                    }
                } catch (Exception ex) {
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }

            if (parser != null) {
                try {
                    parser.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
	 * 
	 */
    private void parseFields() {
        fields = options.fields.Value().split(";");
        if (fields.length == 1 && fields[0].equals("*")) {
            printAllFields = true;
        } else {
            try {
                Integer.parseInt(options.fields.Value().replaceAll(";", ""));
                hasNumericalFields = true;
            } catch (Exception ex) {
                hasNumericalFields = false;
            }
        }

    }

    /** @param parser
     * @throws Exception */
    private void parseHeader(CSVParser parser) throws Exception {
        String method = "parseHeader";

        try {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            if (headerMap == null) {
                if (!hasNumericalFields) {
                    throw new Exception(String.format("param \"fields\" contains non-numeric values %s", options.fields.Value()));
                }
                headerIndexes = new int[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    Integer intVal = Integer.parseInt(fields[i]);
                    headerIndexes[i] = intVal - 1;
                }
            } else {
                if (printAllFields) {
                    headers = new String[headerMap.size()];
                    headerIndexes = new int[headerMap.size()];
                    int j = 0;
                    for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                        headers[j] = entry.getKey();
                        headerIndexes[j] = entry.getValue();
                        j++;
                    }
                } else {
                    headers = new String[fields.length];
                    headerIndexes = new int[fields.length];
                    int j = 0;
                    for (int i = 0; i < fields.length; i++) {
                        String val = fields[i];
                        try {
                            Integer intVal = Integer.parseInt(val);
                            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                                if (intVal == entry.getValue() + 1) {
                                    headers[j] = entry.getKey();
                                    headerIndexes[j] = entry.getValue();
                                    j++;
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            if (headerMap.containsKey(val)) {
                                headers[j] = val;
                                headerIndexes[j] = headerMap.get(val);
                                j++;
                            } else {
                                throw new Exception(String.format("[param \"fields\" = %s] not found declared field %s in the header row %s", options.fields.Value(), val, headerMap.keySet()));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }

    }

    /** @param reader
     * @return
     * @throws Exception */
    private CSVParser getCSVParser(FileReader reader) throws Exception {
        Character inputFileDelimeter = SOSString.isEmpty(options.input_file_delimiter.Value()) ? '\0'
                : options.input_file_delimiter.Value().charAt(0);
        Character inputFileQuoteCharacter = SOSString.isEmpty(options.input_file_quote_character.Value()) ? null
                : options.input_file_quote_character.Value().charAt(0);
        Character inputFileEscapeCharacter = SOSString.isEmpty(options.input_file_escape_character.Value()) ? null
                : options.input_file_escape_character.Value().charAt(0);

        CSVFormat formatReader = CSVFormat.newFormat(inputFileDelimeter).withRecordSeparator(options.input_file_record_separator.Value()).withCommentMarker('#').withIgnoreEmptyLines(false).withQuote(inputFileQuoteCharacter).withQuoteMode(QuoteMode.ALL).withEscape(inputFileEscapeCharacter);
        if (!hasNumericalFields) {
            formatReader = formatReader.withHeader();
        }

        return new CSVParser(reader, formatReader);
    }

    /** @param writer
     * @return
     * @throws Exception */
    private CSVPrinter getCSVPrinter(FileWriter writer) throws Exception {
        Character delimeter = SOSString.isEmpty(options.delimiter.Value()) ? '\0' : options.delimiter.Value().charAt(0);
        Character quoteCharacter = SOSString.isEmpty(options.quote_character.Value()) ? null : options.quote_character.Value().charAt(0);
        Character escapeCharacter = SOSString.isEmpty(options.escape_character.Value()) ? null : options.escape_character.Value().charAt(0);

        CSVFormat formatWriter = CSVFormat.newFormat(delimeter).withRecordSeparator(options.record_separator.Value()).withNullString(options.null_string.Value()).withCommentMarker('#').withIgnoreEmptyLines(false).withQuote(quoteCharacter).withQuoteMode(QuoteMode.ALL).withEscape(escapeCharacter);
        if (!options.skip_header.value()) {
            formatWriter = formatWriter.withHeader(headers);
        }
        return new CSVPrinter(writer, formatWriter);
    }

}
