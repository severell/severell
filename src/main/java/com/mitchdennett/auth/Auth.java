package com.mitchdennett.auth;

import com.mitchdennett.framework.crypto.PasswordUtils;
import com.mitchdennett.framework.http.NeedsRequest;
import com.mitchdennett.models.User;

import java.util.List;

public class Auth extends NeedsRequest {

    public boolean login(String username, String password) {
        List<User> list = User.where("email = '"  + username + "'");
        boolean auth = false;
        if(list != null && list.size() > 0) {
            User user = list.get(0);
            auth = PasswordUtils.checkPassword(password, user.getString("password"));
            if(auth) {
                request.getSession().setAttribute("userid", user.get("id"));
            }
        }

        return auth;
    }

}
