package com.example.arddemoapp.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragment extends ArFragment {

    @Override
    public void onResume() {
        super.onResume();

        // Sau super.onResume() thì session thường đã sẵn sàng
        Session session = getArSceneView().getSession();
        if (session != null) {
            Config config = session.getConfig();

            // Tắt Depth
            config.setDepthMode(Config.DepthMode.DISABLED);

            // Tắt Environmental HDR (tránh acquireEnvironmentalHdrCubeMap)
            config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);

            session.configure(config);
        }
    }
}
