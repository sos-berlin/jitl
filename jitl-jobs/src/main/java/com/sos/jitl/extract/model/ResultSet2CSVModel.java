package com.sos.jitl.extract.model;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.jitl.extract.helper.ExtractUtil;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.reporting.helper.ReportUtil;

public class ResultSet2CSVModel {

    private Logger logger = LoggerFactory.getLogger(ResultSet2CSVModel.class);
    private SOSHibernateSession connection;
    private ResultSet2CSVJobOptions options;

    public ResultSet2CSVModel(SOSHibernateSession conn, ResultSet2CSVJobOptions opt) {
        connection = conn;
        options = opt;
    }

    public void process() throws Exception {
        String method = "process";
        SOSHibernateResultSetProcessor resultSetProcessor = null;
        FileWriter writer = null;
        CSVPrinter printer = null;
        DateTime start = new DateTime();
        boolean removeOutputFile = false;
        String outputFile = options.output_file.getValue();
        try {
            logger.info(String.format("%s: statement = %s, output file = %s", method, options.statement.getValue(), outputFile));
            if (ExtractUtil.hasDateReplacement(outputFile)) {
                outputFile = ExtractUtil.getDateReplacement(outputFile);
                logger.info(String.format("%s: output file after replacement = %s", method, outputFile));
            }
            Optional<Integer> fetchSize = Optional.empty();
            if (!SOSString.isEmpty(this.options.large_result_fetch_size.getValue())) {
                try {
                    if (this.options.large_result_fetch_size.value() != -1) {
                        fetchSize = Optional.of(this.options.large_result_fetch_size.value());
                    }
                } catch (Exception ex) {
                }
            }
            resultSetProcessor = new SOSHibernateResultSetProcessor(connection);
            ResultSet rs = resultSetProcessor.createResultSet(options.statement.getValue(), ScrollMode.FORWARD_ONLY, true, fetchSize);
            Character delimeter = SOSString.isEmpty(options.delimiter.getValue()) ? '\0' : options.delimiter.getValue().charAt(0);
            Character quoteCharacter = SOSString.isEmpty(options.quote_character.getValue()) ? null : options.quote_character.getValue().charAt(0);
            Character escapeCharacter = SOSString.isEmpty(options.escape_character.getValue()) ? null : options.escape_character.getValue().charAt(0);
            CSVFormat format =
                    CSVFormat.newFormat(delimeter).withRecordSeparator(options.record_separator.getValue()).withNullString(options.null_string.getValue()).withCommentMarker(
                            '#').withIgnoreEmptyLines(false).withQuote(quoteCharacter).withQuoteMode(QuoteMode.ALL).withEscape(escapeCharacter);

            writer = new FileWriter(outputFile);
            int headerRows = 0;
            int dataRows = 0;
            if (options.skip_header.value()) {
                printer = new CSVPrinter(writer, format);
            } else {
                printer = format.withHeader(rs).print(writer);
                headerRows++;
            }
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; ++i) {
                    printer.print(rs.getObject(i));
                }
                printer.println();
                dataRows++;
                if ((dataRows + headerRows) % options.log_info_step.value() == 0) {
                    logger.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }
            }
            logger.info(String.format("%s: total rows written = %s (header = %s, data = %s), duration = %s", method, headerRows + dataRows,
                    headerRows, dataRows, ReportUtil.getDuration(start, new DateTime())));
        } catch (Exception ex) {
            removeOutputFile = true;
            throw new Exception(String.format("%s[statement = %s]: %s", method, options.statement.getValue(), ex.toString()),
                    SOSHibernateSession.getException(ex));
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
            if (resultSetProcessor != null) {
                try {
                    resultSetProcessor.close();
                } catch (Exception ex) {
                }
            }
        }
    }

}