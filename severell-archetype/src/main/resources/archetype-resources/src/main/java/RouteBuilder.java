package ${package};

import com.severell.core.http.MiddlewareExecutor;
import com.severell.core.http.RouteExecutor;

import java.util.ArrayList;

public class RouteBuilder {
  public ArrayList<RouteExecutor> build() {return new ArrayList<>();}

  public ArrayList<MiddlewareExecutor> buildDefaultMiddleware() {
      return new ArrayList<>();
  }
}
