package it.vitalegi.structurizr.md;

import com.structurizr.Workspace;
import com.structurizr.view.View;
import it.vitalegi.structurizr.md.markdown.LandscapePageService;
import it.vitalegi.structurizr.md.markdown.SoftwareSystemPage;
import it.vitalegi.structurizr.md.model.MdContext;
import it.vitalegi.structurizr.md.service.C4PlantUmlExporter;
import it.vitalegi.structurizr.md.service.ViewGenerator;
import it.vitalegi.structurizr.md.util.FileUtil;
import it.vitalegi.structurizr.md.util.StructurizrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class GenerateMarkdownApp {

    private static final Logger log = LoggerFactory.getLogger(GenerateMarkdownApp.class);

    MdContext ctx;
    boolean generateViews;

    public GenerateMarkdownApp(Path dsl, Path mainDir, boolean generateViews) {
        var ws = StructurizrUtil.getWorkspace(dsl);
        this.generateViews = generateViews;
        ctx = new MdContext(ws, mainDir);
    }

    protected GenerateMarkdownApp(Workspace ws, Path mainDir) {
        this.ctx = new MdContext(ws, mainDir);
        this.generateViews = true;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Mandatory arguments: dsl, outputDir");
        }
        var dsl = Path.of(args[0]);
        var mainDir = Path.of(args[1]);
        var generateViews = true;
        if (args.length > 2) {
            generateViews = Boolean.parseBoolean(args[2]);
        }
        log.info("DSL:            {}", dsl);
        log.info("Output dir:     {}", mainDir);
        log.info("Generate views: {}", generateViews);
        FileUtil.createDirs(mainDir);
        var app = new GenerateMarkdownApp(dsl, mainDir, generateViews);
        app.createMd();
    }

    public void createMd() {
        if (generateViews) {
            new ViewGenerator(ctx.getWorkspace()).initDefaultViews();
        }
        new C4PlantUmlExporter().exportDiagramsC4Plant(ctx.getWorkspace(), ctx.getImagesRoot());

        loadViews(ctx.getWorkspace());
        new LandscapePageService(ctx).createLandscapePage();

        var ssp = new SoftwareSystemPage(ctx);
        ctx.getSoftwareSystemsSorted().forEach(ssp::softwareSystemPage);
    }

    protected void loadViews(Workspace workspace) {
        workspace.getViews().getViews().stream().forEach(this::loadView);
    }

    protected void loadView(View view) {
        ctx.addImage(view.getKey(), "png");
        ctx.addImage(view.getKey(), "svg");
    }
}