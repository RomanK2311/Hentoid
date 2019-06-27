package me.devsaki.hentoid.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import me.devsaki.hentoid.database.ObjectBoxCollectionAccessor;
import me.devsaki.hentoid.database.ObjectBoxDB;
import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.listener.ContentListener;
import me.devsaki.hentoid.util.FileHelper;
import me.devsaki.hentoid.util.Preferences;
import me.devsaki.hentoid.util.ToastUtil;
import me.devsaki.hentoid.widget.ContentSearchManager;


public class ImageViewerViewModel extends AndroidViewModel implements ContentListener {

    private static final int FIRST_OF_PAGE = -1;
    private static final int LAST_OF_PAGE = -2;

    // Settings
    private boolean shuffleImages = false;      // True if images have to be shuffled; false if presented in the book order

    private ContentSearchManager searchManager = null;

    // Pictures data
    private final MutableLiveData<List<String>> images = new MutableLiveData<>();   // Currently displayed set of images
    private List<String> initialImagesList;         // Initial URL list in the right order, to fallback when shuffling is disabled
    private int imageIndex;                         // 0-based position, as in "programmatic index"

    // Collection data
    private List<Content> contentPage = new ArrayList<>(); // Loaded page of books
    private int contentIndex;                   // Index of currently displayed book within current page
    private int maxPages;                       // Maximum available pages
    private long contentId;                     // Database ID of currently displayed book


    public ImageViewerViewModel(@NonNull Application application) {
        super(application);
        images.setValue(Collections.emptyList());
    }

    @NonNull
    public LiveData<List<String>> getImages() {
        return images;
    }

    public String getImage(int position) {
        List<String> imgs = images.getValue();
        if (imgs != null && position < imgs.size() && position > -1) return imgs.get(position);
        else return "";
    }

    public void setImages(List<String> imgs) {
        initialImagesList = new ArrayList<>(imgs);
        if (shuffleImages) Collections.shuffle(imgs);
        images.postValue(imgs);
    }

    public void setContentId(long contentId) {
        this.contentId = contentId;
    }

    public void setSearchParams(@Nonnull Bundle bundle) {
        Context ctx = getApplication().getApplicationContext();
        searchManager = new ContentSearchManager(new ObjectBoxCollectionAccessor(ctx));
        searchManager.loadFromBundle(bundle, ctx);
        searchManager.searchLibrary(Preferences.getContentPageQuantity(), this);
    }


    public void setImageIndex(int position) {
        this.imageIndex = position;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setShuffleImages(boolean shuffleImages) {
        this.shuffleImages = shuffleImages;
        if (shuffleImages) {
            List<String> imgs = new ArrayList<>(initialImagesList);
            Collections.shuffle(imgs);
            images.setValue(imgs);
        } else images.setValue(initialImagesList);
    }

    public int getInitialPosition() {
        ObjectBoxDB db = ObjectBoxDB.getInstance(getApplication().getApplicationContext());
        if (contentId > 0) {
            Content content = db.selectContentById(contentId);
            if (content != null) return content.getLastReadPageIndex();
        }
        return 0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        searchManager.dispose();
    }

    public void saveCurrentPosition() {
        ObjectBoxDB db = ObjectBoxDB.getInstance(getApplication().getApplicationContext());
        if (contentId > 0) {
            Content content = db.selectContentById(contentId);
            if (content != null) {
                content.setLastReadPageIndex(imageIndex);
                db.insertContent(content);
            }
        }
    }

    @Nullable
    public Content getCurrentContent() {
        ObjectBoxDB db = ObjectBoxDB.getInstance(getApplication().getApplicationContext());
        if (contentId > 0) {
            return db.selectContentById(contentId);
        } else return null;
    }

    public void loadNextContent() {
        if (contentIndex == contentPage.size() - 1 && searchManager.getCurrentPage() < maxPages) // Need to load next content page
        {
            contentIndex = FIRST_OF_PAGE;
            searchManager.increaseCurrentPage();
            searchManager.searchLibrary(Preferences.getContentPageQuantity(), this);
        } else if (contentIndex < contentPage.size() - 1) {
            contentIndex++;
            loadContent();
        }
    }

    public void loadPreviousContent() {
        if (0 == contentIndex && searchManager.getCurrentPage() > 1) // Need to load previous content page
        {
            contentIndex = LAST_OF_PAGE;
            searchManager.decreaseCurrentPage();
            searchManager.searchLibrary(Preferences.getContentPageQuantity(), this);
        } else if (contentIndex > 0) {
            contentIndex--;
            loadContent();
        }
    }

    private void loadContent() {
        // Record last read position before leaving current content
        // TODO

        // Load new content
        Content content = contentPage.get(contentIndex);
        contentId = content.getId();
        File[] pictures = FileHelper.getPictureFilesFromContent(getApplication().getApplicationContext(), content);
        if (pictures != null) {
            List<String> imagesLocations = new ArrayList<>();
            for (File f : pictures) imagesLocations.add(f.getAbsolutePath());
            setImages(imagesLocations);
        }

        // Record 1 more view for new content
        // TODO
    }

    @Override
    public void onContentReady(List<Content> results, long totalSelectedContent, long totalContent) {
        contentPage = results;
        maxPages = (int) Math.ceil(totalContent * 1.0 / Preferences.getContentPageQuantity());

        if (FIRST_OF_PAGE == contentId) contentIndex = 0;
        else if (LAST_OF_PAGE == contentId) contentIndex = contentPage.size() - 1;
        else for (int i = 0; i < contentPage.size(); i++) {
                if (contentPage.get(i).getId() == contentId) {
                    contentIndex = i;
                    break;
                }
            }

        loadContent();
    }

    @Override
    public void onContentFailed(Content content, String message) {
        ToastUtil.toast("Book list loading failed");
    }
}
