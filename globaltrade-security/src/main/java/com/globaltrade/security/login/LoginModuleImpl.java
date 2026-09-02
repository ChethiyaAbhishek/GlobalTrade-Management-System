package com.globaltrade.security.login;


import com.globaltrade.security.encryption.PasswordHasher;
import com.sun.appserv.security.AppservPasswordLoginModule;
import com.sun.enterprise.security.auth.login.common.LoginException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginModuleImpl extends AppservPasswordLoginModule {

    private static final Logger logger = Logger.getLogger(LoginModuleImpl.class.getName());

    private static final String USER_QUERY = "SELECT id, password_hash, is_active FROM user WHERE username = ?";

    private static final String ROLES_QUERY = "SELECT r.role_name FROM role r " +
            "JOIN assigned_role ar ON r.id = ar.role_id WHERE ar.user_id = ?";

    @Override
    protected void authenticateUser() throws LoginException {

        String username = getUsername();
        String password = getPassword();

        if (username == null || username.trim().isEmpty() || password == null) {
            throw new LoginException("Username and password are required");
        }

        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/_GlobalTrade");

            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(USER_QUERY)) {

                ps.setString(1, username.trim());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        logger.warning("Login failed: user not found: " + username);
                        throw new LoginException("Invalid username or password");
                    }

                    long userId = rs.getLong("id");
                    String storedHash = rs.getString("password_hash");
                    boolean active = rs.getBoolean("is_active");

                    if (!active) {
                        logger.warning("Login failed: account disabled: " + username);
                        throw new LoginException("Account is disabled");
                    }

                    if (!PasswordHasher.verify(password, storedHash)) {
                        logger.warning("Login failed: invalid password: " + username);
                        throw new LoginException("Invalid username or password");
                    }

                    List<String> groups = new ArrayList<>();
                    try (PreparedStatement psRoles = conn.prepareStatement(ROLES_QUERY)) {

                        psRoles.setLong(1, userId);

                        try (ResultSet rsRoles = psRoles.executeQuery()) {
                            while (rsRoles.next()) {
                                groups.add(rsRoles.getString("role_name"));
                            }
                        }
                    }

                    logger.info("Login successful: " + username + " roles:" + groups);

                    commitUserAuthentication(groups.toArray(new String[0]));
                }
            }
        } catch (LoginException le) {
            throw le;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected login error", e);
            throw new LoginException("Authentication system error: " + e.getMessage());
        }
    }
}
