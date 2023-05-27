package pcd.assignment2.reactive;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Notifications {
    public static final String NEW_DIRECTORY_VISIT_STARTED = "new-dir-visit-started";
    public static final String NEW_FILE_ANALYSED = "new-file-analysed";


    public static JsonObject newDirectoryVisitStarted(String path) {
        JsonObject obj = new JsonObject();
        obj.put("event", NEW_DIRECTORY_VISIT_STARTED)
            .put("dir_path", path);
        return obj;
    }

    public static JsonObject newFileAnalysed(String path, long nLoC) {
        JsonObject obj = new JsonObject();
        obj.put("event", NEW_FILE_ANALYSED)
            .put("file_path", path)
            .put("nloc", nLoC);
        return obj;
    }
}
