package com.pdm.domohouse.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pdm.domohouse.R;

/**
 * Fragmento para gestionar dispositivos del hogar
 * Fragmento temporal (stub) para evitar errores de navegación
 */
public class DevicesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Por ahora usar layout básico de dispositivos
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Implementar lógica de dispositivos en sesión futura
    }
}