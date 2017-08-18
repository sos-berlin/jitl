package com.sos.jitl.restclient;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.sos.exception.SOSException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

public class CreateApiAccessToken  extends JobSchedulerJobAdapter {

    private JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        }
        return this.signalSuccess();
    }
    
    public void doProcessing() throws SOSException, URISyntaxException {
        Variable_set params = spooler.create_variable_set();
        params.merge(spooler_task.params());
        
        if (this.isJobchain()) {
            params.merge(spooler_task.order().params());
        }
        String userAccount = params.value("user_account");
        String jocUrl = params.value("joc_url");

        JobSchedulerRestApiClient jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addAuthorizationHeader(userAccount);

        String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + "security/login"), "");
        JsonObject login = jsonFromString(answer);
        if (login.get("accessToken") != null) {
            String accessToken = login.getString("accessToken");
            spooler.variables().set_value("X-Access-Token", accessToken);
            spooler.variables().set_value("JOC_Url", jocUrl);
        }
    }

}
