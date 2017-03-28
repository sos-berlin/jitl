package com.sos.jitl.inventory.helper;

import javax.json.JsonObject;

import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;


public class CallableAgent {

    private DBItemInventoryAgentInstance agent;
    private JsonObject result;
    
    public DBItemInventoryAgentInstance getAgent() {
        return agent;
    }
    
    public void setAgent(DBItemInventoryAgentInstance agent) {
        this.agent = agent;
    }
    
    public JsonObject getResult() {
        return result;
    }
    
    public void setResult(JsonObject result) {
        this.result = result;
    }

}
