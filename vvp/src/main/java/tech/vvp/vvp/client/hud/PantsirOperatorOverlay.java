package tech.vvp.vvp.client.hud;

import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.tools.VectorToolKt;
import com.atsuishio.portlib.gui.LegacyRenderSystem;
import com.atsuishio.portlib.gui.LegacyPose;
import net.minecraft.client.Minecraft;
import com.atsuishio.portlib.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.client.PantsirClientHandler;
import tech.vvp.vvp.entity.vehicle.PantsirS1Entity;
import tech.vvp.vvp.network.VVPNetwork;
import tech.vvp.vvp.network.message.PantsirLockRequestMessage;
import tech.vvp.vvp.network.message.PantsirRadarSyncMessage;

@Environment(EnvType.CLIENT)
public class PantsirOperatorOverlay {

    private static final int COLOR_RADAR_GREEN = 0xFF00FF00;
    private static final int COLOR_RADAR_DARK = 0xFF003300;
    private static final int COLOR_RADAR_BG = 0xE0001100;
    private static final int COLOR_TARGET_YELLOW = 0xFFFFFF00;
    private static final int COLOR_TARGET_RED = 0xFFFF3333;
    private static final int COLOR_LOCKED = 0xFF00FF00;
    private static final int COLOR_TARGET_BLIP = 0xFFFF6600;
    private static final int COLOR_TURRET_BEAM = 0xFFFFFF00; // Жёлтый вместо фиолетового
    private static final int COLOR_SSC_SECTOR = 0x40FFFF00; // Жёлтый вместо фиолетового
    private static final int COLOR_PANEL_BG = 0xC0000000; // Полупрозрачный чёрный для фона панели
    private static final int COLOR_MISSILE = 0xFF00FFFF; // Голубой для ракет
    
    private static final int RADAR_RADIUS = 60;
    private static final int RADAR_SEGMENTS = 32;
    private static final int MARKER_SIZE = 24;
    private static final int MARKER_HALF = MARKER_SIZE / 2;
    private static final double RADAR_RANGE = 1100.0;
    private static final float SSC_HALF_ANGLE = 3.0f;
    
    private static boolean wasLockKeyPressed = false;
    private static int lastRadarState = -1;
    private static long lastLockingSoundTime = 0;
    private static boolean wasNextKeyPressed = false;
    private static boolean wasPrevKeyPressed = false;

    private static GuiGraphics gfx;

    public static void onRenderGui(GuiGraphics guiGraphics) {
        gfx = guiGraphics;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null || mc.gui.hud.isHidden()) return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof PantsirS1Entity pantsir)) {
            PantsirClientHandler.reset();
            return;
        }
        
        int seatIndex = pantsir.getSeatIndex(player);
        if (seatIndex != 1) return;
        
        int vehicleId = pantsir.getId();
        
        handleLockKeyInput(player);
        handleTargetSwitchInput(player);
        handleLockSounds(vehicleId);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        LegacyPose poseStack = guiGraphics.pose();
        poseStack.pushPose();
        
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        
        renderRadarDisplay(guiGraphics, poseStack, screenWidth, screenHeight, player, vehicleId);
        renderStatusPanel(guiGraphics, screenWidth, screenHeight, vehicleId);
        renderTargetMarker(guiGraphics, screenWidth, screenHeight, vehicleId);
        renderControlHint(guiGraphics, screenWidth, screenHeight, vehicleId);
        
        poseStack.popPose();
    }
    
    private static void handleLockKeyInput(Player player) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof PantsirS1Entity pantsir)) return;
        
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(pantsir.getId());
        if (data == null) return;
        
        // Используем кнопку VEHICLE_SEEK из SBW (X по умолчанию)
        boolean isPressed = ModKeyMappings.VEHICLE_SEEK.isDown();
        
        if (isPressed && !wasLockKeyPressed) {
            int state = data.radarState;
            
            // X начинает захват когда цель обнаружена
            if (state == PantsirRadarSyncMessage.STATE_DETECTED) {
                VVPNetwork.VVP_HANDLER.sendToServer(
                    new PantsirLockRequestMessage(PantsirLockRequestMessage.ACTION_START_LOCK)
                );
            } 
            // X отменяет захват во время LOCKING или сбрасывает когда LOCKED
            else if (state == PantsirRadarSyncMessage.STATE_LOCKING || 
                     state == PantsirRadarSyncMessage.STATE_LOCKED) {
                VVPNetwork.VVP_HANDLER.sendToServer(
                    new PantsirLockRequestMessage(PantsirLockRequestMessage.ACTION_CANCEL_LOCK)
                );
            }
        }
        wasLockKeyPressed = isPressed;
    }
    
    /**
     * Обработка переключения целей стрелочками
     */
    private static void handleTargetSwitchInput(Player player) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof PantsirS1Entity pantsir)) return;
        
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(pantsir.getId());
        if (data == null) return;
        
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().handle();
        
        // Стрелка влево - предыдущая цель
        boolean leftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
        if (leftPressed && !wasPrevKeyPressed) {
            if (data.radarState != PantsirRadarSyncMessage.STATE_LOCKING &&
                data.radarState != PantsirRadarSyncMessage.STATE_LOCKED) {
                VVPNetwork.VVP_HANDLER.sendToServer(
                    new PantsirLockRequestMessage(PantsirLockRequestMessage.ACTION_PREV_TARGET)
                );
            }
        }
        wasPrevKeyPressed = leftPressed;
        
        // Стрелка вправо - следующая цель
        boolean rightPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
        if (rightPressed && !wasNextKeyPressed) {
            if (data.radarState != PantsirRadarSyncMessage.STATE_LOCKING &&
                data.radarState != PantsirRadarSyncMessage.STATE_LOCKED) {
                VVPNetwork.VVP_HANDLER.sendToServer(
                    new PantsirLockRequestMessage(PantsirLockRequestMessage.ACTION_NEXT_TARGET)
                );
            }
        }
        wasNextKeyPressed = rightPressed;
    }
    
    private static void handleLockSounds(int vehicleId) {
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(vehicleId);
        if (data == null) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int currentState = data.radarState;
        long now = System.currentTimeMillis();

        // Звук в начале LOCKING (один раз при переходе в состояние)
        if (currentState == PantsirRadarSyncMessage.STATE_LOCKING && lastRadarState != PantsirRadarSyncMessage.STATE_LOCKING) {
            player.playSound(ModSounds.MISSILE_LOCKING, 2.0f, 1.0f);
            lastLockingSoundTime = now;
        }

        // Звук LOCKED - не каждый кадр, а раз в 500мс
        if (currentState == PantsirRadarSyncMessage.STATE_LOCKED && now - lastLockingSoundTime > 500) {
            player.playSound(ModSounds.MISSILE_LOCKED, 2.0f, 1.0f);
            lastLockingSoundTime = now;
        }

        lastRadarState = currentState;
    }

    private static void renderRadarDisplay(GuiGraphics guiGraphics, LegacyPose poseStack, 
            int screenWidth, int screenHeight, Player player, int vehicleId) {
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(vehicleId);
        if (data == null) return;
        int centerX = screenWidth - RADAR_RADIUS - 25;
        int centerY = RADAR_RADIUS + 40;
        
        // Тень под радаром
        drawFilledCircle(poseStack, centerX + 2, centerY + 2, RADAR_RADIUS + 2, 0x80000000);
        
        // Фон радара с градиентом
        drawFilledCircle(poseStack, centerX, centerY, RADAR_RADIUS, COLOR_RADAR_BG);
        
        // Сетка радара (линии через центр)
        int gridColor = (COLOR_RADAR_DARK & 0x00FFFFFF) | 0x40000000;
        // Диагональные линии
        int diag = (int)(RADAR_RADIUS * 0.707); // cos(45°) ≈ 0.707
        drawLine(poseStack, centerX - diag, centerY - diag, centerX + diag, centerY + diag, gridColor, 1);
        drawLine(poseStack, centerX - diag, centerY + diag, centerX + diag, centerY - diag, gridColor, 1);
        
        // Концентрические круги с разной яркостью
        drawCircleOutline(poseStack, centerX, centerY, RADAR_RADIUS * 0.25f, COLOR_RADAR_DARK);
        drawCircleOutline(poseStack, centerX, centerY, RADAR_RADIUS * 0.5f, COLOR_RADAR_DARK);
        drawCircleOutline(poseStack, centerX, centerY, RADAR_RADIUS * 0.75f, COLOR_RADAR_DARK);
        
        // Внешняя рамка с эффектом свечения (двойная)
        drawCircleOutline(poseStack, centerX, centerY, RADAR_RADIUS - 2, COLOR_RADAR_GREEN);
        drawCircleOutline(poseStack, centerX, centerY, RADAR_RADIUS - 1, (COLOR_RADAR_GREEN & 0x00FFFFFF) | 0x80000000);
        drawCircleOutline(poseStack, centerX, centerY, RADAR_RADIUS, (COLOR_RADAR_GREEN & 0x00FFFFFF) | 0x40000000);
        
        // Перекрестие с улучшенным дизайном
        guiGraphics.fill(centerX - 1, centerY - 10, centerX + 1, centerY + 10, COLOR_RADAR_DARK);
        guiGraphics.fill(centerX - 10, centerY - 1, centerX + 10, centerY + 1, COLOR_RADAR_DARK);
        // Центральная точка с свечением
        drawFilledCircle(poseStack, centerX, centerY, 3, (COLOR_RADAR_GREEN & 0x00FFFFFF) | 0x60000000);
        guiGraphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, COLOR_RADAR_GREEN);
        
        // Полоска 1: Вращающийся обзорный радар (зелёный)
        drawRadarSweep(poseStack, centerX, centerY, vehicleId, data);
        
        // Полоска 2: Пассивный радар - направление башни (жёлтый)
        drawTurretBeam(poseStack, centerX, centerY, data);
        
        // Все цели на радаре
        drawAllTargetBlips(guiGraphics, poseStack, centerX, centerY, player, data);
        
        // Ракеты на радаре (с пунктирной линией)
        drawMissiles(guiGraphics, poseStack, centerX, centerY, player, data);
        
        Minecraft mc = Minecraft.getInstance();
        // Буквы направлений с тенью для лучшей читаемости
        guiGraphics.drawString(mc.font, "N", centerX - 3, centerY - RADAR_RADIUS - 12, COLOR_RADAR_GREEN, true);
        guiGraphics.drawString(mc.font, "S", centerX - 3, centerY + RADAR_RADIUS + 4, COLOR_RADAR_GREEN, true);
        guiGraphics.drawString(mc.font, "W", centerX - RADAR_RADIUS - 10, centerY - 4, COLOR_RADAR_GREEN, true);
        guiGraphics.drawString(mc.font, "E", centerX + RADAR_RADIUS + 4, centerY - 4, COLOR_RADAR_GREEN, true);
        
        // Дистанция с улучшенным фоном
        String rangeText = "1100m";
        int rangeWidth = mc.font.width(rangeText);
        guiGraphics.fill(centerX + RADAR_RADIUS - rangeWidth - 6, centerY - RADAR_RADIUS + 1, 
                        centerX + RADAR_RADIUS, centerY - RADAR_RADIUS + 11, 0xC0000000);
        guiGraphics.drawString(mc.font, rangeText, centerX + RADAR_RADIUS - rangeWidth - 4, 
                              centerY - RADAR_RADIUS + 3, COLOR_RADAR_GREEN, false);
        
        // Счётчик целей с улучшенным фоном и иконкой
        int targetCount = data.allTargets.size();
        if (targetCount > 0) {
            String tgtText = "◎ " + targetCount;
            int tgtWidth = mc.font.width(tgtText);
            guiGraphics.fill(centerX - RADAR_RADIUS + 1, centerY - RADAR_RADIUS + 1, 
                            centerX - RADAR_RADIUS + tgtWidth + 9, centerY - RADAR_RADIUS + 11, 0xC0000000);
            guiGraphics.drawString(mc.font, tgtText, centerX - RADAR_RADIUS + 5, 
                centerY - RADAR_RADIUS + 3, COLOR_TARGET_YELLOW, false);
        }
    }
    
    private static void drawRadarSweep(LegacyPose poseStack, int centerX, int centerY, int vehicleId, PantsirClientHandler.PantsirRadarData data) {
        // radarAngle от сервера - абсолютный угол вращения радара (в градусах, как yaw)
        float radarAngle = PantsirClientHandler.getInterpolatedRadarAngle(vehicleId);
        
        // Та же формула что и для turret beam
        double radians = Math.toRadians(radarAngle);
        
        int beamLength = RADAR_RADIUS - 5;
        int endX = centerX + (int)(Math.sin(radians) * beamLength);
        int endY = centerY + (int)(Math.cos(radians) * beamLength);
        
        int beamColor = switch (data.radarState) {
            case PantsirRadarSyncMessage.STATE_LOCKED -> COLOR_LOCKED;
            case PantsirRadarSyncMessage.STATE_LOCKING, PantsirRadarSyncMessage.STATE_DETECTED -> COLOR_TARGET_YELLOW;
            default -> COLOR_RADAR_GREEN;
        };
        
        // Рисуем затухающий след за лучом (эффект свечения)
        float sectorAngle = 15.0f;
        int trailSegments = 8;
        for (int i = 0; i < trailSegments; i++) {
            float trailAngle = radarAngle - (i * 3.0f); // След за лучом
            double trailRad = Math.toRadians(trailAngle);
            
            double leftRad = trailRad - Math.toRadians(sectorAngle);
            double rightRad = trailRad + Math.toRadians(sectorAngle);
            
            int leftX = centerX + (int)(Math.sin(leftRad) * beamLength);
            int leftY = centerY + (int)(Math.cos(leftRad) * beamLength);
            int rightX = centerX + (int)(Math.sin(rightRad) * beamLength);
            int rightY = centerY + (int)(Math.cos(rightRad) * beamLength);
            
            // Затухающая прозрачность
            float alpha = (1.0f - (i / (float)trailSegments)) * 0.15f;
            int trailColor = (beamColor & 0x00FFFFFF) | ((int)(alpha * 255) << 24);
            
            drawTriangle(poseStack, centerX, centerY, leftX, leftY, rightX, rightY, trailColor);
        }
        
        // Основной луч с утолщением
        drawLine(poseStack, centerX, centerY, endX, endY, beamColor, 3);
        
        // Яркая точка на конце луча
        int glowSize = 3;
        drawFilledCircle(poseStack, endX, endY, glowSize, beamColor);
        
        // Сектор сканирования
        double leftRad = radians - Math.toRadians(sectorAngle);
        double rightRad = radians + Math.toRadians(sectorAngle);
        
        int leftX = centerX + (int)(Math.sin(leftRad) * beamLength);
        int leftY = centerY + (int)(Math.cos(leftRad) * beamLength);
        int rightX = centerX + (int)(Math.sin(rightRad) * beamLength);
        int rightY = centerY + (int)(Math.cos(rightRad) * beamLength);
        
        // Полупрозрачный сектор
        int sectorAlpha = (beamColor & 0x00FFFFFF) | 0x30000000;
        drawTriangle(poseStack, centerX, centerY, leftX, leftY, rightX, rightY, sectorAlpha);
    }
    
    /**
     * Рисует ССЦ (Станция Сопровождения Целей) - узкий сектор ±3° от направления башни
     * Стабилизированный - север = вверх
     */
    private static void drawTurretBeam(LegacyPose poseStack, int centerX, int centerY, PantsirClientHandler.PantsirRadarData data) {
        float turretAngle = data.turretAngle;
        double radians = Math.toRadians(turretAngle);
        
        int beamLength = RADAR_RADIUS - 5;
        
        // W/E инвертированы - меняем знак X
        int endX = centerX - (int)(Math.sin(radians) * beamLength);
        int endY = centerY + (int)(Math.cos(radians) * beamLength);
        drawLine(poseStack, centerX, centerY, endX, endY, COLOR_TURRET_BEAM, 2);
        
        // Сектор ССЦ (±3°)
        double leftRad = radians - Math.toRadians(SSC_HALF_ANGLE);
        double rightRad = radians + Math.toRadians(SSC_HALF_ANGLE);
        
        int leftX = centerX - (int)(Math.sin(leftRad) * beamLength);
        int leftY = centerY + (int)(Math.cos(leftRad) * beamLength);
        int rightX = centerX - (int)(Math.sin(rightRad) * beamLength);
        int rightY = centerY + (int)(Math.cos(rightRad) * beamLength);
        
        // Полупрозрачный сектор ССЦ
        drawTriangle(poseStack, centerX, centerY, leftX, leftY, rightX, rightY, COLOR_SSC_SECTOR);
    }

    private static void drawAllTargetBlips(GuiGraphics guiGraphics, LegacyPose poseStack, int centerX, int centerY, Player player, PantsirClientHandler.PantsirRadarData data) {
        // Стабилизированный радар - используем позицию панциря, не игрока
        Entity vehicle = player.getVehicle();
        if (vehicle == null) return;
        
        Vec3 radarPos = vehicle.position();
        
        for (PantsirClientHandler.RadarTarget target : data.allTargets) {
            Vec3 targetPos = new Vec3(target.x, target.y, target.z);
            Vec3 toTarget = targetPos.subtract(radarPos);
            double distance = toTarget.horizontalDistance();
            
            double normalizedDist = Math.min(distance / RADAR_RANGE, 1.0);
            
            // В Minecraft: Z+ = Юг, Z- = Север, X+ = Восток, X- = Запад
            // На радаре: вверх = Север (Y-), вправо = Восток (X+)
            // toTarget.x > 0 = восток = вправо на радаре
            // toTarget.z < 0 = север = вверх на радаре
            int radarDist = (int)(normalizedDist * (RADAR_RADIUS - 8));
            int blipX = centerX + (int)(toTarget.x / distance * radarDist);
            int blipY = centerY + (int)(toTarget.z / distance * radarDist);
            
            boolean isMainTarget = (target.entityId == data.targetEntityId);
            int blipColor;
            
            // Союзники всегда зелёные
            if (target.isAlly) {
                blipColor = COLOR_RADAR_GREEN;
            } else if (isMainTarget) {
                blipColor = switch (data.radarState) {
                    case PantsirRadarSyncMessage.STATE_LOCKED -> COLOR_LOCKED;
                    case PantsirRadarSyncMessage.STATE_LOCKING -> COLOR_TARGET_YELLOW;
                    default -> COLOR_TARGET_RED;
                };
            } else {
                // Вражеские ракеты - красные, остальное - оранжевое
                blipColor = (target.targetType == PantsirRadarSyncMessage.TARGET_TYPE_MISSILE) 
                    ? COLOR_TARGET_RED : COLOR_TARGET_BLIP;
            }
            
            // Рисуем иконку в зависимости от типа цели
            drawTargetIcon(guiGraphics, poseStack, blipX, blipY, target.targetType, blipColor, isMainTarget);
            
            // Рамка для залоченной цели
            if (isMainTarget && data.radarState == PantsirRadarSyncMessage.STATE_LOCKED) {
                long time = System.currentTimeMillis();
                if ((time / 250) % 2 == 0) {
                    int size = 6;
                    int thickness = 2;
                    // Угловые скобки вместо полной рамки
                    guiGraphics.fill(blipX - size - 1, blipY - size - 1, blipX - size + 3, blipY - size - 1 + thickness, blipColor);
                    guiGraphics.fill(blipX - size - 1, blipY - size - 1, blipX - size - 1 + thickness, blipY - size + 3, blipColor);
                    
                    guiGraphics.fill(blipX + size - 2, blipY - size - 1, blipX + size + 1, blipY - size - 1 + thickness, blipColor);
                    guiGraphics.fill(blipX + size - 1, blipY - size - 1, blipX + size + 1, blipY - size + 3, blipColor);
                    
                    guiGraphics.fill(blipX - size - 1, blipY + size - 1, blipX - size + 3, blipY + size + 1, blipColor);
                    guiGraphics.fill(blipX - size - 1, blipY + size - 2, blipX - size - 1 + thickness, blipY + size + 1, blipColor);
                    
                    guiGraphics.fill(blipX + size - 2, blipY + size - 1, blipX + size + 1, blipY + size + 1, blipColor);
                    guiGraphics.fill(blipX + size - 1, blipY + size - 2, blipX + size + 1, blipY + size + 1, blipColor);
                }
            }
            
            // Анимация захвата для locking цели
            if (isMainTarget && data.radarState == PantsirRadarSyncMessage.STATE_LOCKING) {
                long time = System.currentTimeMillis();
                float progress = data.lockProgress / 100.0f;
                int ringSize = (int)(8 + Math.sin(time / 100.0) * 2);
                
                // Пульсирующее кольцо
                int ringAlpha = (int)((0.5f + Math.sin(time / 150.0) * 0.3f) * 255);
                int ringColor = (blipColor & 0x00FFFFFF) | (ringAlpha << 24);
                drawCircleOutline(poseStack, blipX, blipY, ringSize * progress, ringColor);
            }
        }
    }
    
    /**
     * Рисует иконку цели в зависимости от типа
     */
    private static void drawTargetIcon(GuiGraphics guiGraphics, LegacyPose poseStack, int x, int y, int targetType, int color, boolean isMain) {
        int size = isMain ? 3 : 2;
        
        // Эффект пульсации для главной цели
        if (isMain) {
            long time = System.currentTimeMillis();
            float pulse = (float)(Math.sin(time / 200.0) * 0.3 + 0.7); // 0.4 - 1.0
            int pulseAlpha = (int)(pulse * 255);
            int glowColor = (color & 0x00FFFFFF) | (pulseAlpha << 24);
            int glowSize = size + 2;
            
            // Внешнее свечение
            drawFilledCircle(poseStack, x, y, glowSize, glowColor);
        }
        
        switch (targetType) {
            case PantsirRadarSyncMessage.TARGET_TYPE_HELICOPTER -> {
                // Вертолёт: улучшенный крестик с точкой в центре
                guiGraphics.fill(x - size, y - 1, x + size, y + 1, color); // горизонталь
                guiGraphics.fill(x - 1, y - size, x + 1, y + size, color); // вертикаль
                // Центральная точка (винт)
                guiGraphics.fill(x - 2, y - 2, x + 2, y + 2, color);
                // Внешний контур для объёма
                if (isMain) {
                    guiGraphics.fill(x - size - 1, y - 1, x - size, y + 1, color);
                    guiGraphics.fill(x + size, y - 1, x + size + 1, y + 1, color);
                    guiGraphics.fill(x - 1, y - size - 1, x + 1, y - size, color);
                    guiGraphics.fill(x - 1, y + size, x + 1, y + size + 1, color);
                }
            }
            case PantsirRadarSyncMessage.TARGET_TYPE_AIRPLANE -> {
                // Самолёт: улучшенный треугольник с крыльями
                drawTriangleUp(poseStack, x, y - size, x - size, y + size, x + size, y + size, color);
                // Крылья (горизонтальная линия)
                guiGraphics.fill(x - size - 1, y, x + size + 1, y + 1, color);
                // Контур для объёма
                if (isMain) {
                    drawLine(poseStack, x, y - size - 1, x - size - 1, y + size, color, 1);
                    drawLine(poseStack, x, y - size - 1, x + size + 1, y + size, color, 1);
                }
            }
            case PantsirRadarSyncMessage.TARGET_TYPE_MISSILE -> {
                // Вражеская ракета: улучшенный ромб с хвостом
                drawDiamond(poseStack, x, y, size, color);
                // Хвост ракеты
                guiGraphics.fill(x - 1, y + size, x + 1, y + size + 3, color);
                // Внутренняя точка
                guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, color);
            }
            default -> {
                // Неизвестная цель: квадрат с крестом
                guiGraphics.fill(x - size, y - size, x + size, y + size, color);
                // Крест внутри
                guiGraphics.fill(x - 1, y - size + 1, x + 1, y + size - 1, (color & 0x00FFFFFF) | 0x80000000);
                guiGraphics.fill(x - size + 1, y - 1, x + size - 1, y + 1, (color & 0x00FFFFFF) | 0x80000000);
            }
        }
    }
    
    /**
     * Рисует треугольник вершиной вверх (для самолёта)
     */
    private static void drawTriangleUp(LegacyPose poseStack, int topX, int topY, int leftX, int leftY, int rightX, int rightY, int color) {
        drawTriangle(poseStack, topX, topY, leftX, leftY, rightX, rightY, color);
    }
    
    /**
     * Рисует ромб (для вражеской ракеты)
     */
    private static void drawDiamond(LegacyPose poseStack, int centerX, int centerY, int size, int color) {
        // Ромб из 4 треугольников
        drawTriangle(poseStack, centerX, centerY - size, centerX - size, centerY, centerX, centerY + size, color);
        drawTriangle(poseStack, centerX, centerY - size, centerX + size, centerY, centerX, centerY + size, color);
    }
    
    /**
     * Рисует свои ракеты на радаре с пунктирной линией от центра
     */
    private static void drawMissiles(GuiGraphics guiGraphics, LegacyPose poseStack, int centerX, int centerY, Player player, PantsirClientHandler.PantsirRadarData data) {
        Entity vehicle = player.getVehicle();
        if (vehicle == null) return;
        
        Vec3 radarPos = vehicle.position();
        
        for (PantsirClientHandler.MissilePosition missile : data.missiles) {
            Vec3 missilePos = new Vec3(missile.x, missile.y, missile.z);
            Vec3 toMissile = missilePos.subtract(radarPos);
            double distance = toMissile.horizontalDistance();
            
            if (distance < 1.0) continue; // Слишком близко к центру
            
            double normalizedDist = Math.min(distance / RADAR_RANGE, 1.0);
            
            int radarDist = (int)(normalizedDist * (RADAR_RADIUS - 8));
            int blipX = centerX + (int)(toMissile.x / distance * radarDist);
            int blipY = centerY + (int)(toMissile.z / distance * radarDist);
            
            // Пунктирная линия от центра к ракете
            drawDashedLine(poseStack, centerX, centerY, blipX, blipY, COLOR_MISSILE, 4, 3);
            
            // Своя ракета - маленький квадрат (точка)
            guiGraphics.fill(blipX - 2, blipY - 2, blipX + 2, blipY + 2, COLOR_MISSILE);
        }
    }

    private static void renderStatusPanel(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int vehicleId) {
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(vehicleId);
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        int x = 15;
        int y = 40;
        
        // Фон панели с рамкой
        int panelWidth = 110;
        int panelHeight = 65;
        guiGraphics.fill(x - 5, y - 5, x + panelWidth, y + panelHeight, COLOR_PANEL_BG);
        guiGraphics.fill(x - 6, y - 6, x + panelWidth + 1, y - 5, COLOR_RADAR_GREEN);
        guiGraphics.fill(x - 6, y + panelHeight, x + panelWidth + 1, y + panelHeight + 1, COLOR_RADAR_GREEN);
        guiGraphics.fill(x - 6, y - 5, x - 5, y + panelHeight, COLOR_RADAR_GREEN);
        guiGraphics.fill(x + panelWidth, y - 5, x + panelWidth + 1, y + panelHeight, COLOR_RADAR_GREEN);
        
        // Заголовок с иконкой
        guiGraphics.drawString(mc.font, Component.literal("▌ PANTSIR-S1"), x, y, COLOR_RADAR_GREEN, true);
        y += 14;
        
        // Разделитель
        guiGraphics.fill(x, y, x + 95, y + 1, COLOR_RADAR_GREEN);
        y += 6;
        
        String stateText;
        int stateColor;
        
        switch (data.radarState) {
            case PantsirRadarSyncMessage.STATE_IDLE -> {
                stateText = "◉ SCANNING";
                stateColor = COLOR_RADAR_GREEN;
            }
            case PantsirRadarSyncMessage.STATE_DETECTED -> {
                stateText = "◎ DETECTED";
                stateColor = COLOR_TARGET_YELLOW;
            }
            case PantsirRadarSyncMessage.STATE_LOCKING -> {
                stateText = "◐ LOCKING " + data.lockProgress + "%";
                stateColor = COLOR_TARGET_YELLOW;
            }
            case PantsirRadarSyncMessage.STATE_LOCKED -> {
                stateText = "◆ LOCKED";
                stateColor = COLOR_LOCKED;
            }
            case PantsirRadarSyncMessage.STATE_LOST -> {
                stateText = "◌ LOST";
                stateColor = COLOR_TARGET_RED;
            }
            default -> {
                stateText = "---";
                stateColor = COLOR_RADAR_DARK;
            }
        }
        
        guiGraphics.drawString(mc.font, Component.literal(stateText), x, y, stateColor, true);
        y += 12;
        
        if (data.radarState == PantsirRadarSyncMessage.STATE_LOCKING) {
            int barWidth = 90;
            int barHeight = 6;
            int progress = data.lockProgress;
            int filledWidth = (int)(barWidth * progress / 100.0);
            
            // Рамка прогресс-бара
            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, COLOR_RADAR_GREEN);
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, COLOR_RADAR_DARK);
            
            // Заполнение с градиентом (эффект анимации)
            long time = System.currentTimeMillis();
            float pulse = (float)(Math.sin(time / 100.0) * 0.2 + 0.8);
            int pulseAlpha = (int)(pulse * 255);
            int barColor = (COLOR_TARGET_YELLOW & 0x00FFFFFF) | (pulseAlpha << 24);
            guiGraphics.fill(x, y, x + filledWidth, y + barHeight, barColor);
            
            y += 10;
        }
        
        if (data.targetEntityId != -1 && (data.radarState == PantsirRadarSyncMessage.STATE_DETECTED ||
            data.radarState == PantsirRadarSyncMessage.STATE_LOCKING ||
            data.radarState == PantsirRadarSyncMessage.STATE_LOCKED)) {
            String distText = String.format("RNG: %.0fm", data.targetDistance);
            guiGraphics.drawString(mc.font, Component.literal(distText), x, y, COLOR_RADAR_GREEN, true);
        }
    }

    private static void renderTargetMarker(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int vehicleId) {
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(vehicleId);
        if (data == null) return;
        if (data.radarState != PantsirRadarSyncMessage.STATE_LOCKING
                && data.radarState != PantsirRadarSyncMessage.STATE_LOCKED) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof PantsirS1Entity)) return;

        Vec3 targetPos;
        boolean isSignalLost = data.uiLostSignal;

        Entity targetEntity = data.targetEntityId >= 0 ? mc.level.getEntity(data.targetEntityId) : null;
        if (targetEntity != null && targetEntity.isAlive()) {
            float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            targetPos = new Vec3(
                    Mth.lerp(partialTick, targetEntity.xOld, targetEntity.getX()),
                    Mth.lerp(partialTick, targetEntity.yOld, targetEntity.getY()) + targetEntity.getBbHeight() * 0.5,
                    Mth.lerp(partialTick, targetEntity.zOld, targetEntity.getZ())
            );
        } else if (isSignalLost && data.uiLastTargetPos != null) {
            targetPos = data.uiLastTargetPos;
        } else {
            targetPos = new Vec3(data.targetX, data.targetY, data.targetZ);
        }

        if (targetPos.equals(Vec3.ZERO)) return;

        Vec3 screenPos = VectorToolKt.worldToScreen(targetPos);
        if (screenPos.z <= 0 || !VectorToolKt.canSee(targetPos)) return;

        int x = (int) screenPos.x;
        int y = (int) screenPos.y;

        int color = data.radarState == PantsirRadarSyncMessage.STATE_LOCKING ? COLOR_TARGET_YELLOW : COLOR_LOCKED;
        if (isSignalLost) {
            long time = System.currentTimeMillis();
            float alpha = (float) (Math.sin(time / 200.0) * 0.4 + 0.6);
            int alphaInt = (int) (alpha * 255);
            color = (COLOR_TARGET_RED & 0x00FFFFFF) | (alphaInt << 24);
            if ((time / 300) % 2 != 0) return;
        }

        drawTargetBrackets(guiGraphics, x, y, color);

        if (data.radarState == PantsirRadarSyncMessage.STATE_LOCKED) {
            String distText = String.format("%.0fm", data.targetDistance);
            guiGraphics.drawString(mc.font, Component.literal(distText), x + MARKER_HALF + 5, y - 12, color, false);

            double speed = Math.sqrt(
                    data.targetVelX * data.targetVelX +
                            data.targetVelY * data.targetVelY +
                            data.targetVelZ * data.targetVelZ
            ) * 20;
            guiGraphics.drawString(mc.font, Component.literal(String.format("%.0f m/s", speed)), x + MARKER_HALF + 5, y, color, false);

            double timeToIntercept = data.targetDistance / 160.0;
            guiGraphics.drawString(mc.font, Component.literal(String.format("ETA: %.1fs", timeToIntercept)), x + MARKER_HALF + 5, y + 12, color, false);

            String status = isSignalLost ? "LOST" : "TRK";
            guiGraphics.drawString(mc.font, Component.literal(status), x + MARKER_HALF + 5, y + 24, color, false);
        } else {
            guiGraphics.drawString(mc.font, Component.literal(data.lockProgress + "%"), x + MARKER_HALF + 5, y - 12, color, false);
        }
    }
    
    private static void drawTargetBrackets(GuiGraphics guiGraphics, int cx, int cy, int color) {
        int half = MARKER_HALF;
        int corner = 8;
        int thick = 2;
        
        guiGraphics.fill(cx - half, cy - half, cx - half + corner, cy - half + thick, color);
        guiGraphics.fill(cx - half, cy - half, cx - half + thick, cy - half + corner, color);
        guiGraphics.fill(cx + half - corner, cy - half, cx + half, cy - half + thick, color);
        guiGraphics.fill(cx + half - thick, cy - half, cx + half, cy - half + corner, color);
        guiGraphics.fill(cx - half, cy + half - thick, cx - half + corner, cy + half, color);
        guiGraphics.fill(cx - half, cy + half - corner, cx - half + thick, cy + half, color);
        guiGraphics.fill(cx + half - corner, cy + half - thick, cx + half, cy + half, color);
        guiGraphics.fill(cx + half - thick, cy + half - corner, cx + half, cy + half, color);
    }

    private static void renderControlHint(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int vehicleId) {
        PantsirClientHandler.PantsirRadarData data = PantsirClientHandler.getRadarData(vehicleId);
        if (data == null) return;
        
        Minecraft mc = Minecraft.getInstance();
        LegacyPose poseStack = guiGraphics.pose();
        
        // Используем кнопку VEHICLE_SEEK из SBW
        String keyName = ModKeyMappings.VEHICLE_SEEK.getTranslatedKeyMessage().getString();
        String fireKey = ModKeyMappings.FIRE.getTranslatedKeyMessage().getString();
        String hint = switch (data.radarState) {
            case PantsirRadarSyncMessage.STATE_IDLE -> data.allTargets.isEmpty() 
                ? "Radar scanning..." 
                : "[←/→] Select target";
            case PantsirRadarSyncMessage.STATE_DETECTED -> "[" + keyName + "] Lock | [←/→] Switch";
            case PantsirRadarSyncMessage.STATE_LOCKING -> "[" + keyName + "] Cancel";
            case PantsirRadarSyncMessage.STATE_LOCKED -> "◆ [" + fireKey + "] FIRE | [" + keyName + "] Release";
            case PantsirRadarSyncMessage.STATE_LOST -> "Target lost";
            default -> "";
        };
        
        int color = switch (data.radarState) {
            case PantsirRadarSyncMessage.STATE_LOCKED -> COLOR_LOCKED;
            case PantsirRadarSyncMessage.STATE_DETECTED, PantsirRadarSyncMessage.STATE_LOCKING -> COLOR_TARGET_YELLOW;
            default -> COLOR_RADAR_GREEN;
        };
        
        // Уменьшаем текст в 1.4 раза (scale = 0.71)
        float scale = 0.71f;
        int textWidth = (int)(mc.font.width(hint) * scale);
        int textHeight = (int)(9 * scale);
        int hintX = (screenWidth - textWidth) / 2;
        int hintY = screenHeight - 40;
        
        // Фон для подсказки
        guiGraphics.fill(hintX - 3, hintY - 2, hintX + textWidth + 3, hintY + textHeight + 2, COLOR_PANEL_BG);
        
        poseStack.pushPose();
        poseStack.translate(hintX, hintY, 0);
        poseStack.scale(scale, scale, 1.0f);
        guiGraphics.drawString(mc.font, Component.literal(hint), 0, 0, color, false);
        poseStack.popPose();
    }

    
    // === Drawing helpers ===
    
    private static void drawFilledCircle(LegacyPose poseStack, int cx, int cy, int radius, int color) {
        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
                Matrix4f matrix = poseStack.last().pose();
        
        GuiGraphics.LegacyGuiBuffer buffer = gfx.begin(GuiGraphics.Mode.TRIANGLE_FAN, false);
        buffer.addVertex(matrix, cx, cy, 0).setColor(r, g, b, a);
        
        for (int i = 0; i <= RADAR_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / RADAR_SEGMENTS;
            float x = cx + (float)(Math.cos(angle) * radius);
            float y = cy + (float)(Math.sin(angle) * radius);
            buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
        }
        
        buffer.end();
    }
    
    private static void drawCircleOutline(LegacyPose poseStack, int cx, int cy, float radius, int color) {
        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
                Matrix4f matrix = poseStack.last().pose();
        
        GuiGraphics.LegacyGuiBuffer buffer = gfx.begin(GuiGraphics.Mode.LINE_STRIP, false);
        
        for (int i = 0; i <= RADAR_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / RADAR_SEGMENTS;
            float x = cx + (float)(Math.cos(angle) * radius);
            float y = cy + (float)(Math.sin(angle) * radius);
            buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
        }
        
        buffer.end();
    }
    
    private static void drawLine(LegacyPose poseStack, int x1, int y1, int x2, int y2, int color, int width) {
        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
        LegacyRenderSystem.lineWidth(width);
                Matrix4f matrix = poseStack.last().pose();
        
        GuiGraphics.LegacyGuiBuffer buffer = gfx.begin(GuiGraphics.Mode.LINES, false);
        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.end();
    }
    
    private static void drawTriangle(LegacyPose poseStack, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
                Matrix4f matrix = poseStack.last().pose();
        
        GuiGraphics.LegacyGuiBuffer buffer = gfx.begin(GuiGraphics.Mode.TRIANGLES, false);
        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x3, y3, 0).setColor(r, g, b, a);
        buffer.end();
    }
    
    /**
     * Рисует пунктирную линию
     * @param dashLength длина штриха
     * @param gapLength длина промежутка
     */
    private static void drawDashedLine(LegacyPose poseStack, int x1, int y1, int x2, int y2, int color, int dashLength, int gapLength) {
        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        if (length < 1) return;
        
        double unitX = dx / length;
        double unitY = dy / length;
        LegacyRenderSystem.lineWidth(2);
                Matrix4f matrix = poseStack.last().pose();
        
        GuiGraphics.LegacyGuiBuffer buffer = gfx.begin(GuiGraphics.Mode.LINES, false);
        
        double pos = 0;
        boolean drawing = true;
        
        while (pos < length) {
            if (drawing) {
                double startX = x1 + unitX * pos;
                double startY = y1 + unitY * pos;
                double endPos = Math.min(pos + dashLength, length);
                double endX = x1 + unitX * endPos;
                double endY = y1 + unitY * endPos;
                
                buffer.addVertex(matrix, (float)startX, (float)startY, 0).setColor(r, g, b, a);
                buffer.addVertex(matrix, (float)endX, (float)endY, 0).setColor(r, g, b, a);
                
                pos = endPos;
            } else {
                pos += gapLength;
            }
            drawing = !drawing;
        }
        
        buffer.end();
    }
}
