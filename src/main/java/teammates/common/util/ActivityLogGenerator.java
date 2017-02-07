package teammates.common.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.log.AppLogLine;

import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.UserType;
import teammates.common.exception.TeammatesException;

/**
 * Factory to generate ActivityLogEntry
 * 
 * @see {@link ActivityLogEntry}
 */
public class ActivityLogGenerator {
    public static final Pattern PATTERN_ACTIONNAME = Pattern.compile("/\\S*/(?<actionName>\\S*)");
    public static final String PATTERN_ACTIONNAME_GROUPNAME = "actionName";
    
    private static final Logger log = Logger.getLogger();
    
    /**
     * Generates log message for all *Action page.
     * 
     * @param url URL of the action
     * @param params parameterMap of the request
     * @param currUser login information generated by {@link teammates.logic.api.GateKeeper}
     * @param userAccount authentication user account generated by action
     * @param unregisteredStudent authentication unregisteredStudent attributes generated by action
     * @param logMessage log message to show to admin
     * @return log message in form specified in {@link ActivityLogEntry}
     */
    public String generateNormalPageActionLogMessage(String url, Map<String, String[]> params,
                                                     UserType currUser, AccountAttributes userAccount,
                                                     StudentAttributes unregisteredStudent, String logMessage) {
        ActivityLogEntry.Builder builder = generateBasicActivityLogEntry(url, params, currUser);
        
        boolean isUnregisteredStudent = unregisteredStudent != null;
        boolean isAccountWithGoogleId = userAccount != null && userAccount.googleId != null;
        if (isUnregisteredStudent) {
            updateInfoForUnregisteredStudent(builder, unregisteredStudent);
        } else if (isAccountWithGoogleId) {
            updateInfoForNormalUser(builder, currUser, userAccount);
        }
        
        builder.withLogMessage(logMessage);
        return builder.build().generateLogMessage();
    }
    
    private void updateInfoForUnregisteredStudent(ActivityLogEntry.Builder builder,
                StudentAttributes unregisteredStudent) {
        String role = Const.ActivityLog.ROLE_UNREGISTERED;
        if (unregisteredStudent.course != null && !unregisteredStudent.course.isEmpty()) {
            role = Const.ActivityLog.ROLE_UNREGISTERED + ":" + unregisteredStudent.course;
        }
        builder.withUserRole(role)
               .withUserName(unregisteredStudent.name)
               .withUserEmail(unregisteredStudent.email);
    }
    
    private void updateInfoForNormalUser(ActivityLogEntry.Builder builder,
            UserType currUser, AccountAttributes userAccount) {
        checkAndUpdateForMasqueradeMode(builder, currUser, userAccount);
        builder.withUserGoogleId(userAccount.googleId)
               .withUserEmail(userAccount.email)
               .withUserName(userAccount.name);
    }
    
    private void degradeRoleToStudentIfNecessary(ActivityLogEntry.Builder builder) {
        if (isStudentPage(builder.getActionServletName())) {
            builder.withUserRole(Const.ActivityLog.ROLE_STUDENT);
        }
    }
    
    private void degradeRoleToInstructorIfNecessary(ActivityLogEntry.Builder builder) {
        if (isInstructorPage(builder.getActionServletName())) {
            builder.withUserRole(Const.ActivityLog.ROLE_INSTRUCTOR);
        }
    }
    
    private void checkAndUpdateForMasqueradeMode(ActivityLogEntry.Builder builder,
                                UserType loggedInUser, AccountAttributes account) {
        if (loggedInUser != null && loggedInUser.id != null && account != null) {
            boolean isMasqueradeMode = !loggedInUser.id.equals(account.googleId);
            builder.withMasqueradeUserRole(isMasqueradeMode);
            // TODO discuss: should we rebuild log id?
            builder.withLogId(generateLogIdWithGoogleId(account.googleId, builder.getLogTime()));
        }
    }

    private boolean isInstructorPage(String servletName) {
        return servletName.toLowerCase().startsWith(Const.ActivityLog.PREFIX_INSTRUCTOR_PAGE)
                || Const.ActionURIs.INSTRUCTOR_FEEDBACK_STATS_PAGE.contains(servletName);
        // TODO rename the special INSTRUCTOR_FEEDBACK_STATS_PAGE to start with instructor
    }

    private boolean isStudentPage(String servletName) {
        return servletName.toLowerCase().startsWith(Const.ActivityLog.PREFIX_STUDENT_PAGE);
    }

    /**
     * Generates log message for servlet action failure.
     * <p>
     * If the user has logged in with google id or authenticated key, his/her identity will be recorded
     * in the log id.
     * 
     * @param url URL of the request
     * @param params parameterMap of the request
     * @param e Exception thrown in the failure
     * @param currUser login information generated by {@link teammates.logic.api.GateKeeper}
     * @return log message in form specified in {@link ActivityLogEntry}
     */
    public String generateServletActionFailureLogMessage(String url, Map<String, String[]> params,
                                                         Exception e, UserType currUser) {
        ActivityLogEntry.Builder builder = generateBasicActivityLogEntry(url, params, currUser);
        
        String message = "<span class=\"text-danger\">Servlet Action failure in "
                       + builder.getActionServletName() + "<br>"
                       + e.getClass() + ": " + TeammatesException.toStringWithStackTrace(e) + "<br>"
                       + JsonUtils.toJson(params, Map.class) + "</span>";
        builder.withLogMessage(message);
        
        builder.withActionName(Const.ACTION_RESULT_FAILURE);
        
        return builder.build().generateLogMessage();
    }
    
    /**
     * Generates log message for system error report.
     * <p>
     * If the user has logged in with google id or authenticated key, his/her identity will be recorded
     * in the log id.
     * 
     * @param url URL of the request
     * @param params parameterMap of the request
     * @param errorEmail EmailWrapper to send to the system admin
     * @param currUser login information generated by {@link teammates.logic.api.GateKeeper}
     * @return log message in form specified in {@link ActivityLogEntry}
     */
    public String generateSystemErrorReportLogMessage(String url, Map<String, String[]> params,
                                                      EmailWrapper errorEmail, UserType currUser) {
        ActivityLogEntry.Builder builder = generateBasicActivityLogEntry(url, params, currUser);
        
        // TODO can use template to generate this
        if (errorEmail != null) {
            String message = "<span class=\"text-danger\">" + errorEmail.getSubject() + "</span>"
                    + "<br>"
                    + "<a href=\"#\" onclick=\"showHideErrorMessage('error" + errorEmail.hashCode() + "');\">"
                        + "Show/Hide Details >>"
                    + "</a>"
                    + "<br>"
                    + "<span id=\"error" + errorEmail.hashCode() + "\" style=\"display: none;\">"
                        + errorEmail.getContent()
                    + "</span>";
            builder.withLogMessage(message);
        }
        
        builder.withActionName(Const.ACTION_RESULT_SYSTEM_ERROR_REPORT);
       
        return builder.build().generateLogMessage();
    }
    
    /**
     * Generates basic builder for activityLogEntry.
     * 
     * @param url URL of the request
     * @param params parameterMap of the request
     * @param req HttpServletRequest in the request
     * @param currUser login information generated by {@link teammates.logic.api.GateKeeper}
     * @return ActivityLogEntry.Builder builder with basic information
     */
    private ActivityLogEntry.Builder generateBasicActivityLogEntry(String url, Map<String, String[]> params,
                                                                   UserType currUser) {
        String servletName = getActionNameFromUrl(url);
        long currTime = System.currentTimeMillis();
        ActivityLogEntry.Builder builder = new ActivityLogEntry.Builder(servletName, url, currTime);
        builder.withActionName(servletName); // by default
        
        if (isAutomatedAction(url)) {
            builder.withLogId(generateLogIdInAutomatedAction(currTime));
            builder.withUserRole(Const.ActivityLog.ROLE_AUTO);
        } else if (currUser == null) {
            builder.withLogId(generateLogIdWithoutGoogleId(params, currTime));
            builder.withUserGoogleId(Const.ActivityLog.AUTH_NOT_LOGIN);
        } else {
            builder.withLogId(generateLogIdWithGoogleId(currUser.id, currTime));
            builder.withUserGoogleId(currUser.id);
            updateRoleForLoggedInUser(builder, currUser);
        }
        
        return builder;
    }
    
    private void updateRoleForLoggedInUser(ActivityLogEntry.Builder builder, UserType currUser) {
        if (currUser.isAdmin) {
            builder.withUserRole(Const.ActivityLog.ROLE_ADMIN);
            degradeRoleToStudentIfNecessary(builder);
            degradeRoleToInstructorIfNecessary(builder);
        } else if (currUser.isInstructor && currUser.isStudent) {
            builder.withUserRole(Const.ActivityLog.ROLE_INSTRUCTOR);
            degradeRoleToStudentIfNecessary(builder);
        } else if (currUser.isStudent) {
            builder.withUserRole(Const.ActivityLog.ROLE_STUDENT);
        } else if (currUser.isInstructor) {
            builder.withUserRole(Const.ActivityLog.ROLE_INSTRUCTOR);
        } else {
            builder.withUserRole(Const.ActivityLog.ROLE_UNREGISTERED);
        }
    }

    /**
     * Generates log message for automated action and public source accessing action.
     * <p>
     * If the user has logged in with google id or authenticated key, his/her identity will be recorded
     * in the log id. If the action is automated action, 'Auto' will be used instead for the identity.
     * 
     * @param url URL of the request
     * @param params parameterMap of the request
     * @param message log message to show to admin
     * @param currUser login information generated by {@link teammates.logic.api.GateKeeper}
     * @return log message in form specified in {@link ActivityLogEntry}
     */
    public String generateBasicActivityLogMessage(String url, Map<String, String[]> params,
                                                  String message, UserType currUser) {
        ActivityLogEntry.Builder builder = generateBasicActivityLogEntry(url, params, null);
        
        builder.withLogMessage(message);
        
        return builder.build().generateLogMessage();
    }
    
    private boolean isAutomatedAction(String url) {
        return url.startsWith(Const.ActivityLog.PREFIX_AUTO_PAGE);
    }

    private String getActionNameFromUrl(String requestUrl) {
        Matcher m = PATTERN_ACTIONNAME.matcher(requestUrl);
        if (m.matches()) {
            return m.group(PATTERN_ACTIONNAME_GROUPNAME);
        } else {
            return String.format(Const.ActivityLog.MESSAGE_ERROR_ACTIONNAME, requestUrl);
        }
    }
    
    private String generateLogIdInAutomatedAction(long time) {
        return Const.ActivityLog.ROLE_AUTO + Const.ActivityLog.FIELD_CONNECTOR + formatTimeForId(new Date(time));
    }
    
    private String generateLogIdWithoutGoogleId(Map<String, String[]> params, long time) {
        String courseId = HttpRequestHelper.getValueFromParamMap(params, Const.ParamsNames.COURSE_ID);
        String studentEmail = HttpRequestHelper.getValueFromParamMap(params, Const.ParamsNames.STUDENT_EMAIL);
        if (courseId != null && studentEmail != null) {
            return studentEmail + Const.ActivityLog.FIELD_CONNECTOR
                    + courseId + Const.ActivityLog.FIELD_CONNECTOR
                    + formatTimeForId(new Date(time));
        }
        return Const.ActivityLog.AUTH_NOT_LOGIN + Const.ActivityLog.FIELD_CONNECTOR + formatTimeForId(new Date(time));
    }
    
    private String generateLogIdWithGoogleId(String googleId, long time) {
        return googleId + Const.ActivityLog.FIELD_CONNECTOR + formatTimeForId(new Date(time));
    }
    
    private String formatTimeForId(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Const.ActivityLog.TIME_FORMAT_LOGID);
        sdf.setTimeZone(TimeZone.getTimeZone(Const.SystemParams.ADMIN_TIME_ZONE));
        return sdf.format(date.getTime());
    }
    
    /**
     * Generates {@link ActivityLogEntry} from {@link AppLogLine} provided by GAE.
     * <p>
     * If the log message in {@link AppLogLine} is not in the desired format specified in {@link ActivityLogEntry},
     * an {@link ActivityLogEntry} will be returned with log message : 'Error. Problem parsing log message from the server.'
     * 
     * @param appLog
     * @return {@link ActivityLogEntry}
     */
    public ActivityLogEntry generateActivityLogFromAppLogLine(AppLogLine appLog) {
        try {
            String[] tokens = appLog.getLogMessage().split(Pattern.quote(Const.ActivityLog.FIELD_SEPARATOR), -1);
            return initActivityLogUsingAppLogMessage(appLog, tokens);
        } catch (ArrayIndexOutOfBoundsException e) {
            return initActivityLogAsFailure(appLog, e);
        }
    }

    private ActivityLogEntry initActivityLogAsFailure(AppLogLine appLog, ArrayIndexOutOfBoundsException e) {
        ActivityLogEntry.Builder builder = new ActivityLogEntry.Builder(Const.ActivityLog.UNKNOWN,
                                                    Const.ActivityLog.UNKNOWN, appLog.getTimeUsec());
        // TODO : can use template here
        String logMessage = "<span class=\"text-danger\">" + Const.ActivityLog.MESSAGE_ERROR_LOGMESSAGE_FORMAT
                    + "</span><br>System Error: " + e.getMessage() + "<br>" + appLog.getLogMessage();
        builder.withLogMessage(logMessage);
        return builder.build();
    }

    private ActivityLogEntry initActivityLogUsingAppLogMessage(AppLogLine appLog, String[] tokens) {
        // TEAMMATESLOG|||SERVLET_NAME|||ACTION|||TO_SHOW|||ROLE|||NAME|||GOOGLE_ID|||EMAIL|||
        // MESSAGE(IN HTML)|||URL|||ID|||TIME_TAKEN
        ActivityLogEntry.Builder builder = new ActivityLogEntry.Builder(
                                            tokens[ActivityLogEntry.POSITION_OF_ACTION_SERVLETNAME],
                                            tokens[ActivityLogEntry.POSITION_OF_ACTION_URL], appLog.getTimeUsec());
        
        builder.withActionName(tokens[ActivityLogEntry.POSITION_OF_ACTION_NAME]);
        builder.withLogId(tokens[ActivityLogEntry.POSITION_OF_LOG_ID]);
        builder.withLogMessage(tokens[ActivityLogEntry.POSITION_OF_LOG_MESSAGE]);
        builder.withMasqueradeUserRole(tokens[ActivityLogEntry.POSITION_OF_USER_ROLE]
                .contains(Const.ActivityLog.MASQUERADE_ROLE_POSTFIX));
        builder.withUserEmail(tokens[ActivityLogEntry.POSITION_OF_USER_EMAIL]);
        builder.withUserGoogleId(tokens[ActivityLogEntry.POSITION_OF_USER_GOOGLEID]);
        builder.withUserName(tokens[ActivityLogEntry.POSITION_OF_USER_NAME]);
        builder.withUserRole(
                tokens[ActivityLogEntry.POSITION_OF_USER_ROLE]
                        .replace(Const.ActivityLog.MASQUERADE_ROLE_POSTFIX, ""));
        
        try {
            long actionTimeTaken = tokens.length == ActivityLogEntry.POSITION_OF_LOG_TIMETAKEN + 1
                                            ? Long.parseLong(tokens[ActivityLogEntry.POSITION_OF_LOG_TIMETAKEN].trim())
                                            : 0;
            builder.withActionTimeTaken(actionTimeTaken);
        } catch (NumberFormatException e) {
            log.severe(String.format(Const.ActivityLog.MESSAGE_ERROR_LOGMESSAGE_FORMAT, Arrays.toString(tokens)));
        }

        return builder.build();
    }
    
}
