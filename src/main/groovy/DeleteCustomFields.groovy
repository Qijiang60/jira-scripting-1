import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.exception.RemoveException
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
 * This is the endpoint which removes all custom fields passed in the payload.
 *
 * @return Response , response object wrapping custom field removal report.
 * */
deleteCustomFields(httpMethod: "POST", groups: ["jira-administrators"]) {
    MultivaluedMap queryParams, String body ->
        log.debug("deleteCustomFields() - start")
        def List<CustomFieldVm> customFields = mapper.readValue(body, new TypeReference<List<CustomFieldVm>>() {})
        def customFieldDtoList = new ArrayList<>();
        customFields.each { customField ->
            def deletedCustomFieldDto = removeCustomField(customField)
            customFieldDtoList.add(deletedCustomFieldDto)
        }
        log.debug("deleteCustomFields - end")
        return Response.ok(mapper.writeValueAsString(customFieldDtoList)).build()
}

/**
 * Removes a custom field from system.
 *
 * @param customField to be deleted.
 * @return deletedCustomFieldDto to build Response Payload.
 */

def removeCustomField(CustomFieldVm customField) {
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def customFieldTbd = customFieldManager.getCustomFieldObject(customField.id)
    def deleted = false;
    try {
        customFieldManager.removeCustomField(customFieldTbd)
        deleted = true;
    } catch (RemoveException e) {
        log.error(e.getMessage())
    }
    def deletedCustomFieldDto = new DeletedCustomFieldDto(customField.name, customField.id, deleted)
    return deletedCustomFieldDto;
}

class CustomFieldVm {
    String name;
    Long id;

    CustomFieldVm() {
    }

    CustomFieldVm(String name, Long id) {
        this.name = name
        this.id = id
    }

    String getName() {
        return name
    }

    Long getId() {
        return id
    }

    void setName(String name) {
        this.name = name
    }

    void setId(Long id) {
        this.id = id
    }
}

class DeletedCustomFieldDto {
    String name;
    Long id;
    boolean deleted;

    DeletedCustomFieldDto(String name, Long id, boolean deleted) {
        this.name = name
        this.id = id
        this.deleted = deleted
    }

    String getName() {
        return name
    }

    Long getId() {
        return id
    }

    boolean getDeleted() {
        return deleted
    }
}
