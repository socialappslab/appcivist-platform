package http;

import play.libs.F.Promise;
import play.mvc.Action.Simple;
import play.mvc.Http.Context;
import play.mvc.Result;

public class Headers extends Simple {
	public Promise<Result> call(final Context ctx) throws Throwable {
        ctx.response().setHeader("Access-Control-Allow-Origin", "*"); 
        return delegate.call(ctx);
    }
}
