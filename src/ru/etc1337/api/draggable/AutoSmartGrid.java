package ru.etc1337.api.draggable;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import ru.etc1337.Client;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.draggable.grid.line.GridLine;
import ru.etc1337.api.draggable.grid.line.GridRotationType;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.timer.Timer;

import java.util.*;

@Getter
public class AutoSmartGrid implements QuickImports {
    private final ArrayList<GridLine> lines = new ArrayList<>();
    private final Timer updateTimer = new Timer();

    @Expose
    private final Map<String, AutoDockingConnection> autoDockingConnections = new HashMap<>();

    private GridLine activeVerticalLine;
    private GridLine activeHorizontalLine;
    private Draggable potentialDockingTarget;
    private DockingSide potentialDockingSide;

    // Для группового перемещения
    private Set<String> currentDragGroup = new HashSet<>();
    private Map<String, Float> groupOffsetX = new HashMap<>();
    private Map<String, Float> groupOffsetY = new HashMap<>();

    private static final float DOCKING_THRESHOLD = 0f; // отключено — элементы не цепляются
    private static final float UNDOCK_THRESHOLD = 30f;
    private static final float DOCKING_OFFSET = 2.5f; // Смещение между состыкованными элементами

    public void render() {
        resetRenderState();

        Draggable currentDragging = getCurrentDragging();
        if (currentDragging == null) return;

        float closestXDistance = Float.MAX_VALUE;
        float closestYDistance = Float.MAX_VALUE;
        float closestDockingDistance = Float.MAX_VALUE;

        // Проверка на отстыковку (если тащим далеко от изначальной позиции)
        checkForUndocking(currentDragging);

        // Проверка стыковки с другими элементами
        for (Draggable other : Client.getInstance().getDraggableManager().draggables.values()) {
            if (other == currentDragging || other.isDragging() || isInSameGroup(currentDragging.getName(), other.getName())) {
                continue;
            }

            DockingResult dockingResult = checkDocking(currentDragging, other);
            if (dockingResult.distance < DOCKING_THRESHOLD && dockingResult.distance < closestDockingDistance) {
                closestDockingDistance = dockingResult.distance;
                potentialDockingTarget = other;
                potentialDockingSide = dockingResult.side;
            }
        }

        // Обычная сетка работает только если нет потенциальной стыковки
        if (potentialDockingTarget == null) {
            findClosestGridLines(currentDragging, closestXDistance, closestYDistance);
        }

        if (!updateTimer.finished(300)) return;
        lines.forEach(GridLine::render);
        renderDockingPreview();
    }

    private void resetRenderState() {
        activeHorizontalLine = null;
        activeVerticalLine = null;
        potentialDockingTarget = null;
        potentialDockingSide = null;
    }

    private void findClosestGridLines(Draggable currentDragging, float closestXDistance, float closestYDistance) {
        for (GridLine line : lines) {
            float[] distanceData = calculateDistance(currentDragging, line);
            float distance = distanceData[0];

            if (line.getRotationType() == GridRotationType.HORIZONTAL) {
                if (distance < 5 && distance < closestYDistance) {
                    closestYDistance = distance;
                    activeHorizontalLine = line;
                }
            } else {
                if (distance < 5 && distance < closestXDistance) {
                    closestXDistance = distance;
                    activeVerticalLine = line;
                }
            }
        }
    }

    public void handleDragPositions() {
        Draggable currentDragging = getCurrentDragging();
        if (currentDragging == null) return;

        // Приоритет стыковке над сеткой
        if (potentialDockingTarget != null) {
            applyDocking(currentDragging, potentialDockingTarget, potentialDockingSide);
            return;
        }

        // Обычное выравнивание по сетке
        applyGridAlignment(currentDragging);
    }

    private void applyGridAlignment(Draggable currentDragging) {
        if (activeHorizontalLine != null) {
            float[] data = calculateDistance(currentDragging, activeHorizontalLine);
            if (data[0] < 5) {
                currentDragging.setY(activeHorizontalLine.getCoordinate() - data[1]);
                updateGroupPositions(currentDragging);
            }
        }

        if (activeVerticalLine != null) {
            float[] data = calculateDistance(currentDragging, activeVerticalLine);
            if (data[0] < 5) {
                currentDragging.setX(activeVerticalLine.getCoordinate() - data[1]);
                updateGroupPositions(currentDragging);
            }
        }
    }

    public void onDragStart(Draggable draggable) {
        // Определяем группу элементов для перемещения
        currentDragGroup.clear();
        groupOffsetX.clear();
        groupOffsetY.clear();

        buildDragGroup(draggable.getName(), draggable.getX(), draggable.getY());

        // Если элемент был состыкован, удаляем его из стыковки
        if (isDocked(draggable.getName())) {
            //System.out.println("Starting drag - removing docking for " + draggable.getName());
        }
    }

    private void buildDragGroup(String draggableName, float rootX, float rootY) {
        if (currentDragGroup.contains(draggableName)) return;

        Draggable draggable = Client.getInstance().getDraggableManager().draggables.get(draggableName);
        if (draggable == null) return;

        currentDragGroup.add(draggableName);
        groupOffsetX.put(draggableName, draggable.getX() - rootX);
        groupOffsetY.put(draggableName, draggable.getY() - rootY);

        // Находим все элементы, которые состыкованы с текущим
        for (Map.Entry<String, AutoDockingConnection> entry : autoDockingConnections.entrySet()) {
            if (entry.getValue().getTargetName().equals(draggableName)) {
                buildDragGroup(entry.getKey(), rootX, rootY);
            }
            if (entry.getKey().equals(draggableName)) {
                buildDragGroup(entry.getValue().getTargetName(), rootX, rootY);
            }
        }
    }

    private void updateGroupPositions(Draggable rootDraggable) {
        if (currentDragGroup.size() <= 1) return;

        float rootX = rootDraggable.getX();
        float rootY = rootDraggable.getY();

        for (String memberName : currentDragGroup) {
            if (memberName.equals(rootDraggable.getName())) continue;

            Draggable member = Client.getInstance().getDraggableManager().draggables.get(memberName);
            if (member != null && !member.isDragging()) {
                float offsetX = groupOffsetX.getOrDefault(memberName, 0f);
                float offsetY = groupOffsetY.getOrDefault(memberName, 0f);

                member.setX(rootX + offsetX);
                member.setY(rootY + offsetY);
            }
        }
    }

    public void onDragRelease(Draggable draggable) {
        // Создаем новую стыковку если есть потенциальная цель
        if (potentialDockingTarget != null && potentialDockingSide != null) {
            createAutoDockingConnection(draggable, potentialDockingTarget, potentialDockingSide);
        }

        // Очищаем группу перемещения
        currentDragGroup.clear();
        groupOffsetX.clear();
        groupOffsetY.clear();
    }

    private void checkForUndocking(Draggable dragging) {
        AutoDockingConnection connection = autoDockingConnections.get(dragging.getName());
        if (connection == null) return;

        Draggable target = Client.getInstance().getDraggableManager().draggables.get(connection.getTargetName());
        if (target == null) return;

        // Вычисляем где должен быть элемент если бы он был состыкован
        float expectedX = 0, expectedY = 0;
        switch (connection.getSide()) {
            case RIGHT:
                expectedX = target.getX() + target.getWidth() + DOCKING_OFFSET + connection.getOffsetX();
                expectedY = target.getY() + connection.getOffsetY();
                break;
            case LEFT:
                expectedX = target.getX() - dragging.getWidth() - DOCKING_OFFSET - connection.getOffsetX();
                expectedY = target.getY() + connection.getOffsetY();
                break;
            case BOTTOM:
                expectedX = target.getX() + connection.getOffsetX();
                expectedY = target.getY() + target.getHeight() + DOCKING_OFFSET + connection.getOffsetY();
                break;
            case TOP:
                expectedX = target.getX() + connection.getOffsetX();
                expectedY = target.getY() - dragging.getHeight() - DOCKING_OFFSET - connection.getOffsetY();
                break;
        }

        // Если элемент сильно отклонился от ожидаемой позиции, отстыковываем
        float distance = (float) Math.sqrt(Math.pow(dragging.getX() - expectedX, 2) + Math.pow(dragging.getY() - expectedY, 2));
        if (distance > UNDOCK_THRESHOLD) {
            removeDockingConnection(dragging.getName());
        }
    }

    private void createAutoDockingConnection(Draggable draggable, Draggable target, DockingSide side) {
        // Удаляем старую стыковку если была
        removeDockingConnection(draggable.getName());

        float offsetX = 0, offsetY = 0;

        switch (side) {
            case RIGHT:
                offsetX = draggable.getX() - (target.getX() + target.getWidth() + DOCKING_OFFSET);
                offsetY = draggable.getY() - target.getY();
                break;
            case LEFT:
                offsetX = (target.getX() - DOCKING_OFFSET) - (draggable.getX() + draggable.getWidth());
                offsetY = draggable.getY() - target.getY();
                break;
            case BOTTOM:
                offsetX = draggable.getX() - target.getX();
                offsetY = draggable.getY() - (target.getY() + target.getHeight() + DOCKING_OFFSET);
                break;
            case TOP:
                offsetX = draggable.getX() - target.getX();
                offsetY = (target.getY() - DOCKING_OFFSET) - (draggable.getY() + draggable.getHeight());
                break;
        }

        autoDockingConnections.put(draggable.getName(),
                new AutoDockingConnection(target.getName(), side, offsetX, offsetY));

        //System.out.println("Auto-docked " + draggable.getName() + " to " + target.getName() + " on " + side);
    }

    public void removeDockingConnection(String draggableName) {
        if (autoDockingConnections.remove(draggableName) != null) {
            //System.out.println("Removed docking for " + draggableName);
        }
    }

    public void updateDockedPositions() {
        Set<String> processed = new HashSet<>();

        for (Map.Entry<String, AutoDockingConnection> entry : autoDockingConnections.entrySet()) {
            String draggableName = entry.getKey();
            if (processed.contains(draggableName)) continue;

            Draggable draggable = Client.getInstance().getDraggableManager().draggables.get(draggableName);
            if (draggable == null || draggable.isDragging()) continue;

            Draggable target = Client.getInstance().getDraggableManager().draggables.get(entry.getValue().getTargetName());
            if (target == null) continue;

            updateDockedPosition(draggable, target, entry.getValue());
            processed.add(draggableName);
        }
    }

    private void updateDockedPosition(Draggable draggable, Draggable target, AutoDockingConnection connection) {
        DockingSide side = connection.getSide();
        float offsetX = connection.getOffsetX();
        float offsetY = connection.getOffsetY();

        switch (side) {
            case RIGHT:
                draggable.setX(target.getX() + target.getWidth() + DOCKING_OFFSET + offsetX);
                // Синхронизируем Y-координату для горизонтальной стыковки
                draggable.setY(target.getY() + offsetY);
                break;
            case LEFT:
                draggable.setX(target.getX() - draggable.getWidth() - DOCKING_OFFSET - offsetX);
                // Синхронизируем Y-координату для горизонтальной стыковки
                draggable.setY(target.getY() + offsetY);
                break;
            case BOTTOM:
                draggable.setX(target.getX() + offsetX);
                draggable.setY(target.getY() + target.getHeight() + DOCKING_OFFSET + offsetY);
                break;
            case TOP:
                draggable.setX(target.getX() + offsetX);
                draggable.setY(target.getY() - draggable.getHeight() - DOCKING_OFFSET - offsetY);
                break;
        }
    }

    private DockingResult checkDocking(Draggable dragging, Draggable target) {
        float dragX = dragging.getX();
        float dragY = dragging.getY();
        float dragW = dragging.getWidth();
        float dragH = dragging.getHeight();

        float targetX = target.getX();
        float targetY = target.getY();
        float targetW = target.getWidth();
        float targetH = target.getHeight();

        // Проверка стыковки справа от target
        float rightDistance = Math.abs(dragX - (targetX + targetW + DOCKING_OFFSET));
        float rightYOverlap = Math.max(0, Math.min(dragY + dragH, targetY + targetH) - Math.max(dragY, targetY));
        if (rightDistance < DOCKING_THRESHOLD && rightYOverlap > Math.min(dragH, targetH) * 0.2f) {
            return new DockingResult(rightDistance, DockingSide.RIGHT);
        }

        // Проверка стыковки слева от target
        float leftDistance = Math.abs((dragX + dragW) - (targetX - DOCKING_OFFSET));
        float leftYOverlap = Math.max(0, Math.min(dragY + dragH, targetY + targetH) - Math.max(dragY, targetY));
        if (leftDistance < DOCKING_THRESHOLD && leftYOverlap > Math.min(dragH, targetH) * 0.2f) {
            return new DockingResult(leftDistance, DockingSide.LEFT);
        }

        // Проверка стыковки снизу от target
        float bottomDistance = Math.abs(dragY - (targetY + targetH + DOCKING_OFFSET));
        float bottomXOverlap = Math.max(0, Math.min(dragX + dragW, targetX + targetW) - Math.max(dragX, targetX));
        if (bottomDistance < DOCKING_THRESHOLD && bottomXOverlap > Math.min(dragW, targetW) * 0.2f) {
            return new DockingResult(bottomDistance, DockingSide.BOTTOM);
        }

        // Проверка стыковки сверху от target
        float topDistance = Math.abs((dragY + dragH) - (targetY - DOCKING_OFFSET));
        float topXOverlap = Math.max(0, Math.min(dragX + dragW, targetX + targetW) - Math.max(dragX, targetX));
        if (topDistance < DOCKING_THRESHOLD && topXOverlap > Math.min(dragW, targetW) * 0.2f) {
            return new DockingResult(topDistance, DockingSide.TOP);
        }

        return new DockingResult(Float.MAX_VALUE, null);
    }

    private void applyDocking(Draggable dragging, Draggable target, DockingSide side) {
        float newX = dragging.getX();
        float newY = dragging.getY();

        switch (side) {
            case RIGHT:
                newX = target.getX() + target.getWidth() + DOCKING_OFFSET;
                // Для горизонтальной стыковки синхронизируем Y-координату
                newY = target.getY();
                break;
            case LEFT:
                newX = target.getX() - dragging.getWidth() - DOCKING_OFFSET;
                // Для горизонтальной стыковки синхронизируем Y-координату
                newY = target.getY();
                break;
            case BOTTOM:
                newY = target.getY() + target.getHeight() + DOCKING_OFFSET;
                newX = Math.max(target.getX() - dragging.getWidth() + 10,
                        Math.min(dragging.getX(), target.getX() + target.getWidth() - 10));
                break;
            case TOP:
                newY = target.getY() - dragging.getHeight() - DOCKING_OFFSET;
                newX = Math.max(target.getX() - dragging.getWidth() + 10,
                        Math.min(dragging.getX(), target.getX() + target.getWidth() - 10));
                break;
        }

        dragging.setX(newX);
        dragging.setY(newY);
        updateGroupPositions(dragging);
    }

    private void renderDockingPreview() {
        if (potentialDockingTarget == null) return;
        // Здесь можно добавить визуальную подсветку
    }

    private Draggable getCurrentDragging() {
        return Client.getInstance().getDraggableManager().draggables.values()
                .stream()
                .filter(Draggable::isDragging)
                .findFirst()
                .orElse(null);
    }

    private float[] calculateDistance(Draggable draggable, GridLine line) {
        boolean isHorizontal = line.getRotationType() == GridRotationType.HORIZONTAL;
        float coord = isHorizontal ? draggable.getY() : draggable.getX();
        float size = isHorizontal ? draggable.getHeight() : draggable.getWidth();

        float dist = Math.abs(coord - line.getCoordinate());
        float distHalf = Math.abs(coord + (size / 2) - line.getCoordinate());
        float distFull = Math.abs(coord + size - line.getCoordinate());

        float minDist = Math.min(dist, Math.min(distHalf, distFull));
        float offset = (minDist == distHalf) ? size / 2 : (minDist == distFull) ? size : 0;

        return new float[] { minDist, offset };
    }

    // Вспомогательные методы
    private boolean isInSameGroup(String name1, String name2) {
        return currentDragGroup.contains(name1) && currentDragGroup.contains(name2);
    }

    public void addHorizontalLine(float y) {
        lines.add(new GridLine(y, GridRotationType.HORIZONTAL));
    }

    public void addVerticalLine(float x) {
        lines.add(new GridLine(x, GridRotationType.VERTICAL));
    }

    public void updateLineList() {
        updateTimer.reset();
        lines.clear();

        float centerX = (float) window.getScaledWidth() / 2 - 0.5f;
        float centerY = (float) window.getScaledHeight() / 2 - 0.5f;

        addHorizontalLine(centerY);
        addVerticalLine(centerX);
        addHorizontalLine(6);
        addHorizontalLine(window.getScaledHeight() - 7);
        addVerticalLine(6);
        addVerticalLine(window.getScaledWidth() - 7);
    }

    public boolean isDocked(String draggableName) {
        return autoDockingConnections.containsKey(draggableName);
    }

    public AutoDockingConnection getDockingInfo(String draggableName) {
        return autoDockingConnections.get(draggableName);
    }

    private static class DockingResult {
        float distance;
        DockingSide side;

        DockingResult(float distance, DockingSide side) {
            this.distance = distance;
            this.side = side;
        }
    }
}