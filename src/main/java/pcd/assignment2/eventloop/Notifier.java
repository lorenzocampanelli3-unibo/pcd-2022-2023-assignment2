package pcd.assignment2.eventloop;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Notifier {
    public static final String NEW_DIRECTORY_VISIT_STARTED = "new-dir-visit-started";
    public static final String NEW_FILE_ANALYSED = "new-file-analysed";

    private Vertx vertx;
    private String address;

    public Notifier(Vertx vertx, String address) {
        this.vertx = vertx;
        this.address = address;
    }

    public void notifyDirectoryVisitStarted(String path) {
        JsonObject obj = new JsonObject();
        obj.put("event", NEW_DIRECTORY_VISIT_STARTED)
            .put("dir_path", path);
        vertx.eventBus().publish(address, obj);
    }

    public void notifyFileAnalysed(String path, long nLoC) {
        JsonObject obj = new JsonObject();
        obj.put("event", NEW_FILE_ANALYSED)
            .put("file_path", path)
            .put("nloc", nLoC);
        vertx.eventBus().publish(address, obj);
    }
}
