package sqlflat;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/flatbase?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "7RootPEugene47";

    private static Connection conn;
    public static void main(String[] args) {
        try {
            try (Scanner sc = new Scanner(System.in)) {
                // create connection
                conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
                initDB();
                while (true) {
                    System.out.println("1: Add apartment");
                    System.out.println("2: Find apartments in the districts");
                    System.out.println("3: Delete apartment");
                    System.out.println("4: Change price apartment");
                    System.out.println("5: View all of apartments of the Property Base ");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addApartment(sc);
                            break;
                        case "2":
                            SearchApartmentInDistrict(sc);
                            break;
                        case "3":
                            deleteApartment(sc);
                            break;
                        case "4":
                            changeApartment(sc);
                            break;
                        case "5":
                            viewApartments();
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
//этот блок как эксперментальный: если базы и таблиц нет... сколько строк в таблице.... foreign key определить в запросе...
    private static void initDB() throws SQLException {
        int count=0;
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS districts(district_id INT(10) NOT NULL AUTO_INCREMENT,name_distr VARCHAR(45) NOT NULL, PRIMARY KEY (district_id))  ENGINE = InnoDB  DEFAULT CHARACTER SET = utf8");
            st.execute("CREATE TABLE IF NOT EXISTS apartment(flat_id INT NOT NULL AUTO_INCREMENT,address VARCHAR(45) NOT NULL ,square DOUBLE  NOT NULL,room INT(10) NOT NULL,price DOUBLE NOT NULL,district_id INT(10) NOT NULL , PRIMARY KEY (flat_id), FOREIGN KEY (district_id) REFERENCES flatbase.districts(district_id))  ENGINE = InnoDB  DEFAULT CHARACTER SET = utf8");
            ResultSet rs = st.executeQuery("SELECT count(district_id)FROM flatbase.districts");
            while (rs.next()) {
                count = rs.getInt(1);
            }
            if (count != 10) {
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (1, 'Pecherskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (2, 'Darnytskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (3, 'Desnianskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (4, 'Dniprovskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (5, 'Obolonskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (6, 'Podilskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (7, 'Shevchenkivskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (8, 'Solomianskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (9, 'Holosiivskyi')");
                st.execute("INSERT INTO flatbase.districts(district_id, name_distr) VALUES (10, 'Sviatoshynskyi')");
            }

        }
    }

    private static void addApartment(Scanner sc) throws SQLException {
        System.out.print("Enter address: ");
        String address = sc.nextLine();

        System.out.print("Enter square: ");
        String square = sc.nextLine();
        double sq = Double.parseDouble(square);

        System.out.print("Enter room: ");
        String room = sc.nextLine();
        int rm = Integer.parseInt(room);

        System.out.print("Enter price: ");
        String price = sc.nextLine();
        double pr = Double.parseDouble(price);

        System.out.print("Enter district: ");
        String district = sc.nextLine();
//поиск номера района по строке введенной с консоли
        Statement statement = conn.createStatement();
        int district_id=new SearchDistrictNumber().searchDistrictNumber(statement, district);
        statement.close();

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO flatbase.apartment (address, square, room, price, district_id) VALUES (?,?,?,?,?)")) {
            ps.setString(1, address);
            ps.setDouble(2, sq);
            ps.setInt(3, rm);
            ps.setDouble(4, pr);
            ps.setInt(5, district_id);

            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        }
    }
    private static void SearchApartmentInDistrict(Scanner sc) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT  * FROM  flatbase.districts");
        System.out.println("Enter the name of the district are listed below: ");
        while (resultSet.next()){
            String n=  resultSet.getString("name_distr");
            System.out.print(n+" - ");
            }
        resultSet.close();
        System.out.println();
        String district = sc.nextLine();
//    поиск номера района по строке введенной с консоли
        int dist_id = new SearchDistrictNumber().searchDistrictNumber(statement, district);
        statement.close();

        try (PreparedStatement ps = conn.prepareStatement("select apartment.flat_id, apartment.address,apartment.square, apartment.room,apartment.price, districts.name_distr, apartment.district_id from apartment, districts where districts.district_id=? and apartment.district_id=?")) {
            ps.setInt(1, dist_id);
            ps.setInt(2, dist_id);
            try (ResultSet rs = ps.executeQuery()) {
                new PrintTableInConsole().printTable(rs);
            }
        }
    }
    private static void deleteApartment(Scanner sc) throws SQLException {
        viewApartments();
        System.out.print("Enter the ID of the apartment you want to remove from the database: ");
        String fl= sc.nextLine();
        int fl_id = Integer.parseInt(fl);
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from apartment where flat_id=?")) {
            ps.setInt(1, fl_id);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        }
    }
    private static void changeApartment(Scanner sc) throws SQLException {
        viewApartments();
        System.out.print("Enter the ID of the apartment you want to change price: ");
        String fl= sc.nextLine();
        int fl_id = Integer.parseInt(fl);

        System.out.print("Enter price: ");
        String price = sc.nextLine();
        double pr = Double.parseDouble(price);
        try (PreparedStatement ps = conn.prepareStatement("UPDATE flatbase.apartment SET  price=? WHERE flat_id=?")) {
            ps.setDouble(1, pr);
            ps.setInt(2, fl_id);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        }
    }
    private static void viewApartments( ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("select  flat_id, address,square, room,price, districts.name_distr, apartment.district_id from apartment, districts  where districts.district_id=apartment.district_id")) {
            try (ResultSet rs = ps.executeQuery()) {
                new PrintTableInConsole().printTable(rs);
            }
        }
        }
    }

