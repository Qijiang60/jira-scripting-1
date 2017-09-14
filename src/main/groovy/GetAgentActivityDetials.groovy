import com.atlassian.bamboo.resultsummary.AgentResultsSummaryManager
import com.atlassian.bamboo.resultsummary.BuildResultsSummary
import com.atlassian.bamboo.util.TextProviderUtils
import com.atlassian.bamboo.utils.DurationUtils
import com.atlassian.sal.api.component.ComponentLocator
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import java.util.concurrent.TimeUnit

@BaseScript CustomEndpointDelegate delegate

/**
 * End point to retrieve Bamboo agent build activity details.
 *
 * @return Response wrapping build activity details
 */

getAgentActivityDetails(httpMethod: "GET", groups: ["bamboo-admin"]) {
    MultivaluedMap queryParams, String body ->
        RequestParams requestParams = RequestParams.from(queryParams);
        AgentActivityDetailsDto agentActivityDetialsDto = doGetAgentActivityDetialsDto(requestParams)
        return Response.ok(agentActivityDetialsDto).build()
}

/**
 * Implementation for retrieving Bamboo agent's activity details.
 *
 * @param params from request.
 * @return Agent activity details wrapped in an object.
 * */
def doGetAgentActivityDetialsDto(RequestParams params) {
    def AgentActivityDetailsDto agentActivityDetialsDto = new AgentActivityDetailsDto();
    def AgentResultsSummaryManager agentManager = ComponentLocator.getComponent(AgentResultsSummaryManager)
    List<BuildResultsSummary> allBuildsSummary = agentManager.getAllBuildResultsSummariesForAgent(params.agentId)
    def long totalQueueDuration = 0L
    def long totalDuration = 0L
    def long totalProcessingDuration = 0L
    def long reportingDaysMills = TimeUnit.DAYS.toMillis(params.reportingDays)
    def long idleMillis = reportingDaysMills - totalProcessingDuration


    allBuildsSummary.each {
        buildSummary ->
            long duration = buildSummary.duration
            long queueDuration = buildSummary.queueDuration
            long processingDuration = buildSummary.processingDuration
            totalQueueDuration += queueDuration
            totalProcessingDuration += processingDuration
            totalDuration += duration
    }

    agentActivityDetialsDto.setTotalBuildsCount(allBuildsSummary.size())
    agentActivityDetialsDto.setIdleTime(formatMillisToTime(idleMillis))
    agentActivityDetialsDto.setTotalProcessingDuration(formatMillisToTime(totalProcessingDuration))
    agentActivityDetialsDto.setTotalDuration(formatMillisToTime(totalDuration))
    agentActivityDetialsDto.setTotalQueueDuration(formatMillisToTime(totalQueueDuration))

    return agentActivityDetialsDto
}

/**
 * Formats time in milli second to pretty printing in minutes.
 *
 * @param millis
 * @return pretty time
 * */
def String formatMillisToTime(long millis) {
    double processingDuration = (double) millis;
    return processingDuration > 0.0D ? DurationUtils.getPrettyPrint((long) processingDuration) : TextProviderUtils
            .getText("global.unknown");

}

class AgentActivityDetailsDto {
    int totalBuildsCount;
    String idleTime;
    String totalDuration;
    String totalQueueDuration;
    String totalProcessingDuration
}

class RequestParams {
    int agentId;
    int startPage;
    int pageSize;
    int reportingDays;

    static RequestParams from(MultivaluedMap queryParams) {
        RequestParams requestParams = new RequestParams();
        requestParams.setAgentId(queryParams.get("agentId"))
        requestParams.setReportingDays(queryParams.get("reportingDays"))
        requestParams.setStartPage(queryParams.get("startPage"))
        requestParams.setPageSize(queryParams.get("pageSize"))
        return requestParams
    }
}

