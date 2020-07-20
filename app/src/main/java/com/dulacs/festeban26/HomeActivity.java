package com.dulacs.festeban26;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    public static String DULACS_FACEBOOK_URL = "https://www.facebook.com/DulacsParaDisfrutaryCompartir";
    public static String DULACS_YOUTUBE_CHANNEL_URL = "https://www.youtube.com/channel/UCtiUrxZOTFpuNTbFyjxsDAg";
    public static String DULACS_FACEBOOK_PAGE_ID = "1690770837848159";

    private BottomNavigationView mBottomNavigationView;
    private NavigationView mNavigationView;

    public void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar_homeActivity);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.accessibilityAction_NavDrawerMenu_Open, R.string.accessibilityAction_NavDrawerMenu_Close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.navView_homeActivity);
        mNavigationView.setNavigationItemSelectedListener(this);

        mBottomNavigationView = findViewById(R.id.bottomNavigationView_HomeActivity);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        // By default load the barcode scanning fragment every time the activity gets created (onCreated)
        BarcodeScanningFragment barcodeScanningFragment = new BarcodeScanningFragment();
        openFragment(barcodeScanningFragment);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.generalNav_ConfirmExitMessage))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.generalNav_Exit),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finishAndRemoveTask();
                                }
                            })
                    .setNegativeButton(getString(R.string.generalNav_Cancel),
                            null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help)
            return true;
        else if (id == R.id.action_UpdateData) {
            updateAppData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message, int length) {
        Toast.makeText(this, message, length).show();
    }

    private void updateDatabase() {
        Database.getInstance().initializeDatabase(new Database.OnInitializeDatabaseListener() {
            @Override
            public void databaseReady() {
                openFragment(new OurProductsFragment());
                showToast("Información de la aplicacion correctamente actualizada.", Toast.LENGTH_LONG);
            }
        });
    }

    public void updateAppData() {
        showToast("Actualizando información de la aplicación...", Toast.LENGTH_LONG);

        ResourcesManager.getInstance().updateApplicationData(new ResourcesManager.OnUpdateApplicationDataListener() {
            @Override
            public void onDataSuccessfullyUpdated(boolean successfullyCompleted) {
                updateDatabase();

            }
        });
    }

    // Create a Facebook intent.
    public static Intent newFacebookIntent(PackageManager packageManager) {
        // By default, the URI is the facebook https link
        Uri uri = Uri.parse(DULACS_FACEBOOK_URL);
        // If facebook app is installed the URI will be "fb://page/..." so it opens the Facebook Page
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled)
                uri = Uri.parse("fb://page/" + DULACS_FACEBOOK_PAGE_ID);
        }
        // If facebook is not installed
        catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    private void updateNavigationMenusForFragment(Fragment fragment) {
        mBottomNavigationView.getMenu().clear();
        if (fragment instanceof BarcodeScanningFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_BarcodeScanning);
            addOurProductsItemToBottomNavigationMenu(mBottomNavigationView);
            mBottomNavigationView.getMenu().setGroupCheckable(0, false, true);
            return;
        } else if (fragment instanceof OurProductsFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_OurProducts);
            addBarcodeScanningItemToBottomNavigationMenu(mBottomNavigationView);
            mBottomNavigationView.getMenu().setGroupCheckable(0, false, true);
            return;
        } else if (fragment instanceof ProductInfoFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_OurProducts);
        } else if (fragment instanceof RecipesFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_Recipes);
        } else if (fragment instanceof DailyDessertFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_DailyDessert);
        } else if (fragment instanceof AboutUsFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_AboutUs);
        } else if (fragment instanceof ContactUsFragment) {
            mNavigationView.setCheckedItem(R.id.navItem_HomeDrawer_ContactUs);
        }
        addOurProductsItemToBottomNavigationMenu(mBottomNavigationView);
        addBarcodeScanningItemToBottomNavigationMenu(mBottomNavigationView);
        mBottomNavigationView.getMenu().setGroupCheckable(0, false, true);
    }

    /**
     * This method replace R.id.frameLayout_HomeActivity with the fragment to open.
     * If the view to be replaced already contains a fragment of the same class, the view will
     * not be replaced.
     *
     * @param fragmentToOpen The fragment to open
     */
    private void openFragment(Fragment fragmentToOpen) {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout_HomeActivity);
        if (currentFragment == null ||
                !currentFragment.getClass().getCanonicalName().equals(fragmentToOpen.getClass().getCanonicalName())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout_HomeActivity, fragmentToOpen)
                    // It is important to disable this line of code. It makes the main UI thread
                    // to skip frames since it has to animate the transition between a surface view
                    // and the next fragment content.
                    //.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commit();
            updateNavigationMenusForFragment(fragmentToOpen);
        }
    }

    private void addRecipesItemToBottomNavigationMenu(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu()
                .add(Menu.NONE, R.id.bottomNavigation_Recipes,
                        Menu.NONE, R.string.navDrawerMenu_Recipes)
                .setIcon(R.drawable.ic_recipe);
    }

    private void addOurProductsItemToBottomNavigationMenu(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu()
                .add(Menu.NONE, R.id.menuItem_BottomNavigation_OurProducts,
                        Menu.NONE, R.string.navDrawerMenu_OurProducts)
                .setIcon(R.drawable.ic_shopping_basket_black_24dp);
    }

    private void addBarcodeScanningItemToBottomNavigationMenu(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu()
                .add(Menu.NONE, R.id.menuItem_BottomNavigation_BarcodeScanning,
                        Menu.NONE, R.string.bottomNavMenu_BarcodeScanning)
                .setIcon(R.drawable.ic_scan_barcode);
    }

    private void addDailyDessertItemToBottomNavigationMenu(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu()
                .add(Menu.NONE, R.id.bottomNavigation_DailyDessert,
                        Menu.NONE, R.string.navDrawerMenu_DailyDessert)
                .setIcon(R.drawable.ic_cake);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.navItem_HomeDrawer_BarcodeScanning:
            case R.id.menuItem_BottomNavigation_BarcodeScanning:
                openFragment(new BarcodeScanningFragment());
                break;
            case R.id.navItem_HomeDrawer_Recipes:
            case R.id.bottomNavigation_Recipes:
                openFragment(new RecipesFragment());
                break;
            case R.id.navItem_HomeDrawer_DailyDessert:
            case R.id.bottomNavigation_DailyDessert:
                openFragment(new DailyDessertFragment());
                break;
            case R.id.menuItem_BottomNavigation_OurProducts:
            case R.id.navItem_HomeDrawer_OurProducts:
                openFragment(new OurProductsFragment());
                break;
            case R.id.navItem_HomeDrawer_AboutUs:
                openFragment(new AboutUsFragment());
                break;
            case R.id.navItem_HomeDrawer_ContactUs:
                openFragment(new ContactUsFragment());
                break;
            case R.id.navItem_HomeDrawer_Facebook:
                startActivity(newFacebookIntent(getPackageManager()));
                break;
            case R.id.navItem_HomeDrawer_Youtube:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(DULACS_YOUTUBE_CHANNEL_URL));
                startActivity(intent);
                break;
            default:
                return false;
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void attachProductInformationFragmentForBarcode(long barcodeReadingResult) {
        ProductInfoFragment productInfoFragment = new ProductInfoFragment();
        Bundle args = new Bundle();
        args.putLong("barcode", barcodeReadingResult);
        productInfoFragment.setArguments(args);
        openFragment(productInfoFragment);
        setActionBarTitle(getResources().getString(R.string.companyName));
    }

    protected void attachProductInformationFragmentForSapCode(String sapCode) {
        ProductInfoFragment productInfoFragment = new ProductInfoFragment();
        Bundle args = new Bundle();
        args.putString("sapCode", sapCode);
        productInfoFragment.setArguments(args);
        openFragment(productInfoFragment);
        setActionBarTitle(getResources().getString(R.string.companyName));
    }

    protected void attachCategoryFragment(String categoryName) {
        CategoryFragment categoryFragment = CategoryFragment.newInstance(categoryName);
        openFragment(categoryFragment);
        setActionBarTitle(categoryName);
    }
}

