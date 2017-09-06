import com.atlassian.jira.ComponentManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.IssueTypeManager
import com.atlassian.jira.issue.fields.config.FieldConfigScheme
import com.atlassian.jira.issue.issuetype.IssueType
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper

import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate endpointDelegate
log = Logger.getLogger("*")
def mapper = new ObjectMapper();

/**
 * This is the endpoint that wraps the logic of finding all issue types which are only associated woth default issue type scheme.
 * @return Response response object which wraps all unused issue types in a JSON array.
 */
getUnusedIssueTypes(httpMethod: "GET", groups: ["jira-administrators"]) {
    def unusedIssueTypes = doGetUnusedIssueTypes()
    return Response.ok(mapper.writeValueAsString(unusedIssueTypes)).build()
}

/**
 * Retrieve all unused issue types.
 *
 * @return all unused issue types.
 *
 * */
def doGetUnusedIssueTypes() {
    def issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager)
    Collection<IssueType> issueTypes = issueTypeManager.getIssueTypes()
    def issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager()
    def FieldConfigScheme defaultIssueTypeScheme = issueTypeSchemeManager.getDefaultIssueTypeScheme()
    def Collection<IssueTypeDto> unusedIssueTypes = new LinkedList<>()

    issueTypes.each { issueType ->
        def Collection<FieldConfigScheme> relatedSchemes = issueTypeSchemeManager.getAllRelatedSchemes(issueType.id)
        if (relatedSchemes.size() == 1) {
            def FieldConfigScheme onlyRelatedScheme = relatedSchemes.first()
            if (onlyRelatedScheme.id == defaultIssueTypeScheme.id) {
                def IssueTypeDto issueTypeDto = new IssueTypeDto(issueType.name, issueType.id)
                unusedIssueTypes.add(issueTypeDto)
            }
        }
    }
    return unusedIssueTypes
}

class IssueTypeDto {

    String name;
    String id;

    IssueTypeDto() {
    }

    IssueTypeDto(String name, String id) {
        this.name = name
        this.id = id
    }

    String getName() {
        return name
    }

    String getId() {
        return id
    }
}