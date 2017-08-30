package com.sos.jitl.extract.model;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSQLExecutor;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.extract.helper.ExtractUtil;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.reporting.helper.ReportUtil;

import sos.util.SOSString;

public class ResultSet2CSVModel {

    private Logger logger = LoggerFactory.getLogger(ResultSet2CSVModel.class);
    private SOSHibernateSession session;
    private ResultSet2CSVJobOptions options;

    public ResultSet2CSVModel(SOSHibernateSession sess, ResultSet2CSVJobOptions opt) {
        session = sess;
        options = opt;
    }

    public void process() throws Exception {
        String method = "process";
        SOSHibernateSQLExecutor sqlExecutor = null;
        ResultSet resultSet = null;
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
                        
            Character delimeter = SOSString.isEmpty(options.delimiter.getValue()) ? '\0' : options.delimiter.getValue().charAt(0);
            Character quoteCharacter = SOSString.isEmpty(options.quote_character.getValue()) ? null : options.quote_character.getValue().charAt(0);
            Character escapeCharacter = SOSString.isEmpty(options.escape_character.getValue()) ? null : options.escape_character.getValue().charAt(0);
            CSVFormat format = CSVFormat.newFormat(delimeter).withRecordSeparator(options.record_separator.getValue()).withNullString(
                    options.null_string.getValue()).withCommentMarker('#').withIgnoreEmptyLines(false).withQuote(quoteCharacter).withQuoteMode(
                            QuoteMode.ALL).withEscape(escapeCharacter);

            writer = new FileWriter(outputFile);
            sqlExecutor = session.getSQLExecutor();
            resultSet = sqlExecutor.getResultSet(options.statement.getValue());
            int headerRows = 0;
            int dataRows = 0;
            if (options.skip_header.value()) {
                printer = new CSVPrinter(writer, format);
            } else {
                printer = format.withHeader(resultSet).print(writer);
                headerRows++;
            }
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; ++i) {
                    printer.print(resultSet.getObject(i));
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
            throw new Exception(String.format("%s[statement = %s]: %s", method, options.statement.getValue(), ex.toString()), ex);
        } finally {
            if (sqlExecutor != null) {
                sqlExecutor.close(resultSet);
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
            if (removeOutputFile) {
                try {
                    File f = new File(outputFile);
                    if (f.exists()) {
                        f.deleteOnExit();
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

}