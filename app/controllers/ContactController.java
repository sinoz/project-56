package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import views.html.contact.index;

public class ContactController extends Controller {
    public Result index() {
        return ok(index.render());
    }
}