package com.example.imageeditorapp.Helper;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.example.imageeditorapp.R;
import com.example.imageeditorapp.TouchImageView;

public class CommonUtils {

    public void showFullScreenImage(Context context, Uri uri) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

           dialog.setContentView(R.layout.full_screen_image);
        final TouchImageView imageView = dialog.findViewById(R.id.ivFullScreen);
        ImageView ivClose = dialog.findViewById(R.id.ivFullScreenClose);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        imageView.setImageURI(uri);

        dialog.show();
    }

}
