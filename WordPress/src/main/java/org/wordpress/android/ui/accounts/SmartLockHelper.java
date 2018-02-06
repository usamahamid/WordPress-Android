package org.wordpress.android.ui.accounts;

import android.app.Activity;
import android.content.IntentSender;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.wordpress.android.ui.RequestCodes;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

public class SmartLockHelper {
    public interface Callback {
        void onCredentialRetrieved(Credential credential);
        void onCredentialsUnavailable();
    }

    private static boolean checkAvailability(Activity activity) {
        return activity != null && GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) ==
                ConnectionResult.SUCCESS;
    }

    static boolean smartLockAutoFill(final Activity activity, @NonNull final Callback callback) {
        if (!checkAvailability(activity)) {
            return false;
        }
        CredentialsOptions options = new CredentialsOptions.Builder().build();
        CredentialsClient client = Credentials.getClient(activity, options);

        // force account chooser
        client.disableAutoSignIn();

        CredentialRequest credentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();
        client.request(credentialRequest).addOnCompleteListener(new OnCompleteListener<CredentialRequestResponse>() {
            @Override
            public void onComplete(@NonNull Task<CredentialRequestResponse> task) {
                if (task.isSuccessful()) {
                    callback.onCredentialRetrieved(task.getResult().getCredential());
                } else {
                    Exception exc = task.getException();
                    if (exc instanceof ResolvableApiException) {
                        try {
                            // Prompt the user to choose a saved credential
                            ((ResolvableApiException) exc).startResolutionForResult(activity,
                                    RequestCodes.SMART_LOCK_READ);
                        } catch (IntentSender.SendIntentException e) {
                            AppLog.d(T.NUX, "SmartLock: Failed to send resolution for credential request");

                            callback.onCredentialsUnavailable();
                        }
                    } else {
                        // The user must create an account or log in manually.
                        AppLog.d(T.NUX, "SmartLock: Unsuccessful credential request.");

                        callback.onCredentialsUnavailable();
                    }
                }
            }
        });

        return true;
    }


    public static void saveCredentialsInSmartLock(final Activity activity, @NonNull final String username,
            @NonNull final String password, @NonNull final String displayName, @Nullable final Uri profilePicture) {
        if (!checkAvailability(activity)) {
            return;
        }
        // need username and password fields for Smart Lock
        // https://github.com/wordpress-mobile/WordPress-Android/issues/5850
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            AppLog.i(T.MAIN, String.format(
                    "Cannot save Smart Lock credentials, username (%s) or password (%s) is empty", username, password));
            return;
        }

        Credential credential = new Credential.Builder(username).setPassword(password)
                .setName(displayName).setProfilePictureUri(profilePicture).build();
        CredentialsOptions options = new CredentialsOptions.Builder().build();
        CredentialsClient client = Credentials.getClient(activity, options);
        client.save(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    AppLog.w(T.NUX, "Saving credentials to SmartLock was unsuccessful! " + (task.getException() != null
                            ? task.getException().getMessage() : "task.getException() is null though."));
                    if (task.getException() instanceof ResolvableApiException) {
                        try {
                            // This prompt the user to resolve the save request
                            ((ResolvableApiException) task.getException()).startResolutionForResult(activity,
                                    RequestCodes.SMART_LOCK_SAVE);
                        } catch (IntentSender.SendIntentException e) {
                            // Could not resolve the request
                        }
                    }
                }
            }
        });
    }
}
