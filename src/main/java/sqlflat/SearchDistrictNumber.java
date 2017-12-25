package sqlflat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SearchDistrictNumber {
    public int searchDistrictNumber(Statement statement, String district) throws SQLException {
        int dist_id=0;
        ResultSet resultSet1 = statement.executeQuery("SELECT  * FROM  flatbase.districts");
        while (resultSet1.next()){
            String n=  resultSet1.getString("name_distr");
            if(n.equals(district)){
                dist_id = resultSet1.getInt("district_id");
            }
        }
        resultSet1.close();
        return dist_id;
    }
}
