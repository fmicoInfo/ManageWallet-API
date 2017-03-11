package controllers;

import helpers.ResultMessage;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;


public class AuthenticatedCheck extends Security.Authenticator{

    @Override
    public String getUsername(Context context) {
        String[] token = context.request().headers().get("X-Auth-Token");

        if ((token != null) && (token.length == 1) && (token[0] != null)) {
            User user = models.User.findByToken(String.join("", token));
            if (user != null) {
                context.args.put("user", user);
                return user.getEmail();
            }
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Context context) {
        ResultMessage resultMessage = new ResultMessage();
        return resultMessage.status(401, "1401", false);
    }

}