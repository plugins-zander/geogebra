package org.geogebra.web.shared;

import org.geogebra.web.resources.SVGResource;
import org.geogebra.web.resources.SassResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SharedResources extends ClientBundle {

    SharedResources INSTANCE = GWT.create(SharedResources.class);

    @Source("org/geogebra/common/icons/png/web/algebra-view-tree-open.png")
    ImageResource algebra_tree_open();

    @Source("org/geogebra/common/icons/png/web/algebra-view-tree-closed.png")
    ImageResource algebra_tree_closed();

    @Source("org/geogebra/web/resources/scss/solver.scss")
    SassResource solverStyleScss();

	@Source("org/geogebra/web/resources/scss/shared.scss")
	SassResource sharedStyleScss();

    @Source("org/geogebra/web/resources/scss/step-tree.scss")
    SassResource stepTreeStyleScss();

	@Source("org/geogebra/common/icons/svg/web/matDesignIcons/av/plusMenu/ic_help_outline_black_24px.svg")
	SVGResource icon_help_black();

	@Source("org/geogebra/common/icons/png/web/button_cancel.png")
	ImageResource dialog_cancel();

	@Source("org/geogebra/common/icons/png/web/general/social-facebook.png")
	ImageResource social_facebook();

	@Source("org/geogebra/common/icons/png/web/general/social-google.png")
	ImageResource social_google();

	@Source("org/geogebra/common/icons/png/web/general/social-twitter.png")
	ImageResource social_twitter();

	@Source("org/geogebra/common/icons/png/web/general/social-google-classroom.png")
	ImageResource social_google_classroom();

	@Source("org/geogebra/common/icons/png/web/general/social-onenote.png")
	ImageResource social_onenote();

	@Source("org/geogebra/common/icons/png/web/general/social-edmodo.png")
	ImageResource social_edmodo();

	@Source("org/geogebra/common/icons/png/web/menu_icons/menu-edit-copy.png")
	ImageResource edit_copy();
}
