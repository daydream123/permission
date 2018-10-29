# permission
An android permission requester replacement for default implementation

The default implementation of permission is not simple, just like `startActivityForResult` the permission request and handle should be in two methods.

```java
private void requestPermission(){
  	// Here, thisActivity is the current activity
	if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {

    // Should we show an explanation?
    if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
            Manifest.permission.READ_CONTACTS)) {
        // Show an expanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
    } else {
        // No explanation needed, we can request the permission.
        ActivityCompat.requestPermissions(thisActivity,
                new String[]{Manifest.permission.READ_CONTACTS},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
    }
}

@Override
public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
    switch (requestCode) {
        case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }
}
```

An third-party library called RxPermission brings a new idea to simplify this job, but need to add dependencies of RxJava and RxAndroid into our project. After researching of this library, I found there is no need to use RxJava and RxAndroid, only a callback between requester and a none-ui fragemnt is enough. Passing grant result from none-ui fragment to requester by callback is the solution.

The useage to request permissions is like below:

```java
public class MainActivity extends AppCompatActivity {
    private Permissions mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init permissions
        mPermissions = new Permissions(this);

        Button requestBtn = findViewById(R.id.requestBtn);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPermissions.request(new OnGrantResult() {
                    @Override
                    public void onGrant() {
                        // do something when grant
                    }

                     @Override
                     public void onDenied() {
                         // do something when denied
                     }
                }, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE);
            }
        });

        Button requestEachBtn = findViewById(R.id.requestEachBtn);
        requestEachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPermissions.requestEach(new OnEachGrantResult() {
                    @Override
                    public void onNext(Permission permission) {
                        // check someone permission and
                        // its grant state and do something you want
                    }
                }, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE);
            }
        });
    }
}
```
