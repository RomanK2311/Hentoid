package me.devsaki.hentoid.activities.sources;

import me.devsaki.hentoid.enums.Site;

public class AllPornComicActivity extends BaseWebActivity {

    public static final String GALLERY_PATTERN = "allporncomic.com/porncomic/[%\\w\\-]+/$";

    private static final String DOMAIN_FILTER = "allporncomic.com";
    private static final String[] GALLERY_FILTER = {GALLERY_PATTERN, GALLERY_PATTERN.replace("$", "[%\\w\\-]+/$")};
    private static final String[] JS_WHITELIST = {DOMAIN_FILTER + "/cdn", DOMAIN_FILTER + "/wp"};
    private static final String[] JS_CONTENT_BLACKLIST = {"var exoloader;", "popunder"};
    private static final String[] AD_ELEMENTS = {"iframe", ".c-ads"};


    Site getStartSite() {
        return Site.ALLPORNCOMIC;
    }

    @Override
    protected CustomWebViewClient createWebClient() {
        CustomWebViewClient client = new CustomWebViewClient(getStartSite(), GALLERY_FILTER, this);
        client.restrictTo(DOMAIN_FILTER);
        client.addRemovableElements(AD_ELEMENTS);
        client.adBlocker.addToJsUrlWhitelist(JS_WHITELIST);
        for (String s : JS_CONTENT_BLACKLIST) client.adBlocker.addJsContentBlacklist(s);
        return client;
    }
}
