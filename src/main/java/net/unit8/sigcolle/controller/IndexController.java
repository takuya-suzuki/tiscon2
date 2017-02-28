package net.unit8.sigcolle.controller;

import javax.inject.Inject;

import enkan.component.doma2.DomaProvider;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;
import net.unit8.sigcolle.auth.LoginUserPrincipal;
import net.unit8.sigcolle.dao.CampaignDao;
import net.unit8.sigcolle.dao.UserDao;
import net.unit8.sigcolle.model.User;

/**
 * @author takahashi
 */
public class IndexController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;

    public HttpResponse index() {
        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        return templateEngine.render("index", "campaigns", campaignDao.selectAll());
    }
}
