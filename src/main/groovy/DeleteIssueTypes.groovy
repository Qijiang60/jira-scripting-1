import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.IssueTypeManager
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.type.TypeReference

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

log = Logger.getLogger("*")
def mapper = new ObjectMapper();

/**
 * This is the endpoint which removes all issue types passed in the payload.
 *
 * @return Response , response object wrapping issue types removal report.
 * */
deleteIssueTypes(httpMethod: "POST", groups: ["jira-administrators"]) {
    MultivaluedMap queryParams, String body ->
        log.debug("deleteIssueTypes() - start")
        def List<IssueTypeVm> issueTypes = mapper.readValue(body, new TypeReference<List<IssueTypeVm>>() {})
        def Collection<DeletedIssueTypeDto> deletedIssueTypeDtos = new ArrayList<>();
        issueTypes.each { issueType ->
            def DeletedIssueTypeDto deletedIssueTypeDto = doDeleteIssueType(issueType);
            deletedIssueTypeDtos.add(deletedIssueTypeDto)
        }
        log.debug("deleteIssueTypes - end")
        return Response.ok(mapper.writeValueAsString(deletedIssueTypeDtos)).build()
}

/**
 * Removes  issue type.
 * @param issueType to be deleted.
 * @return true if deletion was successful else return false.
 * */
def doDeleteIssueType(IssueTypeVm issueType) {
    def issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager);
    boolean isDeleted = false;
    try {
        issueTypeManager.removeIssueType(issueType.id, null)
        isDeleted = true;
    } catch (Exception e) {
    }

    return new DeletedIssueTypeDto(issueType.name, issueType.id, isDeleted)
}


class IssueTypeVm {
    String name;
    String id;

    IssueTypeVm() {
    }

    IssueTypeVm(String name, String id) {
        this.name = name
        this.id = id
    }

    String getName() {
        return name
    }

    String getId() {
        return id
    }

    void setName(String name) {
        this.name = name
    }

    void setId(String id) {
        this.id = id
    }
}

class DeletedIssueTypeDto {

    String name;
    String id;
    boolean deleted;

    DeletedIssueTypeDto() {
    }

    DeletedIssueTypeDto(String name, String id, boolean deleted) {
        this.name = name
        this.id = id
        this.deleted = deleted
    }

    String getName() {
        return name
    }

    String getId() {
        return id
    }

    boolean getDeleted() {
        return deleted
    }

    void setName(String name) {
        this.name = name
    }

    void setId(String id) {
        this.id = id
    }

    void setDeleted(boolean deleted) {
        this.deleted = deleted
    }
}