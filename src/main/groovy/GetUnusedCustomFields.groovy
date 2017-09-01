import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response


@BaseScript CustomEndpointDelegate endpointDelegate
log = Logger.getLogger("*")
def mapper = new ObjectMapper();

/**
 * This is the endpoint that wraps the logic of finding all custom fields which are not associated to screen.
 * @return Response response object which wraps all unused custom fields JSON array.
 */
getUnusedCustomFields(httpMethod: "GET", groups: ["jira-administrators"]) {
    MultivaluedMap queryParams, String body ->
        log.debug("getUnusedCustomFields() - start")
        def customFields = doGetAllUnusedCustomFields()
        log.debug("getUnusedCustomFields() - end")
        return Response.ok(mapper.writeValueAsString(customFields)).build();
}

/**
 * Gets all unused custom fields.
 *
 * @return unusedCustomFields list of all unused custom fields.
 */

def doGetAllUnusedCustomFields() {
    log.debug("doGetAllUnusedCustomFields() - start")
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def fieldScreenManager = ComponentAccessor.getFieldScreenManager();
    def fieldScreens = fieldScreenManager.getFieldScreens();
    def customFields = customFieldManager.getCustomFieldObjects();

    def unusedCustomFields = customFields.findAll({
        customField ->
            !isAssociatedWithAnyScreen(customField, fieldScreens)
    })
    log.debug("doGetAllUnusedCustomFields() - end")
    return unusedCustomFields.collect(new ArrayList<CustomFieldDto>(), { cf -> new CustomFieldDto(cf.name, cf
            .idAsLong) })
}
/**
 * Check if given custom field is associated with any screen.
 *
 * @param customField
 * @param fieldScreens , list of all field screen in system.
 * @return boolean true if custom field is associated with any screen, else return false.
 */
def isAssociatedWithAnyScreen(CustomField customField, List<FieldScreen> fieldScreens) {
    log.debug("isAssociatedWithAnyScreen() - start")
    def isAssociated = false;
    isAssociated = fieldScreens.any { fieldScreen -> fieldScreen.containsField(customField.getId()) }
    log.info("isAssociatedWithAnyScreen() - Custom Field : " + customField.getName() + ", isAssociated? " +
            isAssociated)
    log.debug("isAssociatedWithAnyScreen() - end")
    return isAssociated;
}

class CustomFieldDto {
    String name;
    Long id;

    CustomFieldDto() {
    }

    CustomFieldDto(String name, Long id) {
        this.name = name
        this.id = id
    }

    String getName() {
        return name
    }

    Long getId() {
        return id
    }
}
