package com.sos.jitl.extact;

import com.sos.jitl.extract.job.CSV2CSVJobOptions;
import com.sos.jitl.extract.model.CSV2CSVModel;

public class CSV2CSVModelTest {
	
	/**
	 * 
	 */
	public CSV2CSVModelTest(){
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";
		config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_9_build124/re-dell_4444_snap19_b124/config";
		
		//CSV2CSVModelTest test = new CSV2CSVModelTest();
		CSV2CSVJobOptions options = new CSV2CSVJobOptions();
		
		options.input_file.Value(config+"/out.csv");
		options.output_file.Value(config+"/out_3.csv");
		
		options.input_file_quote_character.Value("\"");
		
		//options.fields.Value();
		//options.fields.Value("HISTORY_ID;END_TIME;LOG");
		options.null_string.Value("null");
		//options.quote_character.Value("\"");
		//options.skip_header.value(false);
		
		try {
			CSV2CSVModel model = new CSV2CSVModel(options);
			model.process();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());		
		}
		
	}

}
