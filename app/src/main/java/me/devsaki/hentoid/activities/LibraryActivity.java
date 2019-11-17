package me.devsaki.hentoid.activities;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import me.devsaki.hentoid.R;
import me.devsaki.hentoid.fragments.library.LibraryFragment;
import me.devsaki.hentoid.util.Preferences;

public class LibraryActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private OnBackPressedCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hentoid);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragment_library);

        if (fragment == null) {
            fragment = new LibraryFragment();

            manager.beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                closeNavigationDrawer();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!Preferences.isFirstRunProcessComplete()) {
            // first run of the app starts with the nav drawer open
            drawerLayout.openDrawer(GravityCompat.START);
            Preferences.setIsFirstRunProcessComplete(true);
        }

        if (!Preferences.getRecentVisibility()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    public void closeNavigationDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
        callback.setEnabled(false);
    }

    public void openNavigationDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
        callback.setEnabled(true);
    }
}
