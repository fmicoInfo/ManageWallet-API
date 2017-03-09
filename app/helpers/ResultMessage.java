package helpers;

import play.data.Form;
import play.i18n.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.twirl.api.Xml;

import play.mvc.Result;
import play.mvc.Results;

import static play.mvc.Controller.request;

public class ResultMessage {

    private String getMsg(String code){
        return Messages.get("api." + code);
    }

    private JsonNode getJson(String code, String type, String msg){
        ObjectNode objectError = play.libs.Json.newObject();
        objectError.put("code", code);
        objectError.put("state", type);
        objectError.put("description", msg);
        return objectError;
    }

    private Xml getXML(String code, String type, String msg){
        return views.xml.result_message.render(code, type, msg);
    }

    public Result status(int htmlCode, String apiCode, Boolean ok){
        String type = (ok)?"OK":"Error";

        if (request().accepts("application/json")) {
            return Results.status(htmlCode, getJson(apiCode, type, getMsg(apiCode)));
        }

        if (request().accepts("application/xml")) {
            return Results.status(htmlCode, getXML(apiCode, type, getMsg(apiCode)));
        }

        return Results.status(htmlCode, "<p>Code: " + apiCode + "</p>" + "<p>Type: " + type + "</p>" + "<p>Message: " + getMsg(apiCode) + "</p>");

    }

    public Result status(Form<Object> form){

        if (request().accepts("application/json")) {
            return Results.badRequest(form.errorsAsJson());
        }

        if (request().accepts("application/xml")) {
            return Results.status(400, views.xml.result_message_form.render(form));
        }

        return Results.status(400, "<p>" + getMsg("1400") + "</p>");

    }


}
