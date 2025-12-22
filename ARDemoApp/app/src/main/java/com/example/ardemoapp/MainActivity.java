package com.example.ardemoapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_BASE_URL = "http://192.168.100.69:3000";
    private static final String MODELS_ENDPOINT = SERVER_BASE_URL + "/api/models";

    private ArFragment arFragment;
    private Button btnRefresh;
    private Button btnPreload;
    private Button btnClear;
    private Spinner spinnerModels;
    private SeekBar scaleSeekBar;
    private SeekBar rotationSeekBar;
    private TextView scaleLabel;
    private TextView rotationLabel;
    private TextView modelInfo;

    private boolean isModelReady = false;
    private String selectedModelUrl = null;
    private final List<ModelItem> modelItems = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, ModelRenderable> renderableCache = new HashMap<>();
    private final List<AnchorNode> placedNodes = new ArrayList<>();

    private float currentScale = 0.6f;
    private float currentRotationDegrees = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ux_fragment);

        btnRefresh = findViewById(R.id.btnRefresh);
        btnPreload = findViewById(R.id.btnPreload);
        btnClear = findViewById(R.id.btnClear);
        spinnerModels = findViewById(R.id.spinnerModels);
        scaleSeekBar = findViewById(R.id.scaleSeekBar);
        rotationSeekBar = findViewById(R.id.rotationSeekBar);
        scaleLabel = findViewById(R.id.scaleLabel);
        rotationLabel = findViewById(R.id.rotationLabel);
        modelInfo = findViewById(R.id.modelInfo);

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModels.setAdapter(spinnerAdapter);

        btnRefresh.setOnClickListener(v -> loadModelList());
        btnPreload.setOnClickListener(v -> preloadSelectedModel());
        btnClear.setOnClickListener(v -> clearPlacedModels());

        spinnerModels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < modelItems.size()) {
                    ModelItem item = modelItems.get(position);
                    selectedModelUrl = item.url;
                    isModelReady = true;
                    updateModelInfo(item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedModelUrl = null;
                isModelReady = false;
                modelInfo.setText("Chưa chọn model");
            }
        });

        setupControls();
        loadModelList();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (!isModelReady) {
                Toast.makeText(this, "Chưa sẵn sàng...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedModelUrl == null) {
                Toast.makeText(this, "Hãy chọn một model trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            placedNodes.add(anchorNode);

            placeModel(anchorNode, selectedModelUrl);
        });
    }

    private void setupControls() {
        scaleSeekBar.setMax(150); // 0 -> 1.5
        rotationSeekBar.setMax(360);

        scaleSeekBar.setProgress((int) (currentScale * 100));
        rotationSeekBar.setProgress((int) (currentRotationDegrees + 180));

        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentScale = Math.max(0.1f, progress / 100f);
                updateLabels();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        rotationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRotationDegrees = progress - 180;
                updateLabels();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        updateLabels();
    }

    private void updateLabels() {
        scaleLabel.setText(String.format(Locale.getDefault(), "Scale: %.2fx", currentScale));
        rotationLabel.setText(String.format(Locale.getDefault(), "Xoay: %.0f°", currentRotationDegrees));
    }

    private void loadModelList() {
        Request request = new Request.Builder()
                .url(MODELS_ENDPOINT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Không tải được danh sách model", Toast.LENGTH_LONG).show());
                Log.e("MODEL_LIST", "Lỗi tải danh sách", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Server trả về lỗi", Toast.LENGTH_LONG).show());
                    return;
                }

                final String body = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    JSONArray modelsArray = jsonObject.getJSONArray("models");

                    List<ModelItem> loadedItems = new ArrayList<>();
                    List<String> names = new ArrayList<>();

                    for (int i = 0; i < modelsArray.length(); i++) {
                        JSONObject item = modelsArray.getJSONObject(i);
                        String name = item.getString("name");
                        String url = item.getString("url");
                        long size = item.optLong("size", 0);
                        String uploadedAt = item.optString("uploadedAt", "");
                        loadedItems.add(new ModelItem(name, url, size, uploadedAt));
                        names.add(name);
                    }

                    runOnUiThread(() -> {
                        modelItems.clear();
                        modelItems.addAll(loadedItems);

                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(names);
                        spinnerAdapter.notifyDataSetChanged();

                        if (!modelItems.isEmpty()) {
                            spinnerModels.setSelection(0);
                            ModelItem first = modelItems.get(0);
                            selectedModelUrl = first.url;
                            isModelReady = true;
                            updateModelInfo(first);
                        } else {
                            selectedModelUrl = null;
                            isModelReady = false;
                            modelInfo.setText("Chưa có model nào, hãy upload lên server");
                        }
                    });
                } catch (JSONException e) {
                    Log.e("MODEL_LIST", "Sai định dạng JSON", e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sai định dạng dữ liệu từ server", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void preloadSelectedModel() {
        if (selectedModelUrl == null) {
            Toast.makeText(this, "Chọn model trước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (renderableCache.containsKey(selectedModelUrl)) {
            Toast.makeText(this, "Model đã sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tải trước model...", Toast.LENGTH_SHORT).show();
        buildRenderable(selectedModelUrl, null);
    }

    private void placeModel(AnchorNode anchorNode, String url) {
        Toast.makeText(this, "Đang tải model về...", Toast.LENGTH_SHORT).show();

        ModelRenderable cached = renderableCache.get(url);
        if (cached != null) {
            attachNode(anchorNode, cached);
            return;
        }

        buildRenderable(url, anchorNode);
    }

    private void buildRenderable(String url, AnchorNode anchorNodeIfAny) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(url))
                .setIsFilamentGltf(true)
                .setRegistryId(url)
                .build()
                .thenAccept(renderable -> {
                    renderableCache.put(url, renderable);
                    runOnUiThread(() -> {
                        if (anchorNodeIfAny != null) {
                            attachNode(anchorNodeIfAny, renderable);
                        } else {
                            Toast.makeText(MainActivity.this, "Model đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("AR_ERROR", "Không tải được model: " + throwable.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Lỗi: Không tải được file model!", Toast.LENGTH_LONG).show()
                    );
                    return null;
                });
    }

    private void attachNode(AnchorNode anchorNode, ModelRenderable renderable) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(renderable);

        Vector3 scaleVector = new Vector3(currentScale, currentScale, currentScale);
        node.setLocalScale(scaleVector);

        Quaternion rotation = Quaternion.axisAngle(Vector3.up(), currentRotationDegrees);
        node.setLocalRotation(rotation);

        node.getScaleController().setMinScale(0.1f);
        node.getScaleController().setMaxScale(3.0f);

        node.select();
    }

    private void clearPlacedModels() {
        for (AnchorNode node : placedNodes) {
            if (node.getParent() != null) {
                node.setParent(null);
            }
        }
        placedNodes.clear();
        Toast.makeText(this, "Đã xoá mọi vật thể", Toast.LENGTH_SHORT).show();
    }

    private void updateModelInfo(ModelItem item) {
        String sizeText = formatSize(item.sizeBytes);
        String timeText = formatDate(item.uploadedAt);
        String info = "Tên: " + item.name + "\n" +
                "Kích thước: " + sizeText + "\n" +
                "Tải lên: " + timeText;
        modelInfo.setText(info);
    }

    private String formatSize(long sizeBytes) {
        if (sizeBytes <= 0) return "Không rõ";
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = (int) (Math.log10(sizeBytes) / Math.log10(1024));
        unitIndex = Math.min(unitIndex, units.length - 1);
        double scaled = sizeBytes / Math.pow(1024, unitIndex);
        return String.format(Locale.getDefault(), "%.2f %s", scaled, units[unitIndex]);
    }

    private String formatDate(String isoString) {
        if (isoString == null || isoString.isEmpty()) return "Không rõ";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = isoFormat.parse(isoString);
            if (parsed == null) return "Không rõ";
            SimpleDateFormat output = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return output.format(parsed);
        } catch (Exception e) {
            return isoString;
        }
    }

    private static class ModelItem {
        final String name;
        final String url;
        final long sizeBytes;
        final String uploadedAt;

        ModelItem(String name, String url, long sizeBytes, String uploadedAt) {
            this.name = name;
            this.url = url;
            this.sizeBytes = sizeBytes;
            this.uploadedAt = uploadedAt;
        }
    }
}
