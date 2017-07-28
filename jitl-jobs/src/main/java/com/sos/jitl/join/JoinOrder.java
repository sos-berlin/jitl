package com.sos.jitl.join;

public class JoinOrder implements java.io.Serializable {

    private static final long serialVersionUID = 6335640211910020020L;
    private String jobChain;
    private String orderId;
    private String joinSessionId;
    private boolean isMainOrder;
    private String joinState;

    public JoinOrder(String jobChain, String orderId, String joinSessionId, boolean isMainOrder, String state) {
        super();
        this.jobChain = jobChain;
        this.orderId = orderId;
        this.isMainOrder = isMainOrder;
        this.joinSessionId = getMainOrderId(joinSessionId);  
        this.joinState = state;
    }

    public String getJobChain() {
        return jobChain;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getJoinSessionId() {
        return joinSessionId;
    }

    public boolean isMainOrder() {
        return isMainOrder;
    }

    public String getJoinState() {
        return joinState;
    }

    public String getTitle() {
        return jobChain + "(" + orderId + ")" + "::" + joinSessionId;
    }

    public String paramNameForSerializedList() {
        return String.format("jitl_joinOrderList_%s_%s", getJobChain(), getJoinState());
    }

    private String getMainOrderId(String joinSessionId) {
        if (isMainOrder) {
            return orderId;
        } else {
            if (!joinSessionId.isEmpty()) {
                return joinSessionId;
            } else {
                return orderId.replaceFirst("_[^_]*$", "");
            }
        }
    }
    
    public String getMainOrderIdTest() {
        return getMainOrderId("");
    }

}
