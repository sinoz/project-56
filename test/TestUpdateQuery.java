import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public final class TestUpdateQuery {
	public static void main(String[] args) throws Exception {
		Class.forName("org.postgresql.Driver");
		try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/restartdb", "postgres", "postgres")) {
			PreparedStatement stmt = conn.prepareStatement("UPDATE gameaccounts SET title=?,description=?,canbuy=?,buyprice=?,cantrade=?,maillast=?,mailcurrent=?,passwordcurrent=? WHERE id=?;");
			stmt.setString(1, "TITLE");
			stmt.setString(2, "DESCR");
			stmt.setBoolean(3, true);
			stmt.setDouble(4, 1);
			stmt.setBoolean(5, true);
			stmt.setString(6, "XD");
			stmt.setString(7, "XDD");
			stmt.setString(8, "XDDD");
			stmt.setInt(9, 1);

			try {
				stmt.execute();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
}
