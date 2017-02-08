package teammates.test.cases.util;

import org.testng.annotations.Test;

import teammates.common.util.ActivityLogEntry;
import teammates.common.util.Const;
import teammates.test.cases.BaseTestCase;
import teammates.test.driver.AssertHelper;

/**
 * SUT: {@link ActivityLogEntry}
 */
public class ActivityLogEntryTest extends BaseTestCase {

    @Test
    public void testDefaultSettings() {
        ActivityLogEntry.Builder builder = new ActivityLogEntry.Builder("instructorHome", "URL", 10);
        String logMessage = "TEAMMATESLOG|||instructorHome|||instructorHome|||true|||Unknown|||Unknown|||Unknown"
                + "|||Unknown|||Unknown|||URL";
        AssertHelper.assertLogMessageEquals(logMessage, builder.build().generateLogMessage());
    }
    
    @Test
    public void testBuilderNullValues() {
        ActivityLogEntry.Builder builder = new ActivityLogEntry.Builder(null, null, 10);
        builder.withActionResponse(null)
               .withLogId(null)
               .withLogMessage(null)
               .withUserEmail(null)
               .withUserGoogleId(null)
               .withUserName(null)
               .withUserRole(null);
        String logMessage = "TEAMMATESLOG|||Unknown|||Unknown|||true|||Unknown|||Unknown|||Unknown"
                + "|||Unknown|||Unknown|||Unknown";
        ActivityLogEntry entry = builder.build();
        AssertHelper.assertLogMessageEquals(logMessage, entry.generateLogMessage());
        assertEquals(Const.ActivityLog.UNKNOWN, entry.getLogId());
    }
    
    @Test
    public void testBuilder() {
        ______TS("Test generateLogMessage");
        
        String statusToAdmin = "<span class=\"text-danger\">Error. ActivityLogEntry object is not created "
                               + "for this servlet action.</span><br>Message";
        String logMessage = "TEAMMATESLOG|||instructorHome|||Servlet Action Failure|||true"
                + "|||Instructor(M)|||Joe|||GoogleIdA|||instructor@email.tmt"
                + "|||" + statusToAdmin + "|||url.com";
        ActivityLogEntry.Builder builder = new ActivityLogEntry.Builder("instructorHome", "url.com", 10);
        builder.withActionResponse(Const.ACTION_RESULT_FAILURE)
               .withUserRole(Const.ActivityLog.ROLE_INSTRUCTOR)
               .withUserName("Joe")
               .withUserGoogleId("GoogleIdA")
               .withUserEmail("instructor@email.tmt")
               .withLogMessage(statusToAdmin)
               .withLogId("GoogleIdA@10")
               .withActionTimeTaken(20)
               .withMasqueradeUserRole(true);
        
        ActivityLogEntry entry = builder.build();
        AssertHelper.assertLogMessageEquals(logMessage, entry.generateLogMessage());
        
        ______TS("Test getters");
        
        assertEquals("instructorHome", entry.getActionName());
        assertEquals(Const.ACTION_RESULT_FAILURE, entry.getActionResponse());
        assertEquals(10, entry.getLogTime());
        assertEquals("url.com", entry.getActionUrl());
        assertEquals("Instructor", entry.getUserRole());
        assertTrue(entry.isMasqueradeUserRole());
        assertEquals("GoogleIdA", entry.getUserGoogleId());
        assertEquals("instructor@email.tmt", entry.getUserEmail());
        assertEquals("Joe", entry.getUserName());
        assertEquals("GoogleIdA@10", entry.getLogId());
        assertEquals(20, entry.getActionTimeTaken());
        assertEquals(statusToAdmin, entry.getLogMessage());
        assertTrue(entry.isTestingData());
        assertTrue(entry.getLogToShow());
    }
    
}
