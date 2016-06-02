package com.sos.jitl.extact;

import com.sos.jitl.extract.job.CSV2CSVJobOptions;
import com.sos.jitl.extract.model.CSV2CSVModel;

public class CSV2CSVModelTest {

    public CSV2CSVModelTest() {
    }

    public static void main(String[] args) throws Exception {
        String config = "D:/scheduler/config";
        CSV2CSVJobOptions options = new CSV2CSVJobOptions();
        options.input_file.setValue(config + "/in.csv");
        options.output_file.setValue(config + "/out.csv");
        options.input_file_quote_character.setValue("\"");
        options.null_string.setValue("null");
        try {
            CSV2CSVModel model = new CSV2CSVModel(options);
            model.process();
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

}