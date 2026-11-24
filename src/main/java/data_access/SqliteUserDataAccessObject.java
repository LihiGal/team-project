package data_access;

import entity.User;
import use_case.user.UserDataAccessInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteUserDataAccessObject implements UserDataAccessInterface {

    private final SqliteConnectionProvider connections;

    public SqliteUserDataAccessObject(
            SqliteConnectionProvider connections) {
        this.connections = connections;
    }

    @Override
    public boolean existsByName(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not check username", e);
        }
    }

    @Override
    public void save(User user) {
        String sql = """
                INSERT INTO users (username, password_hash)
                VALUES (?, ?)
                """;

        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not save user", e);
        }
    }

    @Override
    public User get(String username) {
        String sql = """
                SELECT username, password_hash
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new User(
                            result.getString("username"),
                            result.getString("password_hash")
                    );
                }

                return null;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve user", e);
        }
    }
}
