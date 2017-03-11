package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helpers.ResultMessage;
import models.User;
import org.w3c.dom.Document;
import play.libs.Json;
import play.libs.XPath;
import play.mvc.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Security.Authenticated(AuthenticatedCheck.class)
public class UserController extends Controller {

    public Result list(int page, int count) {

        List<User> users = User.findPage(page, count);

        ResultMessage resultMessage = new ResultMessage();

        if (users.isEmpty()){
            return resultMessage.status(404, "2404", false);
        }

        if (request().accepts("application/json")) {
            JsonNode node = Json.toJson(users);
            return ok(node);
        }

        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(users));
        }

        return resultMessage.status(406, "1406", false);
    }

    public Result retrieve(Long id) {
        User user = User.findById(id);

        ResultMessage resultMessage = new ResultMessage();

        if(user == null){
            return resultMessage.status(404, "1404", false);
        }

        if (request().accepts("application/json")) {
            JsonNode node = user.toJson();
            return ok(node);
        }

        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        }

        return resultMessage.status(406, "1406", false);
    }

    public Result create() {

        String firstName;
        String sureName;
        String email;
        String password;

        ResultMessage resultMessage = new ResultMessage();

        if(request().getHeader("Content-type").equals("application/json")){
            try {
                JsonNode body = request().body().asJson();
                firstName = body.get("firstname").asText();
                sureName = body.get("surename").asText();
                email = body.get("email").asText();
                password = body.get("password").asText();
                if (firstName == null || sureName == null || email == null || password == null) {
                    return resultMessage.status(400, "3901", false);
                }
            }catch (NullPointerException e){
                return resultMessage.status(400, "3800", false);
            }
        } else if(request().getHeader("Content-type").equals("application/xml")){
            try {
                Document dom = request().body().asXml();
                if (dom == null){
                    return resultMessage.status(400, "3900", false);
                }else{
                    firstName = XPath.selectText("//firstname", dom);
                    sureName = XPath.selectText("//surename", dom);
                    email = XPath.selectText("//email", dom);
                    password = XPath.selectText("//password", dom);
                    if (firstName == null || sureName == null || email == null || password == null) {
                        return resultMessage.status(400, "3901", false);
                    }
                }
            }catch (Exception e){
                return resultMessage.status(400, "3900", false);
            }
        } else{
            return resultMessage.status(406, "2406", false);
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setSureName(sureName);
        user.setEmail(email);
        user.setWhenAccess();

        try {
            user.setPassword(password);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return resultMessage.status(500, "2250", false);
        }

        user.save();

        if (request().accepts("application/json")) {
            return Results.status(CREATED, user.toJson());
        }

        if (request().accepts("application/xml")) {
            return Results.status(CREATED, views.xml.user.render(user));
        }

        return resultMessage.status(201, "1201", true);
    }

    public Result update(Long id) {
        User user = User.findById(id);

        ResultMessage resultMessage = new ResultMessage();

        if (user == null) {
            return resultMessage.status(404, "1404", false);
        }

        String firstName = request().getQueryString("firstname");
        if (!firstName.isEmpty()){
            user.setFirstName(firstName);
        }

        String sureName = request().getQueryString("surename");
        if (!sureName.isEmpty()){
            user.setSureName(sureName);
        }

        String email = request().getQueryString("email");
        if (!email.isEmpty()){
            user.setEmail(email);
        }

        String pwd = request().getQueryString("pwd");
        if (!pwd.isEmpty()){
            try {
                user.setPassword(pwd);
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                return resultMessage.status(500, "2250", false);
            }
        }

        user.save();

        if (request().accepts("application/json")) {
            return Results.status(200, user.toJson());
        }

        if (request().accepts("application/xml")) {
            return Results.status(200, views.xml.user.render(user));
        }

        return resultMessage.status(200, "2200", true);

    }

    public Result remove(Long id) {
        User user = User.findById(id);

        ResultMessage resultMessage = new ResultMessage();

        if (user == null) {
            return resultMessage.status(404, "1404", false);
        }

        if (user.delete()) {
            return resultMessage.status(200, "1209", true);
        } else {
            return resultMessage.status(500, "1500", false);
        }
    }

}
