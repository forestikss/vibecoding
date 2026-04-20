package ru.etc1337.api.draggable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.etc1337.Client;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Hover;

@Data
public class Draggable implements QuickImports, EventListener {

	@Expose
	@SerializedName("x")
	private float x;

	@Expose
	@SerializedName("y")
	private float y;

	public float initX, initY;

	private float startX, startY;
	private boolean dragging;
	private boolean wasJustStartedDragging = false;
	private float width, height;

	// Анимированные позиции для плавного перемещения
	private float renderX, renderY;
	private float targetX, targetY;
	private static final float LERP_SPEED = 0.25f;

	@Expose
	@SerializedName("name")
	private final String name;

	public static Draggable currentDragging = null;
	private static final AutoSmartGrid autoSmartGrid = new AutoSmartGrid();

	public Draggable(String name, float initX, float initY, float width, float height) {
		this.name = name;

		this.x = initX;
		this.y = initY;
		this.initX = initX;
		this.initY = initY;
		this.renderX = initX;
		this.renderY = initY;
		this.targetX = initX;
		this.targetY = initY;

		this.width = width;
		this.height = height;

		Client.getInstance().getDraggableManager().draggables.put(name, this);
		Client.getEventManager().register(this);
	}

	public static void init() {
		autoSmartGrid.updateLineList();
	}

	public void resetPosition() {
		// Удаляем стыковку при сбросе позиции
		autoSmartGrid.removeDockingConnection(this.name);
		this.x = this.initX;
		this.y = this.initY;
	}

	public final void onRender(MatrixStack matrixStack, int mouseX, int mouseY) {
		if (this.width == 0) return;

		if (dragging && mc.currentScreen instanceof ChatScreen) {
			float newX = calculateX(mouseX);
			float newY = calculateY(mouseY);

			if (Math.abs(newX - x) > 2 || Math.abs(newY - y) > 2 || wasJustStartedDragging) {
				x = newX;
				y = newY;
				wasJustStartedDragging = false;
			}

			autoSmartGrid.render();
		}

		// Плавная анимация позиции (lerp к целевой)
		targetX = x;
		targetY = y;
		renderX += (targetX - renderX) * LERP_SPEED;
		renderY += (targetY - renderY) * LERP_SPEED;

		if (!dragging) {
			autoSmartGrid.updateDockedPositions();
		}

		autoSmartGrid.handleDragPositions();
	}

	public final void onClick(int mouseX, int mouseY, int button) {
		Client.getInstance().getDraggableManager().onClick(mouseX, mouseY, button);
		if (button == 0 && Hover.isHovered(x, y, width, height, mouseX, mouseY)) {
			if (currentDragging == null) {
				dragging = true;
				currentDragging = this;
				wasJustStartedDragging = true;
				startX = x - mouseX;
				startY = y - mouseY;

				// Уведомляем сетку о начале перетаскивания
				autoSmartGrid.onDragStart(this);

				//System.out.println("Started dragging " + this.name);
			}
		}
	}

	public final void onRelease(int button) {
		if (button == 0 && dragging) {
			// Уведомляем сетку о завершении перетаскивания
			autoSmartGrid.onDragRelease(this);

			dragging = false;
			wasJustStartedDragging = false;

			if (currentDragging == this) {
				currentDragging = null;
			}

			//System.out.println("Released dragging " + this.name);
		}
	}

	private float calculateX(float mouseX) {
		float newX = mouseX + startX;
		// Ограничиваем в пределах экрана
		return Math.max(0, Math.min(newX, window.getScaledWidth() - width));
	}

	private float calculateY(float mouseY) {
		float newY = mouseY + startY;
		// Ограничиваем в пределах экрана
		return Math.max(0, Math.min(newY, window.getScaledHeight() - height));
	}

	// Методы для работы с умной сеткой
	public static AutoSmartGrid getAutoSmartGrid() {
		return autoSmartGrid;
	}

	// Проверка состыкован ли этот элемент
	public boolean isDocked() {
		return autoSmartGrid.isDocked(this.name);
	}

	// Принудительно удалить стыковку
	public void removeDocking() {
		autoSmartGrid.removeDockingConnection(this.name);
	}

	// Получить информацию о стыковке
	public AutoDockingConnection getDockingInfo() {
		return autoSmartGrid.getDockingInfo(this.name);
	}

	// Проверить, является ли этот элемент частью группы состыкованных элементов
	public boolean isPartOfGroup() {
		// Проверяем, есть ли элементы, которые состыкованы с нами
		for (String otherName : Client.getInstance().getDraggableManager().draggables.keySet()) {
			if (!otherName.equals(this.name)) {
				AutoDockingConnection connection = autoSmartGrid.getDockingInfo(otherName);
				if (connection != null && connection.getTargetName().equals(this.name)) {
					return true;
				}
			}
		}
		return isDocked();
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender2D eventRender2D) {
			double mouseX = mc.mouseHelper.getMouseX() * window.getScaledWidth() / (double) window.getWidth();
			double mouseY = mc.mouseHelper.getMouseY() * window.getScaledHeight() / (double) window.getHeight();
			onRender(eventRender2D.getMatrixStack(), (int) mouseX, (int) mouseY);
		}
	}
}