package com.example.ardemoapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    // 👇 SỬA LẠI URL TRỎ THẲNG VÀO FILE (QUAN TRỌNG)
    // Lưu ý: "/uploads/model.glb" khớp với cấu hình trong server.js
    private static final String MODEL_URL_DIRECT = "http://192.168.100.69:3000/uploads/model.glb";

    private ArFragment arFragment;
    private Button btnConnect;

    // Mặc định cho phép đặt model luôn, không cần chờ nút bấm
    private boolean isModelReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ux_fragment);

        btnConnect = findViewById(R.id.btnConnect);

        // Nút bấm chỉ dùng để thông báo cho người dùng yên tâm
        btnConnect.setOnClickListener(v -> {
            Toast.makeText(this, "Đang dùng đường dẫn: " + MODEL_URL_DIRECT, Toast.LENGTH_SHORT).show();
        });

        // Chạm mặt phẳng để đặt model
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (!isModelReady) {
                Toast.makeText(this, "Chưa sẵn sàng...", Toast.LENGTH_SHORT).show();
                return;
            }

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Gọi hàm tải và đặt model
            placeModel(anchorNode, MODEL_URL_DIRECT);
        });
    }

    // Hàm này giữ nguyên logic, chỉ sửa lại thông báo lỗi rõ hơn
    private void placeModel(AnchorNode anchorNode, String url) {
        Toast.makeText(this, "Đang tải model về...", Toast.LENGTH_SHORT).show();

        ModelRenderable.builder()
                .setSource(this, Uri.parse(url))
                .setIsFilamentGltf(true) // 👇 QUAN TRỌNG: Thêm dòng này nếu dùng bản Sceneform mới (hỗ trợ GLB tốt hơn)
                .setRegistryId(url)
                .build()
                .thenAccept(renderable -> {
                    Toast.makeText(this, "Tải xong! Đang hiển thị...", Toast.LENGTH_SHORT).show();

                    TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                    node.setParent(anchorNode);
                    node.setRenderable(renderable);

                    // Chỉnh kích thước
                    node.getScaleController().setMinScale(0.1f);
                    node.getScaleController().setMaxScale(2.0f);
                    node.setLocalScale(new com.google.ar.sceneform.math.Vector3(0.5f, 0.5f, 0.5f));

                    node.select();
                })
                .exceptionally(throwable -> {
                    Log.e("AR_ERROR", "Không tải được model: " + throwable.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi: Không tải được file model.glb!", Toast.LENGTH_LONG).show()
                    );
                    return null;
                });
    }
}