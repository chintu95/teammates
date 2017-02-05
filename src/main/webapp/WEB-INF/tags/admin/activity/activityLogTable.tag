<%@ tag description="Activity Log Table in Admin Activity Log Page" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="logs" type="java.util.Collection" required="true" %>
<%@ tag import="teammates.common.util.Const" %>

<div class="panel panel-primary">
    <div class="panel-heading">
        <strong>Activity Log</strong>
    </div>
    <div class="table-responsive">
        <table class="table table-condensed dataTable" id="logsTable">
            <thead>
                <tr>
                    <th width="10%">Date [Timing]</th>
                    <th>[Role][Action][Google ID][Name][Email]</th>
                </tr>
            </thead>
            <tbody>
                <c:if test="${empty logs}">
                    <tr id="noResultFoundMessage">
                        <td colspan='2'><i>No application logs found</i></td>
                    </tr>
                </c:if>
                <c:forEach items="${logs}" var="log" varStatus="count">
                    <tr id="${count.first ? "first-row" : ""}">
                        <td class="${log.isTimeTakenWarning ? "warning" : 
                        		             log.isTimeTakenDanger ? "danger" : "" }">
                            <a onclick="submitLocalTimeAjaxRequest('${log.logTime}','${log.userGoogleId}','${log.displayedRole}', this);">
                                ${log.displayedLogTime}
                            </a>
                            <p class="localTime"></p>
                            <p class="${log.isTimeTakenWarning ? "text-warning" : 
                                             log.isTimeTakenDanger ? "text-danger" : "" }">
                                <strong>${log.displayedLogTimeTaken}</strong>
                            </p>
                        </td>
                        <td class="${log.isTimeTakenWarning ? "warning" : 
                                             log.isTimeTakenDanger ? "danger" : "" }">
                            <form method="get" action="<%= Const.ActionURIs.ADMIN_ACTIVITY_LOG_PAGE %>">
                                <h4 class="list-group-item-heading">
                                    <c:choose>
                                        <c:when test="${log.isUserAdmin}">
                                            <span class="glyphicon glyphicon-user text-danger"></span>
                                        </c:when>
                                        <c:when test="${log.isUserInstructor}">
                                            <span class="glyphicon glyphicon-user text-primary"></span>
                                        </c:when>
                                        <c:when test="${log.isUserStudent}">
                                            <span class="glyphicon glyphicon-user text-warning"></span>
                                        </c:when>
                                        <c:when test="${log.isUserAuto}">
                                            <span class="glyphicon glyphicon-cog"></span>
                                        </c:when>
                                        <c:when test="${log.isUserUnregistered}">
                                            <span class="glyphicon glyphicon-user"></span>
                                        </c:when>
                                    </c:choose>
                                    <c:if test="${log.isMasqueradeUserRole}">
                                        <span class="glyphicon glyphicon-eye-open text-danger"></span>
                                    </c:if>
                                    <a href="${log.displayedActionUrl}" 
                                       class="${log.isActionWarning || log.isActionDanger 
                                                    ? "text-danger" : "" }" 
                                       target="_blank">
                                        ${log.actionName}
                                    </a>
                                    <small>
                                        id: ${log.logId} 
                                        [
                                        ${log.userName}
                                        <c:choose>
                                            <c:when test="${log.hasUserHomeLink}">
                                                <a href="${log.userHomeLink}" target="_blank">${log.userGoogleId}</a>
                                            </c:when>
                                            <c:otherwise>
                                                ${log.displayedRole}
                                            </c:otherwise>   
                                        </c:choose>
                                        <a href="mailto:${log.userEmail}" target="_blank">${log.userEmail}</a>
                                        ]
                                    </small>
                                    <button type="submit" class="btn btn-xs ${log.isActionWarning ? "btn-warning" : 
                                                                                 log.isActionDanger ? "btn-danger" : "btn-info"}">
                                        <span class="glyphicon glyphicon-zoom-in"></span>
                                    </button>
                                    <input type="hidden" name="filterQuery" value="person:${log.userIdentity}">
                                    <input class="ifShowAll_for_person" type="hidden" name="all" value="false">
                                    <input class="ifShowTestData_for_person" type="hidden" name="testdata"
                                        value="false">
                                </h4>
                                <div>${log.displayedMessage}</div>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>