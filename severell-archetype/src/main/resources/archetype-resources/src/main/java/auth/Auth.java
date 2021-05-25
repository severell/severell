package ${package}.auth;

import ${package}.models.User;
import com.severell.core.crypto.PasswordUtils;
import com.severell.core.http.NeedsRequest;
import io.ebean.DB;

public class Auth extends NeedsRequest {

    public boolean login(String username, String password) {
        User user = DB.find(User.class).where().eq("email", username).findOne();
        boolean auth = false;
        if(user != null) {
            auth = PasswordUtils.checkPassword(password, user.getPassword());
            if(auth) {
                request.session().put("userid", user);
            }
        }

        return auth;
    }

}