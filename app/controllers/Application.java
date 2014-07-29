package controllers;

import java.util.List;

import models.DnsEntry;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {
  
    public static Result index() {
    	List<DnsEntry> entries = DnsEntry.find.all();
        return ok(index.render(entries));
    }
  
}
