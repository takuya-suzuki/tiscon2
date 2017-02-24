package net.unit8.sigcolle.controller;

import enkan.component.doma2.DomaProvider;
import enkan.data.Flash;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;
import net.unit8.sigcolle.auth.LoginUserPrincipal;
import net.unit8.sigcolle.dao.CampaignDao;
import net.unit8.sigcolle.dao.SignatureDao;
import net.unit8.sigcolle.form.CampaignCreateForm;
import net.unit8.sigcolle.form.CampaignForm;
import net.unit8.sigcolle.form.SignatureForm;
import net.unit8.sigcolle.model.Campaign;
import net.unit8.sigcolle.model.Signature;
import net.unit8.sigcolle.model.UserCampaign;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;
import static enkan.util.HttpResponseUtils.redirect;
import static enkan.util.ThreadingUtils.some;

/**
 * @author kawasima
 */
public class CampaignController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;

    /**
     * キャンペーン詳細画面表示.
     * @param form URLパラメータ
     * @param flash flash scope session
     * @return HttpResponse
     */
    public HttpResponse index(CampaignForm form, Flash flash) {
        if (form.hasErrors()) {
            return builder(HttpResponse.of("Invalid"))
                    .set(HttpResponse::setStatus, 400)
                    .build();
        }

        return showCampaign(form.getCampaignIdLong(),
                new SignatureForm(),
                (String) some(flash, Flash::getValue).orElse(null));
    }

    /**
     * 署名の追加処理.
     * @param form 画面入力された署名情報.
     * @return HttpResponse
     */
    @Transactional
    public HttpResponse sign(SignatureForm form) {
        if (form.hasErrors()) {
            return showCampaign(form.getCampaignIdLong(), form, null);
        }
        Signature signature = builder(new Signature())
                .set(Signature::setCampaignId, form.getCampaignIdLong())
                .set(Signature::setName, form.getName())
                .set(Signature::setSignatureComment, form.getSignatureComment())
                .build();
        SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);
        signatureDao.insert(signature);

        return builder(redirect("/campaign/" + form.getCampaignId(), SEE_OTHER))
                .set(HttpResponse::setFlash, new Flash("ご賛同ありがとうございました！"))
                .build();
    }

    /**
     * 新規キャンペーン作成画面表示.
     *
     * @return HttpResponse
     */
    public HttpResponse newCampaign() {
        return templateEngine.render("campaign/new", "form", new CampaignCreateForm());
    }

    /**
     * 新規キャンペーンを作成します.
     * ---------------------------------------
     * FIXME このメソッドは作成途中です.
     *
     * @param form    入力フォーム
     * @param session ログインしているユーザsession
     */
    public HttpResponse create(CampaignCreateForm form,
                               Session session) {
        if (form.hasErrors()) {
            return templateEngine.render("campaign/new", "form", form);
        }
        LoginUserPrincipal principal = (LoginUserPrincipal) session.get("principal");

        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);

        // TODO タイトル, 目標人数を登録する

        Campaign model = builder(new Campaign())
                .set(Campaign::setStatement, processor.markdownToHtml(form.getStatement()))
                .set(Campaign::setCreateUserId, principal.getUserId())
                .set(Campaign::setTitle , form.getTitle())
                .set(Campaign::setGoal , Long.parseLong(form.getGoal()))//string as long
                .build();

        // TODO Databaseに登録する
        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        campaignDao.insert(model);

        // TODO 作成完了した旨のflashメッセージを画面に表示する

        session.put(
                "principal",
                new LoginUserPrincipal(model.getCampaignId() , "作成完了")
        );

        return builder(redirect("/campaign/" + model.getCampaignId(), SEE_OTHER))
                .build();
    }

    /**
     * ログインユーザの作成したキャンペーン一覧を表示します.
     * ---------------------------------------
     * FIXME このメソッドは作成途中です.
     *
     * @param session ログインしているユーザsession
     */
    public HttpResponse listCampaigns(Session session) {
        throw new UnsupportedOperationException("実装してください !!");
    }

    private HttpResponse showCampaign(Long campaignId,
                                      SignatureForm form,
                                      String message) {
        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        UserCampaign campaign = campaignDao.selectById(campaignId);

        SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);
        int signatureCount = signatureDao.countByCampaignId(campaignId);

        return templateEngine.render("campaign/index",
                                     "campaign", campaign,
                                     "signatureCount", signatureCount,
                                     "signature", form,
                                     "message", message
        );
    }
}
