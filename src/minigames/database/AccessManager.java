package minigames.database;

import arc.struct.Seq;

import java.sql.*;
import java.util.Properties;

public class AccessManager {
    private final String DB_Address = "Jdbc:Odbc:Mindustry";
    private final String DB_AdminID = "admin";
    private final String DB_AdminPassword = "kjisdk0!";

    Connection conn = null;

    public Connection getConnection() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            Properties props = new Properties();
            props.put("charSet", "8859_1" );
            props.put("user", DB_AdminID);
            props.put("password", DB_AdminPassword);

            conn = DriverManager.getConnection(DB_Address, props);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public Seq<String> Select(String table, Where term, String... columns) {
        Seq<String> result = Seq.with();
        PreparedStatement psmt = null;
        ResultSet rs = null;
        return result;
    }

    public class Where {
        private final String term, column;
        public boolean UPPER = false;
        protected final Seq<Where> ANDs = Seq.with();
        protected final Seq<Where> ORs = Seq.with();
        private static final Seq<String> operators = Seq.with("=", "<>", "<", ">", "<=", ">=");

        public Where(String column, String term) {
            this.column = column;
            this.term = term;
        }
        /**
         * ex) new Where("element", "target", true) <br/>
         * SELECT * FROM * WHERE element='target'
        * @param operator true(=) / false(<>)
        */
        public Where(String column, String target, boolean operator) {
            this.column = column;
            term = (operator ? "=" : "<>") + "'" + target + "'";
        }

        /**
         * ex) new Where("element", 5, "=") <br/>
         * SELECT * FROM * WHERE element=5
         * @param operator =, <>, <, >, <=, >= / 아닐시에는 "" 반환
         */
        public Where(String column, int target, String operator) {
            this.column = column;
            term = operators.find(s -> s.equals(operator)) != null ? operator + target : "";
        }

        /**
         * ex) new Where("column", 1, A) <br/>
         * SELECT * FROM * WHERE column LIKE '%A%'
         * @param index 0, 1, 2 / 아닐시에는 "" 반환
         */
        public Where(String column, String target, int index) {
            this.column = column;
            term = index > -1 && index < 3 ? ( " LIKE '" + (index != 0 ? "%" : "") + target + (index != 2 ? "%" : "") + "'" ) : "";
        }

        /**
         * ex) new Where("column", A, 2) <br/>
         * SELECT * FROM * WHERE column LIKE 'A_'
         * @param length > 1 / 아닐시에는 "" 반환
         */
        public Where(String column, char target, int length) {
            this.column = column;
            term = length > 1 ? " LIKE '" + target + "_".repeat(length - 1) + "'" : "";
        }

        public String getTerm() {
            return UPPER ? "UPPER(" + column + ")" + term : column + term;
        }

        public Where addAND(Where term) {
            ANDs.add(term);
            return this;
        }

        public Where addOR(Where term) {
            ORs.add(term);
            return this;
        }
    }

}
