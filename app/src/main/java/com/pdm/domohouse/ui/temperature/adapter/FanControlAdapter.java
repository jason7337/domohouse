package com.pdm.domohouse.ui.temperature.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;
import com.pdm.domohouse.data.model.FanControl;
import com.pdm.domohouse.databinding.ItemFanControlBinding;
import com.pdm.domohouse.ui.temperature.FanControlListener;

/**
 * Adaptador para mostrar los controles de ventiladores en un RecyclerView
 */
public class FanControlAdapter extends ListAdapter<FanControl, FanControlAdapter.FanControlViewHolder> {
    
    private final FanControlListener listener;
    
    public FanControlAdapter(FanControlListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    
    private static final DiffUtil.ItemCallback<FanControl> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<FanControl>() {
            @Override
            public boolean areItemsTheSame(@NonNull FanControl oldItem, @NonNull FanControl newItem) {
                return oldItem.getDeviceId().equals(newItem.getDeviceId());
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull FanControl oldItem, @NonNull FanControl newItem) {
                return oldItem.getIsOn() == newItem.getIsOn() &&
                       oldItem.getSpeedPercentage() == newItem.getSpeedPercentage() &&
                       oldItem.getIsConnected() == newItem.getIsConnected() &&
                       oldItem.getPowerConsumption() == newItem.getPowerConsumption() &&
                       oldItem.getIsAutomaticMode() == newItem.getIsAutomaticMode();
            }
        };
    
    @NonNull
    @Override
    public FanControlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFanControlBinding binding = ItemFanControlBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new FanControlViewHolder(binding, listener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FanControlViewHolder holder, int position) {
        FanControl fanControl = getItem(position);
        holder.bind(fanControl);
    }
    
    /**
     * ViewHolder para los items de control de ventiladores
     */
    static class FanControlViewHolder extends RecyclerView.ViewHolder {
        
        private final ItemFanControlBinding binding;
        private final FanControlListener listener;
        private FanControl currentFanControl;
        
        public FanControlViewHolder(@NonNull ItemFanControlBinding binding, FanControlListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            
            setupListeners();
        }
        
        /**
         * Configura los listeners para los controles del ventilador
         */
        private void setupListeners() {
            // Listener para el slider de velocidad
            binding.sliderFanSpeed.addOnChangeListener(new Slider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                    if (fromUser && currentFanControl != null && listener != null) {
                        // Actualizar el texto de velocidad inmediatamente
                        binding.tvSpeedValue.setText(String.format("%d%%", (int) value));
                        
                        // Notificar al listener
                        listener.onFanSpeedChanged(currentFanControl.getDeviceId(), value);
                    }
                }
            });
            
            // Click listener para configuración (click largo en el card)
            binding.getRoot().setOnLongClickListener(v -> {
                if (currentFanControl != null && listener != null) {
                    listener.onFanConfigClicked(currentFanControl.getDeviceId());
                    return true;
                }
                return false;
            });
            
            // Click listener para detalles (click normal en el card)
            binding.getRoot().setOnClickListener(v -> {
                if (currentFanControl != null && listener != null) {
                    listener.onFanClicked(currentFanControl.getDeviceId());
                }
            });
        }
        
        /**
         * Vincula los datos del ventilador con la vista
         */
        public void bind(FanControl fanControl) {
            this.currentFanControl = fanControl;
            
            // Establecer datos usando Data Binding
            binding.setFanControl(fanControl);
            binding.setListener(listener);
            
            // Configurar el slider de velocidad
            binding.sliderFanSpeed.setValue(fanControl.getSpeedPercentage());
            binding.sliderFanSpeed.setEnabled(fanControl.getIsOn() && fanControl.getIsConnected());
            
            // Configurar visibilidad de elementos según el estado
            setupUIBasedOnState(fanControl);
            
            // Ejecutar binding inmediatamente
            binding.executePendingBindings();
        }
        
        /**
         * Configura la UI basada en el estado del ventilador
         */
        private void setupUIBasedOnState(FanControl fanControl) {
            // Alpha para elementos deshabilitados
            float alpha = fanControl.getIsOn() && fanControl.getIsConnected() ? 1.0f : 0.5f;
            binding.sliderFanSpeed.setAlpha(alpha);
            binding.tvSpeedValue.setAlpha(alpha);
            
            // Color del icono basado en el estado
            int iconTint;
            if (!fanControl.getIsConnected()) {
                iconTint = itemView.getContext().getColor(com.pdm.domohouse.R.color.error);
            } else if (fanControl.getIsOn()) {
                iconTint = itemView.getContext().getColor(com.pdm.domohouse.R.color.primary);
            } else {
                iconTint = itemView.getContext().getColor(com.pdm.domohouse.R.color.text_secondary);
            }
            
            binding.tvFanIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(iconTint));
        }
    }
}