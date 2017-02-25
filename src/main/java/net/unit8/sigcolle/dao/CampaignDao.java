package net.unit8.sigcolle.dao;

import java.util.List;

import net.unit8.sigcolle.DomaConfig;
import net.unit8.sigcolle.model.Campaign;
import net.unit8.sigcolle.model.UserCampaign;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;

/**
 * @author kawasima
 */
@Dao(config = DomaConfig.class)
public interface CampaignDao {
    @Select(ensureResult = true)
    UserCampaign selectById(Long campaignId);

    @Select
    List<Campaign> selectbyUserId( String createUserId );

    @Select
    List<Campaign> selectAll();

    @Insert
    int insert(Campaign campaign);
}
