import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenManager
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager
import com.atlassian.jira.workflow.JiraWorkflow
import com.atlassian.jira.workflow.WorkflowActionsBean
import com.atlassian.jira.workflow.WorkflowManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper

import javax.ws.rs.core.Response
import java.util.function.Predicate

@BaseScript CustomEndpointDelegate endpointDelegate
log = Logger.getLogger("com.acme.CreateSubtask")
log.setLevel(Level.INFO)
def mapper = new ObjectMapper()

/**
 * This is the endpoint that wraps the logic of finding all screen which are only associated with any workflow
 * transitions.
 * @return Response response object which wraps all unused issue types in a JSON array.
 */
getUnusedScreens(httpMethod: "GET", groups: ["jira-administrators"]) {
    def unusedScreens = doGetUnusedScreens()
    return Response.ok(mapper.writeValueAsString(unusedScreens)).build()
}

/**
 * Retrieve all unused screens.
 *
 * @return all unused screens.
 *
 * */
def doGetUnusedScreens() {
    def FieldScreenManager screenManager = ComponentAccessor.getFieldScreenManager()
    List<FieldScreenDto> screensNotInAnyWorkflow = getScreensNotInAnyWorkflow()
    List<FieldScreenDto> screensNotInAnyScheme = getScreensNotInAnyScheme()
    List<FieldScreenDto> unusedScreens = new LinkedList<>()

    screenManager.getFieldScreens().each {
        screen ->
            def FieldScreenDto screenDto = new FieldScreenDto(screen.getId().toString(), screen.getName())
            if (screensNotInAnyWorkflow.contains(screenDto) && screensNotInAnyScheme.contains(screenDto)) {
                unusedScreens.add(screenDto)
            }
    }

    return unusedScreens;
}

def getScreensNotInAnyScheme() {
    def FieldScreenSchemeManager fieldScreenSchemeManager = ComponentAccessor.getComponent(FieldScreenSchemeManager
            .class)
    def FieldScreenManager screenManager = ComponentAccessor.getFieldScreenManager()
    def List<FieldScreenDto> screens = screenManager.getFieldScreens().collect {
        fs -> new FieldScreenDto(fs.getId().toString(), fs.getName())
    }

    fieldScreenSchemeManager.getFieldScreenSchemes().each {
        scheme ->
            scheme.getFieldScreenSchemeItems().each {
                fsi ->
                    String screenId = fsi.getFieldScreenId().toString()
                    screens.removeIf({ FieldScreenDto fs -> fs.id == screenId } as Predicate<FieldScreenDto>)
            }
    }
    return screens
}

def getScreensNotInAnyWorkflow() {
    def WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager()
    def FieldScreenManager screenManager = ComponentAccessor.getFieldScreenManager()
    def List<FieldScreenDto> screens = screenManager.getFieldScreens().collect {
        fs -> new FieldScreenDto(fs.getId().toString(), fs.getName())
    }

    workflowManager.getWorkflows().each {
        workflow ->
            workflow.getAllActions().each {
                action ->
                    WorkflowActionsBean workflowActionsBean = new WorkflowActionsBean();
                    def FieldScreen fs = workflowActionsBean.getFieldScreenForView(action);
                    log.info('Wokrflow: '+ workflow.name + ' action:'+ action.name + ' action.getMetaAttributes() : ' + action.getMetaAttributes().toString())
                    if(fs !=null ){
                        def screenId = fs.getId().toString()
                        screens.removeIf({ FieldScreenDto fs1 -> fs1.id == screenId } as Predicate<FieldScreenDto>)
                    }
            }
    }
    return screens;
}

class FieldScreenDto {
    String id;
    String name;

    FieldScreenDto(String id, String name) {
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        FieldScreenDto that = (FieldScreenDto) o

        if (id != that.id) return false
        if (name != that.name) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        return result
    }

    @Override
    public String toString() {
        return "FieldScreenDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}