package com.pdm.domohouse.ui.lights.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.pdm.domohouse.R;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.databinding.ItemLightDeviceBinding;

/**
 * Adaptador para mostrar dispositivos de iluminación individuales
 * Maneja diferentes tipos de controles según el tipo de dispositivo
 */
public class LightDevicesAdapter extends ListAdapter<Device, LightDevicesAdapter.LightDeviceViewHolder> {

    // Interface para manejar clicks en dispositivos
    public interface OnDeviceClickListener {
        void onDeviceToggle(String deviceId);
        void onIntensityChange(String deviceId, float intensity);
    }

    private OnDeviceClickListener clickListener;

    public LightDevicesAdapter() {
        super(DIFF_CALLBACK);
    }

    // DiffUtil callback para optimizar actualizaciones
    private static final DiffUtil.ItemCallback<Device> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<Device>() {
            @Override
            public boolean areItemsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
                return oldItem.equals(newItem) &&
                       oldItem.isEnabled() == newItem.isEnabled() &&
                       oldItem.getCurrentValue() == newItem.getCurrentValue() &&
                       oldItem.isConnected() == newItem.isConnected();
            }
        };

    @NonNull
    @Override
    public LightDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLightDeviceBinding binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()),
            R.layout.item_light_device,
            parent,
            false
        );
        return new LightDeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LightDeviceViewHolder holder, int position) {
        Device device = getItem(position);
        holder.bind(device, clickListener);
    }

    /**
     * Establece el listener para clicks en dispositivos
     */
    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * ViewHolder para dispositivos de luz individuales
     */
    static class LightDeviceViewHolder extends RecyclerView.ViewHolder {
        private final ItemLightDeviceBinding binding;
        private boolean isUpdatingSeekBar = false;

        public LightDeviceViewHolder(@NonNull ItemLightDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Vincula los datos del dispositivo con la vista
         */
        public void bind(Device device, OnDeviceClickListener clickListener) {
            binding.setDevice(device);
            binding.executePendingBindings();

            // Configurar vista según el tipo de dispositivo
            setupDeviceView(device, clickListener);

            // Actualizar estado visual
            updateDeviceStatus(device);
        }

        /**
         * Configura la vista según el tipo de dispositivo
         */
        private void setupDeviceView(Device device, OnDeviceClickListener clickListener) {
            // Configurar icono del dispositivo
            binding.imageDeviceIcon.setImageResource(getDeviceIconResource(device.getType()));

            if (device.getType() == DeviceType.LIGHT_SWITCH) {
                // Interruptor simple - solo switch
                binding.layoutIntensityControl.setVisibility(android.view.View.GONE);
                binding.switchDevice.setVisibility(android.view.View.VISIBLE);
                
                binding.switchDevice.setChecked(device.isEnabled());
                binding.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (clickListener != null) {
                        clickListener.onDeviceToggle(device.getId());
                    }
                });

            } else if (device.getType().hasRange()) {
                // Dispositivo con control de intensidad
                binding.layoutIntensityControl.setVisibility(android.view.View.VISIBLE);
                binding.switchDevice.setVisibility(android.view.View.VISIBLE);

                // Configurar switch principal
                binding.switchDevice.setChecked(device.isEnabled());
                binding.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (clickListener != null) {
                        clickListener.onDeviceToggle(device.getId());
                    }
                });

                // Configurar SeekBar para intensidad
                setupIntensityControl(device, clickListener);

            } else {
                // Dispositivo RGB u otros tipos especiales
                binding.layoutIntensityControl.setVisibility(android.view.View.VISIBLE);
                binding.switchDevice.setVisibility(android.view.View.VISIBLE);
                
                binding.switchDevice.setChecked(device.isEnabled());
                binding.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (clickListener != null) {
                        clickListener.onDeviceToggle(device.getId());
                    }
                });

                setupIntensityControl(device, clickListener);
            }
        }

        /**
         * Configura el control de intensidad (SeekBar)
         */
        private void setupIntensityControl(Device device, OnDeviceClickListener clickListener) {
            int progress = device.getValuePercentage();
            
            isUpdatingSeekBar = true;
            binding.seekBarIntensity.setProgress(progress);
            isUpdatingSeekBar = false;
            
            binding.textIntensityValue.setText(String.format("%d%%", progress));

            binding.seekBarIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && !isUpdatingSeekBar) {
                        // Actualizar texto inmediatamente para respuesta rápida
                        binding.textIntensityValue.setText(String.format("%d%%", progress));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // No hacer nada al empezar
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Enviar cambio cuando el usuario termine de ajustar
                    if (clickListener != null) {
                        float newValue = (seekBar.getProgress() / 100.0f) * device.getMaxValue();
                        clickListener.onIntensityChange(device.getId(), newValue);
                    }
                }
            });

            // Habilitar/deshabilitar SeekBar según el estado del dispositivo
            binding.seekBarIntensity.setEnabled(device.isEnabled() && device.isConnected());
        }

        /**
         * Actualiza el estado visual del dispositivo
         */
        private void updateDeviceStatus(Device device) {
            // Actualizar indicadores de estado
            if (!device.isConnected()) {
                binding.textDeviceStatus.setVisibility(android.view.View.VISIBLE);
                binding.textDeviceStatus.setText("Desconectado");
                binding.textDeviceStatus.setTextColor(
                    binding.getRoot().getContext().getResources().getColor(R.color.error)
                );
            } else if (device.getBatteryLevel() < 20 && device.getType().isBatteryPowered()) {
                binding.textDeviceStatus.setVisibility(android.view.View.VISIBLE);
                binding.textDeviceStatus.setText("Batería baja");
                binding.textDeviceStatus.setTextColor(
                    binding.getRoot().getContext().getResources().getColor(R.color.warning)
                );
            } else {
                binding.textDeviceStatus.setVisibility(android.view.View.GONE);
            }

            // Actualizar valor actual
            binding.textCurrentValue.setText(device.getFormattedValue());

            // Habilitar/deshabilitar controles
            boolean enabled = device.isConnected();
            binding.switchDevice.setEnabled(enabled);
            
            if (binding.seekBarIntensity.getVisibility() == android.view.View.VISIBLE) {
                binding.seekBarIntensity.setEnabled(enabled && device.isEnabled());
            }

            // Actualizar opacidad del contenedor según el estado
            float alpha = enabled ? 1.0f : 0.6f;
            binding.getRoot().setAlpha(alpha);
        }

        /**
         * Obtiene el recurso de icono para el tipo de dispositivo
         */
        private int getDeviceIconResource(DeviceType deviceType) {
            switch (deviceType) {
                case LIGHT_SWITCH:
                    return R.drawable.ic_lightbulb;
                case LIGHT_DIMMER:
                    return R.drawable.ic_light_dimmer;
                case RGB_LIGHT:
                    return R.drawable.ic_light_rgb;
                default:
                    return R.drawable.ic_lightbulb;
            }
        }
    }
}