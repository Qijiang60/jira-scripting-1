import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.type.TypeReference

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate
log = Logger.getLogger("*")
def mapper = new ObjectMapper();

/**
 * This is the endpoint which removes all issue types passed in the payload.
 *
 * @return Response , response object wrapping issue types removal report.
 * */
deleteScreens(httpMethod: "POST", groups: ["jira-administrators"]) {
    MultivaluedMap queryParams, String body ->
        log.debug("deleteScreens() - start")
        def List<FieldScreenVm> screens = mapper.readValue(body, new TypeReference<List<FieldScreenVm>>() {})
        def Collection<DeletedFieldScreenDto> deletedScreens = new ArrayList<>()
        screens.each { screen ->
            def DeletedFieldScreenDto deletedScreenDto = doDeleteScreen(screen)
            deletedScreens.add(deletedScreenDto)
        }
        log.debug("deleteScreens - end")
        return Response.ok(mapper.writeValueAsString(deletedScreens)).build()
}


def doDeleteScreen(FieldScreenVm screen) {
    def FieldScreenManager screenManager = ComponentAccessor.getFieldScreenManager()
    boolean isDeleted = false

    try {
        screenManager.removeFieldScreen(screen.getId().toLong())
        isDeleted = true
    } catch (Exception e) {
        log.error(e)
    }
    return new DeletedFieldScreenDto(screen.getId(), screen.getName(), isDeleted)
}


class FieldScreenVm {
    String id;
    String name;

    FieldScreenVm() {
    }

    FieldScreenVm(String id, String name) {
        this.id = id
        this.name = name
    }

    String getId() {
        return id
    }

    String getName() {
        return name
    }

    void setId(String id) {
        this.id = id
    }

    void setName(String name) {
        this.name = name
    }
}

class DeletedFieldScreenDto {
    String id;
    String name;
    boolean deleted;

    DeletedFieldScreenDto(String id, String name, boolean deleted) {
        this.id = id
        this.name = name
        this.deleted = deleted
    }

    String getId() {
        return id
    }

    String getName() {
        return name
    }

    void setId(String id) {
        this.id = id
    }

    void setName(String name) {
        this.name = name
    }

    boolean getDeleted() {
        return deleted
    }

    void setDeleted(boolean deleted) {
        this.deleted = deleted
    }
}
