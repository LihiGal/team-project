package data_access;

import entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SqliteUserDataAccessObjectTest {

    @TempDir
    Path directory;

    private SqliteUserDataAccessObject createDao()
            throws SQLException {
        var connections = new SqliteConnectionProvider(
                directory.resolve("game.db"));

        new SchemaInitializer(connections).initialize();

        return new SqliteUserDataAccessObject(connections);
    }

    @Test
    void savesAndRetrievesUser() throws SQLException {
        var dao = createDao();

        dao.save(new User("Alex", "testHash"));

        assertTrue(dao.existsByName("Alex"));

        User user = dao.get("Alex");

        assertNotNull(user);
        assertEquals("Alex", user.getUsername());
        assertEquals("testHash", user.getPasswordHash());
    }

    @Test
    void returnsNullForMissingUser() throws SQLException {
        var dao = createDao();

        assertFalse(dao.existsByName("Unknown"));
        assertNull(dao.get("Unknown"));
    }

    @Test
    void rejectsDuplicateUsername() throws SQLException {
        var dao = createDao();

        dao.save(new User("Alex", "originalHash"));

        assertThrows(IllegalStateException.class,
                () -> dao.save(new User("Alex", "newHash")));

        assertEquals("originalHash",
                dao.get("Alex").getPasswordHash());
    }

    @Test
    void preservesUserAfterReopeningDatabase()
            throws SQLException {
        var dao = createDao();

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher();

        String hash = hasher.hash("apple123");

        dao.save(new User("Alex", hash));

        var reopenedDao = createDao();

        assertTrue(reopenedDao.existsByName("Alex"));

        User user = reopenedDao.get("Alex");

        assertNotNull(user);
        assertEquals(hash, user.getPasswordHash());
        assertTrue(hasher.verify("apple123",
                user.getPasswordHash()));
        assertFalse(hasher.verify("wrongPassword",
                user.getPasswordHash()));
    }
}