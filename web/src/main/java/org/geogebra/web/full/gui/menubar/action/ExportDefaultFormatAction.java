package org.geogebra.web.full.gui.menubar.action;

import org.geogebra.web.full.gui.menubar.DefaultMenuAction;
import org.geogebra.web.full.main.AppWFull;

/**
 * Exports ggb or ggs format.
 */
public class ExportDefaultFormatAction extends DefaultMenuAction<Void> {

    @Override
    public void execute(Void item, AppWFull app) {
        app.getFileManager().export(app);
    }
}
