package com.app.facerecogn.utils;

import android.app.Dialog;
import android.content.Context;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by bhuvnesh on 24/10/19.
 */

public class progressdialog
{
    public static Dialog dialog;
    public static KProgressHUD mprogress;

    public static void showwaitingdialog(Context context) {
        if (mprogress != null && mprogress.isShowing()) {
            mprogress.dismiss();
        }

        mprogress = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f)
                .show();
    }

    //
    public static void showwaitingdialog(Context context, String message) {
        /*dialog = new SpotsDialog(context, message, R.style.Custom_dialog);
        dialog.setCancelable(false);
        dialog.show();*/

        if (mprogress != null && mprogress.isShowing()) {
            mprogress.dismiss();
        }

        mprogress = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f)
                .show();
    }

    public static void dismisswaitdialog() {
        if (mprogress != null && mprogress.isShowing()) {
            mprogress.dismiss();
        }
    }
}
