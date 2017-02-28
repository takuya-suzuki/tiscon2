package net.unit8.sigcolle.dao;

import net.unit8.sigcolle.DomaConfig;
import net.unit8.sigcolle.model.User;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;

/**
 * @author takahashi
 */
@Dao(config = DomaConfig.class)
public interface UserDao {
    @Insert
    int insert(User user);

    @Select
    User selectByUserId(Long userId);

    @Select
    int countByEmail(String email);

    @Select(ensureResult = true)
    User selectByEmail(String email);

    @Select
    int matchByEmail( Long userId , String email);

    @Select
    boolean getloginFlag( String userId );

    @Update
    int update( User user );
}
