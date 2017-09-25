package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import views.html.faq.index;

public class FAQController extends Controller {
    public Result index() {
        return ok(index.render());
    }
}
