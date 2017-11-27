
package com.grillecube.client.renderer.model.editor.camera;

import org.lwjgl.glfw.GLFW;

import com.grillecube.client.renderer.camera.CameraPicker;
import com.grillecube.client.renderer.camera.Raycasting;
import com.grillecube.client.renderer.camera.RaycastingCallback;
import com.grillecube.client.renderer.gui.event.GuiEventKeyPress;
import com.grillecube.client.renderer.gui.event.GuiEventMouseScroll;
import com.grillecube.client.renderer.model.editor.gui.GuiModelView;
import com.grillecube.client.renderer.model.editor.mesher.EditableModel;
import com.grillecube.client.renderer.model.editor.mesher.ModelBlockData;
import com.grillecube.client.renderer.model.instance.ModelInstance;
import com.grillecube.common.maths.Maths;
import com.grillecube.common.maths.Matrix4f;
import com.grillecube.common.maths.Vector3f;
import com.grillecube.common.maths.Vector3i;
import com.grillecube.common.maths.Vector4f;
import com.grillecube.common.world.entity.Entity;
import com.grillecube.common.world.entity.collision.Positioneable;
import com.grillecube.common.world.entity.collision.Sizeable;

public class CameraToolPlace extends CameraTool implements Positioneable, Sizeable {

	protected final Vector3i hovered;
	private final Vector3i firstBlock;
	private final Vector3i secondBlock;
	private Vector3i face;
	private int expansion;

	public CameraToolPlace(GuiModelView guiModelView) {
		super(guiModelView);
		this.hovered = new Vector3i();
		this.firstBlock = new Vector3i();
		this.secondBlock = new Vector3i();
		this.expansion = 0;
	}

	@Override
	public void onKeyPress(GuiEventKeyPress<GuiModelView> event) {
		ModelInstance modelInstance = this.guiModelView.getSelectedModelInstance();
		if (modelInstance != null) {
			if (event.getKey() == GLFW.GLFW_KEY_Z) {
				EditableModel model = (EditableModel) modelInstance.getModel();
				if (model != null) {
					int x0 = getX();
					int y0 = getY();
					int z0 = getZ();
					for (int dx = 0; dx < getWidth(); dx++) {
						for (int dy = 0; dy < getHeight(); dy++) {
							for (int dz = 0; dz < getDepth(); dz++) {
								model.setBlockData(new ModelBlockData(x0 + dx, y0 + dy, z0 + dz));
							}
						}
					}
					model.generateMesh();
					this.guiModelView.getToolbox().getSelectedModelPanels().getGuiToolboxModelPanelSkin().refresh();
				}
			} else if (event.getKey() == GLFW.GLFW_KEY_W) {
				this.expand(1);
			} else if (event.getKey() == GLFW.GLFW_KEY_S) {
				this.expand(-1);
			}
		}
	}

	@Override
	public void onMouseMove() {
	}

	@Override
	public void onLeftPressed() {
		this.expansion = 0;
	}

	@Override
	public void onRightPressed() {
		ModelEditorCamera camera = this.getCamera();
		camera.getWindow().setCursor(false);
		float u = this.getBlockSizeUnit();
		float x = 0;
		float y = 0;
		float z = 0;
		this.getCamera().setCenter((x + 0.5f) * u, (y + 0.5f) * u, (z + 0.5f) * u);
		camera.setDistanceFromCenter((float) Vector3f.distance(camera.getCenter(), camera.getPosition()));
	}

	@Override
	public void onRightReleased() {
		this.getCamera().getWindow().setCursor(true);
		this.getCamera().getWindow().setCursorCenter();
	}

	@Override
	public void onLeftReleased() {
		this.expansion = 0;
	}

	private final void updateHoveredBlock() {

		ModelInstance modelInstance = this.guiModelView.getSelectedModelInstance();

		// extract objects
		if (modelInstance == null) {
			return;
		}
		EditableModel model = (EditableModel) modelInstance.getModel();
		Entity entity = modelInstance.getEntity();
		float s = model.getBlockSizeUnit();
		ModelEditorCamera camera = (ModelEditorCamera) this.getCamera();

		// origin relatively to the model
		Vector4f origin = new Vector4f(camera.getPosition(), 1.0f);
		Matrix4f transform = new Matrix4f();
		transform.translate(-entity.getPositionX(), -entity.getPositionY(), -entity.getPositionZ());
		transform.scale(1 / s);
		Matrix4f.transform(transform, origin, origin);

		// ray relatively to the model
		Vector3f ray = new Vector3f();
		CameraPicker.ray(ray, camera, this.guiModelView.getMouseX(), this.guiModelView.getMouseY());

		if (model.getBlockDataCount() > 0) {
			Vector3i pos = new Vector3i();
			Raycasting.raycast(origin.x, origin.y, origin.z, ray.x, ray.y, ray.z, 256.0f, 256.0f, 256.0f,
					new RaycastingCallback() {
						@Override
						public boolean onRaycastCoordinates(int x, int y, int z, Vector3i theFace) {
							// System.out.println(x + " : " + y + " : " + z);
							if (y < model.getMiny() || model.getBlockData(pos.set(x, y, z)) != null) {
								int bx = x + theFace.x;
								int by = y + theFace.y;
								int bz = z + theFace.z;
								hovered.set(bx, by, bz);
								face = theFace;
								return (true);
							}
							return (false);
						}
					});
		} else {
			hovered.set(0, 0, 0);
		}
	}

	@Override
	public void onMouseScroll(GuiEventMouseScroll<GuiModelView> event) {
		if (super.guiModelView.isLeftPressed()) {
			int d = -Maths.sign(event.getScrollY());
			this.expand(d);
		} else {
			float speed = this.getCamera().getDistanceFromCenter() * 0.14f;
			this.getCamera().increaseDistanceFromCenter((float) (-event.getScrollY() * speed));
		}
	}

	private void expand(int d) {
		this.expansion += d;
		this.updateSecondBlock();
	}

	private final void updateSecondBlock() {
		if (this.face == null) {
			this.secondBlock.set(this.hovered);
			return;
		}
		int x = this.hovered.x + this.expansion * this.face.x;
		int y = this.hovered.y + this.expansion * this.face.y;
		int z = this.hovered.z + this.expansion * this.face.z;
		this.secondBlock.set(x, y, z);
	}

	@Override
	public void onUpdate() {
		this.updateCameraRotation();
		this.updateSelection();
	}

	private final void updateSelection() {
		if (!this.guiModelView.isLeftPressed()) {
			this.firstBlock.set(this.hovered);
		}
		this.updateSecondBlock();
		super.guiModelView.getWorldRenderer().getLineRendererFactory().addBox(this, this);
	}

	private final void updateCameraRotation() {
		// rotate
		if (this.guiModelView.isRightPressed()) {
			float pitch = (float) ((this.guiModelView.getPrevMouseY() - this.guiModelView.getMouseY()) * 64.0f);
			this.getCamera().increasePitch(pitch);

			float angle = (float) ((this.guiModelView.getPrevMouseX() - this.guiModelView.getMouseX()) * 128.0f);
			this.getCamera().increaseAngleAroundCenter(angle);

			this.hovered.set(0, 0, 0);
		} else {
			this.updateHoveredBlock();
		}

		float u = this.getBlockSizeUnit();
		float x = 0;
		float y = 0;
		float z = 0;
		this.getCamera().setCenter((x + 0.5f) * u, (y + 0.5f) * u, (z + 0.5f) * u);
	}

	@Override
	public String getName() {
		return ("Place");
	}

	public Vector3i getFirstBlock() {
		return (this.firstBlock);
	}

	public Vector3i getSecondBlock() {
		return (this.secondBlock);
	}

	public Vector3i getFace() {
		return (this.face);
	}

	public final int getX() {
		return (Maths.min(this.firstBlock.x, this.secondBlock.x));
	}

	public final int getY() {
		return (Maths.min(this.firstBlock.y, this.secondBlock.y));
	}

	public final int getZ() {
		return (Maths.min(this.firstBlock.z, this.secondBlock.z));
	}

	public final int getWidth() {
		return (Maths.abs(this.firstBlock.x - this.secondBlock.x) + 1);
	}

	public final int getHeight() {
		return (Maths.abs(this.firstBlock.y - this.secondBlock.y) + 1);
	}

	public final int getDepth() {
		return (Maths.abs(this.firstBlock.z - this.secondBlock.z) + 1);
	}

	@Override
	public float getPositionX() {
		return (this.getX() * this.getBlockSizeUnit());
	}

	@Override
	public float getPositionY() {
		return ((this.getY() + 0.05f) * this.getBlockSizeUnit());
	}

	@Override
	public float getPositionZ() {
		return (this.getZ() * this.getBlockSizeUnit());
	}

	@Override
	public float getPositionVelocityX() {
		return 0;
	}

	@Override
	public float getPositionVelocityY() {
		return 0;
	}

	@Override
	public float getPositionVelocityZ() {
		return 0;
	}

	@Override
	public float getPositionAccelerationX() {
		return 0;
	}

	@Override
	public float getPositionAccelerationY() {
		return 0;
	}

	@Override
	public float getPositionAccelerationZ() {
		return 0;
	}

	@Override
	public void setPositionX(float x) {
	}

	@Override
	public void setPositionY(float y) {
	}

	@Override
	public void setPositionZ(float z) {
	}

	@Override
	public void setPositionVelocityX(float vx) {
	}

	@Override
	public void setPositionVelocityY(float vy) {
	}

	@Override
	public void setPositionVelocityZ(float vz) {
	}

	@Override
	public void setPositionAccelerationX(float ax) {
	}

	@Override
	public void setPositionAccelerationY(float ay) {
	}

	@Override
	public void setPositionAccelerationZ(float az) {
	}

	@Override
	public float getSizeX() {
		return (this.getWidth() * this.getBlockSizeUnit());
	}

	@Override
	public float getSizeY() {
		return (this.getHeight() * this.getBlockSizeUnit());
	}

	@Override
	public float getSizeZ() {
		return (this.getDepth() * this.getBlockSizeUnit());
	}

	@Override
	public float getSizeVelocityX() {
		return 0;
	}

	@Override
	public float getSizeVelocityY() {
		return 0;
	}

	@Override
	public float getSizeVelocityZ() {
		return 0;
	}

	@Override
	public float getSizeAccelerationX() {
		return 0;
	}

	@Override
	public float getSizeAccelerationY() {
		return 0;
	}

	@Override
	public float getSizeAccelerationZ() {
		return 0;
	}

	@Override
	public void setSizeX(float x) {
	}

	@Override
	public void setSizeY(float y) {
	}

	@Override
	public void setSizeZ(float z) {
	}

	@Override
	public void setSizeVelocityX(float vx) {
	}

	@Override
	public void setSizeVelocityY(float vy) {
	}

	@Override
	public void setSizeVelocityZ(float vz) {
	}

	@Override
	public void setSizeAccelerationX(float ax) {
	}

	@Override
	public void setSizeAccelerationY(float ay) {
	}

	@Override
	public void setSizeAccelerationZ(float az) {
	}
}