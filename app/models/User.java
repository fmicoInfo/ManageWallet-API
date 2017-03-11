package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import play.libs.Json;

import javax.persistence.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class User extends Audit{

    private static final int EXPIRE_SESSION_SECONDS = 86400;

    @Constraints.MinLength(message = "validation.min-length", value = 3)
    @Constraints.MaxLength(message = "validation.max-length", value = 40)
    @Constraints.Required(message = "validation.required")
    private String firstName;

    @Constraints.MinLength(message = "validation.min-length", value = 3)
    @Constraints.MaxLength(message = "validation.max-length", value = 40)
    @Constraints.Required(message = "validation.required")
    private String sureName;

    private String token;

    @Constraints.Required(message = "validation.required")
    @Constraints.Email(message = "validation.email")
    @Column(unique = true)
    private String email;

    private Timestamp whenAccess;

    @Transient
    @Constraints.MinLength(message = "validation.min-length", value = 8)
    @Constraints.MaxLength(message = "validation.max-length", value = 30)
    @Constraints.Required(message = "validation.required")
    @JsonIgnore
    private String password;

    @JsonIgnore
    private byte[] cryptPassword;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSureName() {
        return sureName;
    }

    public void setSureName(String sureName) {
        this.sureName = sureName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public void setWhenAccess(){
        this.whenAccess = timestampNow();
    }

    private static Timestamp timestampNow(){
        return new Timestamp(System.currentTimeMillis());
    }

    public String getWhenAccess() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(whenAccess);
    }

    public void setPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.password = password;
        this.cryptPassword = getSHA(password);
    }

    private void setToken() {
        this.token = UUID.randomUUID().toString();
        this.setWhenAccess();
        save();
    }

    public String getToken() {
        return token;
    }

    public void deleteToken(){
        this.token = null;
        save();
    }

    private static byte[] getSHA(String password){

        try {
            return MessageDigest.getInstance("SHA-512").digest(password.getBytes("UTF-8"));
        }catch (Exception e){
            return null;
        }

    }

    private static boolean isTokenValid(Timestamp whenAccess){

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            Date dateWhenAccess = df.parse(df.format(whenAccess));
            Date dateNow = df.parse(df.format(timestampNow()));

            long diff = Math.abs(dateWhenAccess.getTime() - dateNow.getTime()) / 1000;

            if (diff <= EXPIRE_SESSION_SECONDS){
                return true;
            }

        } catch (ParseException e) {
            return false;
        }

        return false;

    }

    public JsonNode toJson() {
        return Json.toJson(this);
    }

    public JsonNode toJsonWithToken() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = Json.toJson(this);

        ((ObjectNode) jsonNode).put("token", this.token);

        return jsonNode;
    }

    private static final Find<Long, User> find = new Find<Long, User>() {
    };

    public static User findById(Long id) {
        return find.byId(id);
    }

    public static User findByLogin(String email, String password){

        User user = find.where().eq("email", email).eq("cryptPassword", getSHA(password)).findUnique();

        if (user != null)
            user.setToken();

        return user;
    }

    public static User findByToken(String token) {
        if (token == null) {
            return null;
        }
        try  {
            User user = find.where().eq("token", token).findUnique();

            if (user == null){
                return null;
            }

            if (isTokenValid(user.whenAccess)){
                user.setWhenAccess();
                user.save();
                return user;
            }

            return null;

        }
        catch (Exception e) {
            return null;
        }
    }

    public static boolean isEmpty(){
        return find.all().isEmpty();
    }

    public static int size(){
        return find.all().size();
    }

    public static List<User> findPage(Integer page, Integer count) {
        return find.orderBy("firstName").setFirstRow(page * count).setMaxRows(count).findList();
    }

}
