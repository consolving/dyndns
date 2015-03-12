package controllers;

import java.io.IOException;
import java.io.StringWriter;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kenshoo.play.metrics.MetricsRegistry;

import fileauth.actions.BasicAuth;

@BasicAuth
public class Metrics extends Controller {

	public static Result metrics() {
	    ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
	    StringWriter stringWriter = new StringWriter();
	    try {
	    	response().setHeader(CACHE_CONTROL, "must-revalidate,no-cache,no-store");
			writer.writeValue(stringWriter, MetricsRegistry.defaultRegistry());
			return ok(stringWriter.toString()).as("application/json");
		} catch (IOException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
			return status(500, "error rendering Metrics");
		}
	}
}