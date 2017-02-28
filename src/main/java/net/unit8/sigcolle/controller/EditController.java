package net.unit8.sigcolle.controller;

import enkan.collection.Multimap;
import enkan.component.doma2.DomaProvider;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;
import net.unit8.sigcolle.auth.LoginUserPrincipal;
import net.unit8.sigcolle.dao.UserDao;
import net.unit8.sigcolle.form.RegisterForm;
import net.unit8.sigcolle.model.User;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;
import static enkan.util.HttpResponseUtils.redirect;

/**
 * @author takahashi
 */
public class EditController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;

    private static final String EMAIL_ALREADY_EXISTS = "このメールアドレスは既に登録されています。";

    /**
     * ユーザー登録画面表示.
     *
     * @return HttpResponse
     */
    public HttpResponse index() {
        return templateEngine.render("user/edit", "user", new RegisterForm());
    }

    /**
     * ユーザー登録処理.
     *
     * @param form 画面入力されたユーザー情報
     * @return HttpResponse
     */
    @Transactional
    public HttpResponse register(RegisterForm form , Session session) {

        if (form.hasErrors()) {
            return templateEngine.render("user/edit", "user", form);
        }

        UserDao userDao = domaProvider.getDao(UserDao.class);
        LoginUserPrincipal principal = (LoginUserPrincipal)session.get("principal");
        User user = userDao.selectByUserId(principal.getUserId());//useridからuserを受け取

        //IDが一緒かつメールが一緒の場合のみ1
        //userIDと入力されたアドレスをデータベースと比較
        //一致した場合はcount=1となる(同盟ユーザのアドレスを認識)よって1の時はif文に入らずそのまま登録する
        //全て不一致の場合0となるはず
        if( userDao.matchByEmail( user.getUserId() , form.getEmail()) != 1)
        {
            // メールアドレス重複チェック
            if (userDao.countByEmail(form.getEmail()) != 0) {
                form.setErrors(Multimap.of("email", EMAIL_ALREADY_EXISTS));
                return templateEngine.render("user/edit",
                        "user", form
                );
            }
        }
        else
        {}

//        User user = builder(new User())
//                .set(User::setLastName, form.getLastName())
//                .set(User::setFirstName, form.getFirstName())
//                .set(User::setEmail, form.getEmail())
//                .set(User::setPass, form.getPass())
//                .build();

        user = builder(user)
                .set(User::setLastName, form.getLastName())
                .set(User::setFirstName, form.getFirstName())
                .set(User::setEmail, form.getEmail())
                .set(User::setPass, form.getPass())
                .build();

        int x = userDao.update(user);

        session.put(
                "principal",
                new LoginUserPrincipal(user.getUserId(), user.getLastName() + " " + user.getFirstName())
        );

        return builder(redirect("/index", SEE_OTHER))
                .set(HttpResponse::setSession, session)
                .build();
    }
}
