package cj.netos.flow.program;

import cj.netos.flow.ITaskQueue;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.app.GatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;

import java.io.File;

@CjService(name = "$.cj.studio.gateway.app", isExoteric = true)
public class AppSiteProgram extends GatewayAppSiteProgram {
    @CjServiceRef(refByName = "taskQueue")
    ITaskQueue taskQueue;

    @Override
    protected void onstart(Destination dest, String assembliesHome, ProgramAdapterType type) throws CircuitException {
        String homeDir = assembliesHome;
        File homeFile = new File(homeDir);
        homeFile = homeFile.getParentFile().getParentFile();
        taskQueue.init(homeFile.getAbsolutePath(), "queues");
    }
}
