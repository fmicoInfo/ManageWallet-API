package controllers;


import helpers.ResultMessage;
import models.User;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.*;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class LoginController extends Controller {

    @Inject
    private FormFactory formFactory;

    public Result login() {
        Form<Login> form = formFactory.form(Login.class).bindFromRequest();

        String email = form.get().email;
        String password = form.get().password;

        User user = User.findByLogin(email, password);

        ResultMessage resultMessage = new ResultMessage();

        if (user == null){
            return resultMessage.status(401,"1401", false);
        }

        if (form.hasErrors()) {
            return resultMessage.status(400, "1400", false);
            //return resultMessage.getErrorResult(400, "1400");
        }

        if (request().accepts("application/json")) {
            return Results.status(ACCEPTED, user.toJsonWithToken());
        }

        if (request().accepts("application/xml")) {
            return Results.status(ACCEPTED, views.xml.login.render(user));
        }

        return resultMessage.status(406, "1406", false);
    }

    public Result firstUser(){

        ResultMessage resultMessage = new ResultMessage();

        if (!User.isEmpty()){
            return resultMessage.status(401, "1009", false);
        }

        User user = new User();
        user.setFirstName("name");
        user.setSureName("first");
        user.setEmail("name@domain.com");
        user.setWhenAccess();

        try {
            user.setPassword("12345678");
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

    @Security.Authenticated(AuthenticatedCheck.class)
    public Result logout(){
        response().discardCookie("X-Auth-Token");
        User user = (User) Http.Context.current().args.get("user");
        user.deleteToken();
        return new ResultMessage().status(401, "3401", true);
    }

    public static class Login {

        @Constraints.Required
        @Constraints.Email
        public String email;

        @Constraints.Required
        public String password;

    }

}
