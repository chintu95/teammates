package teammates.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/** A log entry to describe an action carried out by the app */
public class ActivityLogEntry {
    // The following constants describe the positions of the attributes
    // in the log message. i.e
    // TEAMMATESLOG|||SERVLET_NAME|||ACTION|||TO_SHOW|||ROLE|||NAME|||GOOGLE_ID|||EMAIL|||MESSAGE(IN HTML)|||URL|||TIME_TAKEN
    public static final int POSITION_OF_ACTION_SERVLETNAME = 1;
    public static final int POSITION_OF_ACTION_NAME = 2;
    public static final int POSITION_OF_LOG_TOSHOW = 3;
    public static final int POSITION_OF_USER_ROLE = 4;
    public static final int POSITION_OF_USER_NAME = 5;
    public static final int POSITION_OF_USER_GOOGLEID = 6;
    public static final int POSITION_OF_USER_EMAIL = 7;
    public static final int POSITION_OF_LOG_MESSAGE = 8;
    public static final int POSITION_OF_ACTION_URL = 9;
    public static final int POSITION_OF_LOG_ID = 10;
    public static final int POSITION_OF_LOG_TIMETAKEN = 11;
    
    // UI part
    private static final int TIME_TAKEN_WARNING_LOWER_RANGE = 10000;
    private static final int TIME_TAKEN_WARNING_UPPER_RANGE = 20000;
    private static final int TIME_TAKEN_DANGER_UPPER_RANGE = 60000;
    
    // Required fields
    
    // id can be in the form of <googleId>%<time> e.g. bamboo3250%20151103170618465
    // or <studentemail>%<courseId>%<time> (for unregistered students)
    //     e.g. bamboo@gmail.tmt%instructor.ema-demo%20151103170618465
    private String logId;
    private long logTime;
    
    private String actionServletName;
    private String actionName;
    
    // Optional fields
    
    private String userRole;
    private boolean isMasqueradeUserRole;
    
    private String userName;
    private String userEmail;
    private String userGoogleId;
    
    private String logMessage;
    
    private String actionUrl;
    
    private long actionTimeTaken;
    
    // legacy of messing up UI and logic
    private boolean logToShow = true;
    
    private boolean isFirstRow;
    
    private String[] keyStringsToHighlight;
    
    public ActivityLogEntry(Builder builder) {
        logTime = builder.logTime;
        actionServletName = (builder.actionServletName == null)
                          ? Const.ActivityLog.UNKNOWN : builder.actionServletName;
        actionTimeTaken = builder.actionTimeTaken;
        actionName = (builder.actionName == null)
                   ? Const.ActivityLog.UNKNOWN : builder.actionName;
        userRole = (builder.userRole == null)
                 ? Const.ActivityLog.UNKNOWN : builder.userRole;
        userName = (builder.userName == null)
                 ? Const.ActivityLog.UNKNOWN : builder.userName;
        userGoogleId = (builder.userGoogleId == null)
                     ? Const.ActivityLog.UNKNOWN : builder.userGoogleId;
        userEmail = (builder.userEmail == null)
                  ? Const.ActivityLog.UNKNOWN : builder.userEmail;
        logMessage = (builder.logMessage == null)
                   ? Const.ActivityLog.UNKNOWN : builder.logMessage;
        actionUrl = (builder.actionUrl == null)
                  ? Const.ActivityLog.UNKNOWN : builder.actionUrl;
        logId = (builder.logId == null)
              ? Const.ActivityLog.UNKNOWN : builder.logId;
        actionTimeTaken = builder.actionTimeTaken;
        isMasqueradeUserRole = builder.isMasqueradeUserRole;
    }

    /**
     * Generates a log message that will be logged in the server
     */
    public String generateLogMessage() {
        //TEAMMATESLOG|||SERVLET_NAME|||ACTION|||TO_SHOW|||ROLE|||NAME|||GOOGLE_ID|||EMAIL|||MESSAGE(IN HTML)|||URL|||ID
        return Const.ActivityLog.TEAMMATESLOG + Const.ActivityLog.FIELD_SEPARATOR
                + actionServletName + Const.ActivityLog.FIELD_SEPARATOR
                + actionName + Const.ActivityLog.FIELD_SEPARATOR
                + logToShow + Const.ActivityLog.FIELD_SEPARATOR
                + userRole + (isMasqueradeUserRole ? "(M)" : "") + Const.ActivityLog.FIELD_SEPARATOR
                + userName + Const.ActivityLog.FIELD_SEPARATOR + userGoogleId
                + Const.ActivityLog.FIELD_SEPARATOR
                + userEmail + Const.ActivityLog.FIELD_SEPARATOR
                + logMessage + Const.ActivityLog.FIELD_SEPARATOR
                + actionUrl + Const.ActivityLog.FIELD_SEPARATOR
                + logId;
    }
    
    public String getLogId() {
        return logId;
    }
    
    public boolean getLogToShow() {
        return logToShow;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public String getLogMessage() {
        return logMessage;
    }
    
    public long getLogTime() {
        return logTime;
    }
    
    public long getActionTimeTaken() {
        return actionTimeTaken;
    }
    
    public String getActionServletName() {
        return actionServletName;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public boolean isMasqueradeUserRole() {
        return isMasqueradeUserRole;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getUserGoogleId() {
        return userGoogleId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public boolean isTestingData() {
        return userEmail.endsWith(Const.ActivityLog.TESTING_DATA_EMAIL_POSTFIX);
    }
    
    // ------------- UI part -------------
    
    public String getIconRoleForShow() {
        StringBuilder iconRole = new StringBuilder(100);
        
        if (userRole.contains("Instructor")) {
            iconRole.append("<span class = \"glyphicon glyphicon-user\" style=\"color:#39b3d7;\"></span>");
            if (isMasqueradeUserRole()) {
                iconRole.append("-<span class = \"glyphicon glyphicon-eye-open\" style=\"color:#E61E1E;\"></span>- ");
            }
        } else if (userRole.contains("Student")) {
            iconRole.append("<span class = \"glyphicon glyphicon-user\" style=\"color:#FFBB13;\"></span>");
            if (isMasqueradeUserRole()) {
                iconRole.append("-<span class = \"glyphicon glyphicon-eye-open\" style=\"color:#E61E1E;\"></span>- ");
            }
        } else if (userRole.contains("Unregistered")) {
            iconRole.append("<span class = \"glyphicon glyphicon-user\"></span>");
        } else if (userRole.contains("Auto")) {
            iconRole.append("<span class = \"glyphicon glyphicon-cog\"></span>");
        } else {
            iconRole.append(userRole);
        }

        if (userRole.contains("Admin")) {
            iconRole.append("<span class = \"glyphicon glyphicon-user\" style=\"color:#E61E1E;\"></span>");
        }

        return iconRole.toString();
    }
    
    public String getDateInfo() {
        Calendar appCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(Const.SystemParams.ADMIN_TIME_ZONE));
        appCal.setTimeInMillis(logTime);

        return sdf.format(appCal.getTime());
    }
    
    public String getPersonInfo() {
        if (actionUrl.contains("/student")) {
            if (userGoogleId.contentEquals("Unregistered")) {
                return "[" + userName
                        + " (Unregistered User) "
                        + " <a href=\"mailto:" + userEmail + "\" target=\"_blank\">" + userEmail + "</a>]";
            }
            return "[" + userName
                    + " <a href=\"" + getStudentHomePageViewLink(userGoogleId) + "\" target=\"_blank\">"
                    + userGoogleId + "</a>"
                    + " <a href=\"mailto:" + userEmail + "\" target=\"_blank\">" + userEmail + "</a>]";
        } else if (actionUrl.contains("/instructor")) {
            return "[" + userName
                    + " <a href=\"" + getInstructorHomePageViewLink(userGoogleId) + "\" target=\"_blank\">"
                    + userGoogleId + "</a>"
                    + " <a href=\"mailto:" + userEmail + "\" target=\"_blank\">" + userEmail + "</a>]";
        } else {
            return userGoogleId;
        }
    }
    
    public String getActionInfo() {
        String style = "";
        
        if (logMessage.toLowerCase().contains(Const.ACTION_RESULT_FAILURE.toLowerCase())
                || logMessage.toLowerCase().contains(Const.ACTION_RESULT_SYSTEM_ERROR_REPORT.toLowerCase())) {
            style = "text-danger";
        } else {
            style = "text-success bold";
        }
        return "<a href=\"" + getUrlToShow() + "\" class=\"" + style + "\" target=\"_blank\">" + actionServletName + "</a>";
    }
    
    public String getMessageInfo() {
        
        if (logMessage.toLowerCase().contains(Const.ACTION_RESULT_FAILURE.toLowerCase())) {
            logMessage = logMessage.replace(Const.ACTION_RESULT_FAILURE, "<span class=\"text-danger\"><strong>"
                      + Const.ACTION_RESULT_FAILURE + "</strong><br>");
            logMessage = logMessage + "</span><br>";
        } else if (logMessage.toLowerCase().contains(Const.ACTION_RESULT_SYSTEM_ERROR_REPORT.toLowerCase())) {
            logMessage = logMessage.replace(Const.ACTION_RESULT_SYSTEM_ERROR_REPORT, "<span class=\"text-danger\"><strong>"
                      + Const.ACTION_RESULT_SYSTEM_ERROR_REPORT + "</strong><br>");
            logMessage = logMessage + "</span><br>";
        }
                
        return logMessage;
    }

    public String getColorCode(Long timeTaken) {
        
        if (timeTaken == null) {
            return "";
        }
        
        String colorCode = "";
        if (timeTaken >= TIME_TAKEN_WARNING_LOWER_RANGE && timeTaken <= TIME_TAKEN_WARNING_UPPER_RANGE) {
            colorCode = "text-warning";
        } else if (timeTaken > TIME_TAKEN_WARNING_UPPER_RANGE && timeTaken <= TIME_TAKEN_DANGER_UPPER_RANGE) {
            colorCode = "text-danger";
        }
        
        return colorCode;
    }

    public String getTableCellColorCode(Long timeTaken) {
        
        if (timeTaken == null) {
            return "";
        }
        
        String colorCode = "";
        if (timeTaken >= TIME_TAKEN_WARNING_LOWER_RANGE && timeTaken <= TIME_TAKEN_WARNING_UPPER_RANGE) {
            colorCode = "warning";
        } else if (timeTaken > TIME_TAKEN_WARNING_UPPER_RANGE && timeTaken <= TIME_TAKEN_DANGER_UPPER_RANGE) {
            colorCode = "danger";
        }
        return colorCode;
    }
    
    public String getLogEntryActionsButtonClass() {
        
        String className = "";
        if (logMessage.toLowerCase().contains(Const.ACTION_RESULT_FAILURE.toLowerCase())) {
            className = "btn-warning";
        } else if (logMessage.toLowerCase().contains(Const.ACTION_RESULT_SYSTEM_ERROR_REPORT.toLowerCase())) {
            className = "btn-danger";
        } else {
            className = "btn-info";
        }
        return className;
    }
    
    public String getUrlToShow() {
        if (actionUrl.contains("user=")) {
            return actionUrl;
        }
        // If not in masquerade mode, add masquerade mode
        if (actionUrl.contains("?")) {
            return actionUrl + "&user=" + userGoogleId;
        }
        return actionUrl + "?user=" + userGoogleId;
    }
    
    public void setKeyStringsToHighlight(String[] strings) {
        this.keyStringsToHighlight = strings;
    }
    
    public boolean toShow() {
        return logToShow;
    }
    
    public void setToShow(boolean toShow) {
        this.logToShow = toShow;
    }
    
    private String getInstructorHomePageViewLink(String googleId) {
        String link = Const.ActionURIs.INSTRUCTOR_HOME_PAGE;
        link = Url.addParamToUrl(link, Const.ParamsNames.USER_ID, googleId);
        return link;
    }
    
    private String getStudentHomePageViewLink(String googleId) {
        String link = Const.ActionURIs.STUDENT_HOME_PAGE;
        link = Url.addParamToUrl(link, Const.ParamsNames.USER_ID, googleId);
        return link;
    }
    
    public String getLogInfoForTableRowAsHtml() {
        return "<tr" + (isFirstRow ? " id=\"first-row\"" : "") + ">"
                 + "<td class=\"" + getTableCellColorCode(actionTimeTaken) + "\" style=\"vertical-align: middle;\">"
                     + "<a onclick=\"submitLocalTimeAjaxRequest('" + logTime + "','"
                                 + userGoogleId + "','" + userRole + "',this);\">"
                         + getDateInfo()
                     + "</a>"
                     + "<p class=\"localTime\"></p>"
                     + "<p class=\"" + getColorCode(getActionTimeTaken()) + "\">"
                         + "<strong>" + TimeHelper.convertToStandardDuration(getActionTimeTaken()) + "</strong>"
                     + "</p>"
                 + "</td>"
                 + "<td class=\"" + getTableCellColorCode(actionTimeTaken) + "\">"
                     + "<form method=\"get\" action=\"" + Const.ActionURIs.ADMIN_ACTIVITY_LOG_PAGE + "\">"
                         + "<h4 class=\"list-group-item-heading\">"
                             + getIconRoleForShow() + "&nbsp;" + getActionInfo() + "&nbsp;"
                             + "<small> id:" + logId + " " + getPersonInfo() + "</small>" + "&nbsp;"
                             + "<button type=\"submit\" class=\"btn " + getLogEntryActionsButtonClass() + " btn-xs\">"
                                 + "<span class=\"glyphicon glyphicon-zoom-in\"></span>"
                             + "</button>"
                             + "<input type=\"hidden\" name=\"filterQuery\""
                                     + " value=\"person:" + getAvailableIdenficationString() + "\">"
                             + "<input class=\"ifShowAll_for_person\" type=\"hidden\" name=\"all\""
                                     + " value=\"false\">"
                             + "<input class=\"ifShowTestData_for_person\" type=\"hidden\" name=\"testdata\""
                                     + " value=\"false\">"
                         + "</h4>"
                         + "<div>" + getMessageInfo() + "</div>"
                     + "</form>"
                 + "</td>"
             + "</tr>";
    }
    
    private String getAvailableIdenficationString() {
        if (!getUserGoogleId().contentEquals("Unregistered") && !getUserGoogleId().contentEquals("Unknown")) {
            return getUserGoogleId();
        }
        if (getUserEmail() != null && !getUserEmail().contentEquals("Unknown")) {
            return getUserEmail();
        }
        if (getUserName() != null && !getUserName().contentEquals("Unknown")) {
            return getUserName();
        }
        return "";
    }
    
    public void highlightKeyStringInMessageInfoHtml() {
        
        if (keyStringsToHighlight == null) {
            return;
        }
        
        for (String stringToHighlight : keyStringsToHighlight) {
            if (logMessage.toLowerCase().contains(stringToHighlight.toLowerCase())) {
                
                int startIndex = logMessage.toLowerCase().indexOf(stringToHighlight.toLowerCase());
                int endIndex = startIndex + stringToHighlight.length();
                String realStringToHighlight = logMessage.substring(startIndex, endIndex);
                logMessage = logMessage.replace(realStringToHighlight, "<mark>" + realStringToHighlight + "</mark>");
            }
        }
        
    }
    
    public void setFirstRow() {
        isFirstRow = true;
    }
    
    /**
     * A builder class for {@link ActivityLogEntry}.
     * <p>
     * All null values (if possible) that are passed into the builder will be ignored and will be
     * replaced by {@link Const.ActivityLog.UNKNOWN}
     * 
     * @see {@link ActivityLogEntry}
     */
    public static class Builder {
        // Required parameters
        private String actionServletName;
        private String actionUrl;
        private long logTime;
        
        // Optional parameters - initialized to default values
        private String actionName = Const.ActivityLog.UNKNOWN;
        private long actionTimeTaken;
        private String userRole = Const.ActivityLog.UNKNOWN;
        private String userName = Const.ActivityLog.UNKNOWN;
        private String userGoogleId = Const.ActivityLog.UNKNOWN;
        private String userEmail = Const.ActivityLog.UNKNOWN;
        private String logMessage = Const.ActivityLog.UNKNOWN;
        private String logId = Const.ActivityLog.UNKNOWN;
        private boolean isMasqueradeUserRole;
        
        public Builder(String servletName, String url, long time) {
            actionServletName = servletName;
            actionUrl = url;
            logTime = time;
        }
        
        public Builder withActionName(String val) {
            actionName = val;
            return this;
        }
        
        public Builder withUserRole(String val) {
            userRole = val;
            return this;
        }
        
        public Builder withUserName(String val) {
            userName = val;
            return this;
        }
        
        public Builder withUserGoogleId(String val) {
            userGoogleId = val;
            return this;
        }
        
        public Builder withUserEmail(String val) {
            userEmail = val;
            return this;
        }
        
        public Builder withMasqueradeUserRole(Boolean val) {
            isMasqueradeUserRole = val;
            return this;
        }
        
        public Builder withLogId(String val) {
            logId = val;
            return this;
        }
        
        public Builder withLogMessage(String val) {
            logMessage = val;
            return this;
        }
        
        public Builder withActionTimeTaken(long val) {
            actionTimeTaken = val;
            return this;
        }
        
        public long getLogTime() {
            return logTime;
        }
        
        public String getActionServletName() {
            return actionServletName;
        }
        
        public ActivityLogEntry build() {
            return new ActivityLogEntry(this);
        }
    }

}
