package net.unit8.sigcolle;

import enkan.Application;
import enkan.Endpoint;
import enkan.application.WebApplication;
import enkan.config.ApplicationFactory;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.endpoint.ResourceEndpoint;
import enkan.middleware.*;
import enkan.middleware.devel.HttpStatusCatMiddleware;
import enkan.middleware.devel.StacktraceMiddleware;
import enkan.middleware.devel.TraceWebMiddleware;
import enkan.middleware.doma2.DomaTransactionMiddleware;
import enkan.security.backend.SessionBackend;
import enkan.system.inject.ComponentInjector;
import kotowari.middleware.*;
import kotowari.middleware.serdes.ToStringBodyWriter;
import kotowari.routing.Routes;
import net.unit8.sigcolle.controller.*;

import java.util.Collections;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.RedirectStatusCode.TEMPORARY_REDIRECT;
import static enkan.util.HttpResponseUtils.redirect;
import static enkan.util.Predicates.*;

/**
 * @author [It's you]
 */
public class SigColleApplicationFactory implements ApplicationFactory {
    @Override
    public Application create(ComponentInjector injector) {
        WebApplication app = new WebApplication();

        Routes routes = Routes.define(r -> {
            // public
            r.get("/").to(LoginController.class, "index");//トップページをログイン画面と同様に
            r.get("/index").to(IndexController.class, "index");
            r.get("/campaign/:campaignId").to(CampaignController.class, "index");
            r.post("/campaign/:campaignId").to(CampaignController.class, "sign");
            r.get("/campaign/:campaignId/signatures").to(SignatureController.class, "list");
            r.get("/register").to(RegisterController.class, "index");
            r.post("/register").to(RegisterController.class, "register");
            r.get("/login").to(LoginController.class, "index");
            r.post("/login").to(LoginController.class, "login");
            r.get("/logout").to(LoginController.class, "logout");
            // authenticated(see middleware configuration)
            r.get("/auth/campaign").to(CampaignController.class, "newCampaign");
            r.post("/auth/campaign").to(CampaignController.class, "create");
            r.get("/auth/user/campaigns").to(CampaignController.class, "listCampaigns");
            r.get("/auth/user/edit").to(EditController.class,"index");
            r.post("/auth/user/edit").to(EditController.class, "register");
        }).compile();

        app.use(new DefaultCharsetMiddleware());
        app.use(NONE, new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
        app.use(envIn("development"), new StacktraceMiddleware());
        app.use(envIn("development"), new TraceWebMiddleware());
        app.use(new TraceMiddleware<>());
        app.use(new ContentTypeMiddleware());
        app.use(envIn("development"), new HttpStatusCatMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new MultipartParamsMiddleware());
        app.use(new MethodOverrideMiddleware());
        app.use(new NormalizationMiddleware());
        app.use(new NestedParamsMiddleware());
        app.use(new CookiesMiddleware());
        app.use(new SessionMiddleware());
        app.use(new FlashMiddleware());
        app.use(new ContentNegotiationMiddleware());

        app.use(new AuthenticationMiddleware(Collections.singletonList(new SessionBackend())));

        app.use(new ResourceMiddleware());
        app.use(new RenderTemplateMiddleware());
        app.use(new RoutingMiddleware(routes));

        app.use(and(path("^/auth/.*"), authenticated().negate()),
                (Endpoint<HttpRequest, HttpResponse>) req -> redirect("/login", TEMPORARY_REDIRECT));

        app.use(new DomaTransactionMiddleware<>());
        app.use(new FormMiddleware());
        app.use(builder(new SerDesMiddleware())
                .set(SerDesMiddleware::setBodyWriters, new ToStringBodyWriter())
                .build());
        app.use(new ValidateFormMiddleware());
        app.use(new ControllerInvokerMiddleware(injector));

        return app;
    }
}
