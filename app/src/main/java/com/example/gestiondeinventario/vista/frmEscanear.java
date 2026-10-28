package com.example.gestiondeinventario.vista;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.controlador.ProductoControlador;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.vista.adaptador.AdaptadorEscaneo;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class frmEscanear extends Fragment implements AdaptadorEscaneo.OnEscaneoInteractionListener {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;

    private RecyclerView recyclerView;
    private AdaptadorEscaneo adapter;
    private List<Producto> scannedProducts = new ArrayList<>();
    private Map<Integer, Integer> productQuantities = new HashMap<>();

    private TextView textItemCount, textTotalPrice, bannerText;
    private View successBanner;

    private ProductoRepositorio repositorio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_escanear, container, false);

        repositorio = new ProductoRepositorio(requireContext());

        previewView = view.findViewById(R.id.previewView);
        recyclerView = view.findViewById(R.id.recycler_scan_list);
        textItemCount = view.findViewById(R.id.text_item_count);
        textTotalPrice = view.findViewById(R.id.text_total_price);
        successBanner = view.findViewById(R.id.success_banner);
        bannerText = view.findViewById(R.id.banner_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdaptadorEscaneo(scannedProducts, productQuantities, this);
        recyclerView.setAdapter(adapter);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (checkPermission()) {
            startCamera();
        } else {
            requestPermission();
        }

        view.findViewById(R.id.btn_checkout).setOnClickListener(v -> simulateScan());

        view.findViewById(R.id.btn_confirm_entry).setOnClickListener(v -> {
            if (scannedProducts.isEmpty()) {
                Toast.makeText(getContext(), "La lista está vacía", Toast.LENGTH_SHORT).show();
            } else {
                confirmInventoryEntry();
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(this::simulateScan, 500);

        updateTotals();
        return view;
    }

    private void confirmInventoryEntry() {
        for (Producto p : scannedProducts) {
            int quantityToAdd = productQuantities.getOrDefault(p.getId(), 1);
            p.setStock(p.getStock() + quantityToAdd);
            repositorio.actualizarStock(p.getId(), p.getStock());
        }
        Toast.makeText(getContext(), "Stock actualizado correctamente", Toast.LENGTH_SHORT).show();
        scannedProducts.clear();
        productQuantities.clear();
        adapter.notifyDataSetChanged();
        updateTotals();
    }

    private void simulateScan() {
        List<Producto> allProducts = repositorio.obtenerTodos();
        if (!allProducts.isEmpty()) {
            Producto randomProduct = allProducts.get(new Random().nextInt(allProducts.size()));
            addProductToList(randomProduct);
        }
    }

    private void addProductToList(Producto product) {
        if (product == null) return;
        if (productQuantities.containsKey(product.getId())) {
            productQuantities.put(product.getId(), productQuantities.get(product.getId()) + 1);
        } else {
            scannedProducts.add(0, product);
            productQuantities.put(product.getId(), 1);
        }
        adapter.notifyDataSetChanged();
        showSuccessBanner(product.getNombre());
        updateTotals();
    }

    private void showSuccessBanner(String name) {
        if (bannerText != null && successBanner != null) {
            bannerText.setText("Agregado: " + name);
            successBanner.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) successBanner.setVisibility(View.GONE);
            }, 1500);
        }
    }

    @Override
    public void onCantidadCambiada() {
        updateTotals();
    }

    @Override
    public void onItemClick(Producto product) {
        Intent intent = new Intent(getActivity(), frmDetalleProducto.class);
        intent.putExtra("producto", product);
        startActivity(intent);
    }

    private void updateTotals() {
        int count = 0;
        double total = 0;
        for (Producto p : scannedProducts) {
            int qty = productQuantities.getOrDefault(p.getId(), 1);
            count += qty;
            total += (p.getPrecio() * qty);
        }
        if (textItemCount != null) textItemCount.setText(count + " productos");
        if (textTotalPrice != null) textTotalPrice.setText("Total: S/ " + String.format("%.2f", total));
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("frmEscanear", "Error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        BarcodeScanner scanner = BarcodeScanning.getClient();
        imageAnalysis.setAnalyzer(cameraExecutor, image -> processImageProxy(scanner, image));

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e("frmEscanear", "Error: " + e.getMessage());
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(BarcodeScanner scanner, ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String code = barcode.getRawValue();
                        if (code != null) {
                            handleRealScan(code);
                            break;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleRealScan(String code) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Producto p = repositorio.obtenerPorCodigo(code);
                if (p != null) addProductToList(p);
                else Toast.makeText(getContext(), "Producto no encontrado: " + code, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (repositorio != null) repositorio.cerrar();
    }
}