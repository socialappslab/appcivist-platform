package http;

import play.mvc.Action.Simple;
import play.*;
import play.libs.Yaml;
import play.mvc.Action;
import play.mvc.Call;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Results;
import play.libs.F.Promise;

public class Headers extends Simple {
	public Promise<Result> call(final Context ctx) throws Throwable {
        ctx.response().setHeader("Access-Control-Allow-Origin", "*"); 
        return delegate.call(ctx);
    }
}
