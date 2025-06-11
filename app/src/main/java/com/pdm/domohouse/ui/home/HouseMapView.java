package com.pdm.domohouse.ui.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.pdm.domohouse.R;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista personalizada que muestra una maqueta interactiva de la casa
 * Permite visualizar habitaciones, estados y interactuar con ellas
 */
public class HouseMapView extends View {

    // Listener para clicks en habitaciones
    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    // Lista de habitaciones a mostrar
    private List<Room> rooms = new ArrayList<>();
    
    // Listener para eventos de click
    private OnRoomClickListener onRoomClickListener;

    // Paints para dibujar diferentes elementos
    private Paint roomPaint;           // Para el fondo de habitaciones
    private Paint borderPaint;         // Para bordes de habitaciones
    private Paint textPaint;           // Para texto de habitaciones
    private Paint statusPaint;         // Para indicadores de estado
    private Paint shadowPaint;         // Para sombras
    private Paint backgroundPaint;     // Para fondo general

    // Colores de la paleta
    private int colorPrimary;
    private int colorPrimaryLight;
    private int colorBackground;
    private int colorSurface;
    private int colorTextPrimary;
    private int colorTextSecondary;
    private int colorSuccess;
    private int colorWarning;
    private int colorError;

    // Dimensiones y configuración
    private float density;
    private float roomCornerRadius;
    private float statusIndicatorRadius;
    private float textSize;
    private float titleTextSize;
    private float paddingDp;

    // Habitación actualmente seleccionada
    private Room selectedRoom;
    
    // Animación de selección
    private ValueAnimator selectionAnimator;
    private float selectionScale = 1.0f;

    // Rectángulos temporales para cálculos
    private RectF tempRect = new RectF();

    public HouseMapView(Context context) {
        super(context);
        init(context);
    }

    public HouseMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HouseMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Inicializa la vista con configuraciones y colores
     */
    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        
        // Cargar colores de la paleta
        loadColors(context);
        
        // Configurar dimensiones
        roomCornerRadius = 8 * density;
        statusIndicatorRadius = 6 * density;
        textSize = 12 * density;
        titleTextSize = 14 * density;
        paddingDp = 16 * density;

        // Configurar paints
        setupPaints();

        // Habilitar clicks
        setClickable(true);
    }

    /**
     * Carga los colores de la paleta definida
     */
    private void loadColors(Context context) {
        colorPrimary = ContextCompat.getColor(context, R.color.primary);
        colorPrimaryLight = ContextCompat.getColor(context, R.color.primary_light);
        colorBackground = ContextCompat.getColor(context, R.color.background);
        colorSurface = ContextCompat.getColor(context, R.color.surface);
        colorTextPrimary = ContextCompat.getColor(context, R.color.text_primary);
        colorTextSecondary = ContextCompat.getColor(context, R.color.text_secondary);
        colorSuccess = ContextCompat.getColor(context, R.color.success);
        colorWarning = ContextCompat.getColor(context, R.color.warning);
        colorError = ContextCompat.getColor(context, R.color.error);
    }

    /**
     * Configura todos los objetos Paint necesarios
     */
    private void setupPaints() {
        // Paint para habitaciones
        roomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roomPaint.setStyle(Paint.Style.FILL);
        roomPaint.setColor(colorSurface);

        // Paint para bordes
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2 * density);
        borderPaint.setColor(colorPrimary);

        // Paint para texto
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(colorTextPrimary);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Paint para indicadores de estado
        statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPaint.setStyle(Paint.Style.FILL);

        // Paint para sombras
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(30, 0, 0, 0));

        // Paint para fondo
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(colorBackground);
    }

    /**
     * Establece la lista de habitaciones a mostrar
     */
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms != null ? new ArrayList<>(rooms) : new ArrayList<>();
        invalidate();
    }

    /**
     * Establece el listener para clicks en habitaciones
     */
    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.onRoomClickListener = listener;
    }

    /**
     * Selecciona una habitación con animación
     */
    public void selectRoom(Room room) {
        this.selectedRoom = room;
        
        // Animar selección
        if (selectionAnimator != null) {
            selectionAnimator.cancel();
        }
        
        selectionAnimator = ValueAnimator.ofFloat(1.0f, 1.1f, 1.0f);
        selectionAnimator.setDuration(300);
        selectionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        selectionAnimator.addUpdateListener(animation -> {
            selectionScale = (Float) animation.getAnimatedValue();
            invalidate();
        });
        selectionAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rooms.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        // Dibujar fondo
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Dibujar título
        drawTitle(canvas);

        // Dibujar habitaciones
        for (Room room : rooms) {
            drawRoom(canvas, room);
        }

        // Dibujar leyenda
        drawLegend(canvas);
    }

    /**
     * Dibuja el estado vacío cuando no hay habitaciones
     */
    private void drawEmptyState(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        
        textPaint.setColor(colorTextSecondary);
        textPaint.setTextSize(titleTextSize);
        
        String emptyText = "Cargando maqueta de casa...";
        float x = getWidth() / 2f;
        float y = getHeight() / 2f;
        
        canvas.drawText(emptyText, x, y, textPaint);
    }

    /**
     * Dibuja el título de la vista
     */
    private void drawTitle(Canvas canvas) {
        textPaint.setColor(colorTextPrimary);
        textPaint.setTextSize(titleTextSize);
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        String title = "Maqueta de Casa - Domo House";
        canvas.drawText(title, paddingDp, paddingDp + titleTextSize, textPaint);
    }

    /**
     * Dibuja una habitación individual
     */
    private void drawRoom(Canvas canvas, Room room) {
        // Calcular rectángulo de la habitación
        RectF roomRect = getRoomRect(room);
        
        // Aplicar escala de selección si está seleccionada
        if (room.equals(selectedRoom)) {
            float centerX = roomRect.centerX();
            float centerY = roomRect.centerY();
            float width = roomRect.width() * selectionScale;
            float height = roomRect.height() * selectionScale;
            
            roomRect.set(
                centerX - width / 2,
                centerY - height / 2,
                centerX + width / 2,
                centerY + height / 2
            );
        }

        // Dibujar sombra
        tempRect.set(roomRect);
        tempRect.offset(3 * density, 3 * density);
        canvas.drawRoundRect(tempRect, roomCornerRadius, roomCornerRadius, shadowPaint);

        // Dibujar fondo de la habitación
        roomPaint.setColor(getRoomBackgroundColor(room));
        canvas.drawRoundRect(roomRect, roomCornerRadius, roomCornerRadius, roomPaint);

        // Dibujar borde
        borderPaint.setColor(getRoomBorderColor(room));
        canvas.drawRoundRect(roomRect, roomCornerRadius, roomCornerRadius, borderPaint);

        // Dibujar icono del tipo de habitación
        drawRoomIcon(canvas, room, roomRect);

        // Dibujar nombre de la habitación
        drawRoomName(canvas, room, roomRect);

        // Dibujar indicador de estado
        drawStatusIndicator(canvas, room, roomRect);

        // Dibujar información adicional
        drawRoomInfo(canvas, room, roomRect);
    }

    /**
     * Calcula el rectángulo de una habitación basado en sus coordenadas
     */
    private RectF getRoomRect(Room room) {
        float availableWidth = getWidth() - 2 * paddingDp;
        float availableHeight = getHeight() - 4 * paddingDp - titleTextSize;
        float startY = 2 * paddingDp + titleTextSize;

        float left = paddingDp + room.getPositionX() * availableWidth;
        float top = startY + room.getPositionY() * availableHeight;
        float right = left + room.getWidth() * availableWidth;
        float bottom = top + room.getHeight() * availableHeight;

        return new RectF(left, top, right, bottom);
    }

    /**
     * Obtiene el color de fondo de una habitación basado en su estado
     */
    private int getRoomBackgroundColor(Room room) {
        if (room.equals(selectedRoom)) {
            return colorPrimaryLight;
        }

        switch (room.getOverallStatus()) {
            case ACTIVE:
                return Color.argb(40, Color.red(colorPrimary), Color.green(colorPrimary), Color.blue(colorPrimary));
            case WARNING:
                return Color.argb(40, Color.red(colorWarning), Color.green(colorWarning), Color.blue(colorWarning));
            case ALERT:
                return Color.argb(40, Color.red(colorError), Color.green(colorError), Color.blue(colorError));
            default:
                return colorSurface;
        }
    }

    /**
     * Obtiene el color del borde de una habitación
     */
    private int getRoomBorderColor(Room room) {
        if (room.equals(selectedRoom)) {
            return colorPrimary;
        }

        switch (room.getOverallStatus()) {
            case ACTIVE:
                return colorPrimary;
            case WARNING:
                return colorWarning;
            case ALERT:
                return colorError;
            default:
                return Color.argb(100, Color.red(colorPrimary), Color.green(colorPrimary), Color.blue(colorPrimary));
        }
    }

    /**
     * Dibuja el icono del tipo de habitación
     */
    private void drawRoomIcon(Canvas canvas, Room room, RectF roomRect) {
        textPaint.setTextSize(titleTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(colorTextPrimary);

        String icon = room.getType().getIcon();
        float x = roomRect.centerX();
        float y = roomRect.top + titleTextSize + 8 * density;

        canvas.drawText(icon, x, y, textPaint);
    }

    /**
     * Dibuja el nombre de la habitación
     */
    private void drawRoomName(Canvas canvas, Room room, RectF roomRect) {
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(colorTextPrimary);

        String name = room.getName();
        if (name.length() > 12) {
            name = name.substring(0, 9) + "...";
        }

        float x = roomRect.centerX();
        float y = roomRect.centerY() + textSize / 2;

        canvas.drawText(name, x, y, textPaint);
    }

    /**
     * Dibuja el indicador de estado de la habitación
     */
    private void drawStatusIndicator(Canvas canvas, Room room, RectF roomRect) {
        RoomStatus status = room.getOverallStatus();
        statusPaint.setColor(status.getColorInt());

        float x = roomRect.right - statusIndicatorRadius - 4 * density;
        float y = roomRect.top + statusIndicatorRadius + 4 * density;

        canvas.drawCircle(x, y, statusIndicatorRadius, statusPaint);
    }

    /**
     * Dibuja información adicional de la habitación
     */
    private void drawRoomInfo(Canvas canvas, Room room, RectF roomRect) {
        textPaint.setTextSize(textSize * 0.8f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(colorTextSecondary);

        String info = String.format("%.1f°C", room.getTemperature());
        if (room.getActiveDevices() > 0) {
            info += " • " + room.getActiveDevices() + " activos";
        }

        float x = roomRect.centerX();
        float y = roomRect.bottom - 8 * density;

        canvas.drawText(info, x, y, textPaint);
    }

    /**
     * Dibuja la leyenda de estados
     */
    private void drawLegend(Canvas canvas) {
        float legendY = getHeight() - paddingDp - 20 * density;
        float legendX = paddingDp;

        textPaint.setTextSize(textSize * 0.9f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(colorTextSecondary);

        // Títulos de leyenda
        canvas.drawText("Estados:", legendX, legendY, textPaint);

        float spacing = 80 * density;
        String[] statuses = {"Normal", "Activo", "Advertencia", "Alerta"};
        int[] colors = {colorSuccess, colorPrimary, colorWarning, colorError};

        for (int i = 0; i < statuses.length; i++) {
            float x = legendX + (i + 1) * spacing;
            
            // Dibujar círculo de color
            statusPaint.setColor(colors[i]);
            canvas.drawCircle(x, legendY - textSize / 3, 4 * density, statusPaint);
            
            // Dibujar texto
            canvas.drawText(statuses[i], x + 8 * density, legendY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Buscar habitación tocada
            for (Room room : rooms) {
                RectF roomRect = getRoomRect(room);
                if (roomRect.contains(touchX, touchY)) {
                    selectRoom(room);
                    if (onRoomClickListener != null) {
                        onRoomClickListener.onRoomClick(room);
                    }
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Establecer un tamaño mínimo para la vista
        int minWidth = (int) (300 * density);
        int minHeight = (int) (400 * density);

        int width = resolveSize(minWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}