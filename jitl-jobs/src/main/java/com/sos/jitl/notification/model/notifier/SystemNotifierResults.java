package com.sos.jitl.notification.model.notifier;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

public class SystemNotifierResults {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierResults.class);

    private String _content;
    private Long _lastNotificationId;
    private ArrayList<SendedError> _sendedErrors;

    private static final String DELIMITER = ";";
    private static final String SENDED_ERRORS_DELIMITER = ",";
    private static final String SENDED_ERROR_VALUE_DELIMITER = "-";

    public SystemNotifierResults(final String content) {
        _content = content;
        _sendedErrors = new ArrayList<SendedError>();
        parse();
    }

    public void updateState(final String normalizedState) {
        LOGGER.debug(String.format("updateState: normalizedState=%s", normalizedState));

        Optional<SendedError> entry = _sendedErrors.stream().filter(e -> e.getState().equals(normalizedState)).findFirst();
        if (!entry.isPresent()) {
            SendedError err = new SendedError();
            err.setState(normalizedState);
            err.setCounter(new Long(0));
            _sendedErrors.add(err);
        }
    }

    public void update(final Long notificationId, final String normalizedState, Long step) {
        LOGGER.debug(String.format("update: notificationId=%s, normalizedState=%s, step=%s", notificationId, normalizedState, step
                ));

        _lastNotificationId = notificationId;
        Optional<SendedError> entry = _sendedErrors.stream().filter(e -> e.getState().equals(normalizedState)).findFirst();
        if (entry.isPresent()) {
            SendedError err = entry.get();
            err.setStateStep(step);
            err.setCounter(err.getCounter() + 1);
        }
    }

    public String toString() {
        StringBuffer result = null;

        if (_lastNotificationId != null) {
            result = new StringBuffer("l=" + String.valueOf(_lastNotificationId));
            if (_sendedErrors.size() > 0) {
                result.append(DELIMITER);
                result.append("e=");
                for (int i = 0; i < _sendedErrors.size(); i++) {
                    String eResult = _sendedErrors.get(i).toString();
                    if (eResult != null) {
                        result.append(eResult);
                        result.append(SENDED_ERRORS_DELIMITER);
                    }
                }
            }
        }
        return result == null ? null : result.toString();
    }

    // l=12345678;e=my_job_step1-1-1-r,my_job_step_2-2-1-r,
    private void parse() {
        if (!SOSString.isEmpty(_content)) {
            String[] arr = _content.split(DELIMITER);
            if (arr[0].startsWith("l=")) {
                try {
                    _lastNotificationId = Long.parseLong(arr[0].replace("l=", ""));
                } catch (Exception e) {
                    LOGGER.warn(String.format("can't extract lastNotificationId from %s: %s", arr[0], e.toString()), e);
                }
            }
            if (arr.length > 1) {
                if (arr[1].startsWith("e=")) {
                    String[] earr = arr[1].replace("e=", "").split(SENDED_ERRORS_DELIMITER);
                    for (int i = 0; i < earr.length; i++) {
                        if (!SOSString.isEmpty(earr[i])) {
                            _sendedErrors.add(new SendedError(earr[i]));
                        }
                    }
                }
            }
        }
    }

    public String normalizeState(String state) {
        if (state != null) {
            String r = "-";
            state = state.replaceAll(r, r + r);
            state = state.replaceAll("=", r);
            state = state.replaceAll(DELIMITER, r);
            state = state.replaceAll(SENDED_ERRORS_DELIMITER, r);
            state = state.replaceAll(SENDED_ERROR_VALUE_DELIMITER, r);
        }
        return state;
    }

    public String getContent() {
        return _content;
    }

    public Long getLastNotificationId() {
        return _lastNotificationId;
    }

    public void setLastNotificationId(Long val) {
        _lastNotificationId = val;
    }

    public ArrayList<SendedError> getSendedErrors() {
        return _sendedErrors;
    }

    public void setSendedErrors(ArrayList<SendedError> val) {
        _sendedErrors = val;
    }

    public void addSendedError(SendedError err) {
        _sendedErrors.add(err);
    }

    public class SendedError {

        private String _content;
        private String _state;
        private Long _stateStep;
        private Long _counter;
        private boolean _recovered;

        public SendedError() {

        }

        public SendedError(final String content) {
            _content = content;
            parse();
        }

        public String toString() {
            StringBuffer result = null;
            if (_state != null) {
                result = new StringBuffer(normalizeState(_state));
                if (_stateStep != null) {
                    result.append(SENDED_ERROR_VALUE_DELIMITER);
                    result.append(String.valueOf(_stateStep));
                    if (_counter != null) {
                        result.append(SENDED_ERROR_VALUE_DELIMITER);
                        result.append(String.valueOf(_counter));
                        if (_recovered) {
                            result.append(SENDED_ERROR_VALUE_DELIMITER);
                            result.append("r");
                        }
                    }
                }
            }
            return result == null ? null : result.toString();
        }

        private void parse() {
            if (!SOSString.isEmpty(_content)) {
                String[] arr = _content.split(SENDED_ERROR_VALUE_DELIMITER);
                _state = arr[0];

                if (arr.length > 1) {
                    if (!SOSString.isEmpty(arr[1])) {
                        try {
                            _stateStep = Long.parseLong(arr[1]);
                        } catch (Exception e) {
                            LOGGER.warn(String.format("can't extract stateStep from %s: %s", arr[1], e.toString()), e);
                        }
                    }
                    if (arr.length > 2) {
                        if (!SOSString.isEmpty(arr[2])) {
                            try {
                                _counter = Long.parseLong(arr[2]);
                            } catch (Exception e) {
                                LOGGER.warn(String.format("can't extract counter from %s: %s", arr[2], e.toString()), e);
                            }
                        }
                        if (arr.length > 3) {
                            _recovered = arr[3].equals("r");
                        }
                    }
                }
            }
        }

        public String getContent() {
            return _content;
        }

        public String getState() {
            return _state;
        }

        public void setState(String val) {
            _state = val;
        }

        public Long getStateStep() {
            return _stateStep;
        }

        public void setStateStep(Long val) {
            _stateStep = val;
        }

        public Long getCounter() {
            return _counter;
        }

        public void setCounter(Long val) {
            _counter = val;
        }

        public boolean isRecovered() {
            return _recovered;
        }

        public void setRecovered(boolean val) {
            _recovered = val;
        }

    }

}
