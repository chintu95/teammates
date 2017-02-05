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
    
    private static final String ACTION_UNSCCUSSFUL_HIGHLIGHTER_FRONT = "<span class=\"text-danger\"><strong>";
    private static final String ACTION_UNSCCUSSFUL_HIGHLIGHTER_BACK = "</strong><span>";
    
    private static final String KEYWORDS_HIGHLIGHTER_FRONT = "<mark>";
    private static final String KEYWORDS_HIGHLIGHTER_BACK = "</mark>";
    
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
        switch (activityLog.getRoleWithoutMasquerade()) {
        case Const.ActivityLog.ROLE_STUDENT:
            return Url.addParamToUrl(Const.ActionURIs.STUDENT_HOME_PAGE,
                    Const.ParamsNames.USER_ID, activityLog.getGoogleId());
        case Const.ActivityLog.ROLE_INSTRUCTOR:
            return Url.addParamToUrl(Const.ActionURIs.INSTRUCTOR_HOME_PAGE,
                    Const.ParamsNames.USER_ID, activityLog.getGoogleId());
        default:
            return null;
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
    
    // --------------- 'is' fields to determine css class ---------------
    
    public boolean getIsUserAdmin() {
        return activityLog.getRole().contains(Const.ActivityLog.ROLE_ADMIN);
    }
    
    public boolean getIsUserInstructor() {
        return activityLog.getRole().contains(Const.ActivityLog.ROLE_INSTRUCTOR);
    }
    
    public boolean getIsUserStudent() {
        return activityLog.getRole().contains(Const.ActivityLog.ROLE_STUDENT);
    }
    
    public boolean getIsUserAuto() {
        return activityLog.getRole().contains(Const.ActivityLog.ROLE_AUTO);
    }
    
    public boolean getIsUserUnregistered() {
        return activityLog.getRole().contains(Const.ActivityLog.ROLE_UNREGISTERED);
    }
    
    public boolean getIsTimeTakenWarning() {
        return activityLog.getTimeTaken() >= TIME_TAKEN_WARNING_LOWER_RANGE
                && activityLog.getTimeTaken() <= TIME_TAKEN_WARNING_UPPER_RANGE;
    }
    
    public boolean getIsTimeTakenDanger() {
        return activityLog.getTimeTaken() > TIME_TAKEN_WARNING_UPPER_RANGE
                && activityLog.getTimeTaken() <= TIME_TAKEN_DANGER_UPPER_RANGE;
        // TODO : check why there is upper range
    }
    
    public boolean getIsActionWarning() {
        return activityLog.getAction().contains(Const.ACTION_RESULT_FAILURE);
    }
    
    public boolean getIsActionDanger() {
        return activityLog.getAction().contains(Const.ACTION_RESULT_SYSTEM_ERROR_REPORT);
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
    
    public boolean getIsMasqueradeUserRole() {
        return activityLog.isMasqueradeUserRole();
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
