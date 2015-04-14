package com.sos.jitl.extract.model;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.jitl.extract.helper.ExtractUtil;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.reporting.helper.ReportUtil;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class ResultSet2CSVModel {
	private Logger logger = LoggerFactory.getLogger(ResultSet2CSVModel.class);
	
	private SOSHibernateConnection connection;
	private ResultSet2CSVJobOptions options;
	
	/**
	 * 
	 * @param conn
	 * @param opt
	 */
	public ResultSet2CSVModel(SOSHibernateConnection conn, ResultSet2CSVJobOptions opt){
		connection = conn;
		options = opt;
	}
	
	/**
	 * 
	 */
	public void process() throws Exception{
		String method = "process"; 
		
		SOSHibernateResultSetProcessor resultSetProcessor = null;
		FileWriter writer = null;
		CSVPrinter printer = null;
		DateTime start = new DateTime();
		boolean removeOutputFile = false;
		String outputFile = options.output_file.Value();
		try{
			logger.info(String.format("%s: statement = %s, output file = %s",
					method,
					options.statement.Value(),
					outputFile));
			
			if(ExtractUtil.hasDateReplacement(outputFile)){
				outputFile = ExtractUtil.getDateReplacement(outputFile);
				logger.info(String.format("%s: output file after replacement = %s",
						method,
						outputFile));
			}
			
			resultSetProcessor = new SOSHibernateResultSetProcessor(connection);
			ResultSet rs = resultSetProcessor.createResultSet(options.statement.Value(),
					ScrollMode.FORWARD_ONLY,
					true);
			
			Character delimeter = SOSString.isEmpty(options.delimiter.Value()) ? '\0' : options.delimiter.Value().charAt(0);
			Character quoteCharacter = SOSString.isEmpty(options.quote_character.Value()) ? null : options.quote_character.Value().charAt(0);
			Character escapeCharacter = SOSString.isEmpty(options.escape_character.Value()) ? null : options.escape_character.Value().charAt(0);
			
			CSVFormat format = CSVFormat.newFormat(delimeter)
					.withRecordSeparator(options.record_separator.Value())  
					.withNullString(options.null_string.Value())
		            .withCommentMarker('#')
		            .withIgnoreEmptyLines(false)
		            .withQuote(quoteCharacter)
		            .withQuoteMode(QuoteMode.ALL)
		            .withEscape(escapeCharacter);
			
			writer = new FileWriter(outputFile);
			if(options.skip_header.value()){
				printer = new CSVPrinter(writer,format);
			}
			else{
				printer = format.withHeader(rs).print(writer);
			}
			
			printer.printRecords(rs);
			
			logger.info(String.format("%s: duration = %s",
					method,ReportUtil.getDuration(start,new DateTime())));
		}
		catch(Exception ex){
			removeOutputFile = true;
			throw new Exception(String.format("%s[statement = %s]: %s",
					method,
					options.statement.Value(),
					ex.toString()),
					SOSHibernateConnection.getException(ex));
		}
		finally{
			if(writer != null){
				try{writer.flush();}catch(Exception e){}
				try{writer.close();}catch(Exception e){}
			}
			if(printer != null){
				try{printer.flush();}catch(Exception e){}
				try{printer.close();}catch(Exception e){}
			}
			
			if(removeOutputFile){
				try{
					File f = new File(outputFile);
					if(f.exists()){	f.deleteOnExit();}
				}
				catch(Exception ex){}
			}
			
			if(resultSetProcessor != null){
				try{resultSetProcessor.close();	}catch(Exception ex){}
			}
		}
	}
}
