package teammates.ui.template;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import teammates.common.util.ActivityLogEntry;
import teammates.common.util.Const;
import teammates.common.util.TimeHelper;
import teammates.common.util.Url;

public class AdminActivityLogTableRow {
    
    private static final int TIME_TAKEN_WARNING_LOWER_RANGE = 10000;
    private static final int TIME_TAKEN_WARNING_UPPER_RANGE = 20000;
    private static final int TIME_TAKEN_DANGER_UPPER_RANGE = 60000;
    
    private static final String MASQUERADE_ROLE_ICON_CLASS = "glyphicon glyphicon-eye-open text-danger";
    
    private static final String ACTION_UNSCCUSSFUL_HIGHLIGHTER_FRONT = "<span class=\"text-danger\"><strong>";
    private static final String ACTION_UNSCCUSSFUL_HIGHLIGHTER_BACK = "</strong><span>";
    
    private static final String KEYWORDS_HIGHLIGHTER_FRONT = "<mark>";
    private static final String KEYWORDS_HIGHLIGHTER_BACK = "</mark>";
    
    private enum TimeTakenCssClass {
        NORMAL("", ""),
        WARNING("warning", "text-warning"),
        DANGER("danger", "text-danger");
        
        private String cellClass;
        private String textClass;
        
        TimeTakenCssClass(String cClass, String tClass) {
            cellClass = cClass;
            textClass = tClass;
        }
          
        public String toHtmlTableCellClass() {
            return cellClass;
        }
        
        public String toHtmlTextClass() {
            return textClass;
        }
        
        public static TimeTakenCssClass generateTimeTakenEnum(long timeTaken) {
            if (timeTaken >= TIME_TAKEN_WARNING_LOWER_RANGE && timeTaken <= TIME_TAKEN_WARNING_UPPER_RANGE) {
                return WARNING;
            } else if (timeTaken > TIME_TAKEN_WARNING_UPPER_RANGE && timeTaken <= TIME_TAKEN_DANGER_UPPER_RANGE) {
                return DANGER;
            } else {
                return NORMAL;
            }
        }
    }
    
    private enum ActionTypeCssClass {
        NORMAL("btn-info", "text-success bold"),
        WARNING("btn-warning", "text-danger"),
        DANGER("btn-danger", "text-dnager");
        
        private String buttonClass;
        private String textClass;
        
        ActionTypeCssClass(String cClass, String tClass) {
            buttonClass = cClass;
            textClass = tClass;
        }
          
        public String toHtmlButtonClass() {
            return buttonClass;
        }
        
        public String toHtmlTextClass() {
            return textClass;
        }
        
        public static ActionTypeCssClass generateActionTypeEnum(String action) {
            switch(action) {
            case Const.ACTION_RESULT_FAILURE:
                return WARNING;
            case Const.ACTION_RESULT_SYSTEM_ERROR_REPORT:
                return DANGER;
            default:
                return NORMAL;
            }
        }
    }
    
    private enum UserRoleCssClass {
        ADMIN("glyphicon glyphicon-user text-danger"),
        INSTRUCTOR("glyphicon glyphicon-user text-primary"),
        STUDENT("glyphicon glyphicon-user text-warning"),
        AUTO("glyphicon glyphicon-cog"),
        UNREGISTERED("glyphicon glyphicon-user"),
        UNKNOWN("");
        
        private String iconCssClass;
        
        UserRoleCssClass(String className) {
            iconCssClass = className;
        }
        
        public static UserRoleCssClass generateRoleCssClassHelper(String role) {
            switch(role) {
            case Const.ActivityLog.ROLE_ADMIN:
                return ADMIN;
            case Const.ActivityLog.ROLE_INSTRUCTOR:
                return INSTRUCTOR;
            case Const.ActivityLog.ROLE_STUDENT:
                return STUDENT;
            case Const.ActivityLog.ROLE_AUTO:
                return AUTO;
            default:
                if (role.contains(Const.ActivityLog.ROLE_UNREGISTERED)) {
                    return UNREGISTERED;
                }
                return UNKNOWN;
            }
        }
        
        public String getIconCssClass() {
            return iconCssClass;
        }
    }
    
    private ActivityLogEntry activityLog;
    
    private String[] keyStringsToHighlight;
    
    public AdminActivityLogTableRow(ActivityLogEntry entry) {
        activityLog = entry;
    }
    
    public void setKeyStringsToHighlight(String[] strs) {
        keyStringsToHighlight = strs;
    }
    
    // --------------- Additional generated fields ---------------
    
    public String getUserHomeLink() {
        switch(activityLog.getRoleWithoutMasquerade()) {
        case Const.ActivityLog.ROLE_STUDENT:
            return Url.addParamToUrl(Const.ActionURIs.STUDENT_HOME_PAGE,
                    Const.ParamsNames.USER_ID, activityLog.getGoogleId());
        case Const.ActivityLog.ROLE_INSTRUCTOR:
            return Url.addParamToUrl(Const.ActionURIs.INSTRUCTOR_HOME_PAGE,
                    Const.ParamsNames.USER_ID, activityLog.getGoogleId());
        default:
            return activityLog.getGoogleId();
        }
    }
    
    public boolean getHasUserHomeLink() {
        return activityLog.getRoleWithoutMasquerade().contains(Const.ActivityLog.ROLE_STUDENT)
                || activityLog.getRoleWithoutMasquerade().contains(Const.ActivityLog.ROLE_INSTRUCTOR);
    }
    
    public String getUserIdentity() {
        String googleId = activityLog.getGoogleId();
        if (!googleId.contentEquals(Const.ActivityLog.AUTH_NOT_LOGIN)
                && !googleId.contentEquals(Const.ActivityLog.UNKNOWN)) {
            return googleId;
        }
        
        String email = activityLog.getEmail();
        if (email != null && !email.contentEquals(Const.ActivityLog.UNKNOWN)) {
            return email;
        }
        
        String name = activityLog.getName();
        if (name != null && !name.contentEquals(Const.ActivityLog.UNKNOWN)) {
            return name;
        }
        return "";
    }
    
    public boolean getHasUserEmail() {
        return !activityLog.getEmail().contains(Const.ActivityLog.UNKNOWN);
    }
    
    // --------------- Css Classes of elements ---------------
    
    public String getTableCellClass() {
        return TimeTakenCssClass.generateTimeTakenEnum(activityLog.getTimeTaken()).toHtmlTableCellClass();
    }

    public String getTimeTakenClass() {
        return TimeTakenCssClass.generateTimeTakenEnum(activityLog.getTimeTaken()).toHtmlTextClass();
    }

    public String getUserRoleIconClass() {
        return UserRoleCssClass.generateRoleCssClassHelper(activityLog.getRoleWithoutMasquerade()).getIconCssClass();
    }

    public String getMasqueradeUserRoleIconClass() {
        if (activityLog.isMasqueradeUserRole()) {
            return MASQUERADE_ROLE_ICON_CLASS;
        } else {
            return "";
        }
    }

    public String getActionTextClass() {
        return ActionTypeCssClass.generateActionTypeEnum(activityLog.getAction()).toHtmlTextClass();
    }

    public String getActionButtonClass() {
        return ActionTypeCssClass.generateActionTypeEnum(activityLog.getAction()).toHtmlButtonClass();
    }
    
    // --------------- Enhancement to the fields ---------------

    public String getDisplayedActionUrl() {
        return Url.addParamToUrl(activityLog.getUrl(),
                Const.ParamsNames.USER_ID, activityLog.getGoogleId());
    }

    // TODO find a way to make use of TimeHelper
    public String getDisplayedLogTime() {
        Calendar appCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(Const.SystemParams.ADMIN_TIME_ZONE));
        appCal.setTimeInMillis(activityLog.getTime());
    
        return sdf.format(appCal.getTime());
    }

    public String getDisplayedRole() {
        return activityLog.getRole(); // will change later
    }

    public String getDisplayedLogTimeTaken() {
        return TimeHelper.convertToStandardDuration(activityLog.getTimeTaken());
    }

    public String getDisplayedMessage() {
        String displayedMessage = activityLog.getMessage();
        String[] keywords = {
                Const.ACTION_RESULT_FAILURE,
                Const.ACTION_RESULT_SYSTEM_ERROR_REPORT
        };
        displayedMessage = highlightKeyword(displayedMessage, keywords,
                ACTION_UNSCCUSSFUL_HIGHLIGHTER_FRONT, ACTION_UNSCCUSSFUL_HIGHLIGHTER_BACK);
        displayedMessage = highlightKeyword(displayedMessage, keyStringsToHighlight,
                KEYWORDS_HIGHLIGHTER_FRONT, KEYWORDS_HIGHLIGHTER_BACK);
        return displayedMessage;
    }
    
    // --------------- Forwarding activityLog methods ---------------
    
    public String getUserGoogleId() {
        return activityLog.getGoogleId();
    }

    public String getUserName() {
        return activityLog.getName();
    }

    public String getUserEmail() {
        return activityLog.getEmail();
    }

    public String getLogId() {
        return activityLog.getId();
    }

    public String getActionName() {
        return activityLog.getAction();
    }

    public String getLogTime() {
        return String.valueOf(activityLog.getTime());
    }
    
    // --------------- Helper methods ---------------
    
    // TODO: can be generalized to helper method
    private String highlightKeyword(String original, String[] keywords,
                                    String wrapperTagFront, String wrapperTagEnd) {
        
        if (keywords == null) {
            return original;
        }
        
        String highlightedString = original;
        
        for (String stringToHighlight : keywords) {
            if (highlightedString.toLowerCase().contains(stringToHighlight.toLowerCase())) {
                
                int startIndex = original.toLowerCase().indexOf(stringToHighlight.toLowerCase());
                int endIndex = startIndex + stringToHighlight.length();
                String realStringToHighlight = original.substring(startIndex, endIndex);
                highlightedString = highlightedString.replace(realStringToHighlight,
                        wrapperTagFront + realStringToHighlight + wrapperTagEnd);
            }
        }
        
        return highlightedString;
        
    }
       
}
