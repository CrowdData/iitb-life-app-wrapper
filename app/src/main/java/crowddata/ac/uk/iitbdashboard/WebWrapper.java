package crowddata.ac.uk.iitbdashboard;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebWrapper extends ActionBarActivity {
    WebView dashboardView;
    Activity a;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //assign for loading
        a=this;
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_web_view_dashboard);
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        actionBar=this.getActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      //      WebView.setWebContentsDebuggingEnabled(true);
        }
        //actionBar.setIcon(android.R.color.transparent);
        dashboardView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = dashboardView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        dashboardView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        dashboardView.getSettings().setGeolocationDatabasePath( getApplicationContext().getDatabasePath("Geo-Locations").getAbsolutePath());
        WebChromeClient client=new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
            {
                result.confirm();
                Log.d("alert", message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                return true;
            };

            public void onProgressChanged(WebView view, int progress)
            {

                a.setTitle("Loading... "+progress+"%");
                a.setProgress(progress * 100);


                if(progress == 100){

                    actionBar.hide();
                }
            }




        };


        dashboardView.setWebChromeClient(client);
        dashboardView.setWebViewClient(new WebViewClient(){
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
            //Any links that are not withing crowddata namespace will be forwarded to external activities
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                actionBar.show();
                if(!url.contains(getString(R.string.RESTRICT))){
                    Intent i=new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }


        });
        dashboardView.addJavascriptInterface(new WebAppInterface(this), "Android");

        dashboardView.loadUrl(getString(R.string.IITBLIFE_URL));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //	getMenuInflater().inflate(R.menu.web_view_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Enable back button, if no more pages release key ..
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    if(dashboardView.canGoBack()){
                        dashboardView.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }


}
