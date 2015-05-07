import java.sql.*;
import java.util.*;

/**
 * Created by seunghyun on 2015-05-07.
 */
class mysql_con {
    public ResultSet db_data() {
        ResultSet result = null;
        try {
            return readDataBase();
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }


    }

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private String path ="";

    mysql_con(String path){
        this.path = path;

    }
    public ResultSet readDataBase() throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection(this.path);

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            System.out.println("connection establish");
            // Result set get the result of the SQL query
            resultSet = statement
                    .executeQuery("select reply,time from humap.hiphop_reply");
            writeResultSet(resultSet);
            return resultSet;

            /*
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into  feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
            // "myuser, webpage, datum, summery, COMMENTS from feedback.comments");
            // Parameters start with 1
            preparedStatement.setString(1, "Test");
            preparedStatement.setString(2, "TestEmail");
            preparedStatement.setString(3, "TestWebpage");
            preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
            preparedStatement.setString(5, "TestSummary");
            preparedStatement.setString(6, "TestComment");
            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("SELECT myuser, webpage, datum, summery, COMMENTS from feedback.comments");
            resultSet = preparedStatement.executeQuery();
            writeResultSet(resultSet);

            // Remove again the insert comment
            preparedStatement = connect
                    .prepareStatement("delete from feedback.comments where myuser= ? ; ");
            preparedStatement.setString(1, "Test");
            preparedStatement.executeUpdate();

            resultSet = statement
                    .executeQuery("select * from feedback.comments");
            writeMetaData(resultSet);
*/
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    private void writeMetaData(ResultSet resultSet) throws SQLException {
        //   Now get some metadata from the database
        // Result set get the result of the SQL query

        System.out.println("The columns in the table are: ");

        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            System.out.println("Column " + i + " " + resultSet.getMetaData().getColumnName(i));
        }
    }

    private void writeResultSet(ResultSet resultSet) throws SQLException {
        // ResultSet is initially before the first data set
        while (resultSet.next()) {
            // It is possible to get the columns via name
            // also possible to get the columns via the column number
            // which starts at 1
            // e.g. resultSet.getSTring(2);
            try {
                String reply = resultSet.getString("reply");
                java.util.Date date = resultSet.getDate("time");
                System.out.println("reply: " + reply);
                System.out.println("Date: " + date);
            } catch (SQLException e) {

                e.printStackTrace();
                continue;
            }
        }
    }

    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }

}
